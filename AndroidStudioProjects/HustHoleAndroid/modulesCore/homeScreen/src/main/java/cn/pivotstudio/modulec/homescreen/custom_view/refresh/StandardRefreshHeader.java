package cn.pivotstudio.modulec.homescreen.custom_view.refresh;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.scwang.smart.refresh.layout.api.RefreshHeader;
import com.scwang.smart.refresh.layout.api.RefreshKernel;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.constant.RefreshState;
import com.scwang.smart.refresh.layout.constant.SpinnerStyle;

import cn.pivotstudio.modulec.homescreen.R;


@SuppressLint("RestrictedApi")
public class StandardRefreshHeader extends LinearLayout implements RefreshHeader {

    private ImageView mImage,mImage2,mImage3;
    private AnimationDrawable pullDownAnim;
    private AnimationDrawable refreshingAnim,refreshingAnim2,refreshingAnim3;

    private boolean hasSetPullDownAnim = false;

    public StandardRefreshHeader(Context context) {
        this(context, null, 0);
    }

    public StandardRefreshHeader(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StandardRefreshHeader(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View view = View.inflate(context, R.layout.refresh_header, this);
        mImage = (ImageView) view.findViewById(R.id.refresh_header);
        mImage2 = (ImageView) view.findViewById(R.id.refresh_header_2);
        mImage3 = (ImageView) view.findViewById(R.id.refresh_header_3);

    }

    @NonNull
    @Override
    public View getView() {
        return this;
    }

    @Override
    public SpinnerStyle getSpinnerStyle() {
        return SpinnerStyle.Translate;
    }

    @Override
    public void onStartAnimator(RefreshLayout layout, int height, int extendHeight) {

    }


    @Override
    public void onStateChanged(RefreshLayout refreshLayout, RefreshState oldState, RefreshState newState) {
        switch (newState) {
            case PullDownToRefresh: //??????????????????????????????????????????????????????
                mImage.setImageResource(R.drawable.refresh_point);
                mImage2.setImageResource(R.drawable.refresh_point);
                mImage3.setImageResource(R.drawable.refresh_point);

                break;
            case Refreshing: //??????????????????????????????
                mImage.setImageResource(R.drawable.refresh_2);
                mImage2.setImageResource(R.drawable.refresh_3);
                mImage3.setImageResource(R.drawable.refresh_1);
                refreshingAnim = (AnimationDrawable) mImage.getDrawable();
                refreshingAnim.start();
                refreshingAnim2 = (AnimationDrawable) mImage2.getDrawable();
                refreshingAnim2.start();
                refreshingAnim3 = (AnimationDrawable) mImage3.getDrawable();
                refreshingAnim3.start();

                break;
            case ReleaseToRefresh:

                break;
        }
    }


    @Override
    public int onFinish(RefreshLayout layout, boolean success) {
        // ????????????

        if (pullDownAnim != null && pullDownAnim.isRunning()) {
            pullDownAnim.stop();


        }

        if (refreshingAnim != null && refreshingAnim.isRunning()) {
            refreshingAnim.stop();
            refreshingAnim2.stop();
            refreshingAnim3.stop();

        }
        //????????????
        hasSetPullDownAnim = false;
        return 0;
    }

    /*@Override
    public void onReleasing(float percent, int offset, int headerHeight, int extendHeight) {

    }

    @Override
    public void onRefreshReleased(RefreshLayout layout, int headerHeight, int extendHeight) {

    }*/
    @Override
    public void setPrimaryColors(int... colors) {

    }

    @Override
    public void onInitialized(RefreshKernel kernel, int height, int extendHeight) {

    }

    @Override
    public void onMoving(boolean isDragging, float percent, int offset, int height, int maxDragHeight) {

// ????????????????????????100%?????????????????? setScale ????????????????????????
        mImage.setScaleX(1);
        mImage.setScaleY(1);

        if (percent < 1) {
            mImage2.setScaleX(3*percent/2);
            mImage2.setScaleY(3*percent/2);
            mImage3.setScaleX(3*percent/2);
            mImage3.setScaleY(3*percent/2);
            mImage2.setTranslationX(percent*45);
            mImage3.setTranslationX(-(percent*45));


            if (hasSetPullDownAnim) {
                hasSetPullDownAnim = false;
            }
        }


        if (percent >= 1.0) {
            //???????????????????????????????????????????????????
            if (!hasSetPullDownAnim) {

                mImage2.setScaleX(1.5f);
                mImage2.setScaleY(1.5f);
                mImage3.setScaleX(1.5f);
                mImage3.setScaleY(1.5f);
                mImage2.setTranslationX(45);
                mImage3.setTranslationX(-45);
                //mImage.setImageResource(R.drawable.anim_pull_end);
                //pullDownAnim = (AnimationDrawable) mImage.getDrawable();
               // pullDownAnim.start();

                hasSetPullDownAnim = true;
            }
        }


    }

    @Override
    public void onReleased(@NonNull RefreshLayout refreshLayout, int height, int maxDragHeight) {




    }

    @Override
    public void onHorizontalDrag(float percentX, int offsetX, int offsetMax) {

    }

    @Override
    public boolean isSupportHorizontalDrag() {
        return false;
    }
}
