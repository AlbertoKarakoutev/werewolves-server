package com.akarakoutev.werewolves.net;

import com.akarakoutev.werewolves.TestUtil;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Map;

@SpringBootTest
@AutoConfigureMockMvc
public class GameControllerTest {

    @Autowired
    MockMvc mockMvc;

    private final TestUtil util;

    @Autowired
    public GameControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
        util = new TestUtil(mockMvc);
    }

    @Before
    public void clearGames() {
        util.deleteAllGames();
    }

    @Ignore
    @Test
    public void setTargetTest() {
//        String gameId = util.createGameRequest(1);
//        String username = "username";
//        util.loginUserRequest(gameId, username);
//        util.getResponseAsMessage(TestUtil.Prefix.LOBBY, TestUtil.Type.PUT, util.url("user", username, "ready"), HttpStatus.OK, Map.of("gameId", gameId, "username", username));
//        util.getResponseAsMessage(TestUtil.Prefix.GAME, TestUtil.Type.POST, util.url("loggedIn"), HttpStatus.OK, Map.of("gameId", gameId, "username", username));

    }


}
