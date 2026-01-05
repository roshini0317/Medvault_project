package com.medvault.service;

import com.medvault.model.*;
import com.medvault.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private SlotRepository slotRepository;

    public Optional<Appointment> findById(Long id) {
        return appointmentRepository.findById(id);
    }

    public List<Appointment> findAll() {
        return appointmentRepository.findAll();
    }

    public List<Appointment> findByPatient(Patient patient) {
        return appointmentRepository.findByPatient(patient);
    }

    public List<Appointment> findByDoctor(Doctor doctor) {
        return appointmentRepository.findByDoctor(doctor);
    }

    public List<Appointment> findPendingByDoctor(Doctor doctor) {
        return appointmentRepository.findByDoctorAndStatus(doctor, Appointment.AppointmentStatus.PENDING);
    }

    @Transactional
    public Appointment bookAppointment(Patient patient, Doctor doctor, Slot slot, String symptoms) {
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setSlot(slot);
        appointment.setSymptoms(symptoms);
        appointment.setStatus(Appointment.AppointmentStatus.PENDING);
        
        slot.setBookedCount(slot.getBookedCount() + 1);
        if (slot.getBookedCount() >= slot.getMaxPatients()) {
            slot.setAvailable(false);
        }
        slotRepository.save(slot);
        
        return appointmentRepository.save(appointment);
    }

    @Transactional
    public Appointment updateStatus(Long appointmentId, Appointment.AppointmentStatus status) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        appointment.setStatus(status);
        
        if (status == Appointment.AppointmentStatus.REJECTED || status == Appointment.AppointmentStatus.CANCELLED) {
            Slot slot = appointment.getSlot();
            slot.setBookedCount(Math.max(0, slot.getBookedCount() - 1));
            slot.setAvailable(true);
            slotRepository.save(slot);
        }
        
        return appointmentRepository.save(appointment);
    }

    @Transactional
    public Appointment addNotes(Long appointmentId, String notes) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        appointment.setNotes(notes);
        return appointmentRepository.save(appointment);
    }

    public long countByStatus(Appointment.AppointmentStatus status) {
        return appointmentRepository.findByStatus(status).size();
    }

    public long countPendingByDoctor(Doctor doctor) {
        return appointmentRepository.countByDoctorAndStatus(doctor, Appointment.AppointmentStatus.PENDING);
    }
}
