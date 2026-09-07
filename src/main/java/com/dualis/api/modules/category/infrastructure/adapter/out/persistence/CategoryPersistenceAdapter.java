package com.dualis.api.modules.category.infrastructure.adapter.out.persistence;

import com.dualis.api.modules.category.domain.model.Category;
import com.dualis.api.modules.category.domain.repository.CategoryRepositoryPort;
import com.dualis.api.modules.category.infrastructure.adapter.out.persistence.entity.CategoryJpaEntity;
import com.dualis.api.modules.category.infrastructure.adapter.out.persistence.mapper.CategoryPersistenceMapper;
import com.dualis.api.modules.category.infrastructure.adapter.out.persistence.repository.SpringDataCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CategoryPersistenceAdapter implements CategoryRepositoryPort {

    private final SpringDataCategoryRepository springDataCategoryRepository;
    private final CategoryPersistenceMapper mapper;

    @Override
    public Category save(Category category) {
        CategoryJpaEntity entity = mapper.toEntity(category);
        CategoryJpaEntity saved = springDataCategoryRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Category> findById(UUID id) {
        return springDataCategoryRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Category> findByWorkspaceIdOrSystemDefault(UUID workspaceId) {
        return springDataCategoryRepository.findByWorkspaceIdOrSystemDefault(workspaceId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void delete(UUID id) {
        springDataCategoryRepository.deleteById(id);
    }
}
