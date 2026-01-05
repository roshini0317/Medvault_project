package com.medvault.controller;

import com.medvault.model.*;
import com.medvault.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/patient")
public class PatientController {

    @Autowired
    private UserService userService;
    @Autowired
    private PatientService patientService;
    @Autowired
    private DoctorService doctorService;
    @Autowired
    private AppointmentService appointmentService;
    @Autowired
    private SlotService slotService;
    @Autowired
    private MedicalRecordService medicalRecordService;
    @Autowired
    private PrescriptionService prescriptionService;
    @Autowired
    private ReviewService reviewService;
    @Autowired
    private EmergencyRequestService emergencyRequestService;

    private Patient getCurrentPatient(UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return patientService.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Patient profile not found"));
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Patient patient = getCurrentPatient(userDetails);
        
        List<Appointment> appointments = patientService.getPatientAppointments(patient);
        long upcomingCount = appointments.stream()
                .filter(a -> a.getStatus() == Appointment.AppointmentStatus.APPROVED)
                .count();
        long pendingCount = appointments.stream()
                .filter(a -> a.getStatus() == Appointment.AppointmentStatus.PENDING)
                .count();
        
        model.addAttribute("patient", patient);
        model.addAttribute("upcomingAppointments", upcomingCount);
        model.addAttribute("pendingAppointments", pendingCount);
        model.addAttribute("totalRecords", medicalRecordService.countByPatient(patient));
        model.addAttribute("recentAppointments", appointments.stream().limit(5).toList());
        
        return "patient/dashboard";
    }

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Patient patient = getCurrentPatient(userDetails);
        model.addAttribute("patient", patient);
        return "patient/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                @RequestParam String fullName,
                                @RequestParam String phone,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateOfBirth,
                                @RequestParam(required = false) String gender,
                                @RequestParam(required = false) String bloodGroup,
                                @RequestParam(required = false) String address,
                                @RequestParam(required = false) String emergencyContactName,
                                @RequestParam(required = false) String emergencyContactPhone,
                                @RequestParam(required = false) String allergies,
                                @RequestParam(required = false) String chronicConditions,
                                RedirectAttributes redirectAttributes) {
        Patient patient = getCurrentPatient(userDetails);
        
        User user = patient.getUser();
        user.setFullName(fullName);
        user.setPhone(phone);
        userService.updateUser(user);
        
        patient.setDateOfBirth(dateOfBirth);
        patient.setGender(gender);
        patient.setBloodGroup(bloodGroup);
        patient.setAddress(address);
        patient.setEmergencyContactName(emergencyContactName);
        patient.setEmergencyContactPhone(emergencyContactPhone);
        patient.setAllergies(allergies);
        patient.setChronicConditions(chronicConditions);
        patientService.save(patient);
        
        redirectAttributes.addFlashAttribute("message", "Profile updated successfully");
        return "redirect:/patient/profile";
    }

    @GetMapping("/book-appointment")
    public String bookAppointment(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        List<Doctor> doctors = doctorService.findVerifiedDoctors();
        model.addAttribute("doctors", doctors);
        return "patient/book-appointment";
    }

    @GetMapping("/book-appointment/{doctorId}")
    public String selectSlot(@PathVariable Long doctorId, Model model) {
        Doctor doctor = doctorService.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        List<Slot> slots = slotService.findAvailableSlots(doctor, LocalDate.now());
        
        model.addAttribute("doctor", doctor);
        model.addAttribute("slots", slots);
        return "patient/select-slot";
    }

    @PostMapping("/book-appointment")
    public String confirmBooking(@AuthenticationPrincipal UserDetails userDetails,
                                  @RequestParam Long slotId,
                                  @RequestParam(required = false) String symptoms,
                                  RedirectAttributes redirectAttributes) {
        Patient patient = getCurrentPatient(userDetails);
        Slot slot = slotService.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found"));
        
        appointmentService.bookAppointment(patient, slot.getDoctor(), slot, symptoms);
        redirectAttributes.addFlashAttribute("message", "Appointment booked successfully! Waiting for doctor approval.");
        return "redirect:/patient/appointments";
    }

    @GetMapping("/appointments")
    public String appointments(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Patient patient = getCurrentPatient(userDetails);
        List<Appointment> appointments = patientService.getPatientAppointments(patient);
        model.addAttribute("appointments", appointments);
        return "patient/appointments";
    }

    @PostMapping("/appointments/{id}/cancel")
    public String cancelAppointment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        appointmentService.updateStatus(id, Appointment.AppointmentStatus.CANCELLED);
        redirectAttributes.addFlashAttribute("message", "Appointment cancelled successfully");
        return "redirect:/patient/appointments";
    }

    @GetMapping("/prescriptions")
    public String prescriptions(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Patient patient = getCurrentPatient(userDetails);
        List<Prescription> prescriptions = patientService.getPatientPrescriptions(patient);
        model.addAttribute("prescriptions", prescriptions);
        return "patient/prescriptions";
    }

    @GetMapping("/health-records")
    public String healthRecords(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Patient patient = getCurrentPatient(userDetails);
        List<MedicalRecord> records = medicalRecordService.findByPatient(patient);
        model.addAttribute("records", records);
        model.addAttribute("recordTypes", MedicalRecord.RecordType.values());
        return "patient/health-records";
    }
    @PostMapping("/health-records/upload")
    public String uploadRecord(@AuthenticationPrincipal UserDetails userDetails,
                               @RequestParam String title,
                               @RequestParam MedicalRecord.RecordType recordType,
                               @RequestParam(required = false) String description,
                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate recordDate,
                               @RequestParam MultipartFile file,
                               @RequestParam(required = false) String fileUrl,   // <‑‑ new
                               RedirectAttributes redirectAttributes) {
        try {
            Patient patient = getCurrentPatient(userDetails);
            medicalRecordService.uploadRecord(patient, title, recordType,
                    description, recordDate, file, fileUrl); // pass URL to service
            redirectAttributes.addFlashAttribute("message", "Record uploaded successfully");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to upload file: " + e.getMessage());
        }
        return "redirect:/patient/health-records";
    }


    @PostMapping("/health-records/{id}/delete")
    public String deleteRecord(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            medicalRecordService.deleteRecord(id);
            redirectAttributes.addFlashAttribute("message", "Record deleted successfully");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete record");
        }
        return "redirect:/patient/health-records";
    }

    @GetMapping("/document-permissions")
    public String documentPermissions(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Patient patient = getCurrentPatient(userDetails);
        List<MedicalRecord> records = medicalRecordService.findByPatient(patient);
        List<Doctor> doctors = doctorService.findVerifiedDoctors();
        List<DocumentPermission> permissions = medicalRecordService.getPatientPermissions(patient);
        
        model.addAttribute("records", records);
        model.addAttribute("doctors", doctors);
        model.addAttribute("permissions", permissions);
        return "patient/document-permissions";
    }

    @PostMapping("/document-permissions/grant")
    public String grantPermission(@AuthenticationPrincipal UserDetails userDetails,
                                   @RequestParam Long recordId,
                                   @RequestParam Long doctorId,
                                   @RequestParam(defaultValue = "false") boolean canDownload,
                                   RedirectAttributes redirectAttributes) {
        Patient patient = getCurrentPatient(userDetails);
        MedicalRecord record = medicalRecordService.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Record not found"));
        Doctor doctor = doctorService.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        
        medicalRecordService.grantPermission(patient, doctor, record, canDownload, null);
        redirectAttributes.addFlashAttribute("message", "Permission granted successfully");
        return "redirect:/patient/document-permissions";
    }

    @PostMapping("/document-permissions/{id}/revoke")
    public String revokePermission(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        medicalRecordService.revokePermission(id);
        redirectAttributes.addFlashAttribute("message", "Permission revoked successfully");
        return "redirect:/patient/document-permissions";
    }

    @GetMapping("/reviews")
    public String reviews(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Patient patient = getCurrentPatient(userDetails);
        List<Review> reviews = reviewService.findByPatient(patient);
        List<Appointment> completedAppointments = appointmentService.findByPatient(patient).stream()
                .filter(a -> a.getStatus() == Appointment.AppointmentStatus.COMPLETED)
                .toList();
        
        model.addAttribute("reviews", reviews);
        model.addAttribute("completedAppointments", completedAppointments);
        return "patient/reviews";
    }

    @PostMapping("/reviews")
    public String submitReview(@AuthenticationPrincipal UserDetails userDetails,
                                @RequestParam Long appointmentId,
                                @RequestParam Integer rating,
                                @RequestParam(required = false) String comment,
                                RedirectAttributes redirectAttributes) {
        Patient patient = getCurrentPatient(userDetails);
        Appointment appointment = appointmentService.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        
        reviewService.createReview(patient, appointment.getDoctor(), appointment, rating, comment);
        redirectAttributes.addFlashAttribute("message", "Review submitted successfully");
        return "redirect:/patient/reviews";
    }

    @GetMapping("/emergency")
    public String emergencyRequest(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Patient patient = getCurrentPatient(userDetails);
        List<EmergencyRequest> requests = emergencyRequestService.findByPatient(patient);
        model.addAttribute("requests", requests);
        model.addAttribute("urgencyLevels", EmergencyRequest.UrgencyLevel.values());
        return "patient/emergency";
    }

    @PostMapping("/emergency")
    public String submitEmergency(@AuthenticationPrincipal UserDetails userDetails,
                                   @RequestParam String description,
                                   @RequestParam EmergencyRequest.UrgencyLevel urgencyLevel,
                                   @RequestParam String contactNumber,
                                   @RequestParam(required = false) String location,
                                   RedirectAttributes redirectAttributes) {
        Patient patient = getCurrentPatient(userDetails);
        emergencyRequestService.createRequest(patient, description, urgencyLevel, contactNumber, location);
        redirectAttributes.addFlashAttribute("message", "Emergency request submitted successfully");
        return "redirect:/patient/emergency";
    }

}
