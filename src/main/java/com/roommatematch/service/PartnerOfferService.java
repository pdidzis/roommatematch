package com.roommatematch.service;

import com.roommatematch.dto.request.PartnerOfferRequest;
import com.roommatematch.dto.response.PartnerOfferResponse;
import com.roommatematch.exception.ResourceNotFoundException;
import com.roommatematch.exception.UnauthorizedException;
import com.roommatematch.model.entity.Match;
import com.roommatematch.model.entity.PartnerOffer;
import com.roommatematch.model.entity.User;
import com.roommatematch.model.enums.MatchStatus;
import com.roommatematch.model.enums.UserRole;
import com.roommatematch.repository.MatchRepository;
import com.roommatematch.repository.PartnerOfferRepository;
import com.roommatematch.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PartnerOfferService {

    private final PartnerOfferRepository partnerOfferRepository;
    private final UserRepository userRepository;
    private final MatchRepository matchRepository;

    @Transactional
    public PartnerOfferResponse createOffer(Long partnerId, PartnerOfferRequest request) {
        User partner = userRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found"));

        if (partner.getRole() != UserRole.PARTNER) {
            throw new UnauthorizedException("Only partners can create offers");
        }

        PartnerOffer offer = PartnerOffer.builder()
                .partner(partner)
                .title(request.getTitle())
                .description(request.getDescription())
                .discountPercent(request.getDiscountPercent())
                .validUntil(request.getValidUntil())
                .isActive(true)
                .build();

        offer = partnerOfferRepository.save(offer);
        log.info("Partner offer created: {} by partner: {}", offer.getId(), partnerId);

        return mapToResponse(offer);
    }

    public List<PartnerOfferResponse> getActiveOffers() {
        List<PartnerOffer> offers = partnerOfferRepository
                .findByIsActiveTrueAndValidUntilAfter(LocalDate.now());

        return offers.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<PartnerOfferResponse> getOffersForMatch(Long matchId, Long userId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found"));

        boolean isParticipant = match.getRequester().getId().equals(userId) ||
                match.getReceiver().getId().equals(userId);

        if (!isParticipant) {
            throw new UnauthorizedException("You are not part of this match");
        }

        if (match.getStatus() != MatchStatus.ACCEPTED &&
                match.getStatus() != MatchStatus.ROOMMATE_CONFIRMED) {
            throw new UnauthorizedException("Match must be accepted to view offers");
        }

        return getActiveOffers();
    }

    @Transactional
    public void deactivateOffer(Long partnerId, Long offerId) {
        PartnerOffer offer = partnerOfferRepository.findById(offerId)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found"));

        if (!offer.getPartner().getId().equals(partnerId)) {
            throw new UnauthorizedException("You can only deactivate your own offers");
        }

        offer.setActive(false);
        partnerOfferRepository.save(offer);
        log.info("Partner offer deactivated: {} by partner: {}", offerId, partnerId);
    }

    public List<PartnerOfferResponse> getMyOffers(Long partnerId) {
        User partner = userRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found"));

        List<PartnerOffer> offers = partnerOfferRepository.findByPartner(partner);

        return offers.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private PartnerOfferResponse mapToResponse(PartnerOffer offer) {
        return PartnerOfferResponse.builder()
                .id(offer.getId())
                .title(offer.getTitle())
                .description(offer.getDescription())
                .discountPercent(offer.getDiscountPercent())
                .validUntil(offer.getValidUntil())
                .isActive(offer.isActive())
                .partnerName(offer.getPartner().getFirstName() + " " +
                        offer.getPartner().getLastName())
                .build();
    }
}
