package com.example.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.example.pojo.Comment;
import com.example.pojo.Hotel;
import com.example.pojo.User;
import com.example.repository.CommentRepository;
import com.example.repository.HotelRepository;
import com.example.repository.UserRepository;

import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CommentTest {
    
    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private HotelRepository hotelRepository;

    private Comment c1;
    private Comment c2;
    private Hotel testHotel;

    private void insertCommentToHotel(Comment c, Hotel h) {
        Set<Comment> comments = h.getComments();
        comments.add(c);
        hotelRepository.save(h);
    }

    @Before
    public void setUpTestComment() {
        c1 = new Comment();
        c1.setContent("this is comment 1");
        c1.setCreateTime(new Date());
        c1.setAuthor("Test User");
        c1.setHotelName("test-hotel");
        c1 = commentRepository.save(c1);

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
        
        insertCommentToHotel(c1, testHotel);

    }

    @After 
    public void clearUpResources() {
        testHotel = hotelRepository.findByName("test-hotel");
        if (testHotel != null) {
            hotelRepository.delete(testHotel);
        }

        if (c1 != null && commentRepository.findById(c1.getId()).isPresent()) {
            commentRepository.delete(c1);
        }
        
        if (c2 != null && commentRepository.findById(c2.getId()).isPresent()) {
            commentRepository.delete(c2);
        }
    }

    @Test
    public void repositoryCreationSuccessTest() {
        commentRepository.findAll().forEach((comment) -> {
            System.out.println(comment.getContent());
        });

    }
    
    @Test
    public void createAndDeleteTest() {

        // validate comment
        Comment c2 = commentRepository.findById(c1.getId()).get();
        assertEquals("this is comment 1", c2.getContent());
        assertEquals("Test User", c2.getAuthor());

        testHotel.setComments(new HashSet<>());
        hotelRepository.save(testHotel);

        commentRepository.delete(c1);
    }

    @Test
    public void createAndValidateHotelTest() {

        // validate hotel has the new comment
        testHotel = hotelRepository.findByName("test-hotel");
        boolean containsFlag = false;
        for (Comment c: testHotel.getComments()) {
            if (c.getId() == c1.getId()) {
                containsFlag = true;
            }   
        }
        assertTrue(containsFlag);

        // delete comment
        testHotel.setComments(new HashSet<>());
        hotelRepository.save(testHotel);
        commentRepository.delete(c1);

        // validate hotel also delete the comment
        testHotel = hotelRepository.findByName("test-hotel");
        containsFlag = false;
        for (Comment c: testHotel.getComments()) {
            if (c.getId() == c1.getId()) {
                containsFlag = true;
            }   
        }
        assertFalse(containsFlag);
    }

    @Test
    public void createMultipleCommentsTest() {

        // insert another comment
        c2 = new Comment();
        c2.setContent("this is comment 2");
        c2.setCreateTime(new Date());
        c2.setAuthor("Test User 2");
        c2.setHotelName("test-hotel");
        c2 = commentRepository.save(c2);

        testHotel = hotelRepository.findByName("test-hotel");
        insertCommentToHotel(c2, testHotel);
        
        // validate hotel
        testHotel = hotelRepository.findByName("test-hotel");
        assertEquals(2, testHotel.getComments().size());

    }


}
