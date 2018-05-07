package com.jastley.warmindfordestiny2.Characters.holders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.jastley.warmindfordestiny2.R;
import com.squareup.picasso.Picasso;

import static com.jastley.warmindfordestiny2.api.BungieAPI.baseURL;

public class EquipItemViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.equip_emblem_icon) ImageView emblemIcon;

    private String characterId;
    private String characterLevel;
    private String imageURL;

    public EquipItemViewHolder(View itemView) {
        super(itemView);

        ButterKnife.bind(this, itemView);
    }

    public ImageView getEmblemIcon() {
        return emblemIcon;
    }

    public void setEmblemIcon(String iconUrl, Context context) {

        this.imageURL = iconUrl;

        Picasso.with(context)
                .load(baseURL + iconUrl)
                .into(this.emblemIcon);
    }

    public String getCharacterId() {
        return characterId;
    }

    public void setCharacterId(String characterId) {
        this.characterId = characterId;
    }

    public String getCharacterLevel() {
        return characterLevel;
    }

    public void setCharacterLevel(String characterLevel) {
        this.characterLevel = characterLevel;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
}
