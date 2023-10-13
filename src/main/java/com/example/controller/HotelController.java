package com.example.controller;

import java.util.ArrayList;
import java.util.List;

import com.example.config.MyUserDetails;
import com.example.pojo.Hotel;
import com.example.pojo.User;
import com.example.repository.HotelRepository;
import com.example.repository.UserRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class HotelController {

    Logger logger = LogManager.getLogger(HotelController.class);

    @Autowired
    HotelRepository hotelRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    GeoApiContext geoApiContext;

    @GetMapping("/hotels/create")
    public String getHotelCreationForm(Model model) {
        model.addAttribute("hotel", new Hotel());
        return "hotel/create_hotel";
    }

    @PostMapping("/hotels/create")
    public String processHotelCreation(Model model, @Validated Hotel hotel) {
        logger.info("Start Processing Hotel Creation. Hotel Name: " + hotel.getName());
        // trim the input
        hotel.setAddress(hotel.getAddress().trim());
        hotel.setName(hotel.getName().trim());
        hotel.setPostcode(hotel.getPostcode().trim());
        hotel.setAvatar(hotel.getAvatar().trim());
        hotel.setPhone(hotel.getPhone().trim());

        // current user must be a Hotel Manager, and does not have a managed hotel
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        MyUserDetails userDetails = (MyUserDetails) auth.getPrincipal();
        List<String> roles = new ArrayList<>();
        for (GrantedAuthority authority : userDetails.getAuthorities()) {
            roles.add(authority.getAuthority());
        }
        if (!roles.contains("HOTEL")) {
            String errorMessage = "The User: " + userDetails.getUsername() + " does not have permission to create a Hotel record";
            model.addAttribute("message", errorMessage);
            return "error";
        }

        User user = userRepository.findByEmail(userDetails.getUsername());
        if (user == null) {
            String errorMessage = "The User: " + userDetails.getUsername() + " does not exists!";
            model.addAttribute("message", errorMessage);
            return "error";
        }
        if (user.getHotel() != null) {
            // already have managed hotel
            String errorMessage = "The Hotel Manager: " + userDetails.getUsername() + " already has a managed hotel. Cannot create another one";
            model.addAttribute("message", errorMessage);
            return "error";
        }


        Hotel checkHotel = hotelRepository.findByName(hotel.getName());
        if (checkHotel != null) {
            // hotel already exists
            String errorMessage = "Hotel: " + hotel.getName() + " already exists. Please create with another name.";
            logger.error(errorMessage);
            model.addAttribute("message", errorMessage);
            return "error";
        }
        hotel = hotelRepository.save(hotel);

        // update the one-to-one mapping
        user.setHotel(hotel);
        userRepository.save(user);


        String successMessage = "Hotel Creation Succeeded!";
        logger.info(successMessage);
        model.addAttribute("message", successMessage);

        return "message";
    }

    @GetMapping("/hotels")
    String getAllHotels(Model model) {
        ArrayList<Hotel> hotels = new ArrayList<>();
        for(Hotel x : hotelRepository.findAll()) {
            hotels.add(x);
        }
        model.addAttribute("hotels", hotels);
        return "hotel/hotel_list";
    }

    @GetMapping("/hotels/{id}")
    String getHotelDataById(Model model, @PathVariable(name = "id") long id) {
        
        // fetch hotel
        if (!hotelRepository.findById(id).isPresent()) {
            model.addAttribute("message", "Error: Hotel with id: " + id + " does not exist.");
            return "error";
        }
        Hotel hotel = hotelRepository.findById(id).get();
        model.addAttribute("hotel", hotel);
        model.addAttribute("comments", hotel.getComments());
        model.addAttribute("rooms", hotel.getRooms());

        // get geo location from google maps api
        // Synchronous
        try {
            GeocodingResult[] results =  GeocodingApi.geocode(
                geoApiContext, hotel.getAddress()
            ).await();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            // only for logging
            logger.info("hotel lat: " + results[0].geometry.location.lat);
            logger.info("hotel lng: " + results[0].geometry.location.lng);

            model.addAttribute("lat", results[0].geometry.location.lat);
            model.addAttribute("lng", results[0].geometry.location.lng);

        } catch (Exception e) {
            // Handle error
            e.printStackTrace();
        }

        // setup heading
        model.addAttribute("heading", hotel.getName());
        model.addAttribute("subHeading", hotel.getAddress());
        return "hotel/hotel_details";
    }

}