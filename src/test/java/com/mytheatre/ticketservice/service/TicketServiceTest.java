package com.mytheatre.ticketservice.service;

import com.mytheatre.ticketservice.entity.SeatHold;
import com.mytheatre.ticketservice.entity.SeatLevel;
import com.mytheatre.ticketservice.repository.SeatLevelRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class TicketServiceTest {

    @Mock
    SeatLevelRepository seatLevelRepository;
    @InjectMocks
    TicketServiceImpl ticketService;

    @Test
    public void testNumSeatsAvailableForGivenLevel() {
        int levelId = 1;
        Optional<Integer> optLevelId = Optional.of(levelId);
        SeatLevel seatLevel = new SeatLevel(levelId, "Orchestra", 100, 10, 100);
        Optional<SeatLevel> optSeatLevel = Optional.of(seatLevel);
        when(seatLevelRepository.findById(levelId)).thenReturn(optSeatLevel);
        int noOfSeats = ticketService.numSeatsAvailable(optLevelId);
        assertTrue((noOfSeats == 1000), "no of seats available should be 1000");
    }

    @Test
    public void testNumSeatsAvailableForAllLevels() {
        Optional<Integer> optLevelId = Optional.empty();
        SeatLevel seatLevel1 = new SeatLevel(1, "Orchestra", 100, 10, 100);
        SeatLevel seatLevel2 = new SeatLevel(2, "Main", 150, 20, 100);
        List<SeatLevel> seatLevels = new ArrayList<>();
        seatLevels.add(seatLevel1);
        seatLevels.add(seatLevel2);
        when(seatLevelRepository.findAll()).thenReturn(seatLevels);
        int noOfSeats = ticketService.numSeatsAvailable(optLevelId);
        assertTrue(noOfSeats == 3000,"no of seats available should be 3000");
    }

    @Test
    public void testFindAndHoldSeatsForAllLevels() {
        int numOfSeats = 100;
        Optional<Integer> minLevel = Optional.empty();
        Optional<Integer> maxLevel = Optional.empty();
        String customerEmail = "abc.ttt";
        SeatLevel seatLevel1 = new SeatLevel(1, "Orchestra", 100, 10, 100);
        SeatLevel seatLevel2 = new SeatLevel(2, "Main", 150, 20, 100);
        List<SeatLevel> seatLevels = new ArrayList<>();
        seatLevels.add(seatLevel1);
        seatLevels.add(seatLevel2);
        when(seatLevelRepository.findAll()).thenReturn(seatLevels);
        SeatHold seatHold = ticketService.findAndHoldSeats(numOfSeats, minLevel, maxLevel, customerEmail);
        assertNotNull(seatHold);
        assertTrue(seatHold.getCustomerEmail().equals("abc.ttt"));
    }

    @Test
    public void testFindAndHoldSeatsForGivenLevels() {
        int numOfSeats = 100;
        Optional<Integer> minLevel = Optional.of(1);
        Optional<Integer> maxLevel = Optional.of(2);
        String customerEmail = "abc.ttt";
        SeatLevel seatLevel1 = new SeatLevel(1, "Orchestra", 100, 10, 100);
        SeatLevel seatLevel2 = new SeatLevel(2, "Main", 150, 20, 100);
        Optional<SeatLevel> optSeatLevelOne = Optional.of(seatLevel1);
        Optional<SeatLevel> optSeatLevelTwo = Optional.of(seatLevel2);
        when(seatLevelRepository.findById(1)).thenReturn(optSeatLevelOne);
        when(seatLevelRepository.findById(2)).thenReturn(optSeatLevelTwo);
        SeatHold seatHold = ticketService.findAndHoldSeats(numOfSeats,minLevel,maxLevel,customerEmail);
        assertNotNull(seatHold);
        assertTrue(seatHold.getCustomerEmail().equals("abc.ttt"));
    }

    @Test
    public void testReserveSeats() {
        int seatHoldId = 1;
        String customerEmail = "abc.ttt";
        SeatLevel seatLevel = new SeatLevel(1, "Orchestra", 100, 10, 100);
        Optional<SeatLevel> optSeatLevel = Optional.of(seatLevel);
        when(seatLevelRepository.findById(1)).thenReturn(optSeatLevel);
        SeatHold seatHold = new SeatHold();
        seatHold.setSeatHoldId(1);
        seatHold.setCustomerEmail("abc.ttt");
        SeatHold.SeatsInLevel seatsInLevel = new SeatHold.SeatsInLevel(100,1);
        List<SeatHold.SeatsInLevel> list = new ArrayList<>();
        list.add(seatsInLevel);
        seatHold.setSeatsInLevels(list);
        ticketService.seatHoldMap.put(LocalDateTime.now().plusMinutes(10),seatHold);
        String confirmId = ticketService.reserveSeats(1,customerEmail);
        assertTrue(confirmId.contains("MYTHEATRE-TICKETSERVICE-000"),"confirmation id returned should contain MYTHEATRE-TICKETSERVICE-000");
    }
}
