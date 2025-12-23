package ssafy.bokduck.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ssafy.bokduck.dto.UserDto;
import ssafy.bokduck.service.UserService;

@RestController
@RequestMapping("/api/v1")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return getProfileInternal(userDetails);
    }

    @GetMapping("/user/profile")  // 기존 호환성을 위한 별칭
    public ResponseEntity<?> getProfileLegacy(@AuthenticationPrincipal UserDetails userDetails) {
        return getProfileInternal(userDetails);
    }

    private ResponseEntity<?> getProfileInternal(UserDetails userDetails) {
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

    @PutMapping("/profile/edit")
    public ResponseEntity<?> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UserDto userDto
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        try {
            UserDto currentUser = userService.getUserProfileByEmail(userDetails.getUsername());
            if (currentUser == null) {
                return ResponseEntity.notFound().build();
            }

            // userId 설정
            userDto.setUserId(currentUser.getUserId());
            
            // 이메일은 변경 불가 (보안상 이유)
            userDto.setEmail(null);
            
            // 사용자 정보 업데이트
            userService.updateUser(userDto);
            
            return ResponseEntity.ok("Profile updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/profile")
    public ResponseEntity<?> deleteProfile(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        try {
            UserDto user = userService.getUserProfileByEmail(userDetails.getUsername());
            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            userService.deleteUser(user.getUserId());
            return ResponseEntity.ok("User deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
