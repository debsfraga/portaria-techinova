package br.senai.portaria.data;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import br.senai.portaria.model.Visita;


import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.Map;

import br.senai.portaria.util.Datas;
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

    public DocumentReference visita(String id) {
        return visitas.document(id);
    }

    /**
     * Registra a entrada numa transação
     */
    public Task<Void> registrarEntrada(String id, String porteiroUid) {
        DocumentReference ref = visitas.document(id);
        String hoje = Datas.hojeIso();

        return FirebaseFirestore.getInstance().runTransaction(t -> {
            DocumentSnapshot s = t.get(ref);
            if (!s.exists()
                    || !hoje.equals(s.getString("data"))
                    || !"AGENDADA".equals(s.getString("status"))) {
                throw new FirebaseFirestoreException("Entrada recusada",
                        FirebaseFirestoreException.Code.ABORTED);
            }
            Map<String, Object> m = new HashMap<>();
            m.put("status", "DENTRO");
            m.put("entradaEm", FieldValue.serverTimestamp());
            m.put("porteiroEntradaUid", porteiroUid);
            t.update(ref, m);
            return null;
        });
    }

    /**
     * Registra a saída numa transação: só grava se a visita existir e estiver DENTRO.
     * Dois porteiros tocando no mesmo visitante não registram duas saídas.
     */
    public Task<Void> registrarSaida(String id, String porteiroUid) {
        DocumentReference ref = visitas.document(id);

        return FirebaseFirestore.getInstance().runTransaction(t -> {
            DocumentSnapshot s = t.get(ref);
            if (!s.exists() || !"DENTRO".equals(s.getString("status"))) {
                throw new FirebaseFirestoreException("Saída recusada",
                        FirebaseFirestoreException.Code.ABORTED);
            }
            Map<String, Object> m = new HashMap<>();
            m.put("status", "SAIU");
            m.put("saidaEm", FieldValue.serverTimestamp());
            m.put("porteiroSaidaUid", porteiroUid);
            t.update(ref, m);
            return null;
        });
    }
}