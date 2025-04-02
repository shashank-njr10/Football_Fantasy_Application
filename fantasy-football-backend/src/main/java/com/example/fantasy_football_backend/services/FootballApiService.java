//package com.example.fantasy_football_backend.services;
//
//import com.example.fantasy_football_backend.models.Match;
//import com.example.fantasy_football_backend.models.Player;
//import com.example.fantasy_football_backend.models.PlayerPerformance;
//import com.example.fantasy_football_backend.repositories.MatchRepository;
//import com.example.fantasy_football_backend.repositories.PlayerPerformanceRepository;
//import com.example.fantasy_football_backend.repositories.PlayerRepository;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.*;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class FootballApiService {
//
//    private final RestTemplate footballApiRestTemplate;
//    private final PlayerRepository playerRepository;
//    private final MatchRepository matchRepository;
//    private final PlayerPerformanceRepository playerPerformanceRepository;
//    private final PlayerService playerService;
//
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    @Value("${app.api.football-data.key}")
//    private String apiKey;
//
//    private static final String BASE_URL = "https://api.football-data.org/v4";
//
//    /**
//     * Fetch and update competition matches
//     */
//    public void updateMatches(String competitionCode) {
//        try {
//            String url = BASE_URL + "/competitions/" + competitionCode + "/matches";
//            JsonNode response = footballApiRestTemplate.getForObject(url, JsonNode.class);
//
//            if (response != null && response.has("matches")) {
//                JsonNode matches = response.get("matches");
//
//                for (JsonNode matchNode : matches) {
//                    String status = matchNode.get("status").asText();
//
//                    // Skip matches that are not scheduled or in progress
//                    if (!status.equals("SCHEDULED") && !status.equals("LIVE") &&
//                            !status.equals("IN_PLAY") && !status.equals("PAUSED") &&
//                            !status.equals("FINISHED")) {
//                        continue;
//                    }
//
//                    Long matchId = matchNode.get("id").asLong();
//                    // Check if match already exists
//                    Optional<Match> existingMatch = matchRepository.findById(matchId);
//
//                    if (existingMatch.isPresent()) {
//                        updateExistingMatch(existingMatch.get(), matchNode);
//                    } else {
//                        createNewMatch(matchNode, competitionCode);
//                    }
//                }
//            }
//        } catch (Exception e) {
//            log.error("Error updating matches for competition {}: {}", competitionCode, e.getMessage());
//        }
//    }
//
//    /**
//     * Update players from a team
//     */
//    public void updateTeamPlayers(String teamCode) {
//        try {
//            String url = BASE_URL + "/teams/" + teamCode;
//            JsonNode response = footballApiRestTemplate.getForObject(url, JsonNode.class);
//
//            if (response != null && response.has("squad")) {
//                JsonNode squad = response.get("squad");
//                String teamName = response.get("name").asText();
//
//                for (JsonNode playerNode : squad) {
//                    Long playerId = playerNode.get("id").asLong();
//
//                    // Check if player already exists
//                    Optional<Player> existingPlayer = playerRepository.findById(playerId);
//
//                    if (existingPlayer.isPresent()) {
//                        updateExistingPlayer(existingPlayer.get(), playerNode, teamName);
//                    } else {
//                        createNewPlayer(playerNode, teamName);
//                    }
//                }
//            }
//        } catch (Exception e) {
//            log.error("Error updating players for team {}: {}", teamCode, e.getMessage());
//        }
//    }
//
//    /**
//     * Update match details including player performances
//     */
//    public void updateMatchDetails(Long matchId) {
//        try {
//            String url = BASE_URL + "/matches/" + matchId;
//            JsonNode response = footballApiRestTemplate.getForObject(url, JsonNode.class);
//
//            if (response != null) {
//                Optional<Match> matchOptional = matchRepository.findById(matchId);
//
//                if (!matchOptional.isPresent()) {
//                    log.warn("Match with ID {} not found in database", matchId);
//                    return;
//                }
//
//                Match match = matchOptional.get();
//                String status = response.get("status").asText();
//
//                // Update match status and score
//                match.setStatus(convertMatchStatus(status));
//
//                if (response.has("score") && response.get("score").has("fullTime")) {
//                    JsonNode fullTime = response.get("score").get("fullTime");
//                    if (!fullTime.get("home").isNull() && !fullTime.get("away").isNull()) {
//                        match.setHomeScore(fullTime.get("home").asInt());
//                        match.setAwayScore(fullTime.get("away").asInt());
//                    }
//                }
//
//                matchRepository.save(match);
//
//                // Process player performances if match is finished
//                if (status.equals("FINISHED")) {
//                    updatePlayerPerformances(response, match);
//                }
//            }
//        } catch (Exception e) {
//            log.error("Error updating match details for match {}: {}", matchId, e.getMessage());
//        }
//    }
//
//    private void updatePlayerPerformances(JsonNode matchData, Match match) {
//        try {
//            if (matchData.has("homeTeam") && matchData.has("awayTeam")) {
//                String homeTeam = matchData.get("homeTeam").get("name").asText();
//                String awayTeam = matchData.get("awayTeam").get("name").asText();
//
//                // Process both teams
//                if (matchData.has("homeTeam") && matchData.get("homeTeam").has("lineup")) {
//                    processTeamLineup(matchData.get("homeTeam").get("lineup"), match, homeTeam, true);
//                }
//
//                if (matchData.has("awayTeam") && matchData.get("awayTeam").has("lineup")) {
//                    processTeamLineup(matchData.get("awayTeam").get("lineup"), match, awayTeam, false);
//                }
//
//                // Update player total points after processing performances
//                playerService.updateAllPlayerPoints();
//            }
//        } catch (Exception e) {
//            log.error("Error updating player performances for match {}: {}", match.getId(), e.getMessage());
//        }
//    }
//
//    private void processTeamLineup(JsonNode lineup, Match match, String teamName, boolean isHomeTeam) {
//        for (JsonNode playerNode : lineup) {
//            Long playerId = playerNode.get("id").asLong();
//
//            // Find player in database
//            Optional<Player> playerOptional = playerRepository.findById(playerId);
//
//            if (!playerOptional.isPresent()) {
//                log.warn("Player with ID {} not found in database", playerId);
//                continue;
//            }
//
//            Player player = playerOptional.get();
//
//            // Create or update performance
//            PlayerPerformance performance = playerPerformanceRepository.findByMatchIdAndPlayerId(match.getId(), player.getId());
//
//            if (performance == null) {
//                performance = new PlayerPerformance();
//                performance.setPlayer(player);
//                performance.setMatch(match);
//            }
//
//            // Set basic stats
//            performance.setMinutesPlayed(extractMinutesPlayed(playerNode));
//
//            // Check for goals
//            if (playerNode.has("goals")) {
//                performance.setGoals(playerNode.get("goals").asInt(0));
//            }
//
//            // Check for assists
//            if (playerNode.has("assists")) {
//                performance.setAssists(playerNode.get("assists").asInt(0));
//            }
//
//            // Clean sheet for defenders and goalkeepers
//            boolean isDefensive = player.getPosition().equals("Goalkeeper") || player.getPosition().equals("Defender");
//            boolean cleanSheet = isDefensive &&
//                    ((isHomeTeam && match.getAwayScore() == 0) ||
//                            (!isHomeTeam && match.getHomeScore() == 0));
//            performance.setCleanSheet(cleanSheet);
//
//            // Check for cards
//            if (playerNode.has("cards")) {
//                JsonNode cards = playerNode.get("cards");
//                performance.setYellowCard(cards.has("yellow") && cards.get("yellow").asInt(0) > 0);
//                performance.setRedCard(cards.has("red") && cards.get("red").asInt(0) > 0);
//            }
//
//            // Special stats for goalkeepers
//            if (player.getPosition().equals("Goalkeeper") && playerNode.has("saves")) {
//                performance.setSaves(playerNode.get("saves").asInt(0));
//            }
//
//            // Calculate total points
//            int points = calculatePoints(performance, player.getPosition());
//            performance.setTotalPoints(points);
//
//            playerPerformanceRepository.save(performance);
//        }
//    }
//
//    private int extractMinutesPlayed(JsonNode playerNode) {
//        // Try to get exact minutes played
//        if (playerNode.has("minutesPlayed")) {
//            return playerNode.get("minutesPlayed").asInt(0);
//        }
//
//        // Fallback: If player started the match, assume full game (90 minutes)
//        if (playerNode.has("starting") && playerNode.get("starting").asBoolean()) {
//            return 90;
//        }
//
//        return 0;
//    }
//
//    private int calculatePoints(PlayerPerformance performance, String position) {
//        int points = 0;
//
//        // Points for minutes played
//        if (performance.getMinutesPlayed() >= 60) {
//            points += 2;
//        } else if (performance.getMinutesPlayed() > 0) {
//            points += 1;
//        }
//
//        // Points for goals based on position
//        if (position.equals("Goalkeeper") || position.equals("Defender")) {
//            points += performance.getGoals() * 6;
//        } else if (position.equals("Midfielder")) {
//            points += performance.getGoals() * 5;
//        } else { // Forward
//            points += performance.getGoals() * 4;
//        }
//
//        // Points for assists
//        points += performance.getAssists() * 3;
//
//        // Points for clean sheet
//        if (performance.isCleanSheet() && (position.equals("Goalkeeper") || position.equals("Defender"))) {
//            points += 4;
//        } else if (performance.isCleanSheet() && position.equals("Midfielder")) {
//            points += 1;
//        }
//
//        // Points for goalkeeper saves
//        if (position.equals("Goalkeeper")) {
//            points += (performance.getSaves() / 3);
//        }
//
//        // Deductions for cards
//        if (performance.isYellowCard()) {
//            points -= 1;
//        }
//        if (performance.isRedCard()) {
//            points -= 3;
//        }
//
//        return points;
//    }
//
//    private void updateExistingMatch(Match match, JsonNode matchNode) {
//        try {
//            String status = matchNode.get("status").asText();
//            match.setStatus(convertMatchStatus(status));
//
//            // Update scores if available
//            if (matchNode.has("score") && matchNode.get("score").has("fullTime")) {
//                JsonNode fullTime = matchNode.get("score").get("fullTime");
//                if (!fullTime.get("home").isNull() && !fullTime.get("away").isNull()) {
//                    match.setHomeScore(fullTime.get("home").asInt());
//                    match.setAwayScore(fullTime.get("away").asInt());
//                }
//            }
//
//            matchRepository.save(match);
//
//            // If match is finished, get detailed data for player performances
//            if (status.equals("FINISHED")) {
//                updateMatchDetails(match.getId());
//            }
//        } catch (Exception e) {
//            log.error("Error updating existing match {}: {}", match.getId(), e.getMessage());
//        }
//    }
//
//    private void createNewMatch(JsonNode matchNode, String competitionCode) {
//        try {
//            Match match = new Match();
//
//            match.setId(matchNode.get("id").asLong());
//            match.setHomeTeam(matchNode.get("homeTeam").get("name").asText());
//            match.setAwayTeam(matchNode.get("awayTeam").get("name").asText());
//            match.setCompetition(competitionCode);
//
//            // Parse and set kickoff time
//            String utcDate = matchNode.get("utcDate").asText();
//            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
//            match.setKickoffTime(LocalDateTime.parse(utcDate, formatter));
//
//            // Set status
//            match.setStatus(convertMatchStatus(matchNode.get("status").asText()));
//
//            // Set scores if available
//            if (matchNode.has("score") && matchNode.get("score").has("fullTime")) {
//                JsonNode fullTime = matchNode.get("score").get("fullTime");
//                if (!fullTime.get("home").isNull() && !fullTime.get("away").isNull()) {
//                    match.setHomeScore(fullTime.get("home").asInt());
//                    match.setAwayScore(fullTime.get("away").asInt());
//                } else {
//                    match.setHomeScore(0);
//                    match.setAwayScore(0);
//                }
//            } else {
//                match.setHomeScore(0);
//                match.setAwayScore(0);
//            }
//
//            matchRepository.save(match);
//        } catch (Exception e) {
//            log.error("Error creating new match from API data: {}", e.getMessage());
//        }
//    }
//
//    private void updateExistingPlayer(Player player, JsonNode playerNode, String teamName) {
//        try {
//            player.setName(playerNode.get("name").asText());
//            player.setPosition(convertPosition(playerNode.get("position").asText()));
//            player.setTeam(teamName);
//
//            // Assign points cost based on position (you can refine this logic)
//            player.setCost(getPlayerCost(player.getPosition()));
//
//            playerRepository.save(player);
//        } catch (Exception e) {
//            log.error("Error updating existing player {}: {}", player.getId(), e.getMessage());
//        }
//    }
//
//    private void createNewPlayer(JsonNode playerNode, String teamName) {
//        try {
//            Player player = new Player();
//
//            player.setId(playerNode.get("id").asLong());
//            player.setName(playerNode.get("name").asText());
//            player.setPosition(convertPosition(playerNode.get("position").asText()));
//            player.setTeam(teamName);
//            player.setTotalPoints(0);
//
//            // Assign points cost based on position
//            player.setCost(getPlayerCost(player.getPosition()));
//
//            playerRepository.save(player);
//        } catch (Exception e) {
//            log.error("Error creating new player from API data: {}", e.getMessage());
//        }
//    }
//
//    /**
//     * Convert API position to our application position
//     */
//    private String convertPosition(String apiPosition) {
//        switch (apiPosition) {
//            case "Goalkeeper":
//                return "GK";
//            case "Defence":
//                return "DEF";
//            case "Midfield":
//                return "MID";
//            case "Offence":
//                return "FWD";
//            default:
//                return apiPosition;
//        }
//    }
//
//    /**
//     * Convert API match status to our application status
//     */
//    private String convertMatchStatus(String apiStatus) {
//        switch (apiStatus) {
//            case "SCHEDULED":
//                return "SCHEDULED";
//            case "LIVE":
//            case "IN_PLAY":
//            case "PAUSED":
//                return "LIVE";
//            case "FINISHED":
//                return "FINISHED";
//            default:
//                return apiStatus;
//        }
//    }
//
//    /**
//     * Assign player cost based on position
//     */
//    private int getPlayerCost(String position) {
//        // Random cost within range based on position
//        Random random = new Random();
//        switch (position) {
//            case "GK":
//                return random.nextInt(6) + 4; // 4-9 points
//            case "DEF":
//                return random.nextInt(7) + 4; // 4-10 points
//            case "MID":
//                return random.nextInt(10) + 5; // 5-14 points
//            case "FWD":
//                return random.nextInt(12) + 6; // 6-17 points
//            default:
//                return random.nextInt(10) + 5; // 5-14 points
//        }
//    }
//}

package com.example.fantasy_football_backend.services;

import com.example.fantasy_football_backend.models.Match;
import com.example.fantasy_football_backend.models.Player;
import com.example.fantasy_football_backend.models.PlayerPerformance;
import com.example.fantasy_football_backend.repositories.MatchRepository;
import com.example.fantasy_football_backend.repositories.PlayerPerformanceRepository;
import com.example.fantasy_football_backend.repositories.PlayerRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class FootballApiService {

    private final RestTemplate footballApiRestTemplate;
    private final PlayerRepository playerRepository;
    private final MatchRepository matchRepository;
    private final PlayerPerformanceRepository playerPerformanceRepository;
    private final PlayerService playerService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.api.football-data.key}")
    private String apiKey;

    private static final String BASE_URL = "https://api.football-data.org/v4";

    /**
     * Fetch and update competition matches
     */
    public void updateMatches(String competitionCode) {
        try {
            String url = BASE_URL + "/competitions/" + competitionCode + "/matches";
            JsonNode response = footballApiRestTemplate.getForObject(url, JsonNode.class);

            if (response != null && response.has("matches")) {
                JsonNode matches = response.get("matches");

                for (JsonNode matchNode : matches) {
                    String status = matchNode.get("status").asText();

                    // Skip matches that are not scheduled or in progress
                    if (!status.equals("SCHEDULED") && !status.equals("LIVE") &&
                            !status.equals("IN_PLAY") && !status.equals("PAUSED") &&
                            !status.equals("FINISHED")) {
                        continue;
                    }

                    Long matchId = matchNode.get("id").asLong();
                    // Check if match already exists
                    Optional<Match> existingMatch = matchRepository.findById(matchId);

                    if (existingMatch.isPresent()) {
                        updateExistingMatch(existingMatch.get(), matchNode);
                    } else {
                        createNewMatch(matchNode, competitionCode);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error updating matches for competition {}: {}", competitionCode, e.getMessage());
        }
    }

    /**
     * Update players from a team
     */
    public void updateTeamPlayers(String teamCode) {
        try {
            String url = BASE_URL + "/teams/" + teamCode;
            JsonNode response = footballApiRestTemplate.getForObject(url, JsonNode.class);

            if (response != null && response.has("squad")) {
                JsonNode squad = response.get("squad");
                String teamName = response.get("name").asText();

                for (JsonNode playerNode : squad) {
                    Long playerId = playerNode.get("id").asLong();

                    // Check if player already exists
                    Optional<Player> existingPlayer = playerRepository.findById(playerId);

                    if (existingPlayer.isPresent()) {
                        updateExistingPlayer(existingPlayer.get(), playerNode, teamName);
                    } else {
                        createNewPlayer(playerNode, teamName);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error updating players for team {}: {}", teamCode, e.getMessage());
        }
    }

    /**
     * Update match details including player performances
     */
    public void updateMatchDetails(Long matchId) {
        try {
            String url = BASE_URL + "/matches/" + matchId;
            JsonNode response = footballApiRestTemplate.getForObject(url, JsonNode.class);

            if (response != null) {
                Optional<Match> matchOptional = matchRepository.findById(matchId);

                if (!matchOptional.isPresent()) {
                    log.warn("Match with ID {} not found in database", matchId);
                    return;
                }

                Match match = matchOptional.get();
                String status = response.get("status").asText();

                // Update match status and score
                match.setStatus(convertMatchStatus(status));

                if (response.has("score") && response.get("score").has("fullTime")) {
                    JsonNode fullTime = response.get("score").get("fullTime");
                    if (!fullTime.get("home").isNull() && !fullTime.get("away").isNull()) {
                        match.setHomeScore(fullTime.get("home").asInt());
                        match.setAwayScore(fullTime.get("away").asInt());
                    }
                }

                matchRepository.save(match);

                // Process player performances if match is finished
                if (status.equals("FINISHED")) {
                    updatePlayerPerformances(response, match);
                }
            }
        } catch (Exception e) {
            log.error("Error updating match details for match {}: {}", matchId, e.getMessage());
        }
    }

    private void updatePlayerPerformances(JsonNode matchData, Match match) {
        try {
            if (matchData.has("homeTeam") && matchData.has("awayTeam")) {
                String homeTeam = matchData.get("homeTeam").get("name").asText();
                String awayTeam = matchData.get("awayTeam").get("name").asText();

                // Process both teams
                if (matchData.has("homeTeam") && matchData.get("homeTeam").has("lineup")) {
                    processTeamLineup(matchData.get("homeTeam").get("lineup"), match, homeTeam, true);
                }

                if (matchData.has("awayTeam") && matchData.get("awayTeam").has("lineup")) {
                    processTeamLineup(matchData.get("awayTeam").get("lineup"), match, awayTeam, false);
                }

                // Update player total points after processing performances
                playerService.updateAllPlayerPoints();
            }
        } catch (Exception e) {
            log.error("Error updating player performances for match {}: {}", match.getId(), e.getMessage());
        }
    }

    private void processTeamLineup(JsonNode lineup, Match match, String teamName, boolean isHomeTeam) {
        for (JsonNode playerNode : lineup) {
            Long playerId = playerNode.get("id").asLong();

            // Find player in database
            Optional<Player> playerOptional = playerRepository.findById(playerId);

            if (!playerOptional.isPresent()) {
                log.warn("Player with ID {} not found in database", playerId);
                continue;
            }

            Player player = playerOptional.get();

            // Create or update performance
            PlayerPerformance performance = playerPerformanceRepository.findByMatchIdAndPlayerId(match.getId(), player.getId());

            if (performance == null) {
                performance = new PlayerPerformance();
                performance.setPlayer(player);
                performance.setMatch(match);
            }

            // Set basic stats
            performance.setMinutesPlayed(extractMinutesPlayed(playerNode));

            // Check for goals
            if (playerNode.has("goals")) {
                performance.setGoals(playerNode.get("goals").asInt(0));
            }

            // Check for assists
            if (playerNode.has("assists")) {
                performance.setAssists(playerNode.get("assists").asInt(0));
            }

            // Clean sheet for defenders and goalkeepers
            boolean isDefensive = player.getPosition().equals("Goalkeeper") || player.getPosition().equals("Defender");
            boolean cleanSheet = isDefensive &&
                    ((isHomeTeam && match.getAwayScore() == 0) ||
                            (!isHomeTeam && match.getHomeScore() == 0));
            performance.setCleanSheet(cleanSheet);

            // Check for cards
            if (playerNode.has("cards")) {
                JsonNode cards = playerNode.get("cards");
                performance.setYellowCard(cards.has("yellow") && cards.get("yellow").asInt(0) > 0);
                performance.setRedCard(cards.has("red") && cards.get("red").asInt(0) > 0);
            }

            // Special stats for goalkeepers
            if (player.getPosition().equals("Goalkeeper") && playerNode.has("saves")) {
                performance.setSaves(playerNode.get("saves").asInt(0));
            }

            // Calculate total points
            int points = calculatePoints(performance, player.getPosition());
            performance.setTotalPoints(points);

            playerPerformanceRepository.save(performance);
        }
    }

    private int extractMinutesPlayed(JsonNode playerNode) {
        // Try to get exact minutes played
        if (playerNode.has("minutesPlayed")) {
            return playerNode.get("minutesPlayed").asInt(0);
        }

        // Fallback: If player started the match, assume full game (90 minutes)
        if (playerNode.has("starting") && playerNode.get("starting").asBoolean()) {
            return 90;
        }

        return 0;
    }

    private int calculatePoints(PlayerPerformance performance, String position) {
        int points = 0;

        // Points for minutes played
        if (performance.getMinutesPlayed() >= 60) {
            points += 2;
        } else if (performance.getMinutesPlayed() > 0) {
            points += 1;
        }

        // Points for goals based on position
        if (position.equals("Goalkeeper") || position.equals("Defender")) {
            points += performance.getGoals() * 6;
        } else if (position.equals("Midfielder")) {
            points += performance.getGoals() * 5;
        } else { // Forward
            points += performance.getGoals() * 4;
        }

        // Points for assists
        points += performance.getAssists() * 3;

        // Points for clean sheet
        if (performance.isCleanSheet() && (position.equals("Goalkeeper") || position.equals("Defender"))) {
            points += 4;
        } else if (performance.isCleanSheet() && position.equals("Midfielder")) {
            points += 1;
        }

        // Points for goalkeeper saves
        if (position.equals("Goalkeeper")) {
            points += (performance.getSaves() / 3);
        }

        // Deductions for cards
        if (performance.isYellowCard()) {
            points -= 1;
        }
        if (performance.isRedCard()) {
            points -= 3;
        }

        return points;
    }

    @Transactional
    private void updateExistingMatch(Match match, JsonNode matchNode) {
        try {
            String status = matchNode.get("status").asText();
            match.setStatus(convertMatchStatus(status));

            // Update scores if available
            if (matchNode.has("score") && matchNode.get("score").has("fullTime")) {
                JsonNode fullTime = matchNode.get("score").get("fullTime");
                if (!fullTime.get("home").isNull() && !fullTime.get("away").isNull()) {
                    match.setHomeScore(fullTime.get("home").asInt());
                    match.setAwayScore(fullTime.get("away").asInt());
                }
            }

            // Try to save with retry logic for optimistic locking exceptions
            boolean saved = false;
            int retryCount = 0;
            int maxRetries = 3;

            while (!saved && retryCount < maxRetries) {
                try {
                    matchRepository.save(match);
                    saved = true;
                    log.info("Updated match {}: {} vs {}", match.getId(), match.getHomeTeam(), match.getAwayTeam());
                } catch (OptimisticLockingFailureException e) {
                    retryCount++;
                    if (retryCount < maxRetries) {
                        log.warn("Optimistic locking failure for match {}, retry attempt {}", match.getId(), retryCount);
                        // Refresh entity from database
                        match = matchRepository.findById(match.getId()).orElseThrow();
                        // Reapply changes
                        match.setStatus(convertMatchStatus(status));
                        if (matchNode.has("score") && matchNode.get("score").has("fullTime")) {
                            JsonNode fullTime = matchNode.get("score").get("fullTime");
                            if (!fullTime.get("home").isNull() && !fullTime.get("away").isNull()) {
                                match.setHomeScore(fullTime.get("home").asInt());
                                match.setAwayScore(fullTime.get("away").asInt());
                            }
                        }
                    } else {
                        log.error("Failed to update match {} after {} retries", match.getId(), maxRetries);
                        throw e;
                    }
                }
            }

            // If match is finished, get detailed data for player performances
            if (saved && status.equals("FINISHED")) {
                updateMatchDetails(match.getId());
            }
        } catch (Exception e) {
            log.error("Error updating existing match {}: {}", match.getId(), e.getMessage());
        }
    }

    @Transactional
    private void createNewMatch(JsonNode matchNode, String competitionCode) {
        try {
            Long matchId = matchNode.get("id").asLong();

            // Check if match already exists to avoid concurrent creation
            if (matchRepository.existsById(matchId)) {
                // Match already exists, let's update it instead
                updateExistingMatch(matchRepository.findById(matchId).get(), matchNode);
                return;
            }

            Match match = new Match();

            match.setId(matchId);
            match.setHomeTeam(matchNode.get("homeTeam").get("name").asText());
            match.setAwayTeam(matchNode.get("awayTeam").get("name").asText());
            match.setCompetition(competitionCode);

            // Parse and set kickoff time
            String utcDate = matchNode.get("utcDate").asText();
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
            match.setKickoffTime(LocalDateTime.parse(utcDate, formatter));

            // Set status
            match.setStatus(convertMatchStatus(matchNode.get("status").asText()));

            // Set scores if available
            if (matchNode.has("score") && matchNode.get("score").has("fullTime")) {
                JsonNode fullTime = matchNode.get("score").get("fullTime");
                if (!fullTime.get("home").isNull() && !fullTime.get("away").isNull()) {
                    match.setHomeScore(fullTime.get("home").asInt());
                    match.setAwayScore(fullTime.get("away").asInt());
                } else {
                    match.setHomeScore(0);
                    match.setAwayScore(0);
                }
            } else {
                match.setHomeScore(0);
                match.setAwayScore(0);
            }

            matchRepository.save(match);
            log.info("Created new match: {} vs {}", match.getHomeTeam(), match.getAwayTeam());
        } catch (Exception e) {
            log.error("Error creating new match from API data: {}", e.getMessage());
        }
    }

    private void updateExistingPlayer(Player player, JsonNode playerNode, String teamName) {
        try {
            player.setName(playerNode.get("name").asText());
            player.setPosition(convertPosition(playerNode.get("position").asText()));
            player.setTeam(teamName);

            // Assign points cost based on position (you can refine this logic)
            player.setCost(getPlayerCost(player.getPosition()));

            playerRepository.save(player);
        } catch (Exception e) {
            log.error("Error updating existing player {}: {}", player.getId(), e.getMessage());
        }
    }

    private void createNewPlayer(JsonNode playerNode, String teamName) {
        try {
            Player player = new Player();

            player.setId(playerNode.get("id").asLong());
            player.setName(playerNode.get("name").asText());
            player.setPosition(convertPosition(playerNode.get("position").asText()));
            player.setTeam(teamName);
            player.setTotalPoints(0);

            // Assign points cost based on position
            player.setCost(getPlayerCost(player.getPosition()));

            playerRepository.save(player);
        } catch (Exception e) {
            log.error("Error creating new player from API data: {}", e.getMessage());
        }
    }

    /**
     * Convert API position to our application position
     */
    private String convertPosition(String apiPosition) {
        switch (apiPosition) {
            case "Goalkeeper":
                return "GK";
            case "Defence":
                return "DEF";
            case "Midfield":
                return "MID";
            case "Offence":
                return "FWD";
            default:
                return apiPosition;
        }
    }

    /**
     * Convert API match status to our application status
     */
    private String convertMatchStatus(String apiStatus) {
        switch (apiStatus) {
            case "SCHEDULED":
                return "SCHEDULED";
            case "LIVE":
            case "IN_PLAY":
            case "PAUSED":
                return "LIVE";
            case "FINISHED":
                return "FINISHED";
            default:
                return apiStatus;
        }
    }

    /**
     * Assign player cost based on position
     */
    private int getPlayerCost(String position) {
        // Random cost within range based on position
        Random random = new Random();
        switch (position) {
            case "GK":
                return random.nextInt(6) + 4; // 4-9 points
            case "DEF":
                return random.nextInt(7) + 4; // 4-10 points
            case "MID":
                return random.nextInt(10) + 5; // 5-14 points
            case "FWD":
                return random.nextInt(12) + 6; // 6-17 points
            default:
                return random.nextInt(10) + 5; // 5-14 points
        }
    }
}