# Cluedo Game Server

This repository contains the server-side implementation of a Cluedo game using WebSocket communication in Java Spring Boot. The application uses the STOMP protocol over WebSockets to enable real-time communication between the server and clients.

## Project Structure

The project follows a clean architecture with clear separation of concerns:

- **Controllers**: Handle incoming WebSocket messages and route them to appropriate services
- **Services**: Contain the business logic for game operations
- **Models**: Define the data structures used in the application
  - **gameobjects**: Player, cards, and other game entities
  - **gameboard**: Game board implementation with cells and rooms
  - **gamemanager**: Core game logic and state management
  - **lobby**: Lobby management for pre-game setup
- **DTOs**: Data Transfer Objects for communication between client and server
- **Config**: Configuration classes for WebSocket and other services

## Features

### Player Model

The application uses a rich Player model that includes:

- Player name and character
- Unique player ID (UUID)
- Position coordinates (x, y)
- Game state (active, current player, has won)
- Cards held by the player

### Lobby Management

The application provides functionality for managing game lobbies:

- **Create Lobby**: Players can create a new lobby and become the host
- **Join Lobby**: Players can join an existing lobby using a lobby ID
- **Leave Lobby**: Players can leave a lobby they have joined

### Game Mechanics

The game implements the core mechanics of Cluedo:

- **Game Board**: A detailed game board with rooms, hallways, doors, and secret passages
- **Player Movement**: Players can move around the board using directional controls (W/A/S/D)
- **Dice Rolling**: Movement is determined by dice rolls
- **Room Actions**: Players can perform special actions when in rooms
- **Suggestions**: Players can make suggestions about the crime when in a room
- **Accusations**: Players can make final accusations to try to solve the crime
- **Card System**: Cards are distributed to players, with a secret file containing the solution
- **Win/Lose Conditions**: Players win by making correct accusations or lose by making incorrect ones

### Game Flow

The game follows a structured flow from lobby creation to game completion:

1. **Lobby Creation**: A player creates a lobby and becomes the host
2. **Player Joining**: Other players join the lobby using the lobby ID
3. **Game Start**: Once at least 3 players have joined, the host can start the game
4. **Game Initialization**:
   - Players are positioned on the board at their starting positions
   - Cards are distributed to players
   - A secret file is created with the solution
5. **Game Play**: Players take turns rolling dice, moving, and performing actions
6. **Game End**: The game ends when a player makes a correct accusation or when only one player remains active

## WebSocket Communication

The application uses Spring's WebSocket support with STOMP messaging protocol:

- **Endpoint**: `/ws` - The main WebSocket connection endpoint
- **Application Destination Prefix**: `/app` - Prefix for client-to-server messages
- **Topic Destination Prefix**: `/topic` - Prefix for server-to-client messages

### Message Endpoints

#### Lobby Endpoints

| Endpoint | Description |
|----------|-------------|
| `/app/createLobby` | Create a new lobby |
| `/app/joinLobby/{lobbyId}` | Join an existing lobby |
| `/app/leaveLobby/{lobbyId}` | Leave a lobby |
| `/app/getActiveLobbies` | Get a list of all active lobbies |
| `/app/canStartGame/{lobbyId}` | Check if a lobby has enough players to start a game |

#### Game Endpoints

| Endpoint | Description |
|----------|-------------|
| `/app/startGame/{lobbyId}` | Start a game from a lobby (minimum 3 players required) |

### Subscription Topics

#### Lobby Topics

| Topic | Description |
|-------|-------------|
| `/topic/lobbyCreated` | Receive notifications when a new lobby is created |
| `/topic/lobby/{lobbyId}` | Receive updates about a specific lobby |
| `/topic/activeLobbies` | Receive list of all active lobbies |
| `/topic/canStartGame/{lobbyId}` | Receive information about whether a lobby can start a game |

#### Game Topics

| Topic | Description |
|-------|-------------|
| `/topic/gameStarted/{lobbyId}` | Receive notification when a game has started from a lobby |

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

### Lobby Operations

#### Create Lobby

**Request:**
```json
{
  "player": {
    "name": "playerName",
    "character": "Red",
    "playerID": "550e8400-e29b-41d4-a716-446655440000"
  }
}
```

**Response:**
A string containing the lobby ID.

#### Join Lobby

**Request:**
```json
{
  "player": {
    "name": "playerName",
    "character": "Blue",
    "playerID": "550e8400-e29b-41d4-a716-446655440001"
  }
}
```

**Response:**
```json
{
  "id": "lobbyId",
  "host": {
    "name": "hostName",
    "character": "Red",
    "playerID": "550e8400-e29b-41d4-a716-446655440000",
    "x": 0,
    "y": 0,
    "isCurrentPlayer": false,
    "isActive": true,
    "hasWon": false
  },
  "players": [
    {
      "name": "hostName",
      "character": "Red",
      "playerID": "550e8400-e29b-41d4-a716-446655440000",
      "x": 0,
      "y": 0,
      "isCurrentPlayer": false,
      "isActive": true,
      "hasWon": false
    },
    {
      "name": "playerName",
      "character": "Blue",
      "playerID": "550e8400-e29b-41d4-a716-446655440001",
      "x": 0,
      "y": 0,
      "isCurrentPlayer": false,
      "isActive": true,
      "hasWon": false
    }
  ]
}
```

#### Leave Lobby

**Request:**
```json
{
  "player": {
    "name": "playerName",
    "character": "Blue",
    "playerID": "550e8400-e29b-41d4-a716-446655440001"
  }
}
```

**Response:**
```json
{
  "id": "lobbyId",
  "host": {
    "name": "hostName",
    "character": "Red",
    "playerID": "550e8400-e29b-41d4-a716-446655440000",
    "x": 0,
    "y": 0,
    "isCurrentPlayer": false,
    "isActive": true,
    "hasWon": false
  },
  "players": [
    {
      "name": "hostName",
      "character": "Red",
      "playerID": "550e8400-e29b-41d4-a716-446655440000",
      "x": 0,
      "y": 0,
      "isCurrentPlayer": false,
      "isActive": true,
      "hasWon": false
    }
  ]
}
```

#### Get Active Lobbies

**Request:**
```json
{}
```

**Response:**
```json
{
  "lobbies": [
    {
      "id": "lobby-id-1",
      "hostName": "hostName1",
      "playerCount": 2
    },
    {
      "id": "lobby-id-2",
      "hostName": "hostName2",
      "playerCount": 1
    }
  ]
}
```

#### Check If Lobby Can Start Game

**Request:**
No request body needed, the lobby ID is included in the URL.

**Response:**
```json
{
  "canStart": true
}
```

### Game Operations

#### Start Game

**Request:**
```json
{
  "player": {
    "name": "hostName",
    "character": "Red",
    "playerID": "550e8400-e29b-41d4-a716-446655440000"
  }
}
```

**Response:**
```json
{
  "lobbyId": "lobby-id-1",
  "players": [
    {
      "name": "hostName",
      "character": "Red",
      "playerID": "550e8400-e29b-41d4-a716-446655440000",
      "x": 7,
      "y": 24,
      "isCurrentPlayer": true,
      "isActive": true,
      "hasWon": false,
      "color": "RED"
    },
    {
      "name": "playerName",
      "character": "Blue",
      "playerID": "550e8400-e29b-41d4-a716-446655440001",
      "x": 0,
      "y": 17,
      "isCurrentPlayer": false,
      "isActive": true,
      "hasWon": false,
      "color": "BLUE"
    },
    {
      "name": "playerName2",
      "character": "Green",
      "playerID": "550e8400-e29b-41d4-a716-446655440002",
      "x": 9,
      "y": 0,
      "isCurrentPlayer": false,
      "isActive": true,
      "hasWon": false,
      "color": "GREEN"
    }
  ]
}
```
