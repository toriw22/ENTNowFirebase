package com.chico_ent.layout;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

/**
 * Created by amarj on 7/18/2017.
 */

public class businessesArrayAdapter extends ArrayAdapter<Business> {

    public businessesArrayAdapter(Context context, ArrayList<Business> businesses) {
        super(context, 0, businesses);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Business business = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_business_list_item, parent, false);
        }

        if (business.name != null && !business.name.equals(""))
            ((TextView) convertView.findViewById(R.id.name)).setText(business.name);
        else
            convertView.findViewById(R.id.name).setVisibility(View.GONE);

        if (business.time != null && !business.time.equals("")) {
            convertView.findViewById(R.id.time).setVisibility(View.VISIBLE);
            ((TextView) convertView.findViewById(R.id.time)).setText(business.time);
        }
        else
            convertView.findViewById(R.id.time).setVisibility(View.GONE);

        /*if (business.description != null && !business.description.equals("")) {
            convertView.findViewById(R.id.description).setVisibility(View.VISIBLE);
            ((TextView) convertView.findViewById(R.id.description)).setText(business.description);
        }
        else
            convertView.findViewById(R.id.description).setVisibility(View.GONE);*/

        /*if (business.location != null && !business.location.equals("")) {
            convertView.findViewById(R.id.location).setVisibility(View.VISIBLE);
            ((TextView) convertView.findViewById(R.id.location)).setText(business.location);
        }
        else
            convertView.findViewById(R.id.location).setVisibility(View.GONE);*/

        convertView.findViewById(R.id.location).setVisibility(View.GONE);

        if (business.points != 0) {
            convertView.findViewById(R.id.points).setVisibility(View.VISIBLE);
            if (business.type.equals("businesses"))
                ((TextView) convertView.findViewById(R.id.points)).setText("You have earned " + String.valueOf(business.points) + " points here.");
            else
                ((TextView) convertView.findViewById(R.id.points)).setText("This deal costs " + String.valueOf(business.points) + " points.");

        }
        else
            convertView.findViewById(R.id.points).setVisibility(View.GONE);


        if (business.picture != null && !business.picture.equals("")) {
            ImageLoader imageLoader = ImageLoader.getInstance(); // Get singleton instance
            imageLoader.displayImage(business.picture, (ImageView) convertView.findViewById(R.id.background));


            /*new DownloadImageTask((ImageView) convertView.findViewById(R.id.background))
                    .execute(business.picture);*/
        }
        else {
            convertView.findViewById(R.id.background).setVisibility(View.GONE);
        }

        return convertView;
    }
}