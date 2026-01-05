package com.medvault.service;

import com.medvault.model.*;
import com.medvault.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    public Optional<Patient> findById(Long id) {
        return patientRepository.findById(id);
    }

    public Optional<Patient> findByUserId(Long userId) {
        return patientRepository.findByUserId(userId);
    }

    public Optional<Patient> findByUser(User user) {
        return patientRepository.findByUser(user);
    }

    public List<Patient> findAll() {
        return patientRepository.findAll();
    }

    @Transactional
    public Patient save(Patient patient) {
        return patientRepository.save(patient);
    }

    public List<Appointment> getPatientAppointments(Patient patient) {
        return appointmentRepository.findByPatient(patient);
    }

    public List<MedicalRecord> getPatientMedicalRecords(Patient patient) {
        return medicalRecordRepository.findByPatient(patient);
    }

    public List<Prescription> getPatientPrescriptions(Patient patient) {
        return prescriptionRepository.findByPatientOrderByPrescriptionDateDesc(patient);
    }

    public long countPatients() {
        return patientRepository.count();
    }
}
