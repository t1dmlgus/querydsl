package com.s1.querydsl.domain.member;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.s1.querydsl.domain.team.QTeam;
import com.s1.querydsl.domain.team.Team;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.persistence.TypedQuery;

import java.util.List;

import static com.s1.querydsl.domain.member.QMember.member;
import static com.s1.querydsl.domain.team.QTeam.team;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberTest {

    @Autowired
    EntityManager em;

    JPAQueryFactory queryFactory;

    @BeforeEach
    public void before(){

        queryFactory = new JPAQueryFactory(em);


        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);

        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        // 초기화
        em.flush();     // 영속성 컨텍스트에 있는 엔티티를 실제 쿼리를 만들어서 DB에 날림
        em.clear();     // 영속성 컨텍스트를 완전히 초기화, 캐시같은거 다 날라간다.


    }


    @Test
    public void 관계_테스트() throws Exception{
        //given

        //when
        List<Member> members =
                em.createQuery("select m from Member m", Member.class)
                .getResultList();

        for (Member member : members) {
            System.out.println("member = " + member);
            System.out.println("member.getTeam() = " + member.getTeam());
        }

    }

    @Test
    public void 쿼리_JPQL() throws Exception{

        // member1을 찾아라.
        String qlString = "select m from Member m "
                + "where m.username = :username";

        Member findMember = em.createQuery(qlString, Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        // then
        assertThat(findMember.getUsername()).isEqualTo("member1");

    }
    
    @Test
    public void 쿼리_Querydsl() throws Exception{
        // member1을 찾아라.

        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        //then
        assertThat(findMember.getUsername()).isEqualTo("member1");

    }

    @Test
    public void 검색쿼리() throws Exception{
        //given

        //when
        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1")
                        .and(member.age.between(10, 30)))
                .fetchOne();

        //then
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void 검색쿼리_Param() throws Exception{
        //given

        //when
        Member findMember = queryFactory
                .selectFrom(member)
                .where(
                        member.username.eq("member1"),
                        member.age.eq(10)
                )
                .fetchOne();

        //then
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }


    // 결과조회
    @Test
    public void resultFetch() throws Exception{

//        Member fetchOne = queryFactory
//                .selectFrom(QMember.member)
//                .fetchOne();

        QueryResults<Member> results = queryFactory
                .selectFrom(member)
                .fetchResults();


        long total = results.getTotal();
        System.out.println("total = " + total);
        List<Member> results1 = results.getResults();
        for (Member member1 : results1) {
            System.out.println("member1 = " + member1);
        }

        //then
    }

    
    @Test
    public void 정렬() throws Exception{

        /**
         * 회원 정렬 순서
         * 1. 회원 나이 내림차순(desc)
         * 2. 회원 이름 올림차순(asc)
         *
         * 단, 2에서 회원 이름이 없으면 마지막에 출력(nulls is last)
         *
         */

        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));


        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();

        //then
        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member memberNull = result.get(2);

        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(memberNull.getUsername()).isNull();

    }

    @Test
    public void 페이징() throws Exception{
        //given
        
        //when
        List<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetch();

        for (Member member1 : result) {
            System.out.println("member1 = " + member1);
        }

        //then
        assertThat(result.size()).isEqualTo(2);
    }


    @Test
    public void 집합() throws Exception{
        //given

        //when
        Tuple tuple1 = queryFactory
                .select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min()
                )
                .from(member)
                .fetchOne();

        //then
        System.out.println("tuple1 = " + tuple1);

        assertThat(tuple1.get(member.count())).isEqualTo(4);
        assertThat(tuple1.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple1.get(member.age.avg())).isEqualTo(25.0);
        assertThat(tuple1.get(member.age.max())).isEqualTo(40);
        assertThat(tuple1.get(member.age.min())).isEqualTo(10);
        
    }

    /**
     * 
     * 팀의 이름과 각 팀의 평균 연령을 구해라
     *
     * @throws Exception
     */
    @Test
    public void 그룹() throws Exception{
        //given
        
        //when
        List<Tuple> result = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }

        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        //then
        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);

        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35);

    }

    /**
     * 조인
     * 1. 팀 A에 소속된 모든 회원
     */
    @Test
    public void 조인() throws Exception{
        //given
        
        //when
        List<Member> result = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        //then
        for (Member member1 : result) {
            System.out.println("member1 = " + member1);
        }
    }

    /**
     * 조인 - on절
     * 1. 조인 대상 필터링
     *  회원과 팀을 조회하면서, 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회
     *  JPQL: select m, t from Member m left join m.team t on t.name = 'teamA'
     *
     *
     * 2. 연관관계 없는 엔티티 외부 조인
     *  회원의 이름이 팀 이름과 같은 대상 외부 조인
     *
     *
     */
    @Test
    public void join_on_filtering() throws Exception{
        //given
        
        //when
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team)
                .on(team.name.eq("teamA"))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }

        //then


    }

    // 페치조인
    // 연관된 엔티티를 한방쿼리로 데이터를 가져옴 -> 성능 최적화

    @PersistenceUnit
    EntityManagerFactory emf;


    @Test
    public void 페치조인_미적용() throws Exception{
        //given
        
        //when
        Member fetchMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();


        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(fetchMember.getTeam());

        //then
        assertThat(loaded).as("페치 조인 미적용").isFalse();
    }
    
    @Test
    public void 페지조인_적용() throws Exception{
        //given
        
        //when
        Member findMember = queryFactory
                .selectFrom(member)
                .join(member.team, team).fetchJoin()
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());

        //then
        assertThat(loaded).isTrue();

    }
    
}

