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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.nio.file.Files;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/doctor")
public class DoctorController {

    @Autowired
    private UserService userService;
    @Autowired
    private DoctorService doctorService;
    @Autowired
    private PatientService patientService;
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

    private Doctor getCurrentDoctor(UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return doctorService.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Doctor profile not found"));
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Doctor doctor = getCurrentDoctor(userDetails);
        
        List<Appointment> appointments = doctorService.getDoctorAppointments(doctor);
        long pendingCount = appointments.stream()
                .filter(a -> a.getStatus() == Appointment.AppointmentStatus.PENDING)
                .count();
        long approvedCount = appointments.stream()
                .filter(a -> a.getStatus() == Appointment.AppointmentStatus.APPROVED)
                .count();
        long completedCount = appointments.stream()
                .filter(a -> a.getStatus() == Appointment.AppointmentStatus.COMPLETED)
                .count();
        
        model.addAttribute("doctor", doctor);
        model.addAttribute("pendingAppointments", pendingCount);
        model.addAttribute("todayAppointments", approvedCount);
        model.addAttribute("completedAppointments", completedCount);
        model.addAttribute("totalPatients", appointments.stream().map(a -> a.getPatient().getId()).distinct().count());
        model.addAttribute("recentAppointments", appointments.stream().limit(5).toList());
        model.addAttribute("pendingEmergencies", emergencyRequestService.countPending());
        
        return "doctor/dashboard";
    }

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Doctor doctor = getCurrentDoctor(userDetails);
        model.addAttribute("doctor", doctor);
        return "doctor/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                @RequestParam String fullName,
                                @RequestParam String phone,
                                @RequestParam String specialization,
                                @RequestParam String qualification,
                                @RequestParam(required = false) Integer experienceYears,
                                @RequestParam(required = false) String hospitalAffiliation,
                                @RequestParam(required = false) Double consultationFee,
                                @RequestParam(required = false) String bio,
                                RedirectAttributes redirectAttributes) {
        Doctor doctor = getCurrentDoctor(userDetails);
        
        User user = doctor.getUser();
        user.setFullName(fullName);
        user.setPhone(phone);
        userService.updateUser(user);
        
        doctor.setSpecialization(specialization);
        doctor.setQualification(qualification);
        doctor.setExperienceYears(experienceYears);
        doctor.setHospitalAffiliation(hospitalAffiliation);
        doctor.setConsultationFee(consultationFee);
        doctor.setBio(bio);
        doctorService.save(doctor);
        
        redirectAttributes.addFlashAttribute("message", "Profile updated successfully");
        return "redirect:/doctor/profile";
    }

    @GetMapping("/slots")
    public String slots(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Doctor doctor = getCurrentDoctor(userDetails);
        List<Slot> slots = slotService.findByDoctor(doctor);
        model.addAttribute("slots", slots);
        return "doctor/slots";
    }

    @PostMapping("/slots/create")
    public String createSlot(@AuthenticationPrincipal UserDetails userDetails,
                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
                              @RequestParam(defaultValue = "1") Integer maxPatients,
                              RedirectAttributes redirectAttributes) {
        Doctor doctor = getCurrentDoctor(userDetails);
        
        Slot slot = new Slot();
        slot.setDoctor(doctor);
        slot.setDate(date);
        slot.setStartTime(startTime);
        slot.setEndTime(endTime);
        slot.setMaxPatients(maxPatients);
        slotService.createSlot(slot);
        
        redirectAttributes.addFlashAttribute("message", "Slot created successfully");
        return "redirect:/doctor/slots";
    }

    @PostMapping("/slots/{id}/delete")
    public String deleteSlot(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        slotService.deleteSlot(id);
        redirectAttributes.addFlashAttribute("message", "Slot deleted successfully");
        return "redirect:/doctor/slots";
    }

    @GetMapping("/appointments")
    public String appointments(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Doctor doctor = getCurrentDoctor(userDetails);
        List<Appointment> appointments = doctorService.getDoctorAppointments(doctor);
        model.addAttribute("appointments", appointments);
        return "doctor/appointments";
    }

    @GetMapping("/booking-requests")
    public String bookingRequests(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Doctor doctor = getCurrentDoctor(userDetails);
        List<Appointment> pendingAppointments = doctorService.getPendingAppointments(doctor);
        model.addAttribute("appointments", pendingAppointments);
        return "doctor/booking-requests";
    }

    @PostMapping("/appointments/{id}/approve")
    public String approveAppointment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        appointmentService.updateStatus(id, Appointment.AppointmentStatus.APPROVED);
        redirectAttributes.addFlashAttribute("message", "Appointment approved successfully");
        return "redirect:/doctor/booking-requests";
    }

    @PostMapping("/appointments/{id}/reject")
    public String rejectAppointment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        appointmentService.updateStatus(id, Appointment.AppointmentStatus.REJECTED);
        redirectAttributes.addFlashAttribute("message", "Appointment rejected");
        return "redirect:/doctor/booking-requests";
    }

    @PostMapping("/appointments/{id}/complete")
    public String completeAppointment(@PathVariable Long id, 
                                       @RequestParam(required = false) String notes,
                                       RedirectAttributes redirectAttributes) {
        if (notes != null && !notes.isEmpty()) {
            appointmentService.addNotes(id, notes);
        }
        appointmentService.updateStatus(id, Appointment.AppointmentStatus.COMPLETED);
        redirectAttributes.addFlashAttribute("message", "Appointment marked as completed");
        return "redirect:/doctor/appointments";
    }

    @GetMapping("/patients")
    public String patients(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Doctor doctor = getCurrentDoctor(userDetails);
        List<Appointment> appointments = doctorService.getDoctorAppointments(doctor);
        List<Patient> patients = appointments.stream()
                .map(Appointment::getPatient)
                .distinct()
                .toList();
        model.addAttribute("patients", patients);
        return "doctor/patients";
    }

    @GetMapping("/patients/{id}")
    public String patientDetails(@AuthenticationPrincipal UserDetails userDetails,
                                  @PathVariable Long id, Model model) {
        Doctor doctor = getCurrentDoctor(userDetails);
        Patient patient = patientService.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        
        List<DocumentPermission> permissions = medicalRecordService.getDoctorPermissions(doctor);
        List<MedicalRecord> accessibleRecords = permissions.stream()
                .filter(p -> p.getPatient().getId().equals(id))
                .map(DocumentPermission::getMedicalRecord)
                .toList();
        
        model.addAttribute("patient", patient);
        model.addAttribute("accessibleRecords", accessibleRecords);
        return "doctor/patient-details";
    }

    @GetMapping("/medical-records")
    public String medicalRecords(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Doctor doctor = getCurrentDoctor(userDetails);
        List<DocumentPermission> permissions = medicalRecordService.getDoctorPermissions(doctor);
        model.addAttribute("permissions", permissions);
        return "doctor/medical-records";
    }

    @GetMapping("/prescriptions")
    public String prescriptions(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Doctor doctor = getCurrentDoctor(userDetails);
        List<Prescription> prescriptions = prescriptionService.findByDoctor(doctor);
        List<Appointment> completedAppointments = doctorService.getDoctorAppointments(doctor).stream()
                .filter(a -> a.getStatus() == Appointment.AppointmentStatus.COMPLETED)
                .toList();
        
        model.addAttribute("prescriptions", prescriptions);
        model.addAttribute("completedAppointments", completedAppointments);
        return "doctor/prescriptions";
    }

    @PostMapping("/prescriptions/create")
    public String createPrescription(@AuthenticationPrincipal UserDetails userDetails,
                                      @RequestParam Long appointmentId,
                                      @RequestParam String diagnosis,
                                      @RequestParam String medications,
                                      @RequestParam(required = false) String instructions,
                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate validUntil,
                                      RedirectAttributes redirectAttributes) {
        Doctor doctor = getCurrentDoctor(userDetails);
        Appointment appointment = appointmentService.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        
        prescriptionService.createPrescription(
                appointment.getPatient(), doctor, appointment,
                diagnosis, medications, instructions, validUntil
        );
        
        redirectAttributes.addFlashAttribute("message", "Prescription created successfully");
        return "redirect:/doctor/prescriptions";
    }

    @GetMapping("/reviews")
    public String reviews(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Doctor doctor = getCurrentDoctor(userDetails);
        List<Review> reviews = reviewService.findByDoctor(doctor);
        model.addAttribute("reviews", reviews);
        model.addAttribute("averageRating", doctor.getAverageRating());
        model.addAttribute("totalReviews", doctor.getTotalReviews());
        return "doctor/reviews";
    }

    @GetMapping("/emergency-requests")
    public String emergencyRequests(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Doctor doctor = getCurrentDoctor(userDetails);
        List<EmergencyRequest> pendingRequests = emergencyRequestService.findPending();
        List<EmergencyRequest> myResponses = emergencyRequestService.findByDoctor(doctor);
        
        model.addAttribute("pendingRequests", pendingRequests);
        model.addAttribute("myResponses", myResponses);
        model.addAttribute("statuses", EmergencyRequest.EmergencyStatus.values());
        return "doctor/emergency-requests";
    }

    @PostMapping("/emergency-requests/{id}/respond")
    public String respondToEmergency(@AuthenticationPrincipal UserDetails userDetails,
                                      @PathVariable Long id,
                                      @RequestParam EmergencyRequest.EmergencyStatus status,
                                      @RequestParam String response,
                                      RedirectAttributes redirectAttributes) {
        Doctor doctor = getCurrentDoctor(userDetails);
        emergencyRequestService.respondToRequest(id, doctor, status, response);
        redirectAttributes.addFlashAttribute("message", "Response submitted successfully");
        return "redirect:/doctor/emergency-requests";
    }

    @GetMapping("/document-verification")
    public String documentVerification(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        List<MedicalRecord> unverifiedRecords = medicalRecordService.findUnverifiedRecords();
        model.addAttribute("records", unverifiedRecords);
        return "doctor/document-verification";
    }

    @PostMapping("/documents/{id}/verify")
    public String verifyDocument(@AuthenticationPrincipal UserDetails userDetails,
                                  @PathVariable Long id,
                                  RedirectAttributes redirectAttributes) {
        Doctor doctor = getCurrentDoctor(userDetails);
        medicalRecordService.verifyRecord(id, doctor);
        redirectAttributes.addFlashAttribute("message", "Document verified successfully");
        return "redirect:/doctor/document-verification";
    }
    @GetMapping("/medical-records/{filename}")
    public ResponseEntity<Resource> getMedicalRecord(
            @PathVariable String filename,
            @AuthenticationPrincipal UserDetails userDetails) throws MalformedURLException {

        Doctor doctor = getCurrentDoctor(userDetails);

        Path file = Paths.get("uploads/medical-records").resolve(filename).normalize();
        Resource resource = new UrlResource(file.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.notFound().build();
        }

        // Detect content type (fallback to octet-stream)
        String contentType;
        try {
            contentType = Files.probeContentType(file);
        } catch (IOException e) {
            contentType = null;
        }
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        // For non-PDFs, use attachment so browser downloads instead of PDF viewer
        String disposition = contentType.equals("application/pdf") ? "inline" : "attachment";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition + "; filename=\"" + resource.getFilename() + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    @GetMapping("/medical-records/view/{filename}")
    public ResponseEntity<Resource> viewMedicalRecord(
            @PathVariable String filename,
            @AuthenticationPrincipal UserDetails userDetails) throws MalformedURLException, IOException {

        Doctor doctor = getCurrentDoctor(userDetails);

        Path file = Paths.get("uploads/medical-records").resolve(filename).normalize();
        Resource resource = new UrlResource(file.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.notFound().build();
        }

        String contentType = Files.probeContentType(file);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        // Always inline so browser tries to display it instead of downloading
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + resource.getFilename() + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }


}
