package it.uniroma2.cardtracker.home;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import it.uniroma2.cardtracker.R;
import it.uniroma2.cardtracker.entity.ListDecks;

public class HomeViewModel extends AndroidViewModel {

    private static final String TAG = "HomeViewModel";
    private final String URL;
    private final RequestQueue requestQueue;
    private final MutableLiveData<ListDecks> decks;
    private final MutableLiveData<String> errorMessage;
    private final MutableLiveData<Boolean>  offline;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        requestQueue = Volley.newRequestQueue(application);
        decks = new MutableLiveData<>();
        errorMessage = new MutableLiveData<>();
        offline = new MutableLiveData<>(false);

        URL = application.getString(R.string.url);
    }

    public MutableLiveData<ListDecks> getDecks() { return decks; }

    public MutableLiveData<String> getErrorMessage() { return errorMessage; }
    public void clearErrorMessage() { errorMessage.setValue(null);}
    public MutableLiveData<Boolean> getOffline() { return offline; }

    public void DecksByFormato(String formato) {
        String decksByFormatoURL = URL + "deck?formato=" + formato;

        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                decksByFormatoURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Risposta DecksByFormato (Successo): " + response);
                        parseResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "onErrorResponse DecksByFormato: " + (error.getMessage() != null ? error.getMessage() : "Errore Sconosciuto"));
                        if (error.networkResponse != null) {
                            if (error.networkResponse.statusCode == 500) {
                                Log.e(TAG, "Errore 500 rilevato: Server Down");
                                errorMessage.setValue("Server  irraggiungibile");
                                offline.setValue(true);
                            }
                            else {
                                String errorBody = new String(error.networkResponse.data);
                                errorMessage.setValue(parseError(errorBody));
                            }

                        } else {
                            errorMessage.setValue("Problemi di connessione");
                            offline.setValue(true);
                        }
                    }
                });

        requestQueue.add(stringRequest);
    }

    public void getDecksByName(String name, String formato) {
        String decksByNameURL = URL + "deck?name=" + name + "&formato=" + formato;


        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                decksByNameURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Risposta DecksByName (Successo): " + response);
                        parseResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "onErrorResponse DecksByName: " + (error.getMessage() != null ? error.getMessage() : "Errore Sconosciuto"));
                        if (error.networkResponse != null) {
                            if (error.networkResponse.statusCode == 500) {
                                Log.e(TAG, "Errore 500 rilevato: Server Down");
                                errorMessage.setValue("Server  irraggiungibile");
                                offline.setValue(true);
                            }
                            else {
                                String errorBody = new String(error.networkResponse.data);
                                errorMessage.setValue(parseError(errorBody));
                            }

                        } else {
                            errorMessage.setValue("Problemi di connessione");
                            offline.setValue(true);
                        }
                    }
                });

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


