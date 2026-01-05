package com.medvault.repository;

import com.medvault.model.Doctor;
import com.medvault.model.EmergencyRequest;
import com.medvault.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmergencyRequestRepository extends JpaRepository<EmergencyRequest, Long> {
    List<EmergencyRequest> findByPatient(Patient patient);
    List<EmergencyRequest> findByPatientId(Long patientId);
    List<EmergencyRequest> findByDoctor(Doctor doctor);
    List<EmergencyRequest> findByDoctorId(Long doctorId);
    List<EmergencyRequest> findByStatus(EmergencyRequest.EmergencyStatus status);
    List<EmergencyRequest> findByDoctorAndStatus(Doctor doctor, EmergencyRequest.EmergencyStatus status);
    List<EmergencyRequest> findAllByOrderByCreatedAtDesc();
    long countByStatus(EmergencyRequest.EmergencyStatus status);
}
