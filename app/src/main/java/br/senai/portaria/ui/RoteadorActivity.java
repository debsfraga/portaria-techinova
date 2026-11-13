package br.senai.portaria.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import br.senai.portaria.R;
import br.senai.portaria.ui.anfitriao.MinhasVisitasActivity;
import br.senai.portaria.ui.auth.LoginActivity;
import br.senai.portaria.ui.porteiro.PresentesActivity;

public class RoteadorActivity extends AppCompatActivity {

    private static final String TAG = "Roteador";

    private ProgressBar progresso;
    private TextView txtMensagem;
    private Button btnSair;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roteador);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        progresso = findViewById(R.id.progresso);
        txtMensagem = findViewById(R.id.txtMensagem);
        btnSair = findViewById(R.id.btnSair);

        btnSair.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            abrir(LoginActivity.class);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mostrarCarregando();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {                       // trata o null antes do getUid()
            abrir(LoginActivity.class);
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(this, doc -> {
                    String perfil = doc.getString("perfil");
                    if ("anfitriao".equals(perfil)) {
                        abrir(MinhasVisitasActivity.class);
                    } else if ("porteiro".equals(perfil)) {
                        abrir(PresentesActivity.class);
                    } else {
                        mostrarMensagem(getString(R.string.conta_sem_perfil, user.getUid()));
                    }
                })
                .addOnFailureListener(this, e -> {
                    Log.e(TAG, "Falha ao ler perfil", e);
                    mostrarMensagem(getString(R.string.erro_ler_perfil));
                });
    }

    private void abrir(Class<?> tela) {
        Intent i = new Intent(this, tela);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }

    private void mostrarCarregando() {
        progresso.setVisibility(View.VISIBLE);
        txtMensagem.setVisibility(View.GONE);
        btnSair.setVisibility(View.GONE);
    }

    private void mostrarMensagem(String texto) {
        progresso.setVisibility(View.GONE);
        txtMensagem.setText(texto);
        txtMensagem.setVisibility(View.VISIBLE);
        btnSair.setVisibility(View.VISIBLE);
    }
}