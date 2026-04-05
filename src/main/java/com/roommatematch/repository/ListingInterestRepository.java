package com.roommatematch.repository;

import com.roommatematch.model.entity.Listing;
import com.roommatematch.model.entity.ListingInterest;
import com.roommatematch.model.entity.Match;
import com.roommatematch.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ListingInterestRepository extends JpaRepository<ListingInterest, Long> {

    List<ListingInterest> findByListing(Listing listing);

    List<ListingInterest> findByUserAndMatch(User user, Match match);

    Optional<ListingInterest> findByUserAndListing(User user, Listing listing);

    List<ListingInterest> findByMatch(Match match);

    boolean existsByUserAndListing(User user, Listing listing);
}
