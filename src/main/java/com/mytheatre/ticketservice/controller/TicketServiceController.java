package com.mytheatre.ticketservice.controller;

import com.mytheatre.ticketservice.entity.SeatHold;
import com.mytheatre.ticketservice.service.TicketService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

/**
 * Rest Controller to provide APIs for ticketing service of theatre
 * 1. API to provide total number of seats to hold
 * 2. API to hold seats
 * 3. API to reserve seats on provided seat Hold Id and customer email
 */
@RestController
@RequestMapping("/ticketservice")
public class TicketServiceController {

    final
    TicketService ticketService;

    public TicketServiceController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    /**
     * Rest API to provide available number of seats
     * @param venueLevel level Id
     * @return noOfTickets
     */
    @GetMapping(path= {"/vacant/{venueLevel}", "/vacant", "/vacant/"})
    public ResponseEntity<Integer> getAvailableSeats(@PathVariable(required = false) Optional<Integer> venueLevel) {
        int noOfTickets = ticketService.numSeatsAvailable(venueLevel);
        return new ResponseEntity<>(noOfTickets,HttpStatus.OK);
    }

    /**
     * Rest API to hold seats between given levels
     * @param noOfSeats seats to hold
     * @param minLevel minimum level
     * @param maxLevel maximum level
     * @param customerEmail customer Email
     * @return SeatHold object
     */
    @PostMapping({"/hold", "/hold/"})
    public ResponseEntity<?> hold(@RequestParam int noOfSeats, @RequestParam(required = false) Optional<Integer> minLevel, @RequestParam(required = false) Optional<Integer> maxLevel, @RequestParam String customerEmail) {
        if((!minLevel.isPresent() && maxLevel.isPresent()) || (minLevel.isPresent() && !maxLevel.isPresent())) {
            return new ResponseEntity<>("Provide both minLevel and maxlevel ",HttpStatus.BAD_REQUEST);
        }
        SeatHold seatHold = ticketService.findAndHoldSeats(noOfSeats,minLevel,maxLevel,customerEmail);
        if(seatHold == null)
            return new ResponseEntity<>("Not enough vacant seats available as per the request ",HttpStatus.OK);
        return new ResponseEntity<>(seatHold,HttpStatus.OK);
    }

    /**
     * Rest API to reserve hold seats
     * @param seatHoldId seat hold Id
     * @param customerEmailId customer email Id
     * @return confirmId
     */
    @PostMapping({"/reserve", "/reserve/"})
    public ResponseEntity<String> reserve(@RequestParam Integer seatHoldId, @RequestParam String customerEmailId) {
        String confirmId = ticketService.reserveSeats(seatHoldId, customerEmailId);
        if(confirmId == null) {
            return new ResponseEntity<>("Either provided seatHoldId has expired or not found ", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(confirmId, HttpStatus.OK);
    }
}
