package com.brife.news.controller;

import com.brife.news.dto.WidgetNewsDto;
import com.brife.news.service.NewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "홈", description = "홈 화면 뉴스 추천")
@RestController
@RequestMapping("/home/news")
@RequiredArgsConstructor
public class HomeController {

    private final NewsService newsService;

    @Operation(summary = "관심사 기반 추천 뉴스", description = "JWT로 인증된 유저의 관심 카테고리 기준 Top5 뉴스 반환")
    @GetMapping("/recommended")
    public ResponseEntity<List<WidgetNewsDto>> getRecommendedNews(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(newsService.getRecommendedNews(userId));
    }
}
