package com.nova.LemonDemo.utils;

import android.graphics.Rect;
import android.view.View;

/**
 * Created by Paraselene on 2017/8/9. Email ：15616165649@163.com
 */

public class VisibilePercentsUtils {

    private static VisibilePercentsUtils mVisibilePercentsUtils = null;

    private VisibilePercentsUtils() {
    }

    public static VisibilePercentsUtils getInstance() {
        if (mVisibilePercentsUtils == null) {
            synchronized (VisibilePercentsUtils.class) {
                if (mVisibilePercentsUtils == null) {
                    mVisibilePercentsUtils = new VisibilePercentsUtils();
                }
            }
        }
        return mVisibilePercentsUtils;
    }

    private final Rect mCurrentViewRect = new Rect();

    public int getVisibilityPercents(View view) {

        int percents = 100;

        view.getLocalVisibleRect(mCurrentViewRect);

        int height = view.getHeight();

        if (viewIsPartiallyHiddenTop()) {
            // view is partially hidden behind the top edge
            percents = (height - mCurrentViewRect.top) * 100 / height;
        } else if (viewIsPartiallyHiddenBottom(height)) {
            percents = mCurrentViewRect.bottom * 100 / height;
        }

        return percents;
    }

    private boolean viewIsPartiallyHiddenBottom(int height) {
        return mCurrentViewRect.bottom > 0 && mCurrentViewRect.bottom < height;
    }

    private boolean viewIsPartiallyHiddenTop() {
        return mCurrentViewRect.top > 0;
    }
}
