package com.sporton.SportOn.service.regionService;

import com.sporton.SportOn.entity.Region;
import com.sporton.SportOn.exception.regionException.RegionException;
import com.sporton.SportOn.model.regionModel.RegionRequest;
import com.sporton.SportOn.model.regionModel.RegionResponse;
import com.sporton.SportOn.repository.AppUserRepository;
import com.sporton.SportOn.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RegionImpl implements RegionService{
    private final RegionRepository regionRepository;
    private final AppUserRepository appUserRepository;
    @Override
    public RegionResponse createRegion(RegionRequest body) throws RegionException {
        try {
            Optional<Region> optionalRegion = regionRepository.findByName(body.getName());
            if (!optionalRegion.isPresent()){
                Region region = Region.builder()
                        .name(body.getName())
                        .build();
                regionRepository.save(region);
                RegionResponse regionResponse = RegionResponse.builder()
                        .status(HttpStatus.CREATED)
                        .message("Region Created Successfully")
                        .build();
                return regionResponse;
            }else {
                throw new RegionException("Region with name '" + body.getName() + "' already exists");
            }
        }catch (Exception e){
            throw new RegionException(e.getMessage());
        }
    }

    @Override
    public List<Region> getAllRegions() throws RegionException {
        try {
            Optional<List<Region>> optionalRegions = Optional.of(regionRepository.findAll());
            return optionalRegions.get();
        }catch (Exception e){
            throw new RegionException(e.getMessage());
        }
    }

    @Override
    public RegionResponse updateRegion(Long regionId, RegionRequest body) throws RegionException {
        try {
            Optional<Region> optionalRegion = regionRepository.findById(regionId);
            if (optionalRegion.isPresent()){
                optionalRegion.get().setName(body.getName());
                regionRepository.save(optionalRegion.get());
                return RegionResponse.builder()
                        .status(HttpStatus.OK)
                        .message("Region with id '" + regionId + "' Successfully Updated")
                        .build();
            }else {
                throw new RegionException("No region with id '" + regionId + "' is found");
            }
        }catch (Exception e){
            throw new RegionException(e.getMessage());
        }
    }

    @Override
    public RegionResponse deleteRegion(Long regionId) throws RegionException {
        try {
            Optional<Region> optionalRegion = regionRepository.findById(regionId);
            if (optionalRegion.isPresent()){
                regionRepository.deleteById(regionId);
                return RegionResponse.builder()
                        .status(HttpStatus.OK)
                        .message("Region with id '" + regionId + "' Successfully Deleted")
                        .build();
            }else {
                throw new RegionException("No region with id '" + regionId + "' is found");
            }
        }catch (Exception e){
            throw new RegionException(e.getMessage());
        }
    }
}
