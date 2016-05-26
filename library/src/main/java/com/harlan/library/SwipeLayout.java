package com.harlan.library;

import android.content.Context;
import android.graphics.Rect;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by Harlan1994 on 2016/5/26.
 */
public class SwipeLayout extends FrameLayout {

    private final ViewDragHelper mViewDragHelper;

    private View mLeftView;// view that shows at the beginning.
    private View mRightView;// view that shows if dragged out.

    private int mLeftWidth;//width of left view.
    private int mRightWidth;//width of right view.
    private int mWidth;//width of this viewgroup
    private int mHeight;//height of this viewgroup

    private int mRange;//horizontal range of drag action.

    public SwipeLayout(Context context) {
        this(context, null);
    }

    public SwipeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        //Initialize ViewDragHelper
        mViewDragHelper = ViewDragHelper.create(this, mCallback);
    }

    private ViewDragHelper.Callback mCallback = new ViewDragHelper.Callback() {
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return true;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            /**
             * left is the Rect.left of child, so different child, different ops.
             */
            if (child == mRightView) {
                if (left > mWidth) {
                    return mWidth;
                } else if (left < mWidth - mRange) {
                    left = mWidth - mRange;
                }
            } else {
                if (left > 0) {//the front view can't be dragged left.
                    left = 0;
                } else {//the front view can be dragged right, but can't beyong its range.
                    if (left < -mRange) {
                        left = -mRange;
                    }
                }
            }
            return left;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);

            if (changedView == mLeftView) {
                mRightView.offsetLeftAndRight(dx);
            } else if (changedView == mRightView) {
                mLeftView.offsetLeftAndRight(dx);
            }

            // TODO: 2016/5/26  dispatch Swipe event to interact with users.

            invalidate();
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {

            if (xvel == 0 && mLeftView.getLeft() < -mRange / 2.0f) {
                open();
            } else if (xvel < 0) {
                open();
            } else {
                close();
            }
        }
    };

    public void close() {
        close(true);
    }

    public void close(boolean isSmooth) {
        int finalLeft = 0;
        if (isSmooth) {
            mViewDragHelper.smoothSlideViewTo(mLeftView, finalLeft, 0);
            ViewCompat.postInvalidateOnAnimation(this);
        } else {
            //place it with no animation.
            layoutContent(false);
        }
    }

    public void open() {
        open(true);
    }

    public void open(boolean isSmooth) {
        int finalLeft = -mRange;
        if (isSmooth) {
            mViewDragHelper.smoothSlideViewTo(mLeftView, finalLeft, 0);
            ViewCompat.postInvalidateOnAnimation(this);
        } else {
            //place it with no animation.
            layoutContent(true);
        }
    }

    /**
     * Get the subviews.
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mLeftView = getChildAt(0);
        mRightView = getChildAt(1);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mLeftWidth = mLeftView.getMeasuredWidth();
        mRightWidth = mRightView.getMeasuredWidth();

        mWidth = mLeftWidth;
        mHeight = mLeftView.getMeasuredHeight();

        mRange = mRightWidth;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        layoutContent(false);
    }

    /**
     * layout subviews.
     *
     * @param isOpen
     */
    private void layoutContent(boolean isOpen) {

        //left view
        Rect leftRect = computeLeftViewRect(isOpen);
        mLeftView.layout(leftRect.left, leftRect.top, leftRect.right, leftRect.bottom);

        //right view
        Rect rightRect = computRightViewRect(leftRect);
        mRightView.layout(rightRect.left, rightRect.top, rightRect.right, rightRect.bottom);

        //bring left view to front
        bringChildToFront(mLeftView);
    }

    /**
     * compute the Rect of left view.
     *
     * @param isOpen
     * @return Rect
     */
    private Rect computeLeftViewRect(boolean isOpen) {
        int left = 0;
        if (isOpen) {
            left = -mRange;
        }
        return new Rect(left, 0, left + mWidth, mHeight);
    }

    private Rect computRightViewRect(Rect leftViewRect) {
        int left = leftViewRect.right;
        return new Rect(left, 0, left + mRightWidth, mHeight);
    }

    @Override
    public void computeScroll() {
        if (mViewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mViewDragHelper != null) {
            mViewDragHelper.processTouchEvent(event);
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mViewDragHelper != null) {
            return mViewDragHelper.shouldInterceptTouchEvent(ev);
        }
        return super.onInterceptTouchEvent(ev);
    }
}
