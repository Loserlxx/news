package com.example.news.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.news.App;
import com.example.news.R;
import com.wilddog.wilddogauth.core.Task;
import com.wilddog.wilddogauth.core.listener.OnCompleteListener;
import com.wilddog.wilddogauth.core.result.AuthResult;
import com.wilddog.wilddogauth.model.WilddogUser;

import java.io.ByteArrayOutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InfoActivity extends AppCompatActivity {

    @BindView(R.id.et_email_activity_info)
    TextInputEditText etEmailActivityInfo;
    @BindView(R.id.et_email_layout_activity_info)
    TextInputLayout etEmailLayoutActivityInfo;
    @BindView(R.id.et_password_activity_info)
    TextInputEditText etPasswordActivityInfo;
    @BindView(R.id.et_password_layout_activity_info)
    TextInputLayout etPasswordLayoutActivityInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        ButterKnife.bind(this);

        etEmailActivityInfo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (s.toString().trim().length() < 6) {

                    etEmailLayoutActivityInfo.setError("邮箱不能少于6个字符！");

                } else {

                    etEmailLayoutActivityInfo.setErrorEnabled(false);

                }

            }
        });

        etPasswordActivityInfo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (s.toString().trim().length() < 6) {

                    etPasswordLayoutActivityInfo.setError("password不能少于6个字符！");

                } else {

                    etPasswordLayoutActivityInfo.setErrorEnabled(false);

                }

            }
        });


    }

    public void register(View v) {

        String email = etEmailActivityInfo.getText().toString().trim();
        String password = etPasswordActivityInfo.getText().toString().trim();

        App.auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // 获取用户
                            WilddogUser user = task.getResult().getWilddogUser();
                            App.user = user;
                        } else {
                            // 错误处理
                            Log.d("result", task.getException().toString());

                        }
                    }
                });

    }

    public void login(View v) {

        String email = etEmailActivityInfo.getText().toString().trim();
        String password = etPasswordActivityInfo.getText().toString().trim();

        App.auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete( Task<AuthResult> task) {
                        Log.d(InfoActivity.class.getName(), "signInWithEmail:onComplete:" + task.isSuccessful());
                        if (!task.isSuccessful()) {
                            Log.w(InfoActivity.class.getName(), "signInWithEmail", task.getException());
                            Toast.makeText(InfoActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }else {

                            showDialog();

                        }
                    }
                });

    }

    private void showDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("请选择您的头像")
                .setItems(new String[]{"照相", "图库"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        switch (which) {

                            case 0:
                                fromCamera();
                                break;

                            case 1:
                                fromGallery();
                                break;
                        }

                    }
                }).show();

    }

    private void fromGallery() {
    }

    private void fromCamera() {
        //
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,0);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 0 && resultCode == RESULT_OK){

            Bundle extras = data.getExtras();

            Bitmap bitmap = (Bitmap) extras.get("data");
            //bitmap不能直接上传到云端，因为网络传输图片时，会有些字符无法识别，acsii 128 % & *  大小写字母+ + -  26+26+2 = 64  base64
            //如果遇到网络图片传输问题，首先要把图片进行base64转化  ascii  base64 iso-8859-1 utf-8
            //都是 二进制  0101 0101 1011 1010 1101010101   8 * 8  = 3 + 3 = 6
            //ascii 8位
            //base64 6位

            //把bitmap转化字符串，字符串就可以在网络上传输
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG,100,out);
            String imgStr = Base64.encodeToString(out.toByteArray(), Base64.DEFAULT);
            App.ref.child(App.user.getUid()).setValue(imgStr);

        }

    }
}
