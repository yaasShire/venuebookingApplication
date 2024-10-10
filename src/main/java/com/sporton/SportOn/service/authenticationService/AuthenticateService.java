package com.sporton.SportOn.service.authenticationService;


import com.sporton.SportOn.entity.AppUser;
import com.sporton.SportOn.entity.Subscription;
import com.sporton.SportOn.entity.SubscriptionType;
import com.sporton.SportOn.exception.authenticationException.AuthenticationException;
import com.sporton.SportOn.model.CommonResponseModel;
import com.sporton.SportOn.model.authenticationModel.*;
import org.springframework.web.multipart.MultipartFile;

public interface AuthenticateService {
    public OTPResponseModel signUpUser(SignUpRequestModel body) throws AuthenticationException;

    SignUpResponseModel verifyOTP(OTP otp) throws AuthenticationException;

    SignInResponseModel signIn(SignInRequestModel body) throws AuthenticationException;

    OTPResponseModel sendForgetPasswordOTP(RequestForgetPasswordOTP phoneNumber) throws AuthenticationException;

    ForgetPasswordResponse updatePassword(ChangePasswordCredentials changePasswordCredentials) throws AuthenticationException;

    OTPResponseModel verifyForgetPasswordOTP(OTP otp) throws AuthenticationException;

    AppUser getProfileData(String phoneNumber) throws AuthenticationException;

    CommonResponseModel updateProfileData(UpdateProfileData body, String phoneNumber) throws AuthenticationException;

    CommonResponseModel changePassword(ChangePasswordAuthrorizedRequest body, String phoneNumber) throws AuthenticationException;

    SignInResponseModel singInAsCustomer(SignInRequestModel body) throws AuthenticationException;

    SignInResponseModel singInAsProvider(SignInRequestModel body) throws AuthenticationException;

    OTPResponseModel sendUserForgetPasswordOTP(RequestForgetPasswordOTP phoneNumber) throws AuthenticationException;

    CommonResponseModel uploadProfileImage(MultipartFile file, String phoneNumber) throws AuthenticationException;

    CommonResponseModel getTotalNumberOfProviders(String phoneNumber) throws AuthenticationException;

    CommonResponseModel getTotalNumberOfSubscribedProviders(String phoneNumber) throws AuthenticationException;

    CommonResponseModel isUserSubscribed(String phoneNumber) throws AuthenticationException;

    Subscription createSubscription(String  phoneNumber, SubscriptionModal body) throws AuthenticationException;

    CommonResponseModel getTotalNumberOfUnSubscribedProviders(String phoneNumber) throws AuthenticationException;

    Subscription updateProviderSubscription(String phoneNumber, SubscriptionModal body) throws AuthenticationException;

    CommonResponseModel getTotalProvidersSubscriptionIncomeByMonthlyOrYearly(String phoneNumber, SubscriptionType subscriptionType) throws AuthenticationException;

    CommonResponseModel approveProvider(String phoneNumber, Long providerId) throws AuthenticationException;

    CommonResponseModel getAllProviders(String phoneNumber) throws AuthenticationException;

    CommonResponseModel editProviderBySuperAdmin(String phoneNumber, SignUpRequestModel body, Long providerId) throws AuthenticationException;

    CommonResponseModel getTotalNumberOfCustomers(String phoneNumber) throws AuthenticationException;

    CommonResponseModel getAllProvidersSubscriptions(String phoneNumber) throws AuthenticationException;

    CommonResponseModel getProviderById(String phoneNumber, Long providerId) throws AuthenticationException;

    CommonResponseModel getAllCustomers(String phoneNumber) throws AuthenticationException;

    OTPResponseModel customerSignUp(CustomerSignUpRequestModel body) throws AuthenticationException;

    OTPResponseModel providerSignUp(ProviderSignUpRequestModal body) throws AuthenticationException;

    ProviderSignUpResponseModal verifyOTPForProviderSignUp(OTP otp) throws AuthenticationException;
}
