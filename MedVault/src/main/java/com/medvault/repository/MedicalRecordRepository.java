package com.medvault.repository;

import com.medvault.model.MedicalRecord;
import com.medvault.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    List<MedicalRecord> findByPatient(Patient patient);
    List<MedicalRecord> findByPatientId(Long patientId);
    List<MedicalRecord> findByPatientAndRecordType(Patient patient, MedicalRecord.RecordType recordType);
    List<MedicalRecord> findByVerified(boolean verified);
    List<MedicalRecord> findByVerifiedFalse();
    long countByPatient(Patient patient);
}
