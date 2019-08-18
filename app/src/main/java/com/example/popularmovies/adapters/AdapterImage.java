package com.example.popularmovies.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.popularmovies.Movie;
import com.example.popularmovies.details.MovieDetails;
import com.example.popularmovies.R;
import com.squareup.picasso.Picasso;

public class AdapterImage extends RecyclerView.Adapter<AdapterImage.ViewHolder> {

    private static Movie[] mMovies;
    private Context mContext;

    // Constructor for AdapterImage
    public AdapterImage(Context mContext, Movie[] mMovies) {
        this.mMovies = mMovies;

        if (mContext == null) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException exception) {
                exception.printStackTrace();
            }
        } else {
            this.mContext = mContext;
        }
    }

    @NonNull
    @Override
    // Creating new views - poster images holders, to populate recyclerView
    public AdapterImage.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        // Create a new view
        ImageView imageView = (ImageView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.image_thumb_view, parent, false);

        return new ViewHolder(imageView);
    }

    @Override
    // Populate previously created views with data - poster images
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Picasso picasso = Picasso.with(mContext);
        picasso.load(mMovies[position].getPosterPath())
                .fit()
                .error(R.mipmap.icon_filmroll)
                .placeholder(R.mipmap.icon_filmroll_round)
                .into((ImageView) holder.mImageView.findViewById(R.id.image_view));

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(mContext, MovieDetails.class);
            intent.putExtra("movie", mMovies[position]);
            mContext.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        if (mMovies == null || mMovies.length == 0) {
            return -1;
        }
        return mMovies.length;
    }

    public void setMovies(Movie[] movies) {
        mMovies = movies;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView mImageView;

        private ViewHolder(ImageView imageView) {
            super(imageView);
            mImageView = imageView;
        }
    }
}