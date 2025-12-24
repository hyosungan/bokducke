package ssafy.bokduck.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import ssafy.bokduck.security.JwtTokenProvider;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class InferenceTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    public void testLandmarkInference() {
        // 1. Generate Token (Bypassing AuthController, creating valid token manually)
        String token = jwtTokenProvider.createToken("testuser", "ROLE_USER");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 2. Test Cases
        String[] inputs = {
                "멀티캠퍼스", // Expect Yeoksam-dong
                "낙성대역", // Expect Nakseongdae-dong
                "설악산", // Expect Sokcho-si
                "울산시 전체", // Expect Ulsan Metropolitan City
                "강남 아파트" // Expect Gangnam-gu
        };

        System.out.println("\n========== INFERENCE TEST RESULTS ==========");

        for (String input : inputs) {
            Map<String, String> body = new HashMap<>();
            body.put("message", input);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

            try {
                ResponseEntity<String> response = restTemplate.postForEntity(
                        "http://localhost:" + port + "/api/v1/llm/chat",
                        request,
                        String.class);

                System.out.println("INPUT: " + input);
                System.out.println("RESPONSE: " + response.getBody());
                System.out.println("--------------------------------------------");

                // Basic assertions (soft)
                assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();

            } catch (Exception e) {
                System.err.println("FAILED for input: " + input);
                e.printStackTrace();
            }
        }
        System.out.println("============================================\n");
    }
}
