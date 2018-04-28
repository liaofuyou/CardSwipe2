package me.ajax.cardswipe2.layoutmanager;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.view.View;

/**
 * Created by aj on 2018/4/25
 */

public class CardLayoutManger extends RecyclerView.LayoutManager {

    private SparseBooleanArray mHasAttachedItems = new SparseBooleanArray();
    private SparseArray<Rect> mAllItemFrames = new SparseArray<>();
    private SparseIntArray mAllItemScales = new SparseIntArray();
    private int mAllOffset = 0;
    private int viewWidth;
    private int viewHeight;
    private int topCardIndex = 0;
    private boolean isRight = true;


    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(-2, -2);
    }

    @Override
    public void onScrollStateChanged(int state) {
        if (state == RecyclerView.SCROLL_STATE_IDLE) {

        }
    }


    @Override
    public boolean canScrollHorizontally() {
        return true;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {

        isRight = dx < 0;
        mAllOffset += -dx;
        mAllOffset = Math.min(mAllOffset, 0);
        mAllOffset = Math.max(mAllOffset, -getItemCount() * viewWidth);

        topCardIndex = mAllOffset / -viewWidth;
        topCardIndex = Math.min(topCardIndex, getItemCount() - 1);

        refreshValues();

        layoutItems(recycler);

        return dx;
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getItemCount() == 0 || state.isPreLayout()) return;

        View view = recycler.getViewForPosition(0);
        addView(view);
        measureChildWithMargins(view, 0, 0);
        viewWidth = view.getMeasuredWidth();
        viewHeight = view.getMeasuredHeight();

        detachAndScrapAttachedViews(recycler);

        isRight = false;

        refreshValues();
        layoutItems(recycler);
    }

    private void refreshValues() {

        int topOffset = (getHeight() - viewWidth) / 2;
        float fraction = mAllOffset % viewWidth / (float) viewWidth;

        int count = 0;
        for (int i = topCardIndex; i < getItemCount(); i++, count++) {

            //===== 缩放 =====
            int scale = 100 - count * 10;
            scale = Math.max(scale, 70);

            //有缩放的变化
            if (count > 0 && count < 4) {
                scale -= 10 * fraction;
            }
            mAllItemScales.put(i, scale);

            //===== 滑动 =====

            int offset = count * dp(20);
            offset = Math.min(offset, 3 * dp(20));

            //有滑动的变化
            if (count > 0 && count < 4) {
                offset += dp(20) * fraction;
            }

            Rect rect = mAllItemFrames.get(i);
            if (rect == null) rect = new Rect();
            rect.set(offset, topOffset, offset + viewWidth, topOffset + viewHeight);
            mAllItemFrames.put(i, rect);
        }

        Rect rect = mAllItemFrames.get(topCardIndex);
        rect.left = mAllOffset + topCardIndex * viewWidth;
        rect.right = rect.left + viewWidth;
    }

    private void layoutItems(RecyclerView.Recycler recycler) {

        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            int position = getPosition(view);
            //l(position, mAllItemFrames.get(position).left, -viewWidth, mAllItemFrames.get(position).left <= -viewWidth);
            if (position < topCardIndex) {
                removeAndRecycleView(view, recycler);
                mHasAttachedItems.put(position, false);
            } else {
                layoutItem(view, position);
                mHasAttachedItems.put(position, true);
            }
        }

        for (int i = topCardIndex; i < topCardIndex + 5; i++) {

            if (i >= getItemCount() - 1) continue;
            if (mHasAttachedItems.get(i)) continue;

            View view = recycler.getViewForPosition(i);
            if (isRight) {
                addView(view, 0);
            } else {
                addView(view);
            }
            layoutItem(view, i);
        }
    }

    private void layoutItem(View view, int i) {

        measureChildWithMargins(view, 0, 0);
        Rect rect = mAllItemFrames.get(i);
        layoutDecorated(view, rect.left, rect.top, rect.right, rect.bottom);

        float scale = mAllItemScales.get(i) / 100F;
        view.setScaleX(scale);
        view.setScaleY(scale);
    }

    private int dp(float dp) {
        return (int) dp * 3;
    }

    static void l(Object... list) {
        String text = "";
        for (Object o : list) {
            text += "   " + o.toString();
        }
        Log.e("######", text);
    }
}
