package jpabook.jpashop.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.persistence.*;

@Entity
@Table(name="DELIVERY")
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;

    @Embedded
    private Address address;

    @OneToOne(mappedBy = "delivery")
    private Order order;

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }


    @Override
    public String toString() {
        return ToStringBuilder
                .reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
