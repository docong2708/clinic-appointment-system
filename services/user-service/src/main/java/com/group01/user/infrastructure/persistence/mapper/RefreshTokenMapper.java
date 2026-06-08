package com.group01.user.infrastructure.persistence.mapper;

import com.group01.user.domain.aggregate.RefreshToken;
import com.group01.user.infrastructure.persistence.entity.RefreshTokenJpaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RefreshTokenMapper {
    RefreshToken toDomain(RefreshTokenJpaEntity entity);
    RefreshTokenJpaEntity toEntity(RefreshToken refreshToken);
}