CREATE TABLE IF NOT EXISTS user_account (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(128) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    nickname VARCHAR(40) NOT NULL,
    avatar_url VARCHAR(255) NOT NULL DEFAULT '',
    status VARCHAR(20) NOT NULL DEFAULT 'active',
    last_login_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_account_email UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS user_stats (
    user_id BIGINT PRIMARY KEY,
    total_profit DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    total_loss DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    sold_count INT NOT NULL DEFAULT 0,
    sold_buy_total DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    sold_sell_total DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    unsold_count INT NOT NULL DEFAULT 0,
    unsold_buy_total DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_stats_user_id FOREIGN KEY (user_id) REFERENCES user_account (id)
);

CREATE TABLE IF NOT EXISTS inventory_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    item_name VARCHAR(40) NOT NULL,
    buy_price DECIMAL(12, 2) NOT NULL,
    buy_time DATE NOT NULL,
    sell_price DECIMAL(12, 2) NULL,
    sell_time DATE NULL,
    trade_time DATE NULL,
    channel VARCHAR(20) NOT NULL,
    category VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    profit_amount DECIMAL(12, 2) NULL,
    public_posted TINYINT(1) NOT NULL DEFAULT 0,
    public_post_id BIGINT NULL,
    public_posted_at DATETIME NULL,
    remark VARCHAR(60) NULL,
    image_file_id VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_inventory_item_user_id FOREIGN KEY (user_id) REFERENCES user_account (id)
);

SET @drop_index_sql = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'inventory_item' AND index_name = 'idx_inventory_user_status'
        ),
        'DROP INDEX idx_inventory_user_status ON inventory_item',
        'SELECT 1'
    )
);
PREPARE stmt FROM @drop_index_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
CREATE INDEX idx_inventory_user_status ON inventory_item(user_id, status);
SET @drop_index_sql = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'inventory_item' AND index_name = 'idx_inventory_user_buy_time'
        ),
        'DROP INDEX idx_inventory_user_buy_time ON inventory_item',
        'SELECT 1'
    )
);
PREPARE stmt FROM @drop_index_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
CREATE INDEX idx_inventory_user_buy_time ON inventory_item(user_id, buy_time DESC);
SET @drop_index_sql = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'inventory_item' AND index_name = 'idx_inventory_user_buy_price'
        ),
        'DROP INDEX idx_inventory_user_buy_price ON inventory_item',
        'SELECT 1'
    )
);
PREPARE stmt FROM @drop_index_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
CREATE INDEX idx_inventory_user_buy_price ON inventory_item(user_id, buy_price DESC);
SET @drop_index_sql = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'inventory_item' AND index_name = 'idx_inventory_user_public'
        ),
        'DROP INDEX idx_inventory_user_public ON inventory_item',
        'SELECT 1'
    )
);
PREPARE stmt FROM @drop_index_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
CREATE INDEX idx_inventory_user_public ON inventory_item(user_id, public_posted);
SET @drop_index_sql = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'inventory_item' AND index_name = 'idx_inventory_public_post_id'
        ),
        'DROP INDEX idx_inventory_public_post_id ON inventory_item',
        'SELECT 1'
    )
);
PREPARE stmt FROM @drop_index_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
CREATE INDEX idx_inventory_public_post_id ON inventory_item(public_post_id);

CREATE TABLE IF NOT EXISTS public_post (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    source_inventory_item_id BIGINT NULL,
    item_name VARCHAR(40) NOT NULL,
    price DECIMAL(12, 2) NOT NULL,
    trade_time DATE NOT NULL,
    direction VARCHAR(20) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    category VARCHAR(20) NOT NULL,
    remark VARCHAR(60) NULL,
    image_file_id VARCHAR(255) NULL,
    untrusted_count INT NOT NULL DEFAULT 0,
    publisher_name VARCHAR(40) NULL,
    publisher_avatar VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_public_post_user_id FOREIGN KEY (user_id) REFERENCES user_account (id)
);

SET @drop_index_sql = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'public_post' AND index_name = 'idx_public_post_trade_time'
        ),
        'DROP INDEX idx_public_post_trade_time ON public_post',
        'SELECT 1'
    )
);
PREPARE stmt FROM @drop_index_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
CREATE INDEX idx_public_post_trade_time ON public_post(trade_time DESC);
SET @drop_index_sql = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'public_post' AND index_name = 'idx_public_post_price'
        ),
        'DROP INDEX idx_public_post_price ON public_post',
        'SELECT 1'
    )
);
PREPARE stmt FROM @drop_index_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
CREATE INDEX idx_public_post_price ON public_post(price DESC);
SET @drop_index_sql = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'public_post' AND index_name = 'idx_public_post_user'
        ),
        'DROP INDEX idx_public_post_user ON public_post',
        'SELECT 1'
    )
);
PREPARE stmt FROM @drop_index_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
CREATE INDEX idx_public_post_user ON public_post(user_id);
SET @drop_index_sql = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'public_post' AND index_name = 'idx_public_post_source_item'
        ),
        'DROP INDEX idx_public_post_source_item ON public_post',
        'SELECT 1'
    )
);
PREPARE stmt FROM @drop_index_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
CREATE INDEX idx_public_post_source_item ON public_post(source_inventory_item_id);
SET @drop_index_sql = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'public_post' AND index_name = 'idx_public_post_category'
        ),
        'DROP INDEX idx_public_post_category ON public_post',
        'SELECT 1'
    )
);
PREPARE stmt FROM @drop_index_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
CREATE INDEX idx_public_post_category ON public_post(category);

CREATE TABLE IF NOT EXISTS public_post_flag (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_public_post_flag_user_post UNIQUE (user_id, post_id),
    CONSTRAINT fk_public_post_flag_user_id FOREIGN KEY (user_id) REFERENCES user_account (id),
    CONSTRAINT fk_public_post_flag_post_id FOREIGN KEY (post_id) REFERENCES public_post (id)
);

SET @drop_index_sql = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'public_post_flag' AND index_name = 'idx_public_post_flag_post'
        ),
        'DROP INDEX idx_public_post_flag_post ON public_post_flag',
        'SELECT 1'
    )
);
PREPARE stmt FROM @drop_index_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
CREATE INDEX idx_public_post_flag_post ON public_post_flag(post_id);

CREATE TABLE IF NOT EXISTS operation_guard (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    action VARCHAR(64) NOT NULL,
    guard_key VARCHAR(128) NOT NULL,
    last_request_id VARCHAR(128) NULL,
    last_submit_at DATETIME NULL,
    recent_times_json TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_operation_guard_guard_key UNIQUE (guard_key),
    CONSTRAINT fk_operation_guard_user_id FOREIGN KEY (user_id) REFERENCES user_account (id)
);

SET @drop_index_sql = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'operation_guard' AND index_name = 'idx_operation_guard_user_action'
        ),
        'DROP INDEX idx_operation_guard_user_action ON operation_guard',
        'SELECT 1'
    )
);
PREPARE stmt FROM @drop_index_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
CREATE INDEX idx_operation_guard_user_action ON operation_guard(user_id, action);

CREATE TABLE IF NOT EXISTS feedback (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    content VARCHAR(500) NOT NULL,
    submit_date DATE NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_feedback_user_id FOREIGN KEY (user_id) REFERENCES user_account (id)
);

SET @drop_index_sql = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'feedback' AND index_name = 'idx_feedback_user'
        ),
        'DROP INDEX idx_feedback_user ON feedback',
        'SELECT 1'
    )
);
PREPARE stmt FROM @drop_index_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
CREATE INDEX idx_feedback_user ON feedback(user_id);
SET @drop_index_sql = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'feedback' AND index_name = 'idx_feedback_submit_date'
        ),
        'DROP INDEX idx_feedback_submit_date ON feedback',
        'SELECT 1'
    )
);
PREPARE stmt FROM @drop_index_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
CREATE INDEX idx_feedback_submit_date ON feedback(submit_date DESC);

CREATE TABLE IF NOT EXISTS file_asset (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    storage_provider VARCHAR(30) NOT NULL,
    bucket_name VARCHAR(100) NOT NULL,
    object_key VARCHAR(255) NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    is_public TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_file_asset_user_id FOREIGN KEY (user_id) REFERENCES user_account (id)
);

SET @drop_index_sql = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'file_asset' AND index_name = 'idx_file_asset_user'
        ),
        'DROP INDEX idx_file_asset_user ON file_asset',
        'SELECT 1'
    )
);
PREPARE stmt FROM @drop_index_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
CREATE INDEX idx_file_asset_user ON file_asset(user_id);
SET @drop_index_sql = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'file_asset' AND index_name = 'idx_file_asset_object_key'
        ),
        'DROP INDEX idx_file_asset_object_key ON file_asset',
        'SELECT 1'
    )
);
PREPARE stmt FROM @drop_index_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
CREATE INDEX idx_file_asset_object_key ON file_asset(object_key);
