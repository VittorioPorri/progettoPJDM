package it.uniroma2.cardtracker.error;

import android.os.Bundle; // Necessario per il metodo onCreate
import androidx.appcompat.app.AppCompatActivity;

import it.uniroma2.cardtracker.R;

public class ServerOfflineActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_server_offline);
    }
}