package com.example.popularmovies;

import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModelProviders;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.popularmovies.adapters.AdapterImage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;


public class MainActivity extends AppCompatActivity{
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private int selectedItem;
    private AdapterImage mAdapterImage;
    private Parcelable mListState;

    @SuppressLint({"WrongConstant", "ResourceType"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_main);

        mRecyclerView = findViewById (R.id.recycler_view);

        // Using a Grid Layout Manager
        mLayoutManager = new GridLayoutManager(this, Constants.GRID_NUM_OF_COLUMNS);
        mRecyclerView.getRecycledViewPool ().clear ();
        mRecyclerView.setLayoutManager(mLayoutManager);


        if(savedInstanceState != null) {
            selectedItem = savedInstanceState.getInt("OPTION");
        }
        // Check if online
        if (isOnline ()) {
            //Default to Popular Query Sort
            new FetchDataAsyncTask().execute(Constants.POPULAR_QUERY_PARAM);
        } else {
            Toast.makeText(getApplicationContext(), Constants.NO_INTERNET_TEXT, Constants.TOAST_DURATION).show();
        }
    }

    /***
     * Thanks to https://stackoverflow.com/questions/28236390/recyclerview-store-restore-state-between-activities
     * for the code to store/restore RecyclerView state between activities
     * ***/
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState (outState);

        outState.putInt("OPTION", selectedItem);
        // Save list state
        mListState = mLayoutManager.onSaveInstanceState();
        outState.putParcelable("LIST_STATE_KEY", mListState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle outState) {
        selectedItem = outState.getInt ("OPTION");

        // Retrieve list state and list/item positions
        mListState = outState.getParcelable("LIST_STATE_KEY");
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mListState != null) {
            mLayoutManager.onRestoreInstanceState(mListState);
        }
    }

    /*** MENU METHODS ***/
    //Attempt to try and save selected menu item on screen rotation
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater ();
        inflater.inflate(R.menu.menu, menu);
        MenuItem menuItem;
        switch (selectedItem){
            case R.id.most_popular:
                menuItem = menu.findItem(R.id.most_popular);
                menuItem.setChecked (true);
                break;

            case R.id.top_rated:
                menuItem = menu.findItem(R.id.top_rated);
                menuItem.setChecked (true);
                break;

            case R.id.favorite_movie_setting:
                menuItem = menu.findItem(R.id.most_popular);
            menuItem.setChecked (true);
            break;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId ();
        if (id == R.id.most_popular) {
            selectedItem = id;
            item.setVisible (true);
            new FetchDataAsyncTask ().execute(Constants.POPULAR_QUERY_PARAM);
            return true;
        }
        if (id == R.id.top_rated) {
            selectedItem = id;
            item.setVisible (true);
            new FetchDataAsyncTask().execute(Constants.TOP_RATED_QUERY_PARAM);
            return true;
        }
        if (id == R.id.favorite_movie_setting){
            selectedItem = id;
            item.setVisible (true);
            setUpViewModel (); // Favorite Movies
            return true;
        }

        return super.onOptionsItemSelected (item);
    }

    public void setUpViewModel() {
        MainViewModel viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        viewModel.getMovies ().observe (this, (Movie[] movies1) -> {
            mAdapterImage.notifyDataSetChanged ();
            mAdapterImage.setMovies (movies1);
        });
    }

    /*** METHOD TO MAKE STRING OF MOVIE DATA TO AN ARRAY OF MOVIE OBJECTS ***/
    public Movie[] makeMoviesDataToArray(String moviesJsonResults) throws JSONException {
        Movie[] movies;

        // Get results as an array
        JSONObject moviesJson = new JSONObject(moviesJsonResults);
        JSONArray resultsArray = moviesJson.getJSONArray(Constants.RESULTS_QUERY_PARAM);

        // Create array of Movie objects that stores data from the JSON string
        movies = new Movie[resultsArray.length()];

        // Go through movies one by one and get data
        for (int i = 0; i < resultsArray.length(); i++) {
            // Initialize each object before it can be used
            movies[i] = new Movie();

            // Object contains all tags we're looking for
            JSONObject movieInfo = resultsArray.getJSONObject(i);

            // Store data in movie object
            movies[i].setOriginalTitle(movieInfo.getString(Constants.ORIGINAL_TITLE_QUERY_PARAM));
            movies[i].setOriginalLanguage(movieInfo.getString(Constants.ORIGINAL_LANGUAGE_QUERY_PARAM));
            movies[i].setPosterPath(Constants.MOVIEDB_IMAGE_BASE_URL + movieInfo.getString(Constants.POSTER_PATH_QUERY_PARAM));
            movies[i].setOverview(movieInfo.getString(Constants.OVERVIEW_QUERY_PARAM));
            movies[i].setVoterAverage(movieInfo.getDouble(Constants.VOTER_AVERAGE_QUERY_PARAM));
            movies[i].setVoteCount (movieInfo.getDouble (Constants.VOTE_COUNT));
            movies[i].setReleaseDate(movieInfo.getString(Constants.RELEASE_DATE_QUERY_PARAM));
            movies[i].setMovieId (movieInfo.getInt (Constants.MOVIE_ID_QUERY_PARAM));

        }
        return movies;
    }

    /*** FETCH MOVIE DATA ASYNC TASK ***/
    public class FetchDataAsyncTask extends AsyncTask<String, Void, Movie[]> {
        public FetchDataAsyncTask() {
            super();
        }

        @Override
        protected Movie[] doInBackground(String... params) {
            // Holds data returned from the API
            String movieSearchResults;

            try {
                URL url = QueryUtils.buildUrl(params);
                movieSearchResults = QueryUtils.getResponseFromHttpUrl(url);

                if(movieSearchResults == null) {
                    return null;
                }
                return makeMoviesDataToArray (movieSearchResults);
            } catch (IOException e) {
                return null;
            } catch (JSONException e) {
                e.printStackTrace ();
            }
            return null;
        }

        protected void onPostExecute(Movie[] movies) {
            mAdapterImage = new AdapterImage(getApplicationContext(), movies);
            mRecyclerView.setAdapter(mAdapterImage);
        }
    }

    /*** CHECKS IF ONLINE
     * https://stackoverflow.com/questions/1560788/how-to-check-internet-access-on-android-inetaddress-never-times-out ***/
    public boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec(Constants.INTERNET_CHECK_COMMAND);
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        }
        catch (IOException e)          { e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }

        return false;
    }
}