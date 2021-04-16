package com.example.wuziqi.view.dialog;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.wuziqi.R;

public class WarningDialog extends BaseDialog {

    private TextView title;
    private TextView content;
    private Button leftBtn;
    private Button rightBtn;
    private Button singleBtn;

    private WarningDialog.OnButtonClickListener listener;

    public WarningDialog(@NonNull Context context) {
        super(context, R.style.DialogAlertStyle);
    }

    @Override
    protected int provideLayoutId() {
        return R.layout.dialog_warning_view;
    }

    @Override
    protected void initView() {
        title = findViewById(R.id.alertDialogTitle);
        content = findViewById(R.id.alertDialogContent);
        leftBtn = findViewById(R.id.alertDialogLeftBtn);
        rightBtn = findViewById(R.id.alertDialogRightBtn);
        singleBtn = findViewById(R.id.alertDialogSingleBtn);

        View.OnClickListener onClickListener = view -> {
            if (listener != null) {
                if (view.getId() == R.id.alertDialogLeftBtn) {
                    listener.onLeftButtonClick();
                } else {
                    listener.onRightButtonClick();
                }
            }
            cancel();
        };
        leftBtn.setOnClickListener(onClickListener);
        rightBtn.setOnClickListener(onClickListener);
    }

    public WarningDialog build(int titleId, int contentId, int leftBtnId, int rightBtnId) {
        return build(getContext().getString(titleId), getContext().getString(contentId),
                getContext().getString(leftBtnId), getContext().getString(rightBtnId));
    }

    public WarningDialog build(@NonNull String title, @NonNull String content, @NonNull String leftBtn, @NonNull String rightBtn) {
        this.title.setText(title);
        this.content.setText(content);
        this.leftBtn.setText(leftBtn);
        this.rightBtn.setText(rightBtn);
        return this;
    }

    public WarningDialog build(int titleId, int contentId, int singleBtnId, @Nullable View.OnClickListener onClickListener) {
        return build(getContext().getString(titleId), getContext().getString(contentId), getContext().getString(singleBtnId),
                onClickListener);
    }

    //TODO 可添加多个按钮
    public WarningDialog build(@NonNull String title, @NonNull String content, @NonNull String singleBtn,
                               @Nullable View.OnClickListener onClickListener) {
        this.title.setText(title);
        this.content.setText(content);
        this.leftBtn.setVisibility(View.GONE);
        this.rightBtn.setVisibility(View.GONE);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) this.singleBtn.getLayoutParams();
        params.width = LinearLayout.LayoutParams.MATCH_PARENT;
        this.singleBtn.setLayoutParams(params);
        this.singleBtn.setText(singleBtn);
        this.singleBtn.setBackgroundResource(R.drawable.selector_dialog_radius_bottom_bg);
        this.singleBtn.setOnClickListener(v -> {
            if (onClickListener != null) {
                onClickListener.onClick(v);
            }
            cancel();
        });
        return this;
    }

    public WarningDialog setOnButtonClickListener(@NonNull WarningDialog.OnButtonClickListener listener) {
        this.listener = listener;
        return this;
    }

    public interface OnButtonClickListener {
        default void onLeftButtonClick() {
        }

        default void onRightButtonClick() {
        }
    }
}
