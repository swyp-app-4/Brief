// [레포지토리] UserInterest JPA 레포지토리. userId 기반 조회/삭제/존재 여부 확인.
package com.brife.user.repository;

import com.brife.user.domain.UserInterest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserInterestRepository extends JpaRepository<UserInterest, Long> {

    List<UserInterest> findByUserId(Long userId);

    void deleteByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
