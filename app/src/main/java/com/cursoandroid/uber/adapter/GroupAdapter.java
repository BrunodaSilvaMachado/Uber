package com.cursoandroid.uber.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GroupAdapter<T> extends RecyclerView.Adapter<BaseViewHolder<T>> {

    public interface Layout{
        @LayoutRes
        int getLayout(int viewType);
    }
    public interface OnItemClickListener <T>{
        void onItemClick(T t);
    }
    private List<T> itemList;
    private final Layout layout;
    private final BaseViewHolder.OnViewBinding<T> viewBinding;
    private OnItemClickListener<T> onItemClickListener;

    public GroupAdapter(List<T> itemList, Layout layout, BaseViewHolder.OnViewBinding<T> viewBinding){
        this.itemList = itemList;
        this.layout = layout;
        this.viewBinding = viewBinding;
        this.onItemClickListener = null;
    }

    @NonNull
    @NotNull
    @Override
    public BaseViewHolder<T> onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(layout.getLayout(viewType),parent,false );
        return new BaseViewHolder<>(view,viewBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull BaseViewHolder<T> holder, int position) {
        final T t = itemList.get(position);
        holder.bind(t);
        if (onItemClickListener != null) {
            holder.itemView.setOnClickListener(l->onItemClickListener.onItemClick(t));
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public void addItem(T t){
        itemList.add(t);
        notifyItemInserted(getItemCount());
    }

    @SuppressLint("NotifyDataSetChanged")
    public void addAllItem(List<T> itemList){
        this.itemList = itemList;
        notifyDataSetChanged();
    }

    public List<T> getItemList(){
        return itemList;
    }

    public void setOnItemClickListener(OnItemClickListener<T> onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
