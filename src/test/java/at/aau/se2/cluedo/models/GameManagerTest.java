package at.aau.se2.cluedo.models;

import at.aau.se2.cluedo.models.cards.BasicCard;
import at.aau.se2.cluedo.models.cards.CardType;
import at.aau.se2.cluedo.models.gamemanager.GameManager;
import at.aau.se2.cluedo.models.gamemanager.GameState;
import at.aau.se2.cluedo.models.gameobjects.Player;
import at.aau.se2.cluedo.models.gameobjects.PlayerColor;
import at.aau.se2.cluedo.models.gameobjects.SecretFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class GameManagerTest {

    private GameManager gameManager;
    Player player1;

    @BeforeEach
    void setUp() {
        player1 = new Player("John","Mr. Green",0,0, PlayerColor.GREEN);
        Player player2 = new Player("Bob","Mrs. White",0,0, PlayerColor.WHITE);
        Player player3 = new Player("Foo","Colonel Mustard",0,0, PlayerColor.YELLOW);
        gameManager = new GameManager(List.of(player1, player2, player3));

        System.out.println(gameManager.getPlayers());
        System.out.println();



        ArrayList<BasicCard> characters = new ArrayList<>();
        characters.add(new BasicCard("Red", UUID.randomUUID(), CardType.CHARACTER));
        characters.add(new BasicCard("White", UUID.randomUUID(),  CardType.CHARACTER));

        gameManager.initilizeGame();
    }
    @Test
    void testConstructorWithPlayerCount() {
        GameManager gm = new GameManager(3);
        assertEquals(3, gm.getPlayers().size());
        assertEquals(0, gm.getCurrentPlayerIndex());
        assertTrue(gm.getPlayers().get(0).isCurrentPlayer());
    }
    @Test
    void testGetPlayer(){
        setUp();
        assertEquals(player1,gameManager.getPlayer(player1.getName()));
    }
    @Test
    void testGetNullPlayer(){
        setUp();
        assertNull(gameManager.getPlayer("House"));
    }

    @Test
    void testConstructorWithLobbyPlayers() {
        List<Player> lobbyPlayers = List.of(
                new Player("Test1", "Alias1", 0, 0, PlayerColor.RED),
                new Player("Test2", "Alias2", 0, 0, PlayerColor.YELLOW)
        );

        GameManager gm = new GameManager(lobbyPlayers);
        assertEquals(2, gm.getPlayers().size());
    }
    @Test
    void testGenerateCards() {
        GameManager gm = new GameManager(3);
        assertFalse(gm.getCards().isEmpty());
    }


    @Test
    void testStartGameDoesNotCrash() {
        GameManager gm = new GameManager(2);
        assertDoesNotThrow(gm::initilizeGame);
    }

    @Test
    void testNextPlayerWrapsAround() {
        GameManager gm = new GameManager(2);
        gm.nextTurn();
        assertEquals(1, gm.getCurrentPlayerIndex());
    }

    @Test
    void testCorrectAccusation() {
        GameManager gm = new GameManager(2);
        SecretFile correct = gm.getSecretFile();
        Player p = gm.getPlayers().get(0);
        assertTrue(gm.makeAccusation(p, correct));
    }

    @Test
    void testIncorrectAccusation() {
        GameManager gm = new GameManager(2);
        Player p = gm.getPlayers().get(0);

        BasicCard wrongRoom = new BasicCard("WrongRoom", UUID.randomUUID(), CardType.ROOM);
        BasicCard wrongWeapon = new BasicCard("WrongWeapon", UUID.randomUUID(), CardType.WEAPON);
        BasicCard wrongCharacter = new BasicCard("WrongChar", UUID.randomUUID(), CardType.CHARACTER);

        SecretFile wrong = new SecretFile(wrongRoom, wrongWeapon, wrongCharacter);
        assertFalse(gm.makeAccusation(p, wrong));
    }

    @Test
    void testInvalidMovementBlockedByPlayer() {
        GameManager gm = new GameManager(2);
        gm.setDiceRollS(1);
        Player p1 = gm.getPlayers().get(0);
        Player p2 = gm.getPlayers().get(1);
        p2.move(p1.getX(), p1.getY() + 1);
        int result = gm.performMovement(p1, List.of("S"));
        assertEquals(0, result); // Blocked
    }
    @Test
    void testGameEndByWinning() {
        GameManager gm = new GameManager(2);
        Player p = gm.getPlayers().get(0);
        p.setHasWon(true);
        assertTrue(gm.checkGameEnd());
    }

    @Test
    void testGameEndByOnlyOneActivePlayer() {
        GameManager gm = new GameManager(2);
        gm.getPlayers().get(1).setActive(false);
        assertTrue(gm.checkGameEnd());
    }
    @Test
    void testRollDiceRange() {
        GameManager gm = new GameManager(2);
        for (int i = 0; i < 100; i++) {
            int roll = gm.rollDice();
            assertTrue(roll >= 1 && roll <= 6);
        }
    }
    @Test
    void testGameInitialization() {
        assertEquals(GameState.INITIALIZED, gameManager.getState());
        assertNotNull(gameManager.getSecretFile());
        assertEquals(0, gameManager.getCurrentPlayerIndex());
        assertTrue(gameManager.getPlayers().get(0).isCurrentPlayer());
    }

    @Test
    void testCardDistribution() {
        int totalCards = 0;
        for (Player player : gameManager.getPlayers()) {
            totalCards += player.getCards().size();
        }
        assertEquals(36, totalCards);
    }

    @Test
    void testInitializeGameGeneratesSecretFile() {
        SecretFile secret = gameManager.getSecretFile();
        assertNotNull(secret);
        assertNotNull(secret.room());
        assertNotNull(secret.weapon());
        assertNotNull(secret.character());
    }

    @Test
    void testPlayerMovement(){
        Player p = gameManager.getCurrentPlayer();
        gameManager.performMovement(p, List.of("W","A","A"));
        assertEquals(14,p.getX());
        assertEquals(0,p.getY());
    }


    @Test
    void testAddAndGetPlayers() {
        Player player = new Player("Markus","Markus",2,5, PlayerColor.GREEN);
        assertEquals(3, gameManager.getPlayers().size());
        assertEquals(0, gameManager.getCurrentPlayerIndex());
        assertTrue(gameManager.getPlayers().get(0).isCurrentPlayer());
    }

    @Test
    void testRollDiceWithinRangeMultipleTimes() {
        for (int i = 0; i < 50; i++) {
            int roll = gameManager.rollDice();
            assertTrue(roll >= 1 && roll <= 6);
        }
    }

    @Test
    void testInRoomFalse(){
        setUp();
        assertFalse(gameManager.inRoom(player1));
    }

    @Test
    void testNextTurnAndTopOfRound() {
        gameManager.nextTurn();
        assertEquals("Bob", gameManager.getPlayers().get(gameManager.getCurrentPlayerIndex()).getName());

        gameManager.nextTurn();
        gameManager.nextTurn();
        gameManager.nextTurn();
        gameManager.nextTurn();
        gameManager.nextTurn();
        gameManager.nextTurn();
        assertEquals("Bob", gameManager.getPlayers().get(gameManager.getCurrentPlayerIndex()).getName());
    }

    @Test
    void testNextTurn() {
        assertEquals(0, gameManager.getCurrentPlayerIndex());
        assertTrue(gameManager.getPlayers().get(0).isCurrentPlayer());
        assertFalse(gameManager.getPlayers().get(1).isCurrentPlayer());

        gameManager.nextTurn();
        assertEquals(1, gameManager.getCurrentPlayerIndex());
        assertFalse(gameManager.getPlayers().get(0).isCurrentPlayer());
        assertTrue(gameManager.getPlayers().get(1).isCurrentPlayer());

        gameManager.getPlayers().get(2).setActive(false);
        gameManager.nextTurn();
        assertEquals(0, gameManager.getCurrentPlayerIndex());
    }

    @Test
    void testSecretFileIsProperlyGenerated() {
        SecretFile file = gameManager.getSecretFile();
        assertNotNull(file);
        assertNotNull(file.room());
        assertNotNull(file.weapon());
        assertNotNull(file.character());
    }

    @Test
    @Disabled
    void testCardsAreDistributedEvenlyAmongPlayers() {
        int totalCards = gameManager.getCards().size();
        gameManager.initilizeGame();
        int distributed = gameManager.getPlayers().stream().mapToInt(p -> p.getCards().size()).sum();
        assertEquals(totalCards, distributed);
    }

    @Test
    void testCorrectAccusationWinsGame() {
        SecretFile actual = gameManager.getSecretFile();
        Player player = gameManager.getPlayers().get(0);
        assertTrue(gameManager.makeAccusation(player, actual));
    }

    @Test
    void testIncorrectAccusationRemovesPlayer() {
        Player player = gameManager.getPlayers().get(0);
        SecretFile wrong = new SecretFile(
                new BasicCard("FakeRoom", UUID.randomUUID(), CardType.ROOM),
                new BasicCard("FakeWeapon", UUID.randomUUID(), CardType.WEAPON),
                new BasicCard("FakeChar", UUID.randomUUID(), CardType.CHARACTER)
        );
        gameManager.makeAccusation(gameManager.getPlayers().get(gameManager.getCurrentPlayerIndex()),wrong);
        assertFalse(gameManager.getPlayers().get(gameManager.getCurrentPlayerIndex()).isActive());
    }

    @Test
    void testGetCardByNameReturnsCorrectCard() {
        BasicCard anyCard = gameManager.getCards().get(0);
        BasicCard foundCard = gameManager.getCardByName(anyCard.getCardName());

        assertNotNull(foundCard);
        assertEquals(anyCard.getCardName(), foundCard.getCardName());
    }

    @Test
    void testGetCardByNameReturnsNullIfNotFound() {
        BasicCard foundCard = gameManager.getCardByName("");
        assertNull(foundCard);
    }

    @Test
    void testGameEndsWhenOnlyOnePlayerRemains() {
        for (int i = 1; i < gameManager.getPlayers().size(); i++) {
            gameManager.getPlayers().get(i).setActive(false);
        }

        int activePlayers = (int) gameManager.getPlayers().stream().filter(Player::isActive).count();
        assertEquals(1, activePlayers);
    }

    @Test
    void testCheckGameEnd() {
        assertFalse(gameManager.checkGameEnd());

        gameManager.getPlayers().get(0).setHasWon(true);
        assertTrue(gameManager.checkGameEnd());
    }

    @Test
    void testCheckGameEndState() {
        assertFalse(gameManager.checkGameEnd());

        gameManager.setState(GameState.ENDED);
        assertTrue(gameManager.checkGameEnd());
    }

    @Test
    void testGetACardReturnsBasicCard() {
        assertNotNull(gameManager.getCards().get(0));
        assertInstanceOf(BasicCard.class, gameManager.getCards().get(0));
    }

    @Test
    void testSkipInactivePlayers() {
        // Make player 1 inactive
        gameManager.getPlayers().get(1).setActive(false);

        // Start with player 0
        assertTrue(gameManager.getPlayers().get(0).isCurrentPlayer());

        // Call next player, should skip player 1
        gameManager.nextTurn();

        // Player 2 should be current
        assertTrue(gameManager.getPlayers().get(2).isCurrentPlayer());
        assertEquals(2, gameManager.getCurrentPlayerIndex());
    }
    @Test
    void testTopOftheRound(){
        setUp();
        gameManager.nextTurn();
        gameManager.nextTurn();
        gameManager.nextTurn();
        gameManager.nextTurn();

        assertEquals(1,gameManager.getCurrentPlayerIndex());
    }

    @Test
    void testEliminatePlayer()
    {
        gameManager.eliminateCurrentPlayer();
        assertFalse(gameManager.getPlayer(player1.getName()).isActive());
    }
    @Test
    void testMakeSuggestion(){
        Player player = gameManager.getPlayers().get(1);
        // Move player to a room (Kitchen is at position 1,1)
        player.move(1,1);

        // Ensure another player has one of the cards we're suggesting
        Player otherPlayer = gameManager.getPlayers().get(0);
        BasicCard weaponCard = gameManager.getCardByName("Knife");
        if (weaponCard != null) {
            otherPlayer.addCard(weaponCard);
        }

        // Now the suggestion should be disproved (return true)
        assertTrue(gameManager.makeSuggestion(player,"Mrs. White","Knife"));
    }

    @Test
    void testGetCorrectSuspect(){
        setUp();
        assertEquals(gameManager.getSecretFile().character().getCardName(),gameManager.getCorrectSuspect());
    }
    @Test
    void testGetCorrectRoom(){
        setUp();
        assertEquals(gameManager.getSecretFile().room().getCardName(),gameManager.getCorrectRoom());
    }

    @Test
    void testGetCorrectWeapon(){
        setUp();
        assertEquals(gameManager.getSecretFile().weapon().getCardName(),gameManager.getCorrectWeapon());
    }

    @Test
    void testEliminateCurrentPlayerAndGetCorrectCards() {
        Player current = gameManager.getCurrentPlayer();


        gameManager.eliminateCurrentPlayer();


        assertFalse(current.isActive());


        assertEquals(gameManager.getSecretFile().character().getCardName(), gameManager.getCorrectSuspect());
        assertEquals(gameManager.getSecretFile().room().getCardName(), gameManager.getCorrectRoom());
        assertEquals(gameManager.getSecretFile().weapon().getCardName(), gameManager.getCorrectWeapon());
    }

}
