package br.edu.fatecpg.reviu.controllers;

import br.edu.fatecpg.reviu.domain.user.User;
import br.edu.fatecpg.reviu.domain.user.UserOnboarding;
import br.edu.fatecpg.reviu.domain.user.VerificationCodes;
import br.edu.fatecpg.reviu.dto.requests.*;
import br.edu.fatecpg.reviu.dto.responses.LoginAndRegisterResponseDTO;
import br.edu.fatecpg.reviu.infra.security.TokenService;
import br.edu.fatecpg.reviu.repositories.UserOnboardingRepository;
import br.edu.fatecpg.reviu.repositories.UserRepository;
import br.edu.fatecpg.reviu.repositories.VerificationCodesRepository;
import br.edu.fatecpg.reviu.services.AuthService;
import br.edu.fatecpg.reviu.services.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Endpoints responsáveis pela autenticação, registro e verificação de usuários.")
public class AuthController {

    private final UserOnboardingRepository onboardingRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final VerificationCodesRepository verificationCodesRepository;

    @PostMapping("/login")
    @Operation(summary = "Realiza o login do usuário", description = "Valida as credenciais enviadas e retorna um token JWT para autenticação.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Credenciais inválidas"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO body) {
        User user = this.userRepository.findByEmail(body.email())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (passwordEncoder.matches(body.password(), user.getPassword())) {
            String token = this.tokenService.generateToken(user);
            return ResponseEntity.ok(new LoginAndRegisterResponseDTO(user.getName(), token));
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/register")
    @Operation(summary = "Registra um novo usuário", description = "Cria um novo usuário no sistema, envia o código de verificação por e-mail e retorna uma mensagem de confirmação.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registro realizado com sucesso, verifique seu e-mail"),
            @ApiResponse(responseCode = "400", description = "E-mail já cadastrado ou dados inválidos")
    })
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestDTO body) {
        Optional<User> user = this.userRepository.findByEmail(body.email());
        if (user.isEmpty()) {
            User newUser = new User();
            newUser.setPassword(passwordEncoder.encode(body.password()));
            newUser.setEmail(body.email());
            newUser.setName(body.name());
            newUser.setUsername(body.username());

            this.userRepository.save(newUser);

            UserOnboarding onboarding = new UserOnboarding();
            onboarding.setUser(newUser);
            onboarding.setFocoEstudos(body.focoEstudos());
            onboarding.setPrazoFinal(body.prazoFinal());
            onboarding.setMetaDiaria(body.metaDiaria());
            onboarding.setHorarioFoco(body.horarioFoco());
            onboarding.setRetencaoConteudo(body.retencaoConteudo());
            onboarding.setRitmoRevisao(body.ritmoRevisao());

            onboardingRepository.save(onboarding);

            String code = authService.generateCode();

            verificationCodesRepository
                    .save(new VerificationCodes(
                            null,
                            code,
                            Instant.now().plus(1, ChronoUnit.HOURS),
                            newUser));

            emailService.sendVerificationEmail(newUser);
            return ResponseEntity.ok("Verifique seu e-mail");
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/verify")
    @Operation(summary = "Verifica o e-mail do usuário", description = "Valida o código enviado para o e-mail e ativa a conta do usuário, retornando um token de autenticação.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conta verificada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Código inválido ou expirado")
    })
    public ResponseEntity<?> verifyEmail(@RequestBody VerifyCodeDTO body) {
        Optional<VerificationCodes> optionalVerificationCode = verificationCodesRepository.findByCode(body.code());

        VerificationCodes verificationCode = optionalVerificationCode.orElse(null);

        if (verificationCode == null) {
            return ResponseEntity.badRequest().body("Código inválido.");
        }

        Optional<User> optionalUser = userRepository.findById(verificationCode.getUser().getId());

        if (optionalUser.isEmpty()) {
            return ResponseEntity.badRequest().body("Código inválido.");
        }

        User user = optionalUser.get();

        if (user.getVerified()) {
            return ResponseEntity.ok("Usuário já verificado.");
        }

        if (verificationCode.getExpiry() != null &&
                Instant.now().isAfter(verificationCode.getExpiry())) {
            return ResponseEntity.badRequest().body("Código expirado. Solicite um novo.");
        }

        user.setVerified(true);
        userRepository.save(user);
        verificationCodesRepository.delete(verificationCode);

        String token = this.tokenService.generateToken(user);
        return ResponseEntity.ok(new LoginAndRegisterResponseDTO(user.getName(), token));
    }

    @PostMapping("/resend-verification")
    @Operation(summary = "Reenvia o código de verificação", description = "Gera um novo código de verificação e envia para o e-mail do usuário.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Novo código enviado"),
            @ApiResponse(responseCode = "400", description = "Usuário não encontrado ou já verificado")
    })
    public ResponseEntity<?> resendVerification(@RequestBody EmailRequestDTO body) {
        Optional<User> optionalUser = userRepository.findByEmail(body.email());

        if (optionalUser.isEmpty()) {
            return ResponseEntity.badRequest().body("Usuário não encontrado.");
        }

        User user = optionalUser.get();

        if (user.getVerified()) {
            return ResponseEntity.badRequest().body("Usuário já verificado.");
        }

        String newCode = authService.generateCode();

        Optional<VerificationCodes> optionalVerificationCode = verificationCodesRepository.findByUser(user);

        VerificationCodes verificationCode;

        if (optionalVerificationCode.isEmpty()) {
            verificationCode = verificationCodesRepository
                    .save(new VerificationCodes(
                            null,
                            newCode,
                            Instant.now().plus(1, ChronoUnit.HOURS),
                            user));
        } else {
            verificationCode = optionalVerificationCode.get();

            verificationCode.setCode(newCode);
            verificationCode.setExpiry(Instant.now().plus(1, ChronoUnit.HOURS));
        }

        verificationCodesRepository.save(verificationCode);

        emailService.sendVerificationEmail(user);

        return ResponseEntity.ok("Novo código enviado para o seu e-mail.");
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Envia código de redefinição de senha", description = "Gera um código temporário para redefinir a senha e envia para o e-mail do usuário.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Código de redefinição enviado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<?> forgotPassword(@RequestBody EmailRequestDTO request) {
        User user = this.userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String code = authService.generateCode();

        verificationCodesRepository
                .save(new VerificationCodes(
                        null,
                        code,
                        Instant.now().plus(1, ChronoUnit.HOURS),
                        user));

        emailService.sendForgotPasswordEmail(user);
        return ResponseEntity.ok("Código de redefinição de senha enviado para o seu e-mail.");
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Redefine a senha usando o código de recuperação", description = "Valida o código enviado ao e-mail do usuário e permite a criação de uma nova senha.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Senha alterada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Usuário não encontrado, código inválido ou expirado")
    })
    public ResponseEntity<?> resetForgotPassword(@Valid @RequestBody ResetForgotPasswordDTO request) {
        Optional<VerificationCodes> optionalVerificationCode = verificationCodesRepository.findByCode(request.code());
        VerificationCodes verificationCode = optionalVerificationCode.orElse(null);
        if (verificationCode == null) {
            return ResponseEntity.badRequest().body("Código inválido.");
        }

        Optional<User> optionalUser = userRepository.findById(verificationCode.getUser().getId());

        if (optionalUser.isEmpty()) {
            return ResponseEntity.badRequest().body("Usuário não encontrado.");
        }

        User user = optionalUser.get();

        if (verificationCode.getExpiry() != null &&
                Instant.now().isAfter(verificationCode.getExpiry())) {
            return ResponseEntity.badRequest().body("Código expirado. Solicite um novo.");
        }

        // Redefine a senha do usuário
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        verificationCodesRepository.delete(verificationCode);

        return ResponseEntity.ok("Senha alterada com sucesso!");
    }
}
