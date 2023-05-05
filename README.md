# Java-Shareit

The project provides users with firstly the possibility to tell what things they are willing to share, and secondly to find the thing they want and rent it out for a while. 

The service allows you not only to reserve an item for certain dates, but also to block off access to it from other people for the duration of the reservation. Users have the ability to leave requests in case the item you need is not available at the service. New items can be added on request.

## Technologies Used

- Spring Boot
- Spring Web MVC
- Maven
- PosgreSQL
- Spring Data JPA
- Hibernate ORM
- JUnit
- Mockito

## Deployment guide

### Requirements

- Java Development Kit (JDK) 11 or later
- PostgreSQL

### Building the Project

1. Clone the repository:

   `git clone https://github.com/living221/java-shareit.git`

2. Build the project using Maven:

   `./mvnw clean install`
   
3. Run the application using the following command from CLI:

   `./mvnw spring-boot:run`
 
The application will run on port 8080. Access the public API at `http://localhost:8080`.


