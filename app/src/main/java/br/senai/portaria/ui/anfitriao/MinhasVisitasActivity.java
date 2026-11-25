package br.senai.portaria.ui.anfitriao;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

import br.senai.portaria.R;
import br.senai.portaria.data.VisitaRepository;
import br.senai.portaria.model.Visita;
import br.senai.portaria.ui.BaseLogadaActivity;
import br.senai.portaria.util.Datas;

public class MinhasVisitasActivity extends BaseLogadaActivity {

    private static final String TAG = "MinhasVisitas";

    private final VisitaRepository repo = new VisitaRepository();
    private VisitaAdapter adapter;
    private RecyclerView rvVisitas;
    private View grupoVazio;
    private ListenerRegistration registro;

    // não avisar sobre visitantes que já estavam DENTRO quando a tela abriu
    private boolean primeiraCarga = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_minhas_visitas);
        setTitle(R.string.titulo_minhas_visitas);

        rvVisitas = findViewById(R.id.rvVisitas);
        grupoVazio = findViewById(R.id.grupoVazio);

        // o clique abre o QR da visita (#8)
        adapter = new VisitaAdapter(visita -> {
            Intent i = new Intent(this, VisitaDetalheActivity.class);
            i.putExtra(VisitaDetalheActivity.EXTRA_ID, visita.getId());
            startActivity(i);
        });
        rvVisitas.setLayoutManager(new LinearLayoutManager(this));   // sem isso a lista fica em branco
        rvVisitas.setHasFixedSize(true);
        rvVisitas.setAdapter(adapter);

        findViewById(R.id.fabNova).setOnClickListener(v ->
                startActivity(new Intent(this, NovaVisitaActivity.class)));
    }

    @Override
    protected void onStart() {
        super.onStart();
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;                       // sem login, não há o que listar

        primeiraCarga = true;
        registro = repo.doAnfitriao(uid).addSnapshotListener((query, e) -> {
            if (e != null) {
                Log.w(TAG, "Falha ao ouvir visitas", e);
                return;
            }
            List<Visita> lista = new ArrayList<>();
            for (DocumentSnapshot d : query) {
                Visita v = d.toObject(Visita.class);
                v.setId(d.getId());                    // o id vem do documento
                lista.add(v);
            }
            adapter.atualizar(lista);

            // getDocumentChanges(): só o que mudou desde o último aviso do Firestore
            if (!primeiraCarga) {
                for (DocumentChange dc : query.getDocumentChanges()) {
                    if (dc.getType() != DocumentChange.Type.MODIFIED) continue;
                    Visita mudou = dc.getDocument().toObject(Visita.class);
                    if ("DENTRO".equals(mudou.getStatus()) && mudou.getEntradaEm() != null) {
                        avisarChegada(mudou);
                    }
                }
            }
            primeiraCarga = false;

            boolean vazia = lista.isEmpty();
            rvVisitas.setVisibility(vazia ? View.GONE : View.VISIBLE);
            grupoVazio.setVisibility(vazia ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (registro != null) registro.remove();       // não é opcional
    }

    private void avisarChegada(Visita v) {
        String texto = getString(R.string.aviso_chegada,
                v.getNomeVisitante(), Datas.hora(v.getEntradaEm()));
        Snackbar.make(rvVisitas, texto, Snackbar.LENGTH_LONG).show();
    }
}