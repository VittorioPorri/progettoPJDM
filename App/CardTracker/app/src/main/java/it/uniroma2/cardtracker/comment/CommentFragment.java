package it.uniroma2.cardtracker.comment;

import androidx.appcompat.widget.Toolbar;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import it.uniroma2.cardtracker.R;
import it.uniroma2.cardtracker.entity.Deck;

public class CommentFragment extends Fragment implements CommentAdapter.OnCommentClickListener{

    private CommentViewModel viewModel;

    private Deck deck;
    private Toolbar toolbar;
    private TextView toolbarTitle;
    private RecyclerView recyclerDeck;
    private ProgressBar progressBar;
    private EditText editComment;
    private ImageButton buttonSendComment;
    private CommentAdapter adapter;

    public static CommentFragment newInstance() {
        return new CommentFragment();
    }

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CommentViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_comment, container, false);

        toolbar = view.findViewById(R.id.toolbar);
        toolbarTitle = view.findViewById(R.id.toolbarTitle);
        recyclerDeck = view.findViewById(R.id.recyclerDeck);
        progressBar = view.findViewById(R.id.progressBar);
        editComment = view.findViewById(R.id.editComment);
        buttonSendComment = view.findViewById(R.id.buttonSendComment);

        Bundle args = getArguments();
        if (args != null){
            deck = (Deck) args.getSerializable("deck");

            if (deck != null) {
                toolbarTitle.setText(deck.getName());
                progressBar.setVisibility(View.VISIBLE);
                viewModel.getComment(deck.getIdDeck());
            }
        }

        toolbar.setNavigationOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigateUp();
        });

        adapter = new CommentAdapter(requireContext(), this);
        recyclerDeck.setAdapter(adapter);
        recyclerDeck.setLayoutManager(new LinearLayoutManager(requireContext()));


        buttonSendComment.setOnClickListener(v -> {
            String text = editComment.getText().toString().trim();
            if (text.isEmpty()) {
                Toast.makeText(requireContext(), "Inserisci un commento", Toast.LENGTH_SHORT).show();
                return;
            }
            buttonSendComment.setEnabled(false);
            editComment.setText("");

            String token = requireContext()
                    .getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                    .getString("token", null);

            if (token == null) {
                Toast.makeText(requireContext(), "Errore: utente non autenticato", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            viewModel.addComment(deck.getIdDeck(), text, token);
        });

        observeViewModel();
        return view;
    }

    @Override
    public void onCommentClick(int position) {
        adapter.setSelectedPosition(position);
    }
    @Override
    public void onDeleteClick(int idDeck, int idComment) {
        String token = requireContext()
                .getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                .getString("token", null);

        if (token == null) {
            Toast.makeText(requireContext(), "Errore: utente non autenticato", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        viewModel.deleteComment(idDeck, idComment, token);
    }

    private void observeViewModel() {
        viewModel.getComments().observe(getViewLifecycleOwner(), list -> {
            progressBar.setVisibility(View.GONE);
            adapter.setComments(list);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            progressBar.setVisibility(View.GONE);
            buttonSendComment.setEnabled(true);
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getAdd().observe(getViewLifecycleOwner(), added -> {
            buttonSendComment.setEnabled(true);
            if (Boolean.TRUE.equals(added)) {
                editComment.setText(""); // svuota campo
                Toast.makeText(requireContext(), "Commento aggiunto", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getDelete().observe(getViewLifecycleOwner(), deleted -> {
            if (Boolean.TRUE.equals(deleted)) {
                Toast.makeText(requireContext(), "Commento eliminato", Toast.LENGTH_SHORT).show();
            }
        });
    }

}