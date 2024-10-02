package com.sporton.SportOn.repository;

import com.sporton.SportOn.entity.Role;
import com.sporton.SportOn.entity.Subscription;
import com.sporton.SportOn.entity.SubscriptionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findBySubscriptionTypeAndUserRole(SubscriptionType subscriptionType, Role role);

    @Query("SELECT s FROM Subscription s WHERE s.user.id = :userId AND s.endDate > CURRENT_DATE")
    Optional<Subscription> findActiveSubscriptionByUserId(@Param("userId") Long userId);
}
