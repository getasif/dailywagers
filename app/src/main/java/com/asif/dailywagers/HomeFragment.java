package com.asif.dailywagers;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.asif.dailywagers.data.DailyWagerDbHelper;
import com.asif.dailywagers.model.Wager;
import com.asif.dailywagers.util.DatePickerFragment;
import com.asif.dailywagers.util.DateUtil;
import com.asif.dailywagers.util.IDatePickerDialogListener;

import java.util.List;

/**
 * Home page for the app
 * Created by asif on 6/24/16.
 */
public class HomeFragment extends Fragment implements View.OnClickListener{

    public static final String TAG = "HomeFragment";

    private static final int DATE_DIALOG_ID = 1;
    private TextView mTodayDate;
    private Button mSelectDate;
    private RecyclerView mHomeRecyclerView;
    //private FloatingActionButton mAddNew;
    private TextView mNoItemsMessage;
    private List<Wager> mWagerList;
    private DailyWagerDbHelper mWagerDbHelper;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        mWagerDbHelper = DailyWagerDbHelper.getInstance(getContext());
        Log.d(TAG, "On create");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "On create view");
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(false);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Daily wager");

        mTodayDate = (TextView) view.findViewById(R.id.today_date);
        mSelectDate = (Button) view.findViewById(R.id.check_date);
        //mAddNew = (FloatingActionButton) view.findViewById(R.id.add_new_wager);
        mHomeRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_home_recycler_view);
        mNoItemsMessage = (TextView) view.findViewById(R.id.text_empty);

        mHomeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mTodayDate.setText(DateUtil.getCurrentDisplayDate());
        mSelectDate.setOnClickListener(this);
        //mAddNew.setOnClickListener(this);

        new FetchWagers().execute();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().findViewById(R.id.add_new_wager).setVisibility(View.VISIBLE);
    }

    /**
     * Sets up list of wagers/person to the app
     */
    private void setupAdapter(){
        if(isAdded()){
            Log.d(TAG, "Setting up adapter for view");
            CalendarDay currentDate = DateUtil.getCalendarDay(mTodayDate.getText().toString());
            mHomeRecyclerView.setAdapter(new WagerAdapter(mWagerList, getActivity(), getActivity().getSupportFragmentManager(), currentDate));
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.activity_home_menu, menu);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.check_date:
                Log.d(TAG, "Select date clicked");

                DatePickerFragment datePickerFragment = DatePickerFragment.newInstance(getContext(), R.string.date_picker_title);
                datePickerFragment.setDatePickerDialogListener(new IDatePickerDialogListener() {
                    @Override
                    public void setDate(int year, int month, int day) {
                        mTodayDate.setText(DateUtil.getDisplayDate(year, month, day));
                        setupAdapter();
                    }
                });
                datePickerFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
                break;
            case R.id.add_new_wager:
                AbstractFragmentActivity.updateFragment(new ProfileFragment(), ProfileFragment.TAG, getActivity().getSupportFragmentManager());
                break;
        }

    }

    /**
     * Async task for fetching all the wagers
     */
    private class FetchWagers extends AsyncTask<Void, Void, List<Wager>>{
        @Override
        protected List<Wager> doInBackground(Void... params) {
            return mWagerDbHelper.fetchWagers();
        }

        @Override
        protected void onPostExecute(List<Wager> wagers) {
            super.onPostExecute(wagers);
            Log.d(TAG, "Wager is "+ wagers);
            if(wagers != null && wagers.size() > 0){
                mWagerList = wagers;
                setupAdapter();
                mNoItemsMessage.setVisibility(View.GONE);
            }else {
                mNoItemsMessage.setVisibility(View.VISIBLE);
            }
        }
    }
}