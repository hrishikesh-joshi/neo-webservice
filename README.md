
# neo-webservice

This is a spring boot based web service using spring-webflux which provides a web api to find closest Near Earth Objects in a given period.
This service make calls to NASA webservice called Asteroids - NeoWs, more details can be found at  https://api.nasa.gov/.   
NASA api needs an api key for authentication which is configured in application.properties. 
NASA API URL https://api.nasa.gov/neo/rest/v1/feed is also configured in application.properties. 

### Usage
Endpoint: http://localhost:8080/find-neo/closest?startDate=yyyy-MM-dd01&endDate=yyyy-MM-dd   
example: http://localhost:8080/find-neo/closest?startDate=2022-09-01&endDate=2022-09-30  
This api returns List[NearEarthObject]  
> record NearEarthObject(  
        String id,  
        String name,  
        @JsonProperty("kilometers")  
        BigDecimal missDistanceInKm,  
        @JsonProperty("2015-09-08")  
        LocalDate closeApproachDate  
    ) { }  

### Build
This project uses gradle to build
./gradlew clean build bootRun to build and run the project from project root dir

### TODO
Error cases for calling NASA API are not currently handled. 
Also, there is a request limit of 1000 calls per hour to NASA API which is not explicitly handled at present. 
OpenAPI documentation is to be added. 
