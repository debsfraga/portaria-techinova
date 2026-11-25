package br.senai.portaria.ui.porteiro;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import br.senai.portaria.R;
import br.senai.portaria.data.VisitaRepository;
import br.senai.portaria.model.Visita;
import br.senai.portaria.ui.BaseLogadaActivity;
import br.senai.portaria.ui.anfitriao.VisitaAdapter;

public class HistoricoActivity extends BaseLogadaActivity {

    private static final String TAG = "Historico";

    private final VisitaRepository repo = new VisitaRepository();
    private EditText edtEmpresa;
    private TextView txtTotal, txtVazio;
    private RecyclerView rvHistorico;
    private VisitaAdapter adapter;
    private String ultimaBusca;   // a empresa da última busca feita, para refazer ao girar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico);
        setTitle(R.string.titulo_historico);

        edtEmpresa = findViewById(R.id.edtEmpresa);
        txtTotal = findViewById(R.id.txtTotal);
        txtVazio = findViewById(R.id.txtVazio);
        rvHistorico = findViewById(R.id.rvHistorico);

        // o mesmo adapter da Minhas visitas: aqui o clique não faz nada
        adapter = new VisitaAdapter(visita -> { });
        rvHistorico.setLayoutManager(new LinearLayoutManager(this));
        rvHistorico.setAdapter(adapter);

        findViewById(R.id.btnBuscar).setOnClickListener(v -> buscar());

        // a tecla de busca do teclado também busca
        edtEmpresa.setOnEditorActionListener((v, acao, evento) -> {
            if (acao == EditorInfo.IME_ACTION_SEARCH) {
                buscar();
                return true;
            }
            return false;
        });

        // rotação (NC7): refaz a busca que estava na tela
        if (savedInstanceState != null) {
            String busca = savedInstanceState.getString("ultimaBusca");
            if (busca != null) {
                edtEmpresa.setText(busca);
                buscar();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle out) {
        super.onSaveInstanceState(out);
        out.putString("ultimaBusca", ultimaBusca);
    }

    private void buscar() {
        String empresa = edtEmpresa.getText().toString().trim();
        if (empresa.isEmpty()) {
            edtEmpresa.setError(getString(R.string.erro_empresa));
            edtEmpresa.requestFocus();
            return;
        }
        ultimaBusca = empresa;

        // get(): uma foto do momento, como a aula indica para uma tela de consulta
        repo.porEmpresa(empresa).get()
                .addOnSuccessListener(this, query -> {
                    List<Visita> lista = new ArrayList<>();
                    for (DocumentSnapshot d : query) {
                        Visita v = d.toObject(Visita.class);
                        v.setId(d.getId());
                        lista.add(v);
                    }
                    adapter.atualizar(lista);
                    txtTotal.setText(getResources().getQuantityString(
                            R.plurals.contador_historico, lista.size(), lista.size()));

                    boolean vazia = lista.isEmpty();
                    rvHistorico.setVisibility(vazia ? View.GONE : View.VISIBLE);
                    txtVazio.setVisibility(vazia ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(this, e -> {
                    Log.e(TAG, "Falha na busca", e);
                    Toast.makeText(this, R.string.erro_generico, Toast.LENGTH_SHORT).show();
                });
    }
}