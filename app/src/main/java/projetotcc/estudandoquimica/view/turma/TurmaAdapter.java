package projetotcc.estudandoquimica.view.turma;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import projetotcc.estudandoquimica.R;
import projetotcc.estudandoquimica.databinding.TurmaItemBinding;
import projetotcc.estudandoquimica.model.Turma;
import projetotcc.estudandoquimica.viewmodel.TurmaViewModel;

public class TurmaAdapter extends RecyclerView.Adapter<TurmaAdapter.BindingHolder> {


    private TurmaClickListener turmaClickListener;

    private List<Turma> turmas;
    private Context context;
    private SparseBooleanArray selectedItems;

    public TurmaAdapter(List<Turma> turmas, Context context, TurmaClickListener listener) {
        this.turmas = turmas;
        this.context = context;
       //addTurma(new Turma("ola", "13/09/2018 16:10:55", "Iury"), getItemCount());
       // addTurma(new Turma("ola"), getItemCount());

        this.selectedItems = new SparseBooleanArray();
        setHasStableIds(true);

        this.turmaClickListener = listener;


    }

    @NonNull
    @Override
    public BindingHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        final TurmaItemBinding binding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.getContext()),
                    R.layout.turma_item,
                    parent, false);

        return new BindingHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BindingHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()){
            super.onBindViewHolder(holder, position, payloads);
        }
        else {
            Bundle o = (Bundle) payloads.get(0);
            for (String key : o.keySet()) {
                if(key.equals("nome")){

                    Toast.makeText(holder.itemView.getContext(), "Contact "+position+" : Name Changed", Toast.LENGTH_SHORT).show();
                    TurmaViewModel model = new TurmaViewModel();
                    model.setTurma(turmas.get(position), context);
                    holder.binding.setTurma(model);

                }

            }
        }
        holder.itemView.setActivated(selectedItems.get(position, false));

    }

    @Override
    public void onBindViewHolder(@NonNull BindingHolder holder, int position) {

        TurmaItemBinding binding = holder.binding;

        TurmaViewModel model = new TurmaViewModel();
        model.setTurma(turmas.get(position), context);

        holder.itemView.setActivated(selectedItems.get(position, false));

        binding.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                turmaClickListener.onClick(turmas.get(position));

            }
        });
        binding.setTurma(model);
    }

    public void toggleSelection(int pos) {
        if (selectedItems.get(pos, false)) {
            selectedItems.delete(pos);
        }
        else {
            selectedItems.put(pos, true);
        }
        notifyItemRangeChanged(pos, turmas.size());
        //notifyItemChanged(pos);

    }

    public void clearSelections() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    public List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<Integer>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); i++) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }

    @Override
    public int getItemCount() {
        return turmas == null ? 0 : turmas.size();
    }

    public void setTurmas(List<Turma> turmas) {
        this.turmas = turmas;
        notifyItemRangeChanged(0, turmas.size());
    }

    public void updateTurmas(List<Turma> newTurmas){
        //this.turmas = newTurmas;

        if(this.turmas == null){
            this.turmas = newTurmas;

            //notifyItemRangeChanged(0, newTurmas.size());
        }

        DiffUtil.DiffResult diffUtil = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return turmas != null ? turmas.size() : 0;
            }

            @Override
            public int getNewListSize() {
                return newTurmas != null ? newTurmas.size() : 0;
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {

                return turmas.get(oldItemPosition).equals(newTurmas.get(newItemPosition));
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {

                notifyItemChanged(oldItemPosition);

                Turma antiga = turmas.get(oldItemPosition);
                Turma nova = newTurmas.get(newItemPosition);

                return antiga.getData_criacao().equals(nova.getData_criacao())
                        && antiga.getAdministradorTurma().equals(nova.getAdministradorTurma())
                        && antiga.getNome().equals(nova.getNome())
                        && antiga.getId().equals(nova.getId())
                        && antiga.getCodeTurma().equals(nova.getCodeTurma());
            }

            @Nullable
            @Override
            public Object getChangePayload(int oldItemPosition, int newItemPosition) {

                Turma antiga = turmas.get(oldItemPosition);
                Turma nova = newTurmas.get(newItemPosition);

                Bundle diff = new Bundle();
                if(!nova.getNome().equals(antiga.getNome())){
                    diff.putString("nome", nova.getNome());
                }

                if (diff.size()==0){
                    return null;
                }
                return diff;
            }
        });

        turmas = newTurmas;

        diffUtil.dispatchUpdatesTo(this);
        //turmas.clear();
        //turmas.addAll(newTurmas);
        notifyDataSetChanged();
    }

    public void addTurma(Turma turma, int posicao){

        turmas.add(posicao,turma);
        notifyItemInserted(getItemCount());
        notifyItemRangeChanged(posicao, turmas.size());
    }

    public void removerTurma(int position) {
        turmas.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, turmas.size());
    }

    public Turma getTurma(int posicao){
        return turmas.get(posicao);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class BindingHolder extends RecyclerView.ViewHolder{

        private TurmaItemBinding binding;

        private BindingHolder(TurmaItemBinding binding) {
            super(binding.cardView);
            this.binding = binding;

            this.binding.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    turmaClickListener.onClick(turmas.get(getAdapterPosition()));
                }
            });
        }

    }

//    public static class Common {
//        public static Turma currentTurma = null;
//    }
}
