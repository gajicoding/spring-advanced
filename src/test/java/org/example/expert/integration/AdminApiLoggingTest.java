package org.example.expert.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.security.JwtUtil;
import org.example.expert.domain.user.dto.request.UserRoleChangeRequest;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.service.UserAdminService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
public class AdminApiLoggingTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserAdminService userAdminService;

    @Test
    public void 어드민_댓글_삭제_로깅_통합_테스트(CapturedOutput output) throws Exception {
        //given
        String token = jwtUtil.createToken(123L, "test@example.com", UserRole.ADMIN);

        //when
        mockMvc.perform(
                delete("/admin/comments/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", token)
        ).andExpect(status().isOk());

        // then
        assertThat(output)
                .contains("[ADMIN API REQUEST] userId=123, url=/admin/comments/1")
                .contains("[ADMIN API RESPONSE] userId=123, url=/admin/comments/1");
    }

    @Test
    public void 어드민_UserRole_변경_로깅_통합_테스트(CapturedOutput output) throws Exception {
        //given
        String token = jwtUtil.createToken(123L, "test@example.com", UserRole.ADMIN);
        UserRoleChangeRequest userRoleChangeRequest = new UserRoleChangeRequest("ADMIN");

        doNothing().when(userAdminService).changeUserRole(eq(123L), any(UserRoleChangeRequest.class));

        //when
        mockMvc.perform(
                patch("/admin/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(objectMapper.writeValueAsString(userRoleChangeRequest))
        ).andExpect(status().isOk());

        // then
        assertThat(output)
                .contains("[ADMIN API REQUEST] userId=123, url=/admin/users/1")
                .contains("[ADMIN API RESPONSE] userId=123, url=/admin/users/1");
    }
}
