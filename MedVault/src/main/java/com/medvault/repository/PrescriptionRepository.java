package com.medvault.repository;

import com.medvault.model.Doctor;
import com.medvault.model.Patient;
import com.medvault.model.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    List<Prescription> findByPatient(Patient patient);
    List<Prescription> findByPatientId(Long patientId);
    List<Prescription> findByDoctor(Doctor doctor);
    List<Prescription> findByDoctorId(Long doctorId);
    List<Prescription> findByPatientOrderByPrescriptionDateDesc(Patient patient);
}
