package assignment.ntc.neo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.StreamSupport;

public class JsonParserTest {

    private static NearEarthObject mapToNeo(JsonNode jsonNode) {
            String id = jsonNode.get("id").asText();
            String name = jsonNode.get("name").asText();
            JsonNode approachDataNode = jsonNode.get("close_approach_data").get(0);
            LocalDate approachDate = LocalDate.parse(approachDataNode.get("close_approach_date").asText());
            BigDecimal missDistance = new BigDecimal(approachDataNode.get("miss_distance").get("kilometers").asText());
            return new NearEarthObject(id, name, missDistance, approachDate);
    }

    @Test
    void testJsonParsing() throws IOException {
        String neoJsonText = new String(
                JsonParserTest.class.getClassLoader().getResourceAsStream("nasa-sample-response.json").readAllBytes(), "UTF-8");

        JsonNode neoJsonRootNode = new ObjectMapper().readTree(neoJsonText);
        JsonNode neoJsonNode = neoJsonRootNode.get("near_earth_objects");
        List<NearEarthObject> expectedClosestNearEarthObjects = List.of(new NearEarthObject("2465633", "465633 (2009 JR5)", new BigDecimal("10000.204452618"), LocalDate.parse("2015-09-08")),
                new NearEarthObject("3713989", "(2015 FC35)", new BigDecimal("10000.204452618"), LocalDate.parse("2015-09-07")));
        NearEarthObject expectedClosestNearEarthObject = new NearEarthObject("2465633", "465633 (2009 JR5)", new BigDecimal("10000.204452618"), LocalDate.parse("2015-09-08"));

        List<NearEarthObject> closestNeosUsingCollector = StreamSupport.stream(neoJsonNode.spliterator(), true)
                .filter(jsonNode -> jsonNode.isArray())
                .flatMap(jsonNode-> StreamSupport.stream(jsonNode.spliterator(), true))
                .filter(arrayElement -> arrayElement.isObject())
                .map(JsonParserTest::mapToNeo)
                .collect(CollectorUtil.minList(Comparator.comparing(NearEarthObject::missDistanceInKm)));
        System.out.println(closestNeosUsingCollector);
        Assertions.assertEquals(expectedClosestNearEarthObjects, closestNeosUsingCollector);
    }

}
