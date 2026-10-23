package com.example.l2_rbpo.service;

import com.example.l2_rbpo.dto.MatchCreateRequest;
import com.example.l2_rbpo.dto.MatchPatchRequest;
import com.example.l2_rbpo.dto.MatchResponse;
import com.example.l2_rbpo.dto.MatchResultRequest;
import com.example.l2_rbpo.entity.ApplicationStatus;
import com.example.l2_rbpo.entity.Match;
import com.example.l2_rbpo.entity.MatchStatus;
import com.example.l2_rbpo.entity.Team;
import com.example.l2_rbpo.entity.TeamApplication;
import com.example.l2_rbpo.entity.Tournament;
import com.example.l2_rbpo.entity.TournamentStatus;
import com.example.l2_rbpo.repository.MatchRepository;
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
public class MatchService {

    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;
    private final TournamentRepository tournamentRepository;
    private final TeamApplicationRepository applicationRepository;

    public MatchService(
            MatchRepository matchRepository,
            TeamRepository teamRepository,
            TournamentRepository tournamentRepository,
            TeamApplicationRepository applicationRepository
    ) {
        this.matchRepository = matchRepository;
        this.teamRepository = teamRepository;
        this.tournamentRepository = tournamentRepository;
        this.applicationRepository = applicationRepository;
    }

    @Transactional(readOnly = true)
    public List<MatchResponse> findAll() {
        return matchRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MatchResponse findById(Long id) {
        return toResponse(getMatch(id));
    }

    public MatchResponse create(MatchCreateRequest request) {
        Tournament tournament =
                tournamentRepository.findById(request.tournamentId())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Tournament not found"
                        ));

        Team team1 = teamRepository.findById(request.team1Id())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "First team not found"
                ));

        Team team2 = teamRepository.findById(request.team2Id())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Second team not found"
                ));

        if (team1.getId().equals(team2.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A team cannot play against itself"
            );
        }

        requireApprovedApplication(team1, tournament);
        requireApprovedApplication(team2, tournament);

        Match match = new Match();
        match.setTournament(tournament);
        match.setTeam1(team1);
        match.setTeam2(team2);
        match.setTeam1Score(0);
        match.setTeam2Score(0);
        match.setStatus(MatchStatus.SCHEDULED);

        return toResponse(matchRepository.save(match));
    }

    public MatchResponse update(Long id, MatchPatchRequest request) {
        Match match = getMatch(id);

        if (match.getStatus() == MatchStatus.FINISHED) {
            boolean changesScore =
                    request.team1Score() != null
                            || request.team2Score() != null;

            boolean changesStatus =
                    request.status() != null
                            && request.status() != MatchStatus.FINISHED;

            if (changesScore || changesStatus) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "A finished match cannot be changed"
                );
            }
        }

        if (request.team1Score() != null) {
            match.setTeam1Score(request.team1Score());
        }

        if (request.team2Score() != null) {
            match.setTeam2Score(request.team2Score());
        }

        if (request.status() != null) {
            match.setStatus(request.status());
        }

        return toResponse(matchRepository.save(match));
    }

    // Бизнес-операция 3: назначить матч (турнир + 2 команды + заявки)
    public MatchResponse schedule(MatchCreateRequest request) {
        Tournament tournament = tournamentRepository
                .findByIdForUpdate(request.tournamentId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Tournament not found"
                ));

        if (tournament.getStatus() != TournamentStatus.IN_PROGRESS) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Matches can only be scheduled in a tournament "
                            + "that is in progress"
            );
        }

        Team team1 = teamRepository.findById(request.team1Id())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "First team not found"
                ));

        Team team2 = teamRepository.findById(request.team2Id())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Second team not found"
                ));

        if (team1.getId().equals(team2.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A team cannot play against itself"
            );
        }

        requireApprovedApplication(team1, tournament);
        requireApprovedApplication(team2, tournament);

        boolean alreadyPlayed =
                matchRepository.existsByTournamentIdAndTeam1IdAndTeam2Id(
                        tournament.getId(), team1.getId(), team2.getId())
                || matchRepository.existsByTournamentIdAndTeam1IdAndTeam2Id(
                        tournament.getId(), team2.getId(), team1.getId());

        if (alreadyPlayed) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "These teams already have a match in this tournament"
            );
        }

        Match match = new Match();
        match.setTournament(tournament);
        match.setTeam1(team1);
        match.setTeam2(team2);
        match.setStatus(MatchStatus.SCHEDULED);

        return toResponse(matchRepository.save(match));
    }

    // Бизнес-операция 4: завершить матч со счётом (матч + турнир)
    public MatchResponse finish(Long id, MatchResultRequest request) {
        Match match = getMatch(id);

        if (match.getStatus() == MatchStatus.FINISHED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Match is already finished"
            );
        }

        if (match.getTournament().getStatus()
                != TournamentStatus.IN_PROGRESS) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Match result can only be recorded while "
                            + "the tournament is in progress"
            );
        }

        match.setTeam1Score(request.team1Score());
        match.setTeam2Score(request.team2Score());
        match.setStatus(MatchStatus.FINISHED);

        return toResponse(matchRepository.save(match));
    }

    public void delete(Long id) {
        Match match = getMatch(id);
        matchRepository.delete(match);
    }

    private void requireApprovedApplication(
            Team team,
            Tournament tournament
    ) {
        boolean approved = applicationRepository
                .findByTeamIdAndTournamentId(
                        team.getId(),
                        tournament.getId()
                )
                .map(application ->
                        application.getStatus()
                                == ApplicationStatus.APPROVED)
                .orElse(false);

        if (!approved) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Both teams must have approved applications "
                            + "for the tournament"
            );
        }
    }

    private Match getMatch(Long id) {
        return matchRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Match not found"
                ));
    }

    private MatchResponse toResponse(Match match) {
        return new MatchResponse(
                match.getId(),
                match.getTournament().getId(),
                match.getTeam1().getId(),
                match.getTeam1().getName(),
                match.getTeam2().getId(),
                match.getTeam2().getName(),
                match.getTeam1Score(),
                match.getTeam2Score(),
                match.getStatus()
        );
    }
}