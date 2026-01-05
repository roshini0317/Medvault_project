package com.medvault.service;

import com.medvault.model.*;
import com.medvault.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class MedicalRecordService {

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    @Autowired
    private DocumentPermissionRepository documentPermissionRepository;

    private final String uploadDir = "uploads/medical-records";


    public Optional<MedicalRecord> findById(Long id) {
        return medicalRecordRepository.findById(id);
    }

    public List<MedicalRecord> findByPatient(Patient patient) {
        return medicalRecordRepository.findByPatient(patient);
    }

    public List<MedicalRecord> findByPatientAndType(Patient patient, MedicalRecord.RecordType type) {
        return medicalRecordRepository.findByPatientAndRecordType(patient, type);
    }

    public List<MedicalRecord> findUnverifiedRecords() {
        return medicalRecordRepository.findByVerifiedFalse();
    }

    @Transactional
    public MedicalRecord uploadRecord(Patient patient, String title, MedicalRecord.RecordType recordType,
                                      String description, LocalDate recordDate, MultipartFile file, String fileUrl) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
        String newFilename = UUID.randomUUID().toString() + extension;
        Path filePath = uploadPath.resolve(newFilename);
        Files.copy(file.getInputStream(), filePath);

        MedicalRecord record = new MedicalRecord();
        record.setPatient(patient);
        record.setTitle(title);
        record.setRecordType(recordType);
        record.setDescription(description);
        record.setRecordDate(recordDate);
        record.setFileName(newFilename);          // was originalFilename
        record.setFilePath(filePath.toString());
        record.setFileType(file.getContentType());
        record.setFileSize(file.getSize());

        return medicalRecordRepository.save(record);
    }

    @Transactional
    public MedicalRecord verifyRecord(Long recordId, Doctor doctor) {
        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Record not found"));
        record.setVerified(true);
        record.setVerifiedBy(doctor);
        record.setVerifiedAt(LocalDateTime.now());
        return medicalRecordRepository.save(record);
    }

    @Transactional
    public void deleteRecord(Long recordId) throws IOException {
        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Record not found"));

        // 1) delete related permissions first
        documentPermissionRepository.deleteByMedicalRecord(record);

        // 2) delete file from disk
        Path filePath = Paths.get(record.getFilePath());
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }

        // 3) delete record from DB
        medicalRecordRepository.delete(record);
    }


    @Transactional
    public DocumentPermission grantPermission(Patient patient, Doctor doctor, MedicalRecord record, 
                                               boolean canDownload, LocalDateTime expiresAt) {
        Optional<DocumentPermission> existing = documentPermissionRepository
                .findByPatientAndDoctorAndMedicalRecord(patient, doctor, record);
        
        if (existing.isPresent()) {
            DocumentPermission permission = existing.get();
            permission.setRevoked(false);
            permission.setCanDownload(canDownload);
            permission.setExpiresAt(expiresAt);
            return documentPermissionRepository.save(permission);
        }
        
        DocumentPermission permission = new DocumentPermission();
        permission.setPatient(patient);
        permission.setDoctor(doctor);
        permission.setMedicalRecord(record);
        permission.setCanDownload(canDownload);
        permission.setExpiresAt(expiresAt);
        return documentPermissionRepository.save(permission);
    }

    @Transactional
    public void revokePermission(Long permissionId) {
        DocumentPermission permission = documentPermissionRepository.findById(permissionId)
                .orElseThrow(() -> new RuntimeException("Permission not found"));
        permission.setRevoked(true);
        permission.setRevokedAt(LocalDateTime.now());
        documentPermissionRepository.save(permission);
    }

    public List<DocumentPermission> getPatientPermissions(Patient patient) {
        return documentPermissionRepository.findByPatient(patient);
    }

    public List<DocumentPermission> getDoctorPermissions(Doctor doctor) {
        return documentPermissionRepository.findByDoctorAndRevokedFalse(doctor);
    }

    public boolean hasAccess(Doctor doctor, MedicalRecord record) {
        return documentPermissionRepository.existsByDoctorAndMedicalRecordAndRevokedFalse(doctor, record);
    }

    public long countByPatient(Patient patient) {
        return medicalRecordRepository.countByPatient(patient);
    }

    public long countUnverified() {
        return medicalRecordRepository.findByVerifiedFalse().size();
    }
}
