package com.liwenwei.pinyintextview.demo;

import android.content.Context;
import android.os.Bundle;
import android.util.Pair;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;

import com.liwenwei.pinyintextview.MultiScreenSupportUtils;
import com.liwenwei.pinyintextview.PinyinTextView;

import org.honorato.multistatetogglebutton.MultiStateToggleButton;
import org.honorato.multistatetogglebutton.ToggleButton;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private int[] mLineSpaces = {6, 10, 14};
    private int[] mHorizontalSpaces = {2, 6, 10};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        final Context context = this;
        final PinyinTextView pTVContent = findViewById(R.id.ptv_content);
        pTVContent.setPinyinText(initPinyinData(), PinyinTextView.TYPE_PINYIN_AND_TEXT);

        // Set Text Size
        ((SeekBar) findViewById(R.id.seek_bar_text_size)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                pTVContent.setTextSize(MultiScreenSupportUtils.sp2Px(progress, context));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // Set Underline
        ((Switch)findViewById(R.id.switch_underline)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                pTVContent.setUnderline(isChecked);
            }
        });

        // Set line space
        MultiStateToggleButton mstbLineSpace = findViewById(R.id.mstb_line_space);
        mstbLineSpace.setValue(1);
        mstbLineSpace.setOnValueChangedListener(new ToggleButton.OnValueChangedListener() {
            @Override
            public void onValueChanged(int value) {
                pTVContent.setLineSpacing(MultiScreenSupportUtils.dp2Px(mLineSpaces[value], context));
            }
        });

        // Set horizontal space
        MultiStateToggleButton mstbHorizontalSpace = findViewById(R.id.mstb_horizontal_space);
        mstbHorizontalSpace.setValue(1);
        mstbHorizontalSpace.setOnValueChangedListener(new ToggleButton.OnValueChangedListener() {
            @Override
            public void onValueChanged(int value) {
                pTVContent.setHorizontalSpacing(MultiScreenSupportUtils.dp2Px(mLineSpaces[value], context));
            }
        });
    }

    private List<Pair<String, String>> initPinyinData() {
        List<Pair<String, String>> pinyinList = new ArrayList<>();
        pinyinList.add(Pair.create("这", "zhè"));
        pinyinList.add(Pair.create("是", "shì"));
        pinyinList.add(Pair.create("一个", "yī gè"));
        pinyinList.add(Pair.create("拼音", "pīn yīn"));
        pinyinList.add(Pair.create("组件", "zǔ jiàn"));
        pinyinList.add(Pair.create("，", ""));
        pinyinList.add(Pair.create("它", "tā"));
        pinyinList.add(Pair.create("简单", "jiǎn dān"));
        pinyinList.add(Pair.create("轻便", "qīng biàn"));
        pinyinList.add(Pair.create("，", ""));
        pinyinList.add(Pair.create("可以", "kě yǐ"));
        pinyinList.add(Pair.create("很", "hěn"));
        pinyinList.add(Pair.create("灵活", "líng huó"));
        pinyinList.add(Pair.create("的", "de"));
        pinyinList.add(Pair.create("配置", "pèi zhì"));
        pinyinList.add(Pair.create("很多", "hěn duō"));
        pinyinList.add(Pair.create("设置", "shè zhì"));
        pinyinList.add(Pair.create("，", ""));
        pinyinList.add(Pair.create("同时", "tóng shí"));
        pinyinList.add(Pair.create("可以", "kě yǐ"));
        pinyinList.add(Pair.create("切换", "qiē huàn"));
        pinyinList.add(Pair.create("多种", "duō zhǒng"));
        pinyinList.add(Pair.create("模式", "mó shì"));
        pinyinList.add(Pair.create("。", ""));
        return pinyinList;
    }
}
