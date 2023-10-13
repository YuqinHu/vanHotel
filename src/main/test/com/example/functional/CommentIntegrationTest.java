package com.example.functional;

import com.example.Main;
import com.example.helper.TestUserDetailsHelper;
import com.example.pojo.Comment;
import com.example.pojo.Hotel;
import com.example.pojo.User;
import com.example.repository.CommentRepository;
import com.example.repository.HotelRepository;
import com.example.repository.RoomRepository;
import com.example.repository.UserRepository;
import org.assertj.core.util.Sets;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Main.class})
@AutoConfigureMockMvc
public class CommentIntegrationTest {

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

    private Hotel testHotel;
    private String testHotelName;
    private Comment testComment;

    @Before
    public void setUpTest() {
        customer = userRepository.findByEmail(TestUserDetailsHelper.TEST_CUSTOMER_USERNAME);
        hotelManager = userRepository.findByEmail(TestUserDetailsHelper.TEST_HOTEL_USERNAME);
        admin = userRepository.findByEmail(TestUserDetailsHelper.TEST_ADMIN_USERNAME);

        testHotelName = "Test Hotel";

        testComment = new Comment();
        testComment.setId(1l);
        testComment.setContent("test content");
        testComment.setHotelName(testHotelName);

        testHotel = new Hotel();
        testHotel.setName("Test Hotel");
        testHotel.setStar(3);
        testHotel.setDescription("dummy");
        testHotel.setPhone("xxx-xxx-xxxx");
        testHotel.setAvatar("dummy");
        testHotel.setAddress("dummy");
        testHotel.setPostcode("xxxxxx");
        Set<Comment> comments = new HashSet<>();
        comments.add(testComment);
        testHotel.setComments(comments);
    }

    @Test
    @WithUserDetails(value = "test-customer", userDetailsServiceBeanName = "myUserDetailsService")
    public void customerCreateCommentTest() throws Exception{

        when(hotelRepository.findByName(testHotelName)).thenReturn(testHotel);

        mockMvc.perform(post("/comments/create")
            .with(csrf())           // cross site request
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("hotelName", testHotelName)
            .param("content", "1234556")
            )
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Comment Creation Succeeded!")));

        verify(commentRepository, times(1)).save(any());
        verify(hotelRepository, times(1)).findByName(testHotelName);
        verify(hotelRepository, times(1)).save(any());
    }

    @Test
    @WithUserDetails(value = "test-customer", userDetailsServiceBeanName = "myUserDetailsService")
    public void createCommentInvalidHotelNameShouldFailTest() throws Exception{

        when(hotelRepository.findByName(testHotelName)).thenReturn(testHotel);

        mockMvc.perform(post("/comments/create")
                .with(csrf())           // cross site request
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("hotelName", "Wrong Hotel Name")
                .param("content", "1234556")
        )
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Error Message")));

        verify(hotelRepository, times(1)).findByName(any());
    }

    @Test
    @WithUserDetails(value = "test-hotel", userDetailsServiceBeanName = "myUserDetailsService")
    public void hotelManagerAccessCommentAPIShouldFailTest() throws Exception{

        when(hotelRepository.findByName(testHotelName)).thenReturn(testHotel);

        mockMvc.perform(post("/comments/create")
                .with(csrf())           // cross site request
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("hotelName", testHotelName)
                .param("content", "1234556")
        )
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/comments"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/comments/1"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/comments/1/delete"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = "test-customer", userDetailsServiceBeanName = "myUserDetailsService")
    public void customerGetAllComments() throws Exception{

        List<Comment> commentList = new ArrayList<>();
        when(commentRepository.findAll()).thenReturn(commentList);

        mockMvc.perform(get("/comments"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("records", commentList));

        verify(commentRepository, times(1)).findAll();
    }

    @Test
    @WithUserDetails(value = "test-customer", userDetailsServiceBeanName = "myUserDetailsService")
    public void customerGetCommentDetails() throws Exception{

        when(commentRepository.findById(testComment.getId())).thenReturn(Optional.of(testComment));

        mockMvc.perform(get("/comments/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Comment")));

        verify(commentRepository, times(2)).findById(testComment.getId());
    }

    @Test
    @WithUserDetails(value = "test-customer", userDetailsServiceBeanName = "myUserDetailsService")
    public void deleteCommentTest() throws Exception{
        testComment.setAuthor(TestUserDetailsHelper.TEST_CUSTOMER_USERNAME);

        when(commentRepository.findById(any())).thenReturn(Optional.of(testComment));
        when(hotelRepository.findByName(any())).thenReturn(testHotel);

        mockMvc.perform(get("/comments/1/delete"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Comment Deletion Succeeded!")));

        verify(hotelRepository, times(1)).save(testHotel);
        verify(commentRepository, times(2)).findById(testComment.getId());
    }

    @Test
    @WithUserDetails(value = "test-customer", userDetailsServiceBeanName = "myUserDetailsService")
    public void deleteCommentNtOwnerShouldFailTest() throws Exception{
        testComment.setAuthor("dummy user");

        when(commentRepository.findById(any())).thenReturn(Optional.of(testComment));
        when(hotelRepository.findByName(any())).thenReturn(testHotel);

        mockMvc.perform(get("/comments/1/delete"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Error Message")));

        verify(hotelRepository, times(0)).save(testHotel);
        verify(commentRepository, times(2)).findById(testComment.getId());
    }

}
