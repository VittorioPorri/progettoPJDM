package it.uniroma2.cardtracker.auth;

import android.app.Application;
import android.util.Log;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import it.uniroma2.cardtracker.entity.User;

public class AuthViewModel extends AndroidViewModel {
    private final String TAG = "AuthViewModel";
    private final String URL;
    private RequestQueue requestQueue;
    private MutableLiveData<User> utente;
    private MutableLiveData<String> errorMessage;
    private final MutableLiveData<Boolean>  offline;
    private MutableLiveData<Boolean> registration;
    private MutableLiveData<String> token;

    public AuthViewModel(@NonNull Application application) {
        super(application);
        requestQueue = Volley.newRequestQueue(application);
        utente = new MutableLiveData<>();
        errorMessage = new MutableLiveData<>();
        offline = new MutableLiveData<>(false);
        registration = new MutableLiveData<>();
        token = new MutableLiveData<>();
        URL = application.getString(R.string.url);
    }

    public MutableLiveData<User> getUser() {
        return utente;
    }
    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }
    public MutableLiveData<Boolean> getOffline() { return offline; }
    public MutableLiveData<Boolean> getRegistration() {return registration;}
    public MutableLiveData<String> getToken() {return token;}


    public void login(String email, String password) {
        String LoginURL = URL + "login";

        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            errorMessage.setValue("Email e Password sono obbligatori");
            return;
        }

        if(!isValidEmail(email)){
            errorMessage.setValue("Email non valida");
            return;
        }

        StringRequest stringRequest = new StringRequest(Request.Method.POST, LoginURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Risposta Login (Successo): " + response);
                        parseResponse(response);
                    }

                }, new Response.ErrorListener(){
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "onErrorResponse Login: " + (error.getMessage() != null ? error.getMessage() : "Errore Sconosciuto"));
                    if (error.networkResponse != null) {
                        if (error.networkResponse.statusCode == 500) {
                            Log.e(TAG, "Errore 500 rilevato: Server Down");
                            errorMessage.setValue("Server  irraggiungibile");
                            offline.setValue(true);
                        }
                        else {
                            String errorBody = new String(error.networkResponse.data);
                            Log.e(TAG, "Dati errore Login: " + errorBody);
                            errorMessage.setValue(parseError(errorBody));
                        }

                    } else {
                        errorMessage.setValue("Problemi di connessione");
                        offline.setValue(true);
                    }
                }
        }){
            @Override
            protected String getParamsEncoding() {
                return "UTF-8";
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email.trim());
                params.put("password", password.trim());
                return params;
            }

                        @Override
            protected Response<String> parseNetworkResponse(com.android.volley.NetworkResponse response) {
                assert response.headers != null;
                for (Map.Entry<String, String> entry : response.headers.entrySet()) {
                    if (entry.getKey().equalsIgnoreCase("Authorization")) {
                        Log.d(TAG, "Token ricevuto (Login): " + entry.getValue());
                        token.postValue(entry.getValue());
                        break;
                    }
                }
                return super.parseNetworkResponse(response);
            }


        };
        requestQueue.add(stringRequest);
    }


    public void login(String token) {
        final String LoginURL = URL + "login";

        if (token == null || token.trim().isEmpty()) {
            errorMessage.setValue("Token obbligatorio");
            return;
        }

        StringRequest stringRequest = new StringRequest(Request.Method.POST, LoginURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Risposta Riconnessione Token (Successo): " + response);
                        parseResponse(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "onErrorResponse Login: " + (error.getMessage() != null ? error.getMessage() : "Errore Sconosciuto"));
                if (error.networkResponse != null) {
                    if (error.networkResponse.statusCode == 500) {
                        Log.e(TAG, "Errore 500 rilevato: Server Down");
                        errorMessage.setValue("Server  irraggiungibile");
                        offline.setValue(true);
                    }
                    else {
                        String errorBody = new String(error.networkResponse.data);
                        Log.e(TAG, "Dati errore Login: " + errorBody);
                        errorMessage.setValue(parseError(errorBody));
                    }

                } else {
                    errorMessage.setValue("Problemi di connessione");
                    offline.setValue(true);
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", token.trim());
                return headers;
            }

        };

        requestQueue.add(stringRequest);
    }


    public void register(String email, String password, String name) {
        String RegisterURL = URL + "register";

        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty() || name == null || name.trim().isEmpty()) {
            errorMessage.setValue("Email, Password e Nome sono obbligatori");
            return;
        }

        if (!isValidEmail(email)) {
            errorMessage.setValue("Email non valida");
            return;
        }

        StringRequest stringRequest = new StringRequest(Request.Method.POST, RegisterURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Risposta Register (Successo): " + response);
                        registration.setValue(true);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "onErrorResponse Register: " + (error.getMessage() != null ? error.getMessage() : "Errore Sconosciuto"));
                        registration.setValue(false);
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
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email.trim());
                params.put("password", password.trim());
                params.put("name", name.trim());
                return params;
            }

            @Override
            protected Response<String> parseNetworkResponse(com.android.volley.NetworkResponse response) {
                assert response.headers != null;
                for (Map.Entry<String, String> entry : response.headers.entrySet()) {
                    if (entry.getKey().equalsIgnoreCase("Authorization")) {
                        Log.d(TAG, "Token ricevuto (Register): " + entry.getValue());
                        break;
                    }
                }
                return super.parseNetworkResponse(response);
            }
        };

        requestQueue.add(stringRequest);
    }


    private boolean isValidEmail(String email) {
        return email.contains("@") && email.length() > 4;
    }


    private void parseResponse(String response) {
        try {
            User user = User.fromJSON(response);
            if (user != null) {
                utente.setValue(user);
            } else {
                errorMessage.setValue("Credenziali non valide");
            }
        } catch (Exception e) {
            Log.d(TAG, "ParseResponse: " + e.getMessage());
            errorMessage.setValue("Errore nel parsing della risposta");
        }
    }

    private String parseError(String errorBody) {
        if (errorBody == null || errorBody.trim().isEmpty())
            return "Errore sconosciuto dal server.";

        try {
            JSONObject jsonError = new JSONObject(errorBody);
            if (jsonError.has("error")) {
                return jsonError.getString("error");
            }
        } catch (JSONException e) {
            Log.w(TAG, "Corpo errore non JSON: " + errorBody);
        }
        return "Errore di accesso al server.";
    }

}
