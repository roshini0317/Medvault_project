package com.medvault.service;

import com.medvault.model.*;
import com.medvault.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public User registerPatient(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(User.Role.PATIENT);
        User savedUser = userRepository.save(user);
        
        Patient patient = new Patient();
        patient.setUser(savedUser);
        patientRepository.save(patient);
        
        return savedUser;
    }

    @Transactional
    public User registerDoctor(User user, Doctor doctorDetails) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(User.Role.DOCTOR);
        User savedUser = userRepository.save(user);
        
        doctorDetails.setUser(savedUser);
        doctorDetails.setVerified(false);
        doctorRepository.save(doctorDetails);
        
        return savedUser;
    }

    @Transactional
    public User registerAdmin(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(User.Role.ADMIN);
        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public List<User> findAllByRole(User.Role role) {
        return userRepository.findByRole(role);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public long countByRole(User.Role role) {
        return userRepository.findByRole(role).size();
    }
}
