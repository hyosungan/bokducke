package ssafy.bokduck.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ssafy.bokduck.dto.UserDto;
import ssafy.bokduck.service.UserService;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        UserDto user = userService.getUserProfileByEmail(userDetails.getUsername());
        if (user != null) {
            user.setPasswordHash(null); // Hide password hash
            user.setPassword(null); // Hide raw password if set
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
