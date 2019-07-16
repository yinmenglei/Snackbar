package com.test.snackbar;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.test.snackbar.util.TSnackbarUtils;

/**
 * Function:
 * Author Name: yinmenglei
 * Date: 2019/7/16 12:20
 * Copyright © 2006-2018 高顿网校, All Rights Reserved.
 */
public class MainActivity extends AppCompatActivity {

    private RelativeLayout main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        main = findViewById(R.id.main);

        // 弹顶部提示
        findViewById(R.id.btn1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TSnackbarUtils.getInstance().show(main, "弹顶部提示", TSnackbarUtils.hint_green);
            }
        });
    }
}
