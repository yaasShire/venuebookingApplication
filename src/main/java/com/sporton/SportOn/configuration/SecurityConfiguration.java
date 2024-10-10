package com.sporton.SportOn.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static com.sporton.SportOn.entity.Permission.*;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfiguration {

  private final JWTAuthenticationFilter jwtAuthFilter;
  private final AuthenticationProvider authenticationProvider;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf()
        .disable()
        .cors().and()
        .authorizeHttpRequests()
        .requestMatchers(
                "/api/v1/auth/**",
                "/demo/**",
                "/api/v1/authenticate/**",
                "/api/v1/authenticate/requestForgetPasswordOTP",
                "/api/v1/venue/get",
                "/api/v1/venue/getSingleVenue/**",
                "/api/v1/venue/get",
                "/api/v1/venue/nearByVenues",
                "/api/v1/venue/popularVenues",
                "/api/v1/venue/getSingleVenue/**",
                "/api/v1/court/getCourtsByVenueId/**",
                "/api/v1/venue/search",
                "/api/v1/venue/saveSearchedVenue",
                "/api/v1/venue/getSavedSearchVenues",
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/api/v1/ratings/venue/**",
                "/api/v1/cities",
                "/api/v1/cities/{regionId}",
                "/api/v1/region/getAll"
        )
          .permitAll()


        .requestMatchers(HttpMethod.POST, "/api/v1/region/create").hasAnyAuthority(ADMIN_CREATE.getPermission())
        .requestMatchers(HttpMethod.PUT, "/api/v1/region/update/**").hasAnyAuthority(ADMIN_UPDATE.getPermission())
        .requestMatchers(HttpMethod.DELETE, "/api/v1/region/delete/**").hasAnyAuthority(ADMIN_DELETE.getPermission())
//        .requestMatchers(HttpMethod.GET, "/api/v1/region/getAll").hasAnyAuthority(ADMIN_READ.getPermission(), PROVIDER_READ.getPermission())

//
        .requestMatchers(HttpMethod.GET, "/api/v1/customers/profiles").hasAnyAuthority(ADMIN_READ.getPermission(), PROVIDER_READ.getPermission())
        .requestMatchers(HttpMethod.GET, "/api/v1/customers/returning-customers-percentage").hasAnyAuthority(ADMIN_READ.getPermission(), PROVIDER_READ.getPermission())

//


        .requestMatchers(HttpMethod.POST, "/api/v1/venue/create").hasAnyAuthority(PROVIDER_CREATE.getPermission(), ADMIN_CREATE.getPermission())
        .requestMatchers(HttpMethod.PUT, "/api/v1/venue/update/**").hasAnyAuthority(PROVIDER_UPDATE.getPermission(), ADMIN_UPDATE.getPermission())
        .requestMatchers(HttpMethod.GET, "/api/v1/venue/isVenueFavoritedByUser/**").hasAnyAuthority(PROVIDER_READ.getPermission(), ADMIN_READ.getPermission(), USER_READ.getPermission())
        .requestMatchers(HttpMethod.GET, "/api/v1/venue/getNumberOfVenues").hasAnyAuthority(PROVIDER_READ.getPermission(), ADMIN_READ.getPermission())
        .requestMatchers(HttpMethod.GET, "/api/v1/venue/{venueId}/popular-time-slots").hasAnyAuthority(PROVIDER_READ.getPermission(), ADMIN_READ.getPermission())
        .requestMatchers(HttpMethod.GET, "/api/v1/{venueId}/revenue").hasAnyAuthority(PROVIDER_READ.getPermission(), ADMIN_READ.getPermission())
        .requestMatchers(HttpMethod.GET, "/api/v1/venue/{venueId}/occupancy-rate").hasAnyAuthority(PROVIDER_READ.getPermission(), ADMIN_READ.getPermission())
        .requestMatchers(HttpMethod.GET, "/api/v1/venue/{venueId}/bookings/total").hasAnyAuthority(PROVIDER_READ.getPermission(), ADMIN_READ.getPermission())
        .requestMatchers(HttpMethod.GET, "/api/v1/venue/highest-revenue").hasAnyAuthority(PROVIDER_READ.getPermission(), ADMIN_READ.getPermission())

//
        .requestMatchers(HttpMethod.POST, "/api/v1/cities/register").hasAnyAuthority(ADMIN_CREATE.getPermission())
        .requestMatchers(HttpMethod.PUT, "/api/v1/cities/{cityId}").hasAnyAuthority(ADMIN_UPDATE.getPermission())
        .requestMatchers(HttpMethod.DELETE, "/api/v1/cities/{cityId}").hasAnyAuthority(ADMIN_DELETE.getPermission())
//        .requestMatchers(HttpMethod.GET, "/api/v1/cities").hasAnyAuthority(ADMIN_READ.getPermission(), PROVIDER_READ.getPermission(), USER_READ.getPermission())


//            .requestMatchers(HttpMethod.GET, "/api/v1/venue/search").hasAnyAuthority(PROVIDER_READ.getPermission(), ADMIN_READ.getPermission(), USER_READ.getPermission())
        .requestMatchers(HttpMethod.DELETE, "/api/v1/venue/delete/**").hasAnyAuthority(PROVIDER_DELETE.getPermission(), ADMIN_DELETE.getPermission())
        .requestMatchers(HttpMethod.POST, "/api/v1/court/create").hasAnyAuthority(PROVIDER_CREATE.getPermission(), ADMIN_CREATE.getPermission())
        .requestMatchers(HttpMethod.PUT, "/api/v1/court/update/**").hasAnyAuthority(PROVIDER_UPDATE.getPermission(), ADMIN_UPDATE.getPermission())
        .requestMatchers(HttpMethod.DELETE, "/api/v1/court/delete/**").hasAnyAuthority(PROVIDER_DELETE.getPermission(), ADMIN_DELETE.getPermission())
        .requestMatchers(HttpMethod.POST, "/api/v1/timeslot/create").hasAnyAuthority(PROVIDER_CREATE.getPermission(), ADMIN_CREATE.getPermission())
        .requestMatchers(HttpMethod.PUT, "/api/v1/timeslot/update/**").hasAnyAuthority(PROVIDER_UPDATE.getPermission(), ADMIN_UPDATE.getPermission())
        .requestMatchers(HttpMethod.DELETE, "/api/v1/timeslot/delete/**").hasAnyAuthority(PROVIDER_DELETE.getPermission(), ADMIN_DELETE.getPermission())
        .requestMatchers(HttpMethod.GET, "/api/v1/timeslot/getTimeSlotByCourtId/**").hasAnyAuthority(PROVIDER_READ.getPermission(), ADMIN_DELETE.getPermission(), USER_READ.getPermission())
//        .requestMatchers(HttpMethod.GET, "/api/v1/booking/get").hasAnyAuthority(PROVIDER_READ.getPermission(), ADMIN_READ.getPermission())
        .requestMatchers(HttpMethod.PUT, "/api/v1/booking/update/**").hasAnyAuthority(PROVIDER_UPDATE.getPermission(), ADMIN_UPDATE.getPermission())
        .requestMatchers(HttpMethod.PUT, "/api/v1/booking/accept/**").hasAnyAuthority(PROVIDER_UPDATE.getPermission(), ADMIN_UPDATE.getPermission())
        .requestMatchers(HttpMethod.GET, "/api/v1/booking/getBookingByProviderId").hasAnyAuthority(PROVIDER_READ.getPermission(), ADMIN_READ.getPermission())
        .requestMatchers(HttpMethod.GET, "/api/v1/booking/getTop10NewOrders").hasAnyAuthority(PROVIDER_READ.getPermission(), ADMIN_READ.getPermission())
        .requestMatchers(HttpMethod.GET, "/api/v1/booking/getPendingOrders").hasAnyAuthority(PROVIDER_READ.getPermission(), ADMIN_READ.getPermission())
        .requestMatchers(HttpMethod.GET, "/api/v1/booking/getCompletedOrders").hasAnyAuthority(PROVIDER_READ.getPermission(), ADMIN_READ.getPermission())
        .requestMatchers(HttpMethod.GET, "/api/v1/booking/getExpiredOrders").hasAnyAuthority(PROVIDER_READ.getPermission(), ADMIN_READ.getPermission())
        .requestMatchers(HttpMethod.GET, "/api/v1/booking/totalBookingIncomeForProvider").hasAnyAuthority(PROVIDER_READ.getPermission(), ADMIN_READ.getPermission())
        .requestMatchers(HttpMethod.GET, "/api/v1/booking/getTotalVenueIncome/**").hasAnyAuthority(PROVIDER_READ.getPermission(), ADMIN_READ.getPermission())
        .requestMatchers(HttpMethod.GET, "/api/v1/booking/getTotalIncomeOfTodayOrders/**").hasAnyAuthority(PROVIDER_READ.getPermission(), ADMIN_READ.getPermission())
        .requestMatchers(HttpMethod.GET, "/api/v1/booking/{venueId}/bookings/total").hasAnyAuthority(PROVIDER_READ.getPermission(), ADMIN_READ.getPermission())
        .requestMatchers(HttpMethod.GET, "/api/v1/booking/calculate").hasAnyAuthority(PROVIDER_READ.getPermission(), ADMIN_READ.getPermission())
        .requestMatchers(HttpMethod.GET, "/api/v1/booking/recurring").hasAnyAuthority(USER_READ.getPermission(), ADMIN_READ.getPermission())
        .requestMatchers(HttpMethod.GET, "/api/v1/booking/one-time").hasAnyAuthority(USER_READ.getPermission(), ADMIN_READ.getPermission())



            .requestMatchers(HttpMethod.GET, "/api/v1/booking/getConfirmedOrders").hasAnyAuthority(PROVIDER_READ.getPermission(), ADMIN_READ.getPermission())
        .requestMatchers(HttpMethod.GET, "/api/v1/booking/getCancelledOrders").hasAnyAuthority(PROVIDER_READ.getPermission(), ADMIN_READ.getPermission())
        .requestMatchers(HttpMethod.GET, "/api/v1/booking/getNumberOfTodayOrders").hasAnyAuthority(PROVIDER_READ.getPermission(), ADMIN_READ.getPermission())
        .requestMatchers(HttpMethod.GET, "/api/v1/booking/getNumberOfTodayMatches").hasAnyAuthority(PROVIDER_READ.getPermission(), ADMIN_READ.getPermission())
        .requestMatchers(HttpMethod.GET, "/api/v1/booking/getNumberOfPendingOrders").hasAnyAuthority(PROVIDER_READ.getPermission(), ADMIN_READ.getPermission())
        .requestMatchers(HttpMethod.GET, "/api/v1/booking/getMatchesByDate").hasAnyAuthority(PROVIDER_READ.getPermission(), ADMIN_READ.getPermission())
        .requestMatchers(HttpMethod.GET, "/api/v1/booking/income").hasAnyAuthority(ADMIN_READ.getPermission())
        .requestMatchers(HttpMethod.GET, "/api/v1/booking/getMatchesByDate").hasAnyAuthority(PROVIDER_READ.getPermission(), ADMIN_READ.getPermission())
        .requestMatchers(HttpMethod.GET, "/api/v1/booking/getLast12MonthsIncome").hasAnyAuthority(ADMIN_READ.getPermission())

        .requestMatchers(HttpMethod.GET, "/api/v1/booking/getBookingByCustomerId").hasAnyAuthority(PROVIDER_READ.getPermission(), ADMIN_READ.getPermission(), USER_READ.getPermission())
        .requestMatchers(HttpMethod.POST, "/api/v1/facility/create").hasAnyAuthority(PROVIDER_CREATE.getPermission(), ADMIN_CREATE.getPermission())
        .requestMatchers(HttpMethod.PUT, "/api/v1/facility/update").hasAnyAuthority(PROVIDER_UPDATE.getPermission(), ADMIN_UPDATE.getPermission())
        .requestMatchers(HttpMethod.DELETE, "/api/v1/facility/delete").hasAnyAuthority(PROVIDER_DELETE.getPermission(), ADMIN_DELETE.getPermission())

        .requestMatchers(HttpMethod.PUT, "/api/v1/profile/addOrRemoveFavoriteVenueToUser/**").hasAnyAuthority(PROVIDER_UPDATE.getPermission(), ADMIN_UPDATE.getPermission(), USER_UPDATE.getPermission())

        .requestMatchers(HttpMethod.PUT, "/api/v1/authenticate/uploadProfileImage").hasAnyAuthority(PROVIDER_UPDATE.getPermission(), ADMIN_UPDATE.getPermission(), USER_UPDATE.getPermission())

        .requestMatchers(HttpMethod.GET, "/api/v1/authenticate/isUserSubscribed").hasAnyAuthority(PROVIDER_READ.getPermission(), ADMIN_READ.getPermission())
        .requestMatchers(HttpMethod.POST, "/api/v1/authenticate/providerSubscribe").hasAnyAuthority(PROVIDER_CREATE.getPermission(), ADMIN_CREATE.getPermission())
        .requestMatchers(HttpMethod.GET, "/api/v1/authenticate/getTotalNumberOfSubscribedProviders").hasAnyAuthority(ADMIN_READ.getPermission())

        .requestMatchers(HttpMethod.GET, "/api/v1/authenticate/getTotalNumberOfUnSubscribedProviders").hasAnyAuthority(ADMIN_READ.getPermission())
        .requestMatchers(HttpMethod.GET, "/api/v1/authenticate/getTotalNumberOfProviders").hasAnyAuthority(ADMIN_READ.getPermission())
        .requestMatchers(HttpMethod.PUT, "/api/v1/authenticate/updateProviderSubscription").hasAnyAuthority(PROVIDER_UPDATE.getPermission(), ADMIN_UPDATE.getPermission())
        .requestMatchers(HttpMethod.GET, "/api/v1/authenticate/getTotalProvidersSubscriptionIncomeByMonthlyOrYearly").hasAnyAuthority(ADMIN_READ.getPermission())
        .requestMatchers(HttpMethod.PUT, "/api/v1/authenticate/approveProvider/**").hasAnyAuthority(ADMIN_UPDATE.getPermission())
        .requestMatchers(HttpMethod.GET, "/api/v1/authenticate/getAllProviders").hasAnyAuthority(ADMIN_READ.getPermission())
        .requestMatchers(HttpMethod.PUT, "/api/v1/authenticate/editProviderBySuperAdmin/**").hasAnyAuthority(ADMIN_UPDATE.getPermission())
        .requestMatchers(HttpMethod.GET, "/api/v1/authenticate/getTotalNumberOfCustomers").hasAnyAuthority(ADMIN_READ.getPermission())
        .requestMatchers(HttpMethod.GET, "/api/v1/authenticate/getAllProvidersSubscriptions").hasAnyAuthority(ADMIN_READ.getPermission())
        .requestMatchers(HttpMethod.GET, "/api/v1/authenticate/getAllCustomers").hasAnyAuthority(ADMIN_READ.getPermission())
        .requestMatchers(HttpMethod.GET, "/api/v1/authenticate/getProviderById").hasAnyAuthority(ADMIN_READ.getPermission(), PROVIDER_READ.getPermission())


        .anyRequest()
        .authenticated()
        .and()
        .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
        .authenticationProvider(authenticationProvider)
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
    ;
    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.addAllowedOrigin("http://localhost:3000"); // Replace with your Next.js app domain
    configuration.addAllowedMethod("*");
    configuration.addAllowedHeader("*");
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

}
