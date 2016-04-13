package com.rxj.mymusic.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.rxj.mymusic.R;
import com.rxj.mymusic.activity.DeleteGeDanActivity;
import com.rxj.mymusic.entity.dbEntity;

import java.util.List;

/**
 * Created by Android on 2016/1/20.
 */
public class ShanChuAdapter extends BaseAdapter {
    private Context context;
    private List<dbEntity> list;

    public ShanChuAdapter(Context context, List<dbEntity> list) {
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
        ViewHolder3 vh=null;
        final dbEntity entity=list.get(position);
        if(convertView==null){
            convertView= LayoutInflater.from(context).inflate(R.layout.item_scd,null);
            vh=new ViewHolder3(convertView);
        }else {
            vh= ((ViewHolder3) convertView.getTag());
        }
        vh.setObj(entity);
        convertView.setTag(vh);
        vh.name.setText(entity.getName());
        if (entity.getIds()!=null){
            String[] split = entity.getIds().split(",");
            vh.num.setText("(共" + split.length + "首)");
        }
        vh.cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    DeleteGeDanActivity.delete_list.add(entity);
                }else {
                    if(DeleteGeDanActivity.delete_list.contains(entity)){
                        DeleteGeDanActivity.delete_list.remove(entity);
                    }
                }
            }
        });
        return convertView;
    }

    public class ViewHolder3{
        private Object obj;
        private CheckBox cb;
        private TextView name;
        private TextView num;
        private RelativeLayout layout;

        public ViewHolder3(View itemView){
            cb= ((CheckBox) itemView.findViewById(R.id.cb));
            name= ((TextView) itemView.findViewById(R.id.name));
            num= ((TextView) itemView.findViewById(R.id.num));
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
