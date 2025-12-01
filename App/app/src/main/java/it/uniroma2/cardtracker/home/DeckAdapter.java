package it.uniroma2.cardtracker.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.helper.widget.Layer;
import androidx.recyclerview.widget.RecyclerView;

import it.uniroma2.cardtracker.R;
import it.uniroma2.cardtracker.entity.Deck;
import it.uniroma2.cardtracker.entity.ListDecks;

public class DeckAdapter extends RecyclerView.Adapter<DeckAdapter.MyViewHolder> {
    Context context;
    ListDecks listDecks;

    private final OnDeckClickListener listener;
    public interface OnDeckClickListener {
        void onDeckClick(Deck deck);
    }

    public DeckAdapter(Context context, OnDeckClickListener listener){
        this.context=context;
        this.listener = listener;
        this.listDecks=null;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setDecks(ListDecks decks) {
        clear();
        this.listDecks = decks;
        notifyDataSetChanged();
    }

    public void clear() {
        this.listDecks = new ListDecks();
    }

    @NonNull
    @Override
    public DeckAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Inlate del layout dell'item deck
        LayoutInflater inflater = LayoutInflater.from(context);
        View view  =inflater.inflate(R.layout.item_deck,parent,false);
        return new DeckAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeckAdapter.MyViewHolder holder, int position) {
        //Asseganre i valori alle view
        if(listDecks == null || listDecks.get(position) == null){
            holder.name.setText("");
            return;
        }

        Deck deck = listDecks.get(position);

        if (holder.image != null) {
            switch (deck.getFormato()){
                case "yugioh":
                    holder.image.setImageResource(R.drawable.ic_deckbox_blu);
                    break;
                case "magic":
                    holder.image.setImageResource(R.drawable.ic_deckbox_rosso);
                    break;
                case "pokemon":
                    holder.image.setImageResource(R.drawable.ic_deckbox_giallo);
                    break;
                default:
                    holder.image.setImageResource(R.drawable.ic_deckbox);
                    break;
            }
        }

        holder.name.setText(deck.getName());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onDeckClick(deck);
        });
    }

    @Override
    public int getItemCount() {
        return listDecks == null ? 0 : listDecks.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        //Assegna i valori agli item
        ImageView image;
        TextView name;
        @SuppressLint("ResourceType")
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.textName);
            image = itemView.findViewById(R.id.imageDeckBox);
        }
    }
}
