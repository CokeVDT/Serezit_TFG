package com.example.tfg.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tfg.Activity.CartActivity;
import com.example.tfg.Domain.ItemsModel;
import com.example.tfg.Helper.ChangeNumberItemsListener;
import com.example.tfg.Helper.ManagmentCart;
import com.example.tfg.databinding.ViewholderCartBinding;

import java.util.ArrayList;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.Viewholder> {
    private ArrayList<ItemsModel> listItemSelected;
    private ChangeNumberItemsListener changeNumberItemsListener;
    private ManagmentCart managmentCart;

    public CartAdapter(ArrayList<ItemsModel> listItemSelected, Context context, ChangeNumberItemsListener changeNumberItemsListener) {
        this.listItemSelected = listItemSelected;
        this.changeNumberItemsListener = changeNumberItemsListener;
        this.managmentCart = new ManagmentCart(context);
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderCartBinding binding = ViewholderCartBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new Viewholder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
        ItemsModel item = listItemSelected.get(position);

        holder.binding.titleTxt.setText(item.getTitle());
        holder.binding.feeEachItem.setText("$" + item.getPrice());
        holder.binding.totalEachItem.setText("$" + Math.round(item.getNumberinCart() * item.getPrice()));
        holder.binding.numberItemTxt.setText(String.valueOf(item.getNumberinCart()));

        Glide.with(holder.itemView.getContext())
                .load(item.getPicUrl().get(0))
                .into(holder.binding.pic);

        // Botón Plus
        holder.binding.plusCartBtn.setOnClickListener(v -> {
            managmentCart.plusItem(listItemSelected, position, () -> {
                notifyItemChanged(position);
                changeNumberItemsListener.changed();
            });
        });

        // Botón Minus
        holder.binding.minusCartBtn.setOnClickListener(v -> {
            managmentCart.minusItem(listItemSelected, position, () -> {
                if (item.getNumberinCart() == 0) {
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, listItemSelected.size());
                } else {
                    notifyItemChanged(position);
                }
                changeNumberItemsListener.changed();
            });
        });
    }

    @Override
    public int getItemCount() {
        return listItemSelected.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder {
        ViewholderCartBinding binding;

        public Viewholder(ViewholderCartBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
