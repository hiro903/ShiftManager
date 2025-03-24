CREATE TABLE IF NOT EXISTS users (
    user_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    is_active BOOLEAN DEFAULT TRUE,
    is_admin BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
INSERT INTO users (username, password, email, is_active, is_admin) VALUES ('anna', 'shiii', 'anna@yahoo.com', true, false);


-- SHIFT_REQUESTテーブル
CREATE TABLE shifts (
    shift_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    shift_date DATE NOT NULL,
    timezone ENUM('morning', 'afternoon') DEFAULT 'morning',
    status ENUM('requested', 'approved', 'rejected') DEFAULT 'requested',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    UNIQUE KEY (user_id, shift_date, timezone)
);

CREATE TABLE confirmed(
    confirmed_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    shift_id INT UNSIGNED,
    confirmed_date DATE NOT NULL,
    timezone  ENUM('morning', 'afternoon') DEFAULT 'morning',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (shift_id) REFERENCES shifts(shift_id),
    UNIQUE KEY (user_id, confirmed_date, timezone)
);

CREATE TABLE shift_periods (
    period_id INT PRIMARY KEY AUTO_INCREMENT,
    year INT NOT NULL,
    month INT NOT NULL,
    period ENUM('submitted', 'deadline', 'notified') DEFAULT 'submitted',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY (year, month)
);






















