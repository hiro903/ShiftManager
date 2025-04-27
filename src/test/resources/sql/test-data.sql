-- テストユーザーデータ
INSERT INTO users (user_id, username, password, email, is_active, is_admin)
VALUES (1, 'testuser', '$2a$10$rJ7S6kOjkL9MakJSQc2RTeWDS62ERLjVQZuFiPUc/fq26Apj7Ugdm', 'test@example.com', true, false);

-- シフト希望データ
-- 2024年7月のテストデータ
INSERT INTO shift_requests (request_id, user_id, request_date, timezone, status, is_submitted)
VALUES (1, 1, '2024-07-01', 'MORNING', 'REQUESTED', false);

INSERT INTO shift_requests (request_id, user_id, request_date, timezone, status, is_submitted)
VALUES (2, 1, '2024-07-02', 'AFTERNOON', 'REQUESTED', false);

INSERT INTO shift_requests (request_id, user_id, request_date, timezone, status, is_submitted)
VALUES (3, 1, '2024-07-15', 'MORNING', 'REQUESTED', false);

-- 確定シフトデータ
INSERT INTO confirmed_shifts (confirmed_id, user_id, request_id, confirmed_date, timezone)
VALUES (1, 1, 1, '2024-07-01', 'MORNING'); 