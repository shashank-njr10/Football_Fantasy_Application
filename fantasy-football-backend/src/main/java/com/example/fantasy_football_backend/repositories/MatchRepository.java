//package com.example.fantasy_football_backend.repositories;
//
//import com.example.fantasy_football_backend.models.Match;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.stereotype.Repository;
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Repository
//public interface MatchRepository extends JpaRepository<Match, Long> {
//    List<Match> findByStatus(String status);
//
//    List<Match> findByKickoffTimeBetween(LocalDateTime start, LocalDateTime end);
//
//    @Query("SELECT m FROM Match m WHERE m.homeTeam = :team OR m.awayTeam = :team")
//    List<Match> findByTeam(String team);
//
//    List<Match> findByCompetition(String competition);
//
//    List<Match> findByStatusAndKickoffTimeBefore(String status, LocalDateTime now);
//}

package com.example.fantasy_football_backend.repositories;

import com.example.fantasy_football_backend.models.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByStatus(String status);

    List<Match> findByKickoffTimeBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT m FROM Match m WHERE m.homeTeam = :team OR m.awayTeam = :team")
    List<Match> findByTeam(String team);

    List<Match> findByCompetition(String competition);

    List<Match> findByStatusAndKickoffTimeBefore(String status, LocalDateTime now);

    boolean existsById(Long id);
}