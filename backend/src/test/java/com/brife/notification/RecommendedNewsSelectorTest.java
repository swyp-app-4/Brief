package com.brife.notification;

import com.brife.news.dto.WidgetNewsDto;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.random.RandomGenerator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RecommendedNewsSelectorTest {

    @Test
    void selectsNewsAtGeneratedIndex() {
        RandomGenerator randomGenerator = mock(RandomGenerator.class);
        RecommendedNewsSelector selector = new RecommendedNewsSelector(randomGenerator);
        WidgetNewsDto first = WidgetNewsDto.builder().id(1L).title("첫 번째").build();
        WidgetNewsDto second = WidgetNewsDto.builder().id(2L).title("두 번째").build();
        when(randomGenerator.nextInt(2)).thenReturn(1);

        var selected = selector.select(List.of(first, second));

        assertThat(selected).contains(second);
        verify(randomGenerator).nextInt(2);
    }

    @Test
    void returnsEmptyWithoutCandidates() {
        RandomGenerator randomGenerator = mock(RandomGenerator.class);
        RecommendedNewsSelector selector = new RecommendedNewsSelector(randomGenerator);

        assertThat(selector.select(List.of())).isEmpty();
        assertThat(selector.select(null)).isEmpty();
    }
}
