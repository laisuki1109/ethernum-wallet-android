package com.innopage.core.fragment

import android.app.Application
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.innopage.core.R
import com.innopage.core.base.BaseViewModel
import com.innopage.core.webservice.config.Constants
import com.innopage.core.webservice.model.Error
import timber.log.Timber
import java.util.concurrent.*

/**
 * Firebase Auth Document: https://firebase.google.com/docs/auth/android/facebook-login
 * Facebook Login Document: https://developers.facebook.com/docs/facebook-login/android
 */
abstract class AuthViewModel(application: Application) : BaseViewModel(application) {

    companion object {
        private val FACEBOOK_LOGIN = 64206
        private val GOOGLE_LOGIN = 1
        private val GOOGLE_LINK = 2

        private val CODE_TIMEOUT_DURATION = 60L
    }

    private val _user = MutableLiveData<FirebaseUser?>()
    val user: LiveData<FirebaseUser?>
        get() = _user
//
//    // Init Facebook callback manager
//    private lateinit var callbackManager: CallbackManager

    // Store data for verify phone number
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private var storedVerificationId: String? = null

    private val auth: FirebaseAuth
        get() = FirebaseAuth.getInstance()

    // Sign in FirebaseAuth with credential from different platform
    // 1. Facebook
    // 2. Google
    // 3. Phone Number
    // ...
    private fun signInWithCredential(credential: AuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Timber.d("signInWithCredential:success")
                    _user.postValue(auth.currentUser)
                } else {
                    // If sign in fails, display a message to the user.
                    Timber.d("signInWithCredential:failure, ${task.exception}")
                    _error.postValue(
                        Error(
                            Constants.RESPONSE_LOGIN_ERROR,
                            null,
                            task.exception?.message
                        )
                    )
                }
            }
    }

    // Link FirebaseAuth with credential from different platform
    // 1. Facebook
    // 2. Google
    // 3. Phone Number
    // ...
    private fun linkWithCredential(credential: AuthCredential) {
        auth.currentUser?.linkWithCredential(credential)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Timber.d("signInWithCredential:success")
                    _user.postValue(auth.currentUser)
                } else {
                    // If sign in fails, display a message to the user.
                    Timber.d("signInWithCredential:failure, ${task.exception}")
                    _error.postValue(
                        Error(
                            Constants.RESPONSE_LOGIN_ERROR,
                            null,
                            task.exception?.message
                        )
                    )
                }
            }
    }

//    private fun firebaseAuthSignInWithFacebook(token: AccessToken) {
//        val credential = FacebookAuthProvider.getCredential(token.token)
//        signInWithCredential(credential)
//    }

//    private fun firebaseAuthLinkWithFacebook(token: AccessToken) {
//        val credential = FacebookAuthProvider.getCredential(token.token)
//        linkWithCredential(credential)
//    }

    private fun firebaseAuthSignInWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        signInWithCredential(credential)
    }

    private fun firebaseAuthLinkWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        linkWithCredential(credential)
    }

    private fun firebaseAuthSignInWithEmailPassword(email: String, password: String) {
        val credential = EmailAuthProvider.getCredential(email, password)
        signInWithCredential(credential)
    }

    private fun firebaseAuthLinkWithEmailPassword(email: String, password: String) {
        val credential = EmailAuthProvider.getCredential(email, password)
        linkWithCredential(credential)
    }

    private fun firebaseAuthSignInWithPhoneNumber(verificationId: String, code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        signInWithCredential(credential)
    }

    private fun firebaseAuthLinkWithPhoneNumber(verificationId: String, code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        linkWithCredential(credential)
    }

    // Login Facebook account with same flow with difference actions:
    // 1. FirebaseAuth Login
    // 2. FirebaseAuth Link
//    private fun loginFacebookAccountWithAction(
//        fragment: Fragment,
//        action: (accessToken: AccessToken) -> Unit
//    ) {
//        // Initialize Facebook callback manager
//        callbackManager = CallbackManager.Factory.create()
//
//        // Register Facebook login callback with FirebaseUser
//        LoginManager.getInstance().registerCallback(
//            callbackManager,
//            object : FacebookCallback<LoginResult> {
//                override fun onSuccess(loginResult: LoginResult) {
//                    Timber.d("facebook:onSuccess:$loginResult")
//                    action(loginResult.accessToken)
//                }
//
//                override fun onCancel() {
//                    Timber.d("facebook:onCancel")
//                    _error.postValue(
//                        Error(
//                            Constants.RESPONSE_LOGIN_ERROR,
//                            null,
//                            null
//                        )
//                    )
//                }
//
//                override fun onError(error: FacebookException) {
//                    Timber.d("facebook:onError$error")
//                    _error.postValue(
//                        Error(
//                            Constants.RESPONSE_LOGIN_ERROR,
//                            null,
//                            error.message
//                        )
//                    )
//                }
//            }
//        )
//
//        // Logout existing user
//        LoginManager.getInstance().logOut()
//
//        // Login user
//        LoginManager.getInstance().logInWithReadPermissions(
//            fragment.requireActivity(),
//            listOf("public_profile, email")
//        )
//    }

    // Login Google account with same flow with difference request code:
    // 1. FirebaseAuth Login: GOOGLE_LOGIN
    // 2. FirebaseAuth Link: GOOGLE_LINK
    private fun loginGoogleAccountWithRequestCode(requestCode: Int, fragment: Fragment) {
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(fragment.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        // Build a GoogleSignInClient with the options specified by gso.
        val googleSignInClient = GoogleSignIn.getClient(fragment.requireActivity(), gso)

        // Logout existing user
        googleSignInClient.signOut()

        // Login user
        val signInIntent = googleSignInClient.signInIntent
        fragment.startActivityForResult(signInIntent, requestCode)
    }

    // Login Phone Number with same flow with difference request code:
    // 1. FirebaseAuth Login
    // 2. FirebaseAuth Link
    private fun loginPhoneNumberWithAction(
        fragment: Fragment,
        phoneNumber: String,
        token: PhoneAuthProvider.ForceResendingToken?,
        action: (credential: AuthCredential) -> Unit
    ) {
        if (token == null) {
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber, // Phone number to verify
                CODE_TIMEOUT_DURATION, // Timeout duration
                TimeUnit.SECONDS, // Unit of timeout
                fragment.requireActivity(), // Activity (for callback binding)
                object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                        // This callback will be invoked in two situations:
                        // 1 - Instant verification. In some cases the phone number can be instantly
                        //     verified without needing to send or enter a verification code.
                        // 2 - Auto-retrieval. On some devices Google Play services can automatically
                        //     detect the incoming verification SMS and perform verification without
                        //     user action.
                        Timber.d("onVerificationCompleted:$credential")
                        action(credential)
                    }

                    override fun onVerificationFailed(e: FirebaseException) {
                        // This callback is invoked in an invalid request for verification is made,
                        // for instance if the the phone number format is not valid.
                        Timber.d("onVerificationFailed: $e")

                        if (e is FirebaseAuthInvalidCredentialsException) {
                            // Invalid request
                            _error.postValue(
                                Error(
                                    Constants.RESPONSE_PHONE_NUMBER_ERROR,
                                    null,
                                    e.message
                                )
                            )
                        } else if (e is FirebaseTooManyRequestsException) {
                            // The SMS quota for the project has been exceeded
                            _error.postValue(
                                Error(
                                    Constants.RESPONSE_PHONE_NUMBER_ERROR,
                                    null,
                                    e.message
                                )
                            )
                        }
                    }

                    override fun onCodeSent(
                        verificationId: String,
                        token: PhoneAuthProvider.ForceResendingToken
                    ) {
                        // The SMS verification code has been sent to the provided phone number, we
                        // now need to ask the user to enter the code and then construct a credential
                        // by combining the code with a verification ID.
                        Timber.d("onCodeSent:$verificationId")

                        // Save verification ID and resending token so we can use them later
                        storedVerificationId = verificationId
                        resendToken = token
                    }
                }
            )
        } else {
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber, // Phone number to verify
                CODE_TIMEOUT_DURATION, // Timeout duration
                TimeUnit.SECONDS, // Unit of timeout
                fragment.requireActivity(), // Activity (for callback binding)
                object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                        // This callback will be invoked in two situations:
                        // 1 - Instant verification. In some cases the phone number can be instantly
                        //     verified without needing to send or enter a verification code.
                        // 2 - Auto-retrieval. On some devices Google Play services can automatically
                        //     detect the incoming verification SMS and perform verification without
                        //     user action.
                        Timber.d("onVerificationCompleted:$credential")
                        action(credential)
                    }

                    override fun onVerificationFailed(e: FirebaseException) {
                        // This callback is invoked in an invalid request for verification is made,
                        // for instance if the the phone number format is not valid.
                        Timber.d("onVerificationFailed: $e")

                        if (e is FirebaseAuthInvalidCredentialsException) {
                            // Invalid request
                            _error.postValue(
                                Error(
                                    Constants.RESPONSE_PHONE_NUMBER_ERROR,
                                    null,
                                    e.message
                                )
                            )
                        } else if (e is FirebaseTooManyRequestsException) {
                            // The SMS quota for the project has been exceeded
                            _error.postValue(
                                Error(
                                    Constants.RESPONSE_PHONE_NUMBER_ERROR,
                                    null,
                                    e.message
                                )
                            )
                        }
                    }

                    override fun onCodeSent(
                        verificationId: String,
                        token: PhoneAuthProvider.ForceResendingToken
                    ) {
                        // The SMS verification code has been sent to the provided phone number, we
                        // now need to ask the user to enter the code and then construct a credential
                        // by combining the code with a verification ID.
                        Timber.d("onCodeSent:$verificationId")

                        // Save verification ID and resending token so we can use them later
                        storedVerificationId = verificationId
                        resendToken = token
                    }
                },
                token
            )
        }
    }

    private fun verifyPhoneNumberWithAction(
        code: String,
        action: (verificationId: String, code: String) -> Unit
    ) {
        action(storedVerificationId.orEmpty(), code)
    }

    fun handleCallback(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
//            FACEBOOK_LOGIN -> {
//                // Pass the activity result back to the Facebook SDK
//                callbackManager.onActivityResult(requestCode, resultCode, data)
//            }
            GOOGLE_LOGIN, GOOGLE_LINK -> {
                // Handle google login with requestCode and data
                handleGoogleCallback(requestCode, data)
            }
        }
    }

    fun handleGoogleCallback(requestCode: Int, data: Intent?) {
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            // Google Sign In was successful, authenticate with Firebase
            val account = task.getResult(ApiException::class.java)
            Timber.d("firebaseAuthWithGoogle: ${account?.id}")
            account?.idToken?.let { idToken ->
                when (requestCode) {
                    GOOGLE_LOGIN -> firebaseAuthSignInWithGoogle(idToken)
                    GOOGLE_LINK -> firebaseAuthLinkWithGoogle(idToken)
                }
            }
        } catch (e: ApiException) {
            // Google Sign In failed, update UI appropriately
            Timber.d("Google sign in failed: $e")
            _error.postValue(
                Error(
                    Constants.RESPONSE_GOOGLE_ERROR,
                    null,
                    e.message
                )
            )
        }
    }

    // Facebook Account Login
//    fun loginFacebookAccount(fragment: Fragment) {
//        loginFacebookAccountWithAction(fragment, ::firebaseAuthSignInWithFacebook)
//    }

//    fun linkFacebookAccount(fragment: Fragment) {
//        loginFacebookAccountWithAction(fragment, ::firebaseAuthLinkWithFacebook)
//    }

    // Google Account Sign-in
    fun loginGoogleAccount(fragment: Fragment) {
        loginGoogleAccountWithRequestCode(GOOGLE_LOGIN, fragment)
    }

    fun linkGoogleAccount(fragment: Fragment) {
        loginGoogleAccountWithRequestCode(GOOGLE_LINK, fragment)
    }

    // Email Password Authentication
    fun loginEmailPassword(email: String, password: String) {
        firebaseAuthSignInWithEmailPassword(email, password)
    }

    fun linkEmailPassword(email: String, password: String) {
        firebaseAuthLinkWithEmailPassword(email, password)
    }

    // Phone Number
    fun loginPhoneNumber(fragment: Fragment, phoneNumber: String) {
        loginPhoneNumberWithAction(fragment, phoneNumber, null, ::signInWithCredential)
    }

    fun resendCodeWithLoginPhoneNumber(fragment: Fragment, phoneNumber: String) {
        loginPhoneNumberWithAction(fragment, phoneNumber, resendToken, ::signInWithCredential)
    }

    fun loginPhoneNumberWithCode(code: String) {
        verifyPhoneNumberWithAction(code, ::firebaseAuthSignInWithPhoneNumber)
    }

    fun linkPhoneNumber(fragment: Fragment, phoneNumber: String) {
        loginPhoneNumberWithAction(fragment, phoneNumber, null, ::linkWithCredential)
    }

    fun resendCodeWithLinkPhoneNumber(fragment: Fragment, phoneNumber: String) {
        loginPhoneNumberWithAction(fragment, phoneNumber, resendToken, ::linkWithCredential)
    }

    fun linkPhoneNumberWithCode(code: String) {
        verifyPhoneNumberWithAction(code, ::firebaseAuthLinkWithPhoneNumber)
    }
}