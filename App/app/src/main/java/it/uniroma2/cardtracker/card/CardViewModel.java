package it.uniroma2.cardtracker.card;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import it.uniroma2.cardtracker.R;
import it.uniroma2.cardtracker.entity.ListCards;

public class CardViewModel extends AndroidViewModel {


    private static final String TAG = "CardViewModel";

    private final String URL;
    private final RequestQueue requestQueue;

    private final MutableLiveData<ListCards> cards;
    private final MutableLiveData<String> errorMessage;

    private MutableLiveData<Boolean> add;
    private MutableLiveData<Boolean> delete;

    public CardViewModel(@NonNull Application application) {
        super(application);
        requestQueue = Volley.newRequestQueue(application);
        cards = new MutableLiveData<>();
        errorMessage = new MutableLiveData<>();
        add = new MutableLiveData<>();
        delete = new MutableLiveData<>();

        URL = application.getString(R.string.url);
    }

    public MutableLiveData<ListCards> getCards() { return cards; }

    public MutableLiveData<String> getErrorMessage() { return errorMessage; }
    public void clearErrorMessage() { errorMessage.setValue(null);}
    public  MutableLiveData<Boolean> getAdd() { return add; }
    public  MutableLiveData<Boolean> getDelete() { return delete; }
    public void resetAddStatus() { add.setValue(false); }
    public void resetDeleteStatus() { delete.setValue(false); }

    public void getCard(int deckId) {
        String cardForDeckURL = URL + "card?idDeck=" + deckId;

        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                cardForDeckURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Risposta cardForDeck (Successo): " + response);
                        parseResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "onErrorResponse DecksByFormato: " +
                                (error.getMessage() != null ? error.getMessage() : "Errore Sconosciuto"));
                        if (error.networkResponse != null) {
                            String errorBody = new String(error.networkResponse.data);
                            Log.e(TAG, "Dati errore DecksByFormato: " + errorBody);
                            errorMessage.setValue(parseError(errorBody));
                        } else {
                            errorMessage.setValue("Errore di rete o server irraggiungibile.");
                        }
                    }
                });

        requestQueue.add(stringRequest);

    }

    public void getCardByFormato(String formato) {
        String cardByFormatoURL = URL + "card?formato=" + formato;

        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                cardByFormatoURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Risposta cardForDeck (Successo): " + response);
                        parseResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "onErrorResponse DecksByFormato: " +
                                (error.getMessage() != null ? error.getMessage() : "Errore Sconosciuto"));
                        if (error.networkResponse != null) {
                            String errorBody = new String(error.networkResponse.data);
                            Log.e(TAG, "Dati errore DecksByFormato: " + errorBody);
                            errorMessage.setValue(parseError(errorBody));
                        } else {
                            errorMessage.setValue("Errore di rete o server irraggiungibile.");
                        }
                    }
                });

        requestQueue.add(stringRequest);
    }

    public void getCardByFormatoAndName(String formato, String name) {
        String cardByFormatoAndNamekURL = URL + "card?formato=" + formato + "&name=" + name;

        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                cardByFormatoAndNamekURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Risposta cardForDeck (Successo): " + response);
                        parseResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "onErrorResponse DecksByFormato: " +
                                (error.getMessage() != null ? error.getMessage() : "Errore Sconosciuto"));
                        if (error.networkResponse != null) {
                            String errorBody = new String(error.networkResponse.data);
                            Log.e(TAG, "Dati errore DecksByFormato: " + errorBody);
                            errorMessage.setValue(parseError(errorBody));
                        } else {
                            errorMessage.setValue("Errore di rete o server irraggiungibile.");
                        }
                    }
                });

        requestQueue.add(stringRequest);
    }

    public void addCard(int idDeck, int idCard, String token) {
        String addCardURL = URL + "card";

        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                addCardURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "addCard - Risposta dal server: " + response);
                        add.setValue(true);
                        getCard(idDeck);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "addCard - Errore: " +
                                (error.getMessage() != null ? error.getMessage() : "Errore sconosciuto"));
                        if (error.networkResponse != null) {
                            String errorBody = new String(error.networkResponse.data);
                            Log.e(TAG, "Corpo errore addCard: " + errorBody);
                            errorMessage.setValue(parseError(errorBody));
                        } else {
                            errorMessage.setValue("Errore di rete o server irraggiungibile.");
                        }
                    }
                }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", token.trim());
                headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("idCard", String.valueOf(idCard).trim());
                params.put("idDeck", String.valueOf(idDeck).trim());
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }

    public void deleteCard(int idCard, int idDeck, String token) {
        String deleteCardURL = URL + "card?idDeck=" + idDeck + "&idCard=" + idCard;

        StringRequest stringRequest = new StringRequest(
                Request.Method.DELETE,
                deleteCardURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "addCard - Risposta dal server: " + response);
                        delete.setValue(true);
                        getCard(idDeck);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "addCard - Errore: " +
                                (error.getMessage() != null ? error.getMessage() : "Errore sconosciuto"));
                        if (error.networkResponse != null) {
                            String errorBody = new String(error.networkResponse.data);
                            Log.e(TAG, "Corpo errore addCard: " + errorBody);
                            errorMessage.setValue(parseError(errorBody));
                        } else {
                            errorMessage.setValue("Errore di rete o server irraggiungibile.");
                        }
                    }
                }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", token.trim());
                headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                return headers;
            }

        };

        requestQueue.add(stringRequest);

    }

    public void deleteDeck(String token ,int idDeck) {
        String deleteCardURL = URL + "deck?idDeck=" + idDeck ;

        StringRequest stringRequest = new StringRequest(
                Request.Method.DELETE,
                deleteCardURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "deleteDecks - Risposta dal server: " + response);
                        delete.setValue(true);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "addCard - Errore: " +
                                (error.getMessage() != null ? error.getMessage() : "Errore sconosciuto"));
                        if (error.networkResponse != null) {
                            String errorBody = new String(error.networkResponse.data);
                            Log.e(TAG, "Corpo errore addCard: " + errorBody);
                            errorMessage.setValue(parseError(errorBody));
                        } else {
                            errorMessage.setValue("Errore di rete o server irraggiungibile.");
                        }
                    }
                }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", token.trim());
                headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                return headers;
            }

        };

        requestQueue.add(stringRequest);

    }




    private void parseResponse(String response) {
        try {
            ListCards cards = ListCards.fromJSON(response);
            if (cards != null) {
                this.cards.setValue(cards);
            } else {
                errorMessage.setValue("Nessun mazzo trovato o risposta vuota.");
            }
        } catch (Exception e) {
            Log.e(TAG, "ParseResponse: " + e.getMessage(), e);
            errorMessage.setValue("Errore nel parsing della risposta del server.");
        }
    }

    private String parseError(String errorBody) {
        if (errorBody == null || errorBody.trim().isEmpty()) {
            return "Errore sconosciuto dal server.";
        }

        try {
            JSONObject jsonError = new JSONObject(errorBody);
            if (jsonError.has("error")) {
                return jsonError.getString("error");
            }
        } catch (JSONException e) {
            Log.w(TAG, "Corpo errore non in formato JSON: " + errorBody);
        }
        return "Errore di accesso al server.";
    }
}