# 4장 엔티티 매핑

1. `@Entity`
2. `@Table`
3. 다양한 매핑 사용
4. 데이터베이스 스키마 자동 생성
5. DDL 생성 기능
6. 기본 키 매핑
7. 필드와 컬럼 매핑 : 레퍼런스
8. 정리

- 실전 예제 1. 요구사항 분석과 기본 매핑

---

- 객체와 테이블 매핑 : `@Entity`, `@Table`
- 기본 키 매핑 : `@Id`
- 필드와 컬럼 매핑 : `@Column`
- 연관관계 매핑 : `@ManyToOne`, `@JoinColumn`

## 1. `@Entity`

- 테이블과 매핑할 클래스에 `@Entity` 애노테이션을 붙여줌
- `@Entity`가 붙은 클래스는 JPA가 관리하는 것으로, 엔티티라고 함

| 속성     | 기능                                                                 | 기본값    |
|--------|--------------------------------------------------------------------|--------|
| `name` | JPA에서 사용할 Entity 이름 지정<br/> 다른 패키지에 이름이 같은 클래스가 있다면 이름을 지정해서 충돌 방지 | 클래스 이름 |

#### 주의사항

- 기본생성자 필수
    - public, protected 생성자
- final 클래스, enum, interface, inner 클래스에 사용 불가
- 저장할 필드에 final 사용 불가

## 2. `@Table`

- Entity와 매핑할 테이블을 지정
- 생략하면 Entity 이름을 테이블 이름으로 사용

| 속성                      | 기능                                               | 기본값       |
|-------------------------|--------------------------------------------------|-----------|
| `name`                  | 매핑할 테이블 이름                                       | Entity 이름 |
| `catalog`               | catalog 기능이 있는 DB에서 catalog 매핑                   |           |
| `schema`                | schema 기능이 있는 DB에서 schema 매핑                     |           |
| `uniqueConstraints` DDL | DDL 생성 시 UNIQUE 제약조건을 만듦<br/>스키마 자동생성 기능 사용시 사용됨 |           |

## 3. 다양한 매핑 사용

````java

@Entity
@Table(name = "MEMBER")
public class Member {

    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "NAME")
    private String username;

    private Integer age;

    //=== 추가
    @Enumerated(EnumType.STRING)
    private RoleType roleType;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastModifiedDate;

    @Lob
    private String description;

    @Transient
    private String temp;

    //Getter, Setter
}
````

- `@Enumerated` : Java의 Enum을 사용하기 위함
- `@Temporal` : Java의 날짜 타입 매핑
- `@Lob` : BLOB, CLOB 매핑

## 4. 데이터베이스 스키마 자동 생성

````
<property name="hibernate.hbm2ddl.auto" value="create"/>
````

- JPA는 데이터베이스 스키마를 자동으로 생성하는 기능을 지원
- Entity의 매핑 정보와 데이터베이스 방언을 사용해서 데이터베이스 스키마를 생성
- **개발환경이나 매핑 확인 용도로만 사용하는 것이 적절**
- **매핑 정보 학습도구**로 매우 적절

#### `hibernate.hbm2ddl.auto` 속성

| 속성            | 기능                                                                                   |
|---------------|--------------------------------------------------------------------------------------|
| `create`      | DROP + CREATE<br/>기존 테이블 제거 후 새로 생성                                                  |
| `create-auto` | DROP + CREATE+ DROP<br/>application 종료 시 생성한 DDL 제거                                  |
| `update`      | DB 테이블 정보와 Entity 매핑 정보를 비교해서 변경사항 수정                                                |
| `validate`    | DB 테이블 정보와 Entity 매핑 정보를 비교해서<br/>차이가 있으면 경고마 남기고, application 실행 안함<br/>DDL 수정하지 않음 |
| `none`        | 자동 생성기능을 사용하지 않음 <br/>                                                               |

> ### 자동 생성 기능 사용 전략
> - **운영 환경에서 사용 금지**
> - 개발 초기 단계 : `create` or `update`
> - 자동화된 테스트, CI 서버 : `create` or `create-auto`
> - 테스트 서버 : `update` or `validate`
> - 스테이징, 운영 서버 : `validate` or `none`

#### 이름 매핑 전략

- Java의 camelcase와 DB의 snakecase를 매핑하는 전략
- `org.hibernate.boot.model.naming.PhysicalNamingStrategy` 구현체를 만들어서 등록
    - since hibernate 5

````
<!--persistent.xml-->
<property name="hibernate.physical_naming_strategy"
          value="jpabook.start.CustomPhysicalNamingStrategy"/>
````

<details><summary>CustomPhysicalNamingStrategy.java</summary>

````java
package jpabook.start;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

public class CustomPhysicalNamingStrategy implements PhysicalNamingStrategy {

    @Override
    public Identifier toPhysicalCatalogName(final Identifier identifier, final JdbcEnvironment jdbcEnv) {
        if (identifier == null)
            return null;
        return convertToSnakeCase(identifier);
    }

    @Override
    public Identifier toPhysicalColumnName(final Identifier identifier, final JdbcEnvironment jdbcEnv) {
        return convertToSnakeCase(identifier);
    }

    @Override
    public Identifier toPhysicalSchemaName(final Identifier identifier, final JdbcEnvironment jdbcEnv) {
        if (identifier == null)
            return null;
        return convertToSnakeCase(identifier);
    }

    @Override
    public Identifier toPhysicalSequenceName(final Identifier identifier, final JdbcEnvironment jdbcEnv) {
        return convertToSnakeCase(identifier);
    }

    @Override
    public Identifier toPhysicalTableName(final Identifier identifier, final JdbcEnvironment jdbcEnv) {
        return convertToSnakeCase(identifier);
    }

    private Identifier convertToSnakeCase(final Identifier identifier) {
        final String regex = "([a-z])([A-Z])";
        final String replacement = "$1_$2";
        final String newName = identifier.getText()
                .replaceAll(regex, replacement)
                .toLowerCase();
        return Identifier.toIdentifier(newName);
    }
}
````

</details>

## 5. DDL 생성 기능

- DDL 작성 시에만 사용됨
- JPA 실행 로직에는 영향 없음
- Entity만 보고 제약조건 등을 파악하는 장점

````java

@Entity
@Table(name = "MEMBER", uniqueConstraints = {@UniqueConstraint(
        name = "NAME_AGE_UNIQUE",
        columnNames = {"NAME", "AGE"}
)})
public class Member {

    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "NAME", nullable = false, length = 10)
    private String username;

    private Integer age;

    //Getter, Setter ...
}
````

- `@Table` > `uniqueConstraints` : DDL 생성 시 유니크 제약조건을 만듦
    - `name` : 제약조건 이름
    - `columnNames` : 유니크 제약조건을 사용할 컬럼 이름들
- `@Column`
    - `nullable` : null 허용 여부
    - `length` : 문자 길이 제약조건

````sql
 create table member
 (
     id                 varchar(255) not null,
     age                integer,
     created_date       datetime(6),
     description        longtext,
     last_modified_date datetime(6),
     role_type          varchar(255),
     name               varchar(10)  not null,
     primary key (id)
 ) engine=InnoDB

alter table member
    add constraint NAME_AGE_UNIQUE unique (name, age)
````

## 6. 기본 키 매핑

디비마다 Primary Key를 생성하는 방식이 다름, Oracle Sequence, MySQL Auto Increment, ...

#### JPA의 Primary Key todtjd wjsfir

- 직접 할당 : 기본 키를 applicaiton에서 직접 할당, `@Id`
- 자동 생성 : 대리 키 사용 방식, `@Id` + `@GeneratedValue`
    - `IDENTITY` : 기본 키 생성을 데이터베이스에 위임
    - `SEQUENCE` : 데이터베이스 시퀀스를 사용해서 기본 키를 할당
    - `TABLE` : 키 생성 테이블을 사용

### 6.1 기본키 직접 할당 전략

````
@Id
@Column(name = "ID")
private String id;

...

Member karina = new Member();
karina.setId("id001"); // 기본 키 직접 할당
karina.setUsername("karina");
em.persist(karina);
````

적용 가능 Java 타입

- Java 기본형
- Java Wrapper Class
- String
- `java.util.Date`, `java.sql.Date`
- `java.math.BigDecimal`, `java.math.BigInteger`

### 6.2 IDENTITY 전략

- 기본 키 생성을 데이터베이스에 위임
- 주로 MySQL, PostgreSQL, SQL Server, DB2에서 사용
- DB에 값을 저장하고 나서야 기본 키 값을 구할 수 있음
    - Entity의 식별자 값은 `@Id`기 때문에 **쓰기지연 없이** `persist()`를 호출하면 INSERT SQL을 DB에 보냄
- **JPA가 기본 키값을 가져오기위해 저장 시점에서 추가로 조회함**
    - JDBC3에 추가된 `getGeneratedKeys()`를 사용하면 저장과 동시에 키 값을 얻음. **DB와 한번만 통신**

````
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
@Column(name = "ID")
private Long id;

Member karina = new Member();
karina.setUsername("karina");
em.persist(karina);
system.out.println(karina.getId()); // null이 아님, PK 값
````

### 6.3 SEQUENCE 전략

- SEQUENCE : 데이터베이스 시퀀스는 유일한 값을 순서대로 생성하는 특별한 데이터베이스 오브젝트
- 오라클, PostgreSQL, DB2, H2 데이터베이스에서 사용 가능
- 먼저 DB에 식별자 값을 조회해서 Entity에 할당한 다음 영속성 컨텍스트에 저장

```java

@Entity
@SequenceGenerator(
        name = "MEMBER_SEQ_GENERATOR",
        sequenceName = "MEMBER_SEQ", // 매핑할 데이터베이스 시퀀스 이름
        initialValue = 1, allocationSize = 1)
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = "MEMBER_SEQ_GENERATOR")
    private Long id;

    // ...
}
````

#### `@SequcneGenerator` 속성

| 속성               | 기능                  | 기본값                       |
|------------------|---------------------|---------------------------|
| `name`           | 식별자 생성기 이름          | 필수                        |
| `sequenceName`   | DB object 이름        | hibernate_sequence        |
| `initialValue`   | 최초 시작 값<br/>DDL에 사용 | 1                         |
| `allocationSize` | 증분값                 | **50**<br/>성능으로 인해 50으로 둠 |

#### SEQUENCE 전략과 최적화

````sql
SELECT MEMBER_SEQ.nextval
FROM DUAL;

INSERT INTO MEMBER (ID, AGE, NAME)...;
````

- 식별자 값을 위해 2번 통신함
- `allocationSize`를 적절히 조정해서 성능을 최적화할 수 있음
    - 50으로 설정하면 한번에 50을 증가시키고, 50개를 application memory에 올려둠
    - DB에 직접 접속 시 한번에 50씩 증분한다는 것에 주의해야함 `increment by 50`

#### IDENTITY vs SEQUENCE `persist()`

| 속성         | 식별자 값 조회 시점              | 쓰기지연 여부 |
|------------|--------------------------|---------|
| `IDENTITY` | DB INSERT 이후             | 미사용     |
| `SEQUENCE` | DB INSERT 이전 먼저 식별자 값 조회 | 사용      |

### 6.4 TABLE 전략

- 키 생성 전용 테이블을 하나 만들어서 데이터베이스 시퀀스를 흉내내는 전략
- SELECT -> UPDATE 총 2번의 디비 통신이 필요함
    - SELECT : 다음 키 값을 가져옴
    - UPDATE : 키 값을 증가시킴

````sql
CREATE TABLE MY_SEQUENCES
(
    sequence_name VARCHAR(255) NOT NULL,
    next_val      bigint,
    PRIMARY KEY (sequence_name)
)
````

```java

@Entity
@Table(name = "BOARD")
@TableGenerator(
        name = "BOARD_SEQ_GENERATOR",
        table = "MY_SEQUENCES",
        pkColumnValue = "BOARD_SEQ", allocationSize = 1)
public class Board {

    @Id
    @Column(name = "BOARD_ID")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "BOARD_SEQ_GENERATOR")
    private Long id;

    //..
}
````

#### `@TableGenerator`

| 속성                    | 기능          | 기본값                 |
|-----------------------|-------------|---------------------|
| `name`                | 식별자 생성기 이름  | 필수                  |
| `table`               | 키 생성기 테이블명  | hibernate_sequences |
| `pkColumnName`        | 시퀀스 컬럼명     | sequence_name       |
| `valueColumnName`     | 시퀀스 값 컬럼명   | next_val            |
| `pkColumnValue`       | 키로 사용할 값 이름 | Entity 이름           |
| `uniqueContratis` DDL | UNIQUE 제약조건 |                     |

### 6.5 AUTO 전략

- 데이터베이스별로 방언에 따라 자동 지정
- MySQL : `IDENTITY`, Oracle : `SEQUENCE`, ...
- `@GeneratedValue`의 기본값
- 스키마 자동생성을 사용하면, JPA가 적절하게 SEQUENCE, 키 테이블을 생성함
- 데이터베이스에 의존하지 않는 코드
- 키 생성 전략이 정해지지 않은 개발 초기단계에서 사용

### 6.6 기본 키 매핑 정리

- 기본키는 Entity의 식별자 값으로 반드시 있어야함
- `em.persist()` 호출 시점에 동작이 식별자 할당 전략별로 다름
- 직접 할당 : `persist()` 직전 applicaiton 에서 직접 할당해주어야함, 아니면 예외 발생
- `SEQUENCE` : DB에서 식별자 값 조회 -> Enity에 할당 -> 영속성 컨텍스트에 저장
- `TABLE` : DB에서 키 값을 조회 -> Enity에 할당 -> 영속성 컨텍스트에 저장
- `IDENTITY` : **DB에 Entity를 저장** -> 식별자 값 획득 -> Entity에 할당 -> 영속성 컨텍스트에 저장

## 7. 필드와 컬럼 매핑 : 레퍼런스

JPA가 제공하는 필드-컬럼 매핑용 어노테이션  
[Hibernate Docs > Mapping Annotations > JPA annotations](https://docs.jboss.org/hibernate/orm/5.4/userguide/html_single/Hibernate_User_Guide.html#annotations-jpa)

- 필드-컬럼 매핑
    - `@Column` : 컬럼 매핑
    - `@Enumerated` : 자바 enum 타입 매핑
    - `@Temporal` : 날짜 타입 매핑
    - `@Lob` : BLOB, CLOB 매핑
    - `@Transient` : 특정 필드를 컬럼에 매핑하지 않음
- 기타
    - `@Access` : JPA가 엔티티에 접근하는 방식 지정

### 7.1 `@Column`

- 객체 필드를 테이블 컬럼에 매핑
- `name`, `nullable` 외에는 잘 사용 안됨

| 속성                       | 기능                                                                 | 기본값                              |
|--------------------------|--------------------------------------------------------------------|----------------------------------|
| `name`                   | 필드와 매핑할 테이블 컬럼 이름                                                  | 객체의 필드 이름                        |
| `insertable`             | 엔티티 저장 시, 이 필드도 같이 저장할지 여부<br/>`false` : 읽기 전용                     | `true`                           |
| `updatable`              | 엔티티 수정 시, 이 필드도 같이 수정할지 여부<br/>`false` : 읽기 전용                     | `true`                           |
| `table`                  | 하나의 Entity를 두개 이상의 테이블에 매핑할 때 사용                                   | 현재 클래스가 매핑된 테이블                  |
| `nullable` DDL           | `NOT NULL` 제약조건을 사용할지 여부                                           | `true`                           |
| `unique` DDL             | `UNIQUE` 제약조건을 사용할지 여부 <br/>컬럼 레벨에서 단일 컬럼에 제약조건 추가 시 사용            |                                  |
| `columnDefinition` DDL   | 데이터베이스 컬럼 정보를 직접 줄 수 있음                                            | 필드 타입, 방언을 사용                    |
| `length` DDL             | 문자 길이 제약조건, String 타입에만 사용                                         | 255                              |
| `precision`, `scale` DDL | BigDecimal 타입에서 사용, `precision`은 소수점을 포함한 전체 자릿수, `scale`은 소수의 자릿수 | `precision` = 19<br/>`scale` = 2 |

#### `@Column`과 NOT NULL 제약조건

- java 기본형 사용 시 `nullable = false`가 안전함
- `@Column`생략 + Java 기본형 = Not Null 제약조건이 붙음
- `@Column`생략 + Java 래퍼 클래스 = Not Null 제약조건이 붙지 않음
- `@Column` + Java 기본형 = Not Null X
    - 따라서 `@Column(nullable = false)`를 명시해주는 것이 안전

### 7.2 `@Enumerated`

- 자바 enum 타입을 매핑할 때 사용
- `EnumType.ORDINAL` (기본값) : enum 순서를 DB에 저장, e.g. 1, 2, 3
    - 데이터베이스 저장 값이 작음
    - enum 순서가 바뀌면 데이터베이스에 저장된 값이 달라짐
- `EnumType.STRING` **(권장)** : enum 이름을 DB에 저장, e.g. `ADMIN`, `USER`, ...
    - 데이터베이스 저장 값이 큼
    - Java enum의 순서에 의존하지 않음

<details>
<summary>예제</summary>

````
enum RoleType {
    ADMIN, USER
}

@Enumerated(EnumType.STRING)
private RoleType roleType;

member.setRoleType(RoleType.ADMIN);
em.persist(member); // role_type = 'ADMIN'
````

</details>

### 7.3 `@Temporal`

- 날짜 타입을 매핑할 때 사용
    - `java.util.Date`, `java.util.Calendar`
- `@Temporal` 생략시 timestamp 타입으로 로 DDL 생성
    - MySQL : datetime
- `TemporalType` 필수로 지정해야함
- `TemporalType.DATE` : 날짜, 데이터베이스 date 타입과 매핑
    - e.g. `2021-01-01`
- `TemporalType.TIME` : 시간, 데이터베이스 time 타입과 매핑
    - e.g. `12:34:56`
- `TemporalType.TIMESTAMP` : 날짜와 시간, 데이터베이스 timestamp 타입과 매핑
    - e.g. `2021-01-01 12:34:56`

<details>
<summary>예제</summary>

````
@Temporal(TemporalType.DATE)
private Date date; // 날짜

@Temporal(TemporalType.TIME)
private Date time; // 시각

@Temporal(TemporalType.TIMESTAMP)
private Date timestamp; // 날짜 + 시각

// DDL
create table tb (
    date date,
    time time,
    timestamp timestamp
)
````

</details>

### 7.4 `@Lob`

- DB의 BLOB, CLOB 타입과 매핑
- CLOB : String, char[], java.sql.CLOB
- BLOB : byte[], java.sql.BLOB

<details>
<summary>예제</summary>

````
@Lob
private string lobString; // CLOB

@Lob 
private byte[] lobByte; // BLOB

// DDL
// Oracle
create table tb (
    lobString clob,
    lobByte blob
)

// MySQL
create table tb (
    lobString longtext,
    lobByte longblob
)
````

</details>

### 7.5 `@Transient`

- DB에 매핑하지 않음
- 객체에 임시로 어떤 값을 보관하고 싶을 때 사용

### 7.6 `@Access`

- JPA가 엔티티에 접근하는 방식 지정
- `AccessType.FIELD` : 필드에 직접 접근, private 필드도 접근 가능
- `AccessType.PROPERTY` : 접근자(getter)를 통해서만 접근
- `@Id`가 필드에 있으면 `AccessType.FIELD`, 접근자에 있으면 `AccessType.PROPERTY`로 동작

<details>
<summary>예제 - 필드 접근</summary>

````
@Entity
@Access(AccessType.FIELD)
public class Member {
    @Id
    private Long id;
    
    private String name;
    
    // getter, setter
}
````

- `@Id`가 필드에 붙어있기 때문에 필드 접근 방식으로 동작
- `@Acess` 생략가능

</details>

<details>
<summary>예제 - 프로퍼티 접근</summary>

````
@Entity
@Access(AccessType.PROPERTY)
public class Member {
    private Long id;
    
    private String name;
    
    @Id
    public Long getId() {
        return id;
    }
    
    @Column(name = "name")
    public Stirng getName() {
        return name;
    }
    
    // getter, setter
}
````

- `@Id`가 접근자(getter)에 붙어있기 때문에 프로퍼티 접근 방식으로 동작
- `@Acess` 생략가능

</details>

<details>
<summary>예제 - 필드, 프로퍼티 혼용</summary>

````
@Entity
public class Member {
    @Id
    private Long id;
    
    @Transient 
    private String firstName;
    
    @Transient
    private String lastName;
    
    @Access(AccessType.PROPERTY)
    public String getFullName() {
        return firstName + lastName;
    }
    
    // getter, setter
}
````

- `@Id`가 필드에 있기 떄문에 필드 접근 방식으로 동작
- `getFullName()`은 프로퍼티 접근 방식으로 동작
    - JPA는 db full_name 컬럼에 접근할 때 `getFullName()`을 사용

</details>

## 8. 정리

- 객체와 테이블의 매핑 방법
    - 기본키, 컬럼 매핑
- DB 스키마 자동 생성 방법
    - Entity를 먼저 작성한 뒤 자동 DDL을 통해 스키마를 생성
- JPA 기본 키 매핑 전략, `SEQUENCE`, `IDENTITY`, `TABLE`

