package bpc.dis.readmorelayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

public class ReadMoreLayout extends LinearLayout {

    public final int WHAT = 2;
    public final int WHAT_ANIMATION_END = 3;
    public final int WHAT_EXPAND_ONLY = 4;
    public TextView textView;
    public boolean isShrink = false;
    public boolean isInitTextView = true;
    public int expandLines;
    public int textLines;
    private ReadMoreLayoutHandler handler = new ReadMoreLayoutHandler(this);
    private TextView tvState;
    private ImageView ivExpandOrShrink;
    private RelativeLayout rlToggleLayout;
    private Drawable drawableShrink;
    private Drawable drawableExpand;
    private int textViewStateColor;
    private String textExpand;
    private String textShrink;
    private int sleepTime = 22;
    private boolean isAnim = false;
    private OnExpandListener mOnExpandListener;

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
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        textView.setOnClickListener(null);
        rlToggleLayout.setOnClickListener(null);
//        textView.addTextChangedListener(null);
//        textView.getViewTreeObserver().removeOnPreDrawListener(onPreDrawListener);
    }

    @Override
    public void onViewRemoved(View child) {
        super.onViewRemoved(child);
        textView.setOnClickListener(null);
        rlToggleLayout.setOnClickListener(null);
//        textView.addTextChangedListener(null);
//        textView.getViewTreeObserver().removeOnPreDrawListener(onPreDrawListener);
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
        textView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                clickToggle();
            }
        });
        rlToggleLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                clickToggle();
            }
        });
    }

    private void addTvPreDrawListener() {
        textView.getViewTreeObserver().addOnPreDrawListener(new ReadMoreLayoutOnPreDrawListener(this));
    }

    public void doAnimation(final int startIndex, final int endIndex, final int what) {
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

    public void changeExpandState(int endIndex) {
        rlToggleLayout.setVisibility(View.VISIBLE);
        if (endIndex < textLines) {
            ivExpandOrShrink.setImageDrawable(drawableExpand);
            tvState.setText(textExpand);
        } else {
            ivExpandOrShrink.setImageDrawable(drawableShrink);
            tvState.setText(textShrink);
        }
    }

    public void setExpandState(int endIndex) {
        if (endIndex < textLines) {
            isShrink = true;
            rlToggleLayout.setVisibility(View.VISIBLE);
            ivExpandOrShrink.setImageDrawable(drawableExpand);
            textView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickToggle();
                }
            });
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

    public void doNotExpand() {
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