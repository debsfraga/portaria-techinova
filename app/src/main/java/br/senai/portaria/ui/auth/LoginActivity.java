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

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText edtEmail, edtSenha;
    private Button btnEntrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (getSupportActionBar() != null) getSupportActionBar().hide();   // login sem barra

        auth = FirebaseAuth.getInstance();
        edtEmail = findViewById(R.id.edtEmail);
        edtSenha = findViewById(R.id.edtSenha);
        btnEntrar = findViewById(R.id.btnEntrar);

        btnEntrar.setOnClickListener(v -> entrar());
        findViewById(R.id.txtCriarConta).setOnClickListener(v ->
                startActivity(new Intent(this, CadastroActivity.class)));
    }

    private void entrar() {
        String email = edtEmail.getText().toString().trim();
        String senha = edtSenha.getText().toString();

        if (email.isEmpty()) {
            edtEmail.setError(getString(R.string.erro_email_vazio));
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError(getString(R.string.erro_email_invalido));
            return;
        }
        if (senha.length() < 6) {
            edtSenha.setError(getString(R.string.erro_senha_fraca));
            return;
        }

        btnEntrar.setEnabled(false);
        auth.signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener(this, task -> {
                    btnEntrar.setEnabled(true);
                    if (task.isSuccessful()) {
                        Intent i = new Intent(this, RoteadorActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                    } else {
                        Toast.makeText(this, ErrosFirebase.auth(task.getException()),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}