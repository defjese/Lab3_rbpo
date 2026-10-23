package com.example.l2_rbpo.service;

import com.example.l2_rbpo.dto.TeamCreateRequest;
import com.example.l2_rbpo.dto.TeamPatchRequest;
import com.example.l2_rbpo.dto.TeamResponse;
import com.example.l2_rbpo.entity.Match;
import com.example.l2_rbpo.entity.Team;
import com.example.l2_rbpo.entity.TeamApplication;
import com.example.l2_rbpo.entity.User;
import com.example.l2_rbpo.repository.MatchRepository;
import com.example.l2_rbpo.repository.TeamApplicationRepository;
import com.example.l2_rbpo.repository.TeamRepository;
import com.example.l2_rbpo.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class TeamService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final TeamApplicationRepository applicationRepository;
    private final MatchRepository matchRepository;

    public TeamService(
            TeamRepository teamRepository,
            UserRepository userRepository,
            TeamApplicationRepository applicationRepository,
            MatchRepository matchRepository
    ) {
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
        this.applicationRepository = applicationRepository;
        this.matchRepository = matchRepository;
    }

    @Transactional(readOnly = true)
    public List<TeamResponse> findAll() {
        return teamRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TeamResponse findById(Long id) {
        return toResponse(getTeam(id));
    }

    public TeamResponse create(TeamCreateRequest request) {
        User captain = getUser(request.captainId());

        Team team = new Team();
        team.setName(request.name());
        team.setCaptain(captain);

        return toResponse(teamRepository.save(team));
    }

    public TeamResponse update(Long id, TeamPatchRequest request) {
        Team team = getTeam(id);

        if (request.name() != null) {
            team.setName(request.name());
        }

        if (request.captainId() != null) {
            User captain = getUser(request.captainId());
            team.setCaptain(captain);
        }

        return toResponse(teamRepository.save(team));
    }

    public void delete(Long id) {
        Team team = getTeam(id);

        boolean hasApplications = applicationRepository.findAll()
                .stream()
                .anyMatch(application ->
                        application.getTeam().getId().equals(id));

        boolean hasMatches = matchRepository
                .findByTeam1IdOrTeam2Id(id, id)
                .size() > 0;

        if (hasApplications || hasMatches) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot delete a team with applications or matches"
            );
        }

        teamRepository.delete(team);
    }

    private Team getTeam(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Team not found"
                ));
    }

    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Captain not found"
                ));
    }

    private TeamResponse toResponse(Team team) {
        return new TeamResponse(
                team.getId(),
                team.getName(),
                team.getCaptain().getId(),
                team.getCaptain().getUsername()
        );
    }
}