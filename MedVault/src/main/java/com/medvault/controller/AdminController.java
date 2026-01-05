package com.medvault.controller;

import com.medvault.model.*;
import com.medvault.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;
    @Autowired
    private DoctorService doctorService;
    @Autowired
    private PatientService patientService;
    @Autowired
    private AppointmentService appointmentService;
    @Autowired
    private MedicalRecordService medicalRecordService;
    @Autowired
    private EmergencyRequestService emergencyRequestService;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        model.addAttribute("user", user);
        model.addAttribute("totalDoctors", doctorService.findAll().size());
        model.addAttribute("verifiedDoctors", doctorService.countVerifiedDoctors());
        model.addAttribute("pendingDoctors", doctorService.countUnverifiedDoctors());
        model.addAttribute("totalPatients", patientService.countPatients());
        model.addAttribute("pendingAppointments", appointmentService.countByStatus(Appointment.AppointmentStatus.PENDING));
        model.addAttribute("pendingEmergencies", emergencyRequestService.countPending());
        model.addAttribute("unverifiedDocuments", medicalRecordService.countUnverified());
        
        return "admin/dashboard";
    }

    @GetMapping("/doctors")
    public String manageDoctors(Model model) {
        List<Doctor> allDoctors = doctorService.findAll();
        List<Doctor> pendingDoctors = doctorService.findUnverifiedDoctors();
        
        model.addAttribute("doctors", allDoctors);
        model.addAttribute("pendingDoctors", pendingDoctors);
        return "admin/doctors";
    }

    @PostMapping("/doctors/{id}/verify")
    public String verifyDoctor(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        doctorService.verifyDoctor(id);
        redirectAttributes.addFlashAttribute("message", "Doctor verified successfully");
        return "redirect:/admin/doctors";
    }

    @PostMapping("/doctors/{id}/delete")
    public String deleteDoctor(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Doctor doctor = doctorService.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        userService.deleteUser(doctor.getUser().getId());
        redirectAttributes.addFlashAttribute("message", "Doctor deleted successfully");
        return "redirect:/admin/doctors";
    }

    @PostMapping("/doctors/{id}/toggle-status")
    public String toggleDoctorStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Doctor doctor = doctorService.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        User user = doctor.getUser();
        user.setEnabled(!user.isEnabled());
        userService.updateUser(user);
        redirectAttributes.addFlashAttribute("message", "Doctor status updated successfully");
        return "redirect:/admin/doctors";
    }

    @GetMapping("/patients")
    public String managePatients(Model model) {
        List<Patient> patients = patientService.findAll();
        model.addAttribute("patients", patients);
        return "admin/patients";
    }

    @PostMapping("/patients/{id}/delete")
    public String deletePatient(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Patient patient = patientService.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        userService.deleteUser(patient.getUser().getId());
        redirectAttributes.addFlashAttribute("message", "Patient deleted successfully");
        return "redirect:/admin/patients";
    }

    @PostMapping("/patients/{id}/toggle-status")
    public String togglePatientStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Patient patient = patientService.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        User user = patient.getUser();
        user.setEnabled(!user.isEnabled());
        userService.updateUser(user);
        redirectAttributes.addFlashAttribute("message", "Patient status updated successfully");
        return "redirect:/admin/patients";
    }

    @GetMapping("/document-verification")
    public String documentVerification(Model model) {
        List<MedicalRecord> unverifiedRecords = medicalRecordService.findUnverifiedRecords();
        model.addAttribute("records", unverifiedRecords);
        return "admin/document-verification";
    }
}
