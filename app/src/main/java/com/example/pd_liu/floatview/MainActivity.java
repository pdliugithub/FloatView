package com.example.pd_liu.floatview;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.pd_liu.floatview.view.DragTextView;

public class MainActivity extends AppCompatActivity {

    private DragTextView mDragTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public void openFloat(View view){
        if (mDragTextView != null) {
            mDragTextView.removeThisView();
        }
        mDragTextView = new DragTextView(getApplicationContext());
        mDragTextView.setText("悬浮窗");
        mDragTextView.setPadding(20, 20, 20, 20);
        mDragTextView.setTextSize(25f);
        mDragTextView.setBackgroundResource(R.color.colorPrimary);
        mDragTextView.addToApplication(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mDragTextView != null) {
            if (mDragTextView.isOkPermission(requestCode)) {
                Toast.makeText(this, "权限申请成功", Toast.LENGTH_SHORT).show();
                mDragTextView.addToApplication(this);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mDragTextView != null){
            mDragTextView.removeThisView();
        }
    }
}
