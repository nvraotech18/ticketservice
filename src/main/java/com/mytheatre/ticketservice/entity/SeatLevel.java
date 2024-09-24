package com.mytheatre.ticketservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class SeatLevel {

    @Id
    private int levelId;
    private String levelName;
    private double price;
    private int rows;
    private int seatsInRow;
    private int availableSeats;


    public SeatLevel(int levelId, String levelName, double price, int rows, int seatsInRow) {
        this.levelId = levelId;
        this.levelName = levelName;
        this.price = price;
        this.rows = rows;
        this.seatsInRow = seatsInRow;
        this.availableSeats = rows * seatsInRow;
    }

    public SeatLevel() {}
}
