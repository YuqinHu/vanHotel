package com.example.controller;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.sql.DataSource;

import com.example.pojo.User;
import com.example.pojo.Role;
import com.example.repository.RoleRepository;
import com.example.repository.UserRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class UserController {
    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    private DataSource dataSource;

    // @RequestMapping("/testing")
    // @ResponseBody
    // String getAllUsers(){
    // return userRepository.findAll().toString();
    // }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "user/signup";
    }

    @PostMapping("/process_register")
    public String processRegister(Model model, @Validated User user, @RequestParam(name = "userType", required = true) String userType) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodePassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodePassword);

        //set user authorization
        // customer or hotel manager
        Set<Role> roles = new HashSet<>();
        Role customerRole = roleRepository.findByName("USER");
        Role hotelManagerRole = roleRepository.findByName("HOTEL");

        if (userType.equals("USER")) {
            roles.add(customerRole);
        } else if (userType.equals("HOTEL")) {
            roles.add(hotelManagerRole);
        } else {
            // error message
            String errorMessage = "You can not register a User other than Customer and Hotel Manager";
            model.addAttribute("message", errorMessage);
            return "error";
        }
        user.setRoles(roles);

        // error handling
        // if user already exists
        User checkUser = userRepository.findByEmail(user.getEmail());
        if (checkUser != null) {
            // user already exists
            model.addAttribute("message", "Error: The user " + user.getEmail() + " already exists. Please try with another email address");
            return "error";
        }

        userRepository.save(user);
        return "user/register_success";
    }


    @GetMapping("/users")
    String getAllUsers(Model model) {
        ArrayList<User> users = new ArrayList<>();
        for(User x : userRepository.findAll()) {
            users.add(x);
        }
        model.addAttribute("records", users);
        return "user/user-list";
    }

    @GetMapping("/users/{id}")
    String getUserDataById(@PathVariable(name = "id") long id, Model model) {
        
        if (!userRepository.findById(id).isPresent()) {
            model.addAttribute("message", "User with id: " + id + " does not exist.");
            return "error";
        }
        User user = userRepository.findById(id).get();

        // TODO: add user detail page
        model.addAttribute("user", user);
        model.addAttribute("message", "User with id: " + id + "\n" + user.toString());
        return "message";
    }

    @RequestMapping("/users/delete/{id}")
    public String deleteUserById(@PathVariable(name = "id") long id) {
        System.out.println(id);

        try(Connection connection = dataSource.getConnection()){
            Statement statement = connection.createStatement();
            int newid = (int)id;
            String sql = "DELETE FROM users_roles WHERE user_id=" + newid;
            statement.executeUpdate(sql);
        }catch (Exception e){
            return "error";
        }
        userRepository.deleteById(id);
        return "user/delete_success";
    }

}