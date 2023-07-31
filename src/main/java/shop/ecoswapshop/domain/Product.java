package shop.ecoswapshop.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter
@Setter
public class Product {

    @Id
    @GeneratedValue
    @Column(name = "product_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member memberId;

    private String productName; // 상품명
    private String productDescription; // 상품 설명
    private int price; // 상품가격

    @Enumerated(EnumType.STRING)
    private Condition productCondition; //상품 상태


//    private Category category; // 카테고리

    private LocalDateTime creationDate; // 등록일

}