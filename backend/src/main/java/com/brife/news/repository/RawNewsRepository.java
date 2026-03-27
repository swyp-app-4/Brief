package com.brife.news.repository;

import com.brife.news.domain.RawNews;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface RawNewsRepository extends JpaRepository<RawNews, Long> {

    @Query("SELECT r.naverUrl FROM RawNews r WHERE r.naverUrl IN :urls")
    Set<String> findExistingNaverUrls(@Param("urls") List<String> urls);
}
