package com.example.l2_rbpo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tournaments")
public class Tournament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "team_limit", nullable = false)
    private Integer teamLimit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TournamentStatus status = TournamentStatus.PLANNED;

    public Tournament() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getTeamLimit() {
        return teamLimit;
    }

    public void setTeamLimit(Integer teamLimit) {
        this.teamLimit = teamLimit;
    }

    public TournamentStatus getStatus() {
        return status;
    }

    public void setStatus(TournamentStatus status) {
        this.status = status;
    }
}