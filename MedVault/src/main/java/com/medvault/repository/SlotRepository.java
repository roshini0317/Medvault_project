package com.medvault.repository;

import com.medvault.model.Doctor;
import com.medvault.model.Slot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface SlotRepository extends JpaRepository<Slot, Long> {
    List<Slot> findByDoctor(Doctor doctor);
    List<Slot> findByDoctorId(Long doctorId);
    List<Slot> findByDoctorAndDate(Doctor doctor, LocalDate date);
    List<Slot> findByDoctorAndDateGreaterThanEqualAndAvailableTrue(Doctor doctor, LocalDate date);
    List<Slot> findByDoctorIdAndDateGreaterThanEqualAndAvailableTrue(Long doctorId, LocalDate date);
    List<Slot> findByDateAndAvailableTrue(LocalDate date);
}
