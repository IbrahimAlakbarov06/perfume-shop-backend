package org.perfume.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MostAddedProductResponse {
    private MostPerfumesResponse perfume;
    private Long addedCount;
    private String popularity;
}
