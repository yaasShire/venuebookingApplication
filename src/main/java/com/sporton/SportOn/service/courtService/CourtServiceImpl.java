package com.sporton.SportOn.service.courtService;

import com.sporton.SportOn.entity.Court;
import com.sporton.SportOn.entity.CourtSurface;
import com.sporton.SportOn.entity.Venue;
import com.sporton.SportOn.exception.commonException.CommonException;
import com.sporton.SportOn.model.courtModel.CourtRequestModel;
import com.sporton.SportOn.model.courtModel.CourtResponseModel;
import com.sporton.SportOn.repository.CourtRepository;
import com.sporton.SportOn.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourtServiceImpl implements CourtService{
    private final CourtRepository courtRepository;
    private final VenueRepository venueRepository;
    @Override
    public CourtResponseModel createCourt(CourtRequestModel body) throws CommonException {
        try {
            Optional<Venue> optionalVenue = venueRepository.findById(body.getVenueId());
            if (optionalVenue.isPresent()){
                Optional<Court> optionalCourt = courtRepository.findByName(body.getName());
                if (!optionalCourt.isPresent()){
                    Court court = Court.builder()
                            .venueId(body.getVenueId())
                            .name(body.getName())
                            .width(body.getWidth())
                            .height(body.getHeight())
                            .surface(CourtSurface.ARTIFICIAL_TURF)
                            .activePlayersPerTeam(body.getActivePlayersPerTeam())
                            .basePrice(body.getBasePrice())
                            .additionalInfo(body.getAdditionalInfo())
                            .build();
                    courtRepository.save(court);
                    optionalVenue.get().setNumberOfCourts(optionalVenue.get().getNumberOfCourts() + 1);
                    venueRepository.save(optionalVenue.get());
                    return CourtResponseModel.builder()
                            .status(HttpStatus.CREATED)
                            .message("Court Created Successfully")
                            .build();
                }else {
                    throw new CommonException("Court With Name '" + body.getName() + "' Already Exists");
                }
            }else {
                throw new CommonException("Venue with id " + body.getVenueId() + " does not exist");
            }
        }catch (Exception e){
            throw new CommonException(e.getMessage());

        }
    }

    @Override
    public Court getCourtById(Long courtId) throws CommonException {
        try {
            Optional<Court> optionalCourt = courtRepository.findById(courtId);
            if (optionalCourt.isPresent()){
                return optionalCourt.get();
            }else {
                throw new CommonException("Court with id " + courtId + " does not exist");
            }
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    @Override
    public List<Court> getCourtByVenueId(Long venueId, int page, int size) throws CommonException {
        try {
            Optional<Venue> optionalVenue = venueRepository.findById(venueId);
            if (optionalVenue.isPresent()){
                PageRequest pageRequest = PageRequest.of(page, size);
                Optional<List<Court>> optionalCourts = courtRepository.findByVenueId(venueId, pageRequest);
                if (optionalCourts.isPresent()){
                    return optionalCourts.get();
                }else {
                    throw new CommonException("No Court Found");
                }
            }else {
                throw new CommonException("Venue wit id " + venueId + " does not exist");
            }
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    @Override
    public CourtResponseModel updateCourt(Long courtId, CourtRequestModel body) throws CommonException {
        try {
            Optional<Court> optionalCourt = courtRepository.findById(courtId);
            if (optionalCourt.isPresent()){
                log.info("inside court update data {}", body);
                optionalCourt.get().setName(body.getName());
                optionalCourt.get().setHeight(body.getHeight());
                optionalCourt.get().setWidth(body.getWidth());
                optionalCourt.get().setSurface(body.getSurface());
                optionalCourt.get().setBasePrice(body.getBasePrice());
                optionalCourt.get().setAdditionalInfo(body.getAdditionalInfo());
                optionalCourt.get().setActivePlayersPerTeam(body.getActivePlayersPerTeam());
                courtRepository.save(optionalCourt.get());
                log.info("updated court {}", optionalCourt.get());
                return  CourtResponseModel.builder()
                        .status(HttpStatus.OK)
                        .message("Court Updated Successfully")
                        .build();
            }else {
                throw new CommonException("There is No Court with id " + courtId );
            }
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    @Override
    public CourtResponseModel deleteCourt(Long courtId) throws CommonException {
        try {
            Optional<Court> optionalCourt = courtRepository.findById(courtId);
            if (optionalCourt.isPresent()){
                courtRepository.deleteById(courtId);
                return CourtResponseModel.builder()
                        .status(HttpStatus.OK)
                        .message("Court Deleted Successfully")
                        .build();
            }else {
                throw new CommonException("There is No Court with id " + courtId );
            }
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }
}
