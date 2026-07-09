package RoadReport.TestControllers;

import RoadReport.controllers.UserController;
import RoadReport.controllers.dto.user.UserUpdateDTO;
import RoadReport.entities.User;
import RoadReport.exceptions.core.UserNotFoundException;
import RoadReport.security.service.JwtService;
import RoadReport.security.service.RoadUserDetails;
import RoadReport.security.service.RoadUserDetailsService;
import RoadReport.services.core.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@ExtendWith(MockitoExtension.class)
public class TestUserController {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private RoadUserDetailsService roadUserDetailsService;

    @Mock
    private RoadUserDetails mockDetails;

    @BeforeEach
    public void setUp(){
        User mock = User.builder()
                .id(1L)
                .username("Giorgi")
                .email("gezug@gmail.com")
                .password("gezug2000").build();

        lenient().when(mockDetails.getId()).thenReturn(1L);
        lenient().when(mockDetails.getUsername()).thenReturn("Giorgi");
        lenient().when(mockDetails.getEmail()).thenReturn("gezug@gmail.com");
        lenient().when(mockDetails.getReputationScore()).thenReturn(0);
        lenient().when(userService.getUserById(1L)).thenReturn(mock);
    }

    @Test
    @WithMockUser(username = "Giorgi")
    public void testGetCurrentUserOK() throws Exception {
        mvc.perform(get("/api/users/me")
                        .with(user(mockDetails))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Giorgi"))
                .andExpect(jsonPath("$.email").value("gezug@gmail.com"))
                .andExpect(jsonPath("$.reputationScore").value(0));
    }

    @Test
    public void testGetCurrentUserError() throws Exception {
        mvc.perform(get("/api/users/me")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "Giorgi")
    public void testGetUserOK() throws Exception {
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
        when(userService.getUserById(2L))
                .thenThrow(new UserNotFoundException("couldn't find user with this ID"));

        mvc.perform(get("/api/users/{id}", 2L)
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

        when(userService.updateUser(1L, updateData)).thenReturn(updated);

        mvc.perform(put("/api/users/me")
                        .with(csrf())
                        .with(user(mockDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Gio"))
                .andExpect(jsonPath("$.email").value("gio@gmail.com"));

        verify(userService).updateUser(1L, updateData);
    }

    @Test
    @WithMockUser(username = "Giorgi")
    public void testUpdateUserError() throws Exception {
        mvc.perform(put("/api/users/me")
                        .with(csrf())
                        .with(user(mockDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    @WithMockUser(username = "Giorgi")
    public void testDeleteOK() throws Exception {
        mvc.perform(delete("/api/users/me")
                        .with(csrf())
                        .with(user(mockDetails)))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L);
    }

    @Test
    @WithMockUser(username = "Giorgi")
    public void testDeleteUserError() throws Exception {
        doThrow(new UserNotFoundException("Not found"))
                .when(userService).deleteUser(1L);

        mvc.perform(delete("/api/users/me")
                        .with(csrf())
                        .with(user(mockDetails))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(userService).deleteUser(1L);
    }
}
