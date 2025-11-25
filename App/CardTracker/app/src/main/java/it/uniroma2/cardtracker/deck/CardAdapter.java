package it.uniroma2.cardtracker.deck;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import it.uniroma2.cardtracker.R;
import it.uniroma2.cardtracker.entity.Card;
import it.uniroma2.cardtracker.entity.ListCards;
import it.uniroma2.cardtracker.entity.ListDecks;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.MyViewHolder> {

    private final Context context;
    private ListCards listCards;
    private final OnItemClickListener listener;

    // Interfaccia per gestire i click sugli item
    public interface OnItemClickListener {
        void onItemClick(Card card);
    }

    // Costruttore
    public CardAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.listCards = new ListCards();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setCards(ListCards cards) {
        this.listCards = cards;
        notifyDataSetChanged();
    }
    @SuppressLint("NotifyDataSetChanged")
    public void clear(){
        this.listCards = new ListCards();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = LayoutInflater.from(context).inflate(R.layout.item_card, parent, false);
        return new MyViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Card card = listCards.get(position);
        holder.cardTitle.setText(card.getName());
        holder.cardCount.setText("x" + card.getQuantita());

        //decodifica l'immagine da Base64 a Bitmap
        if (card.getImage() != null && !card.getImage().isEmpty()) {
            byte[] decodedBytes = Base64.decode(card.getImage(), Base64.DEFAULT);
            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            holder.cardImage.setImageBitmap(decodedBitmap);
        } else {
            holder.cardImage.setImageResource(R.drawable.placeholder);
        }

        // Click sull’item
        holder.itemView.setOnClickListener(v -> listener.onItemClick(card));
    }

    @Override
    public int getItemCount() {
        return listCards != null ? listCards.size() : 0;
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView cardImage;
        TextView cardTitle;
        TextView cardCount;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            cardImage = itemView.findViewById(R.id.cardImage);
            cardTitle = itemView.findViewById(R.id.cardTitle);
            cardCount = itemView.findViewById(R.id.cardCount);
        }
    }
}
