package com.jastley.warmindfordestiny2.Vendors.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.jastley.warmindfordestiny2.Characters.models.InventoryItemModel;
import com.jastley.warmindfordestiny2.R;
import com.jastley.warmindfordestiny2.Vendors.holders.XurItemsViewHolder;

import java.util.List;

public class XurItemsRecyclerAdapter extends RecyclerView.Adapter<XurItemsViewHolder> {

    private Context mContext;
    List<InventoryItemModel> xurItems;

    public XurItemsRecyclerAdapter(Context mContext, List<InventoryItemModel> xurItems) {
        this.mContext = mContext;
        this.xurItems = xurItems;
    }

    @Override
    public XurItemsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.xur_inventory_row, parent, false);

        return new XurItemsViewHolder(mView);
    }

    @Override
    public void onBindViewHolder(XurItemsViewHolder holder, int position) {
        holder.setItemName(xurItems.get(position).getItemName());
        holder.setItemIcon(xurItems.get(position).getItemIcon(), mContext);
        holder.setItemType(xurItems.get(position).getItemTypeDisplayName());
        holder.setItemType(xurItems.get(position).getItemTypeDisplayName());
        holder.setItemCostImage(xurItems.get(position).getCostItemIcon(), mContext);
        holder.setItemCostText(xurItems.get(position).getCostsQuantity());
    }

    @Override
    public int getItemCount() {
        return xurItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
}
