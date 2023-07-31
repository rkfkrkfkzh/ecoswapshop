package shop.ecoswapshop.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Category {

    @Id
    @GeneratedValue
    @Column(name = "category_id")
    private Long id; // 카테고리 번호

    private String categoryName; // 카테고리 이름

    // 카테고리와 상품 테이블 간의 일대다 관계 설정
    @OneToMany(mappedBy = "category")
    private List<Product> productList = new ArrayList<>(); // 상품리스트

    public Category() {
    }
}
