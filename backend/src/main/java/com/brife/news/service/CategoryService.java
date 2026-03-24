package com.brife.news.service;

import com.brife.news.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private CategoryRepository categoryRepository;

    // 온보딩 화면 조회 시 카테고리 6개를 반환
}