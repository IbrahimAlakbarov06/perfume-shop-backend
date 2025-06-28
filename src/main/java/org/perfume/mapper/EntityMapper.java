package org.perfume.mapper;

public interface EntityMapper<E, D> {
    D toDto(E entity);

    E toEntity(D dto);
}