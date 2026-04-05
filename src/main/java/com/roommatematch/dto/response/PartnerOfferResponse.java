package com.roommatematch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerOfferResponse {

    private Long id;

    private String title;

    private String description;

    private Integer discountPercent;

    private LocalDate validUntil;

    private boolean isActive;

    private String partnerName;
}
