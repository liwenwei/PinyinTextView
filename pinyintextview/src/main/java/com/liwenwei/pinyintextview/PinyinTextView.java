package com.liwenwei.pinyintextview;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Html;
import android.text.Layout;
import android.text.Layout.Alignment;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.util.TypedValue;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;

/**
 * Displays pinyin and text to the user.<br/>
 * Here is a example how to use this widget in xml.
 * <pre>
 * &lt;com.uudove.pinyin.widget.PinyinTextView
 * android:id="@+id/pinyin_text_view"
 * android:layout_width="wrap_content"
 * android:layout_height="wrap_content"
 * app:horizontalSpacing="10dp"
 * app:verticalSpacing="10dp"
 * app:textColor="#ff0000"
 * app:textSize="20sp"/&gt;
 * </pre>
 *
 * @author liwenwei
 */
public class PinyinTextView extends View {
    /**
     * @hide
     */
    @IntDef({TYPE_PLAIN_TEXT, TYPE_PINYIN_AND_TEXT, TYPE_PINYIN})
    @Retention(RetentionPolicy.SOURCE)
    public @interface PinyinMode {
    }

    /**
     * draw only plain text
     */
    public static final int TYPE_PLAIN_TEXT = 1;
    /**
     * draw pinyin and text
     */
    public static final int TYPE_PINYIN_AND_TEXT = 2;
    /**
     * draw only pinyin
     */
    public static final int TYPE_PINYIN = 3;
    /**
     * draw type. Must be one value of {@link #TYPE_PINYIN_AND_TEXT} or {@link #TYPE_PLAIN_TEXT}
     */
    private int mDrawType = TYPE_PLAIN_TEXT;

    private static final float PINYIN_TEXT_SIZE_RADIO = 0.5F;

    /**
     * Text size in pixels<br/>
     * Def in xml <b>app:textSize=""</b>
     */
    private int mTextSize;

    /**
     * Pinyin text size in pixels, default value equals {@link #mTextSize} * {@value #PINYIN_TEXT_SIZE_RADIO}
     */
    private int mPinyinTextSize;

    /**
     * Text color.<br/>
     * Def attr in xml <b>app:textColor=""</b>
     */
    @ColorInt
    private int mTextColor;

    /**
     * Text color.<br/>
     * Def attr in xml <b>app:pinyinColor=""</b>
     */
    @ColorInt
    private int mPinyinColor;

    /**
     * spacing between 2 token.<br/>
     * Def attr in xml <b>app:horizontalSpacing=""</b>
     */
    private int mHorizontalSpacing = 6;

    /**
     * Line spacing.<br/>
     * Def attr in xml <b>app:verticalSpacing=""</b>
     */
    private int mLineSpacing = 10;

    /**
     * The vertical space(px) between text and underline
     */
    private int mUnderlineVerticalSpacing = 14;

    /**
     * line spacing (between pinyin and text).
     */
    private int mPinyinTextSpacing = 3;

    /**
     * Show the underline or not
     */
    private boolean mUnderline = false;

    // text & pinyin string
    private String mPlainTextString;
    private String mTextString;
    private String mPinyinString;

    // calculated height of text or pinyin
    private int mTextHeight;
    private int mPinyinHeight;

    // Pinyin data
    private List<PinyinCompat> mPinyinCompats = new ArrayList<>();
    private List<Token> mPinyinTokens = new ArrayList<>();

    // text & pinyin paint
    private TextPaint mPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
    // underline
    private Paint mUnderlinePaint = new Paint();

    // bounds
    private Rect mBounds = new Rect();

    // for draw plain text
    private StaticLayout mStaticLayout;

    private boolean debugDraw = false; //  for debug, set false when release
    private Paint mDebugPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public PinyinTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public PinyinTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public PinyinTextView(Context context) {
        super(context);
        init(context, null);
    }

    private void init(Context context, AttributeSet attrs) {
        if (this.isInEditMode()) { // eclipse preview mode
            return;
        }

        initDefault(); // initialize default value

        if (attrs == null) {
            return;
        }

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PinyinTextView);
        if (a.hasValue(R.styleable.PinyinTextView_textSize)) {
            mTextSize = a.getDimensionPixelSize(R.styleable.PinyinTextView_textSize, mTextSize);
        }
        if (a.hasValue(R.styleable.PinyinTextView_textColor)) {
            mTextColor = a.getColor(R.styleable.PinyinTextView_textColor, mTextColor);
        }
        if (a.hasValue(R.styleable.PinyinTextView_pinyinColor)) {
            mPinyinColor = a.getColor(R.styleable.PinyinTextView_pinyinColor, mPinyinColor);
        } else {
            mPinyinColor = mTextColor;
        }
        if (a.hasValue(R.styleable.PinyinTextView_horizontalSpace)) {
            mHorizontalSpacing = a.getDimensionPixelSize(R.styleable.PinyinTextView_horizontalSpace, mHorizontalSpacing);
        }
        if (a.hasValue(R.styleable.PinyinTextView_lineSpace)) {
            mLineSpacing = a.getDimensionPixelSize(R.styleable.PinyinTextView_lineSpace, mLineSpacing);
        }
        if (a.hasValue(R.styleable.PinyinTextView_pinyinTextSpace)) {
            mPinyinTextSpacing = a.getDimensionPixelSize(R.styleable.PinyinTextView_pinyinTextSpace, mPinyinTextSpacing);
        }
        if (a.hasValue(R.styleable.PinyinTextView_underlineVerticalSpace)) {
            mUnderlineVerticalSpacing = a.getDimensionPixelSize(R.styleable.PinyinTextView_underlineVerticalSpace, mUnderlineVerticalSpacing);
        }
        if (a.hasValue(R.styleable.PinyinTextView_underline)) {
            mUnderline = a.getBoolean(R.styleable.PinyinTextView_underline, mUnderline);
        }
        a.recycle();
        setTextSize(mTextSize);
    }

    private void initDefault() {
        Context c = getContext();
        Resources r;

        if (c == null) {
            r = Resources.getSystem();
        } else {
            r = c.getResources();
        }

        DisplayMetrics dm = r.getDisplayMetrics();

        // Text size default 14sp
        mTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14, dm);
        mPinyinTextSize = (int) (mTextSize * PINYIN_TEXT_SIZE_RADIO);

        // spacing
        mHorizontalSpacing = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, dm);
        mLineSpacing = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mLineSpacing, dm);
        mPinyinTextSpacing = mHorizontalSpacing / 2;

        // set default text color
        mTextColor = 0xff333333;
        mPinyinColor = 0xff333333;

        mPaint.setStyle(Paint.Style.FILL);
        mDebugPaint.setStyle(Paint.Style.STROKE);
        mUnderlinePaint.setARGB(255, 0, 0, 0);
        mUnderlinePaint.setStyle(Paint.Style.STROKE);
        mUnderlinePaint.setPathEffect(new DashPathEffect(new float[]{2, 2, 2, 2}, 0));
        // The method setPathEffect is not supported by hardware acceleration. By default it is turned on (I think since Android 4.0)
        // turn off hardware acceleration
        this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    /**
     * Set plain text size in pixels<br/>
     * Def in xml <b>app:textSize=""</b>
     *
     * @param px - text size in pixels
     */
    public void setTextSize(int px) {
        if (px < 2) {
            throw new IllegalArgumentException("Text size must larger than 2px");
        }
        mTextSize = px;

        setPinyinTextSize((int) (px * PINYIN_TEXT_SIZE_RADIO));
    }

    /**
     * Get the plain text size.
     *
     * @return plain text size.
     */
    public int getTextSize() {
        return mTextSize;
    }

    public String getText() {
        return mPlainTextString;
    }

    /**
     * Set pinyin text size in pixels. If not set, pinyin text size will be the size of {@link #getTextSize()} *
     * {@value #PINYIN_TEXT_SIZE_RADIO}. <br/><br/>
     * Attention:<br/>
     * Don't use{@link #setTextSize(int)} method after this method is called, which will set pinyin text size to
     * {@link #getTextSize()} *  {@value #PINYIN_TEXT_SIZE_RADIO}.
     *
     * @param px - pinyin text size in pixels
     */
    private void setPinyinTextSize(int px) {
        mPinyinTextSize = px;
        if (mPinyinTextSize <= 0) {
            throw new IllegalArgumentException("Pinyin text size must larger than 1px");
        }

        // calculate text & pinyin height
        calTextHeight();

        requestLayout();
        invalidate();
    }

    /**
     * Set text color. The color must be @ColorInt(#xxxxxx, or getColor(R.color.yourColor),
     * not @ColorRes(like R.color.yourColor)
     * <p>
     * Def in xml <b>app:textColor=""</b>
     *
     * @param color text color.
     */
    public void setTextColor(@ColorInt int color) {
        mTextColor = color;
        for (Token token : mPinyinTokens) {
            token.setTextColor(mTextColor);
        }
        if (mPinyinTokens.isEmpty()) {
            requestLayout();
            invalidate();
        } else {
            setPinyinTextByTokens(mPinyinTokens, mDrawType);
        }
    }

    /**
     * Set pinyin text color.<br/>
     * Def in xml <b>app:pinyinColor=""</b>
     *
     * @param color pinyin text color.
     */
    public void setPinyinColor(@ColorInt int color) {
        mPinyinColor = color;
        for (Token token : mPinyinTokens) {
            token.setPinyinColor(mPinyinColor);
        }
        if (mPinyinTokens.isEmpty()) {
            requestLayout();
            invalidate();
        } else {
            setPinyinTextByTokens(mPinyinTokens, mDrawType);
        }
    }

    /**
     * Set line spacing in pixels.<br/>
     * The same as method {@link #setHorizontalSpacing(int)}
     *
     * @param px line spacing in pixels.
     * @see #setHorizontalSpacing(int)
     */
    public void setLineSpacing(int px) {
        mLineSpacing = px;
        requestLayout();
        invalidate();
    }

    public void setPinyinTextSpacing(int px) {
        mPinyinTextSpacing = px;
        requestLayout();
        invalidate();
    }

    public void setUnderlineVerticalSpacing(int px) {
        this.mUnderlineVerticalSpacing = px;
    }

    public boolean isShowUnderline() {
        return this.mUnderline;
    }

    public void setUnderline(boolean isShow) {
        this.mUnderline = isShow;
        invalidate();
    }

    /**
     * Set horizontal space between two tokens.<br/>
     * Def in xml <b>app:horizontalSpacing=""</b>
     *
     * @param px line spacing in pixels.
     */
    public void setHorizontalSpacing(int px) {
        mHorizontalSpacing = px;
        mPinyinTextSpacing = mHorizontalSpacing / 2; // half of line spacing
        requestLayout();
        invalidate();
    }

    public void setPinyinText(List<Pair<String, String>> pinyinList, @PinyinMode int mode) {
        mPinyinTokens.clear();
        for (Pair<String, String> pair : pinyinList) {
            Token token = new Token();
            token.setText(pair.first);
            token.setTextColor(mTextColor);
            // If the toke is punctuation, the pinyin is empty, set the pinyin as punctuation
            // why we do this?
            // because we need to draw the punctuation at TYPE_PINYIN and TYPE_PLAIN_TEXT mode
            if (TextUtils.isEmpty(pair.second) && isPunctuation(pair.first)) {
                token.setPinyin(pair.first);
            } else {
                token.setPinyin(pair.second);
            }
            token.setPinyinColor(mPinyinColor);
            mPinyinTokens.add(token);
        }
        setPinyinTextByTokens(mPinyinTokens, mode);
    }

    /**
     * Set the single pinyin.
     */
    public void setPinyinText(Pair<String, String> pair, @PinyinMode int mode) {
        List<Pair<String, String>> pairs = new ArrayList<>();
        pairs.add(pair);
        setPinyinText(pairs, mode);
    }

    /**
     * Init the PinyinTextView with Chinese-Pinyin pair list.
     *
     * @param pinyinList Chinese-Pinyin pair list, like <code>Pair.create("你", "nǐ")</code>, if the
     *                   string is special character, set the pinyin is empty string, like
     *                   <code>Pair.create("!", " ")</code>
     * @param mode
     */
    public void setPinyinTextByTokens(List<Token> pinyinList, @PinyinMode int mode) {
        mDrawType = mode; // set draw type
        clearAll(); // clear what is shown
        mPinyinTokens = pinyinList;
        StringBuilder plainTextBuilder = new StringBuilder();
        StringBuilder textBuilder = new StringBuilder();
        StringBuilder pinyinBuilder = new StringBuilder();
        for (Token token : pinyinList) {
            String src = token.getText();
            String trg = token.getPinyin();
            if (src == null) {
                src = "";
            }
            if (TextUtils.isEmpty(trg)) {
                trg = "";
            }
            pinyinBuilder.append(convertTokenToHtml(trg, convertColorHexString(token.pinyinColor)));
            textBuilder.append(convertTokenToHtml(src, convertColorHexString(token.textColor)));
            plainTextBuilder.append(src);

            PinyinCompat compat = new PinyinCompat();
            compat.text = src;
            compat.textColor = token.getTextColor() == 0 ? mTextColor : token.getTextColor();
            compat.pinyin = trg;
            compat.pinyinColor = token.getPinyinColor() == 0 ? mPinyinColor : token.getPinyinColor();
            compat.textRect = new Rect();
            compat.pinyinRect = new Rect();
            compat.pinyinTextRect = new Rect();
            mPinyinCompats.add(compat);
        }

        // string buffer
        mTextString = textBuilder.toString();
        mPlainTextString = plainTextBuilder.toString();
        mPinyinString = pinyinBuilder.toString();

        // calculate text & pinyin height
        calTextHeight();
        requestLayout();
        invalidate();
    }

    /**
     * Display only plain text to user, like TextView
     *
     * @param text plain text to display.
     */
    public void setText(String text) {
        mDrawType = TYPE_PLAIN_TEXT; // set draw type
        clearAll();
        this.mPlainTextString = text;
        this.mTextString = text;
        this.mPinyinString = text;
        requestLayout();
        invalidate();
    }

    public void setMode(@PinyinMode int mode) {
        mDrawType = mode;
        calTextHeight();
        requestLayout();
        invalidate();
    }

    /**
     * Set whether draw debug rect.
     *
     * @param debugDraw debug mode.
     */
    public void setDebugDraw(boolean debugDraw) {
        this.debugDraw = debugDraw;
    }

    private void clearAll() {
        mPinyinCompats.clear(); // clear

        mPlainTextString = null;
        mTextString = null;
        mPinyinString = null;

        mTextHeight = 0;
        mPinyinHeight = 0;
    }

    /**
     * calculate text & pinyin height
     * <p>
     * Why we calculate the text height by hard code text, not the {@link PinyinTextView#mTextString}
     * and {@link PinyinTextView#mPinyinString} ?
     * <p>
     * Sometimes, we have to align multiple PinyinTextView by horizontal, if we measure different text,
     * we get different height, so we have to measure the same text to keep the same height
     */
    private void calTextHeight() {
        // calculate text height
        String chinese = "你好";
        mPaint.setTextSize(mTextSize);
        mPaint.getTextBounds(chinese, 0, chinese.length(), mBounds);
        mTextHeight = mBounds.height();

        // calculate pinyin height
        String pinyin = "āáǎàaHhJjPpYyGg";
        if (mDrawType == TYPE_PINYIN) {
            mPaint.setTextSize(mTextSize);
        } else {
            mPaint.setTextSize(mPinyinTextSize);
        }
        mPaint.getTextBounds(pinyin, 0, pinyin.length() - 1, mBounds);
        mPinyinHeight = mBounds.height();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mPinyinCompats.isEmpty()) {
            measurePlainText(widthMeasureSpec, heightMeasureSpec);
        } else {
            if (mDrawType == TYPE_PINYIN_AND_TEXT) {
                measurePinyinText(widthMeasureSpec, heightMeasureSpec);
            } else if (mDrawType == TYPE_PLAIN_TEXT && !TextUtils.isEmpty(mTextString)) {
                measurePlainText(widthMeasureSpec, heightMeasureSpec);
            } else if (mDrawType == TYPE_PINYIN && !TextUtils.isEmpty(mPinyinString)) {
                measurePinyin(widthMeasureSpec, heightMeasureSpec);
            } else {
                measureDefault(widthMeasureSpec, heightMeasureSpec);
            }
        }
    }

    private void measureDefault(int widthMeasureSpec, int heightMeasureSpec) {

        // max allowed width or height
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);

        // mode
        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);

        // measured width and height
        int measuredWidth =
                modeWidth == MeasureSpec.EXACTLY ? sizeWidth : getPaddingLeft() + getPaddingRight();
        int measuredHeight =
                modeHeight == MeasureSpec.EXACTLY ? sizeHeight : getPaddingTop() + getPaddingBottom();

        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    @SuppressWarnings("PMD")
    private void measurePinyinText(int widthMeasureSpec, int heightMeasureSpec) {
        int paddingLeft = this.getPaddingLeft();
        int paddingRight = this.getPaddingRight();
        int paddingTop = this.getPaddingTop();
        int paddingBottom = this.getPaddingBottom();

        // max allowed width or height
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec) - paddingLeft - paddingRight;
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec) - paddingTop - paddingBottom;

        // mode
        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);

        // measured width and height
        int measuredWidth = modeWidth == MeasureSpec.EXACTLY ? sizeWidth : 0;
        int measuredHeight = modeHeight == MeasureSpec.EXACTLY ? sizeHeight : 0;

        int line = 0;
        int col = 0;
        int lineLength = 0;
        int baseLine = 0; // top of pinyin
        boolean newLine = false;

        for (PinyinCompat compat : mPinyinCompats) {
            int textWidth = getTextWidth(compat.text, mTextSize);
            int pinyinWidth = getTextWidth(compat.pinyin, mPinyinTextSize);

            int maxWidth = Math.max(textWidth, pinyinWidth);

            if (newLine) {
                line++;
                col = 0;
                newLine = false;
            }

            if (lineLength + maxWidth + (col == 0 ? 0 : mHorizontalSpacing) > sizeWidth) { // new row
                lineLength = maxWidth;

                baseLine += mTextHeight + mPinyinHeight + mPinyinTextSpacing + mLineSpacing;
                // TODO: add the underline vertical space if show underline

                if (modeWidth != MeasureSpec.EXACTLY) {
                    measuredWidth = sizeWidth;
                }

                newLine = true;
            } else {
                if (col != 0 || line != 0) { // not the first item of first row
                    lineLength += mHorizontalSpacing;
                }
                lineLength += maxWidth;

                if (modeWidth != MeasureSpec.EXACTLY && measuredWidth < lineLength) {
                    measuredWidth = lineLength;
                    if (measuredWidth > sizeWidth) {
                        measuredWidth = sizeWidth;
                    }
                }
                col++;
            }

            // Center the pinyin/text
            int pinyinBias = 0;
            int textBias = 0;
            if (pinyinWidth < textWidth) {
                pinyinBias = (textWidth - pinyinWidth) / 2;
            } else {
                textBias = (pinyinWidth - textWidth) / 2;
            }
            compat.pinyinRect.left = lineLength - maxWidth + pinyinBias;
            compat.pinyinRect.right = compat.pinyinRect.left + pinyinWidth;
            compat.pinyinRect.top = baseLine;
            compat.pinyinRect.bottom = compat.pinyinRect.top + mPinyinHeight;

            compat.textRect.left = lineLength - maxWidth + textBias;
            compat.textRect.right = compat.textRect.left + textWidth;
            compat.textRect.top = compat.pinyinRect.bottom + mPinyinTextSpacing;
            compat.textRect.bottom = compat.textRect.top + mTextHeight;

            compat.pinyinTextRect.left = lineLength - maxWidth;
            compat.pinyinTextRect.right = compat.pinyinRect.left + Math.max(pinyinWidth, textWidth);
            compat.pinyinTextRect.top = baseLine;
            compat.pinyinTextRect.bottom = compat.pinyinRect.top + mPinyinHeight + mPinyinTextSpacing + mTextHeight;
        }

        if (modeHeight != MeasureSpec.EXACTLY) {
            measuredHeight = baseLine + mPinyinHeight + mPinyinTextSpacing + mTextHeight + mTextHeight / 4;
        }

        setMeasuredDimension(measuredWidth + paddingLeft + paddingRight, measuredHeight + paddingTop + paddingBottom);
    }

    private void measurePlainText(int widthMeasureSpec, int heightMeasureSpec) {
        measureText(widthMeasureSpec, heightMeasureSpec, mTextString, mTextSize);
    }

    private void measurePinyin(int widthMeasureSpec, int heightMeasureSpec) {
        measureText(widthMeasureSpec, heightMeasureSpec, mPinyinString, mTextSize);
    }

    private void measureText(int widthMeasureSpec, int heightMeasureSpec, String text, float textSize) {
        int paddingLeft = this.getPaddingLeft();
        int paddingRight = this.getPaddingRight();
        int paddingTop = this.getPaddingTop();
        int paddingBottom = this.getPaddingBottom();

        // max allowed width or height
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec) - paddingLeft - paddingRight;
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec) - paddingTop - paddingBottom;

        // mode
        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);

        // calculate text width and height
        mPaint.setColor(mTextColor);
        mPaint.setTextSize(textSize);
        Spanned htmlSpan = fromHtml(text);
        mStaticLayout = new StaticLayout(htmlSpan, mPaint, sizeWidth, Alignment.ALIGN_NORMAL, 1.0f, 0, false);

        // measured width and height
        int measuredWidth =
                modeWidth == MeasureSpec.EXACTLY
                        ? sizeWidth
                        : Math.min(sizeWidth, (int) Math.ceil(Layout.getDesiredWidth(htmlSpan, mPaint)));
        int measuredHeight =
                modeHeight == MeasureSpec.EXACTLY
                        ? sizeHeight
                        : mStaticLayout.getHeight();
        if (mUnderline) {
            measuredHeight += mUnderlineVerticalSpacing;
        }

        setMeasuredDimension(measuredWidth + paddingLeft + paddingRight, measuredHeight + paddingTop + paddingBottom);
    }

    private String convertColorHexString(int color) {
        return String.format("#%06X", 0xFFFFFF & color);
    }

    private String convertTokenToHtml(String text, String textColor) {
        return String.format("<font color=\"%s\">%s</font>", textColor, text);
    }

    private Spanned fromHtml(String html) {
        if (html == null) {
            html = "";
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(html);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.isInEditMode()) { // eclipse preview mode
            return;
        }

        if (mPinyinCompats.isEmpty()) {
            drawPlainText(canvas);
        } else {
            if (mDrawType == TYPE_PINYIN_AND_TEXT) {
                drawPinyinAndText(canvas);
            } else if (mDrawType == TYPE_PLAIN_TEXT) {
                drawPlainText(canvas);
            } else if (mDrawType == TYPE_PINYIN) {
                drawPinyin(canvas);
            }
        }
    }

    private void drawPinyinAndText(Canvas canvas) {
        int paddingLeft = this.getPaddingLeft();
        int paddingTop = this.getPaddingTop();

        for (int i = 0; i < mPinyinCompats.size(); i++) {
            PinyinCompat compat = mPinyinCompats.get(i);

            // draw pinyin
            mPaint.setColor(compat.pinyinColor);
            mPaint.setTextSize(mPinyinTextSize);
            compat.pinyinRect.offset(paddingLeft, paddingTop);
            // If the draw mode is TYPE_PINYIN_AND_TEXT, don't draw the pinyin if it's punctuation
            if (!isPunctuation(compat.pinyin)) {
                canvas.drawText(compat.pinyin, compat.pinyinRect.left, compat.pinyinRect.bottom, mPaint);
            }

            // draw text
            mPaint.setColor(compat.textColor);
            mPaint.setTextSize(mTextSize);
            compat.textRect.offset(paddingLeft, paddingTop);
            canvas.drawText(compat.text, compat.textRect.left, compat.textRect.bottom, mPaint);

            if (mUnderline && !isPunctuation(compat.text)) {
                canvas.drawLine(
                        compat.pinyinTextRect.left,
                        compat.pinyinTextRect.bottom + mUnderlineVerticalSpacing,
                        compat.pinyinTextRect.left + compat.pinyinTextRect.width() + mHorizontalSpacing,
                        compat.pinyinTextRect.bottom + mUnderlineVerticalSpacing,
                        mUnderlinePaint);
            }

            if (debugDraw) {
                mDebugPaint.setColor(mTextColor);
                canvas.drawRect(compat.textRect, mDebugPaint);
            }

            if (debugDraw) {
                mDebugPaint.setColor(mTextColor);
                canvas.drawRect(compat.pinyinRect, mDebugPaint);
            }

            if (debugDraw) {
                mDebugPaint.setColor(mTextColor);
                canvas.drawRect(compat.pinyinTextRect, mDebugPaint);
            }
        }
    }

    private void drawPlainText(Canvas canvas) {
        drawText(canvas);
    }

    // If TYPE_PINYIN or only show Pinyin, we will set the pinyin text color and text size same as the
    // plain text (mTextColor,  mTextSize)
    private void drawPinyin(Canvas canvas) {
        drawText(canvas);
    }

    private void drawText(Canvas canvas) {
        if (mStaticLayout != null) {
            int paddingLeft = this.getPaddingLeft();
            int paddingTop = this.getPaddingTop();
            canvas.translate(paddingLeft, paddingTop);

            mStaticLayout.draw(canvas);

            if (mUnderline && !isPunctuation(mTextString)) {
                for (int i = 0; i < mStaticLayout.getLineCount(); i++) {
                    canvas.drawLine(
                            mStaticLayout.getLineLeft(i),
                            mStaticLayout.getLineBottom(i),
                            mStaticLayout.getLineRight(i),
                            mStaticLayout.getLineBottom(i),
                            mUnderlinePaint);
                }
            }
        }
    }

    private boolean isPunctuation(String text) {
        if (TextUtils.isEmpty(text)) {
            return false;
        }
        text = text.trim();
        if (text.length() != 1) {
            return false;
        }
        return StringUtils.isPunctuation(text.charAt(0));
    }

    private int getTextWidth(String text, int textSize) {
        mPaint.setTextSize(textSize);

        return (int) Math.ceil(Layout.getDesiredWidth(text, mPaint));
    }

    static class PinyinCompat {
        String text;
        @ColorInt
        int textColor;
        String pinyin;
        @ColorInt
        int pinyinColor;

        Rect pinyinTextRect;
        Rect textRect;
        Rect pinyinRect;
    }

    public static class Token {
        private String text;
        private @ColorInt
        int textColor = 0;
        private String pinyin;
        private @ColorInt
        int pinyinColor = 0;

        public Token() {

        }

        public Token(String text, @ColorInt int textColor, String pinyin, @ColorInt int pinyinColor) {
            this.text = text;
            this.textColor = textColor;
            this.pinyin = pinyin;
            this.pinyinColor = pinyinColor;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getPinyin() {
            return pinyin;
        }

        public void setPinyin(String pinyin) {
            this.pinyin = pinyin;
        }

        public int getTextColor() {
            return textColor;
        }

        public void setTextColor(int textColor) {
            this.textColor = textColor;
        }

        public int getPinyinColor() {
            return pinyinColor;
        }

        public void setPinyinColor(int pinyinColor) {
            this.pinyinColor = pinyinColor;
        }
    }
}
