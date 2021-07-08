package com.s1.querydsl.domain.member;


import com.s1.querydsl.domain.team.Team;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;

@ToString(of = {"id", "username", "age"})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
@Entity
public class Member {


    @Id
    @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    private String username;
    private int age;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    public Member(String username) {
        this(username, 0);
    }

    public Member(String username, int age) {

        this(username, age, null);
    }


    public Member(String username, int age, Team team) {

        this.username = username;
        this.age = age;
        if (team != null) {
            changeTeam(team);
        }
    }

    // 양방향 연관관계
    private void changeTeam(Team team) {
        this.team = team;
        team.getMembers().add(this);
        
    }
}
