package com.example.luna.caramelo.Settings.Account;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.luna.caramelo.Tools.BackPressCloseHandler;
import com.example.luna.caramelo.R;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class JoinKakaoActivity extends AppCompatActivity {

    /*
    * 카카오 로그인 성공 시 이메일을 받아와서 수정 못하게, setText해주고
    * 유저네임은 직접 정할 수 있음, 즉 joinGeneral 과 같음
    * */

    TextView tv_kakaoEmail, tv_joinEmailChk,tv_joinUsernameChk,tv_result;
    EditText et_joinUsername, et_joinPassword, et_joinConfirmPassword;
    Button btn_submitGeneral;

    //서버에 보내기 위해 일반명사화
    String email, username, type;
    //카카오 가입인지 일반 가입인지 구별을 위한 것
    //**비번은 받지 않는다


    //뒤로 가기 두번-> 종료
    private BackPressCloseHandler backPressCloseHandler;

    private static String TAG = "가입_JoinKakaoActivity";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_kakao);


        //세팅 액티비티에서 보낸 값 받기
        Intent intent = getIntent();
        final String email = intent.getStringExtra("email");
        final String type = intent.getStringExtra("type");

        //뒤로 가기 두번 -> 종료
        backPressCloseHandler = new BackPressCloseHandler(this);

        tv_kakaoEmail = (TextView) findViewById(R.id.tv_kakaoEmail);
        tv_kakaoEmail.setText(email);

        et_joinUsername = (EditText) findViewById(R.id.et_joinUsername);
       // et_joinPassword = (EditText) findViewById(R.id.et_joinPassword);
       // et_joinConfirmPassword = (EditText) findViewById(R.id.et_joinConfirmPassword);
        //이메일, 유저네임 중복체크 결과
        tv_joinEmailChk = (TextView) findViewById(R.id.tv_joinEmailChk);
        tv_joinUsernameChk = (TextView) findViewById(R.id.tv_joinUsernameChk);
        //제출 결과
        tv_result = (TextView) findViewById(R.id.tv_result);
        //버튼
        btn_submitGeneral = (Button) findViewById(R.id.btn_submitGeneral);

        //이메일, 유저네임, 패스워드 등 입력 시 주의 메시지 뜨도록
        Boolean hasFocus;
        hasFocus = false;

        //가입 버튼 클릭 리스너
        btn_submitGeneral.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //받은 값을 string으로 만들어준다
                //String 변수 설정 제일 위에서 했음
                //이메일과 타입은 받아왔으므로 유저네임만..
                username = et_joinUsername.getText().toString();
                Log.d(TAG+"체크", email+username+type);


                try {
                    username = URLEncoder.encode(username, "UTF-8");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //Toast.makeText(JoinActivity.this, "버튼 눌러서 값을 toString", Toast.LENGTH_SHORT).show();


                View focusView = null;
                boolean cancel = false;

                //TODO 이름 중복체크/ 이메일 중복체크


                //유저네임이 입력 안 됐을 때
                if(TextUtils.isEmpty(username)) {
                    et_joinUsername.setError("사용하실 이름(닉네임 등)을 2자 이상 30자 미만으로 입력해주세요.");
                    focusView = et_joinUsername;
                    cancel = true;
                }

                //위에 것만 하면 공백은 입력되네..-> 이것도 안 됨
//                if(username.length()==0 || username.length() <2){
//                    et_joinUsername.setError("사용하실 이름(닉네임 등)을 2자 이상 30자 미만으로 입력해주세요.");
//                    focusView = et_joinUsername;
//                    cancel = true;
//                }

                String usernameSpaceChk = et_joinUsername.getText().toString();
                if(usernameSpaceChk.trim().length()==0){

                    et_joinUsername.setError("공백은 입력될 수 없습니다.");
                    focusView = et_joinUsername;
                    //ㄴ이거 없으니까 자꾸 에러뜨던데...아, cancel되면 focusView로 돌아가야 되는데 그게 없으니;;
                    cancel = true;

                }
                if(usernameSpaceChk.getBytes().length<=0 || usernameSpaceChk.length() <2) {
                    et_joinUsername.setError("사용하실 이름(닉네임 등)을 2자 이상 30자 미만으로 입력해주세요.");
                    focusView = et_joinUsername;
                    cancel = true;
                }


                //유저네임이 30자 이상이면 안됨
                if(!isUsernameValid(username)) {

                    et_joinUsername.setError("이름은 30자 미만이어야 합니다.");
                    focusView = et_joinUsername;
                    cancel = true;
                }

                if(cancel){
                    focusView.requestFocus();
                    return;

                } else {

                    //DB에 집어 넣어랏

                    JoinKakaoActivity.InsertData task = new InsertData();
                    Log.d(TAG+"체크2", email+username+type);
                    task.execute(email, username, type);
                    //Toast.makeText(JoinActivity.this, "task", Toast.LENGTH_SHORT).show();
                }


            }//OnClick
        });//SetOnClick




    }//onCreate


    //유저네임 30자 미만
    private  boolean isUsernameValid(String username) {
        return username.length() < 30;
    }



    //AsyncTask<doInBackground()의 변수 종류, onProgressUpdate()에서 사용할 변수 종류, onPostExecute()에서 사용할 변수종류>
    //출처: http://itmir.tistory.com/624 [미르의 IT 정복기]
    class InsertData extends AsyncTask<String,Void, String> {

        @Override
        protected String doInBackground(String... params) {

            email = (String) params[0];
            username = (String) params[1];
            type = (String) params[2];

            String serverURL = "http://13.124.67.214/caramelo/account_management/join_kakao.php";
            //http로 했을 때 cast안 된다는 에러메시지..
            String postParameters = null;
            //postParameters = "email="+email+"&username="+username+"&password="+password+"&type="+type;
            postParameters = "email="+email+"&username="+username+"&type="+type;
            Log.d(TAG+"체크3", email+username+type);

            try {

                //https://developer.android.com/reference/java/net/HttpURLConnection.html
                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                //HTTP Methods
                //HttpURLConnection uses the GET method by default. It will use POST if setDoOutput(true) has been called.
                // Other HTTP methods (OPTIONS, HEAD, PUT, DELETE and TRACE) can be used with setRequestMethod(String).
                //아래줄은 예제에서 주석처리 되어 있던 것. 이거 살리면 값이 안 보내지는 걸로 나옴.
                //httpURLConnection.setRequestProperty("content-type", "application/json");
                httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                //ㄴ출처: http://cholol.tistory.com/397 [IT, I Think ]
                //저거 해도 한글 디비 입력은 안 됨...
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();

                OutputStream outputStream = new BufferedOutputStream(httpURLConnection.getOutputStream());
                //원래 아래같았는데 buffer성능이 좋다고 해서 위로 바꿨고 잘 됨
                // OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                //flush-> 스트림의 버퍼에 있는 모든 내용을 출력소스에 쓴다.
                // 버퍼가 있는 출력 스트림의 경우에만 의미가 있으며 outputstream엥 정의된 flush는 아무런 일도 하지 않음
                outputStream.flush();
                outputStream.close();

                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "POST response code-"+responseStatusCode);

                InputStream inputStream;
                if(responseStatusCode==HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                } else {
                    inputStream = httpURLConnection.getErrorStream();
                    //Returns the error stream if the connection failed but the server sent useful data nonetheless.
                }

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                //StringBuilder 객체는 string과 마찬가지로 문자열을 담는 역할을 하지만, 차이점이 있습니다.
                // 그것은 문자열을 수정할 수 있다는 점입니다. 이러한 특성을 C#에서는 mutable이라고 합니다.
                StringBuilder sb = new StringBuilder();
                String line = null;

                while ((line = bufferedReader.readLine()) !=null) {
                    sb.append(line);
                    //append 지정된 문자(열)를 출력소스에 출력한다
                }

                bufferedReader.close();
                return sb.toString();

            } catch (Exception e) {

                Log.d(TAG, "InsertData: Error ", e);

                return new String("Error: " + e.getMessage());
            } //catch


        }//doInBackground..

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            tv_result.setText(result);
            Log.d(TAG, "POST response-"+result);

            View focusView = null;
            boolean cancel = false;

            //결과를 얼럿으로 띄우기
            //성공했을 때 코드 0 반환됨
            if(result.equals("0")){
                //로그
                Log.d(TAG,"잘 처리 됨");
                tv_joinUsernameChk.setText("");
                tv_joinEmailChk.setText("");

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(JoinKakaoActivity.this);
                alertDialogBuilder
                        .setTitle("알림")
                        .setMessage("회원가입 되었습니다.")
                        .setCancelable(true)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        });
                AlertDialog dialog = alertDialogBuilder.create();
                dialog.show();
            } else if(result.equals("1")){

                //로그
                Log.d(TAG,"에러 발생! ERRCODE = " + result);
                tv_joinUsernameChk.setText("");
                tv_joinEmailChk.setText("");

                //출처: http://cholol.tistory.com/404?category=572900 [IT, I Think ]

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(JoinKakaoActivity.this);
                alertDialog
                        .setTitle("알림")
                        .setMessage("등록 중 에러가 발생했습니다.")
                        .setCancelable(true)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        });
                AlertDialog dialog = alertDialog.create();
                dialog.show();
            } else if(result.equals("유저네임중복")) {

                //로그
                Log.d(TAG,"유저네임 중복= " + result);
                tv_joinUsernameChk.setText("이미 등록된 유저네임입니다.");
                tv_joinEmailChk.setText("");
                focusView = tv_joinUsernameChk;
                focusView.requestFocus();


            } else if(result.equals("데이터없음")) {
                tv_result.setText(result);
            }

        }//onPostExecute



    }//async



    //뒤로 가기 두번 누르면 종료
    @Override public void onBackPressed() {
        super.onBackPressed();
        backPressCloseHandler.onBackPressed();
    }


    //액티비티 종료 시
    @Override
    public void finish() {
        super.finish();
        //애니메이션 없앰
        overridePendingTransition(0, 0);
    }

}//activity
