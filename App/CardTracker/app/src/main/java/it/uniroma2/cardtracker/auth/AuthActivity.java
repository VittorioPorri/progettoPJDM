package it.uniroma2.cardtracker.auth;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import it.uniroma2.cardtracker.R;
import it.uniroma2.cardtracker.error.ServerOfflineActivity;
import it.uniroma2.cardtracker.home.HomeActivity;


public class AuthActivity extends AppCompatActivity {
    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        observeViewModel();
        checkAutoLogin();
    }

    private void checkAutoLogin() {
        SharedPreferences sharedPref = getSharedPreferences("auth_prefs", MODE_PRIVATE);
        String token = sharedPref.getString("token", null);
        boolean rememberMe = sharedPref.getBoolean("remember_me", false);

        if (token != null && !token.isEmpty() && rememberMe) {
            Toast.makeText(this, "Accesso automatico in corso...", Toast.LENGTH_SHORT).show();
            viewModel.login(token);
        } else if(!rememberMe){
            setContentView(R.layout.activity_auth);
        }
    }

    private void observeViewModel() {
        viewModel.getUser().observe(this, user -> {
            if (user != null) {
                Toast.makeText(this, "Benvenuto " + user.getName() + "!", Toast.LENGTH_LONG).show();

                // Naviga verso la HomeActivity
                Intent intent = new Intent(AuthActivity.this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        // Observer per la gestione degli errori
        viewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                viewModel.getErrorMessage().setValue(null);
            }
        });

        viewModel.getOffline().observe(this, isOffline -> {
            if (isOffline != null && isOffline) {
                Intent intent = new Intent(AuthActivity.this, ServerOfflineActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    private void removeTokenIfAutoLoginFailed() {
        SharedPreferences sharedPref = getSharedPreferences("auth_prefs", MODE_PRIVATE);
        sharedPref.edit().remove("token").apply();
    }
}