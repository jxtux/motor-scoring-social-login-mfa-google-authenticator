package com.finanscore.motorscoring.infrastructure.security.crypto;

import org.junit.jupiter.api.Test;
import java.util.Base64;
import static org.junit.jupiter.api.Assertions.*;

class AesGcmSecretCipherAdapterTest {
    @Test
    void cifraYDescifraSecretoTotp() {
        byte[] rawKey =
            new byte[32];

        java.util.Arrays.fill(
            rawKey,
            (byte) 7);

        AesGcmSecretCipherAdapter adapter =
            new AesGcmSecretCipherAdapter(
                Base64.getEncoder()
                    .encodeToString(rawKey));

        String secret =
            "JBSWY3DPEHPK3PXP";

        String encrypted =
            adapter.encrypt(secret);

        assertNotEquals(
            secret,
            encrypted);

        assertEquals(
            secret,
            adapter.decrypt(encrypted));
    }
}
