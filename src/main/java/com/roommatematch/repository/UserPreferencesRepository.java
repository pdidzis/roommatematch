package com.roommatematch.repository;

import com.roommatematch.model.entity.User;
import com.roommatematch.model.entity.UserPreferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPreferencesRepository extends JpaRepository<UserPreferences, Long> {

    Optional<UserPreferences> findByUser(User user);

    Optional<UserPreferences> findByUserId(Long userId);
}
