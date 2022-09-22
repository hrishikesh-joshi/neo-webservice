package assignment.ntc.neo;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

public class DateFinderTest {

    @Test
    void testFindDatesBetweenPeriod() {
        LocalDate startDate = LocalDate.of(2022, 9, 1);
        LocalDate endDate = LocalDate.of(2022, 9, 28);
        System.out.println(Period.between(startDate,endDate).getDays());

        List<Pair> datesToFindNeo = new ArrayList<>(); //TODO:Initialize with correct capacity NoOfDays/7+1

        if(isValidPeriod(startDate, endDate)) {
            while(startDate.plusDays(7).isBefore(endDate) || startDate.plusDays(7).equals(endDate)) {
                datesToFindNeo.add (new Pair(startDate, startDate.plusDays(7)));
                startDate = startDate.plusDays(8);
            }
            if(startDate.isBefore(endDate) || startDate.equals(endDate)) {
                datesToFindNeo.add(new Pair(startDate, endDate));
            }

            System.out.println(datesToFindNeo);
        } else {
            System.err.println("Start date is before end date");
        }
    }

    private boolean isValidPeriod(LocalDate startDate, LocalDate endDate) {
        return !Period.between(startDate, endDate).isNegative();
    }
}
