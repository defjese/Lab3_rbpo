package com.example.l2_rbpo.repository;

import com.example.l2_rbpo.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Long> {

    List<Team> findByCaptainId(Long captainId);

    boolean existsByName(String name);
}