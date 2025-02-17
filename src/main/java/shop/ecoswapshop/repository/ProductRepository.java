package shop.ecoswapshop.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import shop.ecoswapshop.domain.Category;
import shop.ecoswapshop.domain.Product;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // 상품 제목과 내용에 해당하는 문자열을 포함하는 상품들 조회
    Page<Product> findByProductNameContaining(String productName, Pageable pageable);

    // 상품 삭제
    void deleteById(Long id);

    // 페이징 처리를 위한 메서드
    Page<Product> findAll(Pageable pageable);

    // 카테고리 조회
    Page<Product> findByCategory(Category category, Pageable pageable);

    Page<Product> findByMemberId(Long memberId, Pageable pageable);
}
