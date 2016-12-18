package com.fengqi.motions;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import static android.support.v7.recyclerview.R.styleable.RecyclerView;

/**
 * Created by fengqi on 15-06-22.
 */
public class MotionsAdapter extends  RecyclerView.Adapter<MotionsAdapter.MotionViewHolder> {
    class MotionViewHolder extends RecyclerView.ViewHolder
    {
        TextView tv;
        ImageView iv;

        public MotionViewHolder(View view)
        {
            super(view);
            tv = (TextView) view.findViewById(R.id.id_desc);
            iv = (ImageView) view.findViewById(R.id.id_thumb);
        }
    }

    private List<Motion> itemList;
    private Context context;

    public MotionsAdapter(Context context, List<Motion> itemList)
    {
        this.itemList = itemList;
        this.context = context;
    }

    @Override
    public MotionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.motion_item, null);
        MotionViewHolder rcv = new MotionViewHolder(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(MotionViewHolder holder, final int position) {
        holder.tv.setText(itemList.get(position).getDescription());
        holder.iv.setImageDrawable(itemList.get(position).getDrawable());

        holder.iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, HomeActivity.class);
                intent.putExtra("OBJECT", itemList.get(position));
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.itemList.size();
    }
}
