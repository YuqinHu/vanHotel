package com.example.controller;

import java.util.*;

import com.example.config.MyUserDetails;
import com.example.pojo.Comment;
import com.example.pojo.Hotel;
import com.example.repository.CommentRepository;
import com.example.repository.HotelRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class CommentController {
    
    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @GetMapping("/comments")
    public String getAllComments(Model model) {

        Iterable<Comment> comments = commentRepository.findAll();
        model.addAttribute("records", comments);
        
        return "comment/comment_list";
    }

    @GetMapping("/comments/{id}")
    @ResponseBody
    public String getCommentById(@PathVariable(name = "id") long id, Model model) {
        if (!commentRepository.findById(id).isPresent()) {
            model.addAttribute("message", "The Commit: " + id + " does not exist!");
        }
        Comment comment = commentRepository.findById(id).get();
        
        return "Comment " + id + " Details: " + comment.toString();
    }

    // create comment
    @GetMapping("/comments/create")
    public String getCommentCreatePage(Model model) {
        model.addAttribute("comment", new Comment());

        return "comment/create_comment";
    }

    @PostMapping("/comments/create")
    public String processCommentCreation(Model model, Comment comment) {
        String hotelName = comment.getHotelName();
        if (hotelName == null) {
            model.addAttribute("message", "Error: Missing parameter hotelName.");
            return "error";
        }
        hotelName = hotelName.trim();

        Hotel targetHotel = hotelRepository.findByName(hotelName);
        if (targetHotel == null) {
            model.addAttribute("message", "Error: Invalid hotel Name.");
            return "error";
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        MyUserDetails userDetails = (MyUserDetails) auth.getPrincipal();
        comment.setCreateTime(new Date());
        comment.setAuthor(userDetails.getUsername());
        comment.setHotelName(hotelName);
        comment = commentRepository.save(comment);

        Set<Comment> comments = targetHotel.getComments();
        comments.add(comment);
        hotelRepository.save(targetHotel);

        model.addAttribute("message", "Comment Creation Succeeded!");
        return "message";
    }


    // delete comment
    @GetMapping("/comments/{id}/delete")
    public String deleteComment(Model model, @PathVariable(name = "id") long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        MyUserDetails userDetails = (MyUserDetails) auth.getPrincipal();
        List<String> roles = new ArrayList<>();
        for (GrantedAuthority authority : userDetails.getAuthorities()) {
            roles.add(authority.getAuthority());
        }

        if (!commentRepository.findById(id).isPresent()) {
            model.addAttribute("message", "Error: Comment " + id + " does not exist");
            return "error";
        }
        Comment targetComment = commentRepository.findById(id).get();
        if (!roles.contains("ADMIN") && !targetComment.getAuthor().equals(userDetails.getUsername())) {
            // not owner
            model.addAttribute("message", "Error: You do not have permission to delete Comment " + id + ", which is not owned by you");
            return "error";
        }

        Hotel targetHotel = hotelRepository.findByName(targetComment.getHotelName());
        if (targetHotel != null) {
            Set<Comment> comments = targetHotel.getComments();    
            for (Comment c: comments) {
                if (c.getId() == targetComment.getId()) {
                    comments.remove(c);
                    break;
                }
            }
        }
        hotelRepository.save(targetHotel);

        commentRepository.delete(targetComment);
        model.addAttribute("message", "Comment Deletion Succeeded!");
        return "message";
    
    }
    
    
}
