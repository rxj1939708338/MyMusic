package com.rxj.mymusic.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.rxj.mymusic.R;
import com.rxj.mymusic.entity.Entity;

import java.util.List;

/**
 * Created by Android on 2016/1/15.
 */
public class MusicListAdapter extends BaseAdapter {
    private Context context;
    private List<Entity> list;
    private int currentId_position;
    private onClick click;

    public void setCurrentId_position(int currentId_position) {
        this.currentId_position = currentId_position;
    }

    public MusicListAdapter(Context context, List<Entity> list, onClick click) {
        this.context = context;
        this.list = list;
        this.click=click;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        final Entity entity=list.get(position);
        if(convertView==null){
            convertView= LayoutInflater.from(context).inflate(R.layout.item,null);
            vh=new ViewHolder(convertView);
        }else {
            vh= ((ViewHolder) convertView.getTag());
        }
        vh.setObj(entity);
        convertView.setTag(vh);
        vh.tv.setText(entity.getName());
        vh.iv.setImageResource(R.mipmap.icon_02);
        vh.iv.setTag(position);
        vh.iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (int) v.getTag();
                click.onWeightClick(position);
            }
        });
        vh.v.setVisibility(currentId_position == position ? View.VISIBLE : View.INVISIBLE);
        vh.layout.setBackgroundColor(currentId_position == position ? Color.argb(100, 255, 228, 196) : Color.argb(100, 152, 245, 255));
        vh.right_layout.setTag(position);
        vh.right_layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int position = (int) v.getTag();
                click.onLongClick(position);
                return true;
            }
        });
//        vh.right_layout.setTag(position);
        vh.right_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (int) v.getTag();
                click.onClick(position);
            }
        });
        return convertView;
    }

    public interface onClick{
        void onWeightClick(int position);
        void onLongClick(int position);
        void onClick(int position);
    }

    public class ViewHolder{
        private Object obj;
        private ImageView iv;
        private TextView tv;
        private View v;
        private LinearLayout layout;
        private LinearLayout right_layout;

        public ViewHolder(View itemView){
            iv= ((ImageView) itemView.findViewById(R.id.item_image));
            tv= ((TextView) itemView.findViewById(R.id.item_title));
            v=itemView.findViewById(R.id.current);
            layout= ((LinearLayout) itemView.findViewById(R.id.item_layout));
            right_layout=((LinearLayout) itemView.findViewById(R.id.item_right));
        }

        public void setObj(Object obj){
            this.obj=obj;
        }

        public Object getObj() {
            return obj;
        }
    }
}
