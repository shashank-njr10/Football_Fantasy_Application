//package com.example.fantasy_football_backend.models;
//
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import java.time.LocalDateTime;
//import java.util.HashSet;
//import java.util.Set;
//
//@Entity
//@Table(name = "matches")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//public class Match {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    private String homeTeam;
//
//    private String awayTeam;
//
//    private String competition;
//
//    private LocalDateTime kickoffTime;
//
//    private String status; // SCHEDULED, LIVE, FINISHED
//
//    private int homeScore;
//
//    private int awayScore;
//
//    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL)
//    private Set<PlayerPerformance> performances = new HashSet<>();
//}

package com.example.fantasy_football_backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "matches")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Match {
    @Id
    private Long id;

    private String homeTeam;

    private String awayTeam;

    private String competition;

    private LocalDateTime kickoffTime;

    private String status; // SCHEDULED, LIVE, FINISHED

    private int homeScore;

    private int awayScore;

    @Version
    private Long version;

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL)
    private Set<PlayerPerformance> performances = new HashSet<>();
}