package com.example.l2_rbpo.controller;

import com.example.l2_rbpo.dto.TournamentCreateRequest;
import com.example.l2_rbpo.dto.TournamentPatchRequest;
import com.example.l2_rbpo.dto.TournamentResponse;
import com.example.l2_rbpo.dto.StandingResponse;
import com.example.l2_rbpo.service.TournamentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tournaments")
public class TournamentController {

    private final TournamentService tournamentService;

    public TournamentController(TournamentService tournamentService) {
        this.tournamentService = tournamentService;
    }

    @GetMapping
    public List<TournamentResponse> findAll() {
        return tournamentService.findAll();
    }

    @GetMapping("/{id}")
    public TournamentResponse findById(@PathVariable Long id) {
        return tournamentService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TournamentResponse create(
            @Valid @RequestBody TournamentCreateRequest request
    ) {
        return tournamentService.create(request);
    }

    @PatchMapping("/{id}")
    public TournamentResponse update(
            @PathVariable Long id,
            @Valid @RequestBody TournamentPatchRequest request
    ) {
        return tournamentService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        tournamentService.delete(id);
    }

    @PostMapping("/{id}/start")
    public TournamentResponse start(@PathVariable Long id) {
        return tournamentService.start(id);
    }

    @PostMapping("/{id}/finish")
    public List<StandingResponse> finish(@PathVariable Long id) {
        return tournamentService.finish(id);
    }
}
