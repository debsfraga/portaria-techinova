package br.senai.portaria.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;

public class Visita {
    private String id;              // não vai para o banco
    private String nomeVisitante;
    private String empresa;
    private String anfitriaoUid;
    private String anfitriaoNome;
    private String data;            // "yyyy-MM-dd"
    private String horaPrevista;    // "HH:mm"
    private String status;          // AGENDADA | DENTRO | SAIU
    @ServerTimestamp
    private Timestamp criadaEm;     // o servidor preenche ao gravar
    private Timestamp entradaEm;
    private Timestamp saidaEm;

    public Visita() { }             // obrigatório para o toObject()

    @Exclude public String getId() { return id; }
    @Exclude
    public void setId(String id) { this.id = id; }
    // getters e setters de todos os outros campos
}
