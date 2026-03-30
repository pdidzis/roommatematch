package com.roommatematch.repository;

import com.roommatematch.model.entity.Listing;
import com.roommatematch.model.entity.User;
import com.roommatematch.model.enums.ListingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ListingRepository extends JpaRepository<Listing, Long> {

    List<Listing> findByCityIgnoreCaseAndStatusAndIsVerified(
            String city, ListingStatus status, boolean isVerified);

    List<Listing> findByLandlord(User landlord);

    List<Listing> findByStatus(ListingStatus status);

    Page<Listing> findByStatusAndIsVerified(
            ListingStatus status, boolean isVerified, Pageable pageable);
}
