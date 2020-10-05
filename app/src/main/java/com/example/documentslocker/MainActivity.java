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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Arrays;

import static com.example.documentslocker.MyDocumentsFragment.detachDatabaseReadListener;

public class MainActivity extends AppCompatActivity {

    private static DatabaseReference mDatabaseReference;
    private static StorageReference mStorageReference;
    public static DatabaseReference getDatabaseReference() {
        return mDatabaseReference;
    }
    public static StorageReference getStorageReference() {
        return mStorageReference;
    }
    public static final String ANONYMOUS = "anonymous";
    private static final int RC_SIGN_IN = 5;
    private String mUserName;

    public String getUserName() {
        return mUserName;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mFirebaseAuth.getCurrentUser();

        if (user != null) {
            mUserName = user.getDisplayName();
            Toast.makeText(this, "Sign in Successful", Toast.LENGTH_SHORT).show();
        } else {
            mUserName = ANONYMOUS;
            startActivityForResult(AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(Arrays.asList(
                                    new AuthUI.IdpConfig.EmailBuilder().build(),
                                    new AuthUI.IdpConfig.GoogleBuilder().build()
                            ))
                            .setTheme(R.style.AppTheme)
                            .setIsSmartLockEnabled(false)
                            .build(),
                    RC_SIGN_IN);
        }

        FirebaseDatabase mDocumentDatabase = FirebaseDatabase.getInstance();
        FirebaseStorage mFirebaseStorage = FirebaseStorage.getInstance();

        mDatabaseReference = mDocumentDatabase.getReference().child("User");
        mStorageReference = mFirebaseStorage.getReference().child("Upload");

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
                        detachDatabaseReadListener();
                        break;
                    case R.id.upload_page:
                        fragment = new UploadFragment();
                        detachDatabaseReadListener();
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
                Toast.makeText(this, "Sign in successful! ", Toast.LENGTH_SHORT).show();
            } else {
                if (response != null && response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    Toast.makeText(this, "Unknown Error Occurred!", Toast.LENGTH_SHORT).show();
                    finish();
                }
                if (response != null && response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }

    }
    @Override
    protected void onStop() {
        super.onStop();
        detachDatabaseReadListener();
        AuthUI.getInstance().signOut(this);
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
            AuthUI.getInstance().signOut(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}