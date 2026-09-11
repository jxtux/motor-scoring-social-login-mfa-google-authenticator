package com.finanscore.motorscoring.presentation.security.controller;

import com.finanscore.motorscoring.application.security.command.*;
import com.finanscore.motorscoring.application.security.model.IssuedSession;
import com.finanscore.motorscoring.application.security.model.RegistrationStage;
import com.finanscore.motorscoring.application.security.port.in.*;
import com.finanscore.motorscoring.presentation.security.dto.AuthDtos.*;
import org.springframework.context.annotation.Profile;
import org.springframework.beans.factory.annotation.Value;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@Profile("api")
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final RegisterUserUseCase register;
    private final VerifyEmailUseCase verifyEmail;
    private final LoginLocalUseCase login;
    private final SetupMfaUseCase setupMfa;
    private final ConfirmMfaUseCase confirmMfa;
    private final VerifyMfaUseCase verifyMfa;
    private final RefreshSessionUseCase refresh;
    private final LogoutUseCase logout;
    private final boolean refreshCookieSecure;
    private final String refreshCookieSameSite;

    public AuthController(RegisterUserUseCase register, VerifyEmailUseCase verifyEmail,
                          LoginLocalUseCase login, SetupMfaUseCase setupMfa,
                          ConfirmMfaUseCase confirmMfa, VerifyMfaUseCase verifyMfa,
                          RefreshSessionUseCase refresh, LogoutUseCase logout,
                          @Value("${iam.refresh-cookie.secure:true}") boolean refreshCookieSecure,
                          @Value("${iam.refresh-cookie.same-site:None}") String refreshCookieSameSite) {
        this.register=register;
        this.verifyEmail=verifyEmail;
        this.login=login;
        this.setupMfa=setupMfa;
        this.confirmMfa=confirmMfa;
        this.verifyMfa=verifyMfa;
        this.refresh=refresh;
        this.logout=logout;
        this.refreshCookieSecure=refreshCookieSecure;
        this.refreshCookieSameSite=refreshCookieSameSite;
    }

    @PostMapping("/register")
    public ResponseEntity<RegistrationStage> register(@Valid @RequestBody RegisterRequest r) {
        RegistrationStage stage = register.register(
            new RegisterUserCommand(r.displayName(), r.email(), r.password()));
        return ResponseEntity.status(HttpStatus.CREATED).body(stage);
    }

    @PostMapping("/verify-email")
    public java.util.Map<String,String> verifyEmail(@Valid @RequestBody VerifyEmailRequest r) {
        String setupToken = verifyEmail.verify(new VerifyEmailCommand(r.email(), r.code()));
        return java.util.Map.of("setupToken", setupToken, "nextStep", "MFA_SETUP");
    }

    @PostMapping("/login")
    public Object login(@Valid @RequestBody LoginRequest r) {
        return login.login(new LoginLocalCommand(r.email(), r.password()));
    }

    @PostMapping("/mfa/setup")
    public Object setup(@Valid @RequestBody SetupMfaRequest r) {
        return setupMfa.setup(r.setupToken());
    }

    /**
     * Primer código de Google Authenticator: confirma TOTP y completa el alta.
     * La respuesta ya contiene el Access JWT y deja el refresh token en cookie HttpOnly.
     */
    @PostMapping("/mfa/confirm")
    public AccessTokenResponse confirm(@Valid @RequestBody ConfirmMfaRequest r,
                                       HttpServletRequest request,
                                       HttpServletResponse response) {
        IssuedSession s = confirmMfa.confirm(new ConfirmMfaCommand(
            r.setupToken(), r.code(), clientIp(request), request.getHeader("User-Agent")));
        setRefreshCookie(response, s.refreshToken(), s.refreshExpiresInSeconds());
        return new AccessTokenResponse(s.accessToken(), s.accessExpiresInSeconds());
    }

    @PostMapping("/mfa/verify")
    public AccessTokenResponse verify(@Valid @RequestBody VerifyMfaRequest r,
                                      HttpServletRequest request,
                                      HttpServletResponse response) {
        IssuedSession s = verifyMfa.verify(new VerifyMfaCommand(
            r.challengeToken(), r.code(), clientIp(request), request.getHeader("User-Agent")));
        setRefreshCookie(response, s.refreshToken(), s.refreshExpiresInSeconds());
        return new AccessTokenResponse(s.accessToken(), s.accessExpiresInSeconds());
    }

    @PostMapping("/refresh")
    public AccessTokenResponse refresh(@CookieValue(name="fs_refresh", required=false) String token,
                                       HttpServletRequest request,
                                       HttpServletResponse response) {
        IssuedSession s = refresh.refresh(token, clientIp(request), request.getHeader("User-Agent"));
        setRefreshCookie(response, s.refreshToken(), s.refreshExpiresInSeconds());
        return new AccessTokenResponse(s.accessToken(), s.accessExpiresInSeconds());
    }

    @PostMapping("/logout")
    public MessageResponse logout(@CookieValue(name="fs_refresh", required=false) String token,
                                  HttpServletResponse response) {
        logout.logout(token);
        ResponseCookie cookie = ResponseCookie.from("fs_refresh", "")
            .httpOnly(true).secure(refreshCookieSecure).sameSite(refreshCookieSameSite)
            .path("/api/v1/auth").maxAge(0).build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return new MessageResponse("Sesión cerrada.");
    }

    private void setRefreshCookie(HttpServletResponse response, String token, long ttl) {
        ResponseCookie cookie = ResponseCookie.from("fs_refresh", token)
            .httpOnly(true).secure(refreshCookieSecure).sameSite(refreshCookieSameSite)
            .path("/api/v1/auth").maxAge(ttl).build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private String clientIp(HttpServletRequest r) {
        String f = r.getHeader("X-Forwarded-For");
        return f != null ? f.split(",")[0].trim() : r.getRemoteAddr();
    }
}
