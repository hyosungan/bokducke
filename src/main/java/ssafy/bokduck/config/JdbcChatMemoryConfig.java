package ssafy.bokduck.config;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import ssafy.bokduck.memory.JdbcChatMemory;

@Configuration
public class JdbcChatMemoryConfig {

    private final JdbcTemplate jdbcTemplate;

    public JdbcChatMemoryConfig(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Bean
    public ChatMemory chatMemory() {
        return new JdbcChatMemory(jdbcTemplate);
    }
}
