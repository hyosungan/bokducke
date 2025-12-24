package ssafy.bokduck.memory;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class JdbcChatMemory implements ChatMemory {

    private final JdbcTemplate jdbcTemplate;

    public JdbcChatMemory(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void add(String conversationId, Message message) {
        String sql = "INSERT INTO conversation_history (conversation_id, message_type, message_content) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, conversationId, message.getMessageType().getValue(), message.getText());
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        for (Message message : messages) {
            add(conversationId, message);
        }
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        String sql = "SELECT message_type, message_content FROM conversation_history WHERE conversation_id = ? ORDER BY ID DESC LIMIT ?";
        List<Message> messages = jdbcTemplate.query(sql, new MessageRowMapper(), conversationId, lastN);
        Collections.reverse(messages); // Return in chronological order
        return messages;
    }

    @Override
    public void clear(String conversationId) {
        String sql = "DELETE FROM conversation_history WHERE conversation_id = ?";
        jdbcTemplate.update(sql, conversationId);
    }

    private static class MessageRowMapper implements RowMapper<Message> {
        @Override
        public Message mapRow(ResultSet rs, int rowNum) throws SQLException {
            String type = rs.getString("message_type");
            String content = rs.getString("message_content");

            if (MessageType.USER.getValue().equalsIgnoreCase(type)) {
                return new UserMessage(content);
            } else if (MessageType.ASSISTANT.getValue().equalsIgnoreCase(type)) {
                return new AssistantMessage(content);
            } else if (MessageType.SYSTEM.getValue().equalsIgnoreCase(type)) {
                return new SystemMessage(content);
            }
            // Default fallback
            return new UserMessage(content);
        }
    }
}
