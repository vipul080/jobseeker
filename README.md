# JobSeeker

A job search web app built with Java 17+ and Spring Boot.

## Features
- **Real job listings**: remote jobs imported from [Remotive](https://remotive.com), refreshed every 6 hours.
- **Browse & search jobs**: filter by keyword, location, job type and remote.
- **Job details & apply**: imported jobs link to the original posting; mark them as applied to track them.
  Jobs posted in this app take applications directly (name, email, phone, résumé link, cover letter).
- **Saved jobs**: bookmark listings to revisit later.
- **Application tracker**: move applications through Applied → Interview → Offer / Rejected, with notes.
- **Post a job**: add new listings with salary range validation.

## Job sources
Each outside job board is a `JobSource` (`src/main/java/com/jobseeker/source`). `JobImportService`
fetches every source when it's due, adds and updates listings, and removes ones the board dropped,
unless you saved or applied to them. Descriptions are sanitized with jsoup before display.

| Source | Setting | Notes |
|---|---|---|
| Remotive | `jobs.remotive.enabled` (default `true`), `jobs.remotive.category` (e.g. `software-dev`) | Free public API, remote jobs only, delayed 24h. Their terms ask for at most ~4 requests a day and a link back to each job on Remotive, which the app does. |

To add a board, implement `JobSource` as a Spring `@Component`.

## Tech stack
Spring Boot (Web MVC, Data JPA, Validation, Scheduling) · Thymeleaf · jsoup · H2 database (file-based, stored in `./data`).

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
  source/  JobSource interface, import/sync service, one package per job board
  web/     JobController, ApplicationController
src/main/resources/
  templates/   Thymeleaf pages
  static/      style.css (light + dark mode)
```
