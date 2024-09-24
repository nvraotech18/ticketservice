package com.mytheatre.ticketservice.Controller;

import com.mytheatre.ticketservice.controller.TicketServiceController;
import com.mytheatre.ticketservice.service.TicketService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.mockito.Mockito.*;

import java.util.Optional;

@WebMvcTest(TicketServiceController.class)
public class TicketServiceControllerTest {

    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private MockMvc mockMvc;

    @MockBean
    TicketService ticketService;

    @Test
    public void testGetAvailableSeatsForGivenLevel() throws Exception {
        Optional<Integer> opt = Optional.of(1);
        when(ticketService.numSeatsAvailable(opt)).thenReturn(100);
        mockMvc.perform(MockMvcRequestBuilders
                .get("/ticketservice/vacant/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testGetAllAvailableSeats() throws Exception {
        Optional<Integer> opt = Optional.of(1);
        when(ticketService.numSeatsAvailable(opt)).thenReturn(100);
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/ticketservice/vacant/").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testHoldWithBothLevelsProvided() throws Exception {
        mockMvc.perform(post("/ticketservice/hold")
                        .param("noOfSeats", "2")
                        .param("minLevel", "1")
                        .param("maxLevel", "5")
                        .param("customerEmail", "test@example.com")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk()); // Adjust based on expected behavior
    }

    @Test
    public void testHoldWithMissingMinLevel() throws Exception {
        mockMvc.perform(post("/ticketservice/hold")
                        .param("noOfSeats", "2")
                        .param("maxLevel", "5")
                        .param("customerEmail", "test@example.com")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Provide both minLevel and maxlevel "));
    }

    @Test
    public void testHoldWithNoLevelsProvided() throws Exception {
        mockMvc.perform(post("/ticketservice/hold")
                        .param("noOfSeats", "2")
                        .param("minLevel", "1")
                        .param("customerEmail", "test@example.com")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Provide both minLevel and maxlevel "));
    }

    @Test
    public void testReserveWithValidInput() throws Exception {
        mockMvc.perform(post("/ticketservice/reserve")
                        .param("seatHoldId", "1")
                        .param("customerEmailId", "test@example.com")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Either provided seatHoldId has expired or not found "));
    }

    @Test
    public void testReserveWithNullSeatHoldId() throws Exception {
        mockMvc.perform(post("/ticketservice/reserve")
                        .param("customerEmailId", "test@example.com")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testReserveWithNullEmailId() throws Exception {
        mockMvc.perform(post("/ticketservice/reserve")
                        .param("seatHoldId", "1")
                        .param("customerEmailId", "")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Either provided seatHoldId has expired or not found "));
    }

    @Test
    public void testReserveWithExpiredSeatHoldId() throws Exception {
        mockMvc.perform(post("/ticketservice/reserve")
                        .param("seatHoldId", "999")
                        .param("customerEmailId", "test@example.com")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Either provided seatHoldId has expired or not found "));
    }
}
