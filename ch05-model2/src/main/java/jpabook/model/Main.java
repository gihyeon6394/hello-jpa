package jpabook.model;

import jpabook.model.entity.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.util.Date;

/**
 * Created by 1001218 on 15. 4. 5..
 */
public class Main {

    public static void main(String[] args) {

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpabook");
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction(); //트랜잭션 기능 획득

        try {

            tx.begin(); //트랜잭션 시작

            logic1(em);

            tx.commit();//트랜잭션 커밋

        } catch (Exception e) {
            e.printStackTrace();
            tx.rollback(); //트랜잭션 롤백
        } finally {
            em.close(); //엔티티 매니저 종료
        }

        emf.close(); //엔티티 매니저 팩토리 종료
    }

    private static void logic1(EntityManager em) {


        Member karina = new Member();
        karina.setName("karina");
        karina.setCity("seoul");
        karina.setStreet("gangnam");
        karina.setZipcode("12345");
        em.persist(karina);

        Item nextLevelAlbum = new Item();
        nextLevelAlbum.setName("Album : NEXT LEVEL");
        nextLevelAlbum.setPrice(10000);
        nextLevelAlbum.setStockQuantity(10);
        em.persist(nextLevelAlbum); //item, member 저장

        // 주문생성
        Order order = new Order();
        order.setMember(karina);
        order.setOrderDate(new Date());
        order.setStatus(OrderStatus.ORDER);

        em.persist(order); //order 저장

        // 주문상품생성
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setItem(nextLevelAlbum);
        em.persist(orderItem); //orderItem 저장


        // 트랙잭션 commit 이전 객체 그래프 탐색
        System.out.println("주문한 사람 : " + order.getMember());
        System.out.println("주문 내역 : " + karina.getOrders());
        System.out.println("2번 객체 그래프 탐색하기 : " + orderItem.getOrder().getMember().getName());

    }

}
