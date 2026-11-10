package br.edu.fatecpg.reviu.dto.responses;

import br.edu.fatecpg.reviu.domain.user.User;

public record FriendResponseDTO(
        Long id,
        String name,
        String username,
        String profileImageUrl
) {
    public FriendResponseDTO(User user) {
        this(
                user.getId(),
                user.getName(),
                user.getUsername(),
                user.getProfileImageUrl()
        );
    }
}