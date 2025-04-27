package io.shiftmanager.you.mapper;

import io.shiftmanager.you.model.ShiftRequest;
import io.shiftmanager.you.model.Status;
import io.shiftmanager.you.model.Timezone;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = {"/sql/schema.sql", "/sql/test-data.sql"})
class ShiftRequestMapperTest {

    @Autowired
    private ShiftRequestMapper shiftRequestMapper;

    @Test
    void getRequestsByMonth_ShouldReturnRequestsForMonth() {
        // Assuming test-data.sql inserts some shift requests for user 1 in July 2024
        List<ShiftRequest> results = shiftRequestMapper.getRequestsByMonth(1L, 2024, 7);
        
        assertFalse(results.isEmpty());
        results.forEach(request -> {
            assertEquals(1L, request.getUserId());
            assertEquals(2024, request.getRequestDate().getYear());
            assertEquals(7, request.getRequestDate().getMonthValue());
        });
    }

    @Test
    void getRequestsByDate_ShouldReturnRequestsForDate() {
        // Assuming test-data.sql inserts a shift request for user 1 on July 15, 2024
        LocalDate testDate = LocalDate.of(2024, 7, 15);
        List<ShiftRequest> results = shiftRequestMapper.getRequestsByDate(1L, testDate);
        
        assertFalse(results.isEmpty());
        results.forEach(request -> {
            assertEquals(1L, request.getUserId());
            assertEquals(testDate, request.getRequestDate());
        });
    }

    @Test
    void insert_ShouldInsertNewRequest() {
        // Create a new shift request
        ShiftRequest request = new ShiftRequest();
        request.setUserId(1L);
        request.setRequestDate(LocalDate.of(2024, 7, 20));
        request.setTimezone(Timezone.AFTERNOON);
        request.setStatus(Status.REQUESTED);
        request.setSubmitted(false);
        
        // Insert the request
        shiftRequestMapper.insert(request);
        
        // Verify the request was inserted with an ID
        assertNotNull(request.getRequestId());
        
        // Fetch the inserted request
        ShiftRequest insertedRequest = shiftRequestMapper.getRequestById(request.getRequestId());
        
        // Verify the inserted request
        assertNotNull(insertedRequest);
        assertEquals(request.getUserId(), insertedRequest.getUserId());
        assertEquals(request.getRequestDate(), insertedRequest.getRequestDate());
        assertEquals(request.getTimezone(), insertedRequest.getTimezone());
        assertEquals(request.getStatus(), insertedRequest.getStatus());
        assertEquals(request.isSubmitted(), insertedRequest.isSubmitted());
    }

    @Test
    void updateStatus_ShouldUpdateRequestStatus() {
        // Assuming test-data.sql inserts a shift request with ID 1
        Long requestId = 1L;
        Status newStatus = Status.APPROVED;
        
        // Update the status
        shiftRequestMapper.updateStatus(requestId, newStatus);
        
        // Fetch the updated request
        ShiftRequest updatedRequest = shiftRequestMapper.getRequestById(requestId);
        
        // Verify the status was updated
        assertEquals(newStatus, updatedRequest.getStatus());
    }

    @Test
    void submitRequests_ShouldMarkRequestsAsSubmitted() {
        // Assuming test-data.sql inserts some non-submitted shift requests for user 1 in July 2024
        Long userId = 1L;
        int year = 2024;
        int month = 7;
        
        // Submit the requests
        shiftRequestMapper.submitRequests(userId, year, month);
        
        // Fetch the submitted requests
        List<ShiftRequest> submittedRequests = shiftRequestMapper.getRequestsByMonth(userId, year, month);
        
        // Verify all requests are marked as submitted
        assertFalse(submittedRequests.isEmpty());
        submittedRequests.forEach(request -> 
            assertTrue(request.isSubmitted())
        );
    }

    @Test
    void delete_ShouldRemoveRequest() {
        // Assuming test-data.sql inserts a shift request with ID 1
        Long requestId = 1L;
        
        // Verify the request exists
        ShiftRequest request = shiftRequestMapper.getRequestById(requestId);
        assertNotNull(request);
        
        // Delete the request
        shiftRequestMapper.delete(requestId);
        
        // Verify the request no longer exists
        ShiftRequest deletedRequest = shiftRequestMapper.getRequestById(requestId);
        assertNull(deletedRequest);
    }

    @Test
    void getRequestById_ShouldReturnRequestWithGivenId() {
        // Assuming test-data.sql inserts a shift request with ID 1
        Long requestId = 1L;
        
        // Fetch the request
        ShiftRequest request = shiftRequestMapper.getRequestById(requestId);
        
        // Verify the request
        assertNotNull(request);
        assertEquals(requestId, request.getRequestId());
    }
} 