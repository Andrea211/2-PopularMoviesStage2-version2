package com.example.popularmovies.details;

import android.annotation.SuppressLint;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.popularmovies.AppDatabase;
import com.example.popularmovies.Constants;
import com.example.popularmovies.Executor;
import com.example.popularmovies.Movie;
import com.example.popularmovies.QueryUtils;
import com.example.popularmovies.R;
import com.example.popularmovies.adapters.AdapterReview;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MovieDetails extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private Movie movie;
    private ToggleButton favoriteButton;
    private AppDatabase movieDatabase;
    private String releaseDate;

    public static void watchYoutubeVideo(Context context, String id) {
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.YOUTUBE_APP_BASE + id));
        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse(Constants.YOUTUBE_BASE_URL + id));
        try {
            context.startActivity(appIntent);
        } catch (ActivityNotFoundException ex) {
            context.startActivity(webIntent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_movie_details);

        android.support.v7.app.ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        if (intent == null) {
            closeOnError();
        }

        movie = intent.getParcelableExtra("movie");
        movieDatabase = AppDatabase.getInstance(getApplicationContext());

        setupDetailsUI(movie);

        setUpFavoriteMovieButton();

        // Check any change in favouriteButton state
        favoriteButton.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            if (isChecked) {
                // Toggle is Enabled
                favoriteButton.getTextOn();
                onFavoriteButtonClicked();
            } else {
                // Toggle is disabled
                favoriteButton.setTextColor(Color.parseColor("#000000"));
                favoriteButton.getTextOff();

                Executor.getInstance().diskIO().execute(() -> runOnUiThread(() ->
                        movieDatabase.movieDao().deleteMovie(movie.getMovieId())));
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    protected void onRestoreInstanceState(Bundle savedState) {
        super.onRestoreInstanceState(savedState);
    }

    void closeOnError() {
        finish();
        Toast.makeText(this, "Something went wrong.", Toast.LENGTH_SHORT).show();
    }

    private void setupDetailsUI(Movie movie) {
        TextView originalTitleTV = findViewById(R.id.titleTextView);
        RatingBar ratingBar = findViewById(R.id.ratingBar);
        TextView releaseDateTV = findViewById(R.id.releaseDateTextView);
        TextView overviewTV = findViewById(R.id.overviewTextView);
        ImageView posterIV = findViewById(R.id.posterImageView);
        TextView voteCount = findViewById(R.id.vote_count);
        TextView originalLanguage = findViewById(R.id.original_language);
        Button trailerBtn = findViewById(R.id.watchTrailerBtn);
        favoriteButton = findViewById(R.id.favoritesBtn);

        RecyclerView.LayoutManager mLayoutManager;

        mRecyclerView = findViewById(R.id.reviewsRecyclerView);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // TITLE
        originalTitleTV.setText(movie.getOriginalTitle());

        // ORIGINAL LANGUAGE
        originalLanguage.setText(movie.getOriginalLanguage());

        // VOTER AVERAGE / RATING
        double rating = movie.getVoterAverage() / 2;
        ratingBar.setRating((float) rating);

        // VOTE COUNT
        double doubleVoteCount = movie.getVoteCount();
        int intVoteCount = (int) doubleVoteCount;
        voteCount.setText("(" + String.valueOf(intVoteCount) + ")");

        // IMAGE
        Picasso.with(this)
                .load(movie.getPosterPath())
                .fit().centerCrop()
                .error(R.mipmap.icon_filmroll)
                .placeholder(R.mipmap.icon_filmroll_round)
                .into(posterIV);

        // OVERVIEW
        overviewTV.setText(movie.getOverview());
        overviewTV.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);

        // RELEASE DATE
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Constants.UNFORMATED_DATE_STRING);
        @SuppressLint("SimpleDateFormat") SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(Constants.FULL_DATE_FORMAT_STRING);

        try {
            Date date = simpleDateFormat.parse(movie.getReleaseDate());
            releaseDate = DATE_FORMAT.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        releaseDateTV.setText(releaseDate);

        // TRAILER BUTTON
        new TrailerButtonAsyncTask(trailerBtn).execute(String.valueOf(movie.getMovieId()), Constants.VIDEO_QUERY_PARAM);

        // LOAD REVIEWS
        new ReviewsAsyncTask().execute(String.valueOf(movie.getMovieId()), Constants.REVIEW_URL_QUERY_PARAM);

        // INITIAL BUTTON VALUES
        favoriteButton.setTextOn(Constants.FAVORITED_STRING);
        favoriteButton.setTextOff(Constants.ADD_TO_FAVORITES_STRING);
    }

    /*** ADD REVIEW DATA TO MOVIE OBJECT ***/
    public Movie[] setMovieDataToArray(String jsonResults) throws JSONException {
        JSONObject root = new JSONObject(jsonResults);
        JSONArray resultsArray = root.getJSONArray(Constants.RESULTS_QUERY_PARAM);
        Movie[] movies = new Movie[resultsArray.length()];

        for (int i = 0; i < resultsArray.length(); i++) {
            // Initialize each object before it can be used
            movies[i] = new Movie();

            // Object contains all tags we're looking for
            JSONObject movieInfo = resultsArray.getJSONObject(i);

            // Store data in movie object
            movies[i].setReviewAuthor(movieInfo.getString(Constants.REVIEW_AUTHOR_QUERY_PARAM));
            movies[i].setReviewContents(movieInfo.getString(Constants.REVIEW_QUERY_PARAM));
            movies[i].setReviewUrl(movieInfo.getString(Constants.REVIEW_URL_PARAM));
        }
        return movies;
    }

    /*** FAVORITE MOVIE BUTTON IS CALLED WHEN "ADD TO FAVORITES" BUTTON IS CLICKED***/
    public void onFavoriteButtonClicked() {
        final Movie movie = getIntent().getExtras().getParcelable("movie");
        Executor.getInstance().diskIO().execute(() -> movieDatabase.movieDao().insertMovie(movie));
    }

    private void setUpFavoriteMovieButton() {
        MovieDetailsViewModelFactory factory =
                new MovieDetailsViewModelFactory(movieDatabase, movie.getMovieId());
        final MovieDetailsViewModel viewModel =
                ViewModelProviders.of(this, factory).get(MovieDetailsViewModel.class);

        viewModel.getMovie().observe(this, new Observer<Movie>() {
            @Override
            public void onChanged(@Nullable Movie movieInDb) {
                viewModel.getMovie().removeObserver(this);

                if (movieInDb == null) {
                    favoriteButton.setTextColor(Color.parseColor("#000000"));
                    favoriteButton.setChecked(false);
                    favoriteButton.getTextOff();
                } else if ((movie.getMovieId() == movieInDb.getMovieId()) && !favoriteButton.isChecked()) {
                    favoriteButton.setChecked(true);
                    favoriteButton.setText(Constants.FAVORITED_STRING);
                    favoriteButton.setTextColor(Color.parseColor("#b5001e"));
                } else {
                    favoriteButton.setTextColor(Color.parseColor("#000000"));
                    favoriteButton.setChecked(false);
                    favoriteButton.getTextOff();
                }
            }
        });
    }

    public void backToGridView() {
        this.finish();
    }

    /*** ASYNC TASK FOR THE "WATCH TRAILER" BUTTON ***/
    private class TrailerButtonAsyncTask extends AsyncTask<String, Void, String> {
        private final Button button;
        String trailerKey = null;

        public TrailerButtonAsyncTask(Button button) {
            this.button = button;
        }

        @Override
        protected String doInBackground(String... strings) {
            Movie[] movies;
            try {
                URL url = QueryUtils.buildMovieIdUrl(strings[0], strings[1]);
                String movieSearchResults = QueryUtils.getResponseFromHttpUrl(url);

                JSONObject root = new JSONObject(movieSearchResults);
                JSONArray resultsArray = root.getJSONArray(Constants.RESULTS_QUERY_PARAM);

                if (resultsArray.length() == 0) {
                    trailerKey = null;
                } else {
                    movies = new Movie[resultsArray.length()];
                    for (int i = 0; i < resultsArray.length(); i++) {
                        // Initialize each object before it can be used
                        movies[i] = new Movie();

                        // Object contains all tags we're looking for
                        JSONObject movieInfo = resultsArray.getJSONObject(i);

                        // Store data in movie object
                        movies[i].setTrailerPath(movieInfo.getString(Constants.VIDEO_TRAILER_KEY_PARAM));
                    }
                    // Returns only the first trailer from the results array, since there can be multiple trailers
                    return movies[0].getTrailerPath();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return trailerKey;
        }

        @SuppressLint("WrongConstant")
        protected void onPostExecute(String temp) {
            button.setOnClickListener((View v) -> {
                if (temp == null) {
                    Toast.makeText(getApplicationContext(), Constants.NO_TRAILERS, Constants.TOAST_DURATION).show();
                } else {
                    watchYoutubeVideo(getApplicationContext(), temp);
                }
            });
        }
    }

    private class ReviewsAsyncTask extends AsyncTask<String, Void, Movie[]> {
        @Override
        protected Movie[] doInBackground(String... strings) {
            try {
                URL url = QueryUtils.buildMovieIdUrl(strings[0], strings[1]);
                String movieSearchResults = QueryUtils.getResponseFromHttpUrl(url);
                return setMovieDataToArray(movieSearchResults);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Movie[] movies) {
            AdapterReview mAdapterReview;
            mAdapterReview = new AdapterReview(movies, getApplicationContext());

            mRecyclerView.setAdapter(mAdapterReview);
            mRecyclerView.setNestedScrollingEnabled(false);
        }
    }
}