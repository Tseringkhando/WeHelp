package com.example.wehelp.admin.search;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.SearchView;

import com.example.wehelp.R;
import com.example.wehelp.search.SearchUsersModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminList extends Fragment {
    private SearchView searchAdmins;
    private List<SearchUsersModel> alluserslists = new ArrayList<>();
    private RecyclerView search_res_adapter;
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private AdminListAdapter userAdapter;
    public AdminList() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v= inflater.inflate(R.layout.fragment_admin_list_adapter, container, false);
        search_res_adapter=v.findViewById(R.id.adminAdapter);
        searchAdmins= (SearchView)v.findViewById(R.id.adminSearchAdmins);
        //expand the search view
        searchAdmins.setIconifiedByDefault(true);
        searchAdmins.setFocusable(true);
        searchAdmins.setIconified(false);
        searchAdmins.requestFocusFromTouch();

        //enable search filter
        searchAdmins.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchAdmins.setQueryHint(getResources().getString(R.string.search_hint));
        searchAdmins.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
        //search feature
        fillUsersDataFromDatabase();
        return v;
    }


    // method to list all the users from database into the arraylist
    public void fillUsersDataFromDatabase() {
        Query q = firestore.collection("users").whereEqualTo("isAdmin",true);
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
                        userAdapter = new AdminListAdapter(alluserslists);
                    }
                    setUpRecyclerView();
                } else {
                    System.out.println("Data not found.");
                }
            }
        });
    }

    private void setUpRecyclerView() {
        search_res_adapter.setHasFixedSize(true);
        search_res_adapter.setLayoutManager(new LinearLayoutManager(getContext()));
        search_res_adapter.setAdapter(userAdapter);
    }
}