CREATE TABLE IF NOT EXISTS count_seq (
    id bigint NOT NULL AUTO_INCREMENT,
    name varchar(255) DEFAULT NULL,
    count int DEFAULT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS conversation_history (
    conversation_id varchar(255) NOT NULL,
    message_type varchar(255) NOT NULL,
    message_content text NOT NULL,
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ID int NOT NULL AUTO_INCREMENT,
    PRIMARY KEY (ID)
);

CREATE TABLE IF NOT EXISTS chat_memory (
	conversation_id varchar(100) NOT NULL,
	message_content text NOT NULL,
    message_type varchar(100) NOT NULL,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (conversation_id, created_at)
);
