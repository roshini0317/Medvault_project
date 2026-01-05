package com.medvault.service;

import com.medvault.model.*;
import com.medvault.repository.PrescriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class PrescriptionService {

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    public Optional<Prescription> findById(Long id) {
        return prescriptionRepository.findById(id);
    }

    public List<Prescription> findByPatient(Patient patient) {
        return prescriptionRepository.findByPatientOrderByPrescriptionDateDesc(patient);
    }

    public List<Prescription> findByDoctor(Doctor doctor) {
        return prescriptionRepository.findByDoctor(doctor);
    }

    @Transactional
    public Prescription createPrescription(Patient patient, Doctor doctor, Appointment appointment,
                                            String diagnosis, String medications, String instructions,
                                            LocalDate validUntil) {
        Prescription prescription = new Prescription();
        prescription.setPatient(patient);
        prescription.setDoctor(doctor);
        prescription.setAppointment(appointment);
        prescription.setDiagnosis(diagnosis);
        prescription.setMedications(medications);
        prescription.setInstructions(instructions);
        prescription.setValidUntil(validUntil);
        return prescriptionRepository.save(prescription);
    }

    public long count() {
        return prescriptionRepository.count();
    }
}
