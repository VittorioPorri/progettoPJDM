package it.uniroma2.cardtracker.card;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import it.uniroma2.cardtracker.R;
import it.uniroma2.cardtracker.deck.CardAdapter;
import it.uniroma2.cardtracker.deck.CardImageFragment;
import it.uniroma2.cardtracker.entity.Card;
import it.uniroma2.cardtracker.entity.Deck;

public class AddCardFragment extends Fragment implements CardAdapter.OnItemClickListener{

    private Deck deck;
    private CardViewModel viewModel;
    private Toolbar back;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private SearchView searchView;
    private CardAdapter adapter;
    private boolean canClickItem = true;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CardViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_card, container, false);

        back = view.findViewById(R.id.toolbar);
        searchView = view.findViewById(R.id.searchViewDecks);
        searchView.setIconifiedByDefault(false);
        searchView.clearFocus();
        recyclerView = view.findViewById(R.id.recyclerViewDecks);
        progressBar = view.findViewById(R.id.progressBar);



        back.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(this);
            navController.popBackStack();
        });

        adapter = new CardAdapter(requireContext(),this);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        Bundle args = getArguments();
        if (args != null) {
            deck = (Deck) args.getSerializable("deck");

            if (deck != null) {
                adapter.clear();
                progressBar.setVisibility(View.VISIBLE);

                viewModel.getCardByFormato(deck.getFormato());
            }
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                viewModel.getCardByFormatoAndName(deck.getFormato(), query);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //quando premo X o cancelli tutto il testo
                if (newText.isEmpty()) {
                    searchView.clearFocus();

                    recyclerView.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);

                    if (deck != null) {
                        viewModel.getCardByFormato(deck.getFormato());
                    }
                }
                return false;
            }
        });

        observeViewModel();

        return view;
    }

    @Override
    public void onItemClick(Card card) {
        if (!canClickItem) {
            return;
        }

        canClickItem = false;

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            canClickItem = true;
        }, 1000);

        if (card.getImage() != null && !card.getImage().isEmpty()) {
            CardImageFragment dialog = CardImageFragment.newInstance(card.getImage(), card.getIdCard(),deck.getIdDeck(),deck.getEmail());
            dialog.show(getParentFragmentManager(), "CardImageDialog");
        } else {
            Toast.makeText(requireContext(), "Immagine non disponibile.", Toast.LENGTH_SHORT).show();
        }
    }

    private void observeViewModel() {
        viewModel.getCards().observe(getViewLifecycleOwner(), cards -> {
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            adapter.clear();
            if (cards != null && !cards.isEmpty()) {
                adapter.setCards(cards);
            } else {
                adapter.clear();
                Toast.makeText(requireContext(), "Nessuna carta trovata per questo mazzo", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            progressBar.setVisibility(View.GONE);

            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                viewModel.clearErrorMessage();
            }
        });

    }

}