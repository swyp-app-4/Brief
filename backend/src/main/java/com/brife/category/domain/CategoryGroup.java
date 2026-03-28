// [엔티티] 대분류 카테고리 그룹 (시사/정치, IT/테크 등 6개).
package com.brife.category.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "category_group")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CategoryGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
}
