# Energy Mix API

This is a Spring Boot application that provides UK data on energy mix and helps find the optimal time for charging based on the cleanliness of the energy sources. The application fetches data from the [Carbon Intensity API](https://api.carbonintensity.org.uk/).

## Technologies Used

- **Java 21**: The core programming language.
- **Spring Boot 3**: The framework for building the application.
- **Maven**: The build and dependency management tool.
- **Lombok**: A library to reduce boilerplate code.

## Getting Started

### Prerequisites

- Java 21 or higher
- Maven

## API Endpoints

The API is available under the `/energy` path.

### 1. Get 3-Day Energy Mix

-   **Endpoint:** `GET /energy/mix`
-   **Description:** Returns the average energy mix for the current day and the next two days. The mix is calculated based on the percentage of different fuel types.
-   **Success Response (200 OK):**
    ```json
    [
      {
        "date": "2024-07-29",
        "mix": {
          "gas": 35.5,
          "coal": 2.1,
          "biomass": 5.8,
          "nuclear": 15.6,
          "hydro": 1.2,
          "imports": 8.9,
          "other": 0.1,
          "solar": 10.4,
          "wind": 20.4
        },
        "cleanEnergyPercentage": 52.6
      },
      ...
    ]
    ```

### 2. Get Optimal Charging Window

-   **Endpoint:** `GET /energy/optimal-charging`
-   **Description:** Calculates the best time window to charge a device based on the highest percentage of clean energy sources in the upcoming 48 hours.
-   **Query Parameters:**
    -   `durationHours` (optional, integer): The desired charging duration in hours.
        -   `Min`: 1
        -   `Max`: 6
        -   `Default`: 3
    ```json
    {
      "from": "2024-07-30T14:00:00Z",
      "to": "2024-07-30T17:00:00Z",
      "averageCleanliness": 85.5
    }
    ```