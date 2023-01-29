package com.cursoandroid.uber.adapter;

import android.view.View;
import androidx.recyclerview.widget.RecyclerView;

public class BaseViewHolder<T> extends RecyclerView.ViewHolder {

    public interface OnViewBinding <T>{
        void bind(T t, View itemView);
    }
    private final OnViewBinding<T> binding;

    public BaseViewHolder(View itemView, OnViewBinding<T> binding){
        super(itemView);
        this.binding = binding;
    }

    public void bind(T t){
        binding.bind(t, itemView);
    }
}
