package com.example.repository;


import com.example.pojo.Comment;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends CrudRepository<Comment, Long>{
    List<Comment> findAllByAuthor(String author);
}
