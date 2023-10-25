# employee-api 
This is the employee API responsible for employee management.

### Summary
This API has endpoints to get an employee by id, list with pagination and sorting, create, update, and delete.
It uses PostgreSQL as a database, Flyway for migrations, RabbitMQ for event publishing, JWT tokens for security on endpoints that modify data, and Swagger-UI to make testing easier.

### Requirements
Java 17, Docker, IDE

### Running the application
1. Clone the project and open it in your preferred IDE.
2. Make sure all the Maven dependencies are loaded.
3. Make sure Docker is up and running.
4. Run the following Docker commands to start the PostgreSQL and RabbitMQ containers:

```sh
docker run -d -ti -v pgdata:/var/lib/postgresql15/data -p 5432:5432 -e POSTGRES_PASSWORD=tester postgres:15
```
```sh
docker run -d -ti -p 5672:5672 -p 15672:15672 -e RABBITMQ_DEFAULT_USER=guest -e RABBITMQ_DEFAULT_PASS=guest rabbitmq:3.8-management-alpine
```

After the application starts, you will be able to  see the Swagger documentation at the following URL:
http://localhost:8080/employee-api/swagger-ui/index.html

To create, update, or delete an employee, you will need a Bearer token. You can generate a token at the following URL:
http://localhost:8080/employee-api/swagger-ui/index.html#/auth-controller/doLogin

Use the following credentials:
* Email: alfred@jloa.com
* Password: 123456

Another way to run, is to run the application in development mode, execute the EmployeeApiApplication class in the test package.
This will start the application and the necessary containers. However, when you stop the application, the containers will be destroyed, and you will lose your data.

### Stack
* Java 17
* Spring Boot 3
* PostgreSQL 15
* Flyway
* RabbitMQ 3.8
* Java JWT
* Maven
