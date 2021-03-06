package com.jastley.exodusnetwork.Account.fragments;

import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.TextView;

import com.jastley.exodusnetwork.Account.adapters.AccountStatsRecyclerAdapter;
import com.jastley.exodusnetwork.Account.viewmodels.AccountStatsViewModel;
import com.jastley.exodusnetwork.MainActivity;
import com.jastley.exodusnetwork.R;
import com.jastley.exodusnetwork.Utils.NoNetworkException;

import butterknife.BindView;
import butterknife.ButterKnife;


public class AccountStatsFragment extends Fragment {

    private OnAccountStatsInteractionListener mListener;
    private AccountStatsViewModel mViewModel;

    @BindView(R.id.account_stats_swipe_refresh) SwipeRefreshLayout mSwipeRefreshLayout;

    //RecyclerViews
    @BindView(R.id.account_stats_pvp_recycler_view) RecyclerView mPVPRecyclerView;
    @BindView(R.id.account_stats_raid_recycler_view) RecyclerView mRaidRecyclerView;
    @BindView(R.id.account_stats_strikes_recycler_view) RecyclerView mStrikesRecyclerView;
    @BindView(R.id.account_stats_story_recycler_view) RecyclerView mStoryRecyclerView;
    @BindView(R.id.account_stats_patrol_recycler_view) RecyclerView mPatrolRecyclerView;

    //Error messages
    @BindView(R.id.account_stats_pvp_error) TextView mPVPErrorMessage;
    @BindView(R.id.account_stats_raid_error) TextView mRaidErrorMessage;
    @BindView(R.id.account_stats_strikes_error) TextView mStrikesErrorMessage;
    @BindView(R.id.account_stats_story_error) TextView mStoryErrorMessage;
    @BindView(R.id.account_stats_patrol_error) TextView mPatrolErrorMessage;

    //RecyclerAdapters
    private AccountStatsRecyclerAdapter pvpStatsAdapter;
    private AccountStatsRecyclerAdapter raidStatsAdapter;
    private AccountStatsRecyclerAdapter strikesStatsAdapter;
    private AccountStatsRecyclerAdapter storyStatsAdapter;
    private AccountStatsRecyclerAdapter patrolStatsAdapter;

    public AccountStatsFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static AccountStatsFragment newInstance() {
        return new AccountStatsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_account_stats, container, false);
        ButterKnife.bind(this, rootView);
        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initialiseRecyclerViews();

        mViewModel = ViewModelProviders.of(this).get(AccountStatsViewModel.class);

        getAccountStats();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSwipeRefreshLayout.setRefreshing(true);

        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            mSwipeRefreshLayout.setRefreshing(true);

            getAccountStats();

        });
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onAccountStatsInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnAccountStatsInteractionListener) {
            mListener = (OnAccountStatsInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnAccountStatsInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();

        MainActivity activity = (MainActivity)getActivity();
        if(activity != null) {
            activity.setActionBarTitle(getString(R.string.account_stats));
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.refresh_toolbar, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {

            case R.id.refresh_button:
                mSwipeRefreshLayout.setRefreshing(true);

                getAccountStats();
        }
        return super.onOptionsItemSelected(item);
    }

    public interface OnAccountStatsInteractionListener {
        // TODO: Update argument type and name
        void onAccountStatsInteraction(Uri uri);
    }

    private void initialiseRecyclerViews() {

        //PVP stats
        pvpStatsAdapter = new AccountStatsRecyclerAdapter();
        mPVPRecyclerView.setLayoutAnimation(getLayoutAnimation());
        mPVPRecyclerView.setLayoutManager(getLayoutManager());
        mPVPRecyclerView.setAdapter(pvpStatsAdapter);
        mPVPRecyclerView.setNestedScrollingEnabled(false);

        //Raid
        raidStatsAdapter = new AccountStatsRecyclerAdapter();
        mRaidRecyclerView.setLayoutAnimation(getLayoutAnimation());
        mRaidRecyclerView.setLayoutManager(getLayoutManager());
        mRaidRecyclerView.setAdapter(raidStatsAdapter);
        mRaidRecyclerView.setNestedScrollingEnabled(false);

        //Strikes
        strikesStatsAdapter = new AccountStatsRecyclerAdapter();
        mStrikesRecyclerView.setLayoutAnimation(getLayoutAnimation());
        mStrikesRecyclerView.setLayoutManager(getLayoutManager());
        mStrikesRecyclerView.setAdapter(strikesStatsAdapter);
        mStrikesRecyclerView.setNestedScrollingEnabled(false);

        //Story
        storyStatsAdapter = new AccountStatsRecyclerAdapter();
        mStoryRecyclerView.setLayoutAnimation(getLayoutAnimation());
        mStoryRecyclerView.setLayoutManager(getLayoutManager());
        mStoryRecyclerView.setAdapter(storyStatsAdapter);
        mStoryRecyclerView.setNestedScrollingEnabled(false);

        //Patrol
        patrolStatsAdapter = new AccountStatsRecyclerAdapter();
        mPatrolRecyclerView.setLayoutAnimation(getLayoutAnimation());
        mPatrolRecyclerView.setLayoutManager(getLayoutManager());
        mPatrolRecyclerView.setAdapter(patrolStatsAdapter);
        mPatrolRecyclerView.setNestedScrollingEnabled(false);
    }

    private void getAccountStats() {

        mViewModel.getPvpStatsList().observe(this, pvpStats -> {
            if(pvpStats.getErrorMessage() != null) {
                mPVPErrorMessage.setText(pvpStats.getErrorMessage());
                mPVPErrorMessage.setVisibility(View.VISIBLE);
            }
            else {
                pvpStatsAdapter.setStats(pvpStats.getStatsList());
                mPVPErrorMessage.setVisibility(View.GONE);
            }

            mSwipeRefreshLayout.setRefreshing(false);
        });

        mViewModel.getRaidStatsList().observe(this, raidStats -> {
            if(raidStats.getErrorMessage() != null) {
                mRaidErrorMessage.setText(raidStats.getErrorMessage());
                mRaidErrorMessage.setVisibility(View.VISIBLE);
            }
            else {
                raidStatsAdapter.setStats(raidStats.getStatsList());
                mRaidErrorMessage.setVisibility(View.GONE);
            }
        });

        mViewModel.getAllStrikesStatsList().observe(this, strikeStats -> {
            if(strikeStats.getErrorMessage() != null) {
                mStrikesErrorMessage.setText(strikeStats.getErrorMessage());
                mStrikesErrorMessage.setVisibility(View.VISIBLE);
            }
            else {
                strikesStatsAdapter.setStats(strikeStats.getStatsList());
                mStrikesErrorMessage.setVisibility(View.GONE);
            }
        });

        mViewModel.getStoryStatsList().observe(this, storyStats -> {
            if(storyStats.getErrorMessage() != null) {
                mStoryErrorMessage.setText(storyStats.getErrorMessage());
                mStoryErrorMessage.setVisibility(View.VISIBLE);
            }
            else {
                storyStatsAdapter.setStats(storyStats.getStatsList());
                mStoryErrorMessage.setVisibility(View.GONE);
            }
        });

        mViewModel.getPatrolStatsList().observe(this, patrolStats -> {
            if(patrolStats.getErrorMessage() != null) {
                mPatrolErrorMessage.setText(patrolStats.getErrorMessage());
                mPatrolErrorMessage.setVisibility(View.VISIBLE);
            }
            else {
                patrolStatsAdapter.setStats(patrolStats.getStatsList());
                mPatrolErrorMessage.setVisibility(View.GONE);
            }
        });

        //Single snackbar NoNetworkException error
        mViewModel.getThrowable().observe(this, throwable -> {
            if(throwable.getThrowable() instanceof NoNetworkException) {
                Snackbar.make(getView(), "No network detected!", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Retry", v -> getAccountStats())
                        .show();
            }
            else {
                Snackbar.make(getView(), throwable.getThrowable().getMessage(), Snackbar.LENGTH_INDEFINITE)
                        .setAction("Retry", v -> getAccountStats())
                        .show();
            }
        });
    }


    private LinearLayoutManager getLayoutManager() {
        return new LinearLayoutManager(getContext());
    }

    private LayoutAnimationController getLayoutAnimation() {
        return AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_animation_slide_right);
    }
}
