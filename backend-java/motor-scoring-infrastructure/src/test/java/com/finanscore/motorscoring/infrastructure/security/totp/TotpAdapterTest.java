package com.finanscore.motorscoring.infrastructure.security.totp;

import org.junit.jupiter.api.Test;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;

class TotpAdapterTest {
    /*
     * RFC 6238 SHA-1 test secret:
     * ASCII "12345678901234567890" en Base32.
     *
     * Para T=59, el RFC da 94287082 con 8 dígitos.
     * El mismo truncado a 6 dígitos da 287082.
     */
    private static final String SECRET =
        "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ";

    @Test
    void generaCodigoEsperadoParaVentanaRfc() {
        TotpAdapter adapter =
            new TotpAdapter(
                Clock.fixed(
                    Instant.ofEpochSecond(59),
                    ZoneOffset.UTC));

        assertEquals(
            "287082",
            adapter.generate(
                SECRET,
                1));
    }

    @Test
    void aceptaVentanaActualYUnaAnterior() {
        // 61 s => ventana 2.
        TotpAdapter adapter =
            new TotpAdapter(
                Clock.fixed(
                    Instant.ofEpochSecond(61),
                    ZoneOffset.UTC));

        String previousCode =
            adapter.generate(
                SECRET,
                1);

        String currentCode =
            adapter.generate(
                SECRET,
                2);

        assertTrue(
            adapter.verify(
                SECRET,
                previousCode));

        assertTrue(
            adapter.verify(
                SECRET,
                currentCode));
    }

    @Test
    void rechazaCodigoFueraDeTolerancia() {
        TotpAdapter adapter =
            new TotpAdapter(
                Clock.fixed(
                    Instant.ofEpochSecond(120),
                    ZoneOffset.UTC));

        String oldCode =
            adapter.generate(
                SECRET,
                1);

        assertFalse(
            adapter.verify(
                SECRET,
                oldCode));
    }
}
