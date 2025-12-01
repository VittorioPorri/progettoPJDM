package it.uniroma2.cardtracker.mydeck;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

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
import it.uniroma2.cardtracker.entity.Deck;
import it.uniroma2.cardtracker.entity.ListDecks;

public class MyActionViewModel extends AndroidViewModel {
    private static final String TAG = "MyActionViewModel";
    private final String URL;
    private final RequestQueue requestQueue;
    private final MutableLiveData<ListDecks> decks;
    private final MutableLiveData<String> errorMessage;
    private MutableLiveData<Deck> add;
    private MutableLiveData<Boolean> delete;

    public MyActionViewModel(@NonNull Application application) {
        super(application);
        requestQueue = Volley.newRequestQueue(application);
        decks = new MutableLiveData<>();
        errorMessage = new MutableLiveData<>();
        add = new MutableLiveData<>();
        delete = new MutableLiveData<>();

        URL = application.getString(R.string.url);
    }

    public MutableLiveData<ListDecks> getDecks() { return decks; }

    public MutableLiveData<String> getErrorMessage() { return errorMessage; }

    public void clearError() {errorMessage.setValue(null);}

    public  MutableLiveData<Deck> getAdd() { return add; }

    public  MutableLiveData<Boolean> getDelete() { return delete; }

    public void myDecks(String token) {
        String decksMyURL = URL + "deck?my";
        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                decksMyURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Risposta decksMyURL (Successo): " + response);
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
                }){

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

    public void addDeck(String token, String name, String formato) {
        String addDeckURL = URL + "deck";
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                addDeckURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "addDeck - Risposta dal server: " + response);
                        try {
                            Deck newDeck = Deck.fromJSON(response);
                            if (newDeck != null) {
                                add.setValue(newDeck);
                            } else {
                                errorMessage.setValue("Errore nel parsing del mazzo creato (risposta vuota).");
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "addDeck - JSON Errore: " + e.getMessage(), e);
                            errorMessage.setValue("Errore nel parsing della risposta del server.");
                        }
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
                params.put("name", String.valueOf(name).trim());
                params.put("formato", String.valueOf(formato).trim());
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }


    private void parseResponse(String response) {
        try {
            ListDecks decks = ListDecks.fromJSON(response);
            if (decks != null) {
                this.decks.setValue(decks);
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