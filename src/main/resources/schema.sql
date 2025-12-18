-- Users Table
CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(100),
    phone VARCHAR(20),
    status VARCHAR(20) DEFAULT 'ACTIVE', -- ACTIVE, WITHDRAWN
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- User Interests
CREATE TABLE IF NOT EXISTS user_interests (
    interest_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    interest_type VARCHAR(50), -- REGION, KEYWORD, PRICE_RANGE
    value VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- DROP obsolete tables
DROP TABLE IF EXISTS property_scraps;
DROP TABLE IF EXISTS properties;

-- Dong Codes
CREATE TABLE IF NOT EXISTS dongcodes (
  dong_code VARCHAR(10) NOT NULL comment '법정동코드',
  sido_name VARCHAR(30) NULL DEFAULT NULL comment '시도이름',
  gugun_name VARCHAR(30) NULL DEFAULT NULL comment '구군이름',
  dong_name VARCHAR(30) NULL DEFAULT NULL comment '동이름',
  PRIMARY KEY (dong_code)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci comment '법정동코드테이블';

-- House Infos
CREATE TABLE IF NOT EXISTS houseinfos (
  apt_seq VARCHAR(20) NOT NULL comment '아파트코드',
  sgg_cd VARCHAR(5) NULL DEFAULT NULL comment '시군구코드',
  umd_cd VARCHAR(5) NULL DEFAULT NULL comment '읍면동코드',
  umd_nm VARCHAR(20) NULL DEFAULT NULL comment '읍면동이름',
  jibun VARCHAR(10) NULL DEFAULT NULL comment '지번',
  road_nm_sgg_cd VARCHAR(5) NULL DEFAULT NULL comment '도로명시군구코드',
  road_nm VARCHAR(20) NULL DEFAULT NULL comment '도로명',
  road_nm_bonbun VARCHAR(10) NULL DEFAULT NULL comment '도로명기초번호',
  road_nm_bubun VARCHAR(10) NULL DEFAULT NULL comment '도로명추가번호',
  apt_nm VARCHAR(40) NULL DEFAULT NULL comment '아파트이름',
  build_year INT NULL DEFAULT NULL comment '준공년도',
  latitude VARCHAR(45) NULL DEFAULT NULL comment '위도',
  longitude VARCHAR(45) NULL DEFAULT NULL comment '경도',
  PRIMARY KEY (apt_seq)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci comment '주택정보테이블';

-- House Deals
CREATE TABLE IF NOT EXISTS housedeals (
  no INT NOT NULL AUTO_INCREMENT comment '거래번호',
  apt_seq VARCHAR(20) NULL DEFAULT NULL comment '아파트코드',
  apt_dong VARCHAR(40) NULL DEFAULT NULL comment '아파트동',
  floor VARCHAR(3) NULL DEFAULT NULL comment '아파트층',
  deal_year INT NULL DEFAULT NULL comment '거래년도',
  deal_month INT NULL DEFAULT NULL comment '거래월',
  deal_day INT NULL DEFAULT NULL comment '거래일',
  exclu_use_ar DECIMAL(7,2) NULL DEFAULT NULL  comment '아파트면적',
  deal_amount VARCHAR(10) NULL DEFAULT NULL  comment '거래가격',
  PRIMARY KEY (no),
  CONSTRAINT apt_seq_to_house_info FOREIGN KEY (apt_seq) REFERENCES houseinfos (apt_seq)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci comment '주택거래정보테이블';

-- Property Scraps (Updated to reference houseinfos)
CREATE TABLE IF NOT EXISTS property_scraps (
    user_id BIGINT NOT NULL,
    apt_seq VARCHAR(20) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, apt_seq),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (apt_seq) REFERENCES houseinfos(apt_seq) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- Board Posts
CREATE TABLE IF NOT EXISTS board_posts (
    post_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    category VARCHAR(50),
    view_count INT DEFAULT 0,
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Board Comments
CREATE TABLE IF NOT EXISTS board_comments (
    comment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    parent_id BIGINT, -- For nested comments
    content TEXT NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES board_posts(post_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- LLM Requests (GPT/Gemini Logs)
CREATE TABLE IF NOT EXISTS llm_requests (
    llm_request_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    apt_seq VARCHAR(20) NULL,
    type VARCHAR(50), -- SEARCH, CHAT
    prompt TEXT,
    response TEXT, -- JSON or Text response being too long might need MEDIUMTEXT
    condition_json TEXT, -- Search filters extracted
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
