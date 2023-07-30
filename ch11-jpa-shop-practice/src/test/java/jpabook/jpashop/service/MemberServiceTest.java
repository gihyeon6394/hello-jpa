package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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

    @Test
    public void joinTest() throws Exception {

        // Given

        Member member = new Member();
        member.setName("카리나");

        // When
        Long idSaved = service.join(member);

        // Then
        assertEquals(member, repository.findOne(idSaved));

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

}
