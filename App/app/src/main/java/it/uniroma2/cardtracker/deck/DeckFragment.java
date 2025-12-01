package it.uniroma2.cardtracker.deck;

import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import it.uniroma2.cardtracker.R;
import it.uniroma2.cardtracker.card.CardViewModel;
import it.uniroma2.cardtracker.entity.Card;
import it.uniroma2.cardtracker.entity.Deck;

public class DeckFragment extends Fragment implements CardAdapter.OnItemClickListener{

    private Deck deck;
    private CardViewModel viewModel;
    private TextView name;
    private Toolbar back;
    private ImageView menuButton;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private  TextView comment;
    private CardAdapter adapter;

    private boolean canClickItem = true;


    public static DeckFragment newInstance() {
        return new DeckFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(CardViewModel.class);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_deck, container, false);

        name = view.findViewById(R.id.toolbarTitle);
        back = view.findViewById(R.id.toolbar);
        menuButton = view.findViewById(R.id.menuButton);
        recyclerView = view.findViewById(R.id.recyclerDeck);
        progressBar = view.findViewById(R.id.progressBar);
        comment = view.findViewById(R.id.commentText);

        adapter = new CardAdapter(requireContext(),this);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));


        Bundle args = getArguments();
        if (args != null) {
            deck = (Deck) args.getSerializable("deck");

            if (deck != null) {
                name.setText(deck.getName());
                adapter.clear();
                progressBar.setVisibility(View.VISIBLE);

                viewModel.getCard(deck.getIdDeck());
            }
        }

        SharedPreferences prefs = requireContext().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);
        String myEmail = prefs.getString("email", null);

        if (myEmail != null && !myEmail.equals(deck.getEmail())) {
            menuButton.setVisibility(View.GONE);
        }else{
            menuButton.setOnClickListener(v -> {
                androidx.appcompat.widget.PopupMenu popupMenu = new androidx.appcompat.widget.PopupMenu(requireContext(), v);
                popupMenu.getMenuInflater().inflate(R.menu.deck_menu, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(item -> {
                    int itemId = item.getItemId();

                    if (itemId == R.id.action_add_card) {
                        NavController navController = NavHostFragment.findNavController(this);
                        Bundle args2 = new Bundle();
                        args2.putSerializable("deck", deck);
                        navController.navigate(R.id.action_deckFragment_to_addCardFragment, args);
                        return true;
                    } else if (itemId == R.id.action_delete_deck) {
                        Toast.makeText(requireContext(), "Mazzo eliminato con successo", Toast.LENGTH_SHORT).show();
                        viewModel.deleteDeck(token, deck.getIdDeck());

                        NavController navController = NavHostFragment.findNavController(this);
                        navController.popBackStack();
                        return true;
                    } else {
                        return false;
                    }
                });

                popupMenu.show();
            });

        }

        back.setNavigationOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(this);
            navController.popBackStack();
        });

        comment.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(this);
            try {
                Bundle args2 = new Bundle();
                args2.putSerializable("deck", deck);
                navController.navigate(R.id.action_deckFragment_to_commentFragment,args);
            } catch (IllegalArgumentException e) {
                Toast.makeText(requireContext(), "Errore di navigazione: Azione non trovata.", Toast.LENGTH_LONG).show();
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

    @Override
    public void onResume() {
        super.onResume();
        adapter.clear();
        progressBar.setVisibility(View.VISIBLE);
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