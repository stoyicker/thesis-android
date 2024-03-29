package org.thesis.android.ui.component.tag;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.Toast;

import org.apache.commons.lang3.text.WordUtils;
import org.thesis.android.R;
import org.thesis.android.io.database.SQLiteDAO;
import org.thesis.android.io.net.SubscriptionManager;
import org.thesis.android.ui.activity.NavigationDrawerActivity;
import org.thesis.android.ui.component.FlowLayout;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import it.gmariotti.cardslib.library.internal.CardExpand;

public class TagCloudCardExpand extends CardExpand implements ITagCard.ITagChangedListener,
        NavigationDrawerActivity.IOnBackPressedListener,
        SubscriptionManager.ISubscriptionListener, SubscriptionManager.IUnsubscriptionListener {

    private final List<ITagCard> mTagCardViews = new LinkedList<>();
    private final ITagCard.ITagChangedListener mCallback;
    private FlowLayout mFlowLayout;
    private ScrollView mScrollView;
    private final View mDummy;
    private View mEmptyView;

    public TagCloudCardExpand(Context context, ITagCard.ITagChangedListener _callback,
                              View dummy) {
        super(context, R.layout.card_tag_group_flow);

        mCallback = _callback;

        mDummy = dummy;

        mDummy.setFocusable(Boolean.TRUE);
        mDummy.setFocusableInTouchMode(Boolean.TRUE);

        dummy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TagCloudCardExpand.this.processClick();
            }
        });
    }

    public void setTagGroupAndRefreshViews(String groupName) {
        final List<String> tags = SQLiteDAO.getInstance().getGroupTags(groupName);

        mTagCardViews.clear();

        for (String x : tags) {
            mTagCardViews.add(new TagCardView(mContext, x, this));
        }
    }

    private synchronized void processClick() {
        AddedTagCardView viewToRemove = null;
        for (ITagCard c : mTagCardViews) {
            if (!(c instanceof AddedTagCardView)) continue;
            final AddedTagCardView castedC = (AddedTagCardView) c;
            if (castedC.isBeingBuilt()) {
                viewToRemove = castedC;
                break;
            }
        }
        if (viewToRemove == null)
            mTagCardViews.add(new AddedTagCardView(mContext, TagCloudCardExpand.this, mDummy,
                    Boolean.TRUE));
        else
            viewToRemove.cancelTagCreation();
    }

    private void updateEmptyViewVisibility() {
        if (mFlowLayout.getChildCount() == 1 && !(mFlowLayout.getChildAt(0) instanceof ITagCard))
            mEmptyView.setVisibility(View.VISIBLE);
        else
            mEmptyView.setVisibility(View.GONE);
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        if (view == null) return;

        mScrollView = (ScrollView) parent;

        mFlowLayout = (FlowLayout) view.findViewById(R.id.flow_layout);

        mEmptyView = view.findViewById(android.R.id.empty);

        mScrollView.setOnTouchListener(new View.OnTouchListener() {

            public static final float CLICK_ACTION_THRESHOLD = 5;
            private float startX;
            private float startY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        startY = event.getY();
                        break;
                    case MotionEvent.ACTION_UP: {
                        float endX = event.getX();
                        float endY = event.getY();
                        if (isClick(startX, endX, startY, endY)) {
                            TagCloudCardExpand.this.processClick();
                            return Boolean.TRUE;
                        }
                        break;
                    }
                }
                return Boolean.FALSE;
            }

            private boolean isClick(float startX, float endX, float startY, float endY) {
                float differenceX = Math.abs(startX - endX);
                float differenceY = Math.abs(startY - endY);
                return !(differenceX > CLICK_ACTION_THRESHOLD || differenceY >
                        CLICK_ACTION_THRESHOLD);
            }
        });

        for (ITagCard v : mTagCardViews) {
            mFlowLayout.addView((View) v);
        }

        updateEmptyViewVisibility();
    }

    private void recalculateFlowLayoutHeight() {
        if (mFlowLayout != null)
            mFlowLayout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
    }

    @Override
    public void onTagCreated(ITagCard tag) {
        mFlowLayout.addView((View) tag);
        mScrollView.smoothScrollTo(0, mScrollView.getHeight());
        recalculateFlowLayoutHeight();
        updateEmptyViewVisibility();
    }

    @Override
    public void onTagAdded(ITagCard tag) {
        mTagCardViews.remove(tag);
        mFlowLayout.removeView((View) tag);
        final TagCardView v = new TagCardView(mContext, WordUtils.capitalizeFully(tag.getName()),
                this);
        SubscriptionManager.subscribeToTagInBackground(mContext, v, this);
    }

    @Override
    public void onSubscriptionAttemptFinished(TagCardView tag, Boolean success) {
        if (success) {
            mTagCardViews.add(tag);
            if (mCallback != null)
                mCallback.onTagAdded(tag);
            mFlowLayout.addView(tag);
        } else {
            Toast.makeText(mContext, String.format(Locale.ENGLISH, mContext.getString(R.string
                            .subscription_error_pattern),
                    tag.getName()), Toast.LENGTH_SHORT).show();
        }
        recalculateFlowLayoutHeight();
        updateEmptyViewVisibility();
    }

    @Override
    public void onTagRemoved(ITagCard tag) {
        SubscriptionManager.unsubscribeFromTagInBackground(mContext, tag, this);
    }

    @Override
    public void onUnsubscriptionAttemptFinished(ITagCard tag, Boolean success) {
        final View tagAsView = (View) tag;
        if (success) {
            mTagCardViews.remove(tag);
            if (mCallback != null)
                mCallback.onTagRemoved(tag);
            mFlowLayout.removeView(tagAsView);
        } else {
            tagAsView.setVisibility(View.VISIBLE);
            Toast.makeText(mContext, String.format(Locale.ENGLISH, mContext.getString(R.string
                            .unsubscription_error_pattern),
                    tag.getName()), Toast.LENGTH_SHORT).show();
        }
        recalculateFlowLayoutHeight();
        updateEmptyViewVisibility();
    }

    @Override
    public void onTagCreationCancelled(ITagCard tag) {
        mTagCardViews.remove(tag);
        mFlowLayout.removeView((View) tag);
        recalculateFlowLayoutHeight();
        updateEmptyViewVisibility();
    }

    @Override
    public Boolean onBackPressed() {
        if (!getParentCard().isExpanded()) return Boolean.FALSE;
        AddedTagCardView viewToRemove = null;
        for (ITagCard c : mTagCardViews) {
            if (!(c instanceof AddedTagCardView)) continue;
            final AddedTagCardView castedC = (AddedTagCardView) c;
            if (castedC.isBeingBuilt()) {
                viewToRemove = castedC;
                break;
            }
        }
        if (viewToRemove == null)
            getParentCard().doToogleExpand(); //Close Expand
        else
            viewToRemove.cancelTagCreation(); //Discard the added tag

        return Boolean.TRUE;
    }

    public void cancelEdits() {
        if (!getParentCard().isExpanded()) return;
        AddedTagCardView viewToRemove = null;
        for (ITagCard c : mTagCardViews) {
            if (!(c instanceof AddedTagCardView)) continue;
            final AddedTagCardView castedC = (AddedTagCardView) c;
            if (castedC.isBeingBuilt()) {
                viewToRemove = castedC;
                break;
            }
        }
        if (viewToRemove != null)
            viewToRemove.cancelTagCreation(); //Discard the added tag
    }
}
