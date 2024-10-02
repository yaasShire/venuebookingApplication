package com.sporton.SportOn.controller;

import com.sporton.SportOn.entity.Region;
import com.sporton.SportOn.exception.regionException.RegionException;
import com.sporton.SportOn.model.regionModel.RegionRequest;
import com.sporton.SportOn.model.regionModel.RegionResponse;
import com.sporton.SportOn.service.regionService.RegionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/region")
@RequiredArgsConstructor
public class RegionController {
    private final RegionService regionService;

    @PostMapping("/create")
    public RegionResponse createRegion(@RequestBody RegionRequest body) throws RegionException {
        return regionService.createRegion(body);
    }

    @GetMapping("/getAll")
    public List<Region> getAllRegions() throws RegionException {
        return regionService.getAllRegions();
    }

    @PutMapping("/update/{regionId}")
    public RegionResponse updateRegion(@PathVariable Long regionId, @RequestBody RegionRequest body) throws RegionException {
        return regionService.updateRegion(regionId, body);
    }


    @DeleteMapping("/delete/{regionId}")
    public RegionResponse deleteRegion(@PathVariable Long regionId) throws RegionException {
        return regionService.deleteRegion(regionId);
    }
}
