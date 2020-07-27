package com.example.mingo;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity implements WzqView.OnPlayChessListeners, WzqView.OnGameOverListeners {

    private WzqView mWzqVIew;
    private TextView mWhoSetText;
    private MyDialog myDialog;
    private TextView mRestart;
    private TextView viewById;
    private TextView mWho_over;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewById = findViewById(R.id.fish);
        viewById.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.finish();
            }
        });
        mRestart = findViewById(R.id.restart);
        mWzqVIew = findViewById(R.id.wzqzz);
        mWho_over = findViewById(R.id.who_over);
        mWho_over.setText("等待游戏结束揭晓...");
        mWzqVIew.setOnGameOverListeners(this);
        mWzqVIew.setOnPlayChessListeners(this);
        mWhoSetText = findViewById(R.id.who_set_text);
        mRestart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWzqVIew.restart();
                mWhoSetText.setText("白子");
                mWho_over.setText("等待游戏结束揭晓...");
            }
        });
    }

    /**
     * 利用反射获取状态栏高度
     *
     * @return
     */
    public int getStatusBarHeight() {
        int result = 0;
        //获取状态栏高度的资源id
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @Override
    public void onWhite() {
        mWhoSetText.setText("白子");
    }

    @Override
    public void onBlack() {
        mWhoSetText.setText("黑子");
//        myDialog = new MyDialog(MainActivity.this, R.style.MyDialog);
//        myDialog.setMessage("游戏结束\n");
//        myDialog.show();
    }

    @Override
    public void onOver(boolean isWhiteVictory) {
        String text = isWhiteVictory ? "白子" : "黑子";
        mWho_over.setText(text);
//        myDialog = new MyDialog(MainActivity.this, R.style.MyDialog);
//
//        myDialog.setMessage("游戏结束\n" + text);
//        myDialog.show();
    }
}