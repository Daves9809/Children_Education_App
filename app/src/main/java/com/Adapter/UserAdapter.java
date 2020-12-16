package com.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ChildrenEducationApp.R;

import java.util.ArrayList;

public class UserAdapter extends ArrayAdapter<User> {

    public UserAdapter(Context context, ArrayList<User> users) {
        super(context, 0,users);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_item, parent, false);
        }

        User currentUser = getItem(position);
        if(currentUser !=null){
            TextView userPosition = (TextView) listItemView.findViewById(R.id.user_position);

            userPosition.setText(String.valueOf(position + 1));

            TextView userName = (TextView) listItemView.findViewById(R.id.user_name);

            userName.setText(currentUser.getName());

            TextView userPoziom = (TextView) listItemView.findViewById(R.id.user_poziom);
            userPoziom.setText(String.valueOf(currentUser.getPoziom()));

            TextView userPoints = (TextView) listItemView.findViewById(R.id.user_points);
            userPoints.setText(String.valueOf(currentUser.getPoints()));

        }

        return listItemView;
    }
}
