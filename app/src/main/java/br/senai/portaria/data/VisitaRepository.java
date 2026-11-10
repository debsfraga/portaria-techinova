package br.senai.portaria.data;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import br.senai.portaria.model.Visita;

public class VisitaRepository {
    private final CollectionReference visitas =
            FirebaseFirestore.getInstance().collection("visitas");

    public Task<DocumentReference> criar(Visita v) {
        return visitas.add(v);                       // id automático, como na aula
    }

    public Query doAnfitriao(String uid) {
        return visitas.whereEqualTo("anfitriaoUid", uid)
                .orderBy("data", Query.Direction.DESCENDING);
    }

    public Query presentes() {
        return visitas.whereEqualTo("status", "DENTRO")
                .orderBy("entradaEm", Query.Direction.ASCENDING);
    }
}