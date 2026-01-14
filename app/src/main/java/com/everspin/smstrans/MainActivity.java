package com.everspin.smstrans;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.Manifest;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


public class MainActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_CODE = 100;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!hasSmsPermission()) {
            requestSmsPermission();
        }

        EditText editText = findViewById(R.id.receive_num_ET);
        TextView numberList = findViewById(R.id.SPList);


        SharedPreferences SP = getSharedPreferences("receiveNum", MODE_PRIVATE);
        SharedPreferences.Editor editor = SP.edit();


        AppCompatButton enterBtn = findViewById(R.id.receive_num_enter_Btn);
        enterBtn.setOnClickListener(view -> {
            String normalized = normalizeNumber(editText.getText().toString());

            if (!isValidJapaneseNumber(normalized)) {
                Log.d(TAG, "Invalid JP number: " + editText.getText().toString());
                Toast.makeText(this, "This is the wrong number. Please type it again ", Toast.LENGTH_SHORT).show();
                return;
            }
            if (SP.getString("Numbers","").contains(normalized)) {
                Log.d(TAG, "Overlapping number: " + editText.getText().toString());
                Toast.makeText(this, "This is the overlapping number. Please type it again ", Toast.LENGTH_SHORT).show();
                return;
            }
            editor.putString("Numbers",SP.getString("Numbers","") +","+normalized);
            editor.apply();

            numberList.setText(SP.getString("Numbers","").replaceAll(",","\n"));

        });
        if (SP.getString("Numbers",null) != null) {
            numberList.setText(SP.getString("Numbers","").replaceAll(",","\n"));
        }
    }

    private String normalizeNumber(String input) {
        if (input == null) return null;
        return input.replaceAll("\\D", "");
    }

    private boolean isValidJapaneseNumber(String raw) {
        if (raw == null) return false;

        String num = normalizeNumber(raw);

        // 국제번호 +81 → 국내형 0
        if (num.startsWith("81") && num.length() >= 11) {
            num = "0" + num.substring(2);
        }

        // 📱 개인 휴대폰 / IP전화 / IoT
        if (num.matches("^(090|080|070|050|020)\\d{8}$")) {
            return true;
        }

        // 기업 SMS 숏코드 (은행·카드사)
        if (num.length() >= 5 && num.length() <= 9) {
            return true;
        }

        return false;
    }



    private boolean hasSmsPermission() {
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECEIVE_SMS
        ) == PackageManager.PERMISSION_GRANTED
                &&
                ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_SMS
                ) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestSmsPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{
                        Manifest.permission.RECEIVE_SMS,
                        Manifest.permission.READ_SMS
                },
                SMS_PERMISSION_CODE
        );
    }

    // 🔥 여기서 override
    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == SMS_PERMISSION_CODE) {
            boolean granted = true;

            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    granted = false;
                    break;
                }
            }

            if (granted) {
                // ✅ 이제부터 SmsReceiver 가 정상적으로 호출됨
            } else {
                // ❌ 퍼미션 거부 → 사용자 안내 필요
                requestSmsPermission();
            }
        }
    }
}
