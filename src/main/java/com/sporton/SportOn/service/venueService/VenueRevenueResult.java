package com.sporton.SportOn.service.venueService;

import com.sporton.SportOn.entity.Venue;

public class VenueRevenueResult {
    private final Venue venue;
    private final Double totalRevenue;

    public VenueRevenueResult(Venue venue, Double totalRevenue) {
        this.venue = venue;
        this.totalRevenue = totalRevenue;
    }

    public Venue getVenue() {
        return venue;
    }

    public Double getTotalRevenue() {
        return totalRevenue;
    }
}

