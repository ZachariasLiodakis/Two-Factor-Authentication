package com.fa._FASim.Controllers;

import com.fa._FASim.Entities.User;
import com.fa._FASim.Services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
public class TwoFactorController {

    @Autowired
    UserService userService;

    // Display the 2FA verification form
    @GetMapping("/2fa")
    public String show2faForm() {
        return "auth/2fa-form";  // Return the Thymeleaf template for 2FA input
    }

    // Handle the submission of the 2FA verification code
    @PostMapping("/2fa")
    public String verify2fa(@RequestParam("code") int code,
                            Principal principal,
                            HttpServletRequest request) throws Exception {
        // Find the logged-in user by username
        User user = userService.findByUsername(principal.getName()).orElseThrow();

        // Verify the 2FA code using the user's secret
        boolean verified = userService.verifyCode(user.getSecret(), code);

        if (verified) {
            // Mark in session that 2FA was successfully passed
            request.getSession().setAttribute("2faPassed", true);
            // Redirect to home page or other secured page
            return "redirect:/";
        } else {
            // If verification failed, reload the form with an error flag
            return "auth/2fa-form?error";
        }
    }
}
