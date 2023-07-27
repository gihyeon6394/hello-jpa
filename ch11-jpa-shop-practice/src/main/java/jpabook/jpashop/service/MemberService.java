package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class MemberService {


    @Autowired
    private MemberRepository repository;

    public Long join(Member m) {
        validateDuplicateMember(m);
        repository.save(m);
        return m.getId();
    }

    private void validateDuplicateMember(Member m) {
        List<Member> members = repository.findByName(m.getName());
        if (!CollectionUtils.isEmpty(members)) {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }

    public List<Member> findMembers() {
        return repository.findAll();
    }

    public Member findOne(Long id) {
        return repository.findOne(id);
    }
}
