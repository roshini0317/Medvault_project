package com.medvault.service;

import com.medvault.model.*;
import com.medvault.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private SlotRepository slotRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    public Optional<Doctor> findById(Long id) {
        return doctorRepository.findById(id);
    }

    public Optional<Doctor> findByUserId(Long userId) {
        return doctorRepository.findByUserId(userId);
    }

    public Optional<Doctor> findByUser(User user) {
        return doctorRepository.findByUser(user);
    }

    public List<Doctor> findAll() {
        return doctorRepository.findAll();
    }

    public List<Doctor> findVerifiedDoctors() {
        return doctorRepository.findByVerifiedTrue();
    }

    public List<Doctor> findUnverifiedDoctors() {
        return doctorRepository.findByVerified(false);
    }

    @Transactional
    public Doctor save(Doctor doctor) {
        return doctorRepository.save(doctor);
    }

    @Transactional
    public void verifyDoctor(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        doctor.setVerified(true);
        doctorRepository.save(doctor);
    }

    @Transactional
    public Slot createSlot(Doctor doctor, Slot slot) {
        slot.setDoctor(doctor);
        return slotRepository.save(slot);
    }

    public List<Slot> getDoctorSlots(Doctor doctor) {
        return slotRepository.findByDoctor(doctor);
    }

    public List<Slot> getAvailableSlots(Doctor doctor, LocalDate fromDate) {
        return slotRepository.findByDoctorAndDateGreaterThanEqualAndAvailableTrue(doctor, fromDate);
    }

    public List<Appointment> getDoctorAppointments(Doctor doctor) {
        return appointmentRepository.findByDoctor(doctor);
    }

    public List<Appointment> getPendingAppointments(Doctor doctor) {
        return appointmentRepository.findByDoctorAndStatus(doctor, Appointment.AppointmentStatus.PENDING);
    }

    @Transactional
    public void updateDoctorRating(Doctor doctor) {
        Double avgRating = reviewRepository.getAverageRatingByDoctor(doctor);
        long totalReviews = reviewRepository.countByDoctor(doctor);
        doctor.setAverageRating(avgRating != null ? avgRating : 0.0);
        doctor.setTotalReviews((int) totalReviews);
        doctorRepository.save(doctor);
    }

    public long countVerifiedDoctors() {
        return doctorRepository.findByVerifiedTrue().size();
    }

    public long countUnverifiedDoctors() {
        return doctorRepository.findByVerified(false).size();
    }
}
