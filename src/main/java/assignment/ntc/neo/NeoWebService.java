package assignment.ntc.neo;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.util.LoggerNameUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.util.Loggers;
import reactor.util.annotation.NonNull;

import java.time.LocalDate;
import java.util.List;


@RestController()
@RequestMapping("/find-neo")
public class NeoWebService {

    @Autowired
    private NasaWebClient nasaWebClient;

    @GetMapping("/hello")
    public String hello() {
        return "Hello";
    }

    @GetMapping("/closest")
    public Mono<List<NearEarthObject>> getClosestNearEarthObject(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") @NonNull LocalDate startDate, @RequestParam @NonNull LocalDate endDate) {
        System.out.println("getClosestNearEarthObject "+startDate+","+endDate);
        return nasaWebClient.findNearEarthObjects(startDate, endDate).log(System.Logger.Level.INFO.name());
    }
}
