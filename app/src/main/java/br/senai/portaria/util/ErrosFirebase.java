package br.senai.portaria.util;

import androidx.annotation.StringRes;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

import br.senai.portaria.R;

public final class ErrosFirebase {
    private ErrosFirebase() { }

    @StringRes
    public static int auth(Exception e) {
        // WeakPassword vem antes: ela é filha de InvalidCredentials
        if (e instanceof FirebaseAuthWeakPasswordException)    return R.string.erro_senha_fraca;
        if (e instanceof FirebaseAuthUserCollisionException)   return R.string.erro_email_em_uso;
        if (e instanceof FirebaseAuthInvalidUserException)     return R.string.erro_conta_inexistente;
        if (e instanceof FirebaseAuthInvalidCredentialsException) return R.string.erro_credenciais;
        if (e instanceof FirebaseNetworkException)             return R.string.erro_sem_internet;
        return R.string.erro_generico;
    }
}