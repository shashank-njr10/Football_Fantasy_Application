package com.example.fantasy_football_backend.repositories;

import com.example.fantasy_football_backend.models.Contest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ContestRepository extends JpaRepository<Contest, Long> {
    List<Contest> findByIsActiveTrue();

    List<Contest> findByEndDateAfter(LocalDateTime date);

    @Query("SELECT c FROM Contest c WHERE c.startDate <= :now AND c.endDate >= :now")
    List<Contest> findActiveContests(LocalDateTime now);

    List<Contest> findByAdminId(Long adminId);
}