package com.hfjs.amapcardemo.adapter;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.hfjs.amapcardemo.R;

import java.util.List;

public class ClassAdapter extends BaseQuickAdapter<Class,BaseViewHolder> {
    public ClassAdapter(int layoutResId, @Nullable List<Class> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, Class item) {
                helper.setText(R.id.tv_class,item.getSimpleName());
    }
}
