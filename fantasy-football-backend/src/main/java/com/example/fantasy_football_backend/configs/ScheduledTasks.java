package com.example.fantasy_football_backend.configs;

import com.example.fantasy_football_backend.models.Match;
import com.example.fantasy_football_backend.repositories.MatchRepository;
import com.example.fantasy_football_backend.services.FantasyTeamService;
import com.example.fantasy_football_backend.services.FootballApiService;
import com.example.fantasy_football_backend.services.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasks {

    private final FootballApiService footballApiService;
    private final MatchRepository matchRepository;
    private final PlayerService playerService;
    private final FantasyTeamService fantasyTeamService;

    // List of competition codes to track (Champions League, Premier League, etc.)
    private static final List<String> COMPETITION_CODES = Arrays.asList("CL", "PL", "PD", "BL1", "SA", "FL1");

    // List of top team codes to track for player data
    private static final List<String> TEAM_CODES = Arrays.asList(
            "65", "66", "73", "78", // Premier League top teams
            "86", "81", "529", "83", // La Liga top teams
            "5", "157", "4", "518", // Bundesliga top teams
            "505", "506", "489", "6195", // Serie A top teams
            "524", "525", "559", // Ligue 1 top teams
            "503", "500", "5", "698" // Champions League top teams
    );

    /**
     * Update matches data every 5 minutes
     */
    @Scheduled(cron = "0 */5 * * * *")
    public void updateMatches() {
        log.info("Starting scheduled match update task");

        // Run each competition update in a separate thread with delays
        for (String competitionCode : COMPETITION_CODES) {
            // Use a new thread for each API call to avoid blocking
            CompletableFuture.runAsync(() -> {
                footballApiService.updateMatches(competitionCode);
            });
        }

        // Also update live matches (with delay)
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(30000); // Wait 30 seconds before updating live matches
                List<Match> liveMatches = matchRepository.findByStatus("LIVE");
                for (Match match : liveMatches) {
                    try {
                        Thread.sleep(6000); // Rate limit
                        footballApiService.updateMatchDetails(match.getId());
                    } catch (Exception e) {
                        log.error("Error updating live match {}", match.getId());
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // Update scheduled matches to live if needed (with delay)
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(60000); // Wait 1 minute before checking for matches to make live
                updateScheduledMatchesToLive();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        log.info("Scheduled match update tasks initiated");
    }

    /**
     * Update team players data once a day (midnight)
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void updatePlayers() {
        log.info("Starting scheduled player update task");

        // Process teams one by one with a delay to respect API rate limits
        CompletableFuture.runAsync(() -> {
            for (String teamCode : TEAM_CODES) {
                try {
                    footballApiService.updateTeamPlayers(teamCode);
                } catch (Exception e) {
                    log.error("Error updating team {}: {}", teamCode, e.getMessage());
                }
            }
            log.info("Completed scheduled player update task");
        });
    }

    /**
     * Update all player points and team scores every 15 minutes
     */
    @Scheduled(cron = "0 */15 * * * *")
    public void updateScores() {
        log.info("Starting scheduled score update task");

        // This doesn't involve API calls, so we can run it directly
        playerService.updateAllPlayerPoints();
        fantasyTeamService.updateAllTeamScores();

        log.info("Completed scheduled score update task");
    }

    /**
     * Update scheduled matches to live if kickoff time has passed
     */
    private void updateScheduledMatchesToLive() {
        log.info("Checking for matches that should be updated to LIVE status");
        matchRepository.findByStatusAndKickoffTimeBefore("SCHEDULED", java.time.LocalDateTime.now())
                .forEach(match -> {
                    match.setStatus("LIVE");
                    matchRepository.save(match);
                    log.info("Updated match {} to LIVE status", match.getId());
                });
    }
}