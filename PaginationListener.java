package com.example.chattingdemo;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public abstract class PaginationListener extends RecyclerView.OnScrollListener {

    public static int PAGE_NO = 1;

    @NonNull
    private LinearLayoutManager layoutManager;

    /**
     * Set scrolling threshold here (for now i'm assuming 10 item in one page)
     */
    private static final int PAGE_SIZE = 20;

    /**
     * Supporting only LinearLayoutManager for now.
     */
    public PaginationListener(@NonNull LinearLayoutManager layoutManager) {
        this.layoutManager = layoutManager;
    }

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        int visibleItemCount = layoutManager.getChildCount();
        int totalItemCount = layoutManager.getItemCount();
        int first = layoutManager.findLastVisibleItemPosition();

        Log.d("beant", "First Item  = " + first + "  total " + (PAGE_SIZE * PAGE_NO));

        if (!isLoading() && !isLastPage() && first < 10) {

            loadMoreItems();
//            if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
//                    && firstVisibleItemPosition >= 0
//                    && totalItemCount >= PAGE_SIZE) {
//                loadMoreItems();
//            }
        }
    }

    protected abstract void loadMoreItems();

    public abstract boolean isLastPage();

    public abstract boolean isLoading();

}