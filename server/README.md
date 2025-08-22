# Automated Input Deviation Detection & Classification (Electrical Issue Wizard)

A comprehensive solution for automated detection and classification of electrical issues using AI-powered tools and mobile data collection.

## Overview

The Electrical Issue Wizard is a sophisticated system that combines mobile data collection with AI-powered analysis to detect and classify electrical issues. The system processes various input types including speech and images to provide accurate problem identification and summarization.

## Project Documentation

A detailed presentation of the project is available at:
- Local development: http://localhost:8080
- Production: https://hackathon2025api-506638310413.europe-west1.run.app

The presentation contains project overview, architecture, and implementation details.

## Features

### Mobile Client
- Data collection interface
- Results viewer
- Cross-platform support through Kotlin Multiplatform
  - Android (Implemented)
  - iOS (Planned)
  - Desktop (Planned)

### Server (REST API)
- Data storage and management
- AI-powered processing pipeline
- Built with Java Spring Boot
- Hosted on Google Cloud Platform

### AI Capabilities
- Speech-to-text transcription
- Image analysis and detection
- Problem summarization
- Powered by Google Cloud services:
  - [Speech-to-text transcription](https://cloud.google.com/speech-to-text/docs/speech-to-text-client-libraries)
  - Vertex AI for image analysis using [gemini-2.0-flash-001](https://cloud.google.com/vertex-ai/generative-ai/docs/models/gemini/2-0-flash?hl=en) model
  - Vertex AI for problem summarization using [gemini-2.0-flash-001](https://cloud.google.com/vertex-ai/generative-ai/docs/models/gemini/2-0-flash?hl=en) model

## Technical Stack

### Mobile
- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform-intro.html)
- Cross-platform business logic

### Backend
- Java Spring Boot
- RESTful API architecture
- Google Cloud Platform integration

### AI Services
- Google Cloud Speech-to-Text
- Google Cloud Vertex AI
  - Image analysis
  - Text summarization

## Getting Started

### Prerequisites
- Android Studio / Xcode (for mobile development)
- JDK 17 or later
- Google Cloud Platform account
- Required API keys and credentials

### Setup
1. Clone the repository
2. Configure Google Cloud credentials
3. Set up the development environment
4. Install dependencies
5. Run the application

## Development

### Mobile Development
```bash
# Open project in Android Studio
# File -> Open -> Select the DeviationWizard directory
```

### Backend Development
```bash
cd server
```
```bash
./mvnw spring-boot:run
```

### Docker Setup
```bash
# Build the Docker image
docker build -t electrical-issue-wizard .

# Run the container
docker run -p 8080:8080 electrical-issue-wizard
```

### Deployment
The application is deployed on Google Cloud Run:
- Region: europe-west1
- Deployment Type: Container
- Service URL: https://hackathon2025api-506638310413.europe-west1.run.app

### Deployment and Updates
To deploy or update the application on Google Cloud Run, follow these steps:

1. Make sure you have the Google Cloud CLI installed and are authenticated:
```bash
gcloud auth login
```

2. Build and push the Docker image to Google Container Registry:
```bash
gcloud builds submit --tag gcr.io/hackathon2025-458305/hackathon2025api .
```

3. Deploy or update the service on Cloud Run:
```bash
gcloud run deploy hackathon2025api \
  --image gcr.io/hackathon2025-458305/hackathon2025api \
  --platform managed \
  --region europe-west1 \
  --allow-unauthenticated
```

The `--allow-unauthenticated` flag makes the service publicly accessible. Remove it if you want to restrict access through IAM policies.

To verify the deployment:
- Check the service status: `gcloud run services describe hackathon2025api --region europe-west1`
- View logs: `gcloud logging read "resource.type=cloud_run_revision AND resource.labels.service_name=hackathon2025api" --limit 50`

## Project Status

Active development - This project is currently under active development.
