package com.bithuoche.foldlayout.demo;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bithuoche.foldlayout.FoldLayout;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    showFoldLayout();
                    return true;
                case R.id.navigation_dashboard:
                    showViewPager();
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    private void showFoldLayout() {
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setVisibility(View.INVISIBLE);
        FoldLayout foldLayout = findViewById(R.id.fold_layout);
        if (foldLayout.getVisibility() == View.VISIBLE) {
            foldLayout.setFoldDegree(180);
        }
        foldLayout.setVisibility(View.VISIBLE);
    }

    private void showViewPager() {
        View foldLayout = findViewById(R.id.fold_layout);
        foldLayout.setVisibility(View.INVISIBLE);
        ViewPager viewPager = findViewById(R.id.view_pager);
        if (viewPager.getVisibility() == View.VISIBLE) {
            viewPager.setCurrentItem(0);
            return;
        }
        viewPager.setVisibility(View.VISIBLE);
        viewPager.setAdapter(new PagerAdapter() {
            private List<View> viewCache = new LinkedList<>();

            @Override
            public int getCount() {
                return 100;
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                View view = viewCache.isEmpty() ? null : viewCache.remove(0);
                if (view == null) {
                    view = LayoutInflater.from(container.getContext()).inflate(R.layout.fold_layout, container, false);
                    ((FoldLayout) view).config(false, true);
                }
                TextView tvMarquee = view.findViewById(R.id.text);
                tvMarquee.setText(new Random().nextBoolean() ? "去什么地方呢？这么晚了，美丽的火车，孤独的火车？凄苦是你汽笛的声音" : "列车飞驰而过，车窗的灯火辉煌");
                tvMarquee.setSelected(true);
                ((TextView) view.findViewById(R.id.index)).setText(String.valueOf(position));
                ((ImageView) view.findViewById(R.id.image)).setImageResource(position % 2 == 0 ? R.drawable.huoche : R.drawable.huoche1);
                container.addView(view);
                return view;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                if (object instanceof View) {
                    container.removeView((View) object);
                    viewCache.add((View) object);
                }
            }
        });

        viewPager.setPageTransformer(true, new ViewPager.PageTransformer() {
            @Override
            public void transformPage(View page, float position) {
                if (position < -1 || position > 1) {
                    return;
                }
                if (position < 0) {
                    page.setTranslationX(-position * page.getWidth());
                    float flipDegree = 180 * (1 + position);
                    ((FoldLayout) page).setFoldDegree(flipDegree);
                } else {
                    page.setTranslationX(-position * page.getWidth());
                    float flipDegree = 180 * (1 + position);
                    ((FoldLayout) page).setFoldDegree(flipDegree);
                }
                page.setVisibility(View.VISIBLE);
            }
        });
    }
}
