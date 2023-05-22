package study.datajpa.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDTO;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback(false)
class MemberRepositoryTest {
    @Autowired
    public MemberRepository memberRepository;
    @Autowired
    public TeamRepository teamRepository;
    @PersistenceContext
    private EntityManager em;
    @Test
    @Transactional
    void testMember() {
        Member member = new Member("memberA");
        System.out.println("memberRepository.getClass() : "+memberRepository.getClass());
        Member savedMember = memberRepository.save(member);

        Member findMember = memberRepository.findById(savedMember.getId()).get();

        Assertions.assertThat(savedMember).isEqualTo(findMember); //JPA 엔티티 동일성 보장
    }

    @Test
    void basicCRUD() {
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberRepository.save(member1);
        memberRepository.save(member2);

        // 단건 조회 검증
        Member findMember1 = memberRepository.findById(member1.getId()).get();
        Member findMember2 = memberRepository.findById(member2.getId()).get();
        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        // 리스트 조회 검증
        List<Member> all = memberRepository.findAll();
        assertThat(all.size()).isEqualTo(2);

        // 카운트 검증
        long count = memberRepository.count();
        assertThat(count).isEqualTo(2);

        // 삭제 검증
        memberRepository.delete(member1);
        memberRepository.delete(member2);
        long deletedCount = memberRepository.count();
        assertThat(deletedCount).isEqualTo(0);
    }

    @Test
    void findUsernameAndAgeGreaterThen() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("AAA", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByUsernameAndAgeGreaterThan("AAA", 10);

        assertThat(result.get(0).getUsername()).isEqualTo("AAA");
        assertThat(result.get(0).getAge()).isEqualTo(20);
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    void findHelloBy() {
        List<Member> helloBy = memberRepository.findHelloBy();
    }

    @Test
    void findTop3() {
        List<Member> helloBy = memberRepository.findTop3HelloBy();
    }

    @Test
    void testNamedQuery() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("AAA", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByUsername("AAA");
        Member findMember = result.get(0);

        assertThat(findMember).isEqualTo(m1);
    }

    @Test
    void testQuery() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("AAA", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findMember("AAA", 10);
        Member findMember = result.get(0);

        assertThat(findMember).isEqualTo(m1);
    }

    @Test
    void findUsernameList(){
        Member m1 = new Member("AAA");
        Member m2 = new Member("BBB");
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<String> usernameList = memberRepository.findUsernameList();
        for (String s : usernameList) {
            System.out.println("username : "+s);
        }
    }

    @Test
    void findMemberDTO(){
        Team teamA = new Team("teamA");
        teamRepository.save(teamA);

        Member m1 = new Member("AAA", 10, teamA);
        memberRepository.save(m1);

        List<MemberDTO> findMemberDTO = memberRepository.findMemberDTO();
        for (MemberDTO dto : findMemberDTO) {
            System.out.println("DTO : " + dto);
        }
    }

    @Test
    void findByNames() {
        Member m1 = new Member("AAA");
        Member m2 = new Member("BBB");
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByNames(Arrays.asList("AAA","BBB"));
        for (Member member : result) {
            System.out.println("Member : "+member);
        }
    }

    @Test
    void returnType() {
        Member m1 = new Member("AAA");
        Member m2 = new Member("BBB");
        memberRepository.save(m1);
        memberRepository.save(m2);

        Optional<Member> result = memberRepository.findOptionalByUsername("AAA");
        System.out.println("findMember = "+ result);

    }

    @Test
    void paging() {
        // given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));

        int age = 10;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        // when
        Page<Member> page = memberRepository.findByAge(age, pageRequest);

        Page<MemberDTO> toMap = page.map(m -> new MemberDTO(m.getId(), m.getUsername(), "팀"));


        // then
        List<Member> content = page.getContent(); // 조회된 데이터

        assertThat(content.size()).isEqualTo(3);          // 조회된 데이터 수
        assertThat(page.getTotalElements()).isEqualTo(5); // 전체 데이터 수
        assertThat(page.getNumber()).isEqualTo(0);        // 페이지 번호
        assertThat(page.getTotalPages()).isEqualTo(2);    // 전체 페이지 번호
        assertThat(page.isFirst()).isTrue();                      // 첫번째 항목인가?
        assertThat(page.hasNext()).isTrue();                      // 다음 페이지가 있는가?
    }

    @Test
    public void bulkUpdate() {
        // given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 19));
        memberRepository.save(new Member("member3", 20));
        memberRepository.save(new Member("member4", 21));
        memberRepository.save(new Member("member5", 40));

        // when
        int resultCount = memberRepository.bulkAgePlus(20);

        // then
        assertThat(resultCount).isEqualTo(3);
    }

    @Test
    public void findMemberLazy() {
        // given
        // member1 -> teamA
        // member2 -> teamB

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamRepository.save(teamA);
        teamRepository.save(teamB);

        Member member1 = new Member("member1", 1, teamA);
        Member member2 = new Member("member1", 1, teamB);
        memberRepository.save(member1);
        memberRepository.save(member2);

        em.flush();
        em.clear();

        // when
        List<Member> members = memberRepository.findEntityGraphByUsername("member1");

        for (Member member : members) {
            System.out.println("member name : "+member.getUsername());
            System.out.println("member team : "+member.getTeam());
        }

    }

    @Test
    void queryHint() {
        // given
        Member member1 = memberRepository.save(new Member("member1", 10));
        em.flush();
        em.clear();

        // when
        Member findMember = memberRepository.findReadOnlyByUsername("member1");
        findMember.setUsername("member2");

        em.flush(); // 읽기전용 이어서 update 되지 않음.
    }

    @Test
    void lock() {
        // given
        Member member1 = memberRepository.save(new Member("member1", 10));
        em.flush();
        em.clear();

        // when
        List<Member> result = memberRepository.findLockByUsername("member1");


        em.flush();
        // then
    }

    @Test
    public void callCustom() {
        List<Member> result = memberRepository.findMemberCustom();
    }
}