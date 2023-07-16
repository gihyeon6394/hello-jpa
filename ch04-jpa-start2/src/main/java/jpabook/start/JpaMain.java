package jpabook.start;

import javax.persistence.*;
import java.util.List;

/**
 * @author holyeye
 */
public class JpaMain {

    public static void main(String[] args) {

        //엔티티 매니저 팩토리 생성
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpabook");
        EntityManager em = emf.createEntityManager(); //엔티티 매니저 생성

        EntityTransaction tx = em.getTransaction(); //트랜잭션 기능 획득

        try {


            tx.begin(); //트랜잭션 시작
            logic(em);  //비즈니스 로직
            logic2(em);
            tx.commit();//트랜잭션 커밋

        } catch (Exception e) {
            e.printStackTrace();
            tx.rollback(); //트랜잭션 롤백
        } finally {
            em.close(); //엔티티 매니저 종료
        }

        emf.close(); //엔티티 매니저 팩토리 종료
    }

    private static void logic2(EntityManager em) {
        Board board1 = new Board();
        board1.setTitle("제목1");
        em.persist(board1);
        System.out.println("1. board.id = " + board1.getId());

        Board board2 = new Board();
        board2.setTitle("제목2");
        em.persist(board2);
        System.out.println("2. board.id = " + board2.getId());
    }

    public static void logic(EntityManager em) {

        String id = "id1";
        Member karina = new Member();
        karina.setId(id);
        karina.setUsername("karina");
        karina.setAge(2);

        //등록
        em.persist(karina);

        //수정
        karina.setAge(20);

        //한 건 조회
        Member findMember = em.find(Member.class, id);
        System.out.println("findMember=" + findMember.getUsername() + ", age=" + findMember.getAge());

        //목록 조회
        List<Member> members = em.createQuery("select m from Member m", Member.class).getResultList();
        System.out.println("members.size=" + members.size());

        //삭제
        em.remove(karina);

    }
}
