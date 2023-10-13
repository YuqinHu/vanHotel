package com.example.functional;

import com.example.Main;
import com.example.helper.TestUserDetailsHelper;
import com.example.pojo.Comment;
import com.example.pojo.Hotel;
import com.example.pojo.Room;
import com.example.pojo.User;
import com.example.repository.CommentRepository;
import com.example.repository.HotelRepository;
import com.example.repository.RoomRepository;
import com.example.repository.UserRepository;
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
public class HotelManagerIntegrationTest {

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
    private Room testRoom;
    private String testRoomName;

    @Before
    public void setUpTest() {
        customer = userRepository.findByEmail(TestUserDetailsHelper.TEST_CUSTOMER_USERNAME);
        hotelManager = userRepository.findByEmail(TestUserDetailsHelper.TEST_HOTEL_USERNAME);
        admin = userRepository.findByEmail(TestUserDetailsHelper.TEST_ADMIN_USERNAME);

        testHotelName = "Test Hotel";
        testRoomName = "Test Room";

        testRoom = new Room();
        testRoom.setName(testRoomName);
        testRoom.setId(1l);
        testRoom.setSize(40);
        testRoom.setPrice(200);
        testRoom.setPeople(4);
        testRoom.setDetails("dummy");
        testRoom.setBeds(2);
        testRoom.setAvatar("dummy");
        testRoom.setHotelName(testHotelName);

        testHotel = new Hotel();
        testHotel.setName("Test Hotel");
        testHotel.setStar(3);
        testHotel.setDescription("dummy");
        testHotel.setPhone("xxx-xxx-xxxx");
        testHotel.setAvatar("dummy");
        testHotel.setAddress("dummy");
        testHotel.setPostcode("xxxxxx");
        testHotel.setQuarantine(true);
        Set<Room> rooms = new HashSet<>();
        rooms.add(testRoom);
        testHotel.setRooms(rooms);
    }

    @Test
    @WithUserDetails(value = "test-hotel", userDetailsServiceBeanName = "myUserDetailsService")
    public void hotelManagerCreateHotelTest() throws Exception{
        User tmp = userRepository.findByEmail(TestUserDetailsHelper.TEST_HOTEL_USERNAME);
        if (tmp.getHotel() != null) {
            tmp.setHotel(null);
        }
        userRepository.save(tmp);

        when(hotelRepository.findByName(any())).thenReturn(null);
        when(hotelRepository.save(any())).thenReturn(null);

        mockMvc.perform(post("/hotels/create")
                .with(csrf())           // cross site request
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", testHotel.getName())
                .param("star", "3")
                .param("address", testHotel.getAddress())
                .param("phone", testHotel.getPhone())
                .param("postcode", testHotel.getPostcode())
                .param("avatar", testHotel.getAvatar())
                .param("description", testHotel.getDescription())
                .param("quarantine", "1")
        )
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Creation Succeeded!")));

        verify(hotelRepository, times(1)).findByName(testHotelName);
        verify(hotelRepository, times(1)).save(any());
    }


    @Test
    @WithUserDetails(value = "test-hotel", userDetailsServiceBeanName = "myUserDetailsService")
    public void hotelManagerCreateHotelAlreadyExistShouldFailTest() throws Exception{
        when(hotelRepository.findByName(testHotelName)).thenReturn(testHotel);
        when(hotelRepository.save(any())).thenReturn(testHotel);

        mockMvc.perform(post("/hotels/create")
                .with(csrf())           // cross site request
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", testHotel.getName())
                .param("star", "3")
                .param("address", testHotel.getAddress())
                .param("phone", testHotel.getPhone())
                .param("postcode", testHotel.getPostcode())
                .param("avatar", testHotel.getAvatar())
                .param("description", testHotel.getDescription())
                .param("quarantine", "1")
        )
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Error Message")));

        verify(hotelRepository, times(1)).findByName(testHotelName);
        verify(hotelRepository, times(0)).save(any());
    }

    @Test
    @WithUserDetails(value = "test-customer", userDetailsServiceBeanName = "myUserDetailsService")
    public void customerCreateHotelShouldFailTest() throws Exception{
        when(hotelRepository.findByName(testHotelName)).thenReturn(testHotel);
        when(hotelRepository.save(any())).thenReturn(testHotel);

        mockMvc.perform(post("/hotels/create")
                .with(csrf())           // cross site request
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", testHotel.getName())
                .param("star", "3")
                .param("address", testHotel.getAddress())
                .param("phone", testHotel.getPhone())
                .param("postcode", testHotel.getPostcode())
                .param("avatar", testHotel.getAvatar())
                .param("description", testHotel.getDescription())
                .param("quarantine", "1")
        )
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Error Message")));
    }

    @Test
    @WithUserDetails(value = "test-hotel", userDetailsServiceBeanName = "myUserDetailsService")
    public void hotelManagerNoHotelCreateRoomShouldFailTest() throws Exception{
        User tmp = userRepository.findByEmail(TestUserDetailsHelper.TEST_HOTEL_USERNAME);
        if (tmp.getHotel() != null) {
            tmp.setHotel(null);
        }
        userRepository.save(tmp);

        when(roomRepository.save(any())).thenReturn(testRoom);
        when(hotelRepository.save(any())).thenReturn(testHotel);

        mockMvc.perform(post("/rooms/create")
                .with(csrf())           // cross site request
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", testRoom.getName())
                .param("beds", "2")
                .param("people", "4")
                .param("price", "200")
                .param("size", "40")
                .param("details", testRoom.getDetails())
                .param("avatar", testRoom.getAvatar())
        )
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Error Message")));

        verify(hotelRepository, times(0)).save(any());
        verify(roomRepository, times(0)).save(any());
    }

    @Test
    @WithUserDetails(value = "test-hotel", userDetailsServiceBeanName = "myUserDetailsService")
    public void hotelManagerCreateRoomTest() throws Exception{
        User tmp = userRepository.findByEmail(TestUserDetailsHelper.TEST_HOTEL_USERNAME);
        if (tmp.getHotel() != null) {
            tmp.setHotel(null);
        }
        userRepository.save(tmp);

        when(roomRepository.save(any())).thenReturn(testRoom);
        when(hotelRepository.save(any())).thenReturn(testHotel);

        mockMvc.perform(post("/rooms/create")
                .with(csrf())           // cross site request
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", testRoom.getName())
                .param("beds", "2")
                .param("people", "4")
                .param("price", "200")
                .param("size", "40")
                .param("details", testRoom.getDetails())
                .param("avatar", testRoom.getAvatar())
        )
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Error Message")));

        verify(hotelRepository, times(0)).save(any());
        verify(roomRepository, times(0)).save(any());
    }

    @Test
    @WithUserDetails(value = "test-hotel", userDetailsServiceBeanName = "myUserDetailsService")
    public void hotelManagerDeleteRoomNoExistsShouldFailTest() throws Exception{
        User tmp = userRepository.findByEmail(TestUserDetailsHelper.TEST_HOTEL_USERNAME);
        if (tmp.getHotel() != null) {
            tmp.setHotel(null);
        }
        userRepository.save(tmp);

        when(roomRepository.findById(1l)).thenReturn(Optional.of(testRoom));
        when(roomRepository.save(any())).thenReturn(testRoom);
        when(hotelRepository.save(any())).thenReturn(testHotel);

        mockMvc.perform(get("/rooms/1/delete"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Error Message")));

        verify(hotelRepository, times(0)).save(any());
        verify(roomRepository, times(0)).delete(any());
    }

    @Test
    @WithUserDetails(value = "test-hotel", userDetailsServiceBeanName = "myUserDetailsService")
    public void hotelManagerDeleteRoomTest() throws Exception{
        User tmp = userRepository.findByEmail(TestUserDetailsHelper.TEST_HOTEL_USERNAME);
        if (tmp.getHotel() != null) {
            tmp.setHotel(null);
        }
        userRepository.save(tmp);

        when(roomRepository.findById(1l)).thenReturn(Optional.of(testRoom));
        when(roomRepository.save(any())).thenReturn(testRoom);
        when(hotelRepository.save(any())).thenReturn(testHotel);

        mockMvc.perform(get("/rooms/1/delete"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Error Message")));

        verify(hotelRepository, times(0)).save(any());
        verify(roomRepository, times(0)).delete(any());
    }

}
