// [엔티티] 유저 관심사 매핑. category(소분류) 또는 categoryGroup(대분류) 중 하나를 가짐.
package com.brife.user.domain;

import com.brife.category.domain.Category;
import com.brife.category.domain.CategoryGroup;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_interest", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "category_id"}),
    @UniqueConstraint(columnNames = {"user_id", "category_group_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserInterest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_group_id")
    private CategoryGroup categoryGroup;
}
