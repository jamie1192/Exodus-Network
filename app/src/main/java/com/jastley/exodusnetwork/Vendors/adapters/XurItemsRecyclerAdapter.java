package com.jastley.exodusnetwork.Vendors.adapters;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jastley.exodusnetwork.R;
import com.jastley.exodusnetwork.Vendors.holders.XurItemsViewHolder;
import com.jastley.exodusnetwork.api.models.XurSaleItemModel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class XurItemsRecyclerAdapter extends RecyclerView.Adapter<XurItemsViewHolder> {

    private Context mContext;
    private List<XurSaleItemModel> xurItems = new ArrayList<>();

    public final PublishSubject<String> onClickSubject = PublishSubject.create();

    public XurItemsRecyclerAdapter(Context mContext) {
        this.mContext = mContext;

    }

    @Override
    public XurItemsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.xur_inventory_row, parent, false);

        final XurItemsViewHolder xurItemsViewHolder = new XurItemsViewHolder(mView, parent.getContext());

        xurItemsViewHolder.itemView.setOnClickListener(view -> {
            onClickSubject.onNext(xurItemsViewHolder.getItemHash());
        });

        return xurItemsViewHolder;
    }

    @Override
    public void onBindViewHolder(XurItemsViewHolder holder, int position) {

        holder.setItemName(xurItems.get(position).getItemData().getDisplayProperties().getName());
        holder.setItemIcon(xurItems.get(position).getItemData().getDisplayProperties().getIcon());
        holder.setItemType(xurItems.get(position).getItemData().getItemTypeDisplayName());
//        holder.setSalesCount(xurItems.get(position).getSalesData().getSalesCount()); //Braytech
        holder.setItemHash(xurItems.get(position).getItemData().getHash());
        if(!xurItems.get(position).getSalesData().getCostsList().isEmpty()) {
            holder.setItemCostImage(xurItems.get(position).getItemCost().getDisplayProperties().getIcon());
            holder.setItemCostText(xurItems.get(position).getSalesData().getCostsList().get(0).getQuantity());
        }

    }

    public Observable<String> getClickedItem() {
        return onClickSubject;
    }

    @Override
    public int getItemCount() {
        return xurItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public void setXurItems(List<XurSaleItemModel> items) {
        xurItems = items;
        notifyDataSetChanged();
    }
}
