package com.example.wehelp.admin.search;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.SearchView;

import com.example.wehelp.R;
import com.example.wehelp.search.SearchList;
import com.example.wehelp.search.SearchUsersModel;
import com.example.wehelp.search.UserListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class Admin_users_list extends Fragment {
    private SearchView searchView1;
    private List<SearchUsersModel> alluserslists = new ArrayList<>();
    private RecyclerView search_res_adapter;
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private AdminUserAdapter userAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_admin_users_list_adapter,container,false);
        userAdapter=new AdminUserAdapter();
        fillUsersDataFromDatabase();
        search_res_adapter = v.findViewById(R.id.admin_users_adapter);
        searchView1= (SearchView)v.findViewById(R.id.adminSearchUsers);
        searchView1.setIconifiedByDefault(true);
        searchView1.setFocusable(true);
        searchView1.setIconified(false);
        searchView1.requestFocusFromTouch();
        searchView1.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView1.setQueryHint(getResources().getString(R.string.search_hint));
        searchView1.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                userAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                userAdapter.getFilter().filter(newText);
                return false;
            }
        });


        return v;
    }


    private void setUpRecyclerView() {

        search_res_adapter.setHasFixedSize(true);
        search_res_adapter.setLayoutManager(new LinearLayoutManager(getContext()));
        search_res_adapter.setAdapter(userAdapter);
    }


    // method to list all the users from database into the arraylist
    public void fillUsersDataFromDatabase() {
        Query q = firestore.collection("users").whereEqualTo("isAdmin",false);
        q.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String firstname = document.getString("firstname");
                        String lastname = document.getString("lastname");
                        String uid = document.getString("user_id");
                        String photo_url = document.getString("profile_image");
                        Timestamp dob = document.getTimestamp("dob");
                        String contact = document.getString("contact");
                        Timestamp datejoined = document.getTimestamp("datejoined");
                        String email = document.getString("email");
                        Boolean isAdmin = document.getBoolean("isAdmin");
                        alluserslists.add(new SearchUsersModel(uid, firstname, lastname, photo_url, contact, email, isAdmin, dob, datejoined));
                        userAdapter = new AdminUserAdapter(alluserslists);
                    }
                    setUpRecyclerView();
                } else {
                    System.out.println("Data not found.");
                }
            }
        });
    }

}