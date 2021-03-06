/*
 *    Copyright 2016 Gurupad Mamadapur
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.calgen.prodek.fadflicks.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.calgen.prodek.fadflicks.R;
import com.calgen.prodek.fadflicks.activity.DetailActivity;
import com.calgen.prodek.fadflicks.activity.MainActivity;
import com.calgen.prodek.fadflicks.fragment.MovieDetailFragment;
import com.calgen.prodek.fadflicks.model.Movie;
import com.calgen.prodek.fadflicks.utils.Parser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Gurupad on 25-Aug-16.
 */
public class GridMovieAdapter extends RecyclerView.Adapter<GridMovieAdapter.MovieViewHolder> implements Filterable {
    public static final int FAV_REQUEST_CODE = 2764;
    private static final String TAG = GridMovieAdapter.class.getSimpleName();
    private Context context;
    private List<Movie> movieList;
    private List<Movie> movieListCopy;

    public GridMovieAdapter(Context context, List<Movie> movieList) {
        this.context = context;
        this.movieList = movieList;
        this.movieListCopy = movieList;
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_movies_grid, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {
        Movie movie = movieList.get(position);

        try {
            holder.title.setText(movie.getTitle());
            holder.rating.setText(String.format(
                    context.getString(R.string.rating_format),
                    movie.getVoteAverage()));
            Drawable drawable = (movie.isFavourite)
                    ? context.getResources().getDrawable(R.drawable.ic_favorite_accent_24dp)
                    : context.getResources().getDrawable(R.drawable.ic_favorite_border_accent_24dp);
            holder.favouriteIcon.setImageDrawable(drawable);
            Picasso.with(context)
                    .load(Parser.formatImageUrl(movie.getPosterPath(), context.getString(R.string.image_size_small)))
                    .placeholder(new ColorDrawable(0xB6B6B6))
                    .into(holder.poster);
        } catch (Exception e) {
            Log.e(TAG, "onBindViewHolder: ", e);
        }
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<Movie> filteredResults;
                if (constraint.length() == 0) {
                    filteredResults = movieListCopy;
                } else {
                    filteredResults = getFilteredResults(constraint.toString().toLowerCase());
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredResults;

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                movieList = (List<Movie>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    private List<Movie> getFilteredResults(String constraint) {
        List<Movie> results = new ArrayList<>();

        for (Movie movie : movieList) {
            String movieName = movie.getTitle().toLowerCase();
            String movieReleaseDate = movie.getReleaseDate().toLowerCase();
            if (movieName.contains(constraint) || movieReleaseDate.contains(constraint)) {
                results.add(movie);
            }
        }
        return results;
    }

    public class MovieViewHolder extends RecyclerView.ViewHolder {
        //@formatter:off
        @BindView(R.id.title) public TextView title;
        @BindView(R.id.movie_rating) public TextView rating;
        @BindView(R.id.poster) public ImageView poster;
        @BindView(R.id.favourite_icon) public ImageView favouriteIcon;
        @BindView(R.id.card_view) public CardView cardView;
        //@formatter:on

        public MovieViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.card_view)
        public void onClick() {
            Movie movie = movieList.get(getLayoutPosition());
            if (MainActivity.twoPane) {
                MovieDetailFragment movieDetailFragment = new MovieDetailFragment();
                Bundle arguments = new Bundle();
                arguments.putSerializable(Intent.EXTRA_TEXT, movie);
                movieDetailFragment.setArguments(arguments);
                ((FragmentActivity) context).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_detail_container, movieDetailFragment, MainActivity.MOVIE_DETAIL_FRAGMENT_TAG)
                        .commit();
                notifyDataSetChanged();
            } else {
                Intent intent = new Intent(context, DetailActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, movie);
                ((Activity) context).startActivityForResult(intent, FAV_REQUEST_CODE);
            }
        }
    }
}
