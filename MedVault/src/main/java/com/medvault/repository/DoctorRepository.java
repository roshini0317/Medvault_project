package com.medvault.repository;

import com.medvault.model.Doctor;
import com.medvault.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByUser(User user);
    Optional<Doctor> findByUserId(Long userId);
    List<Doctor> findByVerified(boolean verified);
    List<Doctor> findBySpecialization(String specialization);
    List<Doctor> findByVerifiedTrue();
}
