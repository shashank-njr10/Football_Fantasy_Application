package com.example.fantasy_football_backend.repositories;

import com.example.fantasy_football_backend.models.ContestEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContestEntryRepository extends JpaRepository<ContestEntry, Long> {
    List<ContestEntry> findByContestId(Long contestId);

    List<ContestEntry> findByTeamId(Long teamId);

    @Query("SELECT ce FROM ContestEntry ce WHERE ce.contest.id = :contestId ORDER BY ce.score DESC")
    List<ContestEntry> findByContestIdOrderByScoreDesc(Long contestId);

    Optional<ContestEntry> findByContestIdAndTeamId(Long contestId, Long teamId);

    @Query("SELECT COUNT(ce) FROM ContestEntry ce WHERE ce.contest.id = :contestId")
    int countByContestId(Long contestId);
}