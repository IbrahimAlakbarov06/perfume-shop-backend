package org.perfume.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FavoriteResponse {

    private Long id;

    private String userName;

    private PerfumeResponse perfume;

    private LocalDateTime createdAt;
}
