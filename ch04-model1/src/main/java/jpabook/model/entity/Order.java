package jpabook.model.entity;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by holyeye on 2014. 3. 11..
 * modified by kgh 2023. 7. 16
 */
@Entity
@Table(name = "ORDERS") // Mysql `ORDER` 는 예약어
public class Order {

    @Id
    @Column(name = "ORDER_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "MEMBER_ID")
    private Long memberId;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "ORDER_DATE")
    private Date orderDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS")
    private OrderStatus status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}
