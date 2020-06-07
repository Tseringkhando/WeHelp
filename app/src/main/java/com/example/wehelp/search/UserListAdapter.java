package com.example.wehelp.search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wehelp.R;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserViewHolder>implements Filterable {
    private List<SearchUsersModel> userlist;
    private List<SearchUsersModel> allUsersList;
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
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        SearchUsersModel currentItem = userlist.get(position);
//        holder.search_user_image.setImageResource(currentItem.getProfile_image());
        String fullname = currentItem.getFirstname()+ " " + currentItem.getLastname();
        holder.search_item_title.setText(fullname);
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
        private TextView search_item_title;
        private CircleImageView search_user_image;
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            search_item_title=itemView.findViewById(R.id.search_res_title);
            search_user_image=itemView.findViewById(R.id.search_profile_pic);

        }
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
                    if (item.getFirstname().toLowerCase().contains(filterPattern) || item.getLastname().toLowerCase().contains(filterPattern)) {
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
