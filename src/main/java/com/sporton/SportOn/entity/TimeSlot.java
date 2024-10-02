package com.sporton.SportOn.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "time_slot")
public class TimeSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NonNull
    private Long courtId;
    @NonNull
    private String startTime;
    @NonNull
    private String endTime;
    @NonNull
    private Boolean available;
    @NonNull
    private Double price;

    @ElementCollection
    private List<LocalDate> bookedDates = new ArrayList<>();

    public List<LocalDate> getBookedDates() {
        if (bookedDates == null) {
            bookedDates = new ArrayList<>();
        }
        return bookedDates;
    }

    public void setBookedDates(List<LocalDate> bookedDates) {
        this.bookedDates = bookedDates;
    }
}