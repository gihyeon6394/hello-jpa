# 10. 객체지향 쿼리 언어

1. 객체지향 쿼리 소개
2. JPQL
3. Criteria
4. QueryDSL
5. 네이티브 SQL
6. 객체지향 쿼리 심화
7. 정리

---

## 1. 객체지향 쿼리 소개

- 테이블이 아닌 객체를 대상으로 검색하는 객체 지향 쿼리
- SQL을 추상화하여 DBMS 벤더에 의존하지 않음
- JPA가 지원하는 검색 방법
    - **JPQL, Java Persistence Query Language**
    - Criteria 쿼리 : JPQL을 편하게 작성하도록 도와주는 API
    - Native SQL : JPQL 대신 직접 SQL 사용
    - QueryDSL : Criteria 같이 JPQL을 편하게 작성하도록 도와주는 빌더 클래스, 비표준 오픈소스 프레임워크
    - JDBC API 직접 사용, Mybatis와 같은 SQL 매퍼 프레임워크 사용

### 1.1 JPQL 소개

- Entity 객체를 조회하는 개체지향 쿼리
- SQL을 추상화하여 특정 DBMS에 의존하지 않음
    - SQL 함수가 DBMS 별로 달라도 JPQL은 같음
- SQL보다 간결

````
Stirng jpql = "select m from IdolMember as m where m.memberName = '카리나'";
List<IdolMember> members = em.createQuery(jpql, IdolMember.class).getResultList();

-- 실제 sql
select m.member_id, m.member_name, m.team_id
from idol_member m
where m.member_name = '카리나';

````

### 1.2 Criteria 소개

- JPQL을 편하게 작성하도록 도와주는 API, JPQL 빌더
- 문자가 아닌 자바 코드로 JPQL 작성 가능
- 문자열 기반 쿼리는 런타임 시점에서 오류를 발견함
- 장점
    - 컴파일 시점에서 오류를 발견
    - IDE 자동완성 지원
    - 동적 쿼리 작성 용이
- 단점
    - 복잡하고 장황
    - 사용이 불편
    - 가독성이 좋지 않음

````
CriteriaBuilder cb = em.getCriteriaBuilder();
CriteriaQuery<IdolMember> query = cb.createQuery(IdolMember.class);

Root<IdolMember> m = query.from(IdolMember.class);

CriteriaQuery<IdolMember> cq = query.select(m).where(cb.equal(m.get("memberName"), "카리나"));
List<IdolMember> members = em.createQuery(cq).getResultList();
````

### 1.3 QueryDSL 소개 (김영한 님이 더 선호)

- Criteria 보다 간단한 빌더 클래스
- 오픈소스 프로젝트이나 spring data project가 지원 중

````
JPAQuery query = new JPAQuery(em);
QIdolMember m = QIdolMember.idolMember;

List<IdolMember> members = query.from(m)
                          .where(m.memberName.eq("카리나"))
                          .list(m);
````

- `QIdolMember` : `IdolMember` 클래스를 기반으로 생성한 QueryDSL 전용 클래스

### 1.4 네이티브 SQL 소개

- JPA가 제공하는 SQL을 직접 사용하는 기능
- 특정 DBMS에만 존재하는 함수 사용 시 유용
- 단점 : DBMS 의존적이라, DBMS 변경 시 SQL 수정 필요

````
String sql = "SELECT MEMBER_ID, MEMBER_NAME, TEAM_ID FROM IDOL_MEMBER WHERE MEMBER_NAME = '카리나'";
List<IdolMember> members = em.createNativeQuery(sql, IdolMember.class).getResultList();
````

### 1.5 JDBC 직접 사용, myBatis 같은 SQL 매퍼 프레임워크 사용

````
Session sess = em.unwrap(Session.class);
sess.doWork(new Work() {
    @Override
    public void execute(Connection connection) throws SQLException {
        // JDBC API 직접 사용 가능
    }
});
````

- JPA를 우회해서 DBMS에 접근하는 방법
    - = **JPA는 어떤 데이터를 수정하는지 모름**
- 영속성 컨텍스트를 적절한 시점에 강제로 `flush()` 해야 함
    - 그렇지 않으면, 영속성 컨텍스트의 데이터와 DB의 데이터가 다를 수 있음
- Spirng AOP를 이용해 트랜잭션을 시작하고 종료할 때 flush()를 호출하도록 할 수 있음

## 2. JPQL

<img src="img.png" width="90%">

### 2.1 기본 문법과 쿼리 API

````
select statement :: = 
    select_clause
    from_clause
    [where_clause]
    [groupby_clause]
    [having_clause]
    [orderby_clause]
    
update statement :: = 
    update_clause
    [where_clause]

delete statement :: =
    delete_clause
    [where_clause]
````

#### `SELECT` statement

````sql
SELECT m
FROM Member AS m
WHERE m.username = '카리나'
````

- 대소문자 구분 : Entity, 속성은 대소문자 구분
- Entity 이름 : `Member`는 Entity 이름, `@Entity(name = "Member")`로 지정한 값
- 별칭 필수, JPA에서는 identification variable이라 함

#### `TypeQuery`, `Query`

- `TypeQuery` : 반환 타입이 명확할 때 사용
- `Query` : 반환 타입이 명확하지 않을 때 사용

````
TypedQuery<Member> query = em.createQuery("SELECT m FROM Member m", Member.class);
List<Member> members = query.getResultList();

Query query = em.createQuery("SELECT m.username, m.age FROM Member m");
List resultList = query.getResultList();
````

#### 결과 조회

- `query.getResultList()` : 결과가 하나 이상일 때, 리스트 반환
- `query.getSingleResult()` : 결과가 정확히 하나, 단일 객체 반환
    - 결과가 없으면 : `javax.persistence.NoResultException` 발생
    - 둘 이상이면 : `javax.persistence.NonUniqueResultException` 발생

### 2.2 파라미터 바인딩

- named parameter가 position parameter보다 명확
- 파라미터 바인딩의 장점
    - JPA가 JPQL을 parsing하여 재사용
    - DBMS 내부에서 SQL을 parsing하여 재사용
- **바인딩이 아닌 직접 문자열로 SQL을 작성하면 안됨**
    - SQL 작성은 SQL Injection 공격에 취약

#### 이름 기준 파라미터 바인딩, named parameter

````
String usernameParam = "카리나";
TypedQuery<Member> query = em.createQuery("SELECT m FROM Member m WHERE m.username = :username", Member.class);
query.setParameter("username", usernameParam);
List<Member> resultList = query.getResultList();
````

````
// 메서드 체인 방식

List<Member> resultList = em.createQuery("SELECT m FROM Member m WHERE m.username = :username", Member.class)
                            .setParameter("username", usernameParam)
                            .getResultList();
````

#### 위치 기준 파라미터, positional parameter

````
List<Member> resultList = em.createQuery("SELECT m FROM Member m WHERE m.username = ?1", Member.class)
                              .setParameter(1, usernameParam);
````

## 3. Criteria

## 4. QueryDSL

## 5. 네이티브 SQL

## 6. 객체지향 쿼리 심화

## 7. 정리
