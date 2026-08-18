package com.brife.notification;

import com.brife.news.dto.WidgetNewsDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.random.RandomGenerator;

@Component
public class RecommendedNewsSelector {

    private final RandomGenerator randomGenerator;

    public RecommendedNewsSelector() {
        this(RandomGenerator.getDefault());
    }

    RecommendedNewsSelector(RandomGenerator randomGenerator) {
        this.randomGenerator = randomGenerator;
    }

    public Optional<WidgetNewsDto> select(List<WidgetNewsDto> recommendations) {
        if (recommendations == null || recommendations.isEmpty()) {
            return Optional.empty();
        }

        int selectedIndex = randomGenerator.nextInt(recommendations.size());
        return Optional.of(recommendations.get(selectedIndex));
    }
}
