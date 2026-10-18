package client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import ru.practicum.ewm.stats.dto.EndpointHit;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;


public class StatsClientTest {
    private RestTemplate restTemplate;
    private MockRestServiceServer mockServer;
    private StatsClient client;

    private final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(restTemplate);
        client = new StatsClient(restTemplate, "http://localhost:9090");
    }

    private static int counter = 0;

    @Test
    void shouldSendHitAndGetStats() {
        LocalDateTime now = LocalDateTime.now();
        String uri = "/client-test-" + (++counter);

        EndpointHit hit = new EndpointHit(
                null,
                "client-test",
                uri,
                "192.168.0.10",
                now.format(formatter)
        );

        mockServer.expect(requestTo("http://localhost:9090/hit"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.CREATED));

        mockServer.expect(requestTo(org.hamcrest.CoreMatchers.startsWith("http://localhost:9090/stats")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        "[{\"app\":\"client-test\",\"uri\":\"" + uri + "\",\"hits\":1}]",
                        MediaType.APPLICATION_JSON
                ));

        client.hit(hit);

        var response = client.stats(
                now.minusMinutes(1),
                now.plusMinutes(1),
                List.of(uri),
                false
        );

        mockServer.verify();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
