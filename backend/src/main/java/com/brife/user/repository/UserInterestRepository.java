package com.brife.user.repository;

import com.brife.user.domain.UserInterest;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserInterestRepository extends JpaRepository<UserInterest, Long> {

    @EntityGraph(attributePaths = {"category", "categoryGroup"})
    List<UserInterest> findByUserId(Long userId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM UserInterest ui WHERE ui.user.id = :userId")
    void deleteByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
