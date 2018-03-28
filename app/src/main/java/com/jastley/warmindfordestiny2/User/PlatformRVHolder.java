package com.jastley.warmindfordestiny2.User;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jastley.warmindfordestiny2.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by jastl on 23/03/2018.
 */

public class PlatformRVHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    @BindView(R.id.platform_row_icon) ImageView platformIcon;
    @BindView(R.id.platform_name_row) TextView platformName;
    @BindView(R.id.hidden_platform_type) TextView platformType;
    protected View mRootView;
    Context context;

    public PlatformRVHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        mRootView = itemView;
        itemView.setOnClickListener(this);
    }



    public void setPlatformIcon(String icon, Context context) {

        if (icon.equals("2")) {
            platformIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.icon_psn));
        }
        else if (icon.equals("1")) {
            platformIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.icon_xbl));
        }
        else if (icon.equals("4")) {
            platformIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.icon_blizzard));
        }
    }

    public void setPlatformName(String platform) {
        platformName.setText(platform);
    }

    //Hidden field to get platformType out of onClick
    public void setPlatformType(String type) {
        platformType.setText(type);
    }

    @Override
    public void onClick(View view) {
        Toast.makeText(context.getApplicationContext(), this.platformType.getText(), Toast.LENGTH_SHORT).show();
    }
}
