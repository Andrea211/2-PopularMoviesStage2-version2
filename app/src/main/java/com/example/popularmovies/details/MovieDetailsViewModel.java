package com.example.popularmovies.details;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.example.popularmovies.AppDatabase;
import com.example.popularmovies.Movie;

public class MovieDetailsViewModel extends ViewModel {

    private LiveData<Movie> movie;

    public MovieDetailsViewModel(AppDatabase database, int movieId) {
        movie = database.movieDao().loadMovieById(movieId);
    }

    public LiveData<Movie> getMovie() {
        return movie;
    }
}
