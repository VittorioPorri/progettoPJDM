package it.uniroma2.cardtracker.mydeck;

import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import it.uniroma2.cardtracker.R;
import it.uniroma2.cardtracker.entity.Deck;
import it.uniroma2.cardtracker.home.DeckAdapter;

public class MyDeckFragment extends Fragment{

    private Deck deck;
    private MyActionViewModel viewModel;
    private Toolbar back;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private Button button;
    private DeckAdapter adapter;


    public static MyDeckFragment newInstance() {
        return new MyDeckFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MyActionViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_my_deck, container, false);

        back = view.findViewById(R.id.toolbar);
        recyclerView = view.findViewById(R.id.recyclerDeck);
        progressBar = view.findViewById(R.id.progressBar);
        button = view.findViewById(R.id.button);


        back.setNavigationOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigateUp();
        });

        button.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(this);
            try {
                Bundle args = new Bundle();
                args.putSerializable("deck", deck);
                navController.navigate(R.id.action_myDeckFragment_to_makeDeckFragment,args);
            } catch (IllegalArgumentException e) {
                Toast.makeText(requireContext(), "Errore di navigazione: Azione non trovata.", Toast.LENGTH_LONG).show();
            }
        });

        String token =requireContext().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE).getString("token", null);

        viewModel.myDecks(token);

        adapter = new DeckAdapter(requireContext(),deck -> {
            NavController navController = NavHostFragment.findNavController(this);

            Bundle args = new Bundle();
            args.putSerializable("deck", deck);

            navController.navigate(R.id.action_myDeckFragment_to_deckFragment2, args);
        });

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

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
                viewModel.clearError();
            }
        });
    }

}