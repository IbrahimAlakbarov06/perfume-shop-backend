package org.perfume.domain.repo;

import org.perfume.domain.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryDao extends JpaRepository<Category, Long> {
    List<Category> findAllByOrderByNameAsc();

    @Query("select distinct c from Category c where lower(c.name) like lower(concat('%', :name, '%'))")
    List<Category> findByNameContainingIgnoreCase(@Param("name") String name);

    boolean existsByName(String name);
}
