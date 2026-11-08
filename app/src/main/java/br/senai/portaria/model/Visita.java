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
    @Exclude public void setId(String id) { this.id = id; }

    public String getNomeVisitante() { return nomeVisitante; }
    public void setNomeVisitante(String nomeVisitante) { this.nomeVisitante = nomeVisitante; }

    public String getEmpresa() { return empresa; }
    public void setEmpresa(String empresa) { this.empresa = empresa; }

    public String getAnfitriaoUid() { return anfitriaoUid; }
    public void setAnfitriaoUid(String anfitriaoUid) { this.anfitriaoUid = anfitriaoUid; }

    public String getAnfitriaoNome() { return anfitriaoNome; }
    public void setAnfitriaoNome(String anfitriaoNome) { this.anfitriaoNome = anfitriaoNome; }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    public String getHoraPrevista() { return horaPrevista; }
    public void setHoraPrevista(String horaPrevista) { this.horaPrevista = horaPrevista; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCriadaEm() { return criadaEm; }
    public void setCriadaEm(Timestamp criadaEm) { this.criadaEm = criadaEm; }

    public Timestamp getEntradaEm() { return entradaEm; }
    public void setEntradaEm(Timestamp entradaEm) { this.entradaEm = entradaEm; }

    public Timestamp getSaidaEm() { return saidaEm; }
    public void setSaidaEm(Timestamp saidaEm) { this.saidaEm = saidaEm; }
}