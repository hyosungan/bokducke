package ssafy.bokduck.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ssafy.bokduck.dto.UserDto;
import ssafy.bokduck.mapper.UserMapper;
import ssafy.bokduck.security.JwtTokenProvider;

@Service
@Transactional
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public UserService(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public void register(UserDto userDto) {
        // Check if email exists
        if (userMapper.findByEmail(userDto.getEmail()) != null) {
            throw new RuntimeException("Email already exists");
        }

        // Hash password
        userDto.setPasswordHash(passwordEncoder.encode(userDto.getPassword()));
        userDto.setStatus("ACTIVE");

        userMapper.insertUser(userDto);
    }

    public String login(String email, String password) {
        UserDto user = userMapper.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("Invalid password");
        }

        return jwtTokenProvider.createToken(email, user.getUserId());
    }

    public UserDto getUserProfile(Long userId) {
        return userMapper.findById(userId);
    }

    public UserDto getUserProfileByEmail(String email) {
        return userMapper.findByEmail(email);
    }

    public void updateUser(UserDto userDto) {
        userMapper.updateUser(userDto);
    }

    public void deleteUser(Long userId) {
        userMapper.deleteUser(userId);
    }
}
