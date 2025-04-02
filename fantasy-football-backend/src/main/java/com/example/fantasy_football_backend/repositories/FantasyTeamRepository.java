package com.example.fantasy_football_backend.repositories;

import com.example.fantasy_football_backend.models.FantasyTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FantasyTeamRepository extends JpaRepository<FantasyTeam, Long> {
    List<FantasyTeam> findByOwnerId(Long userId);

    @Query("SELECT ft FROM FantasyTeam ft JOIN ft.contestEntries ce WHERE ce.contest.id = :contestId ORDER BY ce.score DESC")
    List<FantasyTeam> findTeamsByContestIdOrderByScore(Long contestId);

    @Query("SELECT ft FROM FantasyTeam ft JOIN ft.contestEntries ce WHERE ce.contest.id = :contestId AND ft.owner.id = :userId")
    FantasyTeam findTeamByContestIdAndUserId(Long contestId, Long userId);

    @Query("SELECT COUNT(ft) FROM FantasyTeam ft JOIN ft.players p WHERE p.team = :teamName AND ft.id = :fantasyTeamId")
    int countPlayersByTeam(String teamName, Long fantasyTeamId);
}