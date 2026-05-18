# RoadAlert: Interactive Map with Live Road Condition Reporting

![Language](https://img.shields.io/badge/language-Java%2FJavaScript-blue.svg)
![Framework](https://img.shields.io/badge/framework-Spring%20Boot-green.svg)
![Frontend](https://img.shields.io/badge/frontend-React-61DAFB.svg)
![License](https://img.shields.io/badge/license-MIT-lightgrey.svg)

**RoadAlert** is a real-time, interactive map web application that combines standard navigation with crowd-sourced road hazard reporting. Users can calculate routes, report road conditions, and receive proximity alerts about nearby hazards — all powered by community voting and automated reliability systems.

***

## Key Features

* **Interactive Live Map:** Users can zoom and pan across a real map to view active road hazard reports with intuitive color-coded markers — potholes, accidents, speed cameras, and traffic jams.
* **Standard Navigation:** Users input a starting point and destination to calculate the shortest or fastest route, with multiple alternatives displayed and the best option clearly marked.
* **Live Hazard Reporting:** While viewing the map, users can report a road event at their current GPS coordinates across categories: Damaged Road / Potholes, Accidents / Crashes, Speed Cameras / Police, and Heavy Traffic.
* **Proximity Alerts:** The system continuously fetches active reports within a 5km radius of the user's location and displays real-time notifications for newly detected hazards.
* **Community Voting System:** Users can upvote or downvote any report. If a report accumulates more than 3x downvotes vs upvotes with at least 10 total votes, it is automatically removed.
* **Speed Camera Database:** A dedicated database of speed camera locations populated from OpenStreetMap data, displayed on the map with warning radius circles.
* **User Reputation System:** Users gain and lose reputation points based on the accuracy and reliability of their reports, earning a "Trusted Reporter" badge at high scores.
* **Automatic Ban System:** Users whose reports are repeatedly rejected with bad downvote ratios receive a temporary 1-week reporting ban.

***

## Technical Architecture

### 1. Backend & Data Management
The core logic is driven by a **Java (Spring Boot)** backend, managing REST APIs, scheduled tasks, and database connections while strictly adhering to Object-Oriented Programming (OOP) principles. Data persistence is handled by **MySQL**, storing user accounts, road reports, votes, comments, speed cameras, and reputation scores. **Hibernate** manages all ORM operations and **Lombok** eliminates boilerplate code.

### 2. Frontend & Interactive Mapping
The user interface is built with **React and JavaScript**, powered by **Leaflet** for interactive map rendering. Routes are calculated via the **OpenRouteService API** and traffic overlays are provided by the **HERE Maps Traffic API**.

### 3. Security
Authentication is handled by **Spring Security** with **JWT tokens** and **BCrypt** password hashing. All report submission, voting, and comment endpoints are protected and require authentication.

### 4. Automated Tasks
**Spring Scheduler** runs background tasks every 10 minutes to expire time-limited reports (traffic: 2h, accidents: 6h), and every 24 hours to send re-verification prompts for permanent reports (potholes, cameras) not confirmed in 30+ days.

***

## Auto-Logic Rules

| Rule | Condition | Action |
|---|---|---|
| Auto-remove report | Downvotes > Upvotes×3 AND total votes ≥ 10 | Set status = REMOVED |
| Ban user | 3+ reports removed with bad ratio | Ban for 7 days |
| Promote to permanent | Upvotes ≥ 10, ratio > 60% positive | Set isPermanent = true |
| Expire traffic report | Created > 2 hours ago | Set status = EXPIRED |
| Re-verify permanent item | Not verified in 30 days | Ask nearby users |
| Remove unverified item | Majority "No" votes within 7 days | Delete from DB |

***

## Usage Options

* **View Hazards:** Open the map to instantly see all active road reports near you.
* **Get a Route:** Enter origin and destination to calculate a route with a hazard summary overlay.
* **Report a Hazard:** Tap the "+" button to report a road condition at your current GPS location.
* **Vote on Reports:** Upvote or downvote existing reports to confirm or deny their accuracy.
* **Check Speed Cameras:** View the camera widget to see all verified cameras within your radius.

***

## License
MIT License. Free for educational and research use.

***

## Running the Project

### Prerequisites
- Node.js (for the frontend)
- Java 17+ (for the Spring Boot server)
- MySQL Database

### Environment Setup
Navigate to the `server` directory, copy `.env.example` to `.env` and fill in your local MySQL credentials:
```bash
cp .env.example .env
```

### Frontend
Navigate to the `web` directory to install dependencies and start the Vite development server:
```bash
cd web
npm install
npm run dev
```

### Backend
Navigate to the `server` directory and run the Spring Boot application:
```bash
cd server
./mvnw spring-boot:run
```

The backend runs on `http://localhost:8080` and the frontend on `http://localhost:5173`.

***

## Team
- Giorgi Ezugbaia
- Luka Tasoshvili
- Nikoloz Bendianishvili
- Luka Tsitishvili
- Nikoloz Shubitidze
