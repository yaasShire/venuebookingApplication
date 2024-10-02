package com.sporton.SportOn.repository;

import com.sporton.SportOn.entity.AppUser;
import com.sporton.SportOn.entity.Role;
import com.sporton.SportOn.entity.Subscription;
import com.sporton.SportOn.entity.SubscriptionType;
import com.sporton.SportOn.model.CommonResponseModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByPhoneNumber(String phoneNumber);

    Optional<AppUser> findById(Long id);

    Optional<AppUser> findAppUserById(Long userId);

    Optional<AppUser> findByEmail(String email);

    @Query("SELECT COUNT(u) FROM AppUser u WHERE u.role = :role")
    long findTotalNumberOfProviders(@Param("role") Role role);

    @Query("SELECT COUNT(u) FROM AppUser u WHERE u.role = :role AND EXISTS (SELECT s FROM Subscription s WHERE s.user = u AND s.endDate > CURRENT_DATE)")
    long countByRoleAndSubscriptionStatus(@Param("role") Role role);

    @Query("SELECT COUNT(u) FROM AppUser u WHERE u.role = :role AND NOT EXISTS (SELECT s FROM Subscription s WHERE s.user = u AND s.endDate > CURRENT_DATE)")
    long countByRoleAndUnsubscriptionStatus(@Param("role") Role role);

    @Query("SELECT s FROM Subscription s WHERE s.user.id = :userId AND s.endDate > CURRENT_DATE")
    Optional<Subscription> findActiveSubscriptionByUserId(@Param("userId") Long userId);

    List<AppUser> findByRole(Role role);

    Optional<AppUser> findByIdAndRole(Long providerId, Role role);

    @Query("SELECT COUNT(u) FROM AppUser u WHERE u.role = :role")
    Long findTotalNumberOfCustomers(Role role);
}