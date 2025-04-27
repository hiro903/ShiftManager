package io.shiftmanager.you.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.shiftmanager.you.config.TestConfig;
import io.shiftmanager.you.config.TestSecurityConfig;
import io.shiftmanager.you.model.Shift;
import io.shiftmanager.you.model.User;
import io.shiftmanager.you.model.Timezone;
import io.shiftmanager.you.service.ShiftService;
import io.shiftmanager.you.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "test@example.com")
class CalendarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShiftService shiftService;

    @MockBean
    private UserService userService;

    private User testUser;
    private List<Shift> testShifts;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setEmail("test@example.com");
        testUser.setUsername("テストユーザー");

        testShifts = new ArrayList<>();
        Shift shift = new Shift();
        shift.setShiftId(1L);
        shift.setUserId(1L);
        shift.setShiftDate(LocalDate.now());
        shift.setTimezone("MORNING");
        testShifts.add(shift);

        when(userService.findByEmail("test@example.com")).thenReturn(testUser);
    }

    @Test
    void showCalendar_ShouldDisplayCalendarWithShifts() throws Exception {
        when(shiftService.getShiftsByMonth(anyLong(), anyInt(), anyInt())).thenReturn(testShifts);

        mockMvc.perform(MockMvcRequestBuilders.get("/calendar"))
                .andExpect(status().isOk())
                .andExpect(view().name("calendar"))
                .andExpect(model().attributeExists("calendarWeeks"))
                .andExpect(model().attributeExists("shifts"))
                .andExpect(model().attributeExists("currentUser"))
                .andExpect(model().attributeExists("currentMonth"));

        verify(userService, times(1)).findByEmail("test@example.com");
        verify(shiftService, times(1)).getShiftsByMonth(eq(1L), anyInt(), anyInt());
    }

    @Test
    void requestShift_SuccessfulRequest_ShouldRedirectWithSuccessMessage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/calendar/shift/request")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("date", "2024-07-15")
                .param("timezone", "MORNING"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/calendar"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(userService, times(1)).findByEmail("test@example.com");
        verify(shiftService, times(1)).requestShift(
                eq(1L),
                eq(LocalDate.of(2024, 7, 15)),
                eq("MORNING")
        );
    }

    @Test
    void requestShift_FailedRequest_ShouldRedirectWithErrorMessage() throws Exception {
        doThrow(new RuntimeException("シフト申請に失敗しました"))
                .when(shiftService).requestShift(anyLong(), any(LocalDate.class), anyString());

        mockMvc.perform(MockMvcRequestBuilders.post("/calendar/shift/request")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("date", "2024-07-15")
                .param("timezone", "MORNING"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/calendar"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(userService, times(1)).findByEmail("test@example.com");
    }

    @Test
    void cancelShift_SuccessfulCancel_ShouldRedirectWithSuccessMessage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/calendar/shift/cancel")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("shiftId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/calendar"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(shiftService, times(1)).cancelShift(1L);
    }

    @Test
    void cancelShift_FailedCancel_ShouldRedirectWithErrorMessage() throws Exception {
        doThrow(new RuntimeException("シフトのキャンセルに失敗しました"))
                .when(shiftService).cancelShift(anyLong());

        mockMvc.perform(MockMvcRequestBuilders.post("/calendar/shift/cancel")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("shiftId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/calendar"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    void bulkDayRequest_SuccessfulRequest_ShouldReturnSuccessResponse() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/calendar/shift/bulk-day")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("year", "2024")
                .param("month", "7")
                .param("weekdays", "1", "3", "5")
                .param("timezone", "MORNING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").exists());

        verify(userService, times(1)).findByEmail("test@example.com");
        verify(shiftService, times(1)).bulkRequestShiftByDayOfWeek(
                eq(1L),
                eq(2024),
                eq(7),
                anySet(),
                eq("MORNING")
        );
    }

    @Test
    void resetShifts_SuccessfulReset_ShouldReturnSuccessResponse() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/calendar/shift/reset")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("year", "2024")
                .param("month", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").exists());

        verify(userService, times(1)).findByEmail("test@example.com");
        verify(shiftService, times(1)).resetShiftsForMonth(eq(1L), eq(2024), eq(7));
    }

    @Test
    void submitShifts_SuccessfulSubmit_ShouldReturnSuccessResponse() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/calendar/shift/submit")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("year", "2024")
                .param("month", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").exists());

        verify(userService, times(1)).findByEmail("test@example.com");
        verify(shiftService, times(1)).submitShifts(eq(1L), eq(2024), eq(7));
    }
} 