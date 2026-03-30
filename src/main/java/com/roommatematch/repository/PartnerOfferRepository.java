package com.roommatematch.repository;

import com.roommatematch.model.entity.PartnerOffer;
import com.roommatematch.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PartnerOfferRepository extends JpaRepository<PartnerOffer, Long> {

    List<PartnerOffer> findByPartner(User partner);

    List<PartnerOffer> findByIsActiveTrue();

    List<PartnerOffer> findByIsActiveTrueAndValidUntilAfter(LocalDate date);

    List<PartnerOffer> findByPartnerAndIsActive(User partner, boolean isActive);
}
