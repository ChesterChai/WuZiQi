package com.example.wuziqi.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

import butterknife.ButterKnife;

public abstract class BaseDialog extends Dialog {

    private OnBackPressedListener onBackPressedListener;

    public BaseDialog(@NonNull Context context) {
        super(context);
        init();
    }

    public BaseDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        init();
    }

    protected BaseDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        init();
    }

    private void init() {
        setCanceledOnTouchOutside(false);
        setCancelable(false);

        setContentView(provideLayoutId());
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public final void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
    }

    protected abstract int provideLayoutId();

    protected abstract void initView();

    public void setOnBackPressedListener(OnBackPressedListener onBackPressedListener) {
        this.onBackPressedListener = onBackPressedListener;
    }

    @Override
    public final void onBackPressed() {
        if (onBackPressedListener != null) {
            cancel();
            onBackPressedListener.onBackPressed();
        } else {
            super.onBackPressed();
        }
    }

    // 设置全屏
    protected void setFullScreen() {
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }
    }

    //显示在屏幕底部，横向充满屏幕，纵向自适应
    protected void setShowAtBottom() {
        Window window = getWindow();
        Objects.requireNonNull(window).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.START | Gravity.BOTTOM);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.x = 0;
        lp.y = 0;
        window.setAttributes(lp);
    }

    public interface OnBackPressedListener {
        void onBackPressed();
    }
}

