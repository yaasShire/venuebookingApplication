package com.sporton.SportOn.controller;

import com.sporton.SportOn.entity.City;
import com.sporton.SportOn.exception.commonException.CommonException;
import com.sporton.SportOn.service.cityService.CityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cities")
@RequiredArgsConstructor
public class CityController {

    private final CityService cityService;

    @PostMapping("/register")
    public ResponseEntity<City> registerCity(@RequestParam String cityName, @RequestParam Long regionId) throws CommonException {
        City city = cityService.registerCity(cityName, regionId);
        return ResponseEntity.ok(city);
    }

    @GetMapping
    public ResponseEntity<List<City>> getAllCities() {
        List<City> cities = cityService.getAllCities();
        return ResponseEntity.ok(cities);
    }
    @GetMapping("/{regionId}")
    public ResponseEntity<List<City>> getCitiesByRegionId(
            @PathVariable Long regionId
    ) throws CommonException {
        List<City> cities = cityService.getCitiesByRegionId(regionId);
        return ResponseEntity.ok(cities);
    }
    @PutMapping("/{cityId}")
    public ResponseEntity<City> updateCity(
            @PathVariable Long cityId,
            @RequestParam String newCityName,
            @RequestParam Long newRegionId) {

        City updatedCity = cityService.updateCity(cityId, newCityName, newRegionId);
        return ResponseEntity.ok(updatedCity);
    }
    @DeleteMapping("/{cityId}")
    public ResponseEntity<Void> deleteCity(@PathVariable Long cityId) throws CommonException {
        try {
            cityService.deleteCity(cityId);
            return ResponseEntity.noContent().build();
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }
}
