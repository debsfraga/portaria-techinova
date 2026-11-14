package br.senai.portaria.ui.anfitriao;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.ListenerRegistration;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import br.senai.portaria.R;
import br.senai.portaria.data.VisitaRepository;
import br.senai.portaria.model.Visita;
import br.senai.portaria.ui.BaseLogadaActivity;
import br.senai.portaria.util.Datas;

public class VisitaDetalheActivity extends BaseLogadaActivity {

    public static final String EXTRA_ID = "visitaId";
    private static final String TAG = "VisitaDetalhe";

    private final VisitaRepository repo = new VisitaRepository();
    private String visitaId;

    private ImageView imgQr;
    private TextView txtNome, txtEmpresa, txtPrevisto, txtStatus;
    private ListenerRegistration registro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visita_detalhe);
        setTitle(R.string.titulo_detalhe_visita);

        imgQr = findViewById(R.id.imgQr);
        txtNome = findViewById(R.id.txtNomeVisitante);
        txtEmpresa = findViewById(R.id.txtEmpresa);
        txtPrevisto = findViewById(R.id.txtPrevisto);
        txtStatus = findViewById(R.id.txtStatus);

        visitaId = getIntent().getStringExtra(EXTRA_ID);
        if (visitaId == null) {
            Toast.makeText(this, R.string.visita_nao_encontrada, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        gerarQr(visitaId);   // o QR carrega só o id da visita
    }

    private void gerarQr(String conteudo) {
        try {
            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap qr = encoder.encodeBitmap(conteudo, BarcodeFormat.QR_CODE, 800, 800);
            imgQr.setImageBitmap(qr);
        } catch (WriterException e) {
            Log.e(TAG, "Falha ao gerar QR", e);
            Toast.makeText(this, R.string.erro_gerar_qr, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (visitaId == null) return;

        // ao vivo: quando o porteiro registrar a entrada, o status muda aqui sozinho
        registro = repo.visita(visitaId).addSnapshotListener((doc, e) -> {
            if (e != null) {
                Log.w(TAG, "Falha ao ouvir visita", e);
                return;
            }
            if (doc == null || !doc.exists()) return;
            Visita v = doc.toObject(Visita.class);
            if (v != null) mostrar(v);
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (registro != null) registro.remove();
    }

    private void mostrar(Visita v) {
        txtNome.setText(v.getNomeVisitante());
        txtEmpresa.setText(v.getEmpresa());
        if (v.getData() != null) {
            txtPrevisto.setText(getString(R.string.label_previsto,
                    Datas.paraTela(v.getData()), v.getHoraPrevista()));
        }

        // os dois caminhos sempre escritos, como no VisitaAdapter
        int texto, cor;
        String status = v.getStatus() == null ? "" : v.getStatus();
        switch (status) {
            case "DENTRO": texto = R.string.status_dentro;   cor = R.color.verde_dentro; break;
            case "SAIU":   texto = R.string.status_saiu;     cor = R.color.cinza_texto;  break;
            default:       texto = R.string.status_agendada; cor = R.color.azul_portaria;
        }
        txtStatus.setText(texto);
        txtStatus.setTextColor(ContextCompat.getColor(this, cor));
    }
}