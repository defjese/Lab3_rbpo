package com.example.l2_rbpo.controller;

import com.example.l2_rbpo.dto.TeamApplicationCreateRequest;
import com.example.l2_rbpo.dto.TeamApplicationPatchRequest;
import com.example.l2_rbpo.dto.TeamApplicationResponse;
import com.example.l2_rbpo.service.TeamApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/team-applications")
public class TeamApplicationController {

    private final TeamApplicationService teamApplicationService;

    public TeamApplicationController(
            TeamApplicationService teamApplicationService
    ) {
        this.teamApplicationService = teamApplicationService;
    }

    @GetMapping
    public List<TeamApplicationResponse> findAll() {
        return teamApplicationService.findAll();
    }

    @GetMapping("/{id}")
    public TeamApplicationResponse findById(@PathVariable Long id) {
        return teamApplicationService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TeamApplicationResponse create(
            @Valid @RequestBody TeamApplicationCreateRequest request
    ) {
        return teamApplicationService.create(request);
    }

    @PatchMapping("/{id}")
    public TeamApplicationResponse update(
            @PathVariable Long id,
            @Valid @RequestBody TeamApplicationPatchRequest request
    ) {
        return teamApplicationService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        teamApplicationService.delete(id);
    }

    @PostMapping("/{id}/approve")
    public TeamApplicationResponse approve(@PathVariable Long id) {
        return teamApplicationService.approve(id);
    }
}
