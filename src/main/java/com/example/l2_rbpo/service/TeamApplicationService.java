package com.example.l2_rbpo.service;

import com.example.l2_rbpo.dto.TeamApplicationCreateRequest;
import com.example.l2_rbpo.dto.TeamApplicationPatchRequest;
import com.example.l2_rbpo.dto.TeamApplicationResponse;
import com.example.l2_rbpo.entity.ApplicationStatus;
import com.example.l2_rbpo.entity.Team;
import com.example.l2_rbpo.entity.TeamApplication;
import com.example.l2_rbpo.entity.Tournament;
import com.example.l2_rbpo.entity.TournamentStatus;
import com.example.l2_rbpo.repository.TeamApplicationRepository;
import com.example.l2_rbpo.repository.TeamRepository;
import com.example.l2_rbpo.repository.TournamentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class TeamApplicationService {

    private final TeamApplicationRepository applicationRepository;
    private final TeamRepository teamRepository;
    private final TournamentRepository tournamentRepository;

    public TeamApplicationService(
            TeamApplicationRepository applicationRepository,
            TeamRepository teamRepository,
            TournamentRepository tournamentRepository
    ) {
        this.applicationRepository = applicationRepository;
        this.teamRepository = teamRepository;
        this.tournamentRepository = tournamentRepository;
    }

    @Transactional(readOnly = true)
    public List<TeamApplicationResponse> findAll() {
        return applicationRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TeamApplicationResponse findById(Long id) {
        return toResponse(getApplication(id));
    }

    public TeamApplicationResponse create(
            TeamApplicationCreateRequest request
    ) {
        Team team = teamRepository.findById(request.teamId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Team not found"
                ));

        Tournament tournament =
                tournamentRepository.findById(request.tournamentId())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Tournament not found"
                        ));

        if (tournament.getStatus() != TournamentStatus.PLANNED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Applications can only be submitted to planned tournaments"
            );
        }

        if (applicationRepository
                .findByTeamIdAndTournamentId(
                        team.getId(),
                        tournament.getId()
                ).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This team has already applied to this tournament"
            );
        }

        TeamApplication application = new TeamApplication();
        application.setTeam(team);
        application.setTournament(tournament);
        application.setStatus(ApplicationStatus.PENDING);

        return toResponse(applicationRepository.save(application));
    }

    public TeamApplicationResponse update(
            Long id,
            TeamApplicationPatchRequest request
    ) {
        TeamApplication application = getApplication(id);

        if (request.status() != null) {
            if (request.status() == ApplicationStatus.APPROVED
                    && application.getStatus()
                    != ApplicationStatus.APPROVED) {

                Tournament tournament = application.getTournament();

                long approvedCount =
                        applicationRepository
                                .countByTournamentIdAndStatus(
                                        tournament.getId(),
                                        ApplicationStatus.APPROVED
                                );

                if (approvedCount >= tournament.getTeamLimit()) {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Tournament team limit has been reached"
                    );
                }

                if (tournament.getStatus()
                        != TournamentStatus.PLANNED) {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Cannot approve applications for this tournament"
                    );
                }
            }

            application.setStatus(request.status());
        }

        return toResponse(applicationRepository.save(application));
    }

    // Бизнес-операция 1: одобрить заявку (заявка + турнир)
    public TeamApplicationResponse approve(Long id) {
        TeamApplication application = getApplication(id);

        // блокируем турнир, чтобы два одобрения не превысили лимит
        Tournament tournament = tournamentRepository
                .findByIdForUpdate(application.getTournament().getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Tournament not found"
                ));

        if (tournament.getStatus() != TournamentStatus.PLANNED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Applications can only be approved "
                            + "for planned tournaments"
            );
        }

        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only pending applications can be approved"
            );
        }

        long approvedCount = applicationRepository
                .countByTournamentIdAndStatus(
                        tournament.getId(),
                        ApplicationStatus.APPROVED
                );

        if (approvedCount >= tournament.getTeamLimit()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Tournament team limit has been reached"
            );
        }

        application.setStatus(ApplicationStatus.APPROVED);
        return toResponse(applicationRepository.save(application));
    }

    public void delete(Long id) {
        TeamApplication application = getApplication(id);
        applicationRepository.delete(application);
    }

    private TeamApplication getApplication(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Team application not found"
                ));
    }

    private TeamApplicationResponse toResponse(
            TeamApplication application
    ) {
        return new TeamApplicationResponse(
                application.getId(),
                application.getTeam().getId(),
                application.getTeam().getName(),
                application.getTournament().getId(),
                application.getTournament().getName(),
                application.getStatus()
        );
    }
}