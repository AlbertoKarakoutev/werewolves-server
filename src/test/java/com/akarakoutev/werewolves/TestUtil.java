package com.akarakoutev.werewolves;

import com.akarakoutev.werewolves.net.message.Message;
import com.akarakoutev.werewolves.net.message.MessageUtil;
import com.akarakoutev.werewolves.net.message.ServerMessage;
import com.akarakoutev.werewolves.net.mvc.BaseService;
import com.akarakoutev.werewolves.net.mvc.GameService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TestUtil {

    private static final Gson serializer = new Gson();

    private final MockMvc mockMvc;

    public TestUtil(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    public enum Type {
        GET,
        POST,
        PUT,
        DELETE
    }

    public enum Prefix {
        BASE("/base/"),
        LOBBY("/lobby/"),
        GAME("/game/");

        String value;

        Prefix(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

    }

    public void deleteAllGames() {
        BaseService.deleteAllGames();
    }

    public String createGameRequest(int players) {
        ServerMessage response = getResponseAsMessage(Prefix.BASE, Type.POST, url("game", "create"), HttpStatus.OK, Map.of("players", Integer.toString(players)));
        return response.getContent().getAsJsonObject().get("gameId").getAsString();
    }

    public String deleteGameRequest(String gameId) {
        ServerMessage response = getResponseAsMessage(Prefix.BASE, Type.DELETE, url("game", gameId, "delete"), HttpStatus.OK, Collections.emptyMap());
        return response.getContent().getAsJsonObject().get("gameId").getAsString();
    }

    public String url(String... values) {
        return Arrays.asList(values).stream().collect(Collectors.joining("/"));
    }

    public void loginUserRequest(String gameId, String username) {
        getResponseAsMessage(Prefix.BASE, TestUtil.Type.POST, "/user/login", HttpStatus.OK, Map.of("gameId", gameId, "username", username));
    }

    public ServerMessage getResponseAsMessage(Prefix prefix, Type type, String rawUrl, HttpStatus responseCode, Map<String, String> paramsMap) {
        try {
            MvcResult result = sendRequest(prefix.getValue(), type, rawUrl, responseCode, paramsMap);
            return serializer.fromJson(result.getResponse().getContentAsString(), ServerMessage.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private MvcResult sendRequest(String prefix, Type type, String rawUrl, HttpStatus responseCode, Map<String, String> paramsMap) throws Exception {

        String url = prefix + rawUrl;
        MockHttpServletRequestBuilder request;

        JsonObject content = new JsonObject();
        paramsMap.forEach(content::addProperty);
        String requestBody = (!paramsMap.isEmpty())
                ? MessageUtil.serialize(new Message(content))
                : "{}";
        switch (type) {
            case GET:
                Map<String, List<String>> paramsMapConv = new HashMap<>();
                paramsMap.forEach((k,v) -> paramsMapConv.put(k, List.of(v)));
                MultiValueMap<String, String> params = new LinkedMultiValueMap<>(paramsMapConv);
                request = get(url).params(params);
                break;
            case POST:
                request = post(url).contentType(MediaType.APPLICATION_JSON).content(requestBody);
                break;
            case PUT:
                request = put(url).contentType(MediaType.APPLICATION_JSON).content(requestBody);
                break;
            case DELETE:
                request = delete(url);
                break;
            default:
                throw new IllegalArgumentException();
        }
        return mockMvc.perform(request).andExpect(status().is(responseCode.value())).andReturn();
    }
}
