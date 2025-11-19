package com.example.demo.service;

import org.springframework.stereotype.Service;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;

@Service
public class ChatGptService {

    private static final OpenAIClient client = OpenAIOkHttpClient.builder().baseUrl("https://free.v36.cm")
        .apiKey("sk-xrdieX3Fw00Kc6lv05A78519C5C248B9993a0fBeEa951cE4")
        .build();

    public static String generateResponse() {
        

        ResponseCreateParams params = ResponseCreateParams.builder()
                .input("Say this is a test")
                .model("gpt-3.5-turbo")
                .build();

        Response response = client.responses().create(params);
        return response.output().toString();
    }
}
