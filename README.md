# JobSeeker

A job search web app built with Java 17+ and Spring Boot.

## Features
- **Browse & search jobs**: filter by keyword, location, job type and remote.
- **Job details & apply**: apply with name, email, phone, résumé link and cover letter.
- **Saved jobs**: bookmark listings to revisit later.
- **Application tracker**: move applications through Applied → Interview → Offer / Rejected, with notes.
- **Post a job**: add new listings with salary range validation.

## Tech stack
Spring Boot (Web MVC, Data JPA, Validation) · Thymeleaf · H2 database (file-based, stored in `./data`).

## Run it
Requires Java 17+. No Maven install needed; the wrapper downloads it.

```bash
./mvnw spring-boot:run
```

Then open http://localhost:8080. The H2 console is at http://localhost:8080/h2-console
(JDBC URL `jdbc:h2:file:./data/jobseeker`, user `sa`, empty password).

## Project layout
```
src/main/java/com/jobseeker/
  model/   Job, Application, JobType, ApplicationStatus
  repo/    Spring Data repositories (search query lives in JobRepository)
  web/     JobController, ApplicationController
  SampleData.java   seeds demo listings on first run
src/main/resources/
  templates/   Thymeleaf pages
  static/      style.css (light + dark mode)
```
