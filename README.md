# Application Manager
A Java Spring Boot application for managing applications throughout their lifecycle.

## Table of Contents
- [Overview](#Overview)
- [Features](#Features)
- [API Endpoints](#API-Endpoints)
- [Exceptions Handling](#Exceptions-Handling)
- [Installation & Running](#Installation-and-Running)
- [Dependencies](#Dependencies)
- [Usage](#Usage)
- [Contribution](#Contribution)

## Overview
The Application Manager allows users to create, verify, accept, reject, publish, and delete applications. The system keeps track of each application's history, and each change to an application will be recorded in the system.

## Features
- __Application CRUD operations:__ Create, Read, Update and Delete applications.
- __Status Management:__ Move applications between different statuses: `CREATED`, `VERIFIED`, `ACCEPTED`, `PUBLISHED`, `REJECTED`, `DELETED`.
- __History Recording:__ Every change in application status is recorded with a timestamp in the application history.
- __Advanced Searching:__ Fetch applications based on name and status with pagination support.

## API Endpoints
- __Create Application: POST /applications__
- __Update Application Content: PUT /applications/{id}__
- __Delete Application: DELETE /applications/{id}__
- __Verify Application: PUT /applications/{id}/verify__
- __Reject Application: PUT /applications/{id}/reject__
- __Accept Application: PUT /applications/{id}/accept__
- __Publish Application: PUT /applications/{id}/publish__
- __Get Applications: GET /applications__

## Exceptions Handling
The system has built-in exception handling for various scenarios:

- __Application not found.__
- __Invalid application status.__
- __Application is already published.__
- __Editing content not allowed based on status.__

## Installation and Running
1. Make sure you have Java JDK and Maven installed on your machine.

2. Clone the repository.

3. Navigate to the project directory and run:

```bash
mvn clean install
```
4. After successful build, run:

```bash
java -jar target/application-manager-<version>.jar
```
Or using Maven:

```bash
mvn spring-boot:run
```
The application will start, and you can access the API endpoints through http://localhost:8080/.

## Dependencies
- __Spring Boot:__ For building stand-alone, production-grade Spring based applications.
- __Spring Data JPA:__ For easy database interactions and operations.
- __H2 Database:__ An in-memory database used for development purposes.

## Usage
Once the application is running, you can use tools like __Postman__ or __curl__ to interact with the API. Ensure to set the __Content-Type__ header to __application/json__ for requests.

## Contribution
Feel free to fork the repository, make your changes, and raise a pull request. All contributions are welcome!

