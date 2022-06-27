package com.ehbmed.clinicalhelper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;

import java.util.Enumeration;

public class SwipeView extends AppCompatActivity{

    ViewPager2 viewPager;
    MyAdapter mAdapter;

    int numPages = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swipe_view);
        Intent mIntent = getIntent();
        numPages = mIntent.getIntExtra("numPages", 1);
        findViews();


    }

    private void findViews() {
        mAdapter = new MyAdapter(getSupportFragmentManager(), getLifecycle(), numPages);
        viewPager = findViewById(R.id.pager);
        viewPager.setAdapter(mAdapter);
    }

    public static class MyAdapter extends FragmentStateAdapter {

        int numPages = 1;
        MyAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, int numPages) {
            super(fragmentManager, lifecycle);
            this.numPages = numPages;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return new TextFragment();
        }

        @Override
        public int getItemCount() {
            return numPages;
        }
    }


}
