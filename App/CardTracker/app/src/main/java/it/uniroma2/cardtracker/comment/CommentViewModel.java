package it.uniroma2.cardtracker.comment;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.HashMap;
import java.util.Map;

import it.uniroma2.cardtracker.R;
import it.uniroma2.cardtracker.entity.ListComments;

public class CommentViewModel extends AndroidViewModel {

    private static final String TAG = "CommentViewModel";

    private final String URL;
    private final RequestQueue requestQueue;
    private final MutableLiveData<ListComments> comments;
    private final MutableLiveData<String> errorMessage;
    private MutableLiveData<Boolean> add;
    private MutableLiveData<Boolean> delete;


    public CommentViewModel(@NonNull Application application) {
        super(application);
        requestQueue = Volley.newRequestQueue(application);
        comments = new MutableLiveData<>();
        errorMessage = new MutableLiveData<>();
        add = new MutableLiveData<>();
        delete = new MutableLiveData<>();

        URL = application.getString(R.string.url);
    }

    public MutableLiveData<ListComments> getComments() { return comments; }

    public MutableLiveData<String> getErrorMessage() { return errorMessage; }

    public MutableLiveData<Boolean> getAdd() { return add; }

    public MutableLiveData<Boolean> getDelete() { return delete; }

    public void getComment(int deckId) {
        String commentForDeckURL = URL + "comment?idDeck=" + deckId;
        add.setValue(false);
        delete.setValue(false);

        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                commentForDeckURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Risposta commentForDeckURL (Successo): " + response);
                        parseResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "onErrorResponse Comment: " +
                                (error.getMessage() != null ? error.getMessage() : "Errore Sconosciuto"));
                        if (error.networkResponse != null) {
                            String errorBody = new String(error.networkResponse.data);
                            Log.e(TAG, "Dati errore Comment: " + errorBody);
                            errorMessage.setValue(parseError(errorBody));
                        } else {
                            errorMessage.setValue("Errore di rete o server irraggiungibile.");
                        }
                    }
                });

        requestQueue.add(stringRequest);
    }

    public void addComment(int idDeck, String text, String token) {
        String addCommentURL = URL + "comment";
        add.setValue(false);

        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                addCommentURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Risposta addCommentURL (Successo): " + response);
                        add.setValue(true);
                        getComment(idDeck);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "onErrorResponse Comment: " +
                                (error.getMessage() != null ? error.getMessage() : "Errore Sconosciuto"));
                        if (error.networkResponse != null) {
                            String errorBody = new String(error.networkResponse.data);
                            Log.e(TAG, "Dati errore Comment: " + errorBody);
                            errorMessage.setValue(parseError(errorBody));
                        } else {
                            errorMessage.setValue("Errore di rete o server irraggiungibile.");
                        }
                    }
                }
        ) {
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
                params.put("idDeck", String.valueOf(idDeck));
                params.put("text", text.trim());
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }


    public void deleteComment(int idDeck, int idComment, String token) {
        String deleteCommentURL = URL + "comment?idDeck=" + idDeck + "&idComment=" + idComment;
        delete.setValue(false);

        StringRequest stringRequest = new StringRequest(
                Request.Method.DELETE,
                deleteCommentURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Risposta deleteCommentURL (Successo): " + response);
                        delete.setValue(true);
                        getComment(idDeck);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "onErrorResponse Comment: " +
                                (error.getMessage() != null ? error.getMessage() : "Errore Sconosciuto"));
                        if (error.networkResponse != null) {
                            String errorBody = new String(error.networkResponse.data);
                            Log.e(TAG, "Dati errore Comment: " + errorBody);
                            errorMessage.setValue(parseError(errorBody));
                        } else {
                            errorMessage.setValue("Errore di rete o server irraggiungibile.");
                        }
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", token.trim());
                return headers;
            }
        };

        requestQueue.add(stringRequest);
    }

    private void parseResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            errorMessage.setValue("Risposta del server vuota o nulla.");
            return;
        }

        try {
            Object jsonValue = new JSONTokener(response).nextValue();

            if (jsonValue instanceof JSONObject) {
                JSONObject json = (JSONObject) jsonValue;

                if (json.has("error")) {
                    errorMessage.setValue(json.getString("error"));
                    return;
                }

                Log.w(TAG, "Risposta di tipo JSONObject inattesa, tentiamo il parsing come lista.");

            } else if (jsonValue instanceof JSONArray) {
                Log.d(TAG, "Risposta di tipo JSONArray trovata.");
            }

            ListComments list = ListComments.fromJSON(response);

            if (list != null) {
                this.comments.setValue(list);
            } else {
                errorMessage.setValue("Nessun commento trovato o risposta non interpretabile.");
            }

        } catch (Exception e) {
            Log.e(TAG, "ParseResponse: " + e.getMessage(), e);
            errorMessage.setValue("Errore nel parsing della risposta del server: " + e.getMessage());
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
