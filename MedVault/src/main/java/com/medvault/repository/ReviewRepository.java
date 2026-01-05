package com.medvault.repository;

import com.medvault.model.Doctor;
import com.medvault.model.Patient;
import com.medvault.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByPatient(Patient patient);
    List<Review> findByPatientId(Long patientId);
    List<Review> findByDoctor(Doctor doctor);
    List<Review> findByDoctorId(Long doctorId);
    List<Review> findByDoctorOrderByCreatedAtDesc(Doctor doctor);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.doctor = :doctor")
    Double getAverageRatingByDoctor(Doctor doctor);
    
    long countByDoctor(Doctor doctor);
}
