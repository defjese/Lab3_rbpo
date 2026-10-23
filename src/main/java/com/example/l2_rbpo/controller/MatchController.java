package com.example.l2_rbpo.controller;

import com.example.l2_rbpo.dto.MatchCreateRequest;
import com.example.l2_rbpo.dto.MatchPatchRequest;
import com.example.l2_rbpo.dto.MatchResponse;
import com.example.l2_rbpo.dto.MatchResultRequest;
import com.example.l2_rbpo.service.MatchService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @GetMapping
    public List<MatchResponse> findAll() {
        return matchService.findAll();
    }

    @GetMapping("/{id}")
    public MatchResponse findById(@PathVariable Long id) {
        return matchService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MatchResponse create(
            @Valid @RequestBody MatchCreateRequest request
    ) {
        return matchService.create(request);
    }

    @PatchMapping("/{id}")
    public MatchResponse update(
            @PathVariable Long id,
            @Valid @RequestBody MatchPatchRequest request
    ) {
        return matchService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        matchService.delete(id);
    }

    @PostMapping("/schedule")
    @ResponseStatus(HttpStatus.CREATED)
    public MatchResponse schedule(
            @Valid @RequestBody MatchCreateRequest request
    ) {
        return matchService.schedule(request);
    }

    @PostMapping("/{id}/finish")
    public MatchResponse finish(
            @PathVariable Long id,
            @Valid @RequestBody MatchResultRequest request
    ) {
        return matchService.finish(id, request);
    }
}
