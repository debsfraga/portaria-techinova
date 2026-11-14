package br.senai.portaria.ui.porteiro;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;

import br.senai.portaria.R;
import br.senai.portaria.data.VisitaRepository;
import br.senai.portaria.model.Visita;
import br.senai.portaria.ui.BaseLogadaActivity;
import br.senai.portaria.util.Datas;

public class ConferenciaActivity extends BaseLogadaActivity {

    public static final String EXTRA_ID = "visitaId";
    private static final String TAG = "Conferencia";

    private final VisitaRepository repo = new VisitaRepository();
    private String visitaId;

    private MaterialCardView cardResultado;
    private TextView txtResultado, txtNome, txtEmpresa, txtAnfitriao, txtPrevisto;
    private Button btnRegistrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conferencia);
        setTitle(R.string.titulo_conferencia);

        cardResultado = findViewById(R.id.cardResultado);
        txtResultado = findViewById(R.id.txtResultado);
        txtNome = findViewById(R.id.txtNomeVisitante);
        txtEmpresa = findViewById(R.id.txtEmpresa);
        txtAnfitriao = findViewById(R.id.txtAnfitriao);
        txtPrevisto = findViewById(R.id.txtPrevisto);
        btnRegistrar = findViewById(R.id.btnRegistrarEntrada);

        visitaId = getIntent().getStringExtra(EXTRA_ID);

        btnRegistrar.setOnClickListener(v -> registrar());
        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());

        conferir();
    }

    /** Lê a visita e decide: libera (verde) ou recusa (vermelho), mostrando o motivo. */
    private void conferir() {
        btnRegistrar.setEnabled(false);
        txtResultado.setText(R.string.carregando);
        cardResultado.setCardBackgroundColor(ContextCompat.getColor(this, R.color.cinza_texto));

        // um QR qualquer (link, texto com "/") não pode virar caminho do Firestore
        if (visitaId == null || visitaId.trim().isEmpty() || visitaId.contains("/")) {
            recusar(getString(R.string.erro_qr_invalido));
            return;
        }

        repo.visita(visitaId).get()
                .addOnSuccessListener(this, doc -> {
                    Visita v = doc.exists() ? doc.toObject(Visita.class) : null;
                    if (v == null || v.getData() == null) {
                        recusar(getString(R.string.erro_qr_invalido));
                        return;
                    }
                    mostrarDados(v);

                    if (!Datas.hojeIso().equals(v.getData())) {
                        recusar(getString(R.string.erro_qr_outro_dia, Datas.paraTela(v.getData())));
                    } else if ("DENTRO".equals(v.getStatus())) {
                        recusar(getString(R.string.erro_qr_ja_usado, Datas.hora(v.getEntradaEm())));
                    } else if ("SAIU".equals(v.getStatus())) {
                        recusar(getString(R.string.erro_qr_encerrada));
                    } else {
                        liberar();
                    }
                })
                .addOnFailureListener(this, e -> {
                    Log.e(TAG, "Falha ao ler visita", e);
                    recusar(getString(R.string.erro_generico));
                });
    }

    private void registrar() {
        btnRegistrar.setEnabled(false);
        repo.registrarEntrada(visitaId, FirebaseAuth.getInstance().getUid())
                .addOnSuccessListener(this, r -> {
                    Toast.makeText(this, R.string.entrada_registrada, Toast.LENGTH_SHORT).show();
                    finish();   // volta para "Quem está agora", onde o visitante já aparece
                })
                .addOnFailureListener(this, e -> {
                    // outro porteiro registrou antes, ou a data/status mudou: relê e mostra o motivo
                    Log.w(TAG, "Entrada recusada", e);
                    conferir();
                });
    }

    private void mostrarDados(Visita v) {
        txtNome.setText(v.getNomeVisitante());
        txtEmpresa.setText(v.getEmpresa());
        String anfitriao = v.getAnfitriaoNome() == null ? "—" : v.getAnfitriaoNome();
        txtAnfitriao.setText(getString(R.string.label_anfitriao, anfitriao));
        txtPrevisto.setText(getString(R.string.label_previsto,
                Datas.paraTela(v.getData()), v.getHoraPrevista()));
    }

    private void liberar() {
        txtResultado.setText(R.string.qr_valido);
        cardResultado.setCardBackgroundColor(ContextCompat.getColor(this, R.color.verde_dentro));
        btnRegistrar.setEnabled(true);
        btnRegistrar.setVisibility(View.VISIBLE);
    }

    private void recusar(String motivo) {
        txtResultado.setText(motivo);
        cardResultado.setCardBackgroundColor(ContextCompat.getColor(this, R.color.vermelho_recusa));
        btnRegistrar.setEnabled(false);
        btnRegistrar.setVisibility(View.GONE);
    }
}