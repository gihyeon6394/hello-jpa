package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.item.Album;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.MemberRepository;
import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:appConfig.xml")
@Transactional
public class MemberServiceTest {

    @Autowired
    private MemberService service;

    @Autowired
    private MemberRepository repository;

    @PersistenceContext
    private EntityManager em;

    @Test
    public void joinTest() throws Exception {

        // Given

        Member member = new Member();
        member.setName("카리나");

        // When
        Long idSaved = service.join(member);

        // Then
        // assertEquals(member, repository.findOne(idSaved));

        assertTrue(member == repository.findOne(idSaved)); // 1차 캐시에서 조회


    }

    @Test(expected = IllegalStateException.class)
    public void test2() throws Exception {

        // Given
        Member karina = new Member();
        karina.setName("카리나");

        Member kaina_ = new Member();
        kaina_.setName("카리나");

        // When
        service.join(karina);
        service.join(kaina_); // 예외가 발생해야 한다.

        // Then
        fail("이미 존재하는 회원입니다.");
    }

    @Test
    public void testProxy() {
        Member karina = new Member();
        karina.setName("카리나");

        em.persist(karina);
        em.flush();
        em.clear();

        Member refmember = em.getReference(Member.class, karina.getId()); // proxy 객체
        Member findMember = em.find(Member.class, karina.getId()); // proxy 객체

        System.out.println("refmember = " + refmember.getClass());
        System.out.println("findMember = " + findMember.getClass());

        assertTrue(refmember == findMember);
    }

    @Test
    public void testProxyType() {
        Member karina = new Member();
        karina.setName("카리나");

        em.persist(karina);
        em.flush();
        em.clear();

        Member refmember = em.getReference(Member.class, karina.getId()); // proxy 객체
        System.out.println("refmember = " + refmember.getClass()); // refmember = class jpabook.jpashop.domain.Member_$$_jvstdd1_5

        assertFalse(Member.class == refmember.getClass());
        assertTrue(refmember instanceof Member);

    }

    @Test
    public void testProxyEquality() {
        Member karina = new Member();
        karina.setName("카리나");

        em.persist(karina);
        em.flush();
        em.clear();

        Member karinaNew = new Member(karina.getId(), karina.getName());
        Member karinaRef = em.getReference(Member.class, karina.getId());

        assertTrue(karinaNew.equals(karinaRef)); // failed

    }

}
