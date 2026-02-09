package com.example.foodplanner.data.firebase;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.concurrent.Executors;

/**
 * Helper class for Firebase Authentication operations.
 * Supports email/password and Google Sign-In.
 */
public class FirebaseAuthHelper {

    private static FirebaseAuthHelper instance;
    private final FirebaseAuth firebaseAuth;

    public interface AuthCallback {
        void onSuccess(FirebaseUser user);

        void onError(String message);
    }

    private FirebaseAuthHelper() {
        firebaseAuth = FirebaseAuth.getInstance();
    }

    public static synchronized FirebaseAuthHelper getInstance() {
        if (instance == null) {
            instance = new FirebaseAuthHelper();
        }
        return instance;
    }

    public FirebaseAuth getFirebaseAuth() {
        return firebaseAuth;
    }

    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    public boolean isLoggedIn() {
        return firebaseAuth.getCurrentUser() != null;
    }

    /**
     * Sign in with email and password
     */
    public void signIn(String email, String password, AuthCallback callback) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        callback.onSuccess(user);
                    } else {
                        String message;
                        try {
                            throw task.getException();
                        } catch (com.google.firebase.auth.FirebaseAuthInvalidUserException
                                | com.google.firebase.auth.FirebaseAuthInvalidCredentialsException e) {
                            message = "Wrong password or email";
                        } catch (Exception e) {
                            message = e.getMessage() != null ? e.getMessage() : "Sign in failed";
                        }
                        callback.onError(message);
                    }
                });
    }

    /**
     * Create new user with email and password
     */
    public void signUp(String email, String password, String displayName, AuthCallback callback) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null && displayName != null && !displayName.isEmpty()) {
                            // Update display name
                            user.updateProfile(new com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                    .setDisplayName(displayName)
                                    .build())
                                    .addOnCompleteListener(profileTask -> callback.onSuccess(user));
                        } else {
                            callback.onSuccess(user);
                        }
                    } else {
                        String message = task.getException() != null
                                ? task.getException().getMessage()
                                : "Sign up failed";
                        callback.onError(message);
                    }
                });
    }

    /**
     * Sign in with Google using Credential Manager
     */
    public void signInWithGoogle(Activity activity, String webClientId, AuthCallback callback) {
        CredentialManager credentialManager = CredentialManager.create(activity);

        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        credentialManager.getCredentialAsync(
                activity,
                request,
                null,
                Executors.newSingleThreadExecutor(),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        handleGoogleSignInResult(result, callback);
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        // Check if it's a cancellation
                        if (e instanceof androidx.credentials.exceptions.GetCredentialCancellationException) {
                            // User cancelled, do nothing or log
                            return;
                        }
                        callback.onError("Google Sign-In failed: " + e.getMessage());
                    }
                });
    }

    private void handleGoogleSignInResult(GetCredentialResponse result, AuthCallback callback) {
        if (result.getCredential() instanceof CustomCredential) {
            CustomCredential credential = (CustomCredential) result.getCredential();
            if (GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(credential.getType())) {
                GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential
                        .createFrom(credential.getData());
                String idToken = googleIdTokenCredential.getIdToken();

                // Authenticate with Firebase
                AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
                firebaseAuth.signInWithCredential(firebaseCredential)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                callback.onSuccess(firebaseAuth.getCurrentUser());
                            } else {
                                String message = task.getException() != null
                                        ? task.getException().getMessage()
                                        : "Firebase authentication failed";
                                callback.onError(message);
                            }
                        });
            } else {
                callback.onError("Unexpected credential type");
            }
        } else {
            callback.onError("Unexpected credential type");
        }
    }

    /**
     * Sign out the current user
     */
    public void signOut() {
        firebaseAuth.signOut();
    }

    /**
     * Send password reset email
     */
    public void sendPasswordResetEmail(String email, AuthCallback callback) {
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(null);
                    } else {
                        String message = task.getException() != null
                                ? task.getException().getMessage()
                                : "Failed to send reset email";
                        callback.onError(message);
                    }
                });
    }
}
