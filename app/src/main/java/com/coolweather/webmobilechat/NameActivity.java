package com.coolweather.webmobilechat;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class NameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name);

        // getActionBar().hide();
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) actionBar.hide();

        Button btnJoin = (Button) findViewById(R.id.btnJoin);
        final EditText txtName = (EditText) findViewById(R.id.name);

        btnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(txtName.getText().toString().trim().length() > 0){
                    String name = txtName.getText().toString().trim();
                    Intent intent = new Intent(NameActivity.this, MainActivity.class);
                    intent.putExtra("name", name);
                    startActivity(intent);
                }else{
                    Toast.makeText(NameActivity.this, "输个名字行不行(●'◡'●)",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
