package com.example.popularmovies.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.popularmovies.Movie;
import com.example.popularmovies.R;

public class AdapterReview extends RecyclerView.Adapter<AdapterReview.ViewHolder> {

    private Movie[] movies;
    private Context context;

    // Constructor for AdapterReview
    public AdapterReview(Movie[] movies, Context context) {
        this.context = context;
        this.movies = movies;
    }

    @Override
    // Creating new views - reviews holders, to populate recyclerView
    public AdapterReview.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {

        ConstraintLayout constraintLayout = (ConstraintLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.movie_review, parent, false);

        return new ViewHolder(constraintLayout);
    }

    @Override
    // Populate previously created views with data - reviews
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.authorTV.setText(String.valueOf(movies[position].getReviewAuthor()));
        holder.contentsTV.setText(String.valueOf(movies[position].getReviewContents()));

        holder.reviewButton.setOnClickListener((View v) -> {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(movies[position].getReviewUrl()));
            context.startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        if (movies == null || movies.length == 0) {
            return -1;
        }
        return movies.length;
    }

    // Create View Holder class
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView authorTV;
        TextView contentsTV;
        Button reviewButton;

        public ViewHolder(ConstraintLayout itemView) {
            super(itemView);

            authorTV = (TextView) itemView.findViewById(R.id.reviewAuthorTextView);
            contentsTV = (TextView) itemView.findViewById(R.id.reviewContentTextView);
            reviewButton = (Button) itemView.findViewById(R.id.fullReviewButton);
        }
    }
}
