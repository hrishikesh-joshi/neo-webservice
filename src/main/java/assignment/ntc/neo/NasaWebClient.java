package assignment.ntc.neo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.StreamSupport;
import static assignment.ntc.neo.CollectorUtil.minList;

@Component
/**
 * NasaWebClient uses WebClient to fetch all Near Earth Objects within given startDate and endDate using Nasa's web api.
 * NASA web api : https://api.nasa.gov/neo/rest/v1/feed is configured in properties file. It needs an api key which is also configured in properties file
 * NASA api returns data for 7 days in one request, so end date can be at most startdate + 7 days, 400 bad request error otherwise
 * The api enables client to send 1000 requests per apikey per hour.
 * TDOO: Handle api request limits
 */
public class NasaWebClient {

    @Value("${neo.api.key}")
    private String apiKey;

    @Value("${neo.api.uri}")
    private String apiURI;

    /**
     * findNearEarthObjects : API to find closest NearEarthObject from earth during given period
     * @param startDate LocalDate
     * @param endDate LocalDate
     * @return ParallelFlux<ArrayList<NearEarthObject>>
     */
    Mono<List<NearEarthObject>> findNearEarthObjects(LocalDate startDate, LocalDate endDate) {
        if(isInvalidPeriod(startDate, endDate)) {
            System.err.println("End date "+endDate+" can not be earlier than start date "+startDate);
            throw new IllegalArgumentException("End date "+endDate+" can not be earlier than start date "+startDate);
        }

        return Flux.fromIterable(getPeiodsToFindNeos(startDate, endDate))
                .parallel()
                .flatMap(periodToFindNeo -> callNasaApi(periodToFindNeo.startDate(), periodToFindNeo.endDate()))
                .map(this::processApiResponse)
                .flatMap(neos -> Flux.fromIterable(neos))
                .sequential()
                        .collect(ArrayList::new, collectClosestNeo);
        //TODO: Implement Error handling, handle api call limit of 1000 calls per hour
    }

    private BiConsumer<List<NearEarthObject>,? super NearEarthObject> collectClosestNeo = (collector, neo) -> {
        int comparison;
        if(collector.isEmpty() || (comparison = neo.missDistanceInKm().compareTo(collector.get(0).missDistanceInKm())) == 0) {
            collector.add(neo);
        } else if(comparison < 0){
            collector.clear(); collector.add(neo);
        }
    };

    private boolean isInvalidPeriod(LocalDate startDate, LocalDate endDate) {
        return Period.between(startDate, endDate).isNegative();
    }

    private static NearEarthObject mapToNeo(JsonNode jsonNode) {
        String id = jsonNode.get("id").asText();
        String name = jsonNode.get("name").asText();
        JsonNode approachDataNode = jsonNode.get("close_approach_data").get(0);
        LocalDate approachDate = LocalDate.parse(approachDataNode.get("close_approach_date").asText());
        BigDecimal missDistance = new BigDecimal(approachDataNode.get("miss_distance").get("kilometers").asText());
        return new NearEarthObject(id, name, missDistance, approachDate);
    }

    private Mono<String> callNasaApi(LocalDate startDate, LocalDate endDate) {
        return WebClient.create(apiURI)
                .get()
                .uri(uriBuilder -> uriBuilder.queryParam("api_key", apiKey)
                        .queryParam("start_date", startDate)
                        .queryParam("end_date", endDate).build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class);
    }

    private List<NearEarthObject> processApiResponse(String jsonResponse) {
        JsonNode neoJsonRootNode = null;
        try {
            neoJsonRootNode = new ObjectMapper().readTree(jsonResponse);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse json", e);
        }
        JsonNode neoJsonNode = neoJsonRootNode.get("near_earth_objects");

        List<NearEarthObject> closestNearEarthObjects = StreamSupport.stream(neoJsonNode.spliterator(), true)
                .filter(jsonNode -> jsonNode.isArray())
                .flatMap(jsonNode-> StreamSupport.stream(jsonNode.spliterator(), true))
                .filter(arrayElement -> arrayElement.isObject())
                .map(jsonNode -> mapToNeo(jsonNode))
                //.peek(neo -> printNode(neo.toString()))
                .collect(
                        minList(Comparator.comparing(NearEarthObject::missDistanceInKm))
                );
        return closestNearEarthObjects;
    }

    private List<Pair<LocalDate>> getPeiodsToFindNeos(LocalDate startDate, LocalDate endDate) {
        List<Pair<LocalDate>> periodsToFindNeos = new ArrayList<>();
        while(startDate.plusDays(7).isBefore(endDate) || startDate.plusDays(7).equals(endDate)) {
            periodsToFindNeos.add(new Pair(startDate, startDate.plusDays(7)));
            startDate = startDate.plusDays(8);
        }
        if(startDate.isBefore(endDate) || startDate.equals(endDate)) {
            periodsToFindNeos.add(new Pair(startDate, endDate));
        }
        return periodsToFindNeos;
    }
}
