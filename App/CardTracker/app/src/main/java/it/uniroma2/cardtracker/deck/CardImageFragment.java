package it.uniroma2.cardtracker.deck;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import it.uniroma2.cardtracker.R;
import it.uniroma2.cardtracker.card.CardViewModel;

public class CardImageFragment extends DialogFragment {

    private static final String ARG_IMAGE_BASE64 = "image_base64";

    private CardViewModel viewModel;
    private Button deleteButton;
    private Button addButton;

    public static CardImageFragment newInstance(String imageBase64, int idCard, int idDeck, String email) {
        CardImageFragment fragment = new CardImageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_IMAGE_BASE64, imageBase64);
        args.putInt("idCard", idCard);
        args.putInt("idDeck", idDeck);
        args.putString("email", email);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(CardViewModel.class);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Theme_CardTracker_DialogFullscreen);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_card_image, container, false);

        deleteButton = view.findViewById(R.id.redButton);
        addButton = view.findViewById(R.id.greenButton);
        ImageView imageView = view.findViewById(R.id.fullscreenImageView);

        SharedPreferences prefs = requireContext().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);
        String myEmail = prefs.getString("email", null);


        int idCard = getArguments().getInt("idCard");
        int idDeck = getArguments().getInt("idDeck");
        String email = getArguments().getString("email");


        if (myEmail != null && !myEmail.equals(email)) {
            deleteButton.setVisibility(View.GONE);
            addButton.setVisibility(View.GONE);
        }


        deleteButton.setOnClickListener(v -> {
            setButtonsEnabled(false);
            viewModel.deleteCard(idCard, idDeck, token);

        });

        addButton.setOnClickListener(v -> {
            setButtonsEnabled(false);
            viewModel.addCard(idDeck, idCard, token);

        });

        // Decodifica immagine
        if (getArguments() != null) {
            String imageBase64 = getArguments().getString(ARG_IMAGE_BASE64);
            if (imageBase64 != null && !imageBase64.isEmpty()) {
                byte[] decodedBytes = Base64.decode(imageBase64, Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                imageView.setImageBitmap(decodedBitmap);
            } else {
                imageView.setImageResource(R.drawable.placeholder);
            }
        }


        imageView.setOnClickListener(v -> dismiss());

        observeViewModel();

        return view;
    }

    private void observeViewModel() {
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                viewModel.clearErrorMessage();
            }
        });

        viewModel.getAdd().observe(getViewLifecycleOwner(), isAdded -> {
            if (Boolean.TRUE.equals(isAdded)) {
                Toast.makeText(requireContext(), "Carta aggiunta con successo!", Toast.LENGTH_SHORT).show();
                viewModel.resetAddStatus();
            }
        });


        viewModel.getDelete().observe(getViewLifecycleOwner(), isDeleted -> {
            if (Boolean.TRUE.equals(isDeleted)) {
                Toast.makeText(requireContext(), "Carta rimossa con successo!", Toast.LENGTH_SHORT).show();
                viewModel.resetDeleteStatus();
            }
        });
    }

    private void setButtonsEnabled(boolean enabled) {
        deleteButton.setEnabled(false);
        addButton.setEnabled(false);
        deleteButton.setAlpha(0.5f);
        addButton.setAlpha(0.5f);

        //Creo un Handler che si aggancia al main Thread per gestire la postDelayed
        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            // Controllo se il fragment esiste ancora
            if (isAdded() && getView() != null) {
                deleteButton.setEnabled(true);
                addButton.setEnabled(true);
                deleteButton.setAlpha(1.0f);
                addButton.setAlpha(1.0f);
            }

        }, 3000);
    }
}
