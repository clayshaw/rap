package com.example.demo.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.demo.dto.GeminiMessageDto;
import com.google.api.client.util.Value;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;

@Service
public class GenimiService {
  
  private static final Client client = new Client.Builder()
      .apiKey(System.getenv("GEMINI_API_KEY"))
      .build();

  /**
   * 舊的方法
   */
  public String GenerateTextFromTextInput(String args) {
    GenerateContentResponse response =
        client.models.generateContent(
            "gemini-2.5-flash",
            args,
            null);
    return response.text();
  }


  public String generateChatResponse(List<GeminiMessageDto> history) {
    try {
      
      //    將 ".part(...)" 改成 ".parts(List.of(...))"
      
      List<Content> googleAiHistory = history.stream()
          .map(msg -> Content.builder()
              .role(msg.role())
              // 先建立一個 "Part"
              .parts(List.of(Part.fromText(msg.content()))) 
              .build())
          .collect(Collectors.toList());

      //呼叫 "generateContent" 的多載版本，傳入 "完整" 的歷史紀錄
      GenerateContentResponse response =
          client.models.generateContent(
              "gemini-2.5-flash",
              googleAiHistory,
              null);
      System.out.println("Google AI Response: " + response.text());
      return response.text();

    } catch (Exception e) {
        e.printStackTrace();
        return "Error from Google AI: " + e.getMessage();
    }
  }
}