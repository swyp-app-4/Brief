package com.brife.user.profile;

import com.brife.user.domain.UserInterest;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserInterestRepository extends JpaRepository<UserInterest, Long> {

    @EntityGraph(attributePaths = {"category"})
    List<UserInterest> findByUserId(Long userId);

    void deleteByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
