package io.shiftmanager.you.service;

import io.shiftmanager.you.exception.ShiftNotFoundException;
import io.shiftmanager.you.exception.ShiftValidationException;
import io.shiftmanager.you.mapper.ShiftRequestMapper;
import io.shiftmanager.you.model.ShiftRequest;
import io.shiftmanager.you.model.Status;
import io.shiftmanager.you.model.Timezone;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShiftRequestService {

    private final ShiftRequestMapper shiftRequestMapper;

    public List<ShiftRequest> getRequestsByMonth(Long userId, int year, int month) {
        return shiftRequestMapper.getRequestsByMonth(userId, year, month);
    }

    @Transactional
    public void requestShift(Long userId, LocalDate date, String timezoneStr) {
        // 過去の日付のチェック
        if (date.isBefore(LocalDate.now())) {
            throw new ShiftValidationException("過去の日付にはシフトを申請できません");
        }
        
        // 「勤務しない」オプションの場合は、その日付の既存の申請をすべて削除
        if ("NONE".equals(timezoneStr)) {
            deleteRequestsByDate(userId, date);
            return;
        }

        ShiftRequest request = new ShiftRequest();
        request.setUserId(userId);
        request.setRequestDate(date);
        request.setTimezone(Timezone.fromString(timezoneStr));
        request.setStatus(Status.REQUESTED);
        request.setSubmitted(false);
        shiftRequestMapper.insert(request);
    }
    
    @Transactional
    public void deleteRequestsByDate(Long userId, LocalDate date) {
        List<ShiftRequest> existingRequests = shiftRequestMapper.getRequestsByDate(userId, date);
        for (ShiftRequest request : existingRequests) {
            if (!request.isSubmitted()) {
                shiftRequestMapper.delete(request.getRequestId());
            } else {
                throw new ShiftValidationException("提出済みのシフト希望は変更できません");
            }
        }
    }

    @Transactional
    public void cancelRequest(Long requestId) {
        ShiftRequest request = shiftRequestMapper.getRequestById(requestId);
        if (request == null) {
            throw new ShiftNotFoundException("シフト希望が見つかりません");
        }

        if (request.isSubmitted()) {
            throw new ShiftValidationException("提出済みのシフト希望はキャンセルできません");
        }

        shiftRequestMapper.delete(requestId);
    }

    @Transactional
    public void submitRequests(Long userId, int year, int month) {
        List<ShiftRequest> requests = getRequestsByMonth(userId, year, month);
        if (requests.isEmpty()) {
            throw new ShiftValidationException("提出するシフト希望がありません");
        }

        shiftRequestMapper.submitRequests(userId, year, month);
    }
} 