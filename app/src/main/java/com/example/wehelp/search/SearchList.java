package com.example.wehelp.search;


import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.MergeAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.wehelp.R;
import com.example.wehelp.categories.Categorylist_model;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchList extends AppCompatActivity {
    private List<SearchUsersModel> alluserslists = new ArrayList<>();

    public void setList(List<SearchUsersModel> alluserslists) {
        System.out.println("value set ");
        this.alluserslists=alluserslists;
    }

    public List<SearchUsersModel> getUserArrayList() {
        return this.alluserslists;
    }

    private RecyclerView search_res_adapter;
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private UserListAdapter userAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        search_res_adapter = findViewById(R.id.search_res_adapter);
        fillUsersDataFromDatabase();
    }

    private void setUpRecyclerView() {
        userAdapter = new UserListAdapter(getUserArrayList());
        search_res_adapter.setHasFixedSize(true);
        search_res_adapter.setLayoutManager(new LinearLayoutManager(SearchList.this));
        search_res_adapter.setAdapter(userAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.searchbar, menu);
        MenuItem item3 = menu.findItem(R.id.searchbar);
        SearchView searchView1 = (SearchView) menu.findItem(R.id.searchbar).getActionView();
        searchView1.setIconifiedByDefault(true);
        searchView1.setFocusable(true);
        searchView1.setIconified(false);
        searchView1.requestFocusFromTouch();
        item3.setVisible(true);

        SearchView searchView = (SearchView) item3.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setQueryHint(getResources().getString(R.string.search_hint));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                userAdapter.getFilter().filter(query);
                return false;  }
            @Override
            public boolean onQueryTextChange(String newText) {
                userAdapter.getFilter().filter(newText);
                return false; }  });
        return true;
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
                    }
                    setList(alluserslists);
                    setUpRecyclerView();
                } else {
                    System.out.println("Data not found.");
                }
            }
        });
    }


}
