package com.fa._FASim.Controllers;

import com.fa._FASim.Entities.User;
import com.fa._FASim.Services.UserService;
import com.fa._FASim.Utils.TotpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@Controller
public class UserController {

    @Autowired
    private UserService userService;

    // GET -> Show the registration page
    @GetMapping("/register")
    public String register(Model model, @RequestParam(value = "error", required = false) String error) {
        model.addAttribute("user", new User());

        // If there was a username conflict error, add flag to model
        if ("exists".equals(error)) {
            model.addAttribute("usernameExists", true);
        }

        return "auth/register";
    }

    // POST -> Handle user registration form submission
    @PostMapping("/register")
    public String saveUser(@ModelAttribute("user") User user, RedirectAttributes redirectAttributes) {
        // Check if username already exists
        if (userService.existsUser(user.getUsername())) {
            return "redirect:/register?error=exists";
        }

        try {
            // If user enabled 2FA, generate secret key
            if (user.isTwoFactorEnabled()) {
                String secret = TotpUtil.generateSecret();
                user.setSecret(secret);
            }

            // Save the new user
            userService.saveUser(user);

            // If 2FA is enabled, redirect to QR code setup page with username parameter
            if (user.isTwoFactorEnabled()) {
                redirectAttributes.addAttribute("username", user.getUsername());
                return "redirect:/2fa-setup";
            }

            // Otherwise redirect to login page
            return "redirect:/login";
        } catch (Exception e) {
            throw new RuntimeException("Saving user failed", e);
        }
    }

    // GET -> Show the 2FA QR code setup page
    @GetMapping("/2fa-setup")
    public String setup2fa(Model model, @RequestParam("username") String username) {
        // Retrieve user by username
        User user = userService.findByUsername(username).orElseThrow();

        // Generate the OTP auth URI for QR code
        String qr = TotpUtil.generateQrCodeUri(user.getUsername(), user.getSecret());

        // URL-encode the OTP auth URI to safely pass it to the QR code API
        String encodedQr = URLEncoder.encode(qr, StandardCharsets.UTF_8);

        // Add the encoded QR code URI to the model for rendering in the view
        model.addAttribute("qrCodeUri", encodedQr);

        return "auth/2fa-setup";
    }

}
