package com.example.unit;

import com.example.pojo.Hotel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.Main;
import com.example.repository.HotelRepository;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Main.class)
public class HotelTest {
    
    @Autowired
    private HotelRepository hotelRepository;

    private Hotel h1;

    // Create
    // Get
    // Update
    // Delete 

    @Before
    public void initHotel(){
        h1 = new Hotel();
        h1.setName("VanHotel");
        h1.setStar(3);
        h1.setDescription("dummy");
        h1.setPhone("xxx-xxx-xxxx");
        h1.setAvatar("dummy");
        h1.setAddress("Burnaby");
        h1.setPostcode("V1A2B2");
    }

    @After
    public void cleanUpResources(){
        //clean
        Hotel hotel = hotelRepository.findByName("VanHotel");
        Hotel hotel2 = hotelRepository.findByName("updatehotel");
        if(hotel != null){
            hotelRepository.delete(hotel);
        }
        if(hotel2 != null){
            hotelRepository.delete(hotel2);
        }
    }

    @Test
    public void RetrieveTest(){

        hotelRepository.save(h1);
        //test retrieve
        Hotel h2 = hotelRepository.findByName("VanHotel");
        assertEquals(h2.getName(), "VanHotel");

    }

    @Test
    public void HotelUpdateTest(){
        //test create
        hotelRepository.save(h1);

        Hotel h2 = hotelRepository.findByName("VanHotel");
        //update hotel
        h2.setName("updatehotel");
        hotelRepository.save(h2);
        //retrieve
        Hotel h3 = hotelRepository.findByName("updatehotel");
        assertEquals("updatehotel", h3.getName());

    }
}