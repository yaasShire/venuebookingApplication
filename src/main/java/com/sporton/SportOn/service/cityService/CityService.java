package com.sporton.SportOn.service.cityService;

import com.sporton.SportOn.entity.City;

import com.sporton.SportOn.exception.commonException.CommonException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CityService {
     City registerCity(String cityName, Long regionId) throws CommonException;

    City updateCity(Long cityId, String newCityName, Long newRegionId);

    List<City> getAllCities();

    void deleteCity(Long cityId);

    List<City> getCitiesByRegionId(Long regionId) throws CommonException;
}
