package com.example.fantasy_football_backend;

import com.example.fantasy_football_backend.dto.*;
import com.example.fantasy_football_backend.models.*;
import com.example.fantasy_football_backend.repositories.*;
import com.example.fantasy_football_backend.services.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class FantasyFootballIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private ContestService contestService;

    @Autowired
    private FantasyTeamService fantasyTeamService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContestRepository contestRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Mock
    private FootballApiService footballApiService;

    private User admin;
    private User regularUser;
    private List<Player> testPlayers;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        // Create test users
        admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@example.com");
        admin.setFullName("Admin User");
        admin.setAdmin(true);
        admin = userRepository.save(admin);

        regularUser = new User();
        regularUser.setUsername("user");
        regularUser.setEmail("user@example.com");
        regularUser.setFullName("Regular User");
        regularUser.setAdmin(false);
        regularUser = userRepository.save(regularUser);

        // Create test players with different teams and positions
        testPlayers = new ArrayList<>();

        String[] teams = {"Manchester City", "Liverpool", "Arsenal", "Barcelona"};
        String[] positions = {"GK", "DEF", "MID", "FWD"};

        for (int i = 0; i < 20; i++) {
            Player player = new Player();
            player.setName("Player " + (i + 1));
            player.setTeam(teams[i % teams.length]);
            player.setPosition(positions[i % positions.length]);
            player.setCost(5 + (i % 10)); // Costs between 5-14
            player.setTotalPoints(10 + i);
            testPlayers.add(playerRepository.save(player));
        }
    }

    @Test
    public void testUserManagement() {
        // Test finding users
        Optional<User> foundAdmin = userRepository.findByEmail("admin@example.com");
        assertTrue(foundAdmin.isPresent());
        assertEquals("Admin User", foundAdmin.get().getFullName());

        // Test user roles (using service)
        UserDTO adminDTO = userService.findUserById(admin.getId());
        assertTrue(adminDTO.isAdmin());

        UserDTO userDTO = userService.findUserById(regularUser.getId());
        assertFalse(userDTO.isAdmin());
    }

    @Test
    public void testContestManagement() {
        // Create contest
        Contest contest = new Contest();
        contest.setName("Test Contest");
        contest.setDescription("Test Description");
        contest.setRules("Test Rules");
        contest.setStartDate(LocalDateTime.now());
        contest.setEndDate(LocalDateTime.now().plusDays(30));
        contest.setMaxTeams(100);
        contest.setActive(true);
        contest.setAdmin(admin);

        Contest savedContest = contestRepository.save(contest);

        // Test contest creation
        assertNotNull(savedContest.getId());
        assertEquals("Test Contest", savedContest.getName());
        assertEquals(admin.getId(), savedContest.getAdmin().getId());

        // Test finding contests
        Optional<Contest> foundContest = contestRepository.findById(savedContest.getId());
        assertTrue(foundContest.isPresent());
        assertEquals("Test Description", foundContest.get().getDescription());
    }

    @Test
    public void testFantasyTeamAndPlayers() {
        // Create a fantasy team
        FantasyTeam team = new FantasyTeam();
        team.setName("Test Team");
        team.setOwner(regularUser);
        team = fantasyTeamRepository.save(team);

        // Add players to team
        int totalCost = 0;
        int playersAdded = 0;

        // Add 11 players within budget
        for (int i = 0; i < Math.min(11, testPlayers.size()); i++) {
            Player player = testPlayers.get(i);
            if (totalCost + player.getCost() <= 100) {
                team.getPlayers().add(player);
                totalCost += player.getCost();
                playersAdded++;
            }
        }

        team = fantasyTeamRepository.save(team);

        // Team should have players
        assertFalse(team.getPlayers().isEmpty());
        assertTrue(team.getPlayers().size() > 0);

        // Should respect budget constraint
        assertTrue(team.calculateTeamCost() <= 100);

        // Test team validation
        if (team.getPlayers().size() == 11) {
            // Create a contest
            Contest contest = new Contest();
            contest.setName("Test Contest");
            contest.setDescription("Test Description");
            contest.setStartDate(LocalDateTime.now());
            contest.setEndDate(LocalDateTime.now().plusDays(30));
            contest.setActive(true);
            contest.setAdmin(admin);
            Contest savedContest = contestRepository.save(contest);

            // Create a contest entry
            ContestEntry entry = new ContestEntry();
            entry.setContest(savedContest);
            entry.setTeam(team);
            entry.setJoinedAt(LocalDateTime.now());
            entry.setScore(0);
            entry.setRank(1);
            contestEntryRepository.save(entry);

            // Check if team is in contest
            boolean isInContest = fantasyTeamService.isTeamInContest(team.getId(), savedContest.getId());
            assertTrue(isInContest);
        }
    }

    @Test
    public void testPlayerScoring() {
        // Create a player
        Player player = testPlayers.get(0);
        player.setTotalPoints(0);
        playerRepository.save(player);

        // Create a match
        Match match = new Match();
        match.setId(9999L); // Manually set ID for testing only
        match.setHomeTeam("Team A");
        match.setAwayTeam("Team B");
        match.setStatus("FINISHED");
        match.setCompetition("Test");
        match.setKickoffTime(LocalDateTime.now().minusHours(2));
        Match savedMatch = matchRepository.save(match);

        // Create performance record
        PlayerPerformance performance = new PlayerPerformance();
        performance.setPlayer(player);
        performance.setMatch(savedMatch);
        performance.setGoals(2);
        performance.setAssists(1);
        performance.setMinutesPlayed(90);
        performance.setCleanSheet(true);
        performance.setTotalPoints(10); // Set some points

        // Save the performance
        PlayerPerformance savedPerformance = playerPerformanceRepository.save(performance);

        // Verify performance was saved
        assertNotNull(savedPerformance.getId());
        assertEquals(2, savedPerformance.getGoals());

        // Update player's total points
        playerService.updatePlayerTotalPoints(player);

        // Verify player points were updated
        Player updatedPlayer = playerRepository.findById(player.getId()).orElseThrow();
        assertEquals(10, updatedPlayer.getTotalPoints());
    }

    @Autowired
    private ContestEntryRepository contestEntryRepository;

    @Autowired
    private FantasyTeamRepository fantasyTeamRepository;

    @Autowired
    private PlayerPerformanceRepository playerPerformanceRepository;
}