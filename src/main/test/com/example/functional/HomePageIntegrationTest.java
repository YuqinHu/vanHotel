package com.example.functional;

import com.example.Main;
import com.example.config.MyUserDetails;
import com.example.config.WebSecurityConfig;
import com.example.helper.TestUserDetailsHelper;
import com.example.pojo.Hotel;
import com.example.pojo.Role;
import com.example.pojo.User;
import com.example.repository.CommentRepository;
import com.example.repository.HotelRepository;
import com.example.repository.RoomRepository;
import com.example.repository.UserRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import javax.annotation.PostConstruct;
import java.util.*;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Main.class})
@AutoConfigureMockMvc
public class HomePageIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;

    @MockBean
    private HotelRepository hotelRepository;
    @MockBean
    private RoomRepository roomRepository;
    @MockBean
    private CommentRepository commentRepository;

    private User customer;
    private User hotelManager;
    private User admin;

    @Before
    public void setUpTest() {
        customer = userRepository.findByEmail(TestUserDetailsHelper.TEST_CUSTOMER_USERNAME);
        hotelManager = userRepository.findByEmail(TestUserDetailsHelper.TEST_HOTEL_USERNAME);
        admin = userRepository.findByEmail(TestUserDetailsHelper.TEST_ADMIN_USERNAME);
    }


    @Test
    @WithUserDetails(value = "test-customer", userDetailsServiceBeanName = "myUserDetailsService")
    public void getCustomerHomePageTest() throws Exception {

        List<Hotel> hotelList = new ArrayList<>();
        when(hotelRepository.findAllByStar(anyInt())).thenReturn(hotelList);
        when(hotelRepository.findAll()).thenReturn(hotelList);

        mockMvc.perform(get("/home/customer"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("User Type: Customer")))
                .andExpect(model().attribute("user", customer))
                .andExpect(model().attribute("hotels", hotelList));

        verify(commentRepository, times(1)).findAllByAuthor(TestUserDetailsHelper.TEST_CUSTOMER_USERNAME);
    }

    @Test
    @WithUserDetails(value = "test-hotel", userDetailsServiceBeanName = "myUserDetailsService")
    public void getHotelManagerHomePageTest() throws Exception {

        List<Hotel> hotelList = new ArrayList<>();

        mockMvc.perform(get("/home/hotelManager"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("User Type: Hotel Manager")))
                .andExpect(model().attribute("user", hotelManager));
    }

    @Test
    @WithUserDetails(value = "test-admin", userDetailsServiceBeanName = "myUserDetailsService")
    public void getAdminHomePageTest() throws Exception {
        List<Hotel> hotelList = new ArrayList<>();
        when(hotelRepository.findAllByStar(anyInt())).thenReturn(hotelList);
        when(hotelRepository.findAll()).thenReturn(hotelList);

        mockMvc.perform(get("/home/admin"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Admin Home Page")))
                .andExpect(model().attribute("users", userRepository.findAll()))
                .andExpect(model().attribute("hotels", hotelList));

        verify(hotelRepository, times(1)).findAll();
    }

    @Test
    @WithUserDetails(value = "test-customer", userDetailsServiceBeanName = "myUserDetailsService")
    public void customerGetAdminHomePageShouldFailTest() throws Exception {
        mockMvc.perform(get("/home/admin"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = "test-hotel", userDetailsServiceBeanName = "myUserDetailsService")
    public void hotelManagerGetAdminHomePageShouldFailTest() throws Exception {
        mockMvc.perform(get("/home/admin"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    // TODO: test filter cases

}
