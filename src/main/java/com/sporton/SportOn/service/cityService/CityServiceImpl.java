package com.sporton.SportOn.service.cityService;

import com.sporton.SportOn.entity.City;
import com.sporton.SportOn.entity.Region;
import com.sporton.SportOn.exception.commonException.CommonException;
import com.sporton.SportOn.repository.CityRepository;
import com.sporton.SportOn.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CityServiceImpl implements CityService {

    private final CityRepository cityRepository;
    private final RegionRepository regionRepository;

    public City registerCity(String cityName, Long regionId) throws CommonException {
        try {
            Region region = regionRepository.findById(regionId)
                    .orElseThrow(() -> new IllegalArgumentException("Region not found"));
Optional<City> checkCity = cityRepository.findByName(cityName.toUpperCase());
if (checkCity.isPresent()) throw new CommonException("City " + cityName+" Already Exists");

City city = City.builder()
                    .name(cityName.toUpperCase())
                    .region(region)
                    .build();

            return cityRepository.save(city);
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    public List<City> getAllCities() {
        return cityRepository.findAll();
    }


    public City updateCity(Long cityId, String newCityName, Long newRegionId) {
        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new IllegalArgumentException("City not found"));

        Region newRegion = regionRepository.findById(newRegionId)
                .orElseThrow(() -> new IllegalArgumentException("Region not found"));

        city.setName(newCityName.toUpperCase());
        city.setRegion(newRegion);

        return cityRepository.save(city);
    }

    public void deleteCity(Long cityId) {
        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new IllegalArgumentException("City not found"));

        cityRepository.delete(city);
    }

    @Override
    public List<City> getCitiesByRegionId(Long regionId) throws CommonException {
        try {
            Region region = regionRepository.findById(regionId)
                    .orElseThrow(() -> new IllegalArgumentException("Region not found"));
            return cityRepository.findAllByRegionId(regionId);
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }


}
