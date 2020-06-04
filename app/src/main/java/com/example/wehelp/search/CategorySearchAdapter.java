package com.example.wehelp.search;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wehelp.R;
import com.example.wehelp.categories.Categorylist_model;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import de.hdodenhof.circleimageview.CircleImageView;

public class CategorySearchAdapter extends FirestoreRecyclerAdapter<Categorylist_model,CategorySearchAdapter.CategoryViewHolder> {
    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public CategorySearchAdapter(@NonNull FirestoreRecyclerOptions<Categorylist_model> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull CategoryViewHolder holder, int position, @NonNull Categorylist_model model) {

        holder.search_item_title.setText(model.getCategory());
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_search_res_single,
                parent, false);
        return new CategoryViewHolder(v);
    }

    public class CategoryViewHolder  extends RecyclerView.ViewHolder {
        private TextView search_item_title;
        private CircleImageView search_user_image;
        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            search_item_title=itemView.findViewById(R.id.search_res_title);
            search_user_image=itemView.findViewById(R.id.search_profile_pic);
            search_user_image.setVisibility(View.GONE);

        }
    }
}
