package com.github.devjn.filemanager.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.devjn.filemanager.R;
import com.github.devjn.filemanager.utils.ViewUtils;

/**
 * Created by Cisco on 30.1.2018.
 */

public class FileViewListItem extends FrameLayout {

    private ImageView imageCover;
    private TextView textCount;
    private TextView textName;


    public FileViewListItem(@NonNull Context context) {
        super(context);
        init(context);
    }

    public FileViewListItem(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public FileViewListItem(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    void init(Context context) {
        final int imgSize = 56;

        imageCover = new ImageView(context);
        imageCover.setId(R.id.imageView);
        imageCover.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LayoutParams params = new LayoutParams(ViewUtils.dp(imgSize), ViewUtils.dp(imgSize), Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        imageCover.setLayoutParams(params);
        addView(imageCover);

        textCount = new TextView(context);
        textCount.setId(R.id.size);
        params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        params.topMargin = ViewUtils.dp(imgSize - 28);
        params.leftMargin = ViewUtils.dp(imgSize / 4);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            textCount.setTextAppearance(R.style.TextAppearance_AppTheme_Caption_Inverse);
        } else
            textCount.setTextAppearance(context, (R.style.TextAppearance_AppTheme_Caption_Inverse));
        textCount.setTextSize(12f);
        textCount.setLayoutParams(params);
        addView(textCount);

        textName = new AppCompatTextView(context);
        textName.setId(R.id.name);
        params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        textName.setLayoutParams(params);
        addView(textName);


        int attr = R.attr.selectableItemBackground;
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(new int[]{attr});
        Drawable foreground = typedArray.getDrawable(0);
        typedArray.recycle();

        setPadding(ViewUtils.dp(4), ViewUtils.dp(4), ViewUtils.dp(4), ViewUtils.dp(4));
        setBackgroundResource(R.drawable.statelist_focusable_item_background);
        setForeground(foreground);
        setLayoutParams(new ViewGroup.LayoutParams(ViewUtils.dp(88), ViewUtils.dp(88)));
    }

    public void update() {

    }


    public ImageView getImageCover() {
        return imageCover;
    }

    public TextView getTextCount() {
        return textCount;
    }

    public TextView getTextName() {
        return textName;
    }
}
