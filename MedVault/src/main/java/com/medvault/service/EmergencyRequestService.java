package com.medvault.service;

import com.medvault.model.*;
import com.medvault.repository.EmergencyRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EmergencyRequestService {

    @Autowired
    private EmergencyRequestRepository emergencyRequestRepository;

    public Optional<EmergencyRequest> findById(Long id) {
        return emergencyRequestRepository.findById(id);
    }

    public List<EmergencyRequest> findAll() {
        return emergencyRequestRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<EmergencyRequest> findByPatient(Patient patient) {
        return emergencyRequestRepository.findByPatient(patient);
    }

    public List<EmergencyRequest> findByDoctor(Doctor doctor) {
        return emergencyRequestRepository.findByDoctor(doctor);
    }

    public List<EmergencyRequest> findPending() {
        return emergencyRequestRepository.findByStatus(EmergencyRequest.EmergencyStatus.PENDING);
    }

    @Transactional
    public EmergencyRequest createRequest(Patient patient, String description, 
                                          EmergencyRequest.UrgencyLevel urgencyLevel,
                                          String contactNumber, String location) {
        EmergencyRequest request = new EmergencyRequest();
        request.setPatient(patient);
        request.setDescription(description);
        request.setUrgencyLevel(urgencyLevel);
        request.setContactNumber(contactNumber);
        request.setLocation(location);
        return emergencyRequestRepository.save(request);
    }

    @Transactional
    public EmergencyRequest respondToRequest(Long requestId, Doctor doctor, 
                                             EmergencyRequest.EmergencyStatus status, String response) {
        EmergencyRequest request = emergencyRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Emergency request not found"));
        request.setDoctor(doctor);
        request.setStatus(status);
        request.setResponse(response);
        request.setRespondedAt(LocalDateTime.now());
        return emergencyRequestRepository.save(request);
    }

    public long countPending() {
        return emergencyRequestRepository.countByStatus(EmergencyRequest.EmergencyStatus.PENDING);
    }
}
