package bpc.dis.readmorelayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

public class ReadMoreLayout extends LinearLayout implements View.OnClickListener {

    private final int WHAT = 2;
    private final int WHAT_ANIMATION_END = 3;
    private final int WHAT_EXPAND_ONLY = 4;
    private TextView textView;
    private TextView tvState;
    private ImageView ivExpandOrShrink;
    private RelativeLayout rlToggleLayout;
    private Drawable drawableShrink;
    private Drawable drawableExpand;
    private int textViewStateColor;
    private String textExpand;
    private String textShrink;
    private boolean isShrink = false;
    private boolean isInitTextView = true;
    private int expandLines;
    private int textLines;
    private int sleepTime = 22;
    private boolean isAnim = false;
    private OnExpandListener mOnExpandListener;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (WHAT == msg.what) {
                textView.setMaxLines(msg.arg1);
                textView.invalidate();
            } else if (WHAT_ANIMATION_END == msg.what) {
                setExpandState(msg.arg1);
            } else if (WHAT_EXPAND_ONLY == msg.what) {
                changeExpandState(msg.arg1);
            }
            super.handleMessage(msg);
        }

    };

    private ViewTreeObserver.OnPreDrawListener onPreDrawListener = new ViewTreeObserver.OnPreDrawListener() {
        @Override
        public boolean onPreDraw() {
            if (!isInitTextView) {
                return true;
            }
            textLines = textView.getLineCount();
            boolean isExpandNeeded = textLines > expandLines;
            isInitTextView = false;
            if (isExpandNeeded) {
                isShrink = true;
                doAnimation(textLines, expandLines, WHAT_ANIMATION_END);
            } else {
                isShrink = false;
                doNotExpand();
            }
            return true;
        }
    };

    public ReadMoreLayout(Context context) {
        this(context, null);
    }

    public ReadMoreLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ReadMoreLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.initValue(attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        View textView = getChildAt(0);
        if (!(textView instanceof TextView)) {
            throw new RuntimeException("The first view must be a 'TextView'!");
        } else {
            this.textView = (TextView) textView;
        }
        View exView = View.inflate(getContext(), R.layout.layout_expand, null);
        addView(exView, new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        initView();
        initClick();
        postInvalidate();
    }

    @Override
    public void onViewRemoved(View child) {
        super.onViewRemoved(child);
        if (handler != null) {
            handler.removeCallbacks(null);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.rl_expand_text_view_animation_toggle_layout || v == this.textView) {
            clickToggle();
        }
    }


    private void initValue(AttributeSet attrs) {
        setOrientation(LinearLayout.VERTICAL);
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.ReadMoreLayout);

        expandLines = ta.getInteger(R.styleable.ReadMoreLayout_rmlExpandLines, 5);
        drawableShrink = ta.getDrawable(R.styleable.ReadMoreLayout_rmlShrinkBitmap);
        drawableExpand = ta.getDrawable(R.styleable.ReadMoreLayout_rmlExpandBitmap);

        textViewStateColor = ta.getColor(R.styleable.ReadMoreLayout_rmlTextStateColor, ContextCompat.getColor(getContext(), android.R.color.black));

        textShrink = ta.getString(R.styleable.ReadMoreLayout_rmlTextShrink);
        textExpand = ta.getString(R.styleable.ReadMoreLayout_rmlTextExpand);

        if (null == drawableShrink) {
            drawableShrink = ContextCompat.getDrawable(getContext(), R.drawable.icon_green_arrow_up);
        }

        if (null == drawableExpand) {
            drawableExpand = ContextCompat.getDrawable(getContext(), R.drawable.icon_green_arrow_down);
        }

        if (TextUtils.isEmpty(textShrink)) {
            textShrink = getContext().getString(R.string.shrink);
        }

        if (TextUtils.isEmpty(textExpand)) {
            textExpand = getContext().getString(R.string.expand);
        }

        ta.recycle();
    }

    private void initView() {
        rlToggleLayout = findViewById(R.id.rl_expand_text_view_animation_toggle_layout);

        textView = (TextView) getChildAt(0);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.addTextChangedListener(onTextWatcher());
        addTvPreDrawListener();

        ivExpandOrShrink = findViewById(R.id.iv_expand_text_view_animation_toggle);

        tvState = findViewById(R.id.tv_expand_text_view_animation_hint);
        tvState.setTextColor(textViewStateColor);
    }

    private void initClick() {
        textView.setOnClickListener(this);
        rlToggleLayout.setOnClickListener(this);
    }


    private void addTvPreDrawListener() {
        ViewTreeObserver viewTreeObserver = textView.getViewTreeObserver();
        viewTreeObserver.addOnPreDrawListener(onPreDrawListener);
    }

    private void doAnimation(final int startIndex, final int endIndex, final int what) {

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                isAnim = true;
                if (startIndex < endIndex) {
                    int count = startIndex;
                    while (count++ < endIndex) {
                        Message msg = handler.obtainMessage(WHAT, count, 0);
                        try {
                            Thread.sleep(sleepTime);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        handler.sendMessage(msg);
                    }
                } else if (startIndex > endIndex) {
                    int count = startIndex;
                    while (count-- > endIndex) {
                        Message msg = handler.obtainMessage(WHAT, count, 0);
                        try {
                            Thread.sleep(sleepTime);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        handler.sendMessage(msg);
                    }
                }
                Message msg = handler.obtainMessage(what, endIndex, 0);
                handler.sendMessage(msg);
                isAnim = false;
            }
        });
        thread.start();
    }

    private void changeExpandState(int endIndex) {
        rlToggleLayout.setVisibility(View.VISIBLE);
        if (endIndex < textLines) {
            ivExpandOrShrink.setImageDrawable(drawableExpand);
            tvState.setText(textExpand);
        } else {
            ivExpandOrShrink.setImageDrawable(drawableShrink);
            tvState.setText(textShrink);
        }
    }

    private void setExpandState(int endIndex) {
        if (endIndex < textLines) {
            isShrink = true;
            rlToggleLayout.setVisibility(View.VISIBLE);
            ivExpandOrShrink.setImageDrawable(drawableExpand);
            textView.setOnClickListener(this);
            tvState.setText(textExpand);
        } else {
            isShrink = false;
            rlToggleLayout.setVisibility(View.GONE);
            ivExpandOrShrink.setImageDrawable(drawableShrink);
            textView.setOnClickListener(null);
            tvState.setText(textShrink);
        }

    }

    private void clickToggle() {
        if (isAnim) {
            return;
        }
        if (isShrink) {
            if (mOnExpandListener != null) {
                mOnExpandListener.onOpen();
            }
            doAnimation(expandLines, textLines, WHAT_EXPAND_ONLY);
        } else {
            if (mOnExpandListener != null) {
                mOnExpandListener.onClose();
            }
            doAnimation(textLines, expandLines, WHAT_EXPAND_ONLY);
        }
        isShrink = !isShrink;
    }

    private void doNotExpand() {
        textView.setMaxLines(expandLines);
        rlToggleLayout.setVisibility(View.GONE);
        textView.setOnClickListener(null);
    }

    private TextWatcher onTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                addTvPreDrawListener();
            }
        };
    }


    public int getSleepTime() {
        return sleepTime;
    }

    public void setSleepTime(int sleepTime) {
        this.sleepTime = sleepTime;
    }

    public void setOnExpandListener(OnExpandListener listener) {
        mOnExpandListener = listener;
    }

}