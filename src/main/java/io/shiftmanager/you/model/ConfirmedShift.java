package io.shiftmanager.you.model;

import lombok.Data;
import java.time.LocalDate;

/**
 * 確定シフトを管理するモデルクラス
 */
@Data
public class ConfirmedShift {
    /** 確定シフトID */
    private Long confirmedId;
    
    /** ユーザーID */
    private Long userId;
    
    /** シフト希望ID */
    private Long requestId;
    
    /** 確定シフト日 */
    private LocalDate confirmedDate;
    
    /** 時間帯（午前・午後） */
    private Timezone timezone;
    
    /** 作成日時 */
    private LocalDate createdAt;
    
    /** 更新日時 */
    private LocalDate updatedAt;
} 