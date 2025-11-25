package it.uniroma2.cardtracker.auth;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import it.uniroma2.cardtracker.R;
import it.uniroma2.cardtracker.error.ServerOfflineActivity;
import it.uniroma2.cardtracker.home.HomeActivity;

public class RegisterFragment extends Fragment {

    private AuthViewModel viewModel;
    private EditText emailEditText;
    private EditText nameEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private Button registerButton;
    private TextView loginTextView;

    public static RegisterFragment newInstance() {
        return new RegisterFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);
        nameEditText = view.findViewById(R.id.nameEditText);
        emailEditText = view.findViewById(R.id.emailEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        confirmPasswordEditText = view.findViewById(R.id.confirmPasswordEditText);
        registerButton = view.findViewById(R.id.registerButton);
        loginTextView = view.findViewById(R.id.goToLoginText);

        NavController navController = NavHostFragment.findNavController(this);

        registerButton.setOnClickListener(v -> handleRegister());
        loginTextView.setOnClickListener(v -> {
            try {
                navController.navigate(R.id.action_registerFragment_to_loginFragment);
            } catch (IllegalArgumentException e) {
                Toast.makeText(requireContext(), "Errore di navigazione: Azione Register non trovata.", Toast.LENGTH_LONG).show();
            }
        });

        observeViewModel(navController);

        return view;

    }

    private void handleRegister() {
        String name = nameEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();

        if (!password.equals(confirmPassword)) {
            Toast.makeText(requireContext(), "Le password non corrispondono.", Toast.LENGTH_SHORT).show();
            return;
        }

        viewModel.register(email, password, name);

    }

    private void observeViewModel(NavController navController) {
        viewModel.getRegistration().observe(getViewLifecycleOwner(), isSuccess -> {
            if (isSuccess != null) {
                if (isSuccess) {
                    Toast.makeText(requireContext(), "Registrazione completata! Accedi ora.", Toast.LENGTH_LONG).show();

                    viewModel.getRegistration().setValue(null);

                    try {
                        navController.navigate(R.id.action_registerFragment_to_loginFragment);
                    } catch (IllegalArgumentException e) {
                        navController.popBackStack();
                    }
                } else {
                    Toast.makeText(requireContext(), "Registrazione fallita. Controlla i dati.", Toast.LENGTH_SHORT).show();
                    viewModel.getRegistration().setValue(null);
                }
            }
        });

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