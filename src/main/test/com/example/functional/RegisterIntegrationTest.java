package com.example.functional;

import com.example.Main;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.sql.Time;
import java.util.ArrayList;

import com.example.pojo.User;
import com.example.repository.UserRepository;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Main.class})
@AutoConfigureMockMvc
public class RegisterIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;
    

    @Test
    public void getHomePageTest() throws Exception{
        mockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Welcome to VanHotel")));
    }
    
    @Test
    public void registerCustomerUserTest() throws Exception {
        User user = new User();
        user.setEmail("100test@gmail.com");
        user.setPassword("1234556");
        user.setFirstName("test");
        user.setLastName("Test");

        when(userRepository.save(any(User.class))).thenReturn(user);

        mockMvc.perform(post("/process_register")
            .with(csrf())           // cross site request 
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("email", "test100@gmail.com")
            .param("password", "1234556")
            .param("firstName", "test")
            .param("lastName", "Test")
            .param("userType", "USER")
            )
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("You have signed up successfully!")));

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void registerHotelManagerTest() throws Exception {
        User user = new User();
        user.setEmail("100test@gmail.com");
        user.setPassword("1234556");
        user.setFirstName("test");
        user.setLastName("Test");

        when(userRepository.save(any(User.class))).thenReturn(user);

        mockMvc.perform(post("/process_register")
                .with(csrf())           // cross site request
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("email", "test100@gmail.com")
                .param("password", "1234556")
                .param("firstName", "test")
                .param("lastName", "Test")
                .param("userType", "HOTEL")
        )
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("You have signed up successfully!")));

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void registerAdminShouldFailTest() throws Exception {
        User user = new User();
        user.setEmail("100test@gmail.com");
        user.setPassword("1234556");
        user.setFirstName("test");
        user.setLastName("Test");

        when(userRepository.save(any(User.class))).thenReturn(user);

        mockMvc.perform(post("/process_register")
                .with(csrf())           // cross site request
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("email", "test100@gmail.com")
                .param("password", "1234556")
                .param("firstName", "test")
                .param("lastName", "Test")
                .param("userType", "ADMIN")
        )
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Error Message")));

        verify(userRepository, times(0)).save(any(User.class));
    }

    
}
