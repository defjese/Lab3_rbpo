package com.example.l2_rbpo.repository;

import com.example.l2_rbpo.entity.Tournament;
import com.example.l2_rbpo.entity.TournamentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TournamentRepository extends JpaRepository<Tournament, Long> {

    List<Tournament> findByStatus(TournamentStatus status);

    boolean existsByName(String name);

    // Блокирует строку турнира до конца транзакции:
    // два одновременных запроса не смогут обойти лимит команд
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Tournament t where t.id = :id")
    Optional<Tournament> findByIdForUpdate(@Param("id") Long id);
}
