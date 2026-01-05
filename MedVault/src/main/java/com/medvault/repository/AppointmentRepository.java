package com.medvault.repository;

import com.medvault.model.Appointment;
import com.medvault.model.Doctor;
import com.medvault.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatient(Patient patient);
    List<Appointment> findByPatientId(Long patientId);
    List<Appointment> findByDoctor(Doctor doctor);
    List<Appointment> findByDoctorId(Long doctorId);
    List<Appointment> findByStatus(Appointment.AppointmentStatus status);
    List<Appointment> findByDoctorAndStatus(Doctor doctor, Appointment.AppointmentStatus status);
    List<Appointment> findByPatientAndStatus(Patient patient, Appointment.AppointmentStatus status);
    List<Appointment> findByDoctorIdAndStatus(Long doctorId, Appointment.AppointmentStatus status);
    long countByDoctorAndStatus(Doctor doctor, Appointment.AppointmentStatus status);
    long countByPatientAndStatus(Patient patient, Appointment.AppointmentStatus status);
}
