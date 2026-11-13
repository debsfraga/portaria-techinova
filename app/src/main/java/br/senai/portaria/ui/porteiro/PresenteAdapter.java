package br.senai.portaria.ui.porteiro;

import br.senai.portaria.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import br.senai.portaria.R;
import br.senai.portaria.model.Visita;
import br.senai.portaria.util.Datas;

public class PresenteAdapter extends RecyclerView.Adapter<PresenteAdapter.PresenteViewHolder> {

    public interface AoClicar {
        void noPresente(Visita visita);
    }

    private final List<Visita> presentes = new ArrayList<>();
    private final AoClicar ouvinte;

    public PresenteAdapter(AoClicar ouvinte) {
        this.ouvinte = ouvinte;
    }

    public void atualizar(List<Visita> novos) {
        presentes.clear();
        presentes.addAll(novos);
        notifyDataSetChanged();          // a lista inteira foi trocada pelo listener
    }

    @NonNull
    @Override
    public PresenteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_presente, parent, false);
        return new PresenteViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PresenteViewHolder h, int position) {
        Visita v = presentes.get(position);
        Context ctx = h.itemView.getContext();

        h.txtNomeVisitante.setText(v.getNomeVisitante());
        h.txtEmpresa.setText(v.getEmpresa());

        String anfitriao = v.getAnfitriaoNome() == null ? "—" : v.getAnfitriaoNome();
        h.txtAnfitriao.setText(ctx.getString(R.string.visitando, anfitriao));

        // entradaEm chega null por um instante logo após a gravação: Datas.hora mostra "agora"
        h.txtEntrada.setText(ctx.getString(R.string.entrou_as, Datas.hora(v.getEntradaEm())));

        // Fase 3: o clique vai registrar a saída
        h.itemView.setOnClickListener(x ->
                ouvinte.noPresente(presentes.get(h.getBindingAdapterPosition())));
    }

    @Override
    public int getItemCount() {
        return presentes.size();
    }

    static class PresenteViewHolder extends RecyclerView.ViewHolder {
        final TextView txtNomeVisitante, txtEmpresa, txtAnfitriao, txtEntrada;

        PresenteViewHolder(View itemView) {
            super(itemView);
            txtNomeVisitante = itemView.findViewById(R.id.txtNomeVisitante);
            txtEmpresa = itemView.findViewById(R.id.txtEmpresa);
            txtAnfitriao = itemView.findViewById(R.id.txtAnfitriao);
            txtEntrada = itemView.findViewById(R.id.txtEntrada);
        }
    }
}