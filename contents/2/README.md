# 2장 JPA 시작

1. 이클립스 설치와 프로젝트 불러오기
2. H2 데이터베이스 설치
3. 라이브러리와 프로젝트 구조
    1. 메이븐과 사용 라이브러리 관리
4. 객체 매핑 시작
5. persistence.xml 설정
    1. 데이터베이스 방언
6. 애플리케이션 개발
    1. 엔티티 매니저 설정
    2. 트랜잭션 관리
    3. 비즈니스 로직
    4. JPQL
7. 정리

---

## 1. 이클립스 설치와 프로젝트 불러오기

## 2. H2 데이터베이스 설치

## 3. 라이브러리와 프로젝트 구조

## 4. 객체 매핑 시작

```java
import javax.persistence.*;

@Entity
@Table(name = "MEMBER")
public class Member {

    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "NAME")
    private String username;

    private Integer age;

// ...

```

- `@Entity` : 클래스와 RDMBS 테이블의 매핑 관계
    - `@Entity`가 사용된 클래스를 엔티티 클래스라 함
- `@Table` : 엔티티 클래스에 매핑할 테이블 정보 명시
    - `name` : 매핑할 테이블 이름
- `@Id` : 엔티티 클래스의 필드를 테이블의 기본 키에 매핑, `@Id`가 사용된 필드를 식별자 필드라 함
- `@Column` : 필드를 컬럼에 매핑
    - `name` : 필드와 매핑할 테이블의 컬럼 이름
- 매핑 정보가 없는 필드 `age`
    - 필드명을 그대로 사용하여 컬럼명으로 매핑

## 5. persistence.xml 설정

/META-INF/persistence.xml에 있으면 JPA가 자동 인식

- `javax.persistence.jdbc.driver` : JDBC 드라이버 클래스
- `javax.persistence.jdbc.url` : JDBC URL
- `javax.persistence.jdbc.user` : 데이터베이스 접속 사용자 이름
- `javax.persistence.jdbc.password` : 데이터베이스 접속 사용자 비밀번호
- `hibernate.dialect` : 하이버네이트 방언
    - JPA 표준은 방언을 정의하지 않음
    - 하이버네이트는 JPA 표준을 따르면서도 방언 기능을 제공
    - 방언 : SQL 표준은 지키면서도 특정 데이터베이스에 종속되지 않도록 데이터베이스 마다 SQL을 다르게 생성하는 기능

### 5.1 데이터베이스 방언

- JPA는 특정 데이터베이스에 종속되지 않음
- 그러나 데이터베이스마다 제공하는 SQL 문법과 함수가 조금씩 다름

## 6. 애플리케이션 개발

````
public static void main(String[] args) {

    EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpabook");

    EntityManager em = emf.createEntityManager(); //엔티티 매니저 생성

    EntityTransaction tx = em.getTransaction(); //트랜잭션 기능 획득

    try {
        tx.begin(); //트랜잭션 시작
        logic(em);  //비즈니스 로직
        tx.commit();//트랜잭션 커밋

    } catch (Exception e) {
        e.printStackTrace();
        tx.rollback(); //트랜잭션 롤백
    } finally {
        em.close(); //엔티티 매니저 종료
    }

    emf.close(); //엔티티 매니저 팩토리 종료
}
    
public static void logic(EntityManager em) {

    String id = "idAESPA";
    Member member = new Member();
    member.setId(id);
    member.setUsername("카리나");
    member.setAge(22);

    //등록
    em.persist(member);

    //수정
    member.setAge(20);

    //한 건 조회
    Member findMember = em.find(Member.class, id);
    System.out.println("findMember=" + findMember.getUsername() + ", age=" + findMember.getAge());

    //목록 조회
    List<Member> members = em.createQuery("select m from Member m", Member.class).getResultList();
    System.out.println("members.size=" + members.size());

    //삭제
    em.remove(member);

}
````

### 6.1 엔티티 매니저 설정

- EntityManagerFactory 생성 : JPA 시작의 첫 단계
    - persistence.xml에 설정 정보를 읽어서 EntityManagerFactory를 생성
    - JPA 기반 객체를 만듦
    - 생성 비용이 큼
    - **application 전체에서 딱 한번만 생성하고 공유해서 사용해야함**
- EntityManager 생성 : JPA를 실제 사용하는 단계
    - EntityManagerFactory를 통해 EntityManager 생성
    - JPA 기능 대부분 제공
    - EntityManger를 사용해서 CRUD 가능
    - **EntityManager는 쓰레드간에 공유하면 안됨**
- 종료 : `emf.close();`
    - 사용이 끝난 EntityManger의 종료

### 6.2 트랜잭션 관리

- `tx.begin();` : 트랜잭션 시작
- `tx.commit();` : 트랜잭션 커밋
- `tx.rollback();` : 트랜잭션 롤백

### 6.3 비즈니스 로직

- 등록 : `em.persist(member);`
    - 저장할 Entity를 넘겨줌
    - JPA가 매핑정보를 분석해 `INSERT SQL` 생성
- 수정 : `member.setAge(20);`
    - JPA가 변경 감지 기능으로 `UPDATE SQL` 생성
- 삭제 : `em.remove(member);`
    - JPA가 `DELETE SQL` 생성
- 한 건 조회 : `em.find(Member.class, id);`
    - 조회할 엔티티 타입과 식별자 `@Id`로 Entity 하나를 조회
    - JPA가 `SELECT SQL` 생성

### 6.4 JPQL

- Entity를 대상으로 쿼리
- SQL과 비슷한 문법
- JPQL은 RDBMS TABLE을 모름
- JPA가 JPQL을 분석한 후 SQL을 생성해서 실행
