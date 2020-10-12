package com.example.documentslocker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.util.Arrays;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    public static final String ANONYMOUS = "anonymous";
    private static final int RC_SIGN_IN = 5;
    private static FirebaseDatabase mDocumentDatabase;
    private static FirebaseStorage mDocumentStorage;
    private static FirebaseAuth mFirebaseAuth;
    private static String uid;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private Context mContext = LoginActivity.this;
    private String mUserName;

    public static FirebaseDatabase getDocumentDatabase() {
        return mDocumentDatabase;
    }

    public static FirebaseStorage getDocumentStorage() {
        return mDocumentStorage;
    }

    public static String getUid() {
        return uid;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUserName = ANONYMOUS;
        mFirebaseAuth = FirebaseAuth.getInstance();

        mDocumentDatabase = FirebaseDatabase.getInstance();
        mDocumentStorage = FirebaseStorage.getInstance();

        /*
        * Firebase Authentication State listener
        * */

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    mUserName = user.getDisplayName();
                    uid = user.getUid();
                    Toast.makeText(mContext, "Sign in Successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(mContext, MainActivity.class));
                } else {
                    /*
                    * If user == null
                    * Show Firebase AuthUI with google and email sign in
                    * */
                    startActivityForResult(AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.EmailBuilder().build(),
                                            new AuthUI.IdpConfig.GoogleBuilder().build()
                                    ))
                                    .setIsSmartLockEnabled(false)
                                    .build(),
                            RC_SIGN_IN);
                }

            }
        };

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /*
        * Result for sign in
        * */

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                mUserName = Objects.requireNonNull(mFirebaseAuth.getCurrentUser()).getDisplayName();
                uid = mFirebaseAuth.getCurrentUser().getUid();
                String text = "Signed in as " + mUserName;
                Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(mContext, MainActivity.class));
            }

            else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Sign in cancel! ", Toast.LENGTH_SHORT).show();
            }

            else if (response != null && Objects.requireNonNull(response.getError()).getErrorCode() == ErrorCodes.NO_NETWORK) {
                Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show();
                finish();
            }

            else if (response != null && response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                Toast.makeText(this, "Unknown Error Occurred!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuthStateListener != null) {
            mFirebaseAuth.addAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
        mAuthStateListener = null;
    }

}
