package cn.pivotstudio.modulec.homescreen.oldversion.model;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import cn.pivotstudio.modulec.homescreen.R;
import com.scwang.smart.refresh.layout.api.RefreshFooter;
import com.scwang.smart.refresh.layout.api.RefreshKernel;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.constant.RefreshState;
import com.scwang.smart.refresh.layout.constant.SpinnerStyle;
import com.wang.avi.AVLoadingIndicatorView;

public class MessageStandardRefreshFooter extends LinearLayout implements RefreshFooter {
    private final AVLoadingIndicatorView mLoadmore;
    //private ImageView mImage,mImage2,mImage3;
    private final TextView text;
    private AnimationDrawable pullDownAnim;
    private AnimationDrawable refreshingAnim, refreshingAnim2, refreshingAnim3;
    private Boolean refreshCondition = false;
    private boolean hasSetPullDownAnim = false;

    public MessageStandardRefreshFooter(Context context) {
        this(context, null, 0);
    }

    public MessageStandardRefreshFooter(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MessageStandardRefreshFooter(Context context,
                                        @Nullable AttributeSet attrs,
                                        int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View view = View.inflate(context, R.layout.message_refresh_footer, this);
        mLoadmore = (AVLoadingIndicatorView) view.findViewById(R.id.AVLoadingIndicatorView);
        text = (TextView) view.findViewById(R.id.refreshfooter_text);
            /*
            mImage = (ImageView) view.findViewById(R.id.refresh_header);
            mImage2 = (ImageView) view.findViewById(R.id.refresh_header_2);
            mImage3 = (ImageView) view.findViewById(R.id.refresh_header_3);

            Log.e("?????????","?????????");
             */
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
    public boolean setNoMoreData(boolean noMoreData) {
        return false;
    }

    @Override
    public void onStateChanged(RefreshLayout refreshLayout,
                               RefreshState oldState,
                               RefreshState newState) {
        switch (newState) {
            case PullDownToRefresh:
                //??????????????????????????????????????????????????????
                //??????????????????????????????????????????????????????????????????
                // mImage.setImageResource(R.drawable.refresh_point);
                //mImage2.setImageResource(R.drawable.refresh_point);
                // mImage3.setImageResource(R.drawable.refresh_point);
                //Log.e("?????????","PullDownToRefresh");
                break;
            case Refreshing: //??????????????????????????????
                //????????????????????????????????????????????????????????????????????????????????????????????????
                mLoadmore.show();
                //text.setText("jiazaizhong");
                refreshCondition = true;
                    /*
                    mImage.setImageResource(R.drawable.refresh_2);
                    mImage2.setImageResource(R.drawable.refresh_3);
                    mImage3.setImageResource(R.drawable.refresh_1);
                    refreshingAnim = (AnimationDrawable) mImage.getDrawable();
                    refreshingAnim.start();
                    refreshingAnim2 = (AnimationDrawable) mImage2.getDrawable();
                    refreshingAnim2.start();
                    refreshingAnim3 = (AnimationDrawable) mImage3.getDrawable();
                    refreshingAnim3.start();
                    Log.e("?????????","Refreshing");

                     */
                break;
            case ReleaseToRefresh:

                Log.e("?????????", "ReleaseToRefresh");
                break;
        }
    }

    @Override
    public int onFinish(RefreshLayout layout, boolean success) {
        // ????????????
            /*if (pullDownAnim != null && pullDownAnim.isRunning()) {
                pullDownAnim.stop();
                Log.e("?????????"," pullDownAnim.stop();");

            }
            if (refreshingAnim != null && refreshingAnim.isRunning()) {
                refreshingAnim.stop();
                refreshingAnim2.stop();
                refreshingAnim3.stop();
                Log.e("?????????"," refreshingAnim n.stop();");
            }
            //????????????
            hasSetPullDownAnim = false;

             */
        if (refreshCondition == true) {
            mLoadmore.hide();
            refreshCondition = false;
        }
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
    public void onMoving(boolean isDragging,
                         float percent,
                         int offset,
                         int height,
                         int maxDragHeight) {

        // ????????????????????????100%?????????????????? setScale ????????????????????????
        // mImage.setScaleX(1);
        // mImage.setScaleY(1);

        if (percent < 1) {

            if (hasSetPullDownAnim) {
                hasSetPullDownAnim = false;
            }
        }

        if (percent >= 1.0) {
            //???????????????????????????????????????????????????
            if (!hasSetPullDownAnim) {
                mLoadmore.show();
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

