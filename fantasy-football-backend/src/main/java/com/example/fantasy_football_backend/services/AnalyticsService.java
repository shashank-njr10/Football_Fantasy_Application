package com.example.fantasy_football_backend.services;

import com.example.fantasy_football_backend.dto.ContestEntryDTO;
import com.example.fantasy_football_backend.models.Contest;
import com.example.fantasy_football_backend.models.FantasyTeam;
import com.example.fantasy_football_backend.models.User;
import com.example.fantasy_football_backend.repositories.ContestEntryRepository;
import com.example.fantasy_football_backend.repositories.ContestRepository;
import com.example.fantasy_football_backend.repositories.FantasyTeamRepository;
import com.example.fantasy_football_backend.repositories.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {
    private final UserRepository userRepository;
    private final FantasyTeamRepository fantasyTeamRepository;
    private final ContestRepository contestRepository;
    private final ContestEntryRepository contestEntryRepository;

    @Data
    public static class UserPerformanceStats {
        private Long userId;
        private String username;
        private int totalContestsJoined;
        private int totalTeams;
        private double averageRank;
        private int bestRank;
        private int bestScore;
        private String bestContestName;
        private Map<String, Integer> performanceByContest = new HashMap<>();
    }

    @Data
    public static class ContestStats {
        private Long contestId;
        private String contestName;
        private int totalTeams;
        private double averageScore;
        private int highestScore;
        private String topTeamName;
        private String topTeamOwner;
        private List<Map<String, Object>> topTeams = new ArrayList<>();
    }

    public UserPerformanceStats getUserPerformanceStats(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserPerformanceStats stats = new UserPerformanceStats();
        stats.setUserId(userId);
        stats.setUsername(user.getUsername());

        // Get all teams for this user
        List<FantasyTeam> userTeams = fantasyTeamRepository.findByOwnerId(userId);
        stats.setTotalTeams(userTeams.size());

        // Get all contest entries for all user teams
        List<com.example.fantasy_football_backend.models.ContestEntry> allEntries = new ArrayList<>();
        for (FantasyTeam team : userTeams) {
            allEntries.addAll(team.getContestEntries());
        }

        stats.setTotalContestsJoined(allEntries.size());

        if (!allEntries.isEmpty()) {
            // Calculate average rank
            double avgRank = allEntries.stream()
                    .mapToInt(com.example.fantasy_football_backend.models.ContestEntry::getRank)
                    .average()
                    .orElse(0);
            stats.setAverageRank(avgRank);

            // Find best rank and score
            Optional<com.example.fantasy_football_backend.models.ContestEntry> bestRankEntry = allEntries.stream()
                    .min(Comparator.comparingInt(com.example.fantasy_football_backend.models.ContestEntry::getRank));

            Optional<com.example.fantasy_football_backend.models.ContestEntry> bestScoreEntry = allEntries.stream()
                    .max(Comparator.comparingInt(com.example.fantasy_football_backend.models.ContestEntry::getScore));

            if (bestRankEntry.isPresent()) {
                stats.setBestRank(bestRankEntry.get().getRank());
                stats.setBestContestName(bestRankEntry.get().getContest().getName());
            }

            if (bestScoreEntry.isPresent()) {
                stats.setBestScore(bestScoreEntry.get().getScore());
            }

            // Get performance by contest
            Map<String, Integer> performanceByContest = new HashMap<>();
            for (com.example.fantasy_football_backend.models.ContestEntry entry : allEntries) {
                performanceByContest.put(entry.getContest().getName(), entry.getScore());
            }
            stats.setPerformanceByContest(performanceByContest);
        }

        return stats;
    }

    public ContestStats getContestStats(Long contestId) {
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new RuntimeException("Contest not found"));

        List<com.example.fantasy_football_backend.models.ContestEntry> entries = contestEntryRepository.findByContestIdOrderByScoreDesc(contestId);

        ContestStats stats = new ContestStats();
        stats.setContestId(contestId);
        stats.setContestName(contest.getName());
        stats.setTotalTeams(entries.size());

        if (!entries.isEmpty()) {
            // Calculate average score
            double avgScore = entries.stream()
                    .mapToInt(com.example.fantasy_football_backend.models.ContestEntry::getScore)
                    .average()
                    .orElse(0);
            stats.setAverageScore(avgScore);

            // Get highest score and team info
            com.example.fantasy_football_backend.models.ContestEntry topEntry = entries.get(0);
            stats.setHighestScore(topEntry.getScore());
            stats.setTopTeamName(topEntry.getTeam().getName());
            stats.setTopTeamOwner(topEntry.getTeam().getOwner().getUsername());

            // Get top 10 teams
            List<Map<String, Object>> topTeams = entries.stream()
                    .limit(10)
                    .map(entry -> {
                        Map<String, Object> teamInfo = new HashMap<>();
                        teamInfo.put("rank", entry.getRank());
                        teamInfo.put("teamName", entry.getTeam().getName());
                        teamInfo.put("owner", entry.getTeam().getOwner().getUsername());
                        teamInfo.put("score", entry.getScore());
                        return teamInfo;
                    })
                    .collect(Collectors.toList());

            stats.setTopTeams(topTeams);
        }

        return stats;
    }

    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();

        long totalUsers = userRepository.count();
        long totalTeams = fantasyTeamRepository.count();
        long totalContests = contestRepository.count();

        stats.put("totalUsers", totalUsers);
        stats.put("totalTeams", totalTeams);
        stats.put("totalContests", totalContests);
        stats.put("averageTeamsPerUser", totalUsers > 0 ? (double) totalTeams / totalUsers : 0);

        // Most popular contests
        List<Contest> activeContests = contestRepository.findByIsActiveTrue();
        List<Map<String, Object>> popularContests = activeContests.stream()
                .sorted((c1, c2) -> Integer.compare(c2.getEntries().size(), c1.getEntries().size()))
                .limit(5)
                .map(contest -> {
                    Map<String, Object> contestInfo = new HashMap<>();
                    contestInfo.put("id", contest.getId());
                    contestInfo.put("name", contest.getName());
                    contestInfo.put("participants", contest.getEntries().size());
                    return contestInfo;
                })
                .collect(Collectors.toList());

        stats.put("popularContests", popularContests);

        return stats;
    }
}