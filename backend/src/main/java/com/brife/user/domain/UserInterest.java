// [엔티티] 유저-카테고리 관심사 매핑 (user_id + category_id 복합 유니크).
package com.brife.user.domain;

import com.brife.category.domain.Category;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_interest", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "category_id"})
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
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
}
