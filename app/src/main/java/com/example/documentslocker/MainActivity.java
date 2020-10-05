package com.example.documentslocker;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.util.Arrays;
import java.util.Objects;

import static com.example.documentslocker.MyDocumentsFragment.detachDatabaseReadListener;
import static com.example.documentslocker.MyDocumentsFragment.getDocumentAdapter;

public class MainActivity extends AppCompatActivity {

    private static FirebaseDatabase mDocumentDatabase;
    private static FirebaseStorage mDocumentStorage;
    public static FirebaseDatabase getDocumentDatabase() {
        return mDocumentDatabase;
    }
    public static FirebaseStorage getDocumentStorage() {
        return mDocumentStorage;
    }
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseAuth mFirebaseAuth;
    public static final String ANONYMOUS = "anonymous";
    private static final int RC_SIGN_IN = 5;
    private String mUserName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseAuth = FirebaseAuth.getInstance();

        mDocumentDatabase = FirebaseDatabase.getInstance();
        mDocumentStorage = FirebaseStorage.getInstance();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    mUserName = user.getDisplayName();
                    Toast.makeText(MainActivity.this, "Sign in Successful", Toast.LENGTH_SHORT).show();
                } else {
                    mUserName = ANONYMOUS;
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

        /*
        * Launch with UploadFragment
        * */
        loadFragment(new MyDocumentsFragment());
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.my_doc_page);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = null;
                switch (item.getItemId()){
                    case R.id.favourites_page:
                        fragment = new FavouritesFragment();
                        getDocumentAdapter().clear();
                        break;

                    case R.id.upload_page:
                        fragment = new UploadFragment();
                        getDocumentAdapter().clear();
                        break;

                    case R.id.my_doc_page:
                        fragment = new MyDocumentsFragment();
                        break;
                }
                return loadFragment(fragment);
            }
        });

    }

    private boolean loadFragment(Fragment fragment){
        if (fragment != null){
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        else
            return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                String text = "Signed in as " + mUserName;
                Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
            }
            else if(resultCode == RESULT_CANCELED)
            {
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
    protected void onStop() {
        super.onStop();
        detachDatabaseReadListener();
        getDocumentAdapter().clear();
        if (mAuthStateListener != null){
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
        mAuthStateListener = null;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuthStateListener != null){
            mFirebaseAuth.addAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.sign_out) {
            detachDatabaseReadListener();
            getDocumentAdapter().clear();
            sign_out();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sign_out(){
        AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(MainActivity.this, "Signed out successfully", Toast.LENGTH_SHORT).show();
                MainActivity.this.finish();
            }
        });
    }
}