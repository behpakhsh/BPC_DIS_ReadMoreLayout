package bpc.dis.readmorelayout;

import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;

public class ReadMoreLayoutHandler extends Handler {

    private final WeakReference<ReadMoreLayout> readMoreLayoutWeakReference;

    ReadMoreLayoutHandler(ReadMoreLayout readMoreLayout) {
        readMoreLayoutWeakReference = new WeakReference<>(readMoreLayout);
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        ReadMoreLayout readMoreLayout = readMoreLayoutWeakReference.get();
        if (readMoreLayout != null) {
            if (readMoreLayout.WHAT == msg.what) {
                readMoreLayout.textView.setMaxLines(msg.arg1);
                readMoreLayout.textView.invalidate();
            } else if (readMoreLayout.WHAT_ANIMATION_END == msg.what) {
                readMoreLayout.setExpandState(msg.arg1);
            } else if (readMoreLayout.WHAT_EXPAND_ONLY == msg.what) {
                readMoreLayout.changeExpandState(msg.arg1);
            }
        }
        super.handleMessage(msg);
    }

}