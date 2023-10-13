package com.example.repository;


import java.util.List;

import com.example.pojo.Room;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends CrudRepository<Room, Long>{
    List<Room> findAllByHotelName(String hotelName);
}
