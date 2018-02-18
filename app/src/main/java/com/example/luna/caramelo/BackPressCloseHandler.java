package com.example.luna.caramelo;

import android.app.Activity;
import android.widget.Toast;

/**
 * Created by LUNA on 2017-12-09.
 */

//'뒤로' 버튼을 두번 이상 누르면 종료되도록 한다
//종료 되기 전 사용자에게 토스트 메시지를 날림
public class BackPressCloseHandler {

    private long backKeyPressedTime = 0;
    private Toast toast;
    private Activity activity;

    public BackPressCloseHandler(Activity context) {
        this.activity = context; }


    public void onBackPressed() {
        if (System.currentTimeMillis() > backKeyPressedTime + 3000) {
            backKeyPressedTime = System.currentTimeMillis();
            showGuide();
            return;
        }

        if (System.currentTimeMillis() <= backKeyPressedTime + 3000) {

            activity.finish();
            toast.cancel(); }
    }

    public void showGuide() {
        toast = Toast.makeText(activity, "'뒤로' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
        toast.show();
    }

}
