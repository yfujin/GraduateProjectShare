package com.young.share.interfaces;

import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.AbsListView;
import android.widget.ListView;

/**
 * 上拉加载的滑动监听器
 * <p/>
 * Created by Nearby Yang on 2015-11-15.
 */
public class ListViewRefreshListener implements AbsListView.OnScrollListener,
        SwipeRefreshLayout.OnRefreshListener {

    private boolean isLastItem = false;
    private RefreshListener refreshlistener;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView listView;
    private ScrollStateChange stateChangeListener;

    public ListViewRefreshListener(ListView listView, SwipeRefreshLayout swipeRefreshLayout,
                                   RefreshListener refreshlistener) {
        this.listView = listView;
        this.swipeRefreshLayout = swipeRefreshLayout;
        this.refreshlistener = refreshlistener;

        listView.setOnScrollListener(this);
        swipeRefreshLayout.setOnRefreshListener(this);
    }

    /**
     * 添加滑动状态改变的回调
     * @param listView
     * @param swipeRefreshLayout
     * @param refreshlistener
     * @param stateChangeListener 滑动状态改变的回调
     */
    public ListViewRefreshListener(ListView listView, SwipeRefreshLayout swipeRefreshLayout,
                                   RefreshListener refreshlistener, ScrollStateChange stateChangeListener) {
        this.listView = listView;
        this.swipeRefreshLayout = swipeRefreshLayout;
        this.refreshlistener = refreshlistener;
        this.stateChangeListener = stateChangeListener;

        listView.setOnScrollListener(this);
        swipeRefreshLayout.setOnRefreshListener(this);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {

            if (isLastItem) {
                swipeRefreshLayout.setRefreshing(true);
                if (refreshlistener != null) {
                    refreshlistener.pushToRefresh();
                }

            }

            if (stateChangeListener!=null){
                stateChangeListener.scrollStop();
            }

        } else {
            if (stateChangeListener!=null){
                stateChangeListener.scrollStar();
            }
        }

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        isLastItem = (totalItemCount - 1) == listView.getLastVisiblePosition();
//        LogUtils.logD("totalitemCount = " + totalItemCount + " lastItemcount = " + listView.getLastVisiblePosition());
    }

    @Override
    public void onRefresh() {

        swipeRefreshLayout.setRefreshing(true);
        if (refreshlistener != null) {
            refreshlistener.pullToRefresh();
        }

    }

    /**
     * 刷新回调
     */
    public interface RefreshListener {
        //上拉刷新
        void pushToRefresh();

        //下拉刷新
        void pullToRefresh();

    }

    public interface ScrollStateChange {
        void scrollStar();

        void scrollStop();
    }
}

