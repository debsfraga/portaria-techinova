package br.senai.portaria.ui.anfitriao;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import br.senai.portaria.R;
import br.senai.portaria.model.Visita;
import br.senai.portaria.util.Datas;

public class VisitaAdapter extends RecyclerView.Adapter<VisitaAdapter.VisitaViewHolder> {

    public interface AoClicar {
        void naVisita(Visita visita);
    }

    private final List<Visita> visitas = new ArrayList<>();
    private final AoClicar ouvinte;

    public VisitaAdapter(AoClicar ouvinte) {
        this.ouvinte = ouvinte;
    }

    public void atualizar(List<Visita> novas) {
        visitas.clear();
        visitas.addAll(novas);
        notifyDataSetChanged();          // a lista inteira foi trocada pelo listener
    }

    @NonNull
    @Override
    public VisitaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_visita, parent, false);
        return new VisitaViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VisitaViewHolder h, int position) {
        Visita v = visitas.get(position);
        Context ctx = h.itemView.getContext();

        h.txtNomeVisitante.setText(v.getNomeVisitante());
        h.txtEmpresa.setText(v.getEmpresa());
        h.txtDataHora.setText(Datas.paraTela(v.getData()) + " · " + v.getHoraPrevista());

        // regra de ouro do onBindViewHolder: os dois caminhos sempre escritos
        int texto, cor;
        String status = v.getStatus() == null ? "" : v.getStatus();
        switch (status) {
            case "DENTRO":
                texto = R.string.status_dentro;
                cor = R.color.verde_dentro;
                break;
            case "SAIU":
                texto = R.string.status_saiu;
                cor = R.color.cinza_texto;
                break;
            default:
                texto = R.string.status_agendada;
                cor = R.color.azul_portaria;
        }
        h.txtStatus.setText(texto);
        h.txtStatus.setTextColor(ContextCompat.getColor(ctx, cor));

        h.itemView.setOnClickListener(x ->
                ouvinte.naVisita(visitas.get(h.getBindingAdapterPosition())));
    }

    @Override
    public int getItemCount() {
        return visitas.size();
    }

    static class VisitaViewHolder extends RecyclerView.ViewHolder {
        final TextView txtNomeVisitante, txtEmpresa, txtDataHora, txtStatus;

        VisitaViewHolder(View itemView) {
            super(itemView);
            txtNomeVisitante = itemView.findViewById(R.id.txtNomeVisitante);
            txtEmpresa = itemView.findViewById(R.id.txtEmpresa);
            txtDataHora = itemView.findViewById(R.id.txtDataHora);
            txtStatus = itemView.findViewById(R.id.txtStatus);
        }
    }
}