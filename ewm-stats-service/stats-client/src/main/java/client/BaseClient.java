package client;

import org.springframework.http.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

public class BaseClient {
    protected final RestTemplate rest;

    public BaseClient(RestTemplate rest) {
        this.rest = rest;
    }

    protected ResponseEntity<Object> get(String path) {
        return makeAndSendRequest(HttpMethod.GET, path, null);
    }

    protected <T> ResponseEntity<Object> post(String path, T body) {
        return makeAndSendRequest(HttpMethod.POST, path, body);
    }

    private <T> ResponseEntity<Object> makeAndSendRequest(
            HttpMethod method, String path, T body) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<T> requestEntity = new HttpEntity<>(body, headers);

        try {
            return rest.exchange(
                    path, method, requestEntity, Object.class);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(e.getResponseBodyAsByteArray());
        }
    }

}
