package it.uniroma2.cardtracker.mydeck;

import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;

import it.uniroma2.cardtracker.R;

public class MakeDeckFragment extends Fragment {

    private Toolbar back;
    private TextInputEditText editTextName;
    private Spinner spinnerItems;
    private Button button;
    private MyActionViewModel viewModel;

    public static MakeDeckFragment newInstance() {
        return new MakeDeckFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MyActionViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_make_deck, container, false);

        back = view.findViewById(R.id.toolbar);
        editTextName = view.findViewById(R.id.editTextName);
        spinnerItems = view.findViewById(R.id.spinnerItems);
        button = view.findViewById(R.id.button2);
        String token  = requireContext().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE).getString("token", null);

        back.setNavigationOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigateUp();
        });

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.deck_formats, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerItems.setAdapter(spinnerAdapter);


        button.setOnClickListener(v -> {
            String deckName = editTextName.getText() != null ? editTextName.getText().toString().trim() : "";
            String format = (String) spinnerItems.getSelectedItem();

            if (token == null) {
                Toast.makeText(requireContext(), "Errore di autenticazione: token non trovato.", Toast.LENGTH_LONG).show();
                return;
            }

            if (deckName.isEmpty()) {
                editTextName.setError("Il nome del mazzo non può essere vuoto.");
                return;
            }

            viewModel.addDeck(token, deckName, format);
        });

        observeViewModel();

        return  view;
    }

    private void observeViewModel() {
        viewModel.getAdd().observe(getViewLifecycleOwner(), addedDeck -> {
            if (addedDeck != null) {

                Toast.makeText(requireContext(), "Mazzo '" + addedDeck.getName() + "' creato!", Toast.LENGTH_SHORT).show();

                NavController navController = NavHostFragment.findNavController(this);

                Bundle args = new Bundle();
                args.putSerializable("deck", addedDeck);


                navController.navigate(R.id.action_makeDeckFragment_to_deckFragment, args);

                viewModel.getAdd().setValue(null);
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(requireContext(), "Errore creazione mazzo: " + errorMessage, Toast.LENGTH_LONG).show();

                viewModel.getErrorMessage().setValue(null);
            }
        });
    }

}