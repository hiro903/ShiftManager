package io.shiftmanager.you.model;

import lombok.Data;
import java.time.LocalDate;

/**
 * シフト希望を管理するモデルクラス
 */
@Data
public class ShiftRequest {
    /** シフト希望ID */
    private Long requestId;
    
    /** ユーザーID */
    private Long userId;
    
    /** シフト希望日 */
    private LocalDate requestDate;
    
    /** 時間帯（午前・午後） */
    private Timezone timezone;
    
    /** 希望状態（申請中・承認済み・却下） */
    private Status status;
    
    /** 提出状態（一時保存・提出済み） */
    private boolean isSubmitted;
    
    /** 作成日時 */
    private LocalDate createdAt;
    
    /** 更新日時 */
    private LocalDate updatedAt;
} 