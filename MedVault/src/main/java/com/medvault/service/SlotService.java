package com.medvault.service;

import com.medvault.model.*;
import com.medvault.repository.SlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class SlotService {

    @Autowired
    private SlotRepository slotRepository;

    public Optional<Slot> findById(Long id) {
        return slotRepository.findById(id);
    }

    public List<Slot> findByDoctor(Doctor doctor) {
        return slotRepository.findByDoctor(doctor);
    }

    public List<Slot> findAvailableSlots(Doctor doctor, LocalDate fromDate) {
        return slotRepository.findByDoctorAndDateGreaterThanEqualAndAvailableTrue(doctor, fromDate);
    }

    public List<Slot> findAvailableSlotsByDoctorId(Long doctorId, LocalDate fromDate) {
        return slotRepository.findByDoctorIdAndDateGreaterThanEqualAndAvailableTrue(doctorId, fromDate);
    }

    @Transactional
    public Slot createSlot(Slot slot) {
        return slotRepository.save(slot);
    }

    @Transactional
    public void deleteSlot(Long slotId) {
        slotRepository.deleteById(slotId);
    }

    @Transactional
    public Slot updateSlot(Slot slot) {
        return slotRepository.save(slot);
    }
}
