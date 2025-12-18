package ssafy.bokduck.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ssafy.bokduck.dto.UserDto;
import ssafy.bokduck.service.UserService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody UserDto userDto) {
        try {
            userService.register(userDto);
            return ResponseEntity.ok("User registered successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody ssafy.bokduck.dto.LoginRequestDto loginRequest) {
        try {
            String token = userService.login(loginRequest.getEmail(), loginRequest.getPassword());
            Map<String, String> response = new HashMap<>();
            response.put("accessToken", token);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }
}
