package com.example.unit;

import com.example.pojo.User;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.Main;
import com.example.repository.UserRepository;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Main.class)
public class UserTest {
    
    @Autowired
    private UserRepository userRepository;

    private User user1;

    // Create
    // Get
    // Update
    // Delete 

    @Before
    public void initUser(){
        //clean
        User user = userRepository.findByEmail("unit-test1@sfu.ca");
        if (user != null) {
            userRepository.delete(user);
        }
        user1 = new User();
        user1.setEmail("unit-test1@sfu.ca");
        user1.setPassword("666666");
        user1.setFirstName("test1");
        user1.setLastName("Test1");
    }

    @After
    public void cleanUpResources(){
        //clean
        User user = userRepository.findByEmail("unit-test1@sfu.ca");
        userRepository.delete(user);
    }

    @Test
    public void UserCreateAndRetrieveTest(){

        userRepository.save(user1);

        //test retrieve
        User user2 = userRepository.findByEmail("unit-test1@sfu.ca");
        assertEquals(user2.getFirstName(), "test1");
        assertEquals(user2.getLastName(), "Test1");

    }

    @Test
    public void UserCreateAndUpdateTest(){
        
        //test create
        userRepository.save(user1);

        User user2 = userRepository.findByEmail("unit-test1@sfu.ca");

        //update user
        user2.setFirstName("test1-update");
        userRepository.save(user2);

        //retrieve
        User user3 = userRepository.findByEmail("unit-test1@sfu.ca");
        assertEquals("test1-update", user3.getFirstName());

    }
}
