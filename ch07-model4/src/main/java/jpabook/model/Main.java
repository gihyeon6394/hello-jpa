package jpabook.model;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import jpabook.model.practice.entity.*;
import jpabook.model.practice.entity.item.Album;
import jpabook.model.practice.entity.item.Item;

import java.util.Date;

/**
 * Created by 1001218 on 15. 4. 5..
 */
public class Main {

    public static void main(String[] args) {

        //엔티티 매니저 팩토리 생성
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpabook");
        EntityManager em = emf.createEntityManager(); //엔티티 매니저 생성

        EntityTransaction tx = em.getTransaction(); //트랜잭션 기능 획득

        try {

            tx.begin(); //트랜잭션 시작
            logic(em); //비즈니스 로직
            tx.commit();//트랜잭션 커밋

        } catch (Exception e) {
            e.printStackTrace();
            tx.rollback(); //트랜잭션 롤백
        } finally {
            em.close(); //엔티티 매니저 종료
        }

        emf.close(); //엔티티 매니저 팩토리 종료
    }

    private static void logic(EntityManager em) {
        // 회원 생성
        Member karina = new Member();
        karina.setName("카리나");
        karina.setCity("서울");
        karina.setStreet("강남");
        karina.setZipcode("12345");
        em.persist(karina);


        // 카테고리 생성
        Category ent = new Category();
        ent.setName("대분류 > 엔터테인먼트");
        em.persist(ent);

        Category entChild1 = new Category();
        entChild1.setParent(ent);
        entChild1.setName("중분류 > CD");
        em.persist(entChild1);

        ent.addChild(entChild1);


        Category entChild2 = new Category();
        entChild2.setParent(ent);
        entChild2.setName("중분류 > CD 플레이어");
        em.persist(entChild2);

        ent.addChild(entChild2);

        // 상품 생성
        Album nextLevel = new Album();
        nextLevel.setArtist("aespa");
        nextLevel.setName("Next Level");
        nextLevel.setPrice(10000);
        nextLevel.setStockQuantity(100);
        nextLevel.addCateogry(entChild1);
        nextLevel.addCateogry(entChild2);
        entChild1.addItem(nextLevel);
        em.persist(nextLevel);

        // 주문 생성
        Order order1 = new Order();
        order1.setMember(karina);
        order1.setStatus(OrderStatus.ORDER);
        order1.setCreatedDate(new Date());
        em.persist(order1);

        // 주문 상품 생성

        OrderItem orderItem1 = new OrderItem();
        orderItem1.setCount(3);
        orderItem1.setOrder(order1);
        orderItem1.setItem(nextLevel);
        orderItem1.setCreatedDate(new Date());
        em.persist(orderItem1);


        // 배송 생성
        Delivery delivery1 = new Delivery();
        delivery1.setCity(karina.getCity());
        delivery1.setStreet(karina.getStreet());
        delivery1.setZipcode(karina.getZipcode());
        delivery1.setStatus(DeliveryStatus.READY);
        delivery1.setOrder(order1);
        em.persist(delivery1);


    }

}
