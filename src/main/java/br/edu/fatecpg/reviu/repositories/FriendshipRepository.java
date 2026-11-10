package br.edu.fatecpg.reviu.repositories;

import br.edu.fatecpg.reviu.domain.user.Friendship;
import br.edu.fatecpg.reviu.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    @Query("""
            SELECT f.friend
            FROM Friendship f
            WHERE f.user.id = :userId
              AND f.status = 'ACCEPTED'
            """)
    List<User> findAcceptedFriendsAsUser(
            @Param("userId") Long userId
    );

    @Query("""
            SELECT f.user
            FROM Friendship f
            WHERE f.friend.id = :userId
              AND f.status = 'ACCEPTED'
            """)
    List<User> findAcceptedFriendsAsFriend(
            @Param("userId") Long userId
    );

    @Query("""
            SELECT f
            FROM Friendship f
            WHERE (f.user = :user AND f.friend = :friend)
               OR (f.user = :friend AND f.friend = :user)
            """)
    java.util.Optional<Friendship> findFriendshipBetween(
            @Param("user") User user,
            @Param("friend") User friend
    );

    @Query("""
            SELECT f
            FROM Friendship f
            WHERE f.user.id = :senderId
              AND f.friend.id = :receiverId
              AND f.status = 'PENDING'
            """)
    java.util.Optional<Friendship> findPendingRequest(
            @Param("senderId") Long senderId,
            @Param("receiverId") Long receiverId
    );
}
