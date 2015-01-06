package org.thesis.android.ui.card.tag;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.SystemClock;
import android.support.v7.widget.CardView;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.text.WordUtils;
import org.thesis.android.R;
import org.thesis.android.io.database.SQLiteDAO;

import java.util.Locale;

@SuppressLint("ViewConstructor") //They wouldn't be used anyway
public class AddedTagCardView extends CardView implements ITagCard, View.OnClickListener {

    private ITagChangedListener mCallback;
    private final Context mContext;
    private final EditText mTagNameField;
    private Boolean mBeingBuilt = Boolean.TRUE;
    private final static InputFilter FILTER_TAG_NAME = new InputFilter() {
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            for (int i = start; i < end; i++) {
                if (!Character.isLetterOrDigit(source.charAt(i)) || source.charAt(i) == '_') {
                    return "";
                }
            }
            return null;
        }
    };

    public AddedTagCardView(Context context, ITagChangedListener _callback) {
        super(context);

        mContext = context;

        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context
                .LAYOUT_INFLATER_SERVICE);

        View v = mInflater.inflate(R.layout.custom_view_added_tag_card, this);

        mCallback = _callback;

        super.setCardBackgroundColor(context.getResources().getColor(R.color
                .material_deep_purple_900));

        setOnClickListener(this);

        mTagNameField = ((EditText) v.findViewById(R.id.tag_name));
        mTagNameField.setFilters(new InputFilter[]{FILTER_TAG_NAME});
        mTagNameField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null || !isBeingBuilt()) {
                    if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event
                            .isShiftPressed() && (event
                            .getAction() == KeyEvent
                            .ACTION_DOWN &&
                            event.getKeyCode() == KeyEvent.KEYCODE_ENTER))) {
                        createTag();
                        return Boolean.TRUE;
                    }
                }
                return Boolean.FALSE;
            }
        });

        mTagNameField.setImeOptions(EditorInfo.IME_ACTION_DONE);
        mTagNameField.post(new Runnable() {

            @Override
            public void run() {
                mTagNameField.requestFocusFromTouch();
                mTagNameField.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(),
                        SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN,
                        mTagNameField.getWidth(),
                        mTagNameField.getHeight(), 0));
                mTagNameField.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(),
                        SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, mTagNameField.getWidth(),
                        mTagNameField.getHeight(), 0));
            }
        });

        if (mCallback != null)
            mCallback.onTagCreated(this);
    }

    private synchronized void createTag() {
        if (!isBeingBuilt()) return;
        final String lowerCaseFieldText = mTagNameField.getText().toString().toLowerCase(Locale
                .ENGLISH);
        if (!SQLiteDAO.getInstance().isTagOrGroupNameValid(lowerCaseFieldText)) {
            Toast.makeText(mContext, R.string.invalid_tag_name,
                    Toast.LENGTH_LONG).show();
            return;
        }
        setFocusable(Boolean.FALSE);
        setFocusableInTouchMode(Boolean.FALSE);
        setLongClickable(Boolean.FALSE);
        mTagNameField.setText(WordUtils.capitalizeFully(lowerCaseFieldText));
        mBeingBuilt = Boolean.FALSE;
        mTagNameField.clearFocus();
        mCallback.onTagAdded(this);
    }

    private void forceDelete() {
        final InputMethodManager imm = (InputMethodManager) mContext
                .getSystemService(
                        Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mTagNameField.getWindowToken(), 0);
        setVisibility(View.GONE);
        if (mCallback != null)
            mCallback.onTagRemoved(this);
    }

    @Override
    public void onClick(View v) {
        if (!mBeingBuilt) {
            forceDelete();
        }
    }

    @Override
    public String getName() {
        return mTagNameField.getText().toString();
    }

    public Boolean isBeingBuilt() {
        return mBeingBuilt;
    }
}
