package com.mytheatre.ticketservice.service;

import com.mytheatre.ticketservice.entity.SeatHold;
import com.mytheatre.ticketservice.entity.SeatLevel;
import com.mytheatre.ticketservice.repository.SeatLevelRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TicketServiceImpl implements TicketService {

    private static Logger logger = LoggerFactory.getLogger(TicketServiceImpl.class);

    final
    SeatLevelRepository seatLevelRepository;

    ConcurrentHashMap<LocalDateTime,SeatHold> seatHoldMap = new ConcurrentHashMap<>();

    @Value("${block.expiry.time:60}")
    private int blockExpiryTimeInSecs;

    private int seatHoldCounter = 0;

    public TicketServiceImpl(SeatLevelRepository seatLevelRepository) {
        this.seatLevelRepository = seatLevelRepository;
    }

    @Override
    public int numSeatsAvailable(Optional<Integer> venueLevel) {
        logger.info("number of seats available for given level called");
        if(venueLevel.isPresent()){
            return getAvailableSeatsForGivenLevel(venueLevel.get());
        }else{
            return getAvailableSeatsForAllLevels();
        }
    }

    @Override
    public SeatHold findAndHoldSeats(int numSeats, Optional<Integer> minLevel, Optional<Integer> maxLevel, String customerEmail) {
        SeatHold seatHold = new SeatHold();
        seatHold.setCustomerEmail(customerEmail);
        seatHold.setSeatHoldId(getSeatHoldCounter());
        if(minLevel.isPresent() && maxLevel.isPresent()) {
            int availableSeats = getAvailableSeatsBetweenGivenLevels(minLevel.get(), maxLevel.get());
            if(availableSeats < numSeats) {
                logger.info("number of seats available {1} is less than required  {2}",availableSeats, numSeats);
                return null;
            }
            seatHold = getSeatHoldForGivenLevels(numSeats, minLevel, maxLevel,seatHold);
            return seatHold;
        }else {
            int availableSeatsAllLevels = getAvailableSeatsForAllLevels();
            if(availableSeatsAllLevels < numSeats) {
                logger.info("number of seats available  {1} is less than required  {2}",availableSeatsAllLevels, numSeats);
                return null;
            }
            seatHold = getSeatHoldFromAllLevels(numSeats,seatHold);
            return seatHold;
        }
    }

    @Override
    public String reserveSeats(int seatHoldId, String customerEmail) {
        LocalDateTime now = LocalDateTime.now();
        for(Map.Entry<LocalDateTime, SeatHold> entry : seatHoldMap.entrySet()) {
            if(entry.getKey().isAfter(now)) {
                if(entry.getValue().getSeatHoldId() == seatHoldId && customerEmail.equalsIgnoreCase(entry.getValue().getCustomerEmail())) {
                    SeatHold seatHold = entry.getValue();
                    List<SeatHold.SeatsInLevel> seatLevels = seatHold.getSeatsInLevels();
                    for(SeatHold.SeatsInLevel seatsInLevel : seatLevels) {
                        int reservedSeats = seatsInLevel.getNumberOfSeats();
                        int levelId = seatsInLevel.getLevelId();
                        Optional<SeatLevel> seatLevel = seatLevelRepository.findById(levelId);
                        if(seatLevel.isPresent()) {
                            int netAvailableSeats =   seatLevel.get().getAvailableSeats() - reservedSeats;
                            seatLevel.get().setAvailableSeats(netAvailableSeats);
                            seatLevelRepository.save(seatLevel.get());
                        }
                    }
                    seatHoldMap.remove(entry.getKey());
                    return "MYTHEATRE-TICKETSERVICE-000"+seatHoldId;
                }
            }
        }
        return null;
    }
    // Inserts the initial data in the database
    @PostConstruct
    public void init() {
        logger.info("Inserting the initial data of seats available at each level");
        seatLevelRepository.save(new SeatLevel(1,"Orchestra", 100.00, 25, 50));
        seatLevelRepository.save(new SeatLevel(2,"Main", 75.00, 20, 100));
        seatLevelRepository.save(new SeatLevel(3,"Balcony 1", 50.00, 15, 100));
        seatLevelRepository.save(new SeatLevel(4 ,"Balcony 2", 40.00, 15, 100));
    }

    // Provides hold seats for given level
    private int getHoldSeatsForGivenLevel(int levelId) {
        int totalHoldSeatsForlevelId = 0;
        LocalDateTime now = LocalDateTime.now();
        for(Map.Entry<LocalDateTime,SeatHold> entry : seatHoldMap.entrySet()) {
            if(entry.getKey().isAfter(now)) {
                List<SeatHold.SeatsInLevel> list = entry.getValue().getSeatsInLevels();
                for(SeatHold.SeatsInLevel seatsInLevel : list) {
                    if(seatsInLevel.getLevelId() == levelId) {
                        totalHoldSeatsForlevelId = totalHoldSeatsForlevelId + seatsInLevel.getNumberOfSeats();
                    }
                }
            }
        }
        return totalHoldSeatsForlevelId;
    }

    // Provides hold seats for all levels
    private int getHoldSeatsForAllLevels( List<SeatLevel> seatLevels) {
        int totalHoldSeats = 0;
        for(SeatLevel seatLevel : seatLevels) {
            totalHoldSeats = totalHoldSeats + getHoldSeatsForGivenLevel(seatLevel.getLevelId());
        }
        return totalHoldSeats;
    }

    // Provides available seats for all levels for holding
    private int getAvailableSeatsForAllLevels() {
        List<SeatLevel> seatLevels = seatLevelRepository.findAll();
        return seatLevels.stream().mapToInt(SeatLevel::getAvailableSeats).sum() - getHoldSeatsForAllLevels(seatLevels);
    }

    // Provides available seats for given level for holding
    private int getAvailableSeatsForGivenLevel(int lId) {
        Optional<SeatLevel> seatLevel = seatLevelRepository.findById(lId);
        if(seatLevel.isPresent()) {
            int availableSeats = seatLevel.get().getAvailableSeats() - getHoldSeatsForGivenLevel(lId);
            return availableSeats;
        }else {
            throw new NoSuchElementException("Invalid level Id ");
        }
    }

    // Provides available seats between given levels for holding
    private int getAvailableSeatsBetweenGivenLevels(Integer minLevel, Integer maxLevel) {
        int totalAvailableSeats = 0;
        for(int levelId = minLevel; levelId <= maxLevel; levelId++) {
            totalAvailableSeats = totalAvailableSeats + getAvailableSeatsForGivenLevel(levelId);
        }
        return totalAvailableSeats;
    }

    // Provides SeatHold with holding seats in given levels
    private SeatHold getSeatHoldForGivenLevels(int numSeats, Optional<Integer> minLevel, Optional<Integer> maxLevel,SeatHold seatHold) {
        for(int levelId = minLevel.get(); levelId <= maxLevel.get(); levelId++) {
            int currentAvailableSeats = getAvailableSeatsForGivenLevel(levelId);
            if(currentAvailableSeats > 0) {
                if(currentAvailableSeats >= numSeats) {
                    SeatHold.SeatsInLevel seatsInLevel = new SeatHold.SeatsInLevel(numSeats,levelId);
                    seatHold.getSeatsInLevels().add(seatsInLevel);
                    seatHoldMap.put(getHoldExpiryDateTime(),seatHold);
                    return seatHold;
                }else {
                    SeatHold.SeatsInLevel seatsInLevel = new SeatHold.SeatsInLevel(currentAvailableSeats,levelId);
                    seatHold.getSeatsInLevels().add(seatsInLevel);
                    numSeats = numSeats - currentAvailableSeats;
                }
            }
        }
        return null;
    }
    // Provides SeatHold with holding seats may be in multiple levels
    private SeatHold getSeatHoldFromAllLevels(int numSeats, SeatHold seatHold) {
        List<SeatLevel> seatLevels = seatLevelRepository.findAll();
        for (SeatLevel seatLevel : seatLevels) {
            int currentAvailableSeats = seatLevel.getAvailableSeats() - getHoldSeatsForGivenLevel(seatLevel.getLevelId());
            if(currentAvailableSeats > 0) {
                if(currentAvailableSeats >= numSeats) {
                    SeatHold.SeatsInLevel seatsInLevel = new SeatHold.SeatsInLevel(numSeats,seatLevel.getLevelId());
                    seatHold.getSeatsInLevels().add(seatsInLevel);
                    seatHoldMap.put(getHoldExpiryDateTime(),seatHold);
                    return seatHold;
                }else {
                    SeatHold.SeatsInLevel seatsInLevel = new SeatHold.SeatsInLevel(currentAvailableSeats,seatLevel.getLevelId());
                    seatHold.getSeatsInLevels().add(seatsInLevel);
                    numSeats = numSeats - currentAvailableSeats;
                }
            }else {
                return null;
            }
        }
        return null;
    }

    //provides hold expiry time
    // @return holdExpiryTime
    private synchronized LocalDateTime getHoldExpiryDateTime() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime holdExpiryTime = now.plusSeconds(blockExpiryTimeInSecs);
        return holdExpiryTime;
    }

    //Provides SeatHold Counter
    //@return seatHoldCounter
    private synchronized int getSeatHoldCounter() {
        return ++seatHoldCounter;
    }
}
