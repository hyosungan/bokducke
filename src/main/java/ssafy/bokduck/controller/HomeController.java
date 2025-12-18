package ssafy.bokduck.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public ResponseEntity<String> home() {
        return ResponseEntity.ok(
                "Welcome to Bokduck API Server! 🦆\n\nActive Endpoints:\n- /api/v1/auth/signup\n- /api/v1/auth/login\n- /api/v1/estate\n- /api/v1/board");
    }
}
