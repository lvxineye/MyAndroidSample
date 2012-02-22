package com.alex.android;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.AbsListView.OnScrollListener;
import android.widget.TextView;

public class Drag2RefreshView extends ListView implements OnScrollListener {

	private static final int TAP_TO_REFRESH = 1;
	private static final int DRAG_TO_REFRESH = 2;
	private static final int RELEASE_TO_REFRESH = 3;
	private static final int REFRESHING = 4;

	private RotateAnimation mFlipAnimation;
	private RotateAnimation mReverseFlipAnimation;
	private LayoutInflater mInflater;
	private LinearLayout mRefreshView;
	private TextView mText;
	private TextView mUpdateText;
	private ProgressBar mProgressBar;
	private ImageView mArrowImg;
	private int mRefreshViewHeight;

	private OnRefreshListener mOnRefreshListener;
	private OnScrollListener mOnScrollListener;
	private MotionEvent moveEvent;

	private int mRefreshState;
	private int mRefreshOriginalTopPadding;
	private int mLastMotionY;
	private int mCurrentScrollState;

	public Drag2RefreshView(Context context) {
		super(context);
		init(context);
	}

	public Drag2RefreshView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public Drag2RefreshView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public void init(Context context) {
		mFlipAnimation = new RotateAnimation(0, -180, RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		mFlipAnimation.setInterpolator(new LinearInterpolator());
		mFlipAnimation.setDuration(250);
		mFlipAnimation.setFillAfter(true);

		mReverseFlipAnimation = new RotateAnimation(-180, 0, RotateAnimation.RELATIVE_TO_SELF,
				0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		mReverseFlipAnimation.setInterpolator(new LinearInterpolator());
		mReverseFlipAnimation.setDuration(250);
		mReverseFlipAnimation.setFillAfter(true);

		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mRefreshView = (LinearLayout) mInflater.inflate(R.layout.drag_refresh_head, null);

		mText = (TextView) mRefreshView.findViewById(R.id.drag_refresh_text);
		mProgressBar = (ProgressBar) mRefreshView.findViewById(R.id.refresh_progress);
		mArrowImg = (ImageView) mRefreshView.findViewById(R.id.drag_refresh_img);
		mUpdateText = (TextView) mRefreshView.findViewById(R.id.drag_refresh_text_update);

		mArrowImg.setMinimumHeight(50);
		mRefreshView.setOnClickListener(new OnClickRefreshListener());
		mRefreshOriginalTopPadding = mRefreshView.getPaddingTop();
		mRefreshState = TAP_TO_REFRESH;

		addHeaderView(mRefreshView);

		super.setOnScrollListener(this);
		measureView(mRefreshView);
		mRefreshViewHeight = mRefreshView.getMeasuredHeight();
	}

	private void applyHeaderPadding(MotionEvent ev) {
		final int historySize = ev.getHistorySize();

		// Workaround for getPointCount() which is unavailable in 1.5(it's
		// always 1 in 1.5).
		int pointerCount = 1;
		try {
			Method method = MotionEvent.class.getMethod("getPointerCount");
			pointerCount = (Integer) method.invoke(ev);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			pointerCount = 1;
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

		for (int h = 0; h < historySize; h++) {
			for (int p = 0; p < pointerCount; p++) {
				if (RELEASE_TO_REFRESH == mRefreshState) {
					if (isVerticalFadingEdgeEnabled()) {
						setVerticalScrollBarEnabled(false);
					}

					int historicalY = 0;
					try {
						// For Android > 2.0
						Method method = MotionEvent.class.getMethod("getHistoricalY", Integer.TYPE,
								Integer.TYPE);
						historicalY = ((Float) method.invoke(ev, p, h)).intValue();
					} catch (SecurityException e) {
						e.printStackTrace();
					} catch (NoSuchMethodException e) {
						// For Android < 2.0
						historicalY = (int) (ev.getHistoricalY(h));
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					}

					// Calculate the padding to apply, we divide by 1.7 to
					// simulate a more resistant effect during pull.
					int topPadding = (int) (((historicalY - mLastMotionY) - mRefreshViewHeight) / 3.5);
					mRefreshView.setPadding(mRefreshView.getPaddingLeft(), topPadding,
							mRefreshView.getPaddingRight(), mRefreshView.getPaddingBottom());
				}
			}
		}
	}

	public void PrepareForRefresh() {
		resetHeaderPadding();

		mArrowImg.setVisibility(View.GONE);
		mArrowImg.setImageDrawable(null);
		mProgressBar.setVisibility(View.VISIBLE);

		mText.setText(R.string.drag_to_refresh_refreshing_label);

		mRefreshState = REFRESHING;
	}

	public void onRefresh() {
		if (null != mOnRefreshListener) {
			mOnRefreshListener.onRefresh();
		}
	}

	public void onRefreshComplete(CharSequence lastUpdated) {
		setLastUpdated(lastUpdated);
		onRefreshComplete();
	}

	public void onRefreshComplete() {
		resetHeader();

		// If refresh view is visible when loading completes, scroll down to the
		// next item.
		if (0 < mRefreshView.getBottom()) {
			invalidateViews();
			setSelection(1);
		}
	}

	private void resetHeader() {
		if (null != moveEvent) {
			mLastMotionY = (int) moveEvent.getY();
		}
		if (TAP_TO_REFRESH != mRefreshState) {
			mRefreshState = TAP_TO_REFRESH;

			resetHeaderPadding();

			mText.setText(R.string.drag_to_refresh_tap_label);
			mArrowImg.setImageResource(R.drawable.drag_refresh_arrow);
			mArrowImg.clearAnimation();
			mArrowImg.setVisibility(View.GONE);
			mProgressBar.setVisibility(View.GONE);
		}
	}

	private void resetHeaderPadding() {
		mRefreshView.setPadding(mRefreshView.getPaddingLeft(), mRefreshOriginalTopPadding,
				mRefreshView.getPaddingRight(), mRefreshView.getPaddingBottom());
	}

	private void measureView(View view) {
		ViewGroup.LayoutParams params = view.getLayoutParams();
		if (null == params) {
			params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
		}

		int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0, params.width);
		int lpHeight = params.height;
		int childHeightSpec;
		if (0 < lpHeight) {
			childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
		} else {
			childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		}
		view.measure(childWidthSpec, childHeightSpec);
	}

	public void setLastUpdated(CharSequence lastUpdated) {
		if (null == lastUpdated) {
			mUpdateText.setVisibility(View.GONE);
		} else {
			mUpdateText.setVisibility(View.VISIBLE);
			mUpdateText.setText(lastUpdated);
		}
	}

	private class OnClickRefreshListener implements OnClickListener {

		public void onClick(View v) {
			if (REFRESHING != mRefreshState) {
				PrepareForRefresh();
				onRefresh();
			}
		}
	}

	public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
		mOnRefreshListener = onRefreshListener;
	}

	public interface OnRefreshListener {
		public void onRefresh();
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		final int y = (int) ev.getY();
		switch (ev.getAction()) {
		case MotionEvent.ACTION_UP:
			if (!isVerticalScrollBarEnabled()) {
				setVerticalScrollBarEnabled(true);
			}
			if (0 == getFirstVisiblePosition() && REFRESHING != mRefreshState) {
				if ((mRefreshView.getBottom() > mRefreshViewHeight || 0 <= mRefreshView.getTop())
						&& RELEASE_TO_REFRESH == mRefreshState) {
					mRefreshState = REFRESHING;
					PrepareForRefresh();
					onRefresh();
				} else if (mRefreshView.getBottom() < mRefreshViewHeight
						|| 0 > mRefreshView.getTop()) {
					resetHeader();
					setSelection(1);
				}
			}
			break;

		case MotionEvent.ACTION_DOWN:
			mLastMotionY = y;
			break;

		case MotionEvent.ACTION_MOVE:
			moveEvent = ev;
			applyHeaderPadding(ev);
			break;

		default:
			break;
		}

		return super.onTouchEvent(ev);
	}

	@Override
	protected void onAttachedToWindow() {
		setSelection(1);
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		super.setAdapter(adapter);
		setSelection(1);
	}

	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
			int totalItemCount) {
		// When the refresh view is completely visible, change the text and flip
		// the arrow drawable
		if (SCROLL_STATE_TOUCH_SCROLL == mCurrentScrollState && REFRESHING != mRefreshState) {
			if (0 == firstVisibleItem) {
				mArrowImg.setVisibility(View.VISIBLE);
				if ((mRefreshView.getBottom() > mRefreshViewHeight + 20 || 0 <= mRefreshView
						.getTop()) && RELEASE_TO_REFRESH != mRefreshState) {
					mText.setText(R.string.drag_to_refresh_release_label);
					mArrowImg.clearAnimation();
					mArrowImg.startAnimation(mFlipAnimation);
					mRefreshState = RELEASE_TO_REFRESH;
				} else if ((mRefreshView.getBottom() < mRefreshViewHeight + 20 && 0 > mRefreshView
						.getTop()) && DRAG_TO_REFRESH != mRefreshState) {
					mText.setText(R.string.drag_to_refresh_pull_label);
					if (TAP_TO_REFRESH != mRefreshState) {
						mArrowImg.clearAnimation();
						mArrowImg.startAnimation(mReverseFlipAnimation);
					}
					mRefreshState = DRAG_TO_REFRESH;
				}
			} else {
				mArrowImg.setVisibility(View.GONE);
				resetHeader();
			}
		} else if (SCROLL_STATE_FLING == mCurrentScrollState && 0 == firstVisibleItem
				&& REFRESHING != mRefreshState) {
			setSelection(1);
		}

		if (null != mOnScrollListener) {
			mOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
		}

	}

	public void onScrollStateChanged(AbsListView view, int scrollState) {
		mCurrentScrollState = scrollState;

		if (null != mOnScrollListener) {
			mOnScrollListener.onScrollStateChanged(view, scrollState);
		}
	}
}
