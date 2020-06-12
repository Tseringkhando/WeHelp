package com.example.wehelp.admin.search;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wehelp.R;
import com.example.wehelp.User_profile;
import com.example.wehelp.admin.Admin_user_detail_single;
import com.example.wehelp.search.SearchUsersModel;
import com.example.wehelp.search.UserListAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.UserViewHolder>implements Filterable {
    private List<SearchUsersModel> userlist;
    private List<SearchUsersModel> allUsersList;
    private Context context;
    private FirebaseFirestore firestore= FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth=FirebaseAuth.getInstance();

    AdminUserAdapter(List<SearchUsersModel> userlist){
        this.userlist=userlist;
        allUsersList= new ArrayList<>(userlist);
    }

    public AdminUserAdapter() {

    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_search_res_single,
                parent, false);
        return new UserViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminUserAdapter.UserViewHolder holder, int position) {
        context=holder.itemView.getContext();
        SearchUsersModel currentItem = userlist.get(position);
        final String user_id = currentItem.getUser_id();
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
                openUserProfile(user_id);
            }

        });

        //delete user
        holder.btn_del_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(R.string.app_name);
                builder.setIcon(R.drawable.ic_launcher);
                builder.setMessage("Are you sure to delete user?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                firestore.collection("users").document(user_id)
                                        .delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(context,"User Deleted",Toast.LENGTH_LONG).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(context,"Error!!!",Toast.LENGTH_LONG).show();
                                            }
                                        });
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return userlist.size();
    }

    private void openUserProfile(String userid) {
        Intent userProfile= new Intent(context, Admin_user_detail_single.class);
        userProfile.putExtra("user_id", userid);
        context.startActivity(userProfile);
    }

    @Override
    public Filter getFilter() {
        return userFilter;
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {
            private TextView search_item_title, search_item_email;
            private CircleImageView search_user_image;
            private CardView usercard;
            private Button btn_del_user;
        public UserViewHolder(@NonNull View itemView) {
                super(itemView);
                usercard= itemView.findViewById(R.id.searchuserdata_card);
                search_item_title=itemView.findViewById(R.id.search_res_title);
                search_user_image=itemView.findViewById(R.id.search_profile_pic);
                search_item_email = itemView.findViewById(R.id.search_res_email);
                btn_del_user=itemView.findViewById(R.id.btnDelUser);

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
