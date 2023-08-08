package jpabook.jpashop.service;

import com.querydsl.core.Tuple;
import jpabook.jpashop.domain.*;
import jpabook.jpashop.repository.OrderRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:appConfig.xml")
@Transactional
@Rollback(false)
public class QueryDSLTest {

    @Autowired
    private OrderRepository orderRepository;

    @PersistenceContext
    private EntityManager em;


    @Test
    public void queryDSLSubQueryTest() throws Exception {

        // Given

        // 주소
        Address address = new Address();
        address.setCity("서울시");
        address.setStreet("강남구");
        address.setZipcode("123-123");


        // 회원
        Member karina = new Member();
        karina.setName("카리나");
        karina.setAddress(address);
        em.persist(karina);

        Member winter = new Member();
        winter.setName("윈터");
        winter.setAddress(address);
        em.persist(winter);

        // 카테고리
        Category category = new Category();
        category.setName("카테고리1");

        em.persist(category);

        // 주문
        Order order = new Order();
        order.setMember(karina);
        order.setStatus(OrderStatus.ORDER);
        order.setOrderdate(new Date());
        em.persist(order);


        // When
        List<Tuple> result = orderRepository.SubQueryTest();


        // Then
        assertTrue(result.size() > 0);
        System.out.println("result = " + result.toString());

    }

}
