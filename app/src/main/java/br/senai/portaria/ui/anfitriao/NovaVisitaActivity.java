package br.senai.portaria.ui.anfitriao;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Locale;

import br.senai.portaria.R;
import br.senai.portaria.data.VisitaRepository;
import br.senai.portaria.model.Visita;
import br.senai.portaria.util.Datas;

public class NovaVisitaActivity extends AppCompatActivity {

    private static final String TAG = "NovaVisita";

    private final VisitaRepository repo = new VisitaRepository();

    private EditText edtNomeVisitante, edtEmpresa;
    private Button btnData, btnHora, btnSalvar;

    private String dataIso;        // "2026-12-03"
    private String horaPrevista;   // "14:30"
    private String anfitriaoNome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nova_visita);
        setTitle(R.string.titulo_nova_visita);

        edtNomeVisitante = findViewById(R.id.edtNomeVisitante);
        edtEmpresa = findViewById(R.id.edtEmpresa);
        btnData = findViewById(R.id.btnData);
        btnHora = findViewById(R.id.btnHora);
        btnSalvar = findViewById(R.id.btnSalvar);

        // Rotação (NC7): recupera data e hora escolhidas antes de girar
        if (savedInstanceState != null) {
            dataIso = savedInstanceState.getString("dataIso");
            horaPrevista = savedInstanceState.getString("horaPrevista");
            if (dataIso != null) btnData.setText(Datas.paraTela(dataIso));
            if (horaPrevista != null) btnHora.setText(horaPrevista);
        }

        btnData.setOnClickListener(v -> escolherData());
        btnHora.setOnClickListener(v -> escolherHora());
        btnSalvar.setOnClickListener(v -> salvar());

        // Nome do anfitrião, para gravar junto com a visita
        FirebaseFirestore.getInstance().collection("usuarios")
                .document(FirebaseAuth.getInstance().getUid())
                .get()
                .addOnSuccessListener(doc -> anfitriaoNome = doc.getString("nome"))
                .addOnFailureListener(e -> Log.w(TAG, "Sem nome do anfitrião", e));
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle out) {
        super.onSaveInstanceState(out);
        out.putString("dataIso", dataIso);
        out.putString("horaPrevista", horaPrevista);
    }

    private void escolherData() {
        Calendar c = Calendar.getInstance();
        DatePickerDialog d = new DatePickerDialog(this, (view, ano, mes, dia) -> {
            dataIso = Datas.iso(ano, mes, dia);
            btnData.setText(Datas.paraTela(dataIso));
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        d.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);  // sem datas passadas
        d.show();
    }

    private void escolherHora() {
        new TimePickerDialog(this, (view, h, m) -> {
            horaPrevista = String.format(Locale.US, "%02d:%02d", h, m);
            btnHora.setText(horaPrevista);
        }, 9, 0, true).show();
    }

    private void salvar() {
        String nome = edtNomeVisitante.getText().toString().trim();
        String empresa = edtEmpresa.getText().toString().trim();

        if (nome.isEmpty()) {
            edtNomeVisitante.setError(getString(R.string.erro_nome_visitante));
            return;
        }
        if (empresa.isEmpty()) {
            edtEmpresa.setError(getString(R.string.erro_empresa));
            return;
        }
        if (dataIso == null) { aviso(R.string.erro_data_vazia); return; }
        if (dataIso.compareTo(Datas.hojeIso()) < 0) { aviso(R.string.erro_data_passada); return; }
        if (horaPrevista == null) { aviso(R.string.erro_hora_vazia); return; }

        Visita v = new Visita();
        v.setNomeVisitante(nome);
        v.setEmpresa(empresa);
        v.setData(dataIso);
        v.setHoraPrevista(horaPrevista);
        v.setAnfitriaoUid(FirebaseAuth.getInstance().getUid());
        v.setAnfitriaoNome(anfitriaoNome);
        v.setStatus("AGENDADA");

        btnSalvar.setEnabled(false);   // evita toque duplo gravando duas visitas
        repo.criar(v)
                .addOnSuccessListener(ref -> {
                    aviso(R.string.visita_salva);
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSalvar.setEnabled(true);
                    Log.e(TAG, "Não gravou", e);
                    aviso(R.string.erro_salvar_visita);
                });
    }

    private void aviso(@StringRes int mensagem) {
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show();
    }
}