package it.uniroma2.cardtracker.comment;

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
import it.uniroma2.cardtracker.deck.CardAdapter;
import it.uniroma2.cardtracker.entity.Card;
import it.uniroma2.cardtracker.entity.Comment;
import it.uniroma2.cardtracker.entity.Deck;
import it.uniroma2.cardtracker.entity.ListCards;
import it.uniroma2.cardtracker.entity.ListComments;
import it.uniroma2.cardtracker.entity.ListDecks;
import it.uniroma2.cardtracker.home.DeckAdapter;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.MyViewHolder> {

    private final Context context;
    private ListComments listComments;
    private final OnCommentClickListener listener;
    private int selectedPosition = -1; //Traccia il commento selezionato

    //Interfaccia per gestire click e delete
    public interface OnCommentClickListener {
        void onDeleteClick(int idDeck, int idComment);
        void onCommentClick(int position);

    }


    // Costruttore
    public CommentAdapter(Context context, OnCommentClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.listComments = new ListComments();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setComments(ListComments comments) {
        clear();
        this.listComments = comments;
        this.selectedPosition = -1;
        notifyDataSetChanged();
    }

    public void clear() {
        this.listComments = new ListComments();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setSelectedPosition(int position) {
        if (this.selectedPosition == position) {
            this.selectedPosition = -1;
        } else {
            this.selectedPosition = position;
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new MyViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Comment comment = listComments.get(position);
        holder.commentEmail.setText(comment.getEmail());
        holder.commentText.setText(comment.getText());

        if (position == selectedPosition) {
            holder.buttonDelete.setVisibility(View.VISIBLE);
        } else {
            holder.buttonDelete.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCommentClick(position);
            }
        });

        holder.buttonDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(comment.getIdDeck(), comment.getIdComment());
                setSelectedPosition(-1);
            }
        });

    }

    @Override
    public int getItemCount() {
        return listComments != null ? listComments.size() : 0;
    }
    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView commentEmail;
        TextView commentText;
        ImageView buttonDelete;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            commentEmail = itemView.findViewById(R.id.textUserName);
            commentText = itemView.findViewById(R.id.textContent);
            buttonDelete = itemView.findViewById(R.id.buttonDeleteComment);

        }
    }
}
