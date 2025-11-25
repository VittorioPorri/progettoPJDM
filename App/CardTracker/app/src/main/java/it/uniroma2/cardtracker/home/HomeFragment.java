package it.uniroma2.cardtracker.home;

import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import it.uniroma2.cardtracker.R;
import it.uniroma2.cardtracker.entity.ListDecks;
import it.uniroma2.cardtracker.error.ServerOfflineActivity;

public class HomeFragment extends Fragment {

    private HomeViewModel viewModel;
    private RecyclerView recyclerView;
    private SearchView searchView;
    private Spinner spinnerFormats;
    private Button buttonMyDecks;
    private DeckAdapter adapter;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        searchView = view.findViewById(R.id.searchViewDecks);
        spinnerFormats = view.findViewById(R.id.spinnerFormats);
        recyclerView = view.findViewById(R.id.recyclerViewDecks);
        buttonMyDecks = view.findViewById(R.id.button);
        searchView.setIconifiedByDefault(false);
        searchView.clearFocus();



        buttonMyDecks.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(this);
            try {
                navController.navigate(R.id.action_homeFragment_to_myDeckFragment);
            } catch (IllegalArgumentException e) {
                Toast.makeText(requireContext(), "Errore di navigazione: Azione non trovata.", Toast.LENGTH_LONG).show();
            }
        });


        adapter = new DeckAdapter(requireContext(),deck -> {
            NavController navController = NavHostFragment.findNavController(this);

            Bundle args = new Bundle();
            args.putSerializable("deck", deck);

            navController.navigate(R.id.action_homeFragment_to_deckFragment, args);
        });

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));


        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.deck_formats, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFormats.setAdapter(spinnerAdapter);

        spinnerFormats.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view1, int position, long id) {
                String formato = parent.getItemAtPosition(position).toString();
                String query = searchView.getQuery().toString();
                if(query.isEmpty()){
                    viewModel.DecksByFormato(formato);
                }else{
                    viewModel.getDecksByName(query.trim(), formato);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String formato = spinnerFormats.getSelectedItem() != null ? spinnerFormats.getSelectedItem().toString() : "";
                viewModel.getDecksByName(query.trim(), formato);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText.isEmpty()){
                    searchView.clearFocus();

                    String formato = spinnerFormats.getSelectedItem() != null ? spinnerFormats.getSelectedItem().toString() : "";
                    viewModel.DecksByFormato(formato);
                }
                return false;
            }
        });

        observeViewModel();

        return view;
    }

    private void observeViewModel() {
        viewModel.getDecks().observe(getViewLifecycleOwner(), decks -> {
            adapter.setDecks(decks);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                viewModel.clearErrorMessage();
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
