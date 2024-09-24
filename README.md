# Ticket Service A simple ticket service implemented using Spring Boot, which facilitates the discovery, temporary hold, and final reservation of seats in a high-demand performance venue. 
## Features - Check available seats by venue level. - Hold seats temporarily. - Reserve held seats. 
## Setup and Running 
### Prerequisites - Java 17 - Maven 
### H2 In Memory DB used for providing venue levels data and seats available
### Seats under Hold are added to concurrenthashmap
### Three REST APIs provided
### Build the Application  
### mvn clean compile
###  mvn spring-boot:run



