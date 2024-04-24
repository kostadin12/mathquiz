package com.example.mathquizz;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mathquizz.entity.Question;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class MathQuiz extends AppCompatActivity {
    private final static String EASY = "EASY";
    private final static String MEDIUM = "MEDIUM";
    private final static String HARD = "HARD";
    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseFirestore db;

    Button option1Button, option2Button, option3Button, option4Button;
    TextView scoreTextView, multiplierTextView, livesTextView, questionTextView;

    List<Question> questions;
    List<Question> questionsEasy;
    List<Question> questionsMedium;
    List<Question> questionsHard;
    List<String> options;
    Question correctQuestion;
    int hardCap = 40;
    int mediumCap = 15;
    int score = 0;
    int streak = 0;
    int multiplier = 1;
    int lives = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_math_quiz);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        option1Button = findViewById(R.id.option1Button);
        option2Button = findViewById(R.id.option2Button);
        option3Button = findViewById(R.id.option3Button);
        option4Button = findViewById(R.id.option4Button);
        scoreTextView = findViewById(R.id.scoreTextView);
        multiplierTextView = findViewById(R.id.multiplierTextView);
        livesTextView = findViewById(R.id.livesTextView);
        questionTextView = findViewById(R.id.questionTextView);
        updateViews();

        //throws the user to login if null
        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        }

        loadQuestions();

        View.OnClickListener answerClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button clickedButton = (Button) v;
                checkAnswer(clickedButton.getText().toString());
            }
        };

        option1Button.setOnClickListener(answerClickListener);
        option2Button.setOnClickListener(answerClickListener);
        option3Button.setOnClickListener(answerClickListener);
        option4Button.setOnClickListener(answerClickListener);
    }

    private void loadQuestions() {
        questionsEasy = new ArrayList<>();
        questionsMedium = new ArrayList<>();
        questionsHard = new ArrayList<>();
        questions = new ArrayList<>();
        db.collection("questions")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(com.google.firebase.firestore.QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            Question question = document.toObject(Question.class);
                            if (question == null) {
                                continue;
                            }
                            String difficulty = question.getDifficulty();
                            if (difficulty == null) {
                                continue;
                            }
                            switch (difficulty.toUpperCase()) {
                                case EASY:
                                    questionsEasy.add(question);
                                    break;
                                case MEDIUM:
                                    questionsMedium.add(question);
                                    break;
                                case HARD:
                                    questionsHard.add(question);
                                    break;
                                default:
                                    break;
                            }
                            questions.add(question);
                        }
                        startQuiz();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MathQuiz.this, "Не може да зареди въпросите", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void startQuiz() {
        Random random = new Random();
        correctQuestion = returnQuestion(checkDifficulty((score)));
        options = new ArrayList<>();
        options.add(correctQuestion.getAnswer());
        while (options.size() < 4) {
            Question randomQuestion = questions.get(random.nextInt(questions.size()));
            //check to not have dupes
            if (!options.contains(randomQuestion.getAnswer())) {
                options.add(randomQuestion.getAnswer());
            }
        }
        Collections.shuffle(options);
        questionTextView.setText(correctQuestion.getQuestion());
        resetButtonColors();
        option1Button.setText(options.get(0));
        option2Button.setText(options.get(1));
        option3Button.setText(options.get(2));
        option4Button.setText(options.get(3));
    }

    private void checkAnswer(String selectedOption) {
        Button selectedButton = findButtonByText(selectedOption);
        if (selectedOption.equals(correctQuestion.getAnswer())) {
            streak++;
            multiplier = Math.min(streak, 3);
            score += multiplier;
            selectedButton.setBackgroundColor(Color.GREEN);
            questions.removeIf(question -> Objects.equals(question.getAnswer(), correctQuestion.getAnswer()));
        } else {
            streak = 0;
            multiplier = 1;
            lives--;
            selectedButton.setBackgroundColor(Color.RED);
            Button correctButton = findButtonByText(correctQuestion.getAnswer());
            correctButton.setBackgroundColor(Color.GREEN);
            if (lives <= 0) {
                //go to game over
                Intent intent = new Intent(getApplicationContext(), GameOver.class);
                checkIfHighScoreBeat(score, new HighScoreCallback() {
                    @Override
                    public void onHighScoreChecked(long highScore) {
                        intent.putExtra("топ резултат", highScore);
                        intent.putExtra("резултат", score);
                        startActivity(intent);
                        finish();
                    }
                });
            }
        }
        updateViews();
        new Handler().postDelayed(this::startQuiz, 1000);
    }

    private void checkIfHighScoreBeat(int score, HighScoreCallback callback) {
        DocumentReference docRef = FirebaseFirestore.getInstance().collection("users").document(user.getUid());

        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                long highScore = 0;
                if (documentSnapshot.exists()) {
                    Map<String, Object> data = documentSnapshot.getData();
                    if (data != null) {
                        Object highScoreField = data.get("highScore");
                        if (highScoreField instanceof Map) {
                            Map<String, Long> highScoreMap = (Map<String, Long>) highScoreField;
                            Long highScoreValue = highScoreMap.get("highScore");
                            if (highScoreValue != null) {
                                highScore = highScoreValue;
                            } else {
                                Log.e("Firestore", "High score value not found in the highScoreMap");
                            }
                        } else {
                            Log.e("Firestore", "High score field is not of type Map");
                        }
                    } else {
                        Log.e("Firestore", "Document data is null");
                    }
                } else {
                    Log.e("Firestore", "Document does not exist");
                }

                if (score > highScore) {
                    updateHighScore(docRef, score, user.getDisplayName());
                    highScore = score;
                }

                callback.onHighScoreChecked(highScore);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                callback.onHighScoreChecked(0);
            }
        });
    }

    private void updateHighScore(DocumentReference docRef, int newHighScore, String name) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("highScore", newHighScore);
        updates.put("name", name);

        docRef.update(updates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(MathQuiz.this, "Нов най-висок резултат!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MathQuiz.this, "Провали се да актуализира най-високият резултат", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void resetButtonColors() {
        int defaultColor = Color.parseColor("#4fa095");
        option1Button.setBackgroundColor(defaultColor);
        option2Button.setBackgroundColor(defaultColor);
        option3Button.setBackgroundColor(defaultColor);
        option4Button.setBackgroundColor(defaultColor);
    }

    private void updateViews() {
        scoreTextView.setText("Резултат: " + score);
        multiplierTextView.setText("Сложност: x" + multiplier);
        livesTextView.setText(String.valueOf(lives));
    }
    private String checkDifficulty(int score)
    {
        if(score > mediumCap)
        {
            return MEDIUM;
        }
        if(score>hardCap)
        {
            return HARD;
        }
        return EASY;
    }
    private Question returnQuestion(String difficulty)
    {
        Random random = new Random();
        switch(difficulty)
        {
            case EASY: return questionsEasy.get(random.nextInt(questionsEasy.size()));
            case MEDIUM:return questionsMedium.get(random.nextInt(questionsMedium.size()));
            case HARD:return questionsHard.get(random.nextInt(questionsHard.size()));
            default: return null;
        }
    }
    private Button findButtonByText(String text) {
        if (option1Button.getText().toString().equals(text)) return option1Button;
        if (option2Button.getText().toString().equals(text)) return option2Button;
        if (option3Button.getText().toString().equals(text)) return option3Button;
        if (option4Button.getText().toString().equals(text)) return option4Button;
        return null;
    }
}
