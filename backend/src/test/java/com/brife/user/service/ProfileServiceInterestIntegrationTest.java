package com.brife.user.service;

import com.brife.user.dto.InterestRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.task.scheduling.enabled=false")
class ProfileServiceInterestIntegrationTest {

    @Autowired private ProfileService profileService;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private CacheManager cacheManager;

    @Test
    @Transactional
    void repeatedResetKeepsSingleRowAndEvictsRecommendationCache() {
        Fixture fixture = createFixture();
        Cache cache = cacheManager.getCache("top5News");
        assertThat(cache).isNotNull();
        cache.put(fixture.userId(), List.of("cached"));
        InterestRequest duplicateRequest = new InterestRequest(
                List.of(fixture.firstCategoryId(), fixture.firstCategoryId()),
                List.of());

        profileService.resetInterests(fixture.userId(), duplicateRequest);
        profileService.resetInterests(fixture.userId(), duplicateRequest);

        assertThat(countInterests(fixture.userId())).isEqualTo(1);
        assertThat(cache.get(fixture.userId())).isNull();

        profileService.resetInterests(
                fixture.userId(),
                new InterestRequest(List.of(fixture.secondCategoryId()), List.of()));

        assertThat(countInterests(fixture.userId())).isEqualTo(1);
        assertThat(currentCategoryId(fixture.userId())).isEqualTo(fixture.secondCategoryId());
    }

    @Test
    void concurrentIdenticalRequestsRemainIdempotent() throws Exception {
        Fixture fixture = createFixture();
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        InterestRequest request = new InterestRequest(
                List.of(fixture.firstCategoryId(), fixture.firstCategoryId()),
                List.of());

        try {
            Future<?> first = executor.submit(() -> resetAfterStart(start, fixture.userId(), request));
            Future<?> second = executor.submit(() -> resetAfterStart(start, fixture.userId(), request));
            start.countDown();

            first.get();
            second.get();

            assertThat(countInterests(fixture.userId())).isEqualTo(1);
            assertThat(currentCategoryId(fixture.userId())).isEqualTo(fixture.firstCategoryId());
        } finally {
            executor.shutdownNow();
            deleteFixture(fixture);
        }
    }

    private void resetAfterStart(CountDownLatch start, Long userId, InterestRequest request) {
        try {
            start.await();
            profileService.resetInterests(userId, request);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("test interrupted", e);
        }
    }

    private Fixture createFixture() {
        String suffix = UUID.randomUUID().toString();
        Long groupId = jdbcTemplate.queryForObject(
                "INSERT INTO category_group(name) VALUES (?) RETURNING id",
                Long.class,
                "interest-test-group-" + suffix);
        Long firstCategoryId = insertCategory(groupId, "interest-test-first-" + suffix);
        Long secondCategoryId = insertCategory(groupId, "interest-test-second-" + suffix);
        Long userId = jdbcTemplate.queryForObject("""
                        INSERT INTO app_user(
                            email, provider, provider_id, role,
                            service_terms_agreed, privacy_terms_agreed, created_at)
                        VALUES (?, 'test', ?, 'ROLE_USER', true, true, NOW())
                        RETURNING id
                        """,
                Long.class,
                "interest-" + suffix + "@test.local",
                "interest-provider-" + suffix);
        return new Fixture(userId, groupId, firstCategoryId, secondCategoryId);
    }

    private Long insertCategory(Long groupId, String name) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO category(category_group_id, name, query) VALUES (?, ?, ?) RETURNING id",
                Long.class,
                groupId,
                name,
                name);
    }

    private int countInterests(Long userId) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_interest WHERE user_id = ?",
                Integer.class,
                userId);
    }

    private Long currentCategoryId(Long userId) {
        return jdbcTemplate.queryForObject(
                "SELECT category_id FROM user_interest WHERE user_id = ?",
                Long.class,
                userId);
    }

    private void deleteFixture(Fixture fixture) {
        jdbcTemplate.update("DELETE FROM user_interest WHERE user_id = ?", fixture.userId());
        jdbcTemplate.update("DELETE FROM app_user WHERE id = ?", fixture.userId());
        jdbcTemplate.update("DELETE FROM category WHERE id IN (?, ?)",
                fixture.firstCategoryId(), fixture.secondCategoryId());
        jdbcTemplate.update("DELETE FROM category_group WHERE id = ?", fixture.groupId());
    }

    private record Fixture(Long userId, Long groupId, Long firstCategoryId, Long secondCategoryId) {
    }
}
