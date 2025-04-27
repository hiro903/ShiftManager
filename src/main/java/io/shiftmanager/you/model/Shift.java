package io.shiftmanager.you.model;

import lombok.Data;
import java.time.LocalDate;

/**
 * シフトの基本情報を定義するクラス
 */
@Data
public class Shift {
    /** シフトを識別するための番号 */
    private Long shiftId;

    /** シフトを登録した従業員の番号 */
    private Long userId;

    /** シフトの日付 */
    private LocalDate shiftDate;
    
    /** リクエスト日付（テンプレート用） */
    public LocalDate getRequestDate() {
        return shiftDate;
    }
    
    /** 提出済みかどうか */
    public boolean isProcessed() {
        return Status.CONFIRMED.equals(status) || Status.PUBLISHED.equals(status);
    }

    /** 時間帯（朝・夕） */
    private String timezone;

    /** シフトの状態（申請中・承認済み・却下） */
    private Status status;

    /** 作成日時 */
    private LocalDate createdAt;

    /** 更新日時 */
    private LocalDate updatedAt;

    /** リクエストID（テンプレート用） */
    public Long getRequestId() {
        return shiftId;
    }

    /** ユーザー名を表示するために追加 */
    private String username;
} 