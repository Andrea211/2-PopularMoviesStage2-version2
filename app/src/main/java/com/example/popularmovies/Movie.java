package com.example.popularmovies;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;

@Entity(tableName = "movie")
public class Movie implements Parcelable {

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        public Movie createFromParcel(Parcel src) {
            return new Movie(src);
        }

        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
    @PrimaryKey(autoGenerate = true)
    private int dbMovieId;
    private int movieId;
    private String originalTitle;
    private String originalLanguage;
    private String posterPath;
    private String overview;
    private String releaseDate;
    private Double voterAverage;
    private Double voteCount;
    private String trailerPath;
    private String reviewAuthor;
    private String reviewContents;
    private String reviewUrl;
    @Ignore
    private boolean isFavoriteMovie = false;

    public Movie() {
    }

    private Movie(Parcel parcel) {
        originalTitle = parcel.readString();
        originalLanguage = parcel.readString();
        posterPath = parcel.readString();
        overview = parcel.readString();
        releaseDate = parcel.readString();
        voterAverage = parcel.readDouble();
        voteCount = parcel.readDouble();
        trailerPath = parcel.readString();
        movieId = parcel.readInt();
        reviewAuthor = parcel.readString();
        reviewContents = parcel.readString();
        reviewUrl = parcel.readString();

    }

    // GETTER METHODS
    public int getDbMovieId() {
        return movieId;
    }

    // SETTER METHODS
    public void setDbMovieId(int dbMovieId) {
        this.dbMovieId = movieId;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public Double getVoterAverage() {
        return voterAverage;
    }

    public void setVoterAverage(Double voterAverage) {
        this.voterAverage = voterAverage;
    }

    public String getTrailerPath() {
        return trailerPath;
    }

    public void setTrailerPath(String trailerPath) {
        this.trailerPath = trailerPath;
    }

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public String getReviewAuthor() {
        return reviewAuthor;
    }

    public void setReviewAuthor(String reviewAuthor) {
        this.reviewAuthor = reviewAuthor;
    }

    public String getReviewContents() {
        return reviewContents;
    }

    public void setReviewContents(String reviewContents) {
        this.reviewContents = reviewContents;
    }

    public String getReviewUrl() {
        return reviewUrl;
    }

    public void setReviewUrl(String reviewUrl) {
        this.reviewUrl = reviewUrl;
    }

    public boolean getIsFavoriteMovie() {
        return isFavoriteMovie;
    }

    public void setIsFavoriteMovie(boolean isFavoriteMovie) {
        this.isFavoriteMovie = isFavoriteMovie;
    }

    public Double getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(Double voteCount) {
        this.voteCount = voteCount;
    }

    public String getOriginalLanguage() {
        return originalLanguage;
    }

    public void setOriginalLanguage(String originalLanguage) {
        this.originalLanguage = originalLanguage;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(originalTitle);
        dest.writeString(originalLanguage);
        dest.writeString(posterPath);
        dest.writeString(overview);
        dest.writeString(releaseDate);
        dest.writeDouble(voterAverage);
        dest.writeDouble(voteCount);
        dest.writeString(trailerPath);
        dest.writeInt(movieId);
        dest.writeString(reviewAuthor);
        dest.writeString(reviewContents);
        dest.writeString(reviewUrl);

    }
}
