package org.schabi.newpipe.views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.material.tabs.TabLayout;

import org.schabi.newpipe.R;

/**
 * A TabLayout that is scrollable when tabs exceed its width and adopts the glass pill style.
 * Hides when there are less than 2 tabs.
 */
public class ScrollableTabLayout extends TabLayout {
    private int layoutWidth = 0;
    private int prevVisibility = View.GONE;

    public ScrollableTabLayout(final Context context) {
        super(context);
        applyGlassStyle();
    }

    public ScrollableTabLayout(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        applyGlassStyle();
    }

    public ScrollableTabLayout(final Context context, final AttributeSet attrs,
                               final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        applyGlassStyle();
    }

    private void applyGlassStyle() {
        super.setBackgroundResource(R.drawable.glass_tab_pill);
        super.setTabIconTint(ColorStateList.valueOf(Color.WHITE));
        super.setTabRippleColor(ColorStateList.valueOf(0x33FFFFFF));
        super.setSelectedTabIndicatorColor(0xFFC6D8F0);
        setElevation(getResources().getDisplayMetrics().density * 8);
        setTabMode(MODE_SCROLLABLE);
        setTabGravity(GRAVITY_CENTER);
    }

    /** Keep callers that update service/theme colors from replacing the glass pill. */
    @Override
    public void setBackgroundColor(final int color) {
        super.setBackgroundResource(R.drawable.glass_tab_pill);
    }

    @Override
    protected void onLayout(final boolean changed, final int l, final int t, final int r,
                            final int b) {
        super.onLayout(changed, l, t, r, b);
        remeasureTabs();
    }

    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        layoutWidth = w;
    }

    @Override
    public void addTab(@NonNull final Tab tab, final int position, final boolean setSelected) {
        super.addTab(tab, position, setSelected);
        hasMultipleTabs();
        if (getTabMode() != MODE_SCROLLABLE) {
            remeasureTabs();
        }
    }

    @Override
    public void removeTabAt(final int position) {
        super.removeTabAt(position);
        hasMultipleTabs();
        if (getTabMode() != MODE_FIXED) {
            remeasureTabs();
        }
    }

    @Override
    protected void onVisibilityChanged(final View changedView, final int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (changedView == this) {
            if (prevVisibility == View.INVISIBLE) {
                remeasureTabs();
            }
            prevVisibility = visibility;
        }
    }

    private void setMode(final int mode) {
        if (mode != getTabMode()) {
            setTabMode(mode);
        }
    }

    private void hasMultipleTabs() {
        setVisibility(getTabCount() > 1 ? View.VISIBLE : View.GONE);
    }

    private void remeasureTabs() {
        if (prevVisibility != View.VISIBLE || layoutWidth == 0) {
            return;
        }
        int contentWidth = 0;
        for (int i = 0; i < getTabCount(); i++) {
            final View child = getTabAt(i).view;
            if (child.getVisibility() == View.VISIBLE) {
                contentWidth += Math.max(child.getMinimumWidth(), child.getMeasuredWidth());
            }
        }
        setMode(contentWidth > layoutWidth ? MODE_SCROLLABLE : MODE_FIXED);
        setTabGravity(GRAVITY_CENTER);
    }
}
