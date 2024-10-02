package com.sporton.SportOn.model.bookingModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@Builder
public class ProviderOrdersResponse {
    private int pendingOrdersCount;
    private List<ProviderOrderResponseDTO> orders;

    public ProviderOrdersResponse(int pendingOrdersCount, List<ProviderOrderResponseDTO> orders) {
        this.pendingOrdersCount = pendingOrdersCount;
        this.orders = orders;
    }

    // Getters and Setters
}