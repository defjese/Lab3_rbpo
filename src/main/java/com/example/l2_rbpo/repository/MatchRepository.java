package com.example.l2_rbpo.repository;

import com.example.l2_rbpo.entity.Match;
import com.example.l2_rbpo.entity.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {

    List<Match> findByTournamentId(Long tournamentId);

    List<Match> findByTeam1IdOrTeam2Id(Long team1Id, Long team2Id);

    List<Match> findByStatus(MatchStatus status);

    boolean existsByTournamentIdAndTeam1IdAndTeam2Id(
            Long tournamentId,
            Long team1Id,
            Long team2Id
    );
}
