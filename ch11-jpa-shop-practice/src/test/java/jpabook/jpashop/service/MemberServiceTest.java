package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.transaction.Transactional;

import static org.junit.Assert.assertEquals;

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


    }
}
