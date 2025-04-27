package io.shiftmanager.you.service;

import io.shiftmanager.you.exception.ShiftNotFoundException;
import io.shiftmanager.you.exception.ShiftValidationException;
import io.shiftmanager.you.mapper.ShiftRequestMapper;
import io.shiftmanager.you.model.ShiftRequest;
import io.shiftmanager.you.model.Status;
import io.shiftmanager.you.model.Timezone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShiftRequestServiceTest {

    @Mock
    private ShiftRequestMapper shiftRequestMapper;

    @InjectMocks
    private ShiftRequestService shiftRequestService;

    private ShiftRequest testRequest;
    private final Long userId = 1L;
    private final Long requestId = 1L;
    private final LocalDate currentDate = LocalDate.now();
    private final LocalDate futureDate = LocalDate.now().plusDays(7);

    @BeforeEach
    void setUp() {
        testRequest = new ShiftRequest();
        testRequest.setRequestId(requestId);
        testRequest.setUserId(userId);
        testRequest.setRequestDate(futureDate);
        testRequest.setTimezone(Timezone.MORNING);
        testRequest.setStatus(Status.REQUESTED);
        testRequest.setSubmitted(false);
    }

    @Test
    void getRequestsByMonth_ShouldReturnRequests() {
        List<ShiftRequest> expected = Arrays.asList(testRequest);
        when(shiftRequestMapper.getRequestsByMonth(userId, 2024, 7)).thenReturn(expected);

        List<ShiftRequest> result = shiftRequestService.getRequestsByMonth(userId, 2024, 7);

        assertEquals(expected, result);
        verify(shiftRequestMapper).getRequestsByMonth(userId, 2024, 7);
    }

    @Test
    void requestShift_WithFutureDate_ShouldCreateRequest() {
        doNothing().when(shiftRequestMapper).insert(any(ShiftRequest.class));

        shiftRequestService.requestShift(userId, futureDate, "MORNING");

        verify(shiftRequestMapper).insert(any(ShiftRequest.class));
    }

    @Test
    void requestShift_WithPastDate_ShouldThrowException() {
        LocalDate pastDate = LocalDate.now().minusDays(1);

        ShiftValidationException exception = assertThrows(
                ShiftValidationException.class,
                () -> shiftRequestService.requestShift(userId, pastDate, "MORNING")
        );

        assertEquals("過去の日付にはシフトを申請できません", exception.getMessage());
        verify(shiftRequestMapper, never()).insert(any(ShiftRequest.class));
    }

    @Test
    void requestShift_WithNoneOption_ShouldDeleteExistingRequests() {
        List<ShiftRequest> existingRequests = Arrays.asList(testRequest);
        when(shiftRequestMapper.getRequestsByDate(userId, futureDate)).thenReturn(existingRequests);

        shiftRequestService.requestShift(userId, futureDate, "NONE");

        verify(shiftRequestMapper).getRequestsByDate(userId, futureDate);
        verify(shiftRequestMapper).delete(requestId);
    }

    @Test
    void cancelRequest_WithExistingNonSubmittedRequest_ShouldDeleteRequest() {
        when(shiftRequestMapper.getRequestById(requestId)).thenReturn(testRequest);

        shiftRequestService.cancelRequest(requestId);

        verify(shiftRequestMapper).getRequestById(requestId);
        verify(shiftRequestMapper).delete(requestId);
    }

    @Test
    void cancelRequest_WithNonExistingRequest_ShouldThrowException() {
        when(shiftRequestMapper.getRequestById(requestId)).thenReturn(null);

        ShiftNotFoundException exception = assertThrows(
                ShiftNotFoundException.class,
                () -> shiftRequestService.cancelRequest(requestId)
        );

        assertEquals("シフト希望が見つかりません", exception.getMessage());
        verify(shiftRequestMapper).getRequestById(requestId);
        verify(shiftRequestMapper, never()).delete(any());
    }

    @Test
    void cancelRequest_WithSubmittedRequest_ShouldThrowException() {
        testRequest.setSubmitted(true);
        when(shiftRequestMapper.getRequestById(requestId)).thenReturn(testRequest);

        ShiftValidationException exception = assertThrows(
                ShiftValidationException.class,
                () -> shiftRequestService.cancelRequest(requestId)
        );

        assertEquals("提出済みのシフト希望はキャンセルできません", exception.getMessage());
        verify(shiftRequestMapper).getRequestById(requestId);
        verify(shiftRequestMapper, never()).delete(any());
    }

    @Test
    void submitRequests_WithExistingRequests_ShouldSubmitRequests() {
        List<ShiftRequest> requests = Arrays.asList(testRequest);
        when(shiftRequestMapper.getRequestsByMonth(eq(userId), anyInt(), anyInt())).thenReturn(requests);

        shiftRequestService.submitRequests(userId, 2024, 7);

        verify(shiftRequestMapper).getRequestsByMonth(userId, 2024, 7);
        verify(shiftRequestMapper).submitRequests(userId, 2024, 7);
    }

    @Test
    void submitRequests_WithNoRequests_ShouldThrowException() {
        when(shiftRequestMapper.getRequestsByMonth(eq(userId), anyInt(), anyInt())).thenReturn(Collections.emptyList());

        ShiftValidationException exception = assertThrows(
                ShiftValidationException.class,
                () -> shiftRequestService.submitRequests(userId, 2024, 7)
        );

        assertEquals("提出するシフト希望がありません", exception.getMessage());
        verify(shiftRequestMapper).getRequestsByMonth(userId, 2024, 7);
        verify(shiftRequestMapper, never()).submitRequests(anyLong(), anyInt(), anyInt());
    }
} 