package com.example.l2_rbpo.repository;

import com.example.l2_rbpo.entity.ApplicationStatus;
import com.example.l2_rbpo.entity.TeamApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamApplicationRepository
        extends JpaRepository<TeamApplication, Long> {

    Optional<TeamApplication> findByTeamIdAndTournamentId(
            Long teamId,
            Long tournamentId
    );

    List<TeamApplication> findByTournamentId(Long tournamentId);

    List<TeamApplication> findByTeamId(Long teamId);

    List<TeamApplication> findByTournamentIdAndStatus(
            Long tournamentId,
            ApplicationStatus status
    );

    long countByTournamentIdAndStatus(
            Long tournamentId,
            ApplicationStatus status
    );
}
