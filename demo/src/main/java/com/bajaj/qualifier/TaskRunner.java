package com.bajaj.qualifier;

import com.bajaj.qualifier.model.RegistrationRequest;
import com.bajaj.qualifier.model.RegistrationResponse;
import com.bajaj.qualifier.model.SubmissionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class TaskRunner implements CommandLineRunner {

    @Autowired
    private RestTemplate restTemplate;

    // --- UPDATED DETAILS ---
    private static final String MY_NAME = "Stuti Gupta"; // Change this if you want your real name
    private static final String MY_REG_NO = "22BRS1274";
    private static final String MY_EMAIL = "Stuti0801@gmail.com"; // Change to your email

    // URL from documentation [cite: 9]
    private static final String GENERATE_URL = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

    @Override
    public void run(String... args) throws Exception {
        System.out.println(">>> Starting Application for RegNo: " + MY_REG_NO);

        // 1. Prepare Registration Request
        RegistrationRequest regRequest = new RegistrationRequest(MY_NAME, MY_REG_NO, MY_EMAIL);
        HttpHeaders initHeaders = new HttpHeaders();
        initHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RegistrationRequest> initEntity = new HttpEntity<>(regRequest, initHeaders);

        try {
            // 2. Send POST to generate webhook
            System.out.println(">>> Sending Registration Request...");
            ResponseEntity<RegistrationResponse> responseEntity = restTemplate.postForEntity(
                    GENERATE_URL,
                    initEntity,
                    RegistrationResponse.class
            );

            RegistrationResponse response = responseEntity.getBody();

            if (response != null) {
                String webhookUrl = response.getWebhookUrl();
                String accessToken = response.getAccessToken();

                System.out.println(">>> Token Received.");
                System.out.println(">>> Webhook URL: " + webhookUrl);

                // 3. Get the solution (Logic specifically for Question 2)
                String finalSqlQuery = getQuestion2Solution();

                System.out.println(">>> SQL Solution Prepared: " + finalSqlQuery);

                // 4. Submit Solution [cite: 24-32]
                HttpHeaders authHeaders = new HttpHeaders();
                authHeaders.setContentType(MediaType.APPLICATION_JSON);
                authHeaders.set("Authorization", accessToken); // JWT Token [cite: 27]

                SubmissionRequest subRequest = new SubmissionRequest(finalSqlQuery);
                HttpEntity<SubmissionRequest> subEntity = new HttpEntity<>(subRequest, authHeaders);

                // Use the dynamic webhook URL received, or fallback if null
                String targetUrl = (webhookUrl != null && !webhookUrl.isEmpty())
                        ? webhookUrl
                        : "https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA";

                System.out.println(">>> Submitting to: " + targetUrl);

                String result = restTemplate.postForObject(targetUrl, subEntity, String.class);
                System.out.println("**************************************************");
                System.out.println("FINAL RESPONSE: " + result);
                System.out.println("**************************************************");

            } else {
                System.err.println("Error: Failed to receive response body from registration API.");
            }

        } catch (Exception e) {
            System.err.println(">>> Exception occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Solves Question 2: Average age of high earners per department.
     * Logic derived from 'SQL Qwestion 2 JAVA.pdf' [cite: 73-95].
     */
    private String getQuestion2Solution() {
        // REQUIREMENTS:
        // 1. Filter: Salary > 70000 [cite: 73]
        // 2. Output: Department Name, Avg Age, Employee List (max 10) [cite: 75-79]
        // 3. Order: Department ID Descending [cite: 95]

        return "SELECT " +
                "d.DEPARTMENT_NAME, " +
                "AVG(TIMESTAMPDIFF(YEAR, e.DOB, NOW())) AS AVERAGE_AGE, " +
                "SUBSTRING_INDEX(GROUP_CONCAT(CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) SEPARATOR ', '), ', ', 10) AS EMPLOYEE_LIST " +
                "FROM DEPARTMENT d " +
                "JOIN EMPLOYEE e ON d.DEPARTMENT_ID = e.DEPARTMENT " +
                "WHERE e.EMP_ID IN (SELECT DISTINCT EMP_ID FROM PAYMENTS WHERE AMOUNT > 70000) " +
                "GROUP BY d.DEPARTMENT_ID, d.DEPARTMENT_NAME " +
                "ORDER BY d.DEPARTMENT_ID DESC";
    }
}