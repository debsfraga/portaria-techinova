package br.senai.portaria.ui.porteiro;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

import br.senai.portaria.R;
import br.senai.portaria.data.VisitaRepository;
import br.senai.portaria.model.Visita;
import br.senai.portaria.ui.BaseLogadaActivity;

public class PresentesActivity extends BaseLogadaActivity {

    private static final String TAG = "Presentes";

    private final VisitaRepository repo = new VisitaRepository();
    private PresenteAdapter adapter;
    private RecyclerView rvPresentes;
    private View grupoVazio;
    private TextView txtContador;
    private ListenerRegistration registro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_presentes);
        setTitle(R.string.titulo_presentes);

        txtContador = findViewById(R.id.txtContador);
        rvPresentes = findViewById(R.id.rvPresentes);
        grupoVazio = findViewById(R.id.grupoVazio);

        adapter = new PresenteAdapter(visita -> {
            // Fase 3 (issue #11): registrar a saída deste visitante
        });
        rvPresentes.setLayoutManager(new LinearLayoutManager(this));   // sem isso a lista fica em branco
        rvPresentes.setHasFixedSize(true);
        rvPresentes.setAdapter(adapter);

        // Fase 2 (issue #9): abre o leitor de QR
        findViewById(R.id.btnLerQr).setOnClickListener(v ->
                Toast.makeText(this, R.string.em_breve, Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onStart() {
        super.onStart();
        registro = repo.presentes().addSnapshotListener((query, e) -> {
            if (e != null) {
                Log.w(TAG, "Falha ao ouvir presentes", e);
                return;
            }
            List<Visita> lista = new ArrayList<>();
            for (DocumentSnapshot d : query) {
                Visita v = d.toObject(Visita.class);
                v.setId(d.getId());
                lista.add(v);
            }
            adapter.atualizar(lista);

            txtContador.setText(lista.isEmpty()
                    ? getString(R.string.contador_zero)
                    : getResources().getQuantityString(
                    R.plurals.contador_presentes, lista.size(), lista.size()));

            boolean vazia = lista.isEmpty();
            rvPresentes.setVisibility(vazia ? View.GONE : View.VISIBLE);
            grupoVazio.setVisibility(vazia ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (registro != null) registro.remove();      // não é opcional
    }
}