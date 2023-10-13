package com.example.functional;


import com.example.Main;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;

import com.example.config.WebSecurityConfig;
import com.example.pojo.User;
import com.example.repository.UserRepository;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Main.class, WebSecurityConfig.class})
@AutoConfigureMockMvc
public class UserAPIIntegrationTest {

    @Autowired
	private MockMvc mockMvc;

    @MockBean
    UserRepository userRepository;

	@Test
	public void getIndexPageTest() throws Exception {
		this.mockMvc.perform(get("/")).andExpect(status().isOk())
			.andExpect(content().string(containsString("Welcome to VanHotel")));
	}
 
    @Test
    public void getUserRegisterPageTest() throws Exception {
        this.mockMvc.perform(get("/register")).andExpect(status().isOk())
            .andExpect(content().string(containsString("User Registration - VanHotel")));
    }

    @Test
    @WithMockUser(authorities = {"ADMIN"})
    public void AdminGetAllUsersTest() throws Exception {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            User user = new User();
            user.setId(Long.valueOf(i));
            users.add(user);
        }

        when(userRepository.findAll()).thenReturn(users);

        mockMvc.perform(get("/users").with(csrf()))
            .andExpect(status().isOk())
            .andExpect(model().attribute("records", users));
    }

    @Test
    @WithMockUser(authorities = {"USER"})
    public void nonAuthorizedUserGetAllUsersShouldFailTest() throws Exception {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            User user = new User();
            user.setId(Long.valueOf(i));
            users.add(user);
        }

        when(userRepository.findAll()).thenReturn(users);

        mockMvc.perform(get("/users").with(csrf()))
                .andExpect(status().is4xxClientError());
    }

   @Test
   @WithMockUser(authorities = {"ADMIN"})
   public void AdminGetUserDetailsTest() throws Exception {
       User user = new User();
       user.setEmail("test-email");

       when(userRepository.findById(2l)).thenReturn(Optional.of(user));

       mockMvc.perform(get("/users/2").with(csrf()))
               .andExpect(status().isOk())
               .andExpect(model().attribute("user", user));
   }

}
