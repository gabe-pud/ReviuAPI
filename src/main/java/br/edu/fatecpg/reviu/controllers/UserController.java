package br.edu.fatecpg.reviu.controllers;

import br.edu.fatecpg.reviu.domain.user.User;
import br.edu.fatecpg.reviu.dto.requests.ChangePasswordDTO;
import br.edu.fatecpg.reviu.dto.responses.FriendResponseDTO;
import br.edu.fatecpg.reviu.dto.responses.UserResponseDTO;
import br.edu.fatecpg.reviu.repositories.FriendshipRepository;
import br.edu.fatecpg.reviu.repositories.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@RestController
@RequestMapping("/user")
@Tag(
        name = "Usuário",
        description = "Endpoints relacionados à gestão de dados do usuário autenticado."
)
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private FriendshipRepository friendshipRepository;

    @PutMapping("/change-password")
    @Operation(
            summary = "Altera a senha do usuário",
            description = "Permite que o usuário autenticado altere sua senha atual para uma nova senha."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Senha alterada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Senha atual incorreta ou dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordDTO body) {

        User userPrincipal = (User) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        // Busca o usuário no banco
        User user = userRepository.findByEmail(userPrincipal.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Valida a senha atual
        if (!passwordEncoder.matches(body.currentPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body("Senha atual incorreta.");
        }

        // Atualiza a senha
        user.setPassword(passwordEncoder.encode(body.newPassword()));
        userRepository.save(user);

        return ResponseEntity.ok("Senha alterada com sucesso!");
    }

    @GetMapping("/info")
    @Operation(
            summary = "Obtém informações do usuário",
            description = "Retorna as informações do usuário autenticado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Informações do usuário obtidas com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<UserResponseDTO> getUserInfo() {
        User userPrincipal = (User) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        User user = userRepository.findByEmail(userPrincipal.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        return ResponseEntity.ok(new UserResponseDTO(user));
    }

    @GetMapping("/friends")
    @Operation(
            summary = "Lista os amigos do usuário autenticado",
            description = "Retorna os usuários que possuem uma amizade aceita com o usuário autenticado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Amigos retornados com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    public ResponseEntity<List<FriendResponseDTO>> getFriends() {
        User authenticatedUser = (User) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Map<Long, User> acceptedFriends = new LinkedHashMap<>();
        Stream.concat(
                        friendshipRepository.findAcceptedFriendsAsUser(authenticatedUser.getId()).stream(),
                        friendshipRepository.findAcceptedFriendsAsFriend(authenticatedUser.getId()).stream()
                )
                .forEach(friend -> acceptedFriends.putIfAbsent(friend.getId(), friend));

        List<FriendResponseDTO> friends = acceptedFriends.values().stream()
                .map(FriendResponseDTO::new)
                .toList();

        return ResponseEntity.ok(friends);
    }
}
