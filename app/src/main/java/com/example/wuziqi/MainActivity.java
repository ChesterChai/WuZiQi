package com.example.wuziqi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;

import com.example.wuziqi.core.GameActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final String[] PLAYER_COLOR_SELECTER = {"执黑棋","执白棋"};
    private static final String[] AI_LEVEL_SELECTER = {"简单","一般","困难","很困难"};

    private int boardSize = 2;
    private boolean playerIsBlack = true;
    private int aiLevel = 1;

    @BindView(R.id.player_color_sp)
    Spinner player_color_sp;

    @BindView(R.id.ai_level_sp)
    Spinner ai_level_sp;

    @BindView(R.id.min_checkbox)
    CheckBox min_cx;

    @BindView(R.id.mid_checkbox)
    CheckBox mid_cx;

    @BindView(R.id.max_checkbox)
    CheckBox max_cx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        //设置执棋颜色Spinner选项
        ArrayAdapter<String> playerColorAdapter = new ArrayAdapter<>(this , R.layout.spinner_item_selete, PLAYER_COLOR_SELECTER);
        playerColorAdapter.setDropDownViewResource(R.layout.spinner_item_dropdown);
        player_color_sp.setAdapter(playerColorAdapter);
        player_color_sp.setSelection(0);
        player_color_sp.setOnItemSelectedListener(new PlayerColorItemSelectedListener());

        //设置AI难度Spinner选项
        ArrayAdapter<String> aiLevelAdapter = new ArrayAdapter<>(this , R.layout.spinner_item_selete, AI_LEVEL_SELECTER);
        aiLevelAdapter.setDropDownViewResource(R.layout.spinner_item_dropdown);
        ai_level_sp.setAdapter(aiLevelAdapter);
        ai_level_sp.setSelection(0);
        ai_level_sp.setOnItemSelectedListener(new AiLevelItemSelectedListener());
    }


    @OnClick({R.id.min_checkbox, R.id.mid_checkbox, R.id.max_checkbox})
    public void onClickCheckbox(View v){
        switch (v.getId()){
            case R.id.min_checkbox:
                min_cx.setChecked(true);
                mid_cx.setChecked(false);
                max_cx.setChecked(false);
                boardSize = 1;
                break;
            case R.id.mid_checkbox:
                min_cx.setChecked(false);
                mid_cx.setChecked(true);
                max_cx.setChecked(false);
                boardSize = 2;
                break;
            case R.id.max_checkbox:
                min_cx.setChecked(false);
                mid_cx.setChecked(false);
                max_cx.setChecked(true);
                boardSize = 3;
                break;
        }
    }

    @OnClick(R.id.start)
    public void onClick(View v){
        GameActivity.GameStartParam param = new GameActivity.GameStartParam(boardSize, playerIsBlack, aiLevel);
        Intent intent = new Intent(MainActivity.this, GameActivity.class);
        intent.putExtra(GameActivity.GAME_START_PARAM, param);
        startActivity(intent);
    }

    private class PlayerColorItemSelectedListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            playerIsBlack = position==0?true:false;
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    private class AiLevelItemSelectedListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            aiLevel = position + 1;
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }
}