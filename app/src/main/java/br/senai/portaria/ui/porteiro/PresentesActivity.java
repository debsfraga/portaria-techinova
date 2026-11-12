package br.senai.portaria.ui.porteiro;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

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

    // Leitor de QR (ZXing): o resultado volta aqui e abre a conferência
    private final ActivityResultLauncher<ScanOptions> leitor =
            registerForActivityResult(new ScanContract(), result -> {
                if (result.getContents() == null) return;      // cancelou o leitor
                Intent i = new Intent(this, ConferenciaActivity.class);
                i.putExtra(ConferenciaActivity.EXTRA_ID, result.getContents().trim());
                startActivity(i);
            });

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

        findViewById(R.id.btnLerQr).setOnClickListener(v -> abrirLeitor());
    }

    private void abrirLeitor() {
        ScanOptions op = new ScanOptions();
        op.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        op.setPrompt(getString(R.string.prompt_leitor));
        op.setBeepEnabled(true);
        op.setOrientationLocked(false);
        leitor.launch(op);
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

            // o plural já cobre 0 ("0 visitantes"), 1 ("1 visitante") e vários
            txtContador.setText(getResources().getQuantityString(
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