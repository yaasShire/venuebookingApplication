package com.sporton.SportOn.controller;

import com.sporton.SportOn.configuration.JWTService;
import com.sporton.SportOn.entity.AppUser;
import com.sporton.SportOn.entity.Subscription;
import com.sporton.SportOn.entity.SubscriptionType;
import com.sporton.SportOn.exception.authenticationException.AuthenticationException;
import com.sporton.SportOn.model.CommonResponseModel;
import com.sporton.SportOn.model.authenticationModel.*;
import com.sporton.SportOn.service.authenticationService.AuthenticateService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/authenticate")
@RequiredArgsConstructor
public class AuthenticationController {
    private  final AuthenticateService authenticateService;
    private final JWTService jwtService;

    @PostMapping("/signUp")
    @Operation(summary = "Sign Up", description = "This endpoint register user.")
    public OTPResponseModel signUpUser(@RequestBody SignUpRequestModel body) throws AuthenticationException {
        return authenticateService.signUpUser(body);
    }

    @PostMapping("/customerSignUp")
    @Operation(summary = "Sign Up", description = "This endpoint register user.")
    public OTPResponseModel customerSignUp(@RequestBody CustomerSignUpRequestModel body) throws AuthenticationException {
        return authenticateService.customerSignUp(body);
    }

    @PostMapping("/verifyOTP")
    public SignUpResponseModel verifyOTP(@RequestBody OTP otp) throws AuthenticationException {
        return authenticateService.verifyOTP(otp);
    }

    @PostMapping("/signIn")
    public SignInResponseModel singIn(@RequestBody SignInRequestModel body) throws AuthenticationException {
        return authenticateService.signIn(body);
    }

    @PostMapping("/singInAsCustomer")
    public SignInResponseModel singInAsCustomer(
            @RequestBody SignInRequestModel body
    ) throws AuthenticationException {
        return authenticateService.singInAsCustomer(body);
    }

    @PostMapping("/singInAsProvider")
    public SignInResponseModel singInAsProvider(
            @RequestBody SignInRequestModel body
    ) throws AuthenticationException {
        return authenticateService.singInAsProvider(body);
    }
    @PostMapping("/requestForgetPasswordOTP")
    public OTPResponseModel forgetPasswordVerificationPhoneNumber(@RequestBody RequestForgetPasswordOTP phoneNumber) throws AuthenticationException {
        return authenticateService.sendForgetPasswordOTP(phoneNumber);
    }

    @PostMapping("/userRequestForgetPasswordOTP")
    public OTPResponseModel userForgetPasswordVerificationPhoneNumber(@RequestBody RequestForgetPasswordOTP phoneNumber) throws AuthenticationException {
        return authenticateService.sendUserForgetPasswordOTP(phoneNumber);
    }

    @PostMapping("/verifyForgetPasswordOTP")
    public OTPResponseModel verifyForgetPasswordOTP(@RequestBody OTP otp) throws AuthenticationException {
        return authenticateService.verifyForgetPasswordOTP(otp);
    }

    @PostMapping("/changeForgottedPassword")
    public ForgetPasswordResponse updateForgettedPassword(@RequestBody ChangePasswordCredentials changePasswordCredentials) throws AuthenticationException {
        return authenticateService.updatePassword(changePasswordCredentials);
    }

    @GetMapping("/profileData")
    public AppUser getProfileData(
            @RequestHeader("Authorization") String authorizationHeader
    ) throws AuthenticationException {
        try {
        String token = authorizationHeader.substring(7); // Remove "Bearer " prefix
        String phoneNumber = jwtService.extractUsername(token);
        return authenticateService.getProfileData(phoneNumber);
        }catch (Exception e){
            throw new AuthenticationException(e.getMessage());
        }
    }
    @PutMapping("/updateProfileData")
    public CommonResponseModel updateProfileData(
            @RequestBody UpdateProfileData body,
            @RequestHeader("Authorization") String authorizationHeader
    ) throws AuthenticationException {
        try {
            String token = authorizationHeader.substring(7); // Remove "Bearer " prefix
            String phoneNumber = jwtService.extractUsername(token);
         return authenticateService.updateProfileData(body, phoneNumber);
        }catch (Exception e){
            throw new AuthenticationException(e.getMessage());
        }
    }

    @PutMapping("/changePassword")
    public CommonResponseModel changePassword(
            @RequestBody ChangePasswordAuthrorizedRequest body,
            @RequestHeader("Authorization") String authorizationHeader
    ) throws AuthenticationException {
        try {
            String token = authorizationHeader.substring(7); // Remove "Bearer " prefix
            String phoneNumber = jwtService.extractUsername(token);
            return authenticateService.changePassword(body, phoneNumber);
        }catch (Exception e){
            throw new AuthenticationException(e.getMessage());
        }
    }

    @PutMapping("/uploadProfileImage")
    public CommonResponseModel uploadProfileImage(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("Authorization") String authorizationHeader
    ) throws AuthenticationException {
        try {
            String token = authorizationHeader.substring(7); // Remove "Bearer " prefix
            String phoneNumber = jwtService.extractUsername(token);
            return authenticateService.uploadProfileImage(file, phoneNumber);
        }catch (Exception e){
            throw new AuthenticationException(e.getMessage());
        }
    }

    @GetMapping("/getTotalNumberOfProviders")
    public CommonResponseModel getTotalNumberOfProviders(
            @RequestHeader("Authorization") String authorizationHeader
    ) throws AuthenticationException {
        try {
            String token = authorizationHeader.substring(7); // Remove "Bearer " prefix
            String phoneNumber = jwtService.extractUsername(token);
            return authenticateService.getTotalNumberOfProviders(phoneNumber);
        }catch (Exception e){
            throw new AuthenticationException(e.getMessage());
        }
    }

    @GetMapping("/getTotalNumberOfCustomers")
    public CommonResponseModel getTotalNumberOfCustomers(
            @RequestHeader("Authorization") String authorizationHeader
    ) throws AuthenticationException {
        try {
            String token = authorizationHeader.substring(7); // Remove "Bearer " prefix
            String phoneNumber = jwtService.extractUsername(token);
            return authenticateService.getTotalNumberOfCustomers(phoneNumber);
        }catch (Exception e){
            throw new AuthenticationException(e.getMessage());
        }
    }

    @GetMapping("/getTotalNumberOfSubscribedProviders")
    public CommonResponseModel getTotalNumberOfSubscribedProviders(
            @RequestHeader("Authorization") String authorizationHeader
    ) throws AuthenticationException {
        try {
            String token = authorizationHeader.substring(7); // Remove "Bearer " prefix
            String phoneNumber = jwtService.extractUsername(token);
            return authenticateService.getTotalNumberOfSubscribedProviders(phoneNumber);
        }catch (Exception e){
            throw new AuthenticationException(e.getMessage());
        }
    }

    @GetMapping("/getTotalNumberOfUnSubscribedProviders")
    public CommonResponseModel getTotalNumberOfUnSubscribedProviders(
            @RequestHeader("Authorization") String authorizationHeader
    ) throws AuthenticationException {
        try {
            String token = authorizationHeader.substring(7); // Remove "Bearer " prefix
            String phoneNumber = jwtService.extractUsername(token);
            return authenticateService.getTotalNumberOfUnSubscribedProviders(phoneNumber);
        }catch (Exception e){
            throw new AuthenticationException(e.getMessage());
        }
    }

    @GetMapping("/isUserSubscribed")
    public CommonResponseModel isUserSubscribed(
            @RequestHeader("Authorization") String authorizationHeader
    ) throws AuthenticationException {
        try {
            String token = authorizationHeader.substring(7); // Remove "Bearer " prefix
            String phoneNumber = jwtService.extractUsername(token);
            return authenticateService.isUserSubscribed(phoneNumber);
        }catch (Exception e){
            throw new AuthenticationException(e.getMessage());
        }
    }


    @PostMapping("/providerSubscribe")
    public Subscription subscribeUser(
            @RequestBody SubscriptionModal body,
            @RequestHeader("Authorization") String authorizationHeader
    ) throws AuthenticationException {
        try {
            String token = authorizationHeader.substring(7); // Remove "Bearer " prefix
            String phoneNumber = jwtService.extractUsername(token);
            return authenticateService.createSubscription(phoneNumber, body);
        }catch (Exception e){
            throw new AuthenticationException(e.getMessage());
        }
    }

    @PutMapping("/updateProviderSubscription")
    public Subscription updateProviderSubscription(
            @RequestBody SubscriptionModal body,
            @RequestHeader("Authorization") String authorizationHeader
    ) throws AuthenticationException {
        try {
            String token = authorizationHeader.substring(7); // Remove "Bearer " prefix
            String phoneNumber = jwtService.extractUsername(token);
            return authenticateService.updateProviderSubscription(phoneNumber, body);
        }catch (Exception e){
            throw new AuthenticationException(e.getMessage());
        }
    }

    @GetMapping("/getTotalProvidersSubscriptionIncomeByMonthlyOrYearly")
    public CommonResponseModel getTotalProvidersSubscriptionIncomeByMonthlyOrYearly(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam SubscriptionType subscriptionType
    ) throws AuthenticationException {
        try {
            String token = authorizationHeader.substring(7); // Remove "Bearer " prefix
            String phoneNumber = jwtService.extractUsername(token);
            return authenticateService.getTotalProvidersSubscriptionIncomeByMonthlyOrYearly(phoneNumber, subscriptionType);
        }catch (Exception e){
            throw new AuthenticationException(e.getMessage());
        }
    }

    @PutMapping("/approveProvider/{providerId}")
    public CommonResponseModel approveProvider(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long providerId
    ) throws AuthenticationException {
        try {
            String token = authorizationHeader.substring(7); // Remove "Bearer " prefix
            String phoneNumber = jwtService.extractUsername(token);
            return authenticateService.approveProvider(phoneNumber, providerId);
        }catch (Exception e){
            throw new AuthenticationException(e.getMessage());
        }
    }

    @GetMapping("/getAllProviders")
    public CommonResponseModel getAllProviders(
            @RequestHeader("Authorization") String authorizationHeader
    ) throws AuthenticationException {
        try {
            String token = authorizationHeader.substring(7); // Remove "Bearer " prefix
            String phoneNumber = jwtService.extractUsername(token);
            return authenticateService.getAllProviders(phoneNumber);
        }catch (Exception e){
            throw new AuthenticationException(e.getMessage());
        }
    }

    @GetMapping("/getAllCustomers")
    public CommonResponseModel getAllCustomers(
            @RequestHeader("Authorization") String authorizationHeader
    ) throws AuthenticationException {
        try {
            String token = authorizationHeader.substring(7); // Remove "Bearer " prefix
            String phoneNumber = jwtService.extractUsername(token);
            return authenticateService.getAllCustomers(phoneNumber);
        }catch (Exception e){
            throw new AuthenticationException(e.getMessage());
        }
    }

    @PutMapping("/editProviderBySuperAdmin/{providerId}")
    public CommonResponseModel editProviderBySuperAdmin(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long providerId,
            @RequestBody SignUpRequestModel body
    ) throws AuthenticationException {
        try {
            String token = authorizationHeader.substring(7); // Remove "Bearer " prefix
            String phoneNumber = jwtService.extractUsername(token);
            return authenticateService.editProviderBySuperAdmin(phoneNumber, body, providerId);
        }catch (Exception e){
            throw new AuthenticationException(e.getMessage());
        }
    }

    @GetMapping("/getAllProvidersSubscriptions")
    public CommonResponseModel getAllProvidersSubscriptions(
            @RequestHeader("Authorization") String authorizationHeader
    ) throws AuthenticationException {
        try {
            String token = authorizationHeader.substring(7); // Remove "Bearer " prefix
            String phoneNumber = jwtService.extractUsername(token);
            return authenticateService.getAllProvidersSubscriptions(phoneNumber);
        }catch (Exception e){
            throw new AuthenticationException(e.getMessage());
        }
    }

    @GetMapping("/getProviderById/{providerId}")
    public CommonResponseModel getProviderById(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long providerId
    ) throws AuthenticationException {
        try {
            String token = authorizationHeader.substring(7); // Remove "Bearer " prefix
            String phoneNumber = jwtService.extractUsername(token);
            return authenticateService.getProviderById(phoneNumber, providerId);
        }catch (Exception e){
            throw new AuthenticationException(e.getMessage());
        }
    }
}
