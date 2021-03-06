package projetotcc.estudandoquimica.view;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.OnRebindCallback;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.transition.TransitionManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Adapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import projetotcc.estudandoquimica.MainActivity;
import projetotcc.estudandoquimica.R;
import projetotcc.estudandoquimica.databinding.FragmentTurmaBinding;
import projetotcc.estudandoquimica.model.Turma;
import projetotcc.estudandoquimica.model.Usuario;
import projetotcc.estudandoquimica.view.turma.TurmaActivity;
import projetotcc.estudandoquimica.view.turma.TurmaAdapter;
import projetotcc.estudandoquimica.view.turma.TurmaClickListener;
import projetotcc.estudandoquimica.view.usuario.ListaUsuariosActivity;
import projetotcc.estudandoquimica.viewmodel.TurmaViewModel;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class TurmaFragment extends Fragment implements RecyclerView.OnItemTouchListener,
        View.OnClickListener, TurmaClickListener{

    private List<Turma> list;
    private FragmentTurmaBinding binding;
    private String nome;
    private TurmaAdapter adapter;
    private DatabaseReference ref;
    private TurmaViewModel viewModel;

    private int itemCount;
    private GestureDetectorCompat gestureDetector;
    private ActionMode actionMode;
    private RecyclerView recyclerView;
    private static final int DIALOG_REQUEST = 1;

    public TurmaFragment() {

    }

//    private void runLayoutAnimation(final RecyclerView recyclerView) {
//        final Context context = recyclerView.getContext();
//        final LayoutAnimationController controller =
//                AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation);
//
//        recyclerView.setLayoutAnimation(controller);
//        recyclerView.getAdapter().notifyDataSetChanged();
//        recyclerView.scheduleLayoutAnimation();
//    }
    public void setNome(String nome){

        this.nome = nome;
        adapter.addTurma(viewModel.inserir(nome), adapter.getItemCount());

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_turma, container,false);
        list = new ArrayList<>();
        //list.add(new Turma("Teste1", "13/09/2018 16:10:55", "Iury"));
        //list.add(new Turma("Teste2", "13/09/2018 16:10:55", "Iury"));
        ref = FirebaseDatabase.getInstance().getReference();

        recyclerView = binding.listaTurma;

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(inflater.getContext());
        layoutManager.scrollToPosition(0);
        //recyclerView.setLayoutManager(layoutManager);
        recyclerView.setLayoutManager(new WrapContentLinearLayoutManager(inflater.getContext(),
                LinearLayoutManager.VERTICAL, false));

        //recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());






        adapter = new TurmaAdapter(list, inflater.getContext(), this);

        recyclerView.setAdapter(adapter);
//        int resId = R.anim.layout_animation;
//        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(inflater.getContext(), resId);
//        recyclerView.setLayoutAnimation(animation);

//       recyclerView.addItemDecoration(
//                new DividerItemDecoration(this.getContext(), DividerItemDecoration.VERTICAL));


        recyclerView.addOnItemTouchListener(this);
        gestureDetector =
                new GestureDetectorCompat(this.getContext(), new RecyclerViewDemoOnGestureListener());

        viewModel = ViewModelProviders.of(this).get(TurmaViewModel.class);
        MutableLiveData<DataSnapshot> liveData = viewModel.getDataSnapshotLiveData();



        final String[] nome = new String[1];

        liveData.observe(this, new Observer<DataSnapshot>() {


            @Override
            public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                List<Turma> turmas = new ArrayList<>();
                final int[] c = {0};

                if (dataSnapshot.exists()) {

                    dataSnapshot.getRef().orderByChild("nome").addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot t, @Nullable String s) {
                            Turma turma = new Turma();
                            turma.setNome(t.child("nome").getValue(String.class));
                            turma.setId(t.getKey());
                            turma.setData_criacao(t.child("data_criacao").getValue(String.class));
                            turma.setAdministradorTurma(t.child("administradorTurma").getValue(String.class));
                            turma.setCodeTurma(t.child("codeTurma").getValue(String.class));
                            turma.setProfessor(new Turma.Professor(turma.getAdministradorTurma(), null));


                            final DatabaseReference reference =
                                    FirebaseDatabase.getInstance().getReference("usuarios/" +
                                            t.child("administradorTurma").getValue(String.class));

                                    reference
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            turma.setProfessor(new Turma.Professor(t.child("administradorTurma").
                                                    getValue(String.class), dataSnapshot.child("nome").getValue(String.class)));
                                           // turma.setAdministradorTurma(dataSnapshot.child("nome").getValue(String.class));
                                            adapter.notifyItemChanged(c[0]);
                                            c[0]++;


                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                            //turma.setAdministradorTurma(nome[0]);
                            turmas.add(turma);
                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }

                if(adapter.getItemCount() == 0){
                    adapter.updateTurmas(turmas);
                }

                binding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        adapter.updateTurmas(turmas);
                       // list.addAll(adapter.get);
                        binding.swipeRefreshLayout.setRefreshing(false);
                    }
                });

                binding.executePendingBindings();
            }
        });


        MainActivity activity = (MainActivity) getActivity();

        activity.getSupportActionBar().setTitle("Turmas");

        FloatingActionButton fab = binding.fab;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogCadastrarTurma(null);
                getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

            }
        });

       // setHasOptionsMenu(true);
        return binding.getRoot();
    }

//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//
//        inflater.inflate(R.menu.menu_conteudo, menu);
//        super.onCreateOptionsMenu(menu, inflater);
//
//    }

    public void DialogCadastrarTurma(Turma turma){

        Intent it = new Intent(getContext(), DialogTurmaActivity.class);

        if(turma != null){
            it.putExtra("nome", turma.getNome());
        }

        startActivityForResult(it, DIALOG_REQUEST);


//        final android.support.v4.app.DialogFragment dialogFragment = new CadastrarTurmaDialog();
//        dialogFragment.show(getFragmentManager(), "CadastrarTurmaDialog");

    }
    Turma turma;
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == DIALOG_REQUEST) {
            if(resultCode == RESULT_OK){
                String resultado = data.getStringExtra("resultado");
                int opcao = data.getIntExtra("opcao", 0);

                if(opcao == 1) {
                    viewModel.inserir(resultado);
                }else if(opcao == 2){

                    turma.setNome(resultado);
                    viewModel.atualizar(turma);
                }

            }
        }
        adapter.clearSelections();
    }



    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        gestureDetector.onTouchEvent(e);
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    @Override
    public void onClick(View v) {

        if(!(v instanceof CardView)){
            return;
        }

        int idx = recyclerView.getChildAdapterPosition(v);

        if (actionMode != null) {
            myToggleSelection(idx);
            return;
        }

    }

    private void myToggleSelection(int idx) {
        adapter.toggleSelection(idx);
        String title = String.valueOf(adapter.getSelectedItemCount());

        actionMode.setTitle(title);
        if(adapter.getSelectedItemCount() != 1) {
           // actionMode.getMenu().findItem(R.id.action_membros).setVisible(false);
            actionMode.getMenu().findItem(R.id.action_edit).setVisible(false);
        }else{
           // actionMode.getMenu().findItem(R.id.action_membros).setVisible(true);
            actionMode.getMenu().findItem(R.id.action_edit).setVisible(true);
        }

        if(adapter.getSelectedItemCount() == 0){

            actionMode.finish();
        }

    }

    private android.support.v7.view.ActionMode.Callback startActionMode = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {

            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_turma, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {


            switch (item.getItemId()){

                case R.id.action_delete:
                    List<Integer> selectedItemPositions = adapter.getSelectedItems();
                    int currPos;
                    for (int i = selectedItemPositions.size() - 1; i >= 0; i--) {
                        currPos = selectedItemPositions.get(i);
                        Turma t = adapter.getTurma(currPos);
                        t.setNome("Teste Tudo");
                        viewModel.deletar(t);
                        adapter.removerTurma(currPos);

                    }
                    actionMode.finish();

                    return true;
//                case R.id.action_membros:
//                    Intent it = new Intent(getContext(), ListaUsuariosActivity.class);
//
//                    if(adapter.getSelectedItems().size() == 1) {
//                        it.putExtra("idTurma", adapter.getTurma(
//                                adapter.getSelectedItems().get(0)).getId());
//                    }
//
//                    getActivity().startActivity(it);
//
//                    return true;
                case R.id.action_edit:

                    if(adapter.getSelectedItems().size() == 1){
                        turma = adapter.getTurma(adapter.getSelectedItems().get(0));
                        DialogCadastrarTurma(turma);
                    }
                    actionMode.finish();
                    return true;

                default:
                    return false;
            }

        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
            adapter.clearSelections();
        }

    };

    @Override
    public void onClick(Turma turma) {
        Intent it = new Intent(getContext(), TurmaActivity.class);
        it.putExtra("idTurma", turma.getId());
        it.putExtra("nome", turma.getNome());
        getActivity().startActivity(it);
    }

    @Override
    public void onCompartilheCodigo(Turma turma) {
        Intent compartilha = new Intent(Intent.ACTION_SEND);
        compartilha.setType("text/plain");
        compartilha.putExtra(Intent.EXTRA_SUBJECT, "Compartilhar código da turma");

        compartilha.putExtra(Intent.EXTRA_TEXT,
                "Oiii!, você foi escolhido para fazer parte do nosso grupo de estudo com conteúdos voltados para a química. Baixe nosso" +
                        " aplicativo no link abaixo e insira o código da turma.\n\n " +
                        "Link para baixar o app: \n\n" +
                        "Código turma:" + turma.getCodeTurma());

        startActivity(Intent.createChooser(compartilha, "Compartilhar link!"));
    }

    private class RecyclerViewDemoOnGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            View view = recyclerView.findChildViewUnder(e.getX(), e.getY());
            onClick(view);
            return super.onSingleTapConfirmed(e);
        }

        public void onLongPress(MotionEvent e) {
            View view = recyclerView.findChildViewUnder(e.getX(), e.getY());

            if(!(view instanceof CardView)){
                return;
            }

            if (actionMode != null) {
                return;
            }

            actionMode =  ((MainActivity) TurmaFragment.this.getActivity()).startSupportActionMode(startActionMode);

            int idx = recyclerView.getChildAdapterPosition(view);
            myToggleSelection(idx);
            super.onLongPress(e);
        }
    }

    public class WrapContentLinearLayoutManager extends LinearLayoutManager {

        public WrapContentLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);
        }

        @Override
        public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
            try {
                super.onLayoutChildren(recycler, state);
            } catch (IndexOutOfBoundsException e) {
                Log.e("probe", "meet a IOOBE in RecyclerView");
            }
        }
    }
}
