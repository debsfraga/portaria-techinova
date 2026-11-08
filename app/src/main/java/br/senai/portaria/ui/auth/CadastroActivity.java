package br.senai.portaria.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import br.senai.portaria.R;
import br.senai.portaria.ui.RoteadorActivity;
import br.senai.portaria.util.ErrosFirebase;

public class CadastroActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText edtEmail, edtSenha, edtConfirmarSenha;
    private Button btnCadastrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);
        setTitle(R.string.btn_cadastrar);

        auth = FirebaseAuth.getInstance();
        edtEmail = findViewById(R.id.edtEmail);
        edtSenha = findViewById(R.id.edtSenha);
        edtConfirmarSenha = findViewById(R.id.edtConfirmarSenha);
        btnCadastrar = findViewById(R.id.btnCadastrar);

        btnCadastrar.setOnClickListener(v -> cadastrar());
    }

    private void cadastrar() {
        String email = edtEmail.getText().toString().trim();
        String senha = edtSenha.getText().toString();
        String confirmar = edtConfirmarSenha.getText().toString();

        if (email.isEmpty()) {
            mostrarErro(edtEmail, R.string.erro_email_vazio);
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mostrarErro(edtEmail, R.string.erro_email_invalido);
            return;
        }
        if (senha.length() < 6) {
            mostrarErro(edtSenha, R.string.erro_senha_fraca);
            return;
        }
        if (!senha.equals(confirmar)) {
            mostrarErro(edtConfirmarSenha, R.string.erro_senhas_diferentes);
            return;
        }



        btnCadastrar.setEnabled(false);
        // ... o resto continua igual
        auth.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(this, task -> {
                    btnCadastrar.setEnabled(true);
                    if (task.isSuccessful()) {
                        // cadastro já deixa logado; o perfil é definido no console (NC4)
                        Intent i = new Intent(this, RoteadorActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                    } else {
                        Toast.makeText(this, ErrosFirebase.auth(task.getException()),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void mostrarErro(EditText campo, int mensagem) {
        campo.setError(getString(mensagem));
        campo.requestFocus();   // sem foco, o Android mostra só o ícone, sem o texto
    }
}