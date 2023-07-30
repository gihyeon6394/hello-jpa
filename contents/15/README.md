# 15. 고급 주제와 성능 최적화

1. 예외 처리
2. 엔티티 비교
3. 프록시 심화 주제
4. 성능 최적화
5. 정리

---

## 1. 예외 처리

### 1.1 JPA 표준 예외 정리

- `RuntimeException`의 자손 `javax.persistence.PersistenceException`
- JPA 표준 예외 : `javax.persistence.PersistenceException`의 자손들
    - 트랜잭션 Rollback 예외 : 심각한 예외, 복구하면 안됨, commit하려해도 Rollback됨
    - 트랜잭션 Rollback을 표시하지 않는 예외 : 개발자가 Rollback 여부를 선택할 수 있음

| 트랜잭션 Rollback 예외                                 | Description                                                 |
|--------------------------------------------------|-------------------------------------------------------------|
| `javax.persistence.EntityExistsException`        | `persist()` 호출 시 이미 DB에 같은 식별자를 가진 엔티티가 있으면 발생              |
| `javax.persistence.EntityNotFoundException`      | `getReference()` 호출 시 식별자에 해당하는 엔티티가 없으면 발생                 |
| `javax.persistence.OptimisticLockException`      | 낙관적 락 충돌 시 발생                                               |
| `javax.persistence.PessimisticLockException`     | 비관적 락 충돌 시 발생                                               |
| `javax.persistence.RollbackException`            | `commit()` 실패 시 발생                                          |
| `javax.persistence.TransactionRequiredException` | 트랜잭션이 필요한 상황에서 트랜잭션이 없으면 발생<br/>(주로 트랜잭션 없이 Entity 수정 시 발생) |

| 트랜잭션 Rollback을 표시하지 않는 예외                    | Description                            |
|----------------------------------------------|----------------------------------------|
| `javax.persistence.NoResultException`        | `getSingleResult()` 호출 시 결과가 없으면 발생    |
| `javax.persistence.NonUniqueResultException` | `getSingleResult()` 호출 시 결과가 둘 이상이면 발생 |
| `javax.persistence.LockTimeoutException`     | 비관적 락 시도 시 대기 시간 초과하면 발생               |
| `javax.persistence.QueryTimeoutException`    | 쿼리 시간 초과하면 발생                          |

### 1.2 Spring Framework의 JPA 예외 반환

- Spring Framework는 JPA 예외를 Spring 예외로 변환해서 반환
    - 서비스 계층이 데이터 접근 기술 (JPA, Mybatis 등)에 의존성이 높으면 안됨

| JPA 예외                                           | Spring 예외                                                        |
|--------------------------------------------------|------------------------------------------------------------------|
| `javax.persistence.PersistenceException`         | `org.springframework.orm.jpa.JpaSystemException`                 |
| `javax.persistence.NoResultException`            | `org.springframework.dao.EmptyResultDataAccessException`         |
| `javax.persistence.NonUniqueResultException`     | `org.springframework.dao.IncorrectResultSizeDataAccessException` |
| `javax.persistence.LockTimeoutException`         | `org.springframework.dao.CannotAcquireLockException`             |
| `javax.persistence.QueryTimeoutException`        | `org.springframework.dao.QueryTimeoutException`                  |
| `javax.persistence.EntityExistsException`        | `org.springframework.dao.DataViolationException`                 |
| `javax.persistence.EntityNotFoundException`      | `org.springframework.dao.DataRetrievalFailureException`          |
| `javax.persistence.OptimisticLockException`      | `org.springframework.dao.OptimisticLockingFailureException`      |
| `javax.persistence.PerssimisticLockException`    | `org.springframework.dao.PessimisticLockingFailureException`     |
| `javax.persistence.TransactionRequiredException` | `org.springframework.dao.InvalidDataAccessApiUsageException`     |                           
| `javax.persistence.RollbackException`            | `org.springframework.transaction.TransactionSystemException`     |                              

### 1.3 Spring Framework에 JPA 예외 변환기 적용

- `PersistenceExceptionTranslationPostProcessor` 빈 등록
    - `@Repository` 애노테이션이 붙은 빈에 변환기 AOP 적용
    - `@Repository` 애노테이션이 붙은 빈은 JPA 예외를 Spring 예외로 변환

```java

@Repository
public class MemberRepository {
    @PersistenceContext
    private EntityManager em;

    public Member findById(Long id) {
        return em.createQuery("select m from Member m where m.id = :id", Member.class)
                .setParameter("id", id)
                .getSingleResult();
        /**
         * getSingleResult() 호출 시 결과가 없으면 NoResultException 발생
         * findById()를 빠져나가면서 EmptyResultDataAccessException으로 변환
         * */
    }
}
```

### 1.4 트랜잭션 Rollback 시 주의사항

- 트랜잭션을 Rollback해도 영속성 컨텍스트의 데이터는 Rollback되지 않음
- 따라서, 트랜잭션이 Rollback 되었으면 영속성 컨텍스트를 초기화한 다음 사용해야 함 (`EntityManager.clear()`)
- 트랜잭션 당 영속성 컨텍스트 전략
    - 트랜잭션 AOP 종료 시점에서 트랜잭션을 Rollback하면서 영속성 컨텍스트도 같이 종료됨
- OSIV 전략
    - 하나의 영속성 컨텍스트에 여러 트랜잭션이 있음
    - 트랜잭션 하나가 Rollback해도 영속성 컨텍스트는 유지되는 문제
    - 따라서 Spring은 영속성 컨텍스트의 범위가 트랜잭션보다 넓을 때,
        - 하나의 트랜잭션에서 Rollback시 영속성 컨텍스트를 초기화해서 문제를 해결함 (`EntityManager.clear()`)
    - `org.springframework.orm.jpa.JpaTransactionManger`의 `doRollback()` 참고

## 2. 엔티티 비교

1차 캐시를 활용한 **애플리케이션 수준의 반복 가능한 읽기**

````
Member karina1 = memberRepository.findById('karina001`);
Member karina2 = memberRepository.findById('karina001`);

System.out.println(karina1 == karina2); // true
````

### 2.1 영속성 컨텍스트가 같을 떄 Entity 비교



## 3. 프록시 심화 주제

## 4. 성능 최적화

## 5. 정리