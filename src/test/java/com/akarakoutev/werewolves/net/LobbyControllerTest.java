package com.akarakoutev.werewolves.net;

import com.akarakoutev.werewolves.TestUtil;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

@SpringBootTest
@AutoConfigureMockMvc
public class LobbyControllerTest {

    @Autowired
    MockMvc mockMvc;

    private final TestUtil util;

    @Autowired
    public LobbyControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
        util = new TestUtil(mockMvc);
    }

    @Before
    public void clearGames() {
        util.deleteAllGames();
    }

    @Test
    public void playerReadyTest() {
        String gameId = util.createGameRequest(2);
        String username = "username";
        util.loginUserRequest(gameId, username);
        util.getResponseAsMessage(TestUtil.Prefix.LOBBY, TestUtil.Type.PUT, util.url("user", "ready"), HttpStatus.OK, Map.of("gameId", gameId, "username", username));
    }

}
