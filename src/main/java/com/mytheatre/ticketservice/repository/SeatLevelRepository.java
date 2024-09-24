package com.mytheatre.ticketservice.repository;

import com.mytheatre.ticketservice.entity.SeatLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface SeatLevelRepository extends JpaRepository<SeatLevel, Integer> {

    @Query("select sl from SeatLevel sl order by sl.levelId ASC")
    List<SeatLevel> findAll();
}
