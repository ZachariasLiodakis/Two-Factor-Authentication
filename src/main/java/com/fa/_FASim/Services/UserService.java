package com.fa._FASim.Services;

import com.fa._FASim.Entities.User;
import com.fa._FASim.Repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Collections;
import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import com.google.common.io.BaseEncoding;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final TimeBasedOneTimePasswordGenerator totp;

    // Constructor injecting repository and password encoder
    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        // Initialize TOTP generator with 30-second time step
        this.totp = new TimeBasedOneTimePasswordGenerator(Duration.ofSeconds(30));
    }

    // Save a new user with encrypted password
    @Transactional
    public User saveUser(User user) {
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        return userRepository.save(user);
    }

    // Update existing user
    @Transactional
    public Integer updateUser(User user) {
        user = userRepository.save(user);
        return user.getId();
    }

    // Load user by username for Spring Security authentication
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    // Check if a user exists by username
    public boolean existsUser(String username) {
        return userRepository.existsByUsername(username);
    }

    // Enable 2FA for a user with a given secret key
    @Transactional
    public void enableTwoFactor(User user, String secret) {
        user.setTwoFactorEnabled(true);
        user.setSecret(secret);
        userRepository.save(user);
    }

    // Disable 2FA and clear secret
    @Transactional
    public void disableTwoFactor(User user) {
        user.setTwoFactorEnabled(false);
        user.setSecret(null);
        userRepository.save(user);
    }

    // Find user by username, returning Optional<User>
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Generate a random Base32 secret key compatible with Google Authenticator.
     * Uses the algorithm from the TOTP generator (usually HmacSHA1).
     *
     * @return Base32 encoded secret key string
     * @throws NoSuchAlgorithmException if the algorithm is not available
     */
    public String generateSecret() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(totp.getAlgorithm());
        keyGenerator.init(160); // 160 bits (20 bytes) recommended for TOTP
        SecretKey secretKey = keyGenerator.generateKey();

        // Return Base32-encoded secret, compatible with Google Authenticator
        return BaseEncoding.base32().encode(secretKey.getEncoded());
    }

    /**
     * Generate the otpauth:// URL for the QR code.
     * This URL format is recognized by Google Authenticator and other apps.
     *
     * @param username The user's username (account name)
     * @param secret The Base32-encoded secret key
     * @return The otpauth URL as a String
     */
    public String generateOtpAuthUrl(String username, String secret) {
        String issuer = "MyAppName"; // Change to your application name
        String encodedSecret = URLEncoder.encode(secret, StandardCharsets.UTF_8);
        String encodedIssuer = URLEncoder.encode(issuer, StandardCharsets.UTF_8);
        return String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=6&period=30",
                encodedIssuer, username, encodedSecret, encodedIssuer);
    }

    /**
     * Verify a TOTP code entered by the user.
     * Uses a ±1 time-step window to allow for slight clock skew.
     *
     * Note: Decodes the secret using Base32 (NOT Base64).
     *
     * @param secret The Base32 encoded secret key
     * @param code The user-provided OTP code (usually 6 digits)
     * @return true if the code matches, false otherwise
     * @throws Exception if generation fails
     */
    public boolean verifyCode(String secret, int code) throws Exception {
        // Decode Base32 secret key to bytes
        byte[] keyBytes = BaseEncoding.base32().decode(secret);
        SecretKey secretKey = new javax.crypto.spec.SecretKeySpec(keyBytes, totp.getAlgorithm());
        long currentTimeMillis = System.currentTimeMillis();

        // Allow a window of ±1 time step (usually ±30 seconds)
        for (int i = -1; i <= 1; i++) {
            long time = currentTimeMillis + (i * totp.getTimeStep().toMillis());
            Instant time2 = Instant.ofEpochMilli(time);
            int generated = totp.generateOneTimePassword(secretKey, time2);
            System.out.println("Generated (offset " + i + "): " + generated);
            if (generated == code) {
                return true;
            }
        }
        return false;
    }

}
