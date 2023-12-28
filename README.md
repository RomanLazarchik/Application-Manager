# Application Manager
A Java Spring Boot application for managing applications throughout their lifecycle.

## Table of Contents
- [Overview](#Overview)
- [Features](#Features)
- [API Endpoints](#API-Endpoints)
- [Exceptions Handling](#Exceptions-Handling)
- [Installation & Running](#Installation-and-Running)
- [Usage](#Usage)
- [Contribution](#Contribution)

## Overview
The Application Manager allows users to perform various operations on applications, such as creating, updating, deleting, verifying, rejecting, accepting, and publishing. The system keeps a detailed history of each application, recording changes to its status and content.
## Features
- __Application CRUD operations:__ Create, Read, Update and Delete applications.
- __Status Management:__ Move applications between different statuses: `CREATED`, `VERIFIED`, `ACCEPTED`, `PUBLISHED`, `REJECTED`, `DELETED`.
- __History Recording:__ Every change in application status is recorded with a timestamp in the application history.
- __Advanced Searching:__ Fetch applications based on name and status with pagination support.

## API Endpoints
- **Create Application: POST /applications**
    - Request Body: ApplicationDTO (name, content)
- **Update Application Content: PUT /applications/{id}**
    - Request Body: UpdateContentDTO (content)
- **Delete Application: DELETE /applications/{id}**
    - Request Body: DeleteDTO (reason)
- **Verify Application: PUT /applications/{id}/verify**
- **Reject Application: PUT /applications/{id}/reject**
    - Request Body: RejectDTO (reason)
- **Accept Application: PUT /applications/{id}/accept**
- **Publish Application: PUT /applications/{id}/publish**
- **Get Applications: GET /applications**
    - Query Parameters: name (optional), status (optional), page (optional), size (optional)
  
## Exceptions Handling
The system provides built-in exception handling for various scenarios:

- __Application not found.__
- __Invalid application status.__
- __Application already published.__
- __Editing content not allowed based on status.__
- __Invalid input__

## Installation and Running
1. Make sure you have Java JDK and Maven installed on your machine.

2. Clone the repository.

3. Navigate to the project directory and run:

```bash
mvn clean install
```
4. After successful build, run:

```bash
java -jar target/ApplicationManager-0.0.1-SNAPSHOT.jar
```
Or using Maven:

```bash
mvn spring-boot:run
```
The application will start, and you can access the API endpoints through http://localhost:8080/.

## Usage
Once the application is running, you can use tools like __Postman__ or __curl__ to interact with the API. Ensure to set the __Content-Type__ header to __application/json__ for requests.

## Contribution
Feel free to fork the repository, make your changes, and raise a pull request. All contributions are welcome!

## Future Plans
- Implement JWT-based authentication for secure access.
- Explore the adoption of the "State" design pattern for enhanced application state management.
