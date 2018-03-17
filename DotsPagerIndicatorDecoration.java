package com.greenberg.packagename;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

public class DotsPagerIndicatorDecoration extends RecyclerView.ItemDecoration {

    private int colorActive = 0xFFFFFFFF;
    private int colorInactive = 0x66FFFFFF;

    private static final float DP = Resources.getSystem().getDisplayMetrics().density;

    /**
     * Height of the space the indicator takes up at the bottom of the view.
     */
    private final int mIndicatorHeight = (int) (DP * 16);

    /**
     * Indicator stroke width.
     */
    private final float mIndicatorStrokeWidth = DP * 2;

    /**
     * Indicator width.
     */
    private final float mIndicatorItemLength = DP * 8;
    /**
     * Padding between indicators.
     */
    private final float mIndicatorItemPadding = DP * 4;

    private final float mIndicatorPaddingFromBottom = DP * 2;

    /**
     * Some more natural animation interpolation
     */
    private final Interpolator mInterpolator = new AccelerateDecelerateInterpolator();

    private final Paint mPaint = new Paint();

    public DotsPagerIndicatorDecoration() {
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(mIndicatorStrokeWidth);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(c, parent, state);

        int itemCount = parent.getAdapter().getItemCount();

        // center horizontally, calculate width and subtract half from center
        float totalLength = mIndicatorItemLength * itemCount;
        float paddingBetweenItems = Math.max(0, itemCount - 1) * mIndicatorItemPadding;
        float indicatorTotalWidth = totalLength + paddingBetweenItems;
        float indicatorStartX = (parent.getWidth() - indicatorTotalWidth) / 2F;

        // center vertically in the allotted space
        float indicatorPosY = parent.getHeight() - mIndicatorHeight / 2F + mIndicatorPaddingFromBottom;

        drawInactiveIndicators(c, indicatorStartX, indicatorPosY, itemCount);


        // find active page (which should be highlighted)
        LinearLayoutManager layoutManager = (LinearLayoutManager) parent.getLayoutManager();
        int activePosition = layoutManager.findFirstVisibleItemPosition();
        if (activePosition == RecyclerView.NO_POSITION) {
            return;
        }

        // find offset of active page (if the user is scrolling)
        final View activeChild = layoutManager.findViewByPosition(activePosition);
        int left = activeChild.getLeft();
        int width = activeChild.getWidth();

        // on swipe the active item will be positioned from [-width, 0]
        // interpolate offset for smooth animation
        float progress = mInterpolator.getInterpolation(left * -1 / (float) width);

        drawHighlights(c, indicatorStartX, indicatorPosY, activePosition, progress, itemCount);
    }

    private void drawInactiveIndicators(Canvas c, float indicatorStartX, float indicatorPosY, int itemCount) {
        mPaint.setColor(colorInactive);

        // width of item indicator including padding
        final float itemWidth = mIndicatorItemLength + mIndicatorItemPadding;

        float start = indicatorStartX;
        for (int i = 0; i < itemCount; i++) {
            // draw the dot for every item
            c.drawCircle(start + mIndicatorItemLength / 2, indicatorPosY, mIndicatorItemLength / 2, mPaint);
            start += itemWidth;
        }
    }

    private void drawHighlights(Canvas c, float indicatorStartX, float indicatorPosY,
                                int highlightPosition, float progress, int itemCount) {
        mPaint.setColor(colorActive);

        // width of item indicator including padding
        final float itemWidth = mIndicatorItemLength + mIndicatorItemPadding;

        if (progress == 0F) {
            // no swipe, draw a normal indicator
            float highlightStart = indicatorStartX + itemWidth * highlightPosition;
            c.drawCircle(highlightStart + mIndicatorItemLength / 2, indicatorPosY, mIndicatorItemLength / 2, mPaint);
        } else {
            float highlightStart = indicatorStartX + itemWidth * highlightPosition;
            float centerHighlightDot = highlightStart + mIndicatorItemLength / 2;

            //draw minimizing circle
            if (progress <= 0.5) {
                c.drawCircle(centerHighlightDot, indicatorPosY, mIndicatorItemLength / 2 * (1 - progress * 2), mPaint);
            }

            //draw growing circle
            if (progress > 0.5) {
                c.drawCircle(centerHighlightDot + mIndicatorItemLength + mIndicatorItemPadding, indicatorPosY, mIndicatorItemLength / 2 * (progress * 2 - 1), mPaint);
            }

            // draw the connecting highlight
            float startLinePosition, endLinePosition;
            if (progress <= 0.5) {
                startLinePosition = centerHighlightDot;
                endLinePosition = centerHighlightDot + (mIndicatorItemLength / 2 + mIndicatorItemPadding * 2) * progress * 2;
            } else {
                startLinePosition = centerHighlightDot + (mIndicatorItemLength / 2 + mIndicatorItemPadding * 2) * (progress * 2 - 1);
                endLinePosition = centerHighlightDot + mIndicatorItemLength / 2 + mIndicatorItemPadding * 2;
            }

            c.drawLine(startLinePosition, indicatorPosY,
                    endLinePosition, indicatorPosY, mPaint);
        }
    }
}
