package com.sporton.SportOn.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.sporton.SportOn.entity.token.Token;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "app_user")
public class AppUser implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String fullName;
    private String phoneNumber;
    private String email;
    private String password;
    private String joinedDate;
    private String gender;
    private String dateOfBirth;
    private String city;
    private MembershipType membershipType;
    @ManyToOne
    @JoinColumn(name = "regionId")
    @JsonBackReference
    private Region region;

    @OneToMany(mappedBy = "user")
    @JsonBackReference
    private List<Token> tokens;
    @Enumerated(EnumType.STRING)
    private Role role;
    private String profileImage;
    private Boolean approved=false;

//    @JsonBackReference
//    @OneToMany(mappedBy = "appUser", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    private List<Booking> bookings;

@JsonBackReference
    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    private List<Subscription> subscriptions;
//    @ManyToMany
//    @JoinTable(
//            name = "user_venue_favorite",
//            joinColumns = @JoinColumn(name = "user_id"),
//            inverseJoinColumns = @JoinColumn(name = "venue_id")
//    )
//    private Set<Venue> favoriteVenues = new HashSet<>();
    @Override
    @JsonBackReference
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return role.getAuthorities();
    }

    @JsonBackReference
    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.phoneNumber;
    }

    @JsonBackReference
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @JsonBackReference
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @JsonBackReference
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @JsonBackReference
    @Override
    public boolean isEnabled() {
        return true;
    }

    public boolean isSubscribed() {
        return subscriptions.stream().anyMatch(Subscription::isActive);
    }
}