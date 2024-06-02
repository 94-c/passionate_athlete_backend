package com.backend.athlete.domain.comment.repository;

import com.backend.athlete.domain.comment.model.Comment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByNoticeId(Long id);

    @EntityGraph(attributePaths = "replies")
    Optional<Comment> findById(Long id);

}
