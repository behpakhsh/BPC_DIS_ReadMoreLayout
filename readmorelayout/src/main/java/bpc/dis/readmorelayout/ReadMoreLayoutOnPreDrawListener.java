package bpc.dis.readmorelayout;

import android.view.ViewTreeObserver;

import java.lang.ref.WeakReference;

public class ReadMoreLayoutOnPreDrawListener implements ViewTreeObserver.OnPreDrawListener {

    private final WeakReference<ReadMoreLayout> readMoreLayoutWeakReference;

    ReadMoreLayoutOnPreDrawListener(ReadMoreLayout readMoreLayout) {
        readMoreLayoutWeakReference = new WeakReference<>(readMoreLayout);
    }

    @Override
    public boolean onPreDraw() {
        ReadMoreLayout readMoreLayout = readMoreLayoutWeakReference.get();
        if (readMoreLayout != null) {
            if (!readMoreLayout.isInitTextView) {
                return true;
            }
            readMoreLayout.textLines = readMoreLayout.textView.getLineCount();
            boolean isExpandNeeded = readMoreLayout.textLines > readMoreLayout.expandLines;
            readMoreLayout.isInitTextView = false;
            if (isExpandNeeded) {
                readMoreLayout.isShrink = true;
                readMoreLayout.doAnimation(readMoreLayout.textLines, readMoreLayout.expandLines, readMoreLayout.WHAT_ANIMATION_END);
            } else {
                readMoreLayout.isShrink = false;
                readMoreLayout.doNotExpand();
            }
        }
        return true;
    }

}