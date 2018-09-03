package projetotcc.estudandoquimica.view.offline.ViewPagerCard;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import projetotcc.estudandoquimica.interfaces.CardAdapter;
import projetotcc.estudandoquimica.model.ConteudoOffline;
import projetotcc.estudandoquimica.R;
import projetotcc.estudandoquimica.view.offline.ConteudoActivity;
import projetotcc.estudandoquimica.view.offline.SubConteudoActivity;

public class CardPagerAdapter extends PagerAdapter implements CardAdapter, Button.OnClickListener {

    private List<CardView> cardViews;
    private List<CardItem> cardItems;
    private float mBaseElevation;
    private SubConteudoActivity subConteudoActivity;

    public CardPagerAdapter(SubConteudoActivity subConteudoActivity) {
        this.subConteudoActivity = subConteudoActivity;
        cardItems = new ArrayList<>();
        cardViews = new ArrayList<>();
    }

    public void addCardItem(CardItem item){
        cardViews.add(null);
        cardItems.add(item);
    }

    private void bind(CardItem item, View view){

        TextView tituloTextView = (TextView) view.findViewById(R.id.titleTextView);
        TextView contentTextView = (TextView) view.findViewById(R.id.contentTextView);
        tituloTextView.setText(item.getTitle());
        contentTextView.setText(item.getText());
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull final ViewGroup container, int position) {

        View view = LayoutInflater.from(container.getContext()).inflate(R.layout.conteudo_offline_item, container, false);
        container.addView(view);
        bind(cardItems.get(position), view);
        CardView cardView = (CardView) view.findViewById(R.id.cardView);

        if(mBaseElevation == 0){
            mBaseElevation = cardView.getCardElevation();
        }

        cardView.setMaxCardElevation(mBaseElevation * MAX_ELEVATION_FACTOR);
        cardViews.set(position, cardView);
        Button botaoCardView = view.findViewById(R.id.botaoConteudo);
        botaoCardView.setOnClickListener(this);

        return view;

    }


    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
        cardViews.set(position, null);

    }

    @Override
    public float getBaseElevation() {
        return mBaseElevation;
    }

    @Override
    public CardView getCardViewAt(int position) {
        return cardViews.get(position);
    }

    @Override
    public int getCount() {
        return cardItems.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public void onClick(View v) {

        CardView cardView = getCardViewAt(subConteudoActivity.getViewPager().getCurrentItem());

        ConteudoOffline conteudoOffline = new ConteudoOffline();

        TextView titulo = cardView.findViewById(R.id.titleTextView);
        TextView assunto = cardView.findViewById(R.id.contentTextView);

        conteudoOffline.setAssunto(assunto.getText().toString());
        conteudoOffline.setTitulo(titulo.getText().toString());

        Intent it = new Intent(subConteudoActivity.getApplicationContext(), ConteudoActivity.class);
        it.putExtra("conteudo", conteudoOffline);
        subConteudoActivity.startActivity(it);
    }

}
