package com.sporton.SportOn.service.regionService;

import com.sporton.SportOn.entity.Region;
import com.sporton.SportOn.exception.regionException.RegionException;
import com.sporton.SportOn.model.regionModel.RegionRequest;
import com.sporton.SportOn.model.regionModel.RegionResponse;

import java.util.List;

public interface RegionService {
    RegionResponse createRegion(RegionRequest body) throws RegionException;

    List<Region> getAllRegions() throws RegionException;

    RegionResponse updateRegion(Long regionId, RegionRequest body) throws RegionException;

    RegionResponse deleteRegion(Long regionId) throws RegionException;
}
