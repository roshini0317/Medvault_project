package com.medvault.controller;

import com.medvault.model.*;
import com.medvault.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid username or password");
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully");
        }
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterChoice() {
        return "register-choice";
    }

    @GetMapping("/register/patient")
    public String showPatientRegister(Model model) {
        model.addAttribute("user", new User());
        return "register-patient";
    }

    @PostMapping("/register/patient")
    public String registerPatient(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        if (userService.existsByUsername(user.getUsername())) {
            redirectAttributes.addFlashAttribute("error", "Username already exists");
            return "redirect:/register/patient";
        }
        if (userService.existsByEmail(user.getEmail())) {
            redirectAttributes.addFlashAttribute("error", "Email already exists");
            return "redirect:/register/patient";
        }
        
        userService.registerPatient(user);
        redirectAttributes.addFlashAttribute("message", "Registration successful! Please login.");
        return "redirect:/login";
    }

    @GetMapping("/register/doctor")
    public String showDoctorRegister(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("doctor", new Doctor());
        return "register-doctor";
    }

    @PostMapping("/register/doctor")
    public String registerDoctor(@ModelAttribute User user, 
                                  @RequestParam String specialization,
                                  @RequestParam String qualification,
                                  @RequestParam String licenseNumber,
                                  @RequestParam(required = false) Integer experienceYears,
                                  @RequestParam(required = false) String hospitalAffiliation,
                                  @RequestParam(required = false) Double consultationFee,
                                  @RequestParam(required = false) String bio,
                                  RedirectAttributes redirectAttributes) {
        if (userService.existsByUsername(user.getUsername())) {
            redirectAttributes.addFlashAttribute("error", "Username already exists");
            return "redirect:/register/doctor";
        }
        if (userService.existsByEmail(user.getEmail())) {
            redirectAttributes.addFlashAttribute("error", "Email already exists");
            return "redirect:/register/doctor";
        }
        
        Doctor doctor = new Doctor();
        doctor.setSpecialization(specialization);
        doctor.setQualification(qualification);
        doctor.setLicenseNumber(licenseNumber);
        doctor.setExperienceYears(experienceYears);
        doctor.setHospitalAffiliation(hospitalAffiliation);
        doctor.setConsultationFee(consultationFee);
        doctor.setBio(bio);
        
        userService.registerDoctor(user, doctor);
        redirectAttributes.addFlashAttribute("message", "Registration successful! Your account is pending verification.");
        return "redirect:/login";
    }
}
