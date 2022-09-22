package assignment.ntc.neo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;

record NearEarthObject(
        String id,
        String name,
        @JsonProperty("kilometers")
        BigDecimal missDistanceInKm,
        @JsonProperty("2015-09-08")
        LocalDate closeApproachDate) {
}