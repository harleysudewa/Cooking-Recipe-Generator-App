package com.example.recipeapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class EnglishMainActivity extends AppCompatActivity {
    private EditText editTextIngredients;
    private RecyclerView recyclerView;
    private ItemAdapter itemAdapter;
    private List<String> items;
    RadioButton rb1, rb2, rb3, rb4, rb5;
    RadioGroup radioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_english_main);

        editTextIngredients = findViewById(R.id.editTextPrompt);
        Button buttonOk = findViewById(R.id.buttonOk);
        recyclerView = findViewById(R.id.recyclerView);
        radioGroup = findViewById(R.id.radioGroup);
        rb1 = findViewById(R.id.rb1);
        rb2 = findViewById(R.id.rb2);
        rb3 = findViewById(R.id.rb3);
        rb4 = findViewById(R.id.rb4);
        rb5 = findViewById(R.id.rb5);

        // Inisialisasi RecyclerView
        items = new ArrayList<>();
        itemAdapter = new ItemAdapter(items);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(itemAdapter);

        buttonOk.setOnClickListener(v -> {
            String ingredients = editTextIngredients.getText().toString();
            String preference = getSelectedPreference();
            String prompt = createPromptWithIngredients(ingredients, preference);
            modelCall(prompt);
        });
    }

    private String createPromptWithIngredients(String ingredients, String preference) {
        return "I want you to be as my personal chef. I only have the following ingredients: "
                + ingredients
                + ". My preferred food recipe preference is "
                + preference
                + ". Name 5 food recipes that can be made only from these ingredients and how to make them. Please make your answer in the following format.\n"
                + "1. \"Food Name\"\n"
                + "- Ingredients:\n"
                + "- How to make:";
    }

    public void modelCall(String prompt) {
        GenerativeModel gm =
                new GenerativeModel("gemini-1.5-flash", "YOUR_GEMINI_API_KEY");
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        Content content = new Content.Builder().addText(prompt).build();
        Executor executor = Executors.newSingleThreadExecutor();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        Futures.addCallback(
                response,
                new FutureCallback<GenerateContentResponse>() {
                    @Override
                    public void onSuccess(GenerateContentResponse result) {
                        runOnUiThread(() -> {
                            String promptResult = result.getText();
                            String cleanedResult = promptResult.replaceAll("[*]", "").trim();
                            String finalResult = "Input:\nIngredients: "+ editTextIngredients.getText().toString() + "\nFood Preference: " + getSelectedPreference() + "\n\nOutput:\n" + cleanedResult;
                            items.add(finalResult);
                            itemAdapter.notifyDataSetChanged();
                        });
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        t.printStackTrace();
                        runOnUiThread(() -> items.add("Error: " + t.getMessage()));
                        itemAdapter.notifyDataSetChanged();
                    }
                },
                executor);


    }
    private String getSelectedPreference() {
        int selectedId = radioGroup.getCheckedRadioButtonId();
        RadioButton selectedRadioButton = findViewById(selectedId);
        return selectedRadioButton.getText().toString();
    }
}
