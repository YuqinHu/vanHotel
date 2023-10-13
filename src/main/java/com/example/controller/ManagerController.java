package com.example.controller;

import java.util.ArrayList;

import com.example.pojo.User;
import com.example.repository.UserRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ManagerController {
    @Autowired
    UserRepository userRepository;

    @RequestMapping("/manager")
    String getAllUsers(Model model) {
        ArrayList<User> users = new ArrayList<>();
        for(User x : userRepository.findAll()) {
            users.add(x);
        }
        model.addAttribute("records", users);
        return "user/user_list";
    }
}
