package com.mytheatre.ticketservice.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SeatHold {

    private int seatHoldId;
    private String customerEmail;
    private List<SeatsInLevel> seatsInLevels;

    public SeatHold() {
        this.seatsInLevels = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class SeatsInLevel {

        int numberOfSeats;
        int levelId;
        public SeatsInLevel(int numberOfSeats, int levelId) {
            this.numberOfSeats = numberOfSeats;
            this.levelId = levelId;
        }
    }
}
