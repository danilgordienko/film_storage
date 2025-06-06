package ru.danilgordienko.film_storage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.danilgordienko.film_storage.model.FriendRequest;
import ru.danilgordienko.film_storage.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    Optional<FriendRequest> findBySenderAndReceiver(User sender, User receiver);

    List<FriendRequest> findBySender(User sender);
    List<FriendRequest> findByReceiver(User receiver);
}
