package projetotcc.estudandoquimica.view;


import android.app.DialogFragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import projetotcc.estudandoquimica.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class AgendaFragment extends Fragment{


    public AgendaFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment



        return inflater.inflate(R.layout.fragment_agenda, container, false);

    }


}
