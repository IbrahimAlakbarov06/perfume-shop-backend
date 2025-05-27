package org.perfume.domain.repo;

import org.perfume.domain.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BrandDao extends JpaRepository<Brand, Long> {
    Optional<Brand> findByName(String name);

    @Query("select distinct b from Brand b where lower(b.name) like lower(concat('%', :name, '%'))")
    List<Brand> findByNameContaining(@Param("name") String name);

    List<Brand> findAllByOrderByNameAsc();

    @Query("select b from Brand b where size(b.perfumes)> 0 order by b.name")
    List<Brand> findBrandsWithPerfumes();

    boolean existsByName(String name);
}