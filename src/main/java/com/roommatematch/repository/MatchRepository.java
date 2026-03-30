package com.roommatematch.repository;

import com.roommatematch.model.entity.Match;
import com.roommatematch.model.entity.User;
import com.roommatematch.model.enums.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {

    List<Match> findByRequesterOrReceiver(User requester, User receiver);

    Optional<Match> findByRequesterAndReceiver(User requester, User receiver);

    Optional<Match> findByRequesterAndReceiverOrReceiverAndRequester(
            User requester, User receiver, User receiver2, User requester2);

    List<Match> findByRequesterOrReceiverAndStatus(
            User requester, User receiver, MatchStatus status);

    boolean existsByRequesterAndReceiver(User requester, User receiver);
}
