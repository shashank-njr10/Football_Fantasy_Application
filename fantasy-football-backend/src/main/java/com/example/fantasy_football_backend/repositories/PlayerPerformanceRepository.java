package com.example.fantasy_football_backend.repositories;

import com.example.fantasy_football_backend.models.PlayerPerformance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PlayerPerformanceRepository extends JpaRepository<PlayerPerformance, Long> {
    List<PlayerPerformance> findByPlayerId(Long playerId);

    List<PlayerPerformance> findByMatchId(Long matchId);

    @Query("SELECT pp FROM PlayerPerformance pp WHERE pp.match.id = :matchId AND pp.player.id = :playerId")
    PlayerPerformance findByMatchIdAndPlayerId(Long matchId, Long playerId);

    @Query("SELECT SUM(pp.totalPoints) FROM PlayerPerformance pp WHERE pp.player.id = :playerId")
    Integer getTotalPointsByPlayerId(Long playerId);
}