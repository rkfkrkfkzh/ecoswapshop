package shop.ecoswapshop.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.ecoswapshop.domain.Category;
import shop.ecoswapshop.repository.CategoryRepository;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    @Transactional
    public Category save(Category category) {
        return categoryRepository.save(category);
    }
}