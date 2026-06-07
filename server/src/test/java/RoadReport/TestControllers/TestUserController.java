package RoadReport.TestControllers;

import RoadReport.controllers.UserController;
import RoadReport.controllers.dto.UserUpdateDTO;
import RoadReport.entities.User;
import RoadReport.exceptions.core.UserNotFoundException;
import RoadReport.security.service.JwtService;
import RoadReport.services.core.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class TestUserController {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    private User mock;

    @BeforeEach
    public void setUp(){
        mock = User.builder()
                .id(1L)
                .username("Giorgi")
                .email("gezug@gmail.com")
                .password("gezug2000").build();
    }

    @Test
    @WithMockUser(username = "Giorgi")
    public void testGetCurrentUserOK() throws Exception {
        when(userService.getUserByUsername("Giorgi")).thenReturn(mock);

        mvc.perform(get("/api/users/me")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Giorgi"))
                .andExpect(jsonPath("$.email").value("gezug@gmail.com"))
                .andExpect(jsonPath("$.reputationScore").value(0));

        verify(userService).getUserByUsername("Giorgi");
    }

    @Test
    @WithMockUser(username = "Giorgi")
    public void testGetCurrentUserError() throws Exception {
        when(userService.getUserByUsername("Giorgi"))
                .thenThrow(new UserNotFoundException("couldn't find user: Giorgi"));

        mvc.perform(get("/api/users/me")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "Giorgi")
    public void testGetUserOK() throws Exception {
        when(userService.getUserById(1L)).thenReturn(mock);

        mvc.perform(get("/api/users/{id}", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Giorgi"))
                .andExpect(jsonPath("$.reputationScore").value(0));

        verify(userService).getUserById(1L);
    }

    @Test
    @WithMockUser(username = "Giorgi")
    public void testGetUserError() throws Exception {
        when(userService.getUserById(1L))
                .thenThrow(new UserNotFoundException("couldn't find user with this ID"));

        mvc.perform(get("/api/users/{id}", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }


    @Test
    @WithMockUser(username = "Giorgi")
    public void testUpdateUserOK() throws Exception {
        UserUpdateDTO updateData = new UserUpdateDTO
                ("Gio", "gio@gmail.com", "007");

        User updated = User.builder()
                .id(1L)
                .username("Gio")
                .email("gio@gmail.com")
                .password("007").build();

        when(userService.getUserByUsername("Giorgi")).thenReturn(mock);
        when(userService.updateUser(1L, updateData)).thenReturn(updated);

        mvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData))
                        .accept(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Gio"))
                .andExpect(jsonPath("$.email").value("gio@gmail.com"));

        verify(userService).getUserByUsername("Giorgi");
        verify(userService).updateUser(1L, updateData);
    }

    @Test
    @WithMockUser(username = "Giorgi")
    public void testUpdateUserError() throws Exception {
        mvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    @WithMockUser(username = "Giorgi")
    public void testDeleteOK() throws Exception {
        when(userService.getUserByUsername("Giorgi")).thenReturn(mock);

        mvc.perform(delete("/api/users/me")
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(userService).getUserByUsername("Giorgi");
        verify(userService).deleteUser(1L);
    }

    @Test
    @WithMockUser(username = "Giorgi")
    public void testDeleteUserError() throws Exception {
        mvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }
}
