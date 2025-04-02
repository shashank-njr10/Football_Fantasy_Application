package com.example.fantasy_football_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContestDTO {
    private Long id;
    private String name;
    private String description;
    private String rules;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private int maxTeams;
    private boolean isActive;
    private UserDTO admin;
    private int entryCount;
}