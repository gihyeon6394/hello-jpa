package jpabook.jpashop.service;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.OrderRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:appConfig.xml")
@Transactional
public class OrderServiceTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;


    // 상품 주문
    @Test
    public void order() throws Exception {

        //Given
        Member member = createMember();
        Item item = createBook("에스파 특별 북", 10000, 10);
        int orderCnt = 2;

        // When
        Long orderId = orderService.order(member.getId(), item.getId(), orderCnt);

        // Then
        Order getOrder = orderRepository.findOne(orderId);

        assertEquals("상품 주문시 상태는 ORDER", OrderStatus.ORDER, getOrder.getStatus());
        assertEquals("주문한 상품 종류 수가 정확해야 한다.", 1, getOrder.getOrderItemList().size());
        assertEquals("주문 가격은 가격 * 수량이다.", 10000 * orderCnt, getOrder.getTotalPrice());
        assertEquals("주문 수량만큼 재고가 줄어야 한다.", 8, item.getStockquantity());


    }

    private Item createBook(String name, int price, int stockquantity) {
        Item item = new Item();
        item.setName(name);
        item.setPrice(price);
        item.setStockquantity(stockquantity);
        em.persist(item);
        return item;

    }

    private Member createMember() {
        Member member = new Member();
        member.setName("에스파를 좋아하는 홍길동");
        member.setAddress(new Address("서울", "강남구", "123-123"));
        em.persist(member);
        return member;
    }


    // 주문시 재고 수량 오버
    @Test(expected = NotEnoughStockException.class)
    public void exceptionWhenStockOver() {

        //Given
        Member member = createMember();
        Item item = createBook("에스파 특별 북", 10000, 10);

        int orderCnt = 11; // 재고보다 많은 수량 주문

        // When
        orderService.order(member.getId(), item.getId(), orderCnt);

        // Then
        fail("재고수량 부족 예외 발생해야함");

    }

    // 주문 취소
    @Test
    public void cancelOrder() {

        //Given
        Member member = createMember();
        Item item = createBook("에스파 특별 북", 10000, 10);
        int orderCnt = 2;

        Long orderId = orderService.order(member.getId(), item.getId(), orderCnt);

        // WHen
        orderService.cancelOrder(orderId);

        assertEquals("주문 취소시 상태는 CANCEL이다.", OrderStatus.CANCEL, orderRepository.findOne(orderId).getStatus());
        assertEquals("주문 취소시 재고가 증가해야 한다.", 10, item.getStockquantity());

    }
}
