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

### 2.3 프로젝션, projection

- SELECT 절에 조회할 대상을 지정하는 것
- 프로젝션 대상 : 엔티티, 임베디드 타입, 스칼라 타입

#### 엔티티 프로젝션

```sql
SELECT m
FROM Member m;

SELECT m.team
FROM Member m;
````

#### 임베디드 타입 프로젝션

- 임베디드 타입은 값 타입이므로 엔티티와 다르게 조회 결과를 영속성 컨텍스트에서 관리하지 않음

```sql
-- JPQL
SELECT o.address
FROM Order o;

-- 실제 SQL
SELECT o.zipcode, o.street, o.city
FROM Orders o;
````

#### 스칼라 타입 프로젝션

스칼라 타입 : 숫자, 문자, 날짜

````
List<String> resultList = em.createQuery("SELECT m.username FROM Member m", String.class)
                              .getResultList();
````

#### 여러 값 조회

- 필요한 데이터만 조회할 때
- Query 타입으로 조회

````
List<Object[]> resultList = em.createQuery("SELECT m.username, m.age FROM Member m")
                    .getResultList();
````

#### `new` 명령어

- `Object[]` 타입 말고, DTO로 바로 조회

```java
public class UserDTO {
    private String username;
    private int age;

    public UserDTO(String username, int age) {
        this.username = username;
        this.age = age;
    }
}

public class Foo {
    public void selectDTO(EntityManager em) {
        List<UserDTO> resultList = em.createQuery("SELECT new jpabook.jpql.UserDTO(m.username, m.age) FROM Member m", UserDTO.class)
                .getResultList();
    }
}
````

### 2.4 페이징 API

- `setFirstResult(int startPosition)` : 조회 시작 위치
- `setMaxResults(int maxResult)` : 조회할 데이터 수
- API를 DB 방언에 맞게 변환해서 호출

````
// 11번부터 30번까지 조회 (20개)
TypedQuery<Member> query = em.createQuery("SELECT m FROM Member m ORDER BY m.age DESC", Member.class)
                              .setFirstResult(10)
                              .setMaxResults(20);
````

```sql
-- 실제 sql (오라클)
SELECT *
FROM (SELECT ROW_.*, ROWNUM ROWNUM_
      FROM (SELECT M.ID AS ID1, M.AGE AS AGE2, M.TEAM_ID AS TEAM_ID3, M.NAME AS NAME4
            FROM MEMBER M
            ORDER BY M.AGE DESC) ROW_
      WHERE ROWNUM <= ?)
    )
WHERE ROWNUM_ > ?
```

### 2.5 집합과 정렬

#### `GROUP BY`, `HAVING`, `ORDER BY`

sql과 동일

### 2.6 JPQL 조인

#### 내부 조인

````
String teamName = "Aespa";
String query  = "SELECT m FROM Member m INNER JOIN m.team t WHERE t.name = :teamName";
List<Member> resultList = em.createQuery(query, Member.class)
                              .setParameter("teamName", teamName)
                              .getResultList();
````

````sql
-- JPQL
SELECT m
FROM Member m
         INNER JOIN m.team t -- Entity 연관관계를 활용함
WHERE t.name = :teamName;

-- 실제 SQL
SELECT M.ID AS ID, M.AGE AS AGE, M.TEAM_ID AS TEAM_ID, M.NAME AS NAME
FROM MEMBER M
         INNER JOIN TEAM T ON M.TEAM_ID = T.ID
WHERE T.NAME = ?
````

#### 외부 조인

```sql
-- JPQL
SELECT m
FROM Member m
         LEFT JOIN m.team t
WHERE t.name = :teamName;

-- 실제 SQL
SELECT M.ID AS ID, M.AGE AS AGE, M.TEAM_ID AS TEAM_ID, M.NAME AS NAME
FROM MEMBER M
         LEFT OUTER JOIN TEAM T ON M.TEAM_ID = T.ID
WHERE T.NAME = ?
````

#### 컬렉션 조인

1:N, N:M 같은 컬렉션을 조인할 때 사용

```sql
-- JPQL
SELECT t, m
FROM Team t
         LEFT JOIN t.members m;

-- 실제 SQL
SELECT T.ID AS ID, T.NAME AS NAME, M.ID AS ID1, M.AGE AS AGE, M.TEAM_ID AS TEAM_ID, M.NAME AS NAME1
FROM TEAM T
         LEFT OUTER JOIN MEMBER M ON T.ID = M.TEAM_ID
```

#### 세타 조인

- 내부 조인만 지원
- 전혀 관계 없는 엔티티를 조인할 때 사용

```sql
-- JPQL
SELECT COUNT(m)
FROM Member m,
     Team t
WHERE m.username = t.name;

-- 실제 SQL
SELECT COUNT(M.ID)
FROM MEMBER M,
     TEAM T
WHERE M.USERNAME = T.NAME
```

#### JOIN ON 절 (JPA 2.1부터 지원)

- 내부 JOIN의 `ON` 절은 `WHERE` 절과 같음
- 외부 JOIN 시 주로 사용
    - 조인 대상을 필터링하고 조인

```sql
-- JPQL
select m, t
from Member m
         left join m.team t on t.name = 'Aespa';

-- 실제 SQL
SELECT M.ID AS ID, M.AGE AS AGE, M.TEAM_ID AS TEAM_ID, M.NAME AS NAME, T.ID AS ID1, T.NAME AS NAME1
FROM MEMBER M
         LEFT OUTER JOIN TEAM T ON T.NAME = 'Aespa';
```

### 2.7 페치 조인, FETCH JOIN

- 연관된 Entity나 Collection을 SQL 한 번에 함께 조회하는 기능
- 별칭 사용 불가
    - hibernate는 가능

````
fetch jokin ::== [LEFT [OUTER] | INNER] JOIN FETCH 조인경로
````

#### Entity FETCH JOIN

- 지연 로딩을 설정해도 `fetch join`에는 즉시 로딩함

```sql
-- JPQL
SELECT m
FROM Member m
         JOIN FETCH m.team;

-- 실제 SQL
-- 지연 로딩 없음
SELECT M.*, T.*
FROM MEMBER M
         INNER JOIN TEAM T ON M.TEAM_ID = T.ID;
```

````
String jpql = "SELECT m FROM Member m JOIN FETCH m.team";
List<Member> members = em.createQuery(jpql, Member.class).getResultList();
````

#### Collection FETCH JOIN

1:N 관계

```sql
-- JPQL
select t
from Team t
         join fetch t.members
where t.name = 'Aespa';

-- 실제 SQL
SELECT T.*, M.*
FROM TEAM T
         INNER JOIN MEMBER M ON T.ID = M.TEAM_ID
WHERE T.NAME = 'Aespa';
```

````
String jpql = "SELECT t FROM Team t JOIN FETCH t.members WHERE t.name = 'Aespa'";
List<Team> teams = em.createQuery(jpql, Team.class).getResultList(); 
System.out.println("teams.size() = " + teams.size()); // teams.size() = [member 수] 
````

#### fetch join 과 DISTINCT

- JPQL의 `DISTINCT`는 SQL에 `DISTINCT`를 추가 + app 단에서 중복 제거
- 실제 SQL은 중복제거를 해도 조회하려는 객체의 중복 제거가 안됨
- select 결과를 한번더 app 단에서 중복 제거를 한번 더 함

```sql
-- JPQL
SELECT DISTINCT t
FROM Team t
         JOIN FETCH t.members
WHERE t.name = 'Aespa';

-- 실제 SQL
SELECT DISTINCT t.*, m.*
FROM TEAM T
         INNER JOIN MEMBER M ON T.ID = M.TEAM_ID
WHERE T.NAME = 'Aespa';
```

#### fetch join과 일반 join의 차이

- **일반 join은 연관관계 객체를 조회하지 않음**
    - 지연 로딩 설정 시 : 프록시 반환
    - 즉시 로딩 설정 시 : **연관 객체 SELECT 쿼리를 한번 더 실행**

```sql
-- JPQL : 일반 join
SELECT t
FROM Team t
         JOIN t.members m
WHERE t.name = 'Aespa';

-- 실제 SQL : 일반 join
SELECT T.* -- Entity만
FROM TEAM T
         INNER JOIN MEMBER M ON T.ID = M.TEAM_ID
WHERE T.NAME = 'Aespa';


-- JPQL : fetch join
SELECT t
FROM Team t
         JOIN FETCH t.members m
WHERE t.name = 'Aespa';

-- 실제 SQL : fetch join
SELECT T.*, M.* -- Entity + 연관관계 객체
FROM TEAM T
         INNER JOIN MEMBER M ON T.ID = M.TEAM_ID
WHERE T.NAME = 'Aespa';
```

#### fetch join의 특징과 한계

- sql 호출 회수 최적화 : sql 한번으로 연관 객체 조회
- global loading 전략보다 우선하여 사용됨
    - global loading 전략 : Entity에 직접 적용한 fetch 전략 e.g. `@OneToMany(fetch = FetchType.LAZY)`
    - **gloabl loading 전략은 지연 로딩하고, 필요할 때만 fetch join으로 즉시 로딩**
- 준영속 상태에서도 객체 그래프 탐색 가능
- fetch join 대상에 별칭 불가능
    - hibernate는 지원하지만, 무결성이 꺠질 수 있어 주의해서 사용
- 2개 이상의 컬렉션 fetch 불가
    - 구현체에 따라 지원하지만, 카테시안 곱이 발생할 수 있음
    - hibernate에선 `javax.persistence.PersistenceException` 발생
- 컬렉션 페치 조인 시 페이징 불가, `setFirstResult`, `setMaxResults`
    - 1:1, N:1은 페이징 가능
    - 컬렉션 페치 조인 시, 일대다 조인이 발생하므로 데이터가 예측할 수 없이 증가
        - hibernate는 메모리에서 페이징 처리를 하고, 경고 로깅을 남김 (성능 저하)
- **결론**
    - **fetch join은 객체그래프를 유지하면서 조회할 수 있어 편함**
    - **Entity가 가진 모양과 많이 다른 결과를 조회하고 싶다면 여러번 조회해서 DTO로 변환하여 반환하는 것이 효과적**

### 2.8 경로 표현식, path expression

`.`을 찍어 객체 그래프를 탐색하는 것

```sql
select m.username -- 상태 필드
from Member m
         join m.team t -- 연관 필드 (단일 값 연관 필드)
         join t.orders o -- 연관 필드 (컬렉션 값 연관 필드)
where m.team.name = 'Aespa';
```

```java

@Entity
public class Member {
    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "USERNAME")
    private String username; // 상태 필드

    @ManyToOne
    @JoinColumn(name = "TEAM_ID")
    private Team team; // 연관 필드 (단일 값 연관 필드)

    @OneToMany(mappedBy = "member")
    private List<Order> orders = new ArrayList<>(); // 연관 필드 (컬렉션 값 연관 필드)

    //...
}
```

#### 경로 표현식의 용어 정리

- 상태 필드, state field : 단순히 값을 저장하기 위한 필드
- 연관 필드, association field : 연관관계를 위한 필드, 임베디드 타입 포함
    - 단일 값 연관 필드 : `@ManyToOne`, `@OneToOne`, 대상이 엔티티
    - 컬렉션 값 연관 필드 : `@OneToMany`, `@ManyToMany`, 대상이 컬렉션

#### 경로 표현식과 특징

- 상태 필드 경로 : 탐색의 끝, 더 이상 탐색 불가
- 단일 값 연관 경로 : 묵시적 내부 조인 발생, 계속 탐색 가능
- 컬렉션 값 연관 경로 : 묵시적 내부 조인 발생, 더 이상 탐색 불가
    - 단, `FROM` 절에서 별칭을 얻으면 별칭으로 탐색 가능
- 묵시적 조인 : 단일 값 연관 필드로 경로탐색 시 묵시적으로 SQL `INNER JOIN`
    - `SELECT m.team FROM Member m` : 묵시적 조인 발생

```sql
-- JPQL : 상태 필드 경로 탐색
SELECT m.username, m.age
FROM Member m;

-- 실제 SQL : 상태 필드 경로 탐색
SELECT M.USERNAME, M.AGE
FROM MEMBER M;

-- JPQL : 단일 값 연관 경로 탐색
SELECT o.member
FROM Order o;

-- 실제 SQL : 단일 값 연관 경로 탐색
SELECT M.*
FROM ORDERS O
         INNER JOIN MEMBER M ON O.MEMBER_ID = M.ID;

-- JPQL : 묵시적 조인을 이용한 경로 탐색
SELECT t.*
FROM Order o
WHERE o.product.name = 'NEXT_LEVEL'
  AND o.address.city = 'Seoul';

-- 실제 SQL : 묵시적 조인을 이용한 경로 탐색
SELECT T.*
FROM Orders O
         INNER JOIN MEMBER M ON O.MEMBER_ID = M.ID -- 묵시적 조인 (1)
         INNER JOIN TEAM T ON M.TEAM_ID = T.ID -- 묵시적 조인 (2)
         INNER JOIN PRODUCT P ON O.PRODUCT_ID = P.ID -- 묵시적 조인 (3)
WHERE P.PRODUCT_NAME = 'NEXT_LEVEL'
  AND O.CITY = 'Seoul';

-- JPQL : 컬렉션 값 연관 경로 탐색 (실패)
SELECT t.members.username
FROM Team t;

-- JPQL : 컬렉션 값 연관 경로 탐색 (성공)
SELECT m.username
FROM Team t
         JOIN t.members m -- 별칭 m을 얻어 m으로부터 경로 탐색 가능
;

-- JPQL : 컬렉션 사이즈 구하기
SELECT t.members.size
FROM Team t;
```

#### 경로 탐색을 사용한 묵시적 조인 시 주의사항

- 항상 `INNER JOIN `
- 컬렉션은 경로탐색의 마지막, 추가 탐색하려면 `JOIN`문으로 별칭을 얻어야함
- 경로 탐색은 주로 SELECT, WHERE 절에서 사용하지만, 묵시적 조인으로 인해 SQL의 FROM 절에 영향을 줌
- 묵시적 JOIN 보다는 명시적 JOIN을 추천 (성능)

### 2.9 서브 쿼리

`WHERE`, `HAVING` 절에서 사용 가능 (hibernate HQL은 `SELECT` 절까지 가능)

```sql
-- JPQL : 서브 쿼리 WHERE 절
SELECT m
FROM Member m
WHERE m.age > (SELECT AVG(m2.age) FROM Member m2);

````

#### 서브 쿼리 함수

```sql
-- [NOT] EXISTS (subquery) : 서브 쿼리에 결과가 존재하면 참
select m
from Member m
where exists(select t from m.team t where t.name = 'Aespa');

-- {ALL | ANY | SOME} (subquery) : 조건식을 만족하는지 비교
select m
from Member m
where m.team = ANY (select t from Team t);

-- [NOT] IN (subquery) : 서브 쿼리의 결과 중 하나라도 같은 것이 있으면 참
select t
from Team t
where t in (select t2
            from Team t2
                     join t2.members m2
            where m2.age > 10);
````

### 2.10 조건식

#### 타입 표현

| 종류      | 설명                                                                                         | 예시                                                               |
|---------|--------------------------------------------------------------------------------------------|------------------------------------------------------------------|
| 문자      | 작은따옴표로 감쌈<br/> '로 escape                                                                   | 'HELLO', 'She''s'                                                |
| 숫자      | L, D, F로 타입 지정                                                                             | 10, 10L, 10D                                                     |
| 날짜      | DATE {d 'yyyy-mm-dd'}<br/> TIME {t 'hh:mm:ss'}<br/> TIMESTAMP {ts 'yyyy-mm-dd hh:mm:ss.f'} | {d '2021-10-10'}<br/>m.createDate > {ts '2021-10-10 10:10:10.0'} |
| Boolean | TRUE, FALSE                                                                                | TRUE, FALSE                                                      |

#### 연산자 우선순위

1. 경로 탐색 연산 `.` (ex. `m.username`)
2. 수학 연산 : `*`, `/`, `%`
3. 비교 연산 : `=`, `>`, `<`, `>=`, `<=`, `<>`, `IS`, `LIKE`, `BETWEEN`, `IN`
4. 논리 연산 : `NOT`, `AND`, `OR`

#### 논리 연산과 비교식

- 논리 연산
    - `AND` : 둘다 만족하면 참
    - `OR` : 둘 중 하나만 만족해도 참
    - `NOT` : 논리값의 반대
- 비교식 : `=`, `>`, `<`, `>=`, `<=`, `<>`

#### `BETWEEN`, `IN`, `LIKE`, NULL 비교

- `BETWEEN` : `X [NOT] BETWEEN A AND B`
- `IN` : `X [NOT] IN (A, B, C)`
- `LIKE` : `X [NOT] LIKE 패턴값 [ESCAPE]`
- `NULL` 비교 : `IS [NOT] NULL`

#### 컬렉션 식

- **컬렉션에는 컬렉션 식 외에 다른 조건식 사용 불가**
- `IS [NOT] EMPTY` : 컬렉션이 비어있는지 검사
- `[NOT] MEMBER [OF]` : 컬렉션에 값이 포함되어 있는지 검사

```sql
-- JPQL
select t
from Team t
where t.members IS NOT EMPTY;

-- 실제 SQL
SELECT T.*
FROM TEAM T
WHERE EXISTS(SELECT 1
             FROM MEMBER M
             WHERE M.TEAM_ID = T.ID);

--JPQL : 컬렉션에 컬렉션 식 외의 조건식 사용 (실패)
select t
from Team t
where t.members is null;

-- JPQL
select t
from Team t
where :memberParam MEMBER OF t.members;
```

#### 스칼라 식

| 함수                                                             | 설명                                                                                       | 예시                          |
|----------------------------------------------------------------|------------------------------------------------------------------------------------------|-----------------------------|
| +, -, *, /                                                     | 사칙 연산                                                                                    | age + 10                    |
| CONCAT(문자1, 문자2, ...)                                          | 문자 연결                                                                                    | CONCAT('a', 'b')            |
| SUBSTRING(문자, 위치 [길이])                                         | 문자열 자르기                                                                                  | SUBSTRING(m.username, 2, 3) |
| TRIM([[LEADING &#124; TRAILING &#124; BOTH] [제거할 문자] FROM] 문자) | 문자열 공백 제거                                                                                | TRIM('  a  ')               |
| LOWER(문자), UPPER(문자)                                           | 소문자, 대문자로 변경                                                                             | LOWER(m.username)           |
| LENGTH(문자)                                                     | 문자 길이                                                                                    | LENGTH(m.username)          |
| LOCATE(찾을 문자, 원본 문자, [시작위치])                                   | 문자 위치 찾기                                                                                 | LOCATE('arin', m.username)  |
| ABS, SQRT, MOD, SIZE, INDEX                                    | 수학 함수 <br/> ABS : 절대값<br/>SQRT : 제곱근<br/>MOD : 나머지<br/>SIZE : 컬렉션 크기<br/>INDEX : 컬렉션 인덱스 | ABS(m.age), SIZE(t.members) |
| CURRENT_DATE, CURRENT_TIME, CURRENT_TIMESTAMP                  | 데이터베이스 시스템의 현재 날짜, 시간, 타임스탬프                                                             | CURRENT_DATE                |

#### CASE 식

- 기본 CASE : `CASE {WHEN <조건식> THEN <스칼라식>} + ELSE <스칼라식> END`
- 단순 CASE : `CASE <대상> {WHEN <스칼라식1> THEN <스칼라식2>} + ELSE <스칼라식3> END`
- `COALESCE` : 하나씩 조회해서 null이 아니면 반환
- `NULLIF` : 두 값이 같으면 null 반환, 다르면 첫번째 값 반환

```sql
-- JPQL : 기본 CASE
select case
           when m.username = '카리나' then '최애'
           when m.username = '하니' then '최애 두번쨰'
           else '최애 아님'
           end
from Member m;

-- JPQL : 단순 CASE
select case m.username
           when '카리나' then '최애'
           when '하니' then '최애 두번쨰'
           else '최애 아님'
           end
from Member m;

-- JPQL : COALESCE
-- username이 null이면 '이름 없는 회원' 반환
select coalesce(m.username, '이름 없는 회원')
from Member m;

-- JPQL : NULLIF
-- username이 '이름 없는 회원'이면 null 반환
select nullif(m.username, '이름 없는 회원')
from Member m;
```

### 2.11 다형성 쿼리

JPQL로 부모 Entity 조회시 자식 Entity도 함께 조회됨

```java

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "DTYPE")
public abstract class Person {
    //...
}

@Entity
@DiscriminatorValue("I")
public class Idol extends Person {
    //...
}

@Entity
@DiscriminatorValue("D")
public class Developer extends Person {
    //...
}
```

````
List<Person> result = em.createQuery("select p from Person p", Person.class)
                        .getResultList(); // Idol, Developer 모두 조회됨
````

```sql
-- JPQL
select p
from Person p


-- 실제 SQL : InheritanceType.SINGLE_TABLE
select p.*
from Person p;

-- 실제 SQL : InheritanceType.JOINED
select p.*, i.*, d.*
from Person p
         left outer join Idol i on p.id = i.id
         left outer join Developer d on p.id = d.id;
```

#### TYPE

Entity 상속 구조에서 조회 대상을 특정 자식 타입으로 한정할 떄

```sql
-- JPQL
select p
from Person p
where type(p) in (Idol, Developer);

-- 실제 SQL
select p.*
from Person p
where p.DTYPE in ('I', 'D');
``` 

#### TREAT(JPA 2.1)

- 상속 구조에서 부모 타입을 특정 자식 타입으로 다룰 때 사용
- JPA는 `FROM`, `WHERE`, hibernate는 `SELECT`에서 사용 가능

```sql
-- JPQL
select p
from Person p
where treat(p as Idol).groupName = 'Aespa';

-- 실제 SQL
select p.*
from person p
where p.DTYPE = 'I'
  and p.groupName = 'Aespa';
```

### 2.12 사용자 정의 함수 호출 (JPA 2.1)

- JPQL에서 사용자 정의 함수 호출 가능
- `function_invocation::== FUNCTION(function_name {, function_arg}*)`

```java
public class MySqlDialectCustom extends MySQL5Dialect {
    public MySqlDialectCustom() {
        super();
        registerFunction("group_concat", new StandardSQLFunction("group_concat", StandardBasicTypes.STRING));
    }
}
```

````
<!-- persistence.xml -->
<property name="hibernate.dialect" value="com.example.demo.MySqlDialectCustom"/>
````

```sql
-- jpql
select group_concat(m.username)
from Member m;
````

### 2.13 기타 정리

- enum은 `=` 비교연산자만 지원
- 임베디드 타입은 비교 지원 안함
- EMPTY STRING
    - JPA : 길이가 0인 문자열을 Empty String으로 정의
    - DBMS : 길이가 0인 문자열을 NULL로 정의하기도 함
- NULL
    - 조건을 만족하는 값이 하나도 없으면 NULL 이다.
    - NULl은 알 수 없는 값이다.
    - NULL == NULL 은 알 수 없는 값이다.
    - NULL is NULL 은 참이다.

### 2.14 Entity 직접 사용

#### 기본 키 값

JPQL에서 엔터티를 직접 사용시 자동으로 Entity의 기본 키 값을 사용

```sql
-- JPQL : 기본 키 값 사용
select count(m.id)
from Member m;

-- JQPL : Entity 직접 사용
select count(m)
from Member m;

-- 실제 SQL
-- 둘다 똑같음
select count(m.id)
from Member m;

-- JPQl
select m
from Member m
where m = :member;

-- 실제 SQL
select m.*
from Member m
where m.id = ?;
```

#### 외래 키 값

```sql
-- JPQL
select m
from Member m
where m.team = :team;

-- 실제 SQL
select m.*
from Member m
where m.team_id = ?;
```

### 2.15 Named 쿼리 : 정적 쿼리

- 동적 쿼리 : JPQL을 직접 문자열로 작성
    - runtime에 쿼리가 완성됨
    - e.g. `em.createQuery("select m from Member m where m.username = :username", Member.class);`
- 정적 쿼리 : 미리 정의한 쿼리
    - app 로딩 시점에 JPQL 문법 체크 후 parsing
    - 오류 체크
    - 성능 최적화 : 미리 parsing된 쿼리 재사용, DBMS 조회 성능 최적화
    - `@NamedQuery`, xml 에 등록

#### Named 쿼리를 어노테이션에 정의

```java

@Entity
@NamedQuery(
        name = "Member.findByTeamId",
        query = "select m from Member m where m.teamId = :teamId"
)
public class Member {
    //...
}

public class Foo {
    public void fooSelect() {
        List<Member> memberAespa = em.createNamedQuery("Member.findByTeamId", Member.class)
                .setParameter("teamId", "Aespa001")
                .getResultList();
    }
}
```

#### Named 쿼리를 XML에 정의

- named query는 xml이 더 편리함
- xml에 정의된 named query는 어노테이션에 정의된 named query를 오버라이딩 함

```xml
<?xml version="1.0" encoding="UTF-8"?>

<named-query name="Member.findByTeamId">
    <query>
        select m
        from Member m
        <CDATA[
        where m.teamId = :teamId
        ]]>
    </query>
</named-query>

        <!--persistance.xml-->
<persistence-unit name="jpabook">
<mapping-file>META-INF/orm.xml</mapping-file>
</persistence-unit>
```

## 3. Criteria

- JPQL을 편하게 작성할 수 있는 builder API
- 코드로 JPQL 작성 가능
    - compile-time에 sql 오류 잡을 수 있음
    - 코드가 복잡해지고 가독성이 떨어짐

### 3.1 Criteria 기본 API

`javax.persistence.criteria` 패키지

````
// 1. CriteriaBuilder 얻기
CriteriaBuilder cb = em.getCriteriaBuilder();

// 2. CriteriaQuery 생성, 반환타입 지정
CriteriaQuery<Member> query = cb.createQuery(Member.class);

// 3. FROM 절 생성, 쿼리 루트
Root<Member> m = query.from(Member.class);
// 4. SELECT 절 생성
query.select(m);

TypedQuery<Member> typedQuery = em.createQuery(query);
List<Member> members = typedQuery.getResultList();
````

````
CreteriaBuilder cb = em.getCriteriaBuilder();
CriteriaQuery<Member> query = cb.createQuery(Member.class);
Root<Member> m = query.from(Member.class);

// 1. 검색 조건 지정
Predicate predicate = cb.equal(m.get("username"), "Aespa");

// 2. 정렬 조건 지정
javax.persistence.criteria.Order order = cb.desc(m.get("age"));

// 3. 쿼리 생성
query.select(m)
        .where(predicate)
        .orderBy(order);
        
// 4. 쿼리 실행
List<Member> members = em.createQuery(query).getResultList();
````

````

Root<Member> m = query.from(Member.class);

Predicate predicate = cb.greaterThan(m.<Integer>get("age"), 10);

cq.select(m);
cq.where(predicate);
cq.orderBy(cb.desc(m.get("age")));
````

### 3.2 Criteria 쿼리 생성

`CriteriaBuilder.createQuery()`를 통해 CriteriaQuery 생성

````
CriteriaBuilder cb = em.getCriteriaBuilder();

//Member로 반환 타입 지정
CriteriaQuery<Member> cq = cb.createQuery(Member.class);

...

List<Member> members = em.createQuery(cq).getResultList();
````

### 3.3 조회

#### 조회 대상 한건, 여러건 지정

````
cq.select(m); // JPQL : select m

cq.multiselect(m.get("username"), m.get("age")); // JPQL : select m.username, m.age
cq.select(cb.array(m.get("username"), m.get("age"))); // JPQL : select m.username, m.age
````

#### DISTINCT

````
CriteraQeury<Object[]> cq = cb.createQuery(Object[].class);
Root<Member> m = cq.from(Member.class);
cq.multiselect(m.get("username"), m.get("age")).distinct(true);

TypedQuery<Object[]> query = em.createQuery(cq);
List<Object[]> resultList = query.getResultList();
````

#### NEW, construct()

````
CriteriaQuery<MemberDTO> cq = cb.createQuery(MemberDTO.class);
Root<Member> m = cq.from(Member.class);
cq.select(cb.construct(MemberDTO.class, m.get("username"), m.get("age")));

TypedQuery<MemberDTO> query = em.createQuery(cq);
List<MemberDTO> resultList = query.getResultList();
````

#### Tuple

- Map과 비슷
- 미리 선언한 별칭으로 값을 꺼낼 수 있음

````
// JPQL : select m.username, m.age from Member m

CreteriaBuilder cb = em.getCriteriaBuilder();
CriteriaQuery<Tuple> cq = cb.createTupleQuery();

Root<Member> m = cq.from(Member.class);
cq.multiselect(m.get("username").alias("username"), m.get("age").alias("age"));

TypedQuery<Tuple> query = em.createQuery(cq);
List<Tuple> resultList = query.getResultList();

for (Tuple tuple : resultList) {
    System.out.println("username = " + tuple.get("username", String.class));
    System.out.println("age = " + tuple.get("age", Integer.class));
}
````

### 3.4 집합

#### GROUP BY

````
CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
Root<Member> m = cq.from(Member.class);

Expression maxAge = cb.max(m<Integer>get("age"));
Expression minAge = cb.min(m<Integer>get("age"));

cq.multiselect(m.get("team").get("name"), maxAge, minAge);
cq.groupBy(m.get("team").get("name"));

TypedQuery<Object[]> query = em.createQuery(cq);
List<Object[]> resultList = query.getResultList();
````

#### HAVING

````
cq.multiselect(m.get("team").get("name"), maxAge, minAge)
  .gropuBy(m.get("team").get("name"))
    .having(cb.gt(maxAge, 10));
````

### 3.5 정렬

````
cq.select(m)
  .where(predicate)
    .orderBy(cb.desc(m.get("age")));
````

### 3.6 조인

```
Root<Member> m = cq.from(Member.class);
Join<Member, Team> t = m.join("team", JoinType.INNER);

cq.multiselect(m, t)
  .where(cb.equal(t.get("name"), "Aespa"));
```

- `JoinType.INNER`, `JoinType.LEFT`, `JoinType.RIGHT`
- fetch join :  `join()` 대신 `fetchJoin()`을 사용
    - `m.fetchJoin("team", JoinType.LEFT);`

### 3.7 서브쿼리

#### 간단한 서브 쿼리

````
/* JPQL : select m 
      from Member m 
      where m.age > (select avg(m2.age) from Member m2) */

CriteriaBuilder cb = em.getCriteriaBuilder();
CriteraQuery<Member> mainQuery = cq.subquery(Member.class);

Subquery<Double> subQuery = mainQuery.subquery(Double.class);
Root<Member> m2 = subQuery.from(Member.class);

Root<Member> m = mainQuery.from(Member.class);
mainQuery.select(m)
  .where(cb.gt(m.get("age"), subQuery));
  
````

#### 상호 관련 서브 쿼리

````
/* JPQL : select m 
      from Member m 
      where exist (select t from m.team t where t.name = 'Aespa') */
      
CriteriaBuilder cb = em.getCriteriaBuilder();`
CriteriaQuery<Member> cq = cb.createQuery(Member.class);

Root<Member> m = cq.from(Member.class);

Subquery<Team> subQuery = cq.subquery(Team.class);
Root<Member> subM = subQuery.from(Member.class);

Join<Member, Team> t = subM.join("team");
subQuery.select(t)
  .where(cb.equal(t.get("name"), "Aespa"));

List<Member> members = em.createQuery(cq).getResultList();
````

### 3.8 IN 식

````
/* JPQL : select m 
      from Member m 
      where m.age in (10, 20) */
  
CriteriaBuilder cb = em.getCriteriaBuilder();
CriteriaQuery<Member> cq = cb.createQuery(Member.class);
Root<Member> m = cq.from(Member.class);

cq.select(m)
  .where(m.get("age").in(10, 20));
````

### 3.9 CASE 식

````
/* JPQL :select case
           when m.username = '카리나' then '최애'
           when m.username = '하니' then '최애 두번쨰'
           else '최애 아님'
           end
        from Member m; */

Root<Member> m = cq.from(Member.class);
cq.multiselect(m.get("username"), 
  cb.selectCase()
    .when(cb.equal(m.get("username"), "카리나"), "최애")
    .when(cb.equal(m.get("username"), "하니"), "최애 두번째")
    .otherwise("최애 아님"));
````

### 3.10 파라미터 정의

````

CriteriaBuilder cb = em.getCriteriaBuilder();
CriteriaQuery<Member> cq = cb.createQuery(Member.class);

Root<Member> m = cq.from(Member.class);

cq.select(m)
  .where(cb.equal(m.get("username"), cb.parameter(String.class, "username")));

List<Member> members = em.createQuery(cq)
  .setParameter("username", "카리나") // 파라미터 바인딩
  .getResultList();
````

### 3.11 네이티브 함수 호출

````
Root<Member> m = cq.from(Member.class);
Expression<Long> function = cb.function("SUM", Long.class, m.get("age"));
cq.select(function);
````

### 3.12 동적 쿼리

- JPQL 동적 쿼리는 String을 직접 조합해야 하기 때문에 실수할 가능성이 높음

````
// Where 절 검색 조건
Integer age = 10;
String username = "카리나";
String teamname = null;

CriteriaBuilder cb = em.getCriteriaBuilder();
CriteriaQuery<Member> cq = cb.createQuery(Member.class);

Root<Member> m = cq.from(Member.class);
Join<Member, Team> t = m.join("team", JoinType.INNER);

List<Predicate> criteria = new ArrayList<>();

if(age != null)
  criteria.add(cb.equal(m.get("age"), age));

if(username != null)
    criteria.add(cb.equal(m.get("username"), username));
  
if(teamname != null)
    criteria.add(cb.equal(t.get("name"), teamname));
    
cq.where(cb.and(criteria.toArray(new Predicate[0)])));

TypedQuery<Member> query = em.createQuery(cq);

if(age != null)
  query.setParameter("age", age);
  
if(username != null)
  query.setParameter("username", username);

if(teamname != null)
  query.setParameter("teamname", teamname);

List<Member> members = query.getResultList();
````

### 3.13 함수 정리

[java docs CriteriaBuilder interface 참고](https://docs.oracle.com/javaee/7/api/javax/persistence/criteria/CriteriaBuilder.html)

### 3.14 Criteria 메타 모델 API

- 문자가 아닌 코드로 JPA 메타 모델을 만들어 사용하는 방법
- 코드 자동 생성기가 자동으로 [Entity명]_.java 파일을 생성해줌

#### 코드 생성기 설정

- `hibernate-jpamodelgen` 라이브러리 추가

## 4. QueryDSL

- Criteria의 단점 : 코드가 장황하고, SQL 가독성이 떨어짐
- QueryDSL 은 Criteria의 단점을 보완한 라이브러리
    - sql을 코드로 작성
    - 쉽고 간결하고, sql과 비슷한 구조로 코드가 작성됨
    - JPQL 빌더
- HQL 지원에서 시작하여 현재는 JPA, JDBC, Hibernate, Mongo DB 등 다양한 지원

### 4.1 QueryDSL 설정

- `querydsl-jpa` 라이브러리 추가
- `querydsl-apt` 라이브러리 추가 : 쿼리 타입 생성 시 사용
- 쿼리 타입 생성용 플러그인 추가

### 4.2 시작

`com.mysema.query.jpa.impl.JPAQuery`를 사용

````
EntityManager em = emf.createEntityManager();
JPAQuery query = new JPAQuery(em);
QMember qMember = new QMember("m"); // 별칭 M 지정

Lsit<Member> members = query.from(qMember)
                              .where(qMember.username.eq("카리나"))
                              .orderBy(qMember.age.desc())
                              .list(qMember);
````

````
// 쿼리 타입 지정 방법
QMember qMember = new QMember("m"); // 별칭 M 지정
QMember qMember = QMember.member; // 기본 인스턴스 사용
````

### 4.3 검색 조건 쿼리

````
/* JPQL : select t 
      from Team t 
      where t.name = 'Aespa' 
       */
       
JPAQuery query = new JPAQuery(em);
QTeam qTeam = QTeam.team;
List<Team> teams = query.from(qTeam)
                        .where(qTeam.name.eq("Aespa"))
                        .list(qTeam);
````

### 4.4 결과 조회

- `uniqueResult()` : 단건 조회, 결과가 하나 이상이면 `NonUniqueResultException` 발생
- `singleResult()` : 단건 조회, 결과가 하나 이상이면 처음 데이터 반환
- `list()` : 결과가 하나 이상일 때

### 4.5 페이징과 정렬

````
QTeam team = QTeam.team;
List<Team> teams = query.from(team)
                        .orderBy(team.name.desc())
                        .offset(10)
                        .limit(20)
                        .list(team);
````

````
// QueryModifiers 사용
QueryModifiers queryModifiers = new QueryModifiers(20L, 10L); // limit, offset
List<Team> teams = query.from(team)
                        .orderBy(team.name.desc())
                        .restrict(queryModifiers)
                        .list(team);
````

````
// 페이징 처리
SearchResults<Team> results = query.from(team)
                                    .orderBy(team.name.desc())
                                    .offset(10)
                                    .limit(20)
                                    .listResults(team);

long total = results.getTotal(); // 검색된 전체 데이터 수
long limit = results.getLimit();
long offset = results.getOffset();
List<Team> teams = results.getResults(); // 조회된 데이터
````                            

### 4.6 그룹

````
query.from(team)
        .groupBy(team.name)
        .having(team.isDeleted.eq("N"))
        .list(team);
````

### 4.7 조인

- 내부 조인, 외부 조인, 페치 조인 지원
- `join(join target, alias query type)`

````
// 기본 조인
QTeam team = QTeam.team;
QMember member = QMember.member;
QMemberPhone memberPhone = QMemberPhone.memberPhone;

query.from(team)
    .join(team.members, member)
    .leftJoin(member.phones, memberPhone)
    .list(team);
    
// on 활용
query.from(team)
    .join(team.members, member)
    .on(member.age.gt(10))
    .list(team);
    
// fetch join
query.from(team)
  .innerJoin(team.members, member).fetch()
  .leftJoin(member.phones, memberPhone).fetch()
  .list(team);
  
// 세타 조인
query.from(team, member)
  .where(team.member.eq(member))
  .list(team);
````

### 4.8 서브 쿼리

`com.mysema.query.jpa.JPASubQuery` 사용

````
QTeam team = QTeam.team;
QMember member = QMember.member;

query.from(team)
      .where(team.in(
        new JPASubQuery().from(member)
                         .where(member.age.gt(10))
                         .list(team))
      .list(team);
````

### 4.9 프로젝션과 결과 반환

````
// 프로젝션 대상이 하나
QMember member = QMember.member;
list<String> memberNameList = query.from(member).list(memer.username);

// 여러 컬럼, 튜플
QMember member = QMember.member;
List<Tuple> result = query.from(member).list(member.username, member.age);
````

#### 빈 생성

`com.mysema.query.types.Projections` 사용

```java
public class MemberDTO {
    private String name;
    private int age;

    // Constructor, getter, setter
}

public class Foo {

    public void test() {
        // 프로퍼티 접근 (setter)
        QMember member = QMember.member;
        List<MemberDTO> result = query.from(member)
                .list(Projections.bean(MemberDTO.class, member.username.as("name"), member.age));

        // 필드 직접 접근
        List<MemerDTO> result = query.from(member)
                .list(Projections.fields(MemberDTO.class, member.username.as("name"), member.age));

        // 생성자 사용
        List<MemberDTO> result = query.from(member)
                .list(Projections.constructor(MemberDTO.class, member.username, member.age));
    }
}
````

#### DISTINCT

````
QMember member = QMember.member;
List<String> result = query.distinct()
                          .from(member)
                          .list(member.username);
````

### 4.10 수정, 삭제 배치 쿼리

`com.mysema.query.jpa.JPAUpdateClause` 사용

````
QMember member = QMember.member;

// 수정
JPAUpdateClause updateClause = new JPAUpdateClause(em, member);

long count = updateClause.where(team.name.eq("Aespa"))
                         .set(team.name, "에스파")
                         .execute();
                         
// 삭제
JPAUpdateClause deleteClause = new JPAUpdateClause(em, member);
long count = deleteClause.where(team.name.eq("Aespa"))
                         .execute(member);
````

### 4.11 동적 쿼리

`com.mysema.query.BooleanBuilder` 사용

````
SearchParam param = new SearchParam();
param.setCity("서울");
param.setAge(23);

QMember member = QMember.member;
BooleanBuilder builder = new BooleanBuilder();

if(StringUtils.hasText(param.getCity())) {
    builder.and(member.name.eq(param.getCity()));
}

if(param.getAge() != null) {
    builder.and(member.age.eq(param.getAge()));
}

List<Member> members = query.from(member)
                            .where(builder)
                            .list(member);
````

### 4.12 메소드 위임, Delegate methods

- 쿼리 타입에 검색조건 직접 정의

```java
import javafx.beans.binding.BooleanExpression;

public class MemberExpression {

    @QueryDelegate(Member.class)
    public static BooleanExpression isOlderThan(QMember member, Integer age) {
        return member.age.gt(age);
    }

}

// 쿼리 타입에 생성된 자동 결과
public class QMember extends EntityPathBase<Member> {
    //...
    public BooleanExpression isOlderThan(Integer age) {
        return MemberExpression.isOlderThan(this, age);
    }
}
```

````
query.from(member)
      .where(member.isOlderThan(21))
      .list(member);
````

### 4.13 QueryDSL 정리

- QueryDSL은 코드로 JPQL을 작성 + 동적 쿼리 작성을 편리하게 지원

## 5. 네이티브 SQL

- JPA는 SQL을 추상화한 JPQL을 제공
- 특정 DBMS에 의존하는 기능 미제공
    - 문법, SQL 힌트, stored procedure, UNION 등
- **Native SQL을 사용하면 Entity 사용 가능, 영속성 컨텍스트 관리** (JDBC API와의 차이점)

#### 데이터베이스에 의존하는 기능 사용법

- 특정 DBMS에서만 사용하는 함수
    - JPQL의 네이티브 SQL 함수 호출 사용
    - hibernate의 방언에 각 DBMS에 맞는 함수가 등록되어있음
- SQL 힌트
    - hibernate를 포함한 몇 구현체들이 지원
- 인라인 뷰, UNION, INTERSECT
    - hibernate 미지원
- stored procedure
    - JPQL에서 호출 가능
- 특정 DBMS에만 있는 문버
    - native sql로 작성

### 5.1 네이티브 SQL 사용

````
// 결과 타입 정의
public Query createNativeQuery(String sqlString, Class resultClass);

// 결과 타입 미정의
public Query createNativeQuery(String sqlString);

// 결과 매핑 사용
public Query createNativeQuery(String sqlString, String resultSetMapping);
````

#### Entity 조회

````
String sql = "SELECT ID, AGE, NAME, TEAM_ID FROM MEMBER WHERE NAME = ?";

Query nativeQuery = em.createNativeQuery(sql, Member.class)
                       .setParameter(1, "카리나");
                       
List<Member> result = nativeQuery.getResultList();
````

#### 값 조회

단순히 값으로 조회되어 영속성 컨텍스트에 관리되지 않음

````
String sql = "SELECT ID, AGE, NAME, TEAM_ID FROM MEMBER WHERE NAME = ?";

Query nativeQuery = em.createNativeQuery(sql)
                       .setParameter(1, "카리나");

List<Object[]> result = nativeQuery.getResultList();
````

#### 결과 매핑 사용

- Entity와 스칼라 값을 조합하는 등 결과가 복잡해지면 `@SqlResultSetMapping`을 사용
- `@ColumnResult` : 스칼라 값 매핑
- `@FieldResult` : 스칼라 값 매핑하지만 `@ColumnResult`보다 우선순위 높음
    - 결과 셋에 컬럼명이 중복일때 용이

```java

//  @ColumnResult 사용
@Entity
@SqlResultSetMapping(
        name = "teamWithMemberCount",
        entities = {@EntityResult(entityClass = Team.class)},
        columns = {@ColumnResult(name = "CNT_MEMBER")}
)
public class Team {
    //...
}

// @FieldResult 사용
@SqlResultSetMapping(
        name = "teamWithMemberCount",
        entities = {
                @EntityResult(entityClass = Team.class, fields = {
                        @FieldResult(name = "id", column = "ID"),
                        @FieldResult(name = "teamName", column = "TEAM_NAME")})},
        columns = {@ColumnResult(name = "CNT_MEMBER")})
public class Team {
    //...
}
```

````
String sql = "SELECT ID, TEAM_NAME, CNT_MEMBER" +
             "FROM TEAM T" +
              "LEFT JOIN " +
              "( SELECT TEAM_ID, COUNT(*) AS CNT_MEMBER" +
              "  FROM MEMBER M INNER JOIN TEAM T ON M.TEAM_ID = T.ID" +
              "  GROUP BY TEAM_ID ) TI " +
              "ON T.ID = TI.TEAM_ID";

Query nativeQuery = em.createNativeQuery(sql, "teamWithMemberCount");
List<Object[]> result = nativeQuery.getResultList();
````

#### 결과 매핑 어노테이션

- `@SqlResultSetMapping`
    - name : 결과 매핑 이름
    - entities : `@EntityResult`로 Entity를 결과로 매핑
    - columns : `@ColumnResult`로 컬럼을 결과로 매핑
- `@EntityResult`
    - entityClass : 결과를 매핑할 Entity 클래스
    - fields : `@FieldResult`로 결과를 Entity 필드에 매핑
    - discriminatorColumn : 상속 구조일 때 구분 컬럼
- `@FieldResult`
    - name : Entity 필드명
    - column : 결과 셋의 컬럼명
- `@ColumnResult`
    - name : 결과 셋의 컬럼명

### 5.2 Named native SQL

```java

@Entity
@NamedNativeQuery(
        name = "Member.isOlderThan",
        query = "SELECT ID, AGE, NAME, TEAM_ID FROM MEMBER WHERE AGE > ?",
        resultClass = Member.class
)
public class Member {
    //...
}

public class Foo {
    public void test() {

        TypedQuery<Member> nativeQuery = em.createNamedQuery("Member.isOlderThan", Member.class)
                .setParameter(1, 10);

    }

}
```

```java
// @SqlResultSetMapping, @NamedNativeQuery 같이 사용 

@Entity
@SqlResultSetMapping(
        name = "teamWithMemberCount",
        entities = {
                @EntityResult(entityClass = Team.class, fields = {
                        @FieldResult(name = "id", column = "ID"),
                        @FieldResult(name = "teamName", column = "TEAM_NAME")})},
        columns = {@ColumnResult(name = "CNT_MEMBER")})
@NamedNativeQuery(
        name = "Member.teamWithMemberCount",
        query = "SELECT ID, TEAM_NAME, CNT_MEMBER" +
                "FROM TEAM T" +
                "LEFT JOIN " +
                "( SELECT TEAM_ID, COUNT(*) AS CNT_MEMBER" +
                "  FROM MEMBER M INNER JOIN TEAM T ON M.TEAM_ID = T.ID" +
                "  GROUP BY TEAM_ID ) TI " +
                "ON T.ID = TI.TEAM_ID",
        resultSetMapping = "teamWithMemberCount"
)

public class Member {
    //...
}

public class Foo {
    public void test() {

        List<Object[]> resultList = em.createNamedQuery("Member.teamWithMemberCount")
                .getResultList();
    }
}
```

#### `@NamedNativeQuery`

| 속성               | 기능                        |
|------------------|---------------------------|
| name             | 네이티브 쿼리 이름                |
| query            | 네이티브 쿼리                   |
| hints            | hibernate 같은 구현체가 제공하는 힌트 |
| resultClass      | 결과 매핑할 Entity 클래스         |
| resultSetMapping | 결과 매핑 이름                  |

### 5.3 Native SQL XML에 정의

- native query는 보통 장황하므로, xml 에 관리를 추천

```xml
<?xml version="1.0" encoding="UTF-8"?>
<entity-mappiings>
    <named-native-query name="Member.isOlderThan">
        <query>
            <![CDATA[
                SELECT ID, AGE, NAME, TEAM_ID FROM MEMBER WHERE AGE > ?
                ]]>
        </query>
    </named-native-query>
</entity-mappings>
````

### 5.4 Native SQL 정리

- Native SQL 사용빈도가 많아지면 DBMS 의존도가 심해지고 관리가 어려워짐
- 사용 우선순위
    1. 표준 JPQL
    2. hibernate가 제공하는 JPQL 확장 기능
    3. Native SQL
    4. JDBC API 직접 사용, MyBatis 같은 SQL 매퍼 사용

### 5.5 stored procedure (JPA 2.1)

````
// 순서 기반 파라미터
StoredProceduceQuery procedureQuery = em.createStoredProcedureQuery("proc_multiply");
procedureQuery.registerStoredProcedureParameter(1, Integer.class, ParameterMode.IN);
procedureQuery.registerStoredProcedureParameter(2, Integer.class, ParameterMode.OUT);
procedureQuery.setParameter(1, 100);
procedureQuery.execute();

Integer out = (Integer) procedureQuery.getOutputParameterValue(2);
````

````
// 이름 기반 파라미터
StoredProceduceQuery procedureQuery = em.createStoredProcedureQuery("proc_multiply");
procedureQuery.registerStoredProcedureParameter("inParam", Integer.class, ParameterMode.IN);
procedureQuery.registerStoredProcedureParameter("outParam", Integer.class, ParameterMode.OUT);
procedureQuery.setParameter("inParam", 100);
procedureQuery.execute();

Integer out = (Integer) procedureQuery.getOutputParameterValue("outParam");
````

#### Named stored procedure 사용

- entity, xml에 정의 가능

```java

@NamedStoredProcedureQuery(
        name = "multiply",
        procedureName = "proc_multiply",
        parameters = {
                @StoredProcedureParameter(name = "inParam", type = Integer.class, mode = ParameterMode.IN),
                @StoredProcedureParameter(name = "outParam", type = Integer.class, mode = ParameterMode.OUT)
        })
@Entity
public class Member {
    //...
}
````

```xml
<?xml version="1.0" encoding="UTF-8"?>
<entity-mappiings>
    <named-stored-procedure-query name="multiply" procedure-name="proc_multiply">
        <parameter name="inParam" mode="IN" class="java.lang.Integer"/>
        <parameter name="outParam" mode="OUT" class="java.lang.Integer"/>
    </named-stored-procedure-query>
</entity-mappings>
````

````
// named stored procedure 사용

StoredProcedureQuery procedureQuery = em.createNamedStoredProcedureQuery("multiply");
procedureQuery.setParameter("inParam", 100);
procedureQuery.execute();

Integer out = (Integer) procedureQuery.getOutputParameterValue("outParam");
````

## 6. 객체지향 쿼리 심화

### 6.1 벌크 연산

- 쿼리 한 번으로 테이블 여러 로우 변경
- **많은 row 수정 시 Entity 건마다 UPDATE 문은 비효율**
- JPA 표준은 아니지만, hibernate 는 벌크 INSERT 지원
- 가능하면 벌크연산을 가장 먼저 실행할 것
    - 상황에 따라 영속성 컨텍스트 초기화 필요

````
String sql = "UPDATE MEMBER M SET M.AGE = AGE + 1 WHERE M.TEAM_ID = ?";
int resultCount = em.createNativeQuery(sql)
                    .setParameter(1, teamId)
                    .executeUpdate();
````

- **벌크 연산은 영속성 컨텍스트를 통하지 않고 직접 DB에 쿼리를 날림**
- 영속성 컨텍스트의 데이터가 실제 데이터와 다를 수 있음

#### 해결 방안

- `em.refresh()` : Entity 다시 조회
    - e.g. `em.refresh(member)` : member 엔티티만 초기화
- 벌크 연산 먼저 실행
    - 벌크연산 이후 Entity를 조회하면 DB에서 조회
- 벌크 연산 수행 후 영속성 컨텍스트 초기화
    - 영속성 컨텍스트를 초기화하여 다시 조회 시 DB에서 가져오게 함

### 6.2 영속성 컨텍스트와 JPQL

- JPQL로 조회한 데이터 중 Entity만 영속성 컨텍스트에 관리
- JPQL은 항상 DB에 조회 (`find()`와 다른점)
    - 1차 캐시에 이미 있는 Entity면 조회한 값을 버리고, 1차 캐시 반환

#### JPQL로 조회한 Entity와 영속성 컨텍스트

- JPQL로 조회한 Entity 중 영속성 컨텍스트 1차캐시에 있는 Entity는 1차캐시에서 조회
    - **실제 DB의 Entity는 버린다**
    - 1차 캐시에 없으면 조회한 값을 1차캐시에 넣고 1차 캐시를 반환
- **영속성 컨텍스트는 영속 상태인 Entity의 동일성을 보장한다**
    - 따라서 `em.find()`이건 JPQL이건 동일한 Entity를 반환

#### `find()` vs JPQL

- `find()`는 1차캐시에서 Entity를 반환하고, 없으면 DB에서 조회
    - 메모리에서 바로 찾으므로 성능상 이점
- JPQL은 항상 DB에서 조회
    - DB에서 먼저 조회하고, 1차캐시에 있을 경우에는 조회한 데이터 버림

### 6.3 JPQL과 플러시 모드

- flush : 영속성 컨텍스트의 내용을 DB에 반영하는 것
    - `FlushModeType.AUTO` : 커밋이나 쿼리를 실행하기 전에 플러시 (기본값)
    - `FlushModeType.COMMIT` : 커밋할 때만 플러시

#### 쿼리와 플러시 모드

````
// FlushModeType.AUTO 일 떄
// 영속성 컨텍스트 변화
member.setName("카리나");


// JPQL 쿼리 실행
// 실행 전에 플러시가 자동으로 호출됨
Member member = em.createQuery("SELECT m FROM Member m Where m.name ='카리나`", Member.class)
                  .getSingleResult();

System.out.println(member.getName()); // 카리나
````

````
// FlushModeType.COMMIT 일 때

// 영속성 컨텍스트 변화
member.setName("카리나");

// em.flush(); // 직접 호출해도 됨

// JPQL 쿼리 실행
// 실행 전에 플러시가 자동으로 호출되지 않음
Member member = em.createQuery("SELECT m FROM Member m Where m.name ='카리나`", Member.class) 
                  .setFlushMode(FlushModeType.AUTO) // 플러시 모드 변경
                  .getSingleResult();
                  
System.out.println(member.getName()); // 카리나
````

#### 플러시 모드와 최적화

- JPA를 통하지 않는 어떤 방법이건 flush를 신경 써야함
    - 쿼리 전 flush를 하는 것이 안전
- `FlushModeType.COMMIT` : 트랜잭션 COMMIT 시에만 flush 함
    - 플러시가 자주 일어나는 상황에 적용할만함

## 7. 정리

- JPQL은 SQL을 추상화하여 DBMS에 의존하지 않음
- Criteria, QueryDSL은 JPQL을 작성해주는 빌더일 뿐
    - 동적 쿼리 작성이 편함
- Criteria는 JPA 공식 지원이지만 QueryDSL이 더 편함ㅣ
- JPA도 Native SQL을 지원하지만 DBMS 의존적이므로 가장 후순위에 둘 것
- JPQL은 벌크 연산을 지원
