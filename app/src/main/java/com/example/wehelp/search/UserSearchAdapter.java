package com.example.wehelp.search;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wehelp.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserSearchAdapter extends FirestoreRecyclerAdapter<SearchUsersModel,UserSearchAdapter.UserAdapterView> {
    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public UserSearchAdapter(@NonNull FirestoreRecyclerOptions<SearchUsersModel> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull UserAdapterView holder, int position, @NonNull SearchUsersModel model) {
        String fullname= model.getFirstname()+" "+model.getLastname();
        holder.search_item_title.setText(fullname);
    }

    @NonNull
    @Override
    public UserAdapterView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_search_res_single,
                parent, false);
        return new UserAdapterView(v);
    }

    public class UserAdapterView extends RecyclerView.ViewHolder {
        private TextView search_item_title;
        private CircleImageView search_user_image;
        public UserAdapterView(@NonNull View itemView) {
            super(itemView);
            search_item_title=itemView.findViewById(R.id.search_res_title);
            search_user_image=itemView.findViewById(R.id.search_profile_pic);

        }
    }

    @Override
    public void onDataChanged() {
        super.onDataChanged();

    }
}
