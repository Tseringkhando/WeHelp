package com.example.wehelp.search;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wehelp.HomeFragment;
import com.example.wehelp.R;
import com.example.wehelp.User_profile;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserViewHolder>implements Filterable {
    private List<SearchUsersModel> userlist;
    private List<SearchUsersModel> allUsersList;
    private  Context context;
    UserListAdapter(List<SearchUsersModel> userlist){
        this.userlist=userlist;
        allUsersList= new ArrayList<>(userlist);
    }
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_search_res_single,
                parent, false);
        return new UserViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final UserViewHolder holder, int position) {
        context=holder.itemView.getContext();
        SearchUsersModel currentItem = userlist.get(position);
       final String userid = currentItem.getUser_id();
//        holder.search_user_image.setImageResource(currentItem.getProfile_image());
        String fullname = currentItem.getFirstname()+ " " + currentItem.getLastname();

        holder.search_item_title.setText(fullname);
        holder.search_item_email.setText(currentItem.getEmail());
        String profile_image = currentItem.getProfile_image();
        Uri profile_image_uri = Uri.parse(profile_image);
        Picasso.with(holder.search_user_image.getContext()).load(profile_image_uri).fit().placeholder(R.drawable.default_profile_img)
                .into(holder.search_user_image);
        holder.usercard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                holder.usercard.setBackgroundColor(Color.parseColor("#ebebeb"));
                openUserProfile(userid);
            }

        });
    }

    private void openUserProfile(String userid) {
        Intent myprofile= new Intent(context, User_profile.class);
        myprofile.putExtra("user_id", userid);
        context.startActivity(myprofile);
    }

    @Override
    public int getItemCount() {
        return userlist.size();
    }

    @Override
    public Filter getFilter() {
        return userFilter;
    }

    public class UserViewHolder extends RecyclerView.ViewHolder{
        private TextView search_item_title, search_item_email;
        private CircleImageView search_user_image;
        private CardView usercard;
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            usercard= itemView.findViewById(R.id.searchuserdata_card);
            search_item_title=itemView.findViewById(R.id.search_res_title);
            search_user_image=itemView.findViewById(R.id.search_profile_pic);
            search_item_email = itemView.findViewById(R.id.search_res_email);

        }
        public Context getContext() {return itemView.getContext();}
    }


    // filtering the user lists
    private Filter userFilter= new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            System.out.println(constraint);
            List<SearchUsersModel> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(allUsersList);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (SearchUsersModel item : allUsersList) {
                    if (item.getFirstname().toLowerCase().contains(filterPattern) ||
                            item.getLastname().toLowerCase().contains(filterPattern) ||
                    item.getEmail().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                        System.out.println(item);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            userlist.clear();
            userlist.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };
}
