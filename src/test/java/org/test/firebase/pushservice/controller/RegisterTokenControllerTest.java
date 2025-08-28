package org.test.firebase.pushservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.test.firebase.pushservice.model.dto.TokenDTO;
import org.test.firebase.pushservice.service.TokenService;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RegisterTokenController.class)
class RegisterTokenControllerTest {

    private final String validToken = "valid_token_123";
    private final String validUserId = "valid_userId_322";
    private final String apiPath = "/api/pushservice/v1/";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private TokenService tokenService;

    @Test
    @DisplayName("Возвращает 201 на валидный запрос POST и вызывает save с переданным ДТО")
    void addToken_returns201WhenValidRequest() throws Exception {
        TokenDTO tokenDTO = new TokenDTO();
        tokenDTO.setUserId(validUserId);
        tokenDTO.setToken(validToken);

        mockMvc.perform(post(apiPath + "token").contentType(MediaType.APPLICATION_JSON)
                                               .content(objectMapper.writeValueAsString(tokenDTO)))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.status").value("201 CREATED"))
               .andExpect(jsonPath("$.message").value("Token registered successfully"));

        Mockito.verify(tokenService)
               .save(tokenDTO);
    }

    @Test
    @DisplayName("Возвращает 400 на некорректный запрос POST")
    void addToken_returns400WhenEmptyRequest() throws Exception {
        TokenDTO tokenDTO = new TokenDTO();
        tokenDTO.setUserId(validUserId);
        tokenDTO.setToken(null);

        mockMvc.perform(post(apiPath + "token").contentType(MediaType.APPLICATION_JSON)
                                               .content("{}"))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.status").value("400 BAD_REQUEST"));

        mockMvc.perform(post(apiPath + "token").contentType(MediaType.APPLICATION_JSON)
                                               .content(objectMapper.writeValueAsString(tokenDTO)))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.status").value("400 BAD_REQUEST"));

    }

    @Test
    @DisplayName("Возвращает 200 на валидный запрос DELETE и вызывает delete с переданным ДТО")
    void deleteToken_returns200WhenValidRequest() throws Exception {
        TokenDTO tokenDTO = new TokenDTO();
        tokenDTO.setUserId(validUserId);
        tokenDTO.setToken(validToken);

        mockMvc.perform(delete(apiPath + "token").contentType(MediaType.APPLICATION_JSON)
                                                 .content(objectMapper.writeValueAsString(tokenDTO)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.status").value("200 OK"))
               .andExpect(jsonPath("$.message").value("Token deleted successfully"));

        Mockito.verify(tokenService)
               .delete(tokenDTO);
    }

    @Test
    @DisplayName("Возвращает 400 на некорректный запрос DELETE")
    void deleteToken_returns400WhenEmptyRequest() throws Exception {
        TokenDTO tokenDTO = new TokenDTO();
        tokenDTO.setUserId(validUserId);
        tokenDTO.setToken(null);

        mockMvc.perform(delete(apiPath + "token").contentType(MediaType.APPLICATION_JSON)
                                                 .content("{}"))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.status").value("400 BAD_REQUEST"));

        mockMvc.perform(delete(apiPath + "token").contentType(MediaType.APPLICATION_JSON)
                                                 .content(objectMapper.writeValueAsString(tokenDTO)))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.status").value("400 BAD_REQUEST"));
    }

}
