package com.example.wehelp.search;


import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wehelp.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchList extends AppCompatActivity {
    private String searchText="";
    private TextView search_res_title;
    private CircleImageView search_res_profile_pic;
    private String result_type= "";
    private RecyclerView search_res_adapter;
    private FirebaseFirestore firestore =FirebaseFirestore.getInstance();
    CollectionReference usersdb= firestore.collection("users");

    private UserSearchAdapter adapter,adapter2;


    private String profile_pic_url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_list);
        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //got user search query
        searchText=getIntent().getStringExtra("searchText");
        search_res_title = findViewById(R.id.search_res_title);
        search_res_profile_pic=findViewById(R.id.search_profile_pic);
        search_res_adapter=findViewById(R.id.search_res_adapter);

        //tutorial from: https://codinginflow.com/tutorials/android/firebaseui-firestorerecycleradapter/part-3-firestorerecycleradapter
        setUpRecyclerView();
    }
    //queries
    private void setUpRecyclerView() {
         Query search_users_query1= usersdb.orderBy("firstname").startAt(searchText).endAt(searchText+"\uf8ff");
         Query search_users_query2= firestore.collection("users").whereArrayContains("lastname", searchText);


//        search_users_query1.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                if(task.isSuccessful())
//                {
//                    for(QueryDocumentSnapshot documentSnapshots:task.getResult())
//                    {
//                        System.out.println(documentSnapshots.getString("firstname"));
//                        search_res_title.setText(documentSnapshots.getString("firstname"));
//                    }
//                }
//            }
//        });
        //firstname and lastname
        FirestoreRecyclerOptions<SearchUsersModel> options = new FirestoreRecyclerOptions.Builder<SearchUsersModel>()
                .setQuery(search_users_query1, SearchUsersModel.class)
                .build();
        adapter = new UserSearchAdapter(options);
        search_res_adapter.setHasFixedSize(true);
        search_res_adapter.setLayoutManager(new LinearLayoutManager(SearchList.this));
        search_res_adapter.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }
    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem item3 = menu.findItem(R.id.app_bar_search);
        item3.setVisible(true);
        MenuItem item = menu.findItem(R.id.action_login);
        item.setVisible(false);

        MenuItem item2 = menu.findItem(R.id.action_register);
        item2.setVisible(false);

        MenuItem setting = menu.findItem(R.id.action_settings);
        setting.setVisible(false);

        MenuItem logout = menu.findItem(R.id.action_signout);
        logout.setVisible(false);

//
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.app_bar_search).getActionView();
        // searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryHint(getResources().getString(R.string.search_hint));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Intent searchIntent = new Intent(SearchList.this, SearchList.class);
                searchIntent.putExtra("searchText", query);
                startActivity(searchIntent);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                return false;
            }
        });

        return true;
    }



//getter setter
    public String getProfile_pic_url() {
    return profile_pic_url;
}
    public void setProfile_pic_url(String profile_pic_url)
    {
        this.profile_pic_url = profile_pic_url;
    }
}
