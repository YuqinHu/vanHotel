package com.example.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.example.config.MyUserDetails;
import com.example.pojo.*;
import com.example.repository.HotelRepository;
import com.example.repository.RoomRepository;

import com.example.repository.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class RoomController {
    Logger logger = LogManager.getLogger(RoomController.class);

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/rooms")
    public String getAllRoomsByHotelId(Model model, @RequestParam(name = "hotelName", required = true) String hotelName) {

        Iterable<Room> rooms = roomRepository.findAllByHotelName(hotelName);
        model.addAttribute("records", rooms);
        
        return "room/room_list";
    }

    @GetMapping("/rooms/{id}")
    @ResponseBody
    public String getRoomById(@PathVariable(name = "id") long id, Model model) {
        if (!roomRepository.findById(id).isPresent()) {
            model.addAttribute("message", "The Room: " + id + " does not exist!");
            return "error";
        }
        Room room = roomRepository.findById(id).get();
        
        return "Room " + id + " Details: " + room.toString();
    }

    // create room
    // Authority: HOTEL or ADMIN
    @GetMapping("/rooms/create")
    public String getRoomCreatePage(Model model) {
        logger.info("Start getRoomCreatePage");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        MyUserDetails userDetails = (MyUserDetails) auth.getPrincipal();
        List<String> roles = new ArrayList<>();
        for (GrantedAuthority authority : userDetails.getAuthorities()) {
            roles.add(authority.getAuthority());
        }

        Room room = new Room();
        model.addAttribute("room", room);
        if (roles.contains("ADMIN")) {
            model.addAttribute("isAdmin", true);
        } else {
            model.addAttribute("isAdmin", false);

            User user = userRepository.findByEmail(userDetails.getUsername());
            if (user.getHotel() == null) {
                String errorMessage = "You do not have a managed hotel yet. Please go to your home page and create a managed hotel first";
                logger.error(errorMessage);
                model.addAttribute("message", errorMessage);
                return "error";
            }

            model.addAttribute("hotelName", user.getHotel().getName());
        }

        return "room/create_room";
    }

     @PostMapping("/rooms/create")
     public String processRoomCreation(Model model, Room room, @RequestParam(name = "hotelName", required = false) String hotelName) {
         Authentication auth = SecurityContextHolder.getContext().getAuthentication();
         MyUserDetails userDetails = (MyUserDetails) auth.getPrincipal();
         List<String> roles = new ArrayList<>();
         for (GrantedAuthority authority : userDetails.getAuthorities()) {
             roles.add(authority.getAuthority());
         }

         // trim the input
         room.setAvatar(room.getAvatar().trim());
         room.setName(room.getName().trim());
         room.setDetails(room.getDetails().trim());

         if (roles.contains("ADMIN")) {
             // admin
             if (hotelName == null || hotelRepository.findByName(hotelName) == null) {
                 String errorMessage = "Hello Administrator: " + userDetails.getUsername() + ", Please assign a valid Hotel name to this new Room.";
                 logger.error(errorMessage);
                 model.addAttribute("message", errorMessage);
                 return "error";
             }
             Hotel hotel = hotelRepository.findByName(hotelName);

             // process room creation
             room.setHotelName(hotel.getName());
             room = roomRepository.save(room);

             // add room to hotel
             hotel = addRoomToHotel(room, hotel);

             String successMessage = "Create Room: " + room.getName() + " in Hotel: " + hotel.getName() + " success.";
             logger.info(successMessage);
             model.addAttribute("message", successMessage);
             return "message";


         } else if (roles.contains("HOTEL")) {
             // Hotel Manager
             User user = userRepository.findByEmail(userDetails.getUsername());
             if (user.getHotel() == null) {
                 String errorMessage = "You do not have a managed hotel yet. Please go to your home page and create a managed hotel first";
                 logger.error(errorMessage);
                 model.addAttribute("message", errorMessage);
                 return "error";
             }
             Hotel hotel = user.getHotel();

             // process room creation
             room.setHotelName(hotel.getName());
             room = roomRepository.save(room);

             // add room to hotel
             hotel = addRoomToHotel(room, hotel);

             String successMessage = "Create Room: " + room.getName() + " in Hotel: " + hotel.getName() + " success.";
             logger.info(successMessage);
             model.addAttribute("message", successMessage);
             return "message";

         } else {
             // no permission
             String errorMessage = "You do not have the permission to create a Room.";
             logger.error(errorMessage);
             model.addAttribute("message", errorMessage);
             return "error";
         }

     }

     private Hotel addRoomToHotel(Room room, Hotel hotel) {
         // add room to hotel
         Set<Room> rooms = hotel.getRooms();
         rooms.add(room);
         hotel.setRooms(rooms);
         hotel = hotelRepository.save(hotel);

         return hotel;
     }

    // delete room
    @GetMapping("/rooms/{id}/delete")
    public String deleteRoom(Model model, @PathVariable(name = "id") long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        MyUserDetails userDetails = (MyUserDetails) auth.getPrincipal();
        List<String> roles = new ArrayList<>();
        for (GrantedAuthority authority : userDetails.getAuthorities()) {
            roles.add(authority.getAuthority());
        }
        User user = userRepository.findByEmail(userDetails.getUsername());

        
        if (!roomRepository.findById(id).isPresent()) {
            model.addAttribute("message", "Error: Room " + id + " does not exist");
            return "error";
        }
        Room targetRoom = roomRepository.findById(id).get();
        if (!roles.contains("ADMIN") &&
                (user.getHotel() == null || !targetRoom.getHotelName().equals(user.getHotel().getName())) ) {
            // correct owner
            model.addAttribute("message", "Error: You do not have permission to delete Room " + id + ", which is not owned by you");
            return "error";
        }

        Hotel targetHotel = hotelRepository.findByName(targetRoom.getHotelName());
        if (targetHotel != null) {
            Set<Room> rooms = targetHotel.getRooms();
            for (Room room: rooms) {
                if (room.getId() == targetRoom.getId()) {
                    rooms.remove(room);
                    break;
                }
            }
        }
        hotelRepository.save(targetHotel);

        roomRepository.delete(targetRoom);
        model.addAttribute("message", "Room " + targetRoom.getId() + " Deletion Succeeded!");
        return "message";

    }
    
}
