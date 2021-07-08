package com.s1.querydsl.domain.team;


import com.s1.querydsl.domain.member.Member;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@ToString(of = {"id", "name"})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
@Entity
public class Team {

    @Id
    @GeneratedValue
    @Column(name = "team_id")
    private Long id;

    private String name;

    // 읽기만 가능
    @OneToMany(mappedBy = "team")
    List<Member> members = new ArrayList<>();


    public Team(String name) {
        this.name = name;
    }
}

