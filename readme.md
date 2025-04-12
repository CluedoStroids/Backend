# Cluedo Game Server

This repository contains the server-side implementation of a Cluedo game using WebSocket communication in Java Spring Boot. The application uses the STOMP protocol over WebSockets to enable real-time communication between the server and clients.

## Project Structure

The project follows a clean architecture with clear separation of concerns:

- **Controllers**: Handle incoming WebSocket messages and route them to appropriate services
- **Services**: Contain the business logic for game operations
- **Models**: Define the data structures used in the application
- **DTOs**: Data Transfer Objects for communication between client and server

## Features

### Lobby Management

The application provides functionality for managing game lobbies:

- **Create Lobby**: Players can create a new lobby and become the host
- **Join Lobby**: Players can join an existing lobby using a lobby ID
- **Leave Lobby**: Players can leave a lobby they have joined

## WebSocket Communication

The application uses Spring's WebSocket support with STOMP messaging protocol:

- **Endpoint**: `/ws` - The main WebSocket connection endpoint
- **Application Destination Prefix**: `/app` - Prefix for client-to-server messages
- **Topic Destination Prefix**: `/topic` - Prefix for server-to-client messages

### Message Endpoints

| Endpoint | Description |
|----------|-------------|
| `/app/createLobby` | Create a new lobby |
| `/app/joinLobby/{lobbyId}` | Join an existing lobby |
| `/app/leaveLobby/{lobbyId}` | Leave a lobby |

### Subscription Topics

| Topic | Description |
|-------|-------------|
| `/topic/lobbyCreated` | Receive notifications when a new lobby is created |
| `/topic/lobby/{lobbyId}` | Receive updates about a specific lobby |

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven

### Running the Application

1. Clone the repository
2. Navigate to the project directory
3. Run the application using Maven:

```bash
./mvnw spring-boot:run
```

The server will start on port 8080 by default.

## Testing

The project includes both unit tests and integration tests:

- **Unit Tests**: Test individual components in isolation
- **Integration Tests**: Test the interaction between components

To run the tests, use the following command:

```bash
./mvnw test
```

## API Documentation

### Create Lobby

**Request:**
```json
{
  "username": "playerName"
}
```

**Response:**
A string containing the lobby ID.

### Join Lobby

**Request:**
```json
{
  "username": "playerName"
}
```

**Response:**
```json
{
  "id": "lobbyId",
  "host": "hostName",
  "participants": ["hostName", "playerName"]
}
```

### Leave Lobby

**Request:**
```json
{
  "username": "playerName"
}
```

**Response:**
```json
{
  "id": "lobbyId",
  "host": "hostName",
  "participants": ["hostName"]
}
```
