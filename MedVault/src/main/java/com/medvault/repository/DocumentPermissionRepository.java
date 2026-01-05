package com.medvault.repository;

import com.medvault.model.Doctor;
import com.medvault.model.DocumentPermission;
import com.medvault.model.MedicalRecord;
import com.medvault.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentPermissionRepository extends JpaRepository<DocumentPermission, Long> {
    List<DocumentPermission> findByPatient(Patient patient);
    List<DocumentPermission> findByPatientId(Long patientId);
    List<DocumentPermission> findByDoctor(Doctor doctor);
    List<DocumentPermission> findByDoctorId(Long doctorId);
    List<DocumentPermission> findByDoctorAndRevokedFalse(Doctor doctor);
    Optional<DocumentPermission> findByPatientAndDoctorAndMedicalRecord(Patient patient, Doctor doctor, MedicalRecord medicalRecord);
    List<DocumentPermission> findByMedicalRecord(MedicalRecord medicalRecord);
    boolean existsByDoctorAndMedicalRecordAndRevokedFalse(Doctor doctor, MedicalRecord medicalRecord);
    void deleteByMedicalRecord(MedicalRecord medicalRecord);
}
