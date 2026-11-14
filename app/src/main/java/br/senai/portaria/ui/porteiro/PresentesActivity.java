package br.senai.portaria.ui.porteiro;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
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

        adapter = new PresenteAdapter(this::confirmarSaida);
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

    /** Confirmação antes de uma ação que não tem desfazer (padrão da aula de Firestore). */
    private void confirmarSaida(Visita visita) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.titulo_registrar_saida)
                .setMessage(getString(R.string.msg_registrar_saida, visita.getNomeVisitante()))
                .setNegativeButton(R.string.btn_cancelar, null)
                .setPositiveButton(R.string.btn_registrar_saida, (dialog, which) -> registrarSaida(visita))
                .show();
    }

    private void registrarSaida(Visita visita) {
        repo.registrarSaida(visita.getId(), FirebaseAuth.getInstance().getUid())
                .addOnSuccessListener(this, r ->
                        Toast.makeText(this,
                                getString(R.string.saida_registrada, visita.getNomeVisitante()),
                                Toast.LENGTH_SHORT).show())
                .addOnFailureListener(this, e -> {
                    Log.w(TAG, "Saída recusada", e);
                    Toast.makeText(this, R.string.erro_saida, Toast.LENGTH_LONG).show();
                });
        // não precisa atualizar a lista: o snapshot listener tira o visitante sozinho
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