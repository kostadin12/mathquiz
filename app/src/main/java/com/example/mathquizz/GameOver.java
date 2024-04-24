package com.example.mathquizz;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;

import com.google.firebase.auth.FirebaseUser;

public class GameOver extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseUser user;
    Button logout;
    Button mainMenu;

    TextView scoreText;
    TextView highScoreText;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game_over);

        auth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        logout = findViewById(R.id.new_game);
        mainMenu = findViewById(R.id.return_to_menu);
        scoreText = findViewById(R.id.your_score);
        highScoreText = findViewById(R.id.high_score);

        //if user is null go to login
        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        }

        //displays the score and the high score
        Intent scoreIntent = getIntent();
        scoreText.setText("Твоят резултат: " + scoreIntent.getIntExtra("score", 0));
        highScoreText.setText("Топ резултат: " + + scoreIntent.getLongExtra("highScore", 0));

        //start the game again
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MathQuiz.class);
                startActivity(intent);
                finish();
            }
        });

        //got to main menu
        mainMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}