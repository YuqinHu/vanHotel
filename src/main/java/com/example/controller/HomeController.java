package com.example.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.management.modelmbean.ModelMBean;
import javax.servlet.http.HttpServletRequest;
import javax.swing.text.View;

import com.example.config.MyUserDetails;
import com.example.pojo.Comment;
import com.example.pojo.Hotel;
import com.example.pojo.Role;
import com.example.pojo.User;
import com.example.repository.CommentRepository;
import com.example.repository.HotelRepository;
import com.example.repository.RoleRepository;
import com.example.repository.UserRepository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class HomeController {
    
    Logger logger = LogManager.getLogger(HomeController.class);

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentRepository commentRepository;

    @GetMapping("home")
    public String dispatchUserByRole(Model model){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        MyUserDetails userDetails = (MyUserDetails) auth.getPrincipal();
        List<String> roles = new ArrayList<>();
        for (GrantedAuthority authority : userDetails.getAuthorities()) {
            roles.add(authority.getAuthority());
        }

        if (roles.contains("ADMIN")) {
            return "redirect:/home/admin";
        } else if (roles.contains("HOTEL")) {
            return "redirect:/home/hotelManager";
        } else if (roles.contains("USER")) {
            return "redirect:/home/customer";
        }

        // no valid authorities attached to the current user
        // redirect to an error page
        model.addAttribute("message", "Error in the current Logined User account: No Valid Authorities. \n Please logout and register a new Account, Thanks");
        return "error";
    }

    @GetMapping("home/customer")
    public String getCustomerHomePage(Model model, 
        @RequestParam(name = "star", required = false) Integer star
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        MyUserDetails userDetails = (MyUserDetails) auth.getPrincipal();

        User user = userRepository.findByEmail(userDetails.getUsername());
        if (user == null) {
            String errorMessage = "The User: " + userDetails.getUsername() + " does not exists!";
            model.addAttribute("message", errorMessage);
            return "error";
        }
        model.addAttribute("user", user);
        
        List<Hotel> hotels;
        if (star != null && star > 0) {
            // filter the hotel result
            hotels = hotelRepository.findAllByStar(star);
        } else {
            hotels = new ArrayList<>();
            for (Hotel hotel : hotelRepository.findAll()) {
                hotels.add(hotel);
            } 
        }
        model.addAttribute("hotels", hotels);

        // fetch all comments
        List<Comment> comments = commentRepository.findAllByAuthor(user.getEmail());
        model.addAttribute("comments", comments);
        model.addAttribute("isCommentOwner", true);
        
        return "customer/customer_home";
    }

    @GetMapping("home/hotelManager")
    public String getHotelManagerHomePage(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        MyUserDetails userDetails = (MyUserDetails) auth.getPrincipal();

        User user = userRepository.findByEmail(userDetails.getUsername());
        if (user == null) {
            String errorMessage = "The User: " + userDetails.getUsername() + " does not exists!";
            model.addAttribute("message", errorMessage);
            return "error";
        }
        model.addAttribute("user", user);

        // fetch hotel information if the manager has
        if (user.getHotel() != null) {
            model.addAttribute("isRoomOwner", true);
            model.addAttribute("hotel", user.getHotel());
            model.addAttribute("rooms", user.getHotel().getRooms());
        }

        return "hotel/hotel_home";
    }

    @GetMapping("home/admin") 
    public String getAdminHomePage(Model model,
        @RequestParam(name = "star", required = false) Integer star,
        @RequestParam(name = "userType", required = false) String userType
    ) {
        // fetch User
        List<User> users = new ArrayList<>();
        if (userType == null || userType.isEmpty() || userType.equals("ALL")) {
            // search all users
            for(User user : userRepository.findAll()) {
                users.add(user);
            }
        } else {
            // filter by user type
            for(User user : userRepository.findAll()) {
                for (Role role: user.getRoles()) {
                    if (role.getName().equals(userType)) {
                        users.add(user);
                    }
                }
            }
        }
        model.addAttribute("users", users);

        // fetch hotel
        List<Hotel> hotels;
        if (star != null) {
            // filter the hotel result
            hotels = hotelRepository.findAllByStar(star);
        } else {
            hotels = new ArrayList<>();
            for (Hotel hotel : hotelRepository.findAll()) {
                hotels.add(hotel);
            } 
        }
        model.addAttribute("hotels", hotels);

        return "admin/admin_home";
    }
}
