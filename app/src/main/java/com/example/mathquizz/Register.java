package com.example.mathquizz;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.example.mathquizz.DBCryptService.encodePassword;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Register extends AppCompatActivity {

    FirebaseAuth mAuth;
    ProgressBar progressBar;

    TextInputEditText editTextName, editTextEmail, editTextPassword, editTextConfirmPassword;
    Button buttonReg;
    TextView textView;

    //if the user is already logged in it will automatically go to the main activity
    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        editTextName = findViewById(R.id.name);
        editTextConfirmPassword = findViewById(R.id.confirm_password);
        progressBar = findViewById(R.id.progressBar);
        textView = findViewById(R.id.click_to_login);
        buttonReg = findViewById(R.id.btn_register);

        buttonReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                String name, email, password;

                //check if the passwords are the same
                if (!Objects.equals(String.valueOf(editTextPassword.getText()), String.valueOf(editTextConfirmPassword.getText()))) {
                    Toast.makeText(Register.this, "Паролите не съвпадат.", Toast.LENGTH_SHORT).show();
                    return;
                }

                //get the input
                name = String.valueOf(editTextName.getText());
                email = String.valueOf(editTextEmail.getText());
                password = encodePassword(editTextPassword);

                //if the fields are empty
                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(Register.this, "Моля въведете потребителско име.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(Register.this, "Моля въведете имейл.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(Register.this, "Моля въведете парола.", Toast.LENGTH_SHORT).show();
                    return;
                }

                //creating user
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                    if (firebaseUser != null) {
                                        String userId = firebaseUser.getUid();

                                        UserProfileChangeRequest changeRequest = new UserProfileChangeRequest.Builder()
                                                .setDisplayName(name)
                                                .build();

                                        firebaseUser.updateProfile(changeRequest);

                                        Map<String, Integer> userData = new HashMap<>();
                                        userData.put("highScore", 0);
                                        FirebaseFirestore.getInstance().collection("users")
                                                .document(userId)
                                                .set(userData)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Toast.makeText(Register.this, "Успешно създадохте акаунт.",
                                                                Toast.LENGTH_SHORT).show();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(Register.this, "Грешка при създаване на акаунт.",
                                                                Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                } else {
                                    Toast.makeText(Register.this, "Неуспешна идентификация.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        //click go login
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });
    }
}