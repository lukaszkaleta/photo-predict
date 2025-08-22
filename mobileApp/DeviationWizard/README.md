# DeviationWizard

A mobile application for capturing and analyzing deviations in electrical installations.

## Repository

This project is part of the Cursor Hackathon 2025 and is hosted on GitLab:

- SSH: `git@gitlab.hantverksdata.se:integrator/hackathon2025.git`
- HTTPS: `https://gitlab.hantverksdata.se/integrator/hackathon2025.git`

## Overview

DeviationWizard is a cross-platform mobile application built with Kotlin Multiplatform Mobile (KMM) that enables users to:
- Capture photos of electrical installation deviations
- Record audio descriptions of electrical issues
- Create detailed deviation reports for electrical systems
- View and manage electrical deviation history

## Project Structure

```
DeviationWizard/
├── androidApp/     # Android application
└── shared/         # Shared Kotlin code
```

## API Documentation

### Endpoints

#### 1. Create Deviation
```http
POST /api/deviation
Content-Type: application/json
```
Request:
```json
{
  "images": ["base64_encoded_image1", "base64_encoded_image2"],
  "recordings": ["base64_encoded_recording1"],
  "comment": "Description of the electrical deviation"
}
```
Response:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2024-03-14T12:00:00",
  "images": ["img_123456", "img_789012"],
  "recordings": ["rec_345678"],
  "comment": "Description of the electrical deviation"
}
```

#### 2. Get All Deviations
```http
GET /api/deviations
```
Returns a list of all deviations, sorted by timestamp (newest first):
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "timestamp": "2024-03-14T12:00:00",
    "images": ["img_123456", "img_789012"],
    "recordings": ["rec_345678"],
    "comment": "Description of the electrical deviation"
  }
]
```

#### 3. Get Specific Deviation
```http
GET /api/deviations/{id}
```
Returns a specific deviation by ID:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2024-03-14T12:00:00",
  "images": ["img_123456", "img_789012"],
  "recordings": ["rec_345678"],
  "comment": "Description of the electrical deviation"
}
```

#### 4. Delete Deviation
```http
DELETE /api/deviations/{id}
```
Deletes a specific deviation by ID. Returns 204 No Content on success.

#### 5. Get Deviation Analysis
```http
GET /api/deviations/{id}/analysis
```
Returns the analysis data for a specific deviation:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "status": "completed",
  "analysis": {
    "transcriptions": {
      "rec_345678": "Found exposed wiring in the main panel. Insulation appears damaged."
    },
    "images": {
      "img_123456": "Exposed wiring in main panel",
      "img_789012": "Damaged insulation on power cable"
    },
    "solution": {
      "issueType": "Electrical Safety Hazard",
      "summary": "Exposed wiring and damaged insulation in main panel",
      "priorityLevel": "High",
      "repairEffortHours": "2",
      "checkList": [
        "Turn off main power supply",
        "Replace damaged insulation",
        "Secure exposed wiring",
        "Test circuit after repair"
      ]
    }
  }
}
```

**Note:** Analysis data is not immediately available after creating a deviation. The system needs time to process the images and recordings. The analysis endpoint will return a status of "processing" until the analysis is complete.

### API Configuration

The application uses different BASE_URL values depending on the environment:

- Development: `http://10.0.2.2:8080` (Android Emulator)
- Production: `https://hackathon2025api-506638310413.europe-west1.run.app`

For local development with Android Emulator:
- Use `10.0.2.2` to access the host machine's localhost
- The port `8080` is used for the development server

## Development Setup

### Prerequisites
- Android Studio
- JDK 17 or later
- Android SDK
- Gradle 8.0 or later

### Building the Project

1. Clone the repository:
```bash
# Using HTTPS
git clone https://gitlab.hantverksdata.se/integrator/hackathon2025.git
cd hackathon2025/mobileApp/DeviationWizard

# Or using SSH
git clone git@gitlab.hantverksdata.se:integrator/hackathon2025.git
cd hackathon2025/mobileApp/DeviationWizard
```

2. Build the shared module:
```bash
./gradlew :shared:build
```

3. Build the Android app:
```bash
./gradlew :androidApp:build
```

### Running the App

1. Start an Android emulator
2. Install and run the app:
```bash
./gradlew :androidApp:installDebug
```