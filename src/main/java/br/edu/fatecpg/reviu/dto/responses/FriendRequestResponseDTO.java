package br.edu.fatecpg.reviu.dto.responses;

import br.edu.fatecpg.reviu.domain.user.Friendship;
import br.edu.fatecpg.reviu.domain.user.User;

public record FriendRequestResponseDTO(
        Long id,
        String name,
        String username,
        String profileImageUrl,
        String requestStatus
) {
    public FriendRequestResponseDTO(Friendship friendship, Long authenticatedUserId) {
        this(
                getOtherUser(friendship, authenticatedUserId).getId(),
                getOtherUser(friendship, authenticatedUserId).getName(),
                getOtherUser(friendship, authenticatedUserId).getUsername(),
                getOtherUser(friendship, authenticatedUserId).getProfileImageUrl(),
                friendship.getUser().getId().equals(authenticatedUserId) ? "SENT" : "RECIVED"
        );
    }

    private static User getOtherUser(Friendship friendship, Long authenticatedUserId) {
        return friendship.getUser().getId().equals(authenticatedUserId)
                ? friendship.getFriend()
                : friendship.getUser();
    }
}
