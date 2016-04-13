package com.rxj.mymusic.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.rxj.mymusic.R;
import com.rxj.mymusic.entity.SheZhiEntity;

import java.util.List;

/**
 * Created by Android on 2016/1/20.
 */
public class SheZhiAdapter extends BaseAdapter{
    private Context context;
    private List<SheZhiEntity> list;

    public SheZhiAdapter(Context context, List<SheZhiEntity> list) {
        this.context = context;
        this.list = list;
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
        ViewHolder2 vh=null;
        final SheZhiEntity entity=list.get(position);
        if(convertView==null){
            convertView= LayoutInflater.from(context).inflate(R.layout.item_gd,null);
            vh=new ViewHolder2(convertView);
        }else {
            vh= ((ViewHolder2) convertView.getTag());
        }
        vh.setObj(entity);
        convertView.setTag(vh);
        vh.tv.setText(entity.getName());
        if(entity.getPic()!=null){
            vh.iv.setImageBitmap(entity.getPic());
        }else if(entity.getImg_url()!=null){
            //下载图片吧
        }else{
            vh.iv.setImageResource(R.mipmap.ic_launcher);
        }
        vh.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                entity.getClicks().onClickListener();
            }
        });
        return convertView;
    }

    public class ViewHolder2{
        private Object obj;
        private ImageView iv;
        private TextView tv;
        private RelativeLayout layout;

        public ViewHolder2(View itemView){
            iv= ((ImageView) itemView.findViewById(R.id.item_gd_iv));
            tv= ((TextView) itemView.findViewById(R.id.item_gd_tv));
            layout= ((RelativeLayout) itemView.findViewById(R.id.item_layout));
        }

        public void setObj(Object obj){
            this.obj=obj;
        }

        public Object getObj() {
            return obj;
        }
    }
}
