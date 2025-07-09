package com.fa._FASim.Security;

import com.fa._FASim.Services.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;

    // Inject UserService via constructor for user-related operations
    @Autowired
    public CustomAuthenticationSuccessHandler(UserService userService) {
        this.userService = userService;
    }

    /**
     * This method is called upon successful authentication.
     * It checks if the user has Two-Factor Authentication enabled.
     * If yes, redirects to the 2FA verification page.
     * Otherwise, redirects to the home page.
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        // Get username of authenticated user
        String username = authentication.getName();

        // Check if 2FA is enabled for this user
        boolean isTwoFactorEnabled = userService.findByUsername(username)
                .map(user -> user.isTwoFactorEnabled())
                .orElse(false);

        if (isTwoFactorEnabled) {
            // Redirect to 2FA verification page if enabled
            response.sendRedirect("/2fa");
        } else {
            // Redirect to home page otherwise
            response.sendRedirect("/");
        }
    }
}
