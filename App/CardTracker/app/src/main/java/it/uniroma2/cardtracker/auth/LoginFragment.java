package it.uniroma2.cardtracker.auth;

import static android.content.Context.MODE_PRIVATE;

import androidx.lifecycle.ViewModelProvider;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import it.uniroma2.cardtracker.R;
import it.uniroma2.cardtracker.error.ServerOfflineActivity;
import it.uniroma2.cardtracker.home.HomeActivity;

public class LoginFragment extends Fragment {

    private AuthViewModel viewModel;

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private CheckBox remember;
    private TextView registerTextView;

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        emailEditText = view.findViewById(R.id.emailEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        loginButton = view.findViewById(R.id.loginButton);
        registerTextView = view.findViewById(R.id.goToRegisterText);
        remember = view.findViewById(R.id.remember);


        loginButton.setOnClickListener(v -> handleLogin());
        registerTextView.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(this);
            try {
                navController.navigate(R.id.action_loginFragment_to_registerFragment);
            } catch (IllegalArgumentException e) {
                Toast.makeText(requireContext(), "Errore di navigazione: Azione Register non trovata.", Toast.LENGTH_LONG).show();
            }
        });

        observeViewModel();

        return view;
    }

    private void handleLogin() {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        viewModel.login(email, password);
    }

    private void observeViewModel() {
        //Token login
        viewModel.getToken().observe(getViewLifecycleOwner(),token -> {
            if(token !=null){

                SharedPreferences authPrefs = requireActivity().getSharedPreferences("auth_prefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = authPrefs.edit();

                editor.putString("token", token);
                editor.putBoolean("remember_me", remember.isChecked());

                editor.apply();
            }
        });


        // Naviga verso la HomeActivity
        viewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                Toast.makeText(requireContext(), "Benvenuto " + user.getName() + "!", Toast.LENGTH_LONG).show();

                SharedPreferences authPrefs = requireActivity().getSharedPreferences("auth_prefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = authPrefs.edit();

                editor.putString("email", user.getEmail());
                editor.apply();

                Intent intent = new Intent(requireActivity(), HomeActivity.class);

                // Chiudo tutte le Activity precedenti e avviano la HomeActivity
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

                requireActivity().finish();

            }
        });

        //Eventuali errori
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                viewModel.getErrorMessage().setValue(null);
            }
        });

        viewModel.getOffline().observe(getViewLifecycleOwner(), isOffline -> {
            if (isOffline != null && isOffline) {

                Intent intent = new Intent(requireActivity(), ServerOfflineActivity.class);
                startActivity(intent);

                requireActivity().finish();
            }
        });

    }

}