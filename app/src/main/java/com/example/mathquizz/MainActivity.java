package com.example.mathquizz;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    Button logout;
    Button leaderboard;
    TextView textView;
    FirebaseUser user;
    TextView startGame;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        logout = findViewById(R.id.logout);
        leaderboard = findViewById(R.id.leaderboard);
        textView = findViewById(R.id.user_details);
        startGame = findViewById(R.id.math_quiz);
        //get user
        user = mAuth.getCurrentUser();

        //if there is no user open login
        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        } else {
            //display name
            textView.setText(" Здравей, " + user.getDisplayName() + "!");
        }

        //sign out the user and go to login
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });

        //go to leaderboard
        leaderboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Leaderboard.class);
                startActivity(intent);
                finish();
            }
        });

        //start the quiz
        startGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MathQuiz.class);
                startActivity(intent);
                finish();
            }
        });

    }
}