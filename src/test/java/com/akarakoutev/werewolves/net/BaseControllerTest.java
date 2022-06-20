package com.akarakoutev.werewolves.net;

import com.akarakoutev.werewolves.TestUtil;
import com.akarakoutev.werewolves.net.message.MessageUtil;
import com.akarakoutev.werewolves.net.message.ServerMessage;
import com.google.gson.JsonObject;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
public class BaseControllerTest {

    @Autowired
    MockMvc mockMvc;

    private final TestUtil util;

    @Autowired
    public BaseControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
        util = new TestUtil(mockMvc);
    }

    @Before
    public void clearGames() {
        util.deleteAllGames();
    }

    @Test
    void createGameTest() {
        String gameId = util.createGameRequest(2);
        assertDoesNotThrow( () ->  Integer.parseInt(gameId));
    }

    @Test
    void deleteGameTest() {
        String gameId = util.createGameRequest(2);
        String gameIdResponse = util.deleteGameRequest(gameId);
        assertDoesNotThrow( () ->  Integer.parseInt(gameIdResponse));
    }

    @Test
    void getAllGamesTest() {
        String game1Id = util.createGameRequest(1);
        String game2Id = util.createGameRequest(2);

        ServerMessage allGamesResponse = util.getResponseAsMessage(TestUtil.Prefix.BASE, TestUtil.Type.GET, util.url("game", "all"), HttpStatus.OK, Collections.emptyMap());
        List<?> content = MessageUtil.fromContent(allGamesResponse.getContent(), List.class);

        LinkedHashMap<String, Object> game1expected = new LinkedHashMap<>(Map.of("id", game1Id, "players", "0/1", "started", false));
        LinkedHashMap<String, Object> game2expected = new LinkedHashMap<>(Map.of("id", game2Id, "players", "0/2", "started", false));

        assertTrue(content.contains(game1expected));
        assertTrue(content.contains(game2expected));

    }

    @Test
    void loginTest() {
        String gameId = util.createGameRequest(2);
        util.loginUserRequest(gameId, "username");
    }

    @Test
    void getPlayersTest() {
        String username1 = "username1";

        String gameId = util.createGameRequest(2);
        util.loginUserRequest(gameId, username1);

        ServerMessage response = util.getResponseAsMessage(TestUtil.Prefix.BASE, TestUtil.Type.GET,util.url("game", gameId, "players"), HttpStatus.OK, Collections.emptyMap());
        List<?> content = MessageUtil.fromContent(response.getContent().getAsJsonArray(), List.class);

        LinkedHashMap<String, String> game1expected = new LinkedHashMap<>(Map.of("name", username1, "ready", "false"));
        assertTrue(content.contains(game1expected));
     }

    @Test
    void loginSameUsernameTwiceFails() {
        String gameId = util.createGameRequest(2);
        util.loginUserRequest(gameId, "username");
        util.getResponseAsMessage(TestUtil.Prefix.BASE, TestUtil.Type.POST, util.url("user", "login"), HttpStatus.FORBIDDEN, Map.of("gameId", gameId, "username", "username"));

    }

    @Test
    void loginWrongGameFails() {
        util.getResponseAsMessage(TestUtil.Prefix.BASE, TestUtil.Type.POST, util.url("user", "login"), HttpStatus.NOT_FOUND, Map.of("gameId", "0000", "username", "username"));
    }


}
