package br.edu.fatecpg.reviu.dto.responses;

import br.edu.fatecpg.reviu.domain.user.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "UserResponseDTO",
        description = "DTO de resposta com os dados públicos do usuário autenticado"
)
public record UserResponseDTO(

        @Schema(description = "Identificador do usuário", example = "1")
        Long id,

        @Schema(description = "Nome do usuário", example = "Thiago Almeida")
        String name,

        @Schema(description = "Nome de usuário", example = "thiago.almeida")
        String username,

        @Schema(description = "E-mail do usuário", example = "thiago@example.com")
        String email,

        @Schema(description = "URL da imagem de perfil", example = "https://res.cloudinary.com/demo/image/upload/profile.jpg")
        String profileImageUrl,

        @Schema(description = "Indica se o e-mail do usuário foi verificado", example = "true")
        Boolean verified

) {
    public UserResponseDTO(User user) {
        this(
                user.getId(),
                user.getName(),
                user.getUsername(),
                user.getEmail(),
                user.getProfileImageUrl(),
                user.getVerified()
        );
    }
}
