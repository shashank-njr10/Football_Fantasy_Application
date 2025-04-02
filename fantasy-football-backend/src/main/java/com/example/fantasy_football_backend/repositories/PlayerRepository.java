package com.example.fantasy_football_backend.repositories;

import com.example.fantasy_football_backend.models.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    List<Player> findByTeam(String team);

    List<Player> findByPosition(String position);

    @Query("SELECT p FROM Player p ORDER BY p.totalPoints DESC")
    List<Player> findTopPerformers();

    @Query("SELECT p FROM Player p WHERE p.cost <= :maxCost ORDER BY p.totalPoints DESC")
    List<Player> findTopPerformersByMaxCost(int maxCost);

    @Query("SELECT p FROM Player p JOIN p.performances pp WHERE pp.match.id = :matchId")
    List<Player> findByMatchId(Long matchId);
}