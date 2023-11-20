package com.lyk.router.demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.lyk.gradle.router.runtime.Router;
import com.lyk.router.annotations.Destination;

@Destination(
        url = "router://page-home",
        description = "应用主页"
)
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Router.INSTANCE.go(v.getContext(),
                        "router://lyk/profile?name=lyk&message=hello");
            }
        });
    }

}





