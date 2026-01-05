package com.medvault.service;

import com.medvault.model.*;
import com.medvault.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private DoctorService doctorService;

    public Optional<Review> findById(Long id) {
        return reviewRepository.findById(id);
    }

    public List<Review> findByPatient(Patient patient) {
        return reviewRepository.findByPatient(patient);
    }

    public List<Review> findByDoctor(Doctor doctor) {
        return reviewRepository.findByDoctorOrderByCreatedAtDesc(doctor);
    }

    @Transactional
    public Review createReview(Patient patient, Doctor doctor, Appointment appointment, 
                               Integer rating, String comment) {
        Review review = new Review();
        review.setPatient(patient);
        review.setDoctor(doctor);
        review.setAppointment(appointment);
        review.setRating(rating);
        review.setComment(comment);
        
        Review savedReview = reviewRepository.save(review);
        doctorService.updateDoctorRating(doctor);
        
        return savedReview;
    }

    public Double getAverageRating(Doctor doctor) {
        return reviewRepository.getAverageRatingByDoctor(doctor);
    }

    public long countByDoctor(Doctor doctor) {
        return reviewRepository.countByDoctor(doctor);
    }
}
