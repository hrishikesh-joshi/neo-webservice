package assignment.ntc.neo;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = NasaWebClient.class)
@TestPropertySource(value = "/application.properties")
class NasaWebClientTest {

    public static MockWebServer mockWebServer;

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Autowired
    private NasaWebClient nasaWebClient;

    @DynamicPropertySource
    static void backendProperties(DynamicPropertyRegistry registry) {
        registry.add("neo.api.url", () -> mockWebServer.url("/nasa-api").toString());
    }

    @Test
    void test_findNearEarthObjects_returns_expected_result() throws IOException, InterruptedException {
        String nasaApiResponseJson = Files.readString(ResourceUtils.getFile("classpath:nasa-sample-response.json").toPath());
        Assert.assertNotNull(nasaApiResponseJson);
        mockWebServer.enqueue(new MockResponse().setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(nasaApiResponseJson));

        List<NearEarthObject> expectedClosestNearEarthObjects = List.of(new NearEarthObject("2465633", "465633 (2009 JR5)", new BigDecimal("10000.204452618"), LocalDate.parse("2015-09-08")),
                new NearEarthObject("3713989", "(2015 FC35)", new BigDecimal("10000.204452618"), LocalDate.parse("2015-09-07")));
        List<NearEarthObject> actualClosestNearEarthObjects = nasaWebClient.findNearEarthObjects(LocalDate.parse("2015-09-07"), LocalDate.parse("2015-09-14"))
                .block();
        Assert.assertNotNull(actualClosestNearEarthObjects);
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("GET");
        assertThat(recordedRequest.getPath()).isEqualTo("/nasa-api?api_key=fUy9rTc5wMKVL8KkANAa4eWta0y8afTeJggoghlJ&start_date=2015-09-07&end_date=2015-09-14");
        Assertions.assertIterableEquals(expectedClosestNearEarthObjects, actualClosestNearEarthObjects);
    }

    @Test
    void test_findNearEarthObjects_throws_exception_on_invalid_date() throws IOException, InterruptedException {
        String nasaApiResponseJson = Files.readString(ResourceUtils.getFile("classpath:nasa-sample-response.json").toPath());
        Assert.assertNotNull(nasaApiResponseJson);
        mockWebServer.enqueue(new MockResponse().setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(nasaApiResponseJson));

        List<NearEarthObject> expectedClosestNearEarthObjects = List.of(new NearEarthObject("2465633", "465633 (2009 JR5)", new BigDecimal("10000.204452618"), LocalDate.parse("2015-09-08")),
                new NearEarthObject("3713989", "(2015 FC35)", new BigDecimal("10000.204452618"), LocalDate.parse("2015-09-07")));

        Assertions.assertThrows(IllegalArgumentException.class, () ->  nasaWebClient.findNearEarthObjects(LocalDate.parse("2015-09-07"), LocalDate.parse("2015-09-06")));

    }
}