package br.edu.fatecpg.reviu.controllers;

import br.edu.fatecpg.reviu.domain.user.Friendship;
import br.edu.fatecpg.reviu.domain.user.User;
import br.edu.fatecpg.reviu.domain.user.UserOnboarding;
import br.edu.fatecpg.reviu.dto.requests.ChangePasswordDTO;
import br.edu.fatecpg.reviu.dto.requests.OnboardingRequestDTO;
import br.edu.fatecpg.reviu.dto.responses.FriendResponseDTO;
import br.edu.fatecpg.reviu.dto.responses.FriendRequestResponseDTO;
import br.edu.fatecpg.reviu.dto.responses.UserResponseDTO;
import br.edu.fatecpg.reviu.repositories.FriendshipRepository;
import br.edu.fatecpg.reviu.repositories.UserOnboardingRepository;
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

    @Autowired
    private UserOnboardingRepository onboardingRepository;

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

    @GetMapping("/friends/requests")
    @Operation(
            summary = "Lista os pedidos de amizade pendentes do usuário autenticado",
            description = "Retorna os pedidos de amizade pendentes, indicando se foram enviados ou recebidos pelo usuário autenticado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Amigos retornados com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    public ResponseEntity<List<FriendRequestResponseDTO>> getFriendRequests() {
        User authenticatedUser = (User) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Map<Long, Friendship> pendingFriends = new LinkedHashMap<>();
        Stream.concat(
                        friendshipRepository.findPendingFriendsAsUser(authenticatedUser.getId()).stream(),
                        friendshipRepository.findPendingFriendsAsFriend(authenticatedUser.getId()).stream()
                )
                .forEach(friendship -> {
                    User otherUser = friendship.getUser().getId().equals(authenticatedUser.getId())
                            ? friendship.getFriend()
                            : friendship.getUser();
                    pendingFriends.putIfAbsent(otherUser.getId(), friendship);
                });

        List<FriendRequestResponseDTO> friends = pendingFriends.values().stream()
                .map(friendship -> new FriendRequestResponseDTO(friendship, authenticatedUser.getId()))
                .toList();

        return ResponseEntity.ok(friends);
    }

    @PostMapping("/friends/request/{friendId}")
    @Operation(
            summary = "Enviar pedido de amizade",
            description = "Envia um pedido de amizade para outro usuário utilizando o ID dele."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido enviado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Regra de negócio violada (ex: auto-adição, usuário não verificado, ou relação existente)"),
            @ApiResponse(responseCode = "404", description = "Usuário destino não encontrado")
    })
    public ResponseEntity<?> sendFriendRequest(@PathVariable Long friendId) {
        // 1. Pega o usuário logado
        User authenticatedUser = (User) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        // 2. Impede o usuário de adicionar a si mesmo
        if (authenticatedUser.getId().equals(friendId)) {
            return ResponseEntity.badRequest().body("Você não pode enviar um pedido de amizade para si mesmo.");
        }

        // 3. Busca o usuário que vai receber o pedido (Corrigindo o erro 403)
        var targetUserOpt = userRepository.findById(friendId);
        if (targetUserOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Usuário não encontrado.");
        }
        User targetUser = targetUserOpt.get();

        // 4. Impede envio de pedido para usuários que não confirmaram o e-mail (Sua sugestão)
        if (!targetUser.getVerified()) {
            return ResponseEntity.badRequest().body("Não é possível enviar pedido de amizade para uma conta não verificada.");
        }

        // 5. Verifica se já existe um pedido ou amizade entre os dois
        var existingFriendship = friendshipRepository.findFriendshipBetween(authenticatedUser, targetUser);
        if (existingFriendship.isPresent()) {
            return ResponseEntity.badRequest().body("Já existe uma relação de amizade ou pedido pendente com este usuário.");
        }

        // 6. Cria e salva o pedido com status PENDING
        Friendship friendship = new Friendship();
        friendship.setUser(authenticatedUser);
        friendship.setFriend(targetUser);
        friendship.setStatus("PENDING");

        friendshipRepository.save(friendship);

        return ResponseEntity.ok("Pedido de amizade enviado com sucesso!");
    }

    @PutMapping("/friends/request/{senderId}/accept")
    @Operation(
            summary = "Aceitar pedido de amizade",
            description = "Aceita um pedido de amizade pendente enviado por outro usuário."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido aceito com sucesso"),
            @ApiResponse(responseCode = "400", description = "Nenhum pedido pendente encontrado deste usuário"),
    })
    public ResponseEntity<?> acceptFriendRequest(@PathVariable Long senderId) {
        // 1. Pega o usuário logado (quem está recebendo e aceitando o pedido)
        User authenticatedUser = (User) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        // 2. Busca o pedido pendente onde o senderId é quem enviou e o logado é quem recebeu
        var friendshipOpt = friendshipRepository.findPendingRequest(senderId, authenticatedUser.getId());

        if (friendshipOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Nenhum pedido de amizade pendente encontrado para este usuário.");
        }

        // 3. Atualiza o status para ACCEPTED
        Friendship friendship = friendshipOpt.get();
        friendship.setStatus("ACCEPTED");

        friendshipRepository.save(friendship);

        return ResponseEntity.ok("Pedido de amizade aceito com sucesso!");
    }

    @DeleteMapping("/friends/request/{receiverId}/cancel")
    @Operation(
            summary = "Cancelar pedido de amizade",
            description = "Cancela um pedido de amizade pendente que VOCÊ enviou para outro usuário."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido cancelado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Nenhum pedido pendente enviado para este usuário")
    })
    public ResponseEntity<?> cancelFriendRequest(@PathVariable Long receiverId) {
        User authenticatedUser = (User) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        // O usuário logado é o remetente, o target é o destinatário
        var friendshipOpt = friendshipRepository.findPendingRequest(authenticatedUser.getId(), receiverId);

        if (friendshipOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Nenhum pedido de amizade pendente enviado para este usuário.");
        }

        friendshipRepository.delete(friendshipOpt.get());

        return ResponseEntity.ok("Pedido de amizade cancelado com sucesso!");
    }

    @DeleteMapping("/friends/request/{senderId}/reject")
    @Operation(
            summary = "Recusar pedido de amizade",
            description = "Recusa um pedido de amizade pendente que outro usuário enviou para VOCÊ."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido recusado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Nenhum pedido pendente recebido deste usuário")
    })
    public ResponseEntity<?> rejectFriendRequest(@PathVariable Long senderId) {
        User authenticatedUser = (User) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        // O target é o remetente, o usuário logado é o destinatário
        var friendshipOpt = friendshipRepository.findPendingRequest(senderId, authenticatedUser.getId());

        if (friendshipOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Nenhum pedido de amizade pendente recebido deste usuário.");
        }

        friendshipRepository.delete(friendshipOpt.get());

        return ResponseEntity.ok("Pedido de amizade recusado com sucesso!");
    }

    @DeleteMapping("/friends/{friendId}/unfriend")
    @Operation(
            summary = "Desfazer amizade",
            description = "Remove uma amizade existente (status ACCEPTED) com outro usuário."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Amizade desfeita com sucesso"),
            @ApiResponse(responseCode = "400", description = "Vocês não possuem uma amizade ativa"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<?> unfriend(@PathVariable Long friendId) {
        User authenticatedUser = (User) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        // 1. Busca o usuário que será removido da lista de amigos
        var targetUserOpt = userRepository.findById(friendId);
        if (targetUserOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Usuário não encontrado.");
        }
        User targetUser = targetUserOpt.get();

        // 2. Busca qualquer relação existente entre os dois usando o método do primeiro passo
        var friendshipOpt = friendshipRepository.findFriendshipBetween(authenticatedUser, targetUser);

        // 3. Valida se a relação existe E se o status é de amizade aceita
        if (friendshipOpt.isEmpty() || !friendshipOpt.get().getStatus().equals("ACCEPTED")) {
            return ResponseEntity.badRequest().body("Vocês não possuem uma amizade ativa.");
        }

        // 4. Deleta o registro para desfazer a amizade
        friendshipRepository.delete(friendshipOpt.get());

        return ResponseEntity.ok("Amizade desfeita com sucesso!");
    }
    
    @PutMapping("/onboarding")
    @Operation(summary = "Atualizar configuração de onboarding", description = "Atualiza as preferências do algoritmo de repetição espaçada do usuário.")
    public ResponseEntity<?> updateOnboarding(@RequestBody OnboardingRequestDTO dto) {
        User authenticatedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        UserOnboarding onboarding = onboardingRepository.findByUserId(authenticatedUser.getId())
                .orElseThrow(() -> new RuntimeException("Onboarding não encontrado para este usuário."));

        onboarding.setFocoEstudos(dto.focoEstudos());
        onboarding.setPrazoFinal(dto.prazoFinal());
        onboarding.setMetaDiaria(dto.metaDiaria());
        onboarding.setHorarioFoco(dto.horarioFoco());
        onboarding.setRetencaoConteudo(dto.retencaoConteudo());
        onboarding.setRitmoRevisao(dto.ritmoRevisao());

        onboardingRepository.save(onboarding);
        return ResponseEntity.ok("Onboarding atualizado com sucesso!");
    }

}
