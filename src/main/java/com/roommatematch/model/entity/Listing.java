package com.roommatematch.model.entity;

import com.roommatematch.model.enums.ListingStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "listings")
public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "landlord_id")
    private User landlord;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String address;

    private String city;

    private String country;

    private BigDecimal monthlyRent;

    private LocalDate availableFrom;

    private Integer totalRooms;

    private Integer availableRooms;

    private boolean petsAllowed;

    private boolean smokingAllowed;

    private String photoUrls;

    @Enumerated(EnumType.STRING)
    private ListingStatus status;

    @Builder.Default
    private boolean isVerified = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
