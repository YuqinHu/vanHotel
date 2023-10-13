package com.example.repository;

import java.util.List;

import com.example.pojo.Hotel;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HotelRepository extends CrudRepository<Hotel, Long>{
     Hotel findByName(String name);
     List<Hotel> findAllByStar(int star);
     List<Hotel> findAllByQuarantine(boolean quarantine);
}