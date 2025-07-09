package com.fa._FASim.Utils;

import com.google.common.io.BaseEncoding;
import java.security.SecureRandom;

public class TotpUtil {

    // Secure random generator for creating secret keys
    private static final SecureRandom random = new SecureRandom();

    /**
     * Generates a random secret key encoded in Base32.
     * This secret is used for TOTP generation and should be
     * shared with the user’s authenticator app (e.g., Google Authenticator).
     *
     * @return Base32-encoded secret key string
     */
    public static String generateSecret() {
        byte[] buffer = new byte[10]; // 80 bits secret (recommended minimum)
        random.nextBytes(buffer);
        // Encode the secret key in Base32 for compatibility with TOTP apps
        return BaseEncoding.base32().encode(buffer);
    }

    /**
     * Constructs the otpauth:// URL used to generate the QR code
     * that users scan with their authenticator app.
     *
     * Format of the URL follows the standard expected by apps like Google Authenticator.
     *
     * @param username The user's username or account name
     * @param secret The Base32-encoded secret key
     * @return The otpauth URI as a string
     */
    public static String generateQrCodeUri(String username, String secret) {
        String issuer = "FASimApp"; // The name of your application or company
        String otpauth = String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=6&period=30",
                issuer, username, secret, issuer
        );
        return otpauth;
    }
}
