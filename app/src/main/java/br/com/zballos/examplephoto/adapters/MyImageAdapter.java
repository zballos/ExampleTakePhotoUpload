package br.com.zballos.examplephoto.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import br.com.zballos.examplephoto.R;
import br.com.zballos.examplephoto.model.MyImage;

/**
 * Created by zballos on 06/05/17.
 */

public class MyImageAdapter extends RecyclerView.Adapter<MyImageAdapter.MyViewHolder>{
    private Context mContext;
    private List<MyImage> mList;
    private LayoutInflater mLayoutInflater;
    private float scale;
    private int width, height, roundPixels;

    private boolean withAnimation;
    private boolean withCardLayout;

    public MyImageAdapter(Context c, List<MyImage> l){
        this(c, l, true, true);
    }

    public MyImageAdapter(Context c, List<MyImage> l, boolean wa, boolean wcl){
        mContext = c;
        mList = l;
        mLayoutInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        withAnimation = wa;
        withCardLayout = wcl;

        scale = mContext.getResources().getDisplayMetrics().density;
        width = mContext.getResources().getDisplayMetrics().widthPixels - (int)(14 * scale + 0.5f);
        height = (width / 16) * 9;

        roundPixels = (int)(2 * scale + 0.5f);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View mView;

        if(withCardLayout){
            mView = mLayoutInflater.inflate(R.layout.card_image, parent, false);
        } else {
            mView = mLayoutInflater.inflate(R.layout.card_image, parent, false);
        }

        MyViewHolder viewHolder = new MyViewHolder(mView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.tvTitle.setText(mList.get(position).getTitle());

        BitmapFactory.Options options = new BitmapFactory.Options();
        //options.inSampleSize = 8;
        //options.inJustDecodeBounds = true;

        final Bitmap bitmap = BitmapFactory.decodeFile(mList.get(position).getPathName(),
                options);

        holder.ivPicture.setImageBitmap(bitmap);
    }

    public void addListItem(MyImage myImage, int position){
        mList.add(myImage);
        notifyItemInserted(position);
    }


    public void removeListItem(int position){
        mList.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        public TextView tvTitle;
        public ImageView ivPicture;

        public MyViewHolder(View itemView) {
            super(itemView);

            tvTitle = (TextView) itemView.findViewById(R.id.tvPhoto);
            ivPicture = (ImageView) itemView.findViewById(R.id.ivPhoto);

        }

        /*@Override
        public void onClick(View v) {
            if(mRecyclerViewOnClickListenerHack != null){
                mRecyclerViewOnClickListenerHack.onClickListener(v, getPosition());
            }
        }*/
    }
}
