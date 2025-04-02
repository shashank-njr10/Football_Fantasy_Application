package com.example.fantasy_football_backend.services;

import com.example.fantasy_football_backend.dto.ContestDTO;
import com.example.fantasy_football_backend.dto.UserDTO;
import com.example.fantasy_football_backend.exceptions.BadRequestException;
import com.example.fantasy_football_backend.exceptions.ResourceNotFoundException;
import com.example.fantasy_football_backend.models.Contest;
import com.example.fantasy_football_backend.models.ContestEntry;
import com.example.fantasy_football_backend.models.User;
import com.example.fantasy_football_backend.repositories.ContestEntryRepository;
import com.example.fantasy_football_backend.repositories.ContestRepository;
import com.example.fantasy_football_backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContestService {
    private final ContestRepository contestRepository;
    private final UserRepository userRepository;
    private final ContestEntryRepository contestEntryRepository;

    public List<ContestDTO> getAllContests() {
        return contestRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<ContestDTO> getActiveContests() {
        return contestRepository.findActiveContests(LocalDateTime.now()).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public ContestDTO getContestById(Long id) {
        Contest contest = contestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contest not found with id: " + id));
        return mapToDTO(contest);
    }

    @Transactional
    public ContestDTO createContest(Contest contest, Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id: " + adminId));

        if (!admin.isAdmin()) {
            throw new BadRequestException("User is not an admin");
        }

        contest.setAdmin(admin);
        contest.setActive(true);
        return mapToDTO(contestRepository.save(contest));
    }

    @Transactional
    public ContestDTO updateContest(Long id, Contest contestDetails) {
        Contest contest = contestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contest not found with id: " + id));

        contest.setName(contestDetails.getName());
        contest.setDescription(contestDetails.getDescription());
        contest.setRules(contestDetails.getRules());
        contest.setStartDate(contestDetails.getStartDate());
        contest.setEndDate(contestDetails.getEndDate());
        contest.setMaxTeams(contestDetails.getMaxTeams());
        contest.setActive(contestDetails.isActive());

        return mapToDTO(contestRepository.save(contest));
    }

    @Transactional
    public void deleteContest(Long id) {
        Contest contest = contestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contest not found with id: " + id));

        // Only allow deletion if no entries yet
        if (contest.getEntries().isEmpty()) {
            contestRepository.delete(contest);
        } else {
            throw new BadRequestException("Cannot delete contest with existing entries");
        }
    }

    public List<ContestDTO> getContestsByAdmin(Long adminId) {
        return contestRepository.findByAdminId(adminId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private ContestDTO mapToDTO(Contest contest) {
        ContestDTO dto = new ContestDTO();
        dto.setId(contest.getId());
        dto.setName(contest.getName());
        dto.setDescription(contest.getDescription());
        dto.setRules(contest.getRules());
        dto.setStartDate(contest.getStartDate());
        dto.setEndDate(contest.getEndDate());
        dto.setMaxTeams(contest.getMaxTeams());
        dto.setActive(contest.isActive());

        // Map admin
        UserDTO adminDTO = new UserDTO();
        adminDTO.setId(contest.getAdmin().getId());
        adminDTO.setUsername(contest.getAdmin().getUsername());
        adminDTO.setEmail(contest.getAdmin().getEmail());
        adminDTO.setFullName(contest.getAdmin().getFullName());
        adminDTO.setProfilePicture(contest.getAdmin().getProfilePicture());
        adminDTO.setAdmin(true);

        dto.setAdmin(adminDTO);

        // Get entry count
        dto.setEntryCount(contestEntryRepository.countByContestId(contest.getId()));

        return dto;
    }
}