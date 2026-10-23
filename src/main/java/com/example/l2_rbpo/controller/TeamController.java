package com.example.l2_rbpo.controller;

import com.example.l2_rbpo.dto.TeamCreateRequest;
import com.example.l2_rbpo.dto.TeamPatchRequest;
import com.example.l2_rbpo.dto.TeamResponse;
import com.example.l2_rbpo.service.TeamService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @GetMapping
    public List<TeamResponse> findAll() {
        return teamService.findAll();
    }

    @GetMapping("/{id}")
    public TeamResponse findById(@PathVariable Long id) {
        return teamService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TeamResponse create(
            @Valid @RequestBody TeamCreateRequest request
    ) {
        return teamService.create(request);
    }

    @PatchMapping("/{id}")
    public TeamResponse update(
            @PathVariable Long id,
            @Valid @RequestBody TeamPatchRequest request
    ) {
        return teamService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        teamService.delete(id);
    }
}