package com.chico_ent.layout;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

/**
 * Created by amarj on 8/12/2017.
 */

public class dealsArrayAdapter extends ArrayAdapter<Business> {

    public dealsArrayAdapter(Context context, ArrayList<Business> deals) {
        super(context, 0, deals);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Business deal = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.deals_list_item, parent, false);
        }

        String name = "";

        if (deal.name != null) {
            name += deal.name;
        }

        if (deal.description != null) {
            name += ": ";
            name += deal.description;
        }

        if (deal.location != null && !deal.location.equals("")) {
            convertView.findViewById(R.id.location).setVisibility(View.VISIBLE);
            ((TextView) convertView.findViewById(R.id.location)).setText(deal.location);
        }
        else
            convertView.findViewById(R.id.location).setVisibility(View.GONE);

        if (deal.type != null && deal.type.equals("flash-deals")) {
            name = "\uD83D\uDD25" + name;
        }

        ((TextView) convertView.findViewById(R.id.name)).setText(name);

        if (deal.picture != null && !deal.picture.equals("")) {
            ImageLoader imageLoader = ImageLoader.getInstance(); // Get singleton instance
            imageLoader.displayImage(deal.picture, (ImageView) convertView.findViewById(R.id.background));


            /*new DownloadImageTask((ImageView) convertView.findViewById(R.id.background))
                    .execute(business.picture);*/
        }

        return convertView;
    }
}
