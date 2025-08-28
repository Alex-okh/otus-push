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
import org.test.firebase.pushservice.model.dto.MessageDTO;
import org.test.firebase.pushservice.service.SendMessageService;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SendMessageController.class)
class SendMessageControllerTest {

    private final String apiPath = "/api/pushservice/v1/";
    private final String validUserId = "valid_userId_322";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SendMessageService sendMessageService;

    @Test
    @DisplayName("Возвращает 200 на валидный запрос POST и вызывает send с переданным ДТО")
    void sendMessageRetuns200WhenValidRequest() throws Exception {
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setUserId(validUserId);
        messageDTO.setMessageTitle("TEST Title");
        messageDTO.setMessageText("TEST Message text");

        mockMvc.perform(post(apiPath + "send").contentType(MediaType.APPLICATION_JSON)
                                              .content(objectMapper.writeValueAsString(messageDTO)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.status").value("200 OK"))
               .andExpect(jsonPath("$.message").value("Message sent successfully"));

        Mockito.verify(sendMessageService)
               .send(messageDTO);
    }

    @Test
    @DisplayName("Возвращает 400 на невалидный запрос POST")
    void sendMessageReturns400WhenInvalidRequest() throws Exception {
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setUserId(validUserId);
        messageDTO.setMessageTitle(null);
        messageDTO.setMessageText("");

        mockMvc.perform(post(apiPath + "send").contentType(MediaType.APPLICATION_JSON)
                                              .content("{}"))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.status").value("400 BAD_REQUEST"));

        mockMvc.perform(post(apiPath + "send").contentType(MediaType.APPLICATION_JSON)
                                              .content(objectMapper.writeValueAsString(messageDTO)))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.status").value("400 BAD_REQUEST"));
    }
}