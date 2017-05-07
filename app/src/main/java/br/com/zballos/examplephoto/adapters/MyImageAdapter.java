package br.com.zballos.examplephoto.adapters;

import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import br.com.zballos.examplephoto.R;
import br.com.zballos.examplephoto.model.MyImage;

/**
 * Created by zballos on 06/05/17.
 */

public class MyImageAdapter extends RecyclerView.Adapter<MyImageAdapter.MyViewHolder> {
    private Context mContext;
    private List<MyImage> mList;
    private LayoutInflater mLayoutInflater;

    public MyImageAdapter(Context context, List<MyImage> list) {
        mContext = context;
        mList = list;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View mView = mLayoutInflater.inflate(R.layout.card_image, parent, false);

        MyViewHolder viewHolder = new MyViewHolder(mView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        holder.tvTitle.setText(mList.get(position).getTitle());

        Glide.with(mContext)
            .load(mList.get(position).getPathName())
            .centerCrop()
            .crossFade()
            .into(holder.ivPicture);

        holder.ibMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(v, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle;
        private ImageView ivPicture;
        private ImageButton ibMenu;

        public MyViewHolder(View itemView) {
            super(itemView);
            tvTitle = (TextView) itemView.findViewById(R.id.tvPhoto);
            ivPicture = (ImageView) itemView.findViewById(R.id.ivPhoto);
            ibMenu = (ImageButton) itemView.findViewById(R.id.imageButton);
        }
    }

    private void showPopupMenu(View view, int position) {
        PopupMenu popup = new PopupMenu(view.getContext(), view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_card_image, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.delete:
                        // TODO: ACTION DELETE
                        Log.e("Click", "Menu delete");
                        break;
                    case R.id.sendToWeb:
                        // TODO: MAKE ACTION SEND TO WEB_ADMIN
                        Log.e("Click", "Menu sincroize");
                        break;
                }

                return false;
            }
        });
        popup.show();
    }
}
