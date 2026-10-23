package com.example.l2_rbpo.service;

import com.example.l2_rbpo.dto.TournamentCreateRequest;
import com.example.l2_rbpo.dto.TournamentPatchRequest;
import com.example.l2_rbpo.dto.TournamentResponse;
import com.example.l2_rbpo.entity.Match;
import com.example.l2_rbpo.entity.MatchStatus;
import com.example.l2_rbpo.entity.TournamentStatus;
import com.example.l2_rbpo.entity.ApplicationStatus;
import com.example.l2_rbpo.dto.StandingResponse;
import com.example.l2_rbpo.entity.TeamApplication;
import com.example.l2_rbpo.entity.Tournament;
import com.example.l2_rbpo.repository.MatchRepository;
import com.example.l2_rbpo.repository.TeamApplicationRepository;
import com.example.l2_rbpo.repository.TournamentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@Transactional
public class TournamentService {

    private final TournamentRepository tournamentRepository;
    private final TeamApplicationRepository applicationRepository;
    private final MatchRepository matchRepository;

    public TournamentService(
            TournamentRepository tournamentRepository,
            TeamApplicationRepository applicationRepository,
            MatchRepository matchRepository
    ) {
        this.tournamentRepository = tournamentRepository;
        this.applicationRepository = applicationRepository;
        this.matchRepository = matchRepository;
    }

    @Transactional(readOnly = true)
    public List<TournamentResponse> findAll() {
        return tournamentRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TournamentResponse findById(Long id) {
        return toResponse(getTournament(id));
    }

    public TournamentResponse create(TournamentCreateRequest request) {
        Tournament tournament = new Tournament();
        tournament.setName(request.name());
        tournament.setTeamLimit(request.teamLimit());
        tournament.setStatus(request.status());

        return toResponse(tournamentRepository.save(tournament));
    }

    public TournamentResponse update(
            Long id,
            TournamentPatchRequest request
    ) {
        Tournament tournament = getTournament(id);

        if (request.name() != null) {
            tournament.setName(request.name());
        }

        if (request.teamLimit() != null) {
            long approvedCount =
                    applicationRepository.countByTournamentIdAndStatus(
                            id,
                            com.example.l2_rbpo.entity.ApplicationStatus.APPROVED
                    );

            if (request.teamLimit() < approvedCount) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Team limit cannot be lower than the number "
                                + "of approved applications"
                );
            }

            tournament.setTeamLimit(request.teamLimit());
        }

        if (request.status() != null) {
            tournament.setStatus(request.status());
        }

        return toResponse(tournamentRepository.save(tournament));
    }

    // Бизнес-операция 2: запустить турнир (турнир + заявки)
    public TournamentResponse start(Long id) {
        Tournament tournament = getTournamentForUpdate(id);

        if (tournament.getStatus() != TournamentStatus.PLANNED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only a planned tournament can be started"
            );
        }

        long approvedCount = applicationRepository
                .countByTournamentIdAndStatus(id, ApplicationStatus.APPROVED);

        if (approvedCount < 2) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "At least 2 approved teams are required "
                            + "to start a tournament"
            );
        }

        // все неодобренные заявки автоматически отклоняются
        List<TeamApplication> pending = applicationRepository
                .findByTournamentIdAndStatus(id, ApplicationStatus.PENDING);
        pending.forEach(application ->
                application.setStatus(ApplicationStatus.REJECTED));
        applicationRepository.saveAll(pending);

        tournament.setStatus(TournamentStatus.IN_PROGRESS);
        return toResponse(tournamentRepository.save(tournament));
    }

    // Бизнес-операция 5: завершить турнир (турнир + матчи + команды)
    public List<StandingResponse> finish(Long id) {
        Tournament tournament = getTournamentForUpdate(id);

        if (tournament.getStatus() != TournamentStatus.IN_PROGRESS) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only a tournament in progress can be finished"
            );
        }

        List<Match> matches = matchRepository.findByTournamentId(id);

        if (matches.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot finish a tournament without matches"
            );
        }

        boolean hasUnfinished = matches.stream()
                .anyMatch(match -> match.getStatus() != MatchStatus.FINISHED);

        if (hasUnfinished) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "All matches must be finished before "
                            + "finishing the tournament"
            );
        }

        tournament.setStatus(TournamentStatus.FINISHED);
        tournamentRepository.save(tournament);

        return buildStandings(id, matches);
    }

    public void delete(Long id) {
        Tournament tournament = getTournament(id);

        boolean hasApplications = applicationRepository.findAll()
                .stream()
                .anyMatch(application ->
                        application.getTournament().getId().equals(id));

        boolean hasMatches = !matchRepository
                .findByTournamentId(id).isEmpty();

        if (hasApplications || hasMatches) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot delete a tournament with applications or matches"
            );
        }

        tournamentRepository.delete(tournament);
    }

    private Tournament getTournamentForUpdate(Long id) {
        return tournamentRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Tournament not found"
                ));
    }

    // Турнирная таблица: победа = 3 очка, ничья = 1, поражение = 0
    private List<StandingResponse> buildStandings(
            Long tournamentId,
            List<Match> matches
    ) {
        // для каждой команды: [сыграно, победы, ничьи, поражения]
        Map<Long, int[]> stats = new LinkedHashMap<>();
        Map<Long, String> names = new HashMap<>();

        for (TeamApplication application : applicationRepository
                .findByTournamentIdAndStatus(
                        tournamentId, ApplicationStatus.APPROVED)) {
            stats.put(application.getTeam().getId(), new int[4]);
            names.put(application.getTeam().getId(),
                    application.getTeam().getName());
        }

        for (Match match : matches) {
            Long id1 = match.getTeam1().getId();
            Long id2 = match.getTeam2().getId();
            names.putIfAbsent(id1, match.getTeam1().getName());
            names.putIfAbsent(id2, match.getTeam2().getName());

            int[] first = stats.computeIfAbsent(id1, key -> new int[4]);
            int[] second = stats.computeIfAbsent(id2, key -> new int[4]);

            first[0]++;
            second[0]++;

            int score1 = match.getTeam1Score();
            int score2 = match.getTeam2Score();

            if (score1 > score2) {
                first[1]++;
                second[3]++;
            } else if (score1 < score2) {
                second[1]++;
                first[3]++;
            } else {
                first[2]++;
                second[2]++;
            }
        }

        return stats.entrySet().stream()
                .map(entry -> {
                    int[] s = entry.getValue();
                    return new StandingResponse(
                            entry.getKey(),
                            names.get(entry.getKey()),
                            s[0], s[1], s[2], s[3],
                            s[1] * 3 + s[2]
                    );
                })
                .sorted(Comparator
                        .comparingInt(StandingResponse::points).reversed()
                        .thenComparing(StandingResponse::teamName))
                .toList();
    }

    private Tournament getTournament(Long id) {
        return tournamentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Tournament not found"
                ));
    }

    private TournamentResponse toResponse(Tournament tournament) {
        return new TournamentResponse(
                tournament.getId(),
                tournament.getName(),
                tournament.getTeamLimit(),
                tournament.getStatus()
        );
    }
}