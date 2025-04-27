package io.shiftmanager.you.controller;

import io.shiftmanager.you.exception.ShiftNotFoundException;
import io.shiftmanager.you.exception.ShiftValidationException;
import io.shiftmanager.you.model.ShiftRequest;
import io.shiftmanager.you.model.User;
import io.shiftmanager.you.service.ShiftRequestService;
import io.shiftmanager.you.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(ShiftRequestController.class)
class ShiftRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShiftRequestService shiftRequestService;

    @MockBean
    private UserService userService;

    private User testUser;
    private List<ShiftRequest> testRequests;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setUsername("testUser");
        testUser.setEmail("test@example.com");
        
        ShiftRequest request = new ShiftRequest();
        request.setRequestId(1L);
        request.setUserId(1L);
        
        testRequests = Arrays.asList(request);
        
        // Setup security context
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "test@example.com",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
        
        when(userService.findByEmail("test@example.com")).thenReturn(testUser);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void showCalendar_ShouldDisplayCalendarWithRequests() throws Exception {
        when(shiftRequestService.getRequestsByMonth(anyLong(), anyInt(), anyInt())).thenReturn(testRequests);

        mockMvc.perform(MockMvcRequestBuilders.get("/shifts/calendar"))
                .andExpect(status().isOk())
                .andExpect(view().name("calendar"))
                .andExpect(model().attributeExists("requests"))
                .andExpect(model().attributeExists("currentUser"));

        verify(userService, times(1)).findByEmail("test@example.com");
        verify(shiftRequestService, times(1)).getRequestsByMonth(eq(1L), anyInt(), anyInt());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void requestShift_SuccessfulRequest_ShouldRedirectWithSuccessMessage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/shifts/request")
                .with(csrf())
                .param("date", "2024-07-15")
                .param("timezone", "MORNING"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/shifts/calendar"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(userService, times(1)).findByEmail("test@example.com");
        verify(shiftRequestService, times(1)).requestShift(
                eq(1L),
                eq(LocalDate.of(2024, 7, 15)),
                eq("MORNING")
        );
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void requestShift_FailedRequest_ShouldRedirectWithErrorMessage() throws Exception {
        doThrow(new ShiftValidationException("過去の日付にはシフトを申請できません"))
                .when(shiftRequestService).requestShift(anyLong(), any(LocalDate.class), anyString());

        mockMvc.perform(MockMvcRequestBuilders.post("/shifts/request")
                .with(csrf())
                .param("date", "2024-07-15")
                .param("timezone", "MORNING"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/shifts/calendar"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(userService, times(1)).findByEmail("test@example.com");
        verify(shiftRequestService, times(1)).requestShift(
                eq(1L),
                eq(LocalDate.of(2024, 7, 15)),
                eq("MORNING")
        );
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void cancelRequest_SuccessfulCancel_ShouldRedirectWithSuccessMessage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/shifts/cancel")
                .with(csrf())
                .param("requestId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/shifts/calendar"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(shiftRequestService, times(1)).cancelRequest(1L);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void cancelRequest_NotFoundRequest_ShouldRedirectWithErrorMessage() throws Exception {
        doThrow(new ShiftNotFoundException("シフト希望が見つかりません"))
                .when(shiftRequestService).cancelRequest(anyLong());

        mockMvc.perform(MockMvcRequestBuilders.post("/shifts/cancel")
                .with(csrf())
                .param("requestId", "999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/shifts/calendar"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(shiftRequestService, times(1)).cancelRequest(999L);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void submitRequests_SuccessfulSubmit_ShouldRedirectWithSuccessMessage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/shifts/submit")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/shifts/calendar"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(userService, times(1)).findByEmail("test@example.com");
        verify(shiftRequestService, times(1)).submitRequests(eq(1L), anyInt(), anyInt());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void submitRequests_NoRequests_ShouldRedirectWithErrorMessage() throws Exception {
        doThrow(new ShiftValidationException("提出するシフト希望がありません"))
                .when(shiftRequestService).submitRequests(anyLong(), anyInt(), anyInt());

        mockMvc.perform(MockMvcRequestBuilders.post("/shifts/submit")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/shifts/calendar"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(userService, times(1)).findByEmail("test@example.com");
        verify(shiftRequestService, times(1)).submitRequests(eq(1L), anyInt(), anyInt());
    }
} 