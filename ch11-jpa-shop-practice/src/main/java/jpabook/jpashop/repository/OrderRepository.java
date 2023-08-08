package jpabook.jpashop.repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jpabook.jpashop.domain.*;
import jpabook.jpashop.domain.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;


@Repository
public class OrderRepository {

    @PersistenceContext
    private EntityManager em;

    // JPAQueryFactory Bean 설정 후 주입
    @Autowired
    private JPAQueryFactory jpaQueryFactory;

    public void save(Order order) {
        em.persist(order);
    }

    public Order findOne(Long id) {
        return em.find(Order.class, id);
    }

    public List<Order> findAll(OrderSearch orderSearch) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> o = cq.from(Order.class);

        List<Predicate> criteria = new ArrayList<>();

        if (orderSearch.getOrderStatus() != null) {
            Predicate status = cb.equal(o.get("status"), orderSearch.getOrderStatus());
            criteria.add(status);
        }

        if (StringUtils.isEmpty(orderSearch.getMemberName())) {
            Join<Order, Member> m = o.join("member", JoinType.INNER);
            Predicate name = cb.like(m.<String>get("name"), "%" + orderSearch.getMemberName() + "%");
            criteria.add(name);
        }

        cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
        TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000); //최대 1000건

        return query.getResultList();

    }

    public List<Tuple> SubQueryTest() {
        QMember qMember = new QMember("m"); // 별칭 M 지정
        QOrder subOrders = new QOrder("o"); // 별칭 M 지정

        return jpaQueryFactory.select(qMember.id,
                        qMember.name,
                        JPAExpressions.select(subOrders.orderdate.max())
                                .from(subOrders)
                                .where(subOrders.member.eq(qMember))
                )
                .from(qMember)
                .fetch();

    }
}
