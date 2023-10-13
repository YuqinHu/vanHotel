package com.example.unit;

import com.example.pojo.Hotel;
import com.example.pojo.Room;
import com.example.repository.HotelRepository;
import com.example.repository.RoomRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RoomTest {
    
    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private HotelRepository hotelRepository;

    private Room r1;
    private Room r2;
    private Hotel testHotel;

    private void insertRoomToHotel(Room room, Hotel hotel) {
        Set<Room> rooms = hotel.getRooms();
        rooms.add(room);
        hotelRepository.save(hotel);
    }

    @Before
    public void setUpTestRoom() {
        r1 = new Room();
        r1.setName("Test Room 1");
        r1.setAvatar("dummy");
        r1.setDetails("dummy");
        r1.setBeds(2);
        r1.setPeople(2);
        r1.setPrice(100);
        r1.setSize(38);
        r1.setHotelName("test-hotel");
        r1 = roomRepository.save(r1);

        testHotel = hotelRepository.findByName("test-hotel");
        if (testHotel == null) {
            testHotel = new Hotel();
            testHotel.setName("test-hotel");
            testHotel.setDescription("dummy");
            testHotel.setQuarantine(true);
            testHotel.setAddress("dummy address");
            testHotel.setPhone("xxx-xxx-xxxx");
            testHotel.setPostcode("XXX000");
            testHotel.setStar(4);
            testHotel = hotelRepository.save(testHotel);
        }
        
        insertRoomToHotel(r1, testHotel);
    }

    @After 
    public void clearUpResources() {
        testHotel = hotelRepository.findByName("test-hotel");
        if (testHotel != null) {
            hotelRepository.delete(testHotel);
        }

        if (r1 != null && roomRepository.findById(r1.getId()).isPresent()) {
            roomRepository.delete(r1);
        }
        
        if (r2 != null && roomRepository.findById(r2.getId()).isPresent()) {
            roomRepository.delete(r2);
        }
    }

    @Test
    public void repositoryCreationSuccessTest() {
        roomRepository.findAll().forEach((room) -> {
            assertTrue(room.getId() > 0);
        });
    }
    
    @Test
    public void createAndDeleteTest() {

        // validate room
        Room r2 = roomRepository.findById(r1.getId()).get();
        assertEquals("Test Room 1", r2.getName());
        assertEquals("test-hotel", r2.getHotelName());

        testHotel.setRooms(new HashSet<>());
        hotelRepository.save(testHotel);

        roomRepository.delete(r1);
        assertFalse(roomRepository.findById(r1.getId()).isPresent());
    }

    @Test
    public void createAndValidateHotelTest() {

        // validate hotel has the new room
        testHotel = hotelRepository.findByName("test-hotel");
        boolean containsFlag = false;
        for (Room room: testHotel.getRooms()) {
            if (room.getId() == r1.getId()) {
                containsFlag = true;
            }   
        }
        assertTrue(containsFlag);

        // delete rooms
        testHotel.setRooms(new HashSet<>());
        hotelRepository.save(testHotel);
        roomRepository.delete(r1);

        // validate hotel also delete the room
        testHotel = hotelRepository.findByName("test-hotel");
        containsFlag = false;
        for (Room room: testHotel.getRooms()) {
            if (room.getId() == r1.getId()) {
                containsFlag = true;
            }   
        }
        assertFalse(containsFlag);
    }

    @Test
    public void createMultipleRoomsTest() {

        // insert another room
        r2 = new Room();
        r2.setName("Test Room 2");
        r2.setAvatar("dummy");
        r2.setDetails("dummy");
        r2.setBeds(2);
        r2.setPeople(3);
        r2.setPrice(200);
        r2.setSize(33);
        r2.setHotelName("test-hotel");
        r2 = roomRepository.save(r2);

        testHotel = hotelRepository.findByName("test-hotel");
        insertRoomToHotel(r2, testHotel);
        
        // validate hotel
        testHotel = hotelRepository.findByName("test-hotel");
        assertEquals(2, testHotel.getRooms().size());

        boolean containsFlag = false;
        for (Room room : testHotel.getRooms()) {
            if (room.getId() == r2.getId()) {
                containsFlag = true;
            }
        }
        assertTrue(containsFlag);
    }

}
