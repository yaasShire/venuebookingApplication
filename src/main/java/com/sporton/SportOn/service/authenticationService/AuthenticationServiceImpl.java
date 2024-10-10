package com.sporton.SportOn.service.authenticationService;


import com.google.gson.Gson;

import com.sporton.SportOn.SportOnApplication;
import com.sporton.SportOn.configuration.JWTService;
import com.sporton.SportOn.dto.UserSubscriptionDTO;
import com.sporton.SportOn.entity.*;
import com.sporton.SportOn.entity.token.Token;
import com.sporton.SportOn.entity.token.TokenType;
import com.sporton.SportOn.exception.authenticationException.AuthenticationException;
import com.sporton.SportOn.model.CommonResponseModel;
import com.sporton.SportOn.model.authenticationModel.*;
import com.sporton.SportOn.repository.AppUserRepository;
import com.sporton.SportOn.repository.RegionRepository;
import com.sporton.SportOn.repository.SubscriptionRepository;
import com.sporton.SportOn.repository.TokenRepository;
import com.sporton.SportOn.service.aswS3Service.AWSS3Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@EnableScheduling
public class AuthenticationServiceImpl implements AuthenticateService {
    private final PasswordEncoder passwordEncoder;
    private final AppUserRepository appUserRepository;
    private final JWTService jwtService;
    private final TokenRepository tokenRepository;
    private final AuthenticationManager authenticationManager;
    private final AWSS3Service awss3Service;
    private final SubscriptionRepository subscriptionRepository;
    private final RegionRepository regionRepository;
    private final JavaMailSender javaEmailSender;

    @Autowired
    private JavaMailSender emailSender;
    LocalDateTime generatedTime=LocalDateTime.now();
    private AppUser user;
    private AppUser providerUser;
    private String providerPassword;

    GeneratedOTP generatedOTP = new GeneratedOTP();
    GeneratedOTP generatedOTPForProvider = new GeneratedOTP();

    private String currentOTP;
    private static final int OTP_LENGTH = 6;
    private static final int EXPIRATION_TIME_IN_MINUTES = 3;
    @Override
    public OTPResponseModel signUpUser(SignUpRequestModel body) throws AuthenticationException {

        if(body.getFullName() == null){
            throw new AuthenticationException("Full name is required");
        }
        if(body.getPhoneNumber() == null){
            throw new AuthenticationException("Phone number is required");
        }
        if(body.getPassword() == null){
            throw new AuthenticationException("Password is required");
        }
        Optional<AppUser> checkIfUserExistsByPhoneNumber = appUserRepository.findByPhoneNumber(body.getPhoneNumber());
        Optional<AppUser> checkIfUserExistsByEmail = appUserRepository.findByEmail(body.getEmail());

        if(checkIfUserExistsByPhoneNumber.isPresent()){
            throw new AuthenticationException("This User with number '"+ body.getPhoneNumber()+ "' already exists");
        }
        if(checkIfUserExistsByEmail.isPresent()){
            throw new AuthenticationException("This User with email '"+ body.getEmail()+ "' already exists");
        }

        try {
            if(SportOnApplication.getAccessToken() == null || SportOnApplication.getAccessToken().equalsIgnoreCase("")){
                System.out.println(SportOnApplication.getAccessToken());
                throw new AuthenticationException("Access token is not provide for SMS");
            }else{
                log.info("user body {}", body);
//            sendOTP(body.getPhoneNumber());
                sendEmailVerification(body.getEmail());
                user = AppUser.builder()
                        .fullName(body.getFullName())
                        .email(body.getEmail())
                        .phoneNumber(body.getPhoneNumber())
                        .password(passwordEncoder.encode(body.getPassword()))
                        .joinedDate(getCurrentDate())
                        .role(body.getRole())
                        .approved(false)
                        .build();
                OTPResponseModel response = OTPResponseModel.builder()
                        .message("OTP is sent, verify")
                        .build();
                return response;
            }

        }catch (Exception e){
            System.out.println(e.getMessage());
            throw new AuthenticationException(e.getMessage());
        }


    }

    @Override
    public SignUpResponseModel verifyOTP(OTP otp) throws AuthenticationException {
        if (otp == null){
            throw new AuthenticationException("Enter the otp sent to your phone number.");
        }
        if (isOtpValid(otp.getOtp(), generatedTime)) {
            if(otp.getOtp().equalsIgnoreCase(generatedOTP.getGeneratedOTP())){
                appUserRepository.save(user);
                String token = jwtService.generateToken(user);
                String refreshToken = jwtService.generateRefreshToken(user);
                saveUserToken(user, token);
                SignUpResponseModel signUpResponseModel = SignUpResponseModel.builder()
                        .accessToken(token)
                        .refreshToken(refreshToken)
                        .status(HttpStatus.CREATED)
                        .message("User is created successfully")
                        .build();
                return signUpResponseModel;
            }
            else {
                throw new AuthenticationException("OTP is invalid");
            }
        } else {
            System.out.println("OTP is invalid");
            throw new AuthenticationException("OTP is invalid");
        }

    }

    @Override
    public SignInResponseModel signIn(SignInRequestModel body) throws AuthenticationException {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(body.getPhoneNumber(), body.getPassword()));
            // User is authenticated, proceed with the request
        } catch (Exception e) {
            // Authentication failed, handle the exception
            throw new AuthenticationException("Invalid credentials");
        }

        Optional<AppUser> appUser = appUserRepository.findByPhoneNumber(body.getPhoneNumber());
        if (appUser.isPresent()){
        var jwtToken = jwtService.generateToken(appUser.get());
        var refreshToken = jwtService.generateRefreshToken(appUser.get());
        revokeAllUserTokens(appUser.get());
        saveUserToken(appUser.get(), jwtToken);
        return SignInResponseModel.builder()
                .status(HttpStatus.OK)
                .message("User successfully loged in")
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .id(appUser.get().getId())
                .phoneNumber(appUser.get().getPhoneNumber())
                .joinedDate(appUser.get().getJoinedDate())
                .build();
        }else {
        throw new AuthenticationException("User not found");
        }
//        log.info("app user = {}", appUser.get());
    }

    @Override
    public OTPResponseModel sendForgetPasswordOTP(RequestForgetPasswordOTP body) throws AuthenticationException {
        if (body.getPhoneNumber() ==null || body.getPhoneNumber().equalsIgnoreCase("")){
            throw new AuthenticationException("Enter your phone number");
        }
        try {
            Optional<AppUser> appUser = appUserRepository.findByPhoneNumber(body.getPhoneNumber());
            if (appUser.isPresent()){
                log.info("user {} email {}", appUser.get().getPhoneNumber(), appUser.get().getEmail());
//                sendOTP(body.getPhoneNumber());
                sendEmailVerification(appUser.get().getEmail());
                generatedTime=LocalDateTime.now();
                return OTPResponseModel
                        .builder()
                        .message("OTP is sent successfully please verify")
                        .build();
            }else{
                throw new AuthenticationException("User does not exist");
            }
        }catch (Exception e){
            log.info(e.getMessage());
            throw new AuthenticationException(e.getMessage());
        }
    }



    @Override
    public OTPResponseModel verifyForgetPasswordOTP(OTP otp) throws AuthenticationException {
        if (otp.getOtp() == null || otp.getOtp().equalsIgnoreCase("")) {
            throw new AuthenticationException("Enter the otp sent to your phone number.");
        }
        if (isOtpValid(otp.getOtp(), generatedTime)){
            System.out.println("OTP is valid");
            if (otp.getOtp().equalsIgnoreCase(generatedOTP.getGeneratedOTP())) {
                OTPResponseModel response = OTPResponseModel
                        .builder()
                        .message("OTP verified successfully")
                        .build();
                return response;
            }
            else {
                throw new AuthenticationException("Invalid OTP");
            }
        } else {
            throw new AuthenticationException("OTP is invalid");
        }


    }

    @Override
    public AppUser getProfileData(String phoneNumber) throws AuthenticationException {
        try {
            Optional<AppUser> appUser = appUserRepository.findByPhoneNumber(phoneNumber);
            if (appUser.isPresent()){
                return appUser.get();
            }else {
                throw new AuthenticationException("No User Found");
            }
        }catch (Exception e){
            throw new AuthenticationException(e.getMessage());
        }
    }

    @Override
    public CommonResponseModel updateProfileData(UpdateProfileData body, String phoneNumber) throws AuthenticationException {
        try {
            Optional<AppUser> appUser = appUserRepository.findByPhoneNumber(phoneNumber);
            if (appUser.isPresent()){
                if (body.getFullName() !=null){
                    appUser.get().setFullName(body.getFullName());
                }
                if (body.getEmail() !=null){
                    appUser.get().setEmail(body.getEmail());
                }
                appUserRepository.save(appUser.get());
                return CommonResponseModel.builder()
                        .status(HttpStatus.OK)
                        .message("User Data Updated Successfully")
                        .build();
            }else {
                throw new AuthenticationException("No User Found");
            }
        }catch (Exception e){
            throw new AuthenticationException(e.getMessage());
        }
    }

    @Override
    public CommonResponseModel changePassword(ChangePasswordAuthrorizedRequest body, String phoneNumber) throws AuthenticationException {
        try {
            Optional<AppUser> appUser = appUserRepository.findByPhoneNumber(phoneNumber);
            if (appUser.isPresent()){
                log.info("old password {}",passwordEncoder.encode(body.getOldPassword()));
                log.info("current password {}",appUser.get().getPassword());

                if (passwordEncoder.matches(body.getOldPassword(), appUser.get().getPassword())){
                    if (body.getNewPassword() !=null){
                        appUser.get().setPassword(passwordEncoder.encode(body.getNewPassword()));
                    }
                    appUserRepository.save(appUser.get());
                    return CommonResponseModel.builder()
                            .status(HttpStatus.OK)
                            .message("User Password Updated Successfully")
                            .build();
                }else {
                    throw new AuthenticationException("Old Password Is Not Correct");
                }
            }else {
                throw new AuthenticationException("No User Found");
            }
        }catch (Exception e){
            throw new AuthenticationException(e.getMessage());
        }
    }

    @Override
    public SignInResponseModel singInAsCustomer(SignInRequestModel body) throws AuthenticationException {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(body.getPhoneNumber(), body.getPassword()));
            // User is authenticated, proceed with the request
        } catch (Exception e) {
            // Authentication failed, handle the exception
            throw new AuthenticationException("Invalid credentials");
        }

        Optional<AppUser> appUser = appUserRepository.findByPhoneNumber(body.getPhoneNumber());
        if (appUser.isPresent()){
            if (appUser.get().getRole() != Role.USER) throw new AuthenticationException("Invalid User");
            var jwtToken = jwtService.generateToken(appUser.get());
            var refreshToken = jwtService.generateRefreshToken(appUser.get());
            revokeAllUserTokens(appUser.get());
            saveUserToken(appUser.get(), jwtToken);
            return SignInResponseModel.builder()
                    .status(HttpStatus.OK)
                    .message("User successfully loged in")
                    .accessToken(jwtToken)
                    .refreshToken(refreshToken)
                    .id(appUser.get().getId())
                    .phoneNumber(appUser.get().getPhoneNumber())
                    .joinedDate(appUser.get().getJoinedDate())
                    .build();
        }else {
            throw new AuthenticationException("User not found");
        }
    }

    @Override
    public SignInResponseModel singInAsProvider(SignInRequestModel body) throws AuthenticationException {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(body.getPhoneNumber(), body.getPassword()));
            // User is authenticated, proceed with the request
            Optional<AppUser> appUser = appUserRepository.findByPhoneNumber(body.getPhoneNumber());
            if (appUser.isPresent()){
                if (appUser.get().getRole() != Role.PROVIDER) throw new AuthenticationException("Invalid User");
                var jwtToken = jwtService.generateToken(appUser.get());
                var refreshToken = jwtService.generateRefreshToken(appUser.get());
                revokeAllUserTokens(appUser.get());
                saveUserToken(appUser.get(), jwtToken);
                return SignInResponseModel.builder()
                        .status(HttpStatus.OK)
                        .message("User successfully loged in")
                        .accessToken(jwtToken)
                        .refreshToken(refreshToken)
                        .id(appUser.get().getId())
                        .phoneNumber(appUser.get().getPhoneNumber())
                        .joinedDate(appUser.get().getJoinedDate())
                        .build();
            }else {
                throw new AuthenticationException("User not found");
            }
        } catch (Exception e) {
            // Authentication failed, handle the exception
            throw new AuthenticationException("Invalid credentials");
        }

    }

    @Override
    public OTPResponseModel sendUserForgetPasswordOTP(RequestForgetPasswordOTP body) throws AuthenticationException {
        if (body.getPhoneNumber() ==null || body.getPhoneNumber().equalsIgnoreCase("")){
            throw new AuthenticationException("Enter your phone number");
        }
        try {
            Optional<AppUser> appUser = appUserRepository.findByPhoneNumber(body.getPhoneNumber());
            if (appUser.isPresent()){
                if (appUser.get().getRole() !=Role.USER) throw new AuthenticationException("User IS NOT VALID", HttpStatus.BAD_REQUEST);
                log.info("user {} email {}", appUser.get().getPhoneNumber(), appUser.get().getEmail());
//                sendOTP(body.getPhoneNumber());
                sendEmailVerification(appUser.get().getEmail());
                generatedTime=LocalDateTime.now();
                return OTPResponseModel
                        .builder()
                        .message("OTP is sent successfully please verify")
                        .build();
            }else{
                throw new AuthenticationException("User does not exist");
            }
        }catch (Exception e){
            log.info(e.getMessage());
            throw new AuthenticationException(e.getMessage());
        }
    }

    @Override
    public CommonResponseModel uploadProfileImage(MultipartFile file, String phoneNumber) throws AuthenticationException {
        try {
            Optional<AppUser> appUser =  appUserRepository.findByPhoneNumber(phoneNumber);
            if (!appUser.isPresent()) throw new AuthenticationException("No User Found");
            appUser.get().setProfileImage(awss3Service.uploadImage(file));
            appUserRepository.save(appUser.get());
            return CommonResponseModel.builder()
                    .status(HttpStatus.OK)
                    .message("User Profile Image Updated Successfully")
                    .build();

        }catch (Exception e){
            throw new AuthenticationException(e.getMessage());
        }
    }

    @Override
    public CommonResponseModel getTotalNumberOfProviders(String phoneNumber) throws AuthenticationException {
        try {
            Optional<AppUser> appUser =  appUserRepository.findByPhoneNumber(phoneNumber);
            if (appUser.isPresent() && appUser.get().getRole()==Role.ADMIN){
                Long totalNumberOfProviders = appUserRepository.findTotalNumberOfProviders(Role.PROVIDER);
                return CommonResponseModel.builder()
                        .status(HttpStatus.OK)
                        .message("Total Number Of Provider Retrieved Successfully")
                        .data(totalNumberOfProviders)
                        .build();
            }else {
                throw new AuthenticationException("Invalid User");
            }
        }catch (Exception e){
            throw new AuthenticationException(e.getMessage());
        }
    }

    @Override
    public CommonResponseModel getTotalNumberOfSubscribedProviders(String phoneNumber) throws AuthenticationException {
        try {
            AppUser user = appUserRepository.findByPhoneNumber(phoneNumber)
                    .orElseThrow(() -> new AuthenticationException("User not found"));

            if (user.getRole() != Role.ADMIN) {
                throw new AuthenticationException("User is not a provider");
            }
            long count = appUserRepository.countByRoleAndSubscriptionStatus(Role.PROVIDER);
            return CommonResponseModel.builder()
                    .status(HttpStatus.OK)
                    .message("Total Number Of Subscribed Providers Retrieved Successfully")
                    .data(count)
                    .build();
        }catch (Exception e){
            throw new AuthenticationException(e.getMessage());
        }
    }

    @Override
    public CommonResponseModel isUserSubscribed(String phoneNumber) throws AuthenticationException {
        try {
            Optional<AppUser> appUser =  appUserRepository.findByPhoneNumber(phoneNumber);
            if (appUser.isPresent() && (appUser.get().getRole() == Role.PROVIDER || appUser.get().getRole() == Role.ADMIN)) {
                if (appUser.get().isSubscribed() && !appUser.get().getApproved()) {
                    return CommonResponseModel.builder()
                            .status(HttpStatus.OK)
                            .message("User is Subscribed But Not Approved")
                            .data(appUser.get().isSubscribed())
                            .build();
                } else if (appUser.get().isSubscribed() && appUser.get().getApproved()) {
                    return CommonResponseModel.builder()
                            .status(HttpStatus.OK)
                            .message("User is Subscribed And Approved")
                            .data(appUser.get().isSubscribed())
                            .build();
                } else if (appUser.get().isSubscribed()) {
                    return CommonResponseModel.builder()
                            .status(HttpStatus.OK)
                            .message("User is Subscribed And Approved")
                            .data(appUser.get().isSubscribed())
                            .build();
                }
                return CommonResponseModel.builder()
                        .status(HttpStatus.OK)
                        .message("User subscription result retrieved successfully")
                        .data(appUser.get().isSubscribed())
                        .build();
            }else {
                throw new AuthenticationException("Invalid User");
            }

        }catch (Exception e){
            throw new AuthenticationException(e.getMessage());
        }
    }

    @Override
    public Subscription createSubscription(String phoneNumber, SubscriptionModal body) throws AuthenticationException {
        final double defaultMonthlyPrice = 40.0;
        final double defaultYearlyPrice = 300.0;
        try {
            AppUser user = appUserRepository.findByPhoneNumber(phoneNumber)
                    .orElseThrow(() -> new AuthenticationException("User not found"));

            if (user.getRole() != Role.PROVIDER) {
                throw new AuthenticationException("User is not a provider");
            }
//
//            if (user.getPhoneNumber() != phoneNumber) {
//                throw new AuthenticationException("User is not a provider");
//            }

            Optional<Subscription> existingSubscription = subscriptionRepository.findActiveSubscriptionByUserId(user.getId());
            if (existingSubscription.isPresent()) {
                throw new AuthenticationException("User already has an active subscription");
            }

            LocalDate startDate = LocalDate.now();
            LocalDate endDate = body.getSubscriptionType() == SubscriptionType.MONTHLY ? startDate.plusMonths(1) : startDate.plusYears(1);
            double subscriptionPrice = body.getPrice() != null ? body.getPrice() : (body.getSubscriptionType() == SubscriptionType.MONTHLY ? defaultMonthlyPrice : defaultYearlyPrice);

            Subscription subscription = Subscription.builder()
                    .user(user)
                    .subscriptionType(body.getSubscriptionType())
                    .startDate(startDate)
                    .endDate(endDate)
                    .price(subscriptionPrice)
                    .build();
            return subscriptionRepository.save(subscription);
        } catch (Exception e) {
            throw new AuthenticationException(e.getMessage());
        }
    }

    @Override
    public CommonResponseModel getTotalNumberOfUnSubscribedProviders(String phoneNumber) throws AuthenticationException {
        try {
            AppUser user = appUserRepository.findByPhoneNumber(phoneNumber)
                    .orElseThrow(() -> new AuthenticationException("User not found"));

            if (user.getRole() != Role.ADMIN) {
                throw new AuthenticationException("User is not a provider");
            }

            long count = appUserRepository.countByRoleAndUnsubscriptionStatus(Role.PROVIDER);
            return CommonResponseModel.builder()
                    .status(HttpStatus.OK)
                    .message("Total Number Of UnSubscribed Providers Retrieved Successfully")
                    .data(count)
                    .build();
        }catch (Exception e){
            throw new AuthenticationException(e.getMessage());
        }
    }

    @Override
    public Subscription updateProviderSubscription(String phoneNumber, SubscriptionModal body) throws AuthenticationException {
        final double defaultMonthlyPrice = 40.0;
        final double defaultYearlyPrice = 300.0;

        try {
            AppUser user = appUserRepository.findByPhoneNumber(phoneNumber)
                    .orElseThrow(() -> new AuthenticationException("User not found"));

            if (user.getRole() != Role.PROVIDER) {
                throw new AuthenticationException("User is not a provider");
            }

            Optional<Subscription> existingSubscription = subscriptionRepository.findActiveSubscriptionByUserId(user.getId());
            if (existingSubscription.isEmpty()) {
                throw new AuthenticationException("User does not have an active subscription");
            }

            Subscription subscription = existingSubscription.get();
            subscription.setSubscriptionType(body.getSubscriptionType());
            LocalDate endDate = body.getSubscriptionType() == SubscriptionType.MONTHLY ? LocalDate.now().plusMonths(1) : LocalDate.now().plusYears(1);
            subscription.setEndDate(endDate);

            double subscriptionPrice = body.getPrice() != null ? body.getPrice() : (body.getSubscriptionType() == SubscriptionType.MONTHLY ? defaultMonthlyPrice : defaultYearlyPrice);
            subscription.setPrice(subscriptionPrice);

            return subscriptionRepository.save(subscription);
        } catch (Exception e) {
            throw new AuthenticationException(e.getMessage());
        }
    }

    @Override
    public CommonResponseModel getTotalProvidersSubscriptionIncomeByMonthlyOrYearly(String phoneNumber, SubscriptionType subscriptionType) throws AuthenticationException {
        try {
            AppUser user = appUserRepository.findByPhoneNumber(phoneNumber)
                    .orElseThrow(() -> new AuthenticationException("User not found"));

            // Assuming only an admin can access this information
            if (!user.getRole().equals(Role.ADMIN)) {
                throw new AuthenticationException("User does not have the required role to access this information");
            }

            List<Subscription> subscriptions = subscriptionRepository.findBySubscriptionTypeAndUserRole(subscriptionType, Role.PROVIDER);
            double totalIncome = subscriptions.stream()
                    .mapToDouble(Subscription::getPrice)
                    .sum();

            return CommonResponseModel.builder()
                    .status(HttpStatus.OK)
                    .message("Total Provider Subscription Income Retrieved Successfully")
                    .data(totalIncome)
                    .build();
        } catch (Exception e) {
            throw new AuthenticationException(e.getMessage());
        }
    }

    @Override
    public CommonResponseModel approveProvider(String phoneNumber, Long providerId) throws AuthenticationException {
        try {
            Optional<AppUser> appUser =  appUserRepository.findAppUserById(providerId);
            if (!appUser.isPresent()) throw new AuthenticationException("No User Found");
            if (appUser.get().getApproved() !=null && appUser.get().getApproved()){
                appUser.get().setApproved(false);
                appUserRepository.save(appUser.get());
                return CommonResponseModel.builder()
                        .status(HttpStatus.OK)
                        .message("Provider Is Un Approved Successfully")
                        .build();
            };
            appUser.get().setApproved(true);
            appUserRepository.save(appUser.get());
            return CommonResponseModel.builder()
                    .status(HttpStatus.OK)
                    .message("Provider Approved Successfully")
                    .build();
        }catch (Exception e){
            throw new AuthenticationException(e.getMessage());
        }
    }

    @Override
    public CommonResponseModel getAllProviders(String phoneNumber) throws AuthenticationException {
        try {
            Optional<List<AppUser>> providers = Optional.ofNullable(appUserRepository.findByRole(Role.PROVIDER));
            if (providers.isEmpty()) throw new AuthenticationException("No Providers Found");
            return CommonResponseModel.builder()
                    .status(HttpStatus.OK)
                    .message("List Of All Providers Retrieved Successfully")
                    .data(providers)
                    .build();
        }catch (Exception e){
            throw new AuthenticationException(e.getMessage());
        }
    }

    @Override
    public CommonResponseModel editProviderBySuperAdmin(String phoneNumber, SignUpRequestModel body, Long providerId) throws AuthenticationException {
        try {
            Optional<AppUser> appUser = appUserRepository.findByIdAndRole(providerId, Role.PROVIDER);
            if (appUser.isEmpty()) throw new AuthenticationException("No User Found");
            if (body.getEmail()!=null){
                appUser.get().setEmail(body.getEmail());
            }
            if (body.getPhoneNumber()!=null){
                appUser.get().setPhoneNumber(body.getPhoneNumber());
            }
            if (body.getFullName()!=null){
                appUser.get().setFullName(body.getFullName());
            }
            if (body.getPassword()!=null){
                appUser.get().setPassword(passwordEncoder.encode(body.getPassword()));
            }
            appUserRepository.save(appUser.get());
            return CommonResponseModel.builder()
                    .status(HttpStatus.OK)
                    .message("User Updated Successfully")
                    .build();

        }catch (Exception e){
            throw new AuthenticationException(e.getMessage());
        }
    }

    @Override
    public CommonResponseModel getTotalNumberOfCustomers(String phoneNumber) throws AuthenticationException {
        try {
            Optional<AppUser> appUser =  appUserRepository.findByPhoneNumber(phoneNumber);
            if (appUser.isPresent() && appUser.get().getRole()==Role.ADMIN){
                Long totalNumberOfProviders = appUserRepository.findTotalNumberOfCustomers(Role.USER);
                return CommonResponseModel.builder()
                        .status(HttpStatus.OK)
                        .message("Total Number Of Customers Retrieved Successfully")
                        .data(totalNumberOfProviders)
                        .build();
            }else {
                throw new AuthenticationException("Invalid User");
            }
        }catch (Exception e){
            throw new AuthenticationException(e.getMessage());
        }
    }

    @Override
    public CommonResponseModel getAllProvidersSubscriptions(String phoneNumber) throws AuthenticationException {
        try {
            Optional<AppUser> user = appUserRepository.findByPhoneNumber(phoneNumber);
            if (user.isEmpty()) throw new AuthenticationException("User Does Not Exist");
            Optional<List<Subscription>> optionalSubscriptions = Optional.of(subscriptionRepository.findAll());

            List<UserSubscriptionDTO> providerOrderResponseDTOs = optionalSubscriptions.get().stream().map(subscription -> {
                return UserSubscriptionDTO
                        .builder()
                        .userId(subscription.getUser().getId())
                        .price(subscription.getPrice())
                        .userName(subscription.getUser().getFullName())
                        .phoneNumber(subscription.getUser().getPhoneNumber())
                        .subscriptionType(subscription.getSubscriptionType())
                        .startDate(subscription.getStartDate())
                        .endDate(subscription.getEndDate())
                        .subscribed(subscription.isActive())
                        .build();
            }).collect(Collectors.toList());

            return CommonResponseModel.builder()
                    .status(HttpStatus.OK)
                    .message("Subscriptions List Retrieved Successfully")
                    .data(providerOrderResponseDTOs)
                    .build();

        }catch (Exception e){
            throw new AuthenticationException(e.getMessage());
        }
    }

    @Override
    public CommonResponseModel getProviderById(String phoneNumber, Long providerId) throws AuthenticationException {
        try {
            Optional<AppUser> user = appUserRepository.findByPhoneNumber(phoneNumber);
            if (user.isEmpty()) throw new AuthenticationException("User Does Not Exist");
            Optional<AppUser> providerUser = appUserRepository.findAppUserById(providerId);
            if (providerUser.isEmpty()) throw new AuthenticationException("No Provider User Found For This Id "+ providerId);
            if (providerUser.get().getRole() != Role.PROVIDER) throw new AuthenticationException("This Is Not Provider");
            return CommonResponseModel.builder()
                    .status(HttpStatus.OK)
                    .message("Provider Data Retrieved Successfully")
                    .data(providerUser.get())
                    .build();
        }catch (Exception e){
            throw new AuthenticationException(e.getMessage());
        }
    }

    @Override
    public CommonResponseModel getAllCustomers(String phoneNumber) throws AuthenticationException {
        try {
            Optional<List<AppUser>> providers = Optional.ofNullable(appUserRepository.findByRole(Role.USER));
            if (providers.isEmpty()) throw new AuthenticationException("No Customers Found");
            return CommonResponseModel.builder()
                    .status(HttpStatus.OK)
                    .message("List Of All Customers Retrieved Successfully")
                    .data(providers)
                    .build();
        }catch (Exception e){
            throw new AuthenticationException(e.getMessage());
        }
    }

    @Override
    public OTPResponseModel customerSignUp(CustomerSignUpRequestModel body) throws AuthenticationException {
        if(body.getFullName() == null){
            throw new AuthenticationException("Full name is required");
        }
        if(body.getPhoneNumber() == null){
            throw new AuthenticationException("Phone number is required");
        }
        if(body.getPassword() == null){
            throw new AuthenticationException("Password is required");
        }
        if(body.getDateOfBirth() == null){
            throw new AuthenticationException("DOB is required");
        }
        if(body.getCity() == null){
            throw new AuthenticationException("City is required");
        }
        if(body.getRegionId() == null){
            throw new AuthenticationException("Region is required");
        }
        if(body.getGender() == null){
            throw new AuthenticationException("Gender is required");
        }
        Optional<Region> optionalRegion = Optional.ofNullable(regionRepository.findById(body.getRegionId())
                .orElseThrow(() -> new AuthenticationException("Region not found with ID: " + body.getRegionId())));

        Optional<AppUser> checkIfUserExistsByPhoneNumber = appUserRepository.findByPhoneNumber(body.getPhoneNumber());
        Optional<AppUser> checkIfUserExistsByEmail = appUserRepository.findByEmail(body.getEmail());

        if(checkIfUserExistsByPhoneNumber.isPresent()){
            throw new AuthenticationException("This User with number '"+ body.getPhoneNumber()+ "' already exists");
        }
        if(checkIfUserExistsByEmail.isPresent()){
            throw new AuthenticationException("This User with email '"+ body.getEmail()+ "' already exists");
        }

        try {
            if(SportOnApplication.getAccessToken() == null || SportOnApplication.getAccessToken().equalsIgnoreCase("")){
                System.out.println(SportOnApplication.getAccessToken());
                throw new AuthenticationException("Access token is not provide for SMS");
            }else{
                log.info("user body {}", body);
//            sendOTP(body.getPhoneNumber());
                sendEmailVerification(body.getEmail());
                user = AppUser.builder()
                        .fullName(body.getFullName())
                        .email(body.getEmail())
                        .phoneNumber(body.getPhoneNumber())
                        .dateOfBirth(body.getDateOfBirth())
                        .region(optionalRegion.get())
                        .city(body.getCity())
                        .membershipType(MembershipType.REGULAR)
                        .password(passwordEncoder.encode(body.getPassword()))
                        .joinedDate(getCurrentDate())
                        .role(body.getRole())
                        .approved(true)
                        .gender(body.getGender())
                        .build();
                return OTPResponseModel.builder()
                        .message("OTP is sent, verify")
                        .build();
            }

        }catch (Exception e){
            System.out.println(e.getMessage());
            throw new AuthenticationException(e.getMessage());
        }
    }

    @Override
    public OTPResponseModel providerSignUp(ProviderSignUpRequestModal body) throws AuthenticationException {
        if(body.getFullName() == null){
            throw new AuthenticationException("Full name is required");
        }
        if(body.getPhoneNumber() == null){
            throw new AuthenticationException("Phone number is required");
        }
        if(body.getPassword() == null){
            throw new AuthenticationException("Password is required");
        }
        if(body.getDateOfBirth() == null){
            throw new AuthenticationException("DOB is required");
        }
        if(body.getCity() == null){
            throw new AuthenticationException("City is required");
        }
        if(body.getRegionId() == null){
            throw new AuthenticationException("Region is required");
        }
        if(body.getGender() == null){
            throw new AuthenticationException("Gender is required");
        }
        Optional<Region> optionalRegion = Optional.ofNullable(regionRepository.findById(body.getRegionId())
                .orElseThrow(() -> new AuthenticationException("Region not found with ID: " + body.getRegionId())));

        Optional<AppUser> checkIfUserExistsByPhoneNumber = appUserRepository.findByPhoneNumber(body.getPhoneNumber());
        Optional<AppUser> checkIfUserExistsByEmail = appUserRepository.findByEmail(body.getEmail());

        if(checkIfUserExistsByPhoneNumber.isPresent()){
            throw new AuthenticationException("This User with number '"+ body.getPhoneNumber()+ "' already exists");
        }
        if(checkIfUserExistsByEmail.isPresent()){
            throw new AuthenticationException("This User with email '"+ body.getEmail()+ "' already exists");
        }

        try {
//            if(SportOnApplication.getAccessToken() == null || SportOnApplication.getAccessToken().equalsIgnoreCase("")){
//                System.out.println(SportOnApplication.getAccessToken());
//                throw new AuthenticationException("Access token is not provide for SMS");
//            }else{
                log.info("user body {}", body);
//            sendOTP(body.getPhoneNumber());
                providerPassword = body.getPassword();
                sendEmailVerificationForProvider(body.getEmail());
                providerUser = AppUser.builder()
                        .fullName(body.getFullName())
                        .email(body.getEmail())
                        .phoneNumber(body.getPhoneNumber())
                        .dateOfBirth(body.getDateOfBirth())
                        .region(optionalRegion.get())
                        .city(body.getCity())
                        .membershipType(MembershipType.REGULAR)
                        .password(passwordEncoder.encode(body.getPassword()))
                        .joinedDate(getCurrentDate())
                        .role(Role.PROVIDER)
                        .approved(false)
                        .gender(body.getGender())
                        .build();
                return OTPResponseModel.builder()
                        .message("OTP is sent, verify")
                        .build();
//            }

        }catch (Exception e){
            System.out.println(e.getMessage());
            throw new AuthenticationException(e.getMessage());
        }
    }

    @Override
    public ProviderSignUpResponseModal verifyOTPForProviderSignUp(OTP otp) throws AuthenticationException {
        if (otp == null){
            throw new AuthenticationException("Enter the otp sent to your phone number.");
        }
        if (isOtpValid(otp.getOtp(), generatedTime)) {
            if(otp.getOtp().equalsIgnoreCase(generatedOTPForProvider.getGeneratedOTP())){
                appUserRepository.save(providerUser);
                String token = jwtService.generateToken(providerUser);
                String refreshToken = jwtService.generateRefreshToken(providerUser);
                saveUserToken(providerUser, token);
                sendEmailForProviderForCredentialSharing(providerUser);

                ProviderSignUpResponseModal signUpResponseModel = ProviderSignUpResponseModal.builder()
                        .status(HttpStatus.CREATED)
                        .message("Provider User is created successfully"+
                                "We have sent an email to you, inside the email is your credentials to your account as a provider.")
                        .build();
                return signUpResponseModel;
            }
            else {
                throw new AuthenticationException("OTP is invalid");
            }
        } else {
            System.out.println("OTP is invalid");
            throw new AuthenticationException("OTP is invalid");
        }
    }

//    private UserSubscriptionDTO mapToUserSubscriptionDTO(Subscription subscription) {
//        String userName = getUserNameById(subscription.getUserId()); // Assume a method to fetch user name
//        String phoneNumber = getUserPhoneNumberById(subscription.getUserId()); // Assume a method to fetch phone number
//
//        return new UserSubscriptionDTO(
//                userName,
//                phoneNumber,
//                subscription.getUserId(),
//                subscription.getType(),
//                subscription.getStartDate(),
//                subscription.getEndDate(),
//                subscription.getPrice()
//        );
//    }

    @Override
    public ForgetPasswordResponse updatePassword(ChangePasswordCredentials changePasswordCredentials) throws AuthenticationException {
        try {
            Optional<AppUser> appUser = appUserRepository.findByPhoneNumber(changePasswordCredentials.getPhoneNumber());
            if (appUser.isPresent()){
            appUser.get().setPassword(passwordEncoder.encode(changePasswordCredentials.getNewPassword()));
            appUserRepository.save(appUser.get());
            ForgetPasswordResponse forgetPasswordResponse = ForgetPasswordResponse
                    .builder()
                    .message("Password update successfully")
                    .build();
            return forgetPasswordResponse;
            }else{
                throw new AuthenticationException("User does not exist");
            }
        }catch (Exception e){
            throw new AuthenticationException("User not found");
        }
    }


    private void sendOTP(String phoneNumber) throws URISyntaxException, IOException, InterruptedException, AuthenticationException {
        generatedOTP.setGeneratedOTP(generateOTP());
        generatedTime=LocalDateTime.now();
        SendOTPRequestModel otpRequestModel = SendOTPRequestModel.builder()
                .mobile(phoneNumber)
                .message(generatedOTP.getGeneratedOTP())
                .RequestDate(getCurrentDate())
                .build();
        Gson gson = new Gson();
        String otpJsonRequest =  gson.toJson(otpRequestModel);
        log.info("SMS token for hormuud {}", SportOnApplication.getAccessToken());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("https://smsapi.hormuud.com/api/SendSMS"))
                .setHeader("Content-Type", "application/json")
                .header("Authorization", "Bearer "+ SportOnApplication.getAccessToken())
                .POST(HttpRequest.BodyPublishers.ofString(otpJsonRequest))
                .build();
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpResponse response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        log.info("Sent OTP response {}", response.body());
        System.out.println("otp result = "+ response.body());
        if (response.statusCode() != 200){
            throw new AuthenticationException("OTP Could not be sent");
        }
    }

        public void sendEmailVerification(String to) throws AuthenticationException {
        try {
            generatedOTP.setGeneratedOTP(generateOTP());
            generatedTime=LocalDateTime.now();
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Verify OTP For Account Verification");
            message.setText("OTP : " + generatedOTP.getGeneratedOTP());
            emailSender.send(message);
        }catch (Exception e){
            log.info(e.getMessage());
            throw new AuthenticationException(e.getMessage());
        }
        }
    public void sendEmailVerificationForProvider(String to) throws AuthenticationException {
        try {
            generatedOTPForProvider.setGeneratedOTP(generateOTP());
            generatedTime=LocalDateTime.now();
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Verify OTP For Provider Account Verification");
            message.setText("OTP : " + generatedOTPForProvider.getGeneratedOTP());
            emailSender.send(message);
        }catch (Exception e){
            log.info(e.getMessage());
            throw new AuthenticationException(e.getMessage());
        }
    }


    public void sendEmailForProviderForCredentialSharing(AppUser providerUser) throws AuthenticationException {
        try {
            MimeMessage mimeMessage = javaEmailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            String htmlContent = "<!DOCTYPE html>"
                    + "<html>"
                    + "<head>"
                    + "<meta charset='UTF-8'>"
                    + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                    + "<style>"
                    + "body {font-family: Arial, sans-serif; background-color: #f4f4f4;}"
                    + ".email-container {background-color: #ffffff; max-width: 600px; margin: 20px auto; padding: 20px; border-radius: 10px; box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);}"
                    + ".header {background-color: #4CAF50; color: #ffffff; text-align: center; padding: 10px 0; border-radius: 10px 10px 0 0;}"
                    + ".header h1 {margin: 0; font-size: 24px;}"
                    + ".content {padding: 20px; font-size: 16px; line-height: 1.6;}"
                    + ".content h2 {color: #333333;}"
                    + ".button-container {text-align: center; margin-top: 20px;}"
                    + ".button {background-color: #4CAF50; color: #ffffff; padding: 10px 20px; text-decoration: none; border-radius: 5px; font-size: 16px;}"
                    + ".footer {text-align: center; color: #888888; margin-top: 20px;}"
                    + "</style>"
                    + "</head>"
                    + "<body>"
                    + "<div class='email-container'>"
                    + "<div class='header'>"
                    + "<h1>Welcome to Our Service</h1>"
                    + "</div>"
                    + "<div class='content'>"
                    + "<h2>Credentials</h2>"
                    + "<p>Hi " + providerUser.getFullName() + ",</p>"
                    + "<p>We are excited to have you on board. Below are your login credentials:</p>"
                    + "<ul>"
                    + "<li><strong>Phone Number:</strong> " + providerUser.getPhoneNumber() + "</li>"
                    + "<li><strong>Password:</strong> " + providerPassword + "</li>"
                    + "</ul>"
//                    + "<p>Please use the above information to log into your account. We recommend you change your password after logging in for the first time.</p>"
//                    + "<div class='button-container'>"
//                    + "<a href='#' class='button'>Log In Now</a>"
//                    + "</div>"
                    + "<p>If you need any assistance, feel free to contact our support team using our website contact form.</p>"
                    + "</div>"
                    + "<div class='footer'>"
                    + "<p>&copy; 2024 SportOn. All rights reserved.</p>"
                    + "</div>"
                    + "</div>"
                    + "</body>"
                    + "</html>";

            helper.setText(htmlContent, true); // Set 'true' for HTML content
            helper.setTo(providerUser.getEmail());
            helper.setSubject("Your Account Credentials");

            emailSender.send(mimeMessage);

        } catch (MessagingException e) {
            log.error("Error while sending email", e);
            throw new AuthenticationException("Failed to send email.");
        }
    }



    private String generateOTP(){
        Random random = new Random();
        StringBuilder otp = new StringBuilder();

        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }

        return otp.toString();
    }
    private void saveUserToken(AppUser user, String token){
        Token jwtToken = Token.builder()
                .token(token)
                .user(user)
                .expired(false)
                .revoked(false)
                .tokenType(TokenType.BEARER)
                .build();
        tokenRepository.save(jwtToken);
    }
    private void revokeAllUserTokens(AppUser user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }
    public String getCurrentDate(){
        // Get the current date and time
        Date date = new Date();
        Calendar cal = Calendar.getInstance();

        // Convert the date to a string
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = formatter.format(date);
        return formattedDate;
    }

    public static boolean isOtpValid(String otp, LocalDateTime generatedTime) {
        LocalDateTime expirationTime = generatedTime.plusMinutes(EXPIRATION_TIME_IN_MINUTES);
        LocalDateTime now = LocalDateTime.now();
        return now.isBefore(expirationTime);
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void checkSubscriptions() throws AuthenticationException {
        try {
            List<AppUser> users = appUserRepository.findByRole(Role.PROVIDER);
            for (AppUser user : users) {
                boolean anyActiveSubscription = user.getSubscriptions().stream()
                        .anyMatch(Subscription::isActive);

                if (!anyActiveSubscription && user.getApproved()) {
                    user.setApproved(false);
                    appUserRepository.save(user);
                }
            }
        }catch (Exception e){
            throw new AuthenticationException(e.getMessage());
        }
    }
}