package it.unibz.inf.pp.clash.model.bot;

import it.unibz.inf.pp.clash.model.snapshot.impl.GameSnapshot;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class LLMBot implements BotPlayer {

    private StringBuilder llmContext = new StringBuilder();
    private String apiUrl = "https://api.groq.com/openai/v1/chat/completions";
    // DO NOT COMMIT THIS
    private String apiKey = "";

    // Sends a message to groq and returns the content of the reply
    private Optional<String> sendRestMessage() {
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setDoOutput(true);

            StringBuilder jsonSb = new StringBuilder();
            jsonSb.append("{\n\"messages\":[\n");
            jsonSb.append(llmContext.toString());
            jsonSb.append("""
                    ],
                     "model": "meta-llama/llama-4-scout-17b-16e-instruct",
                     "temperature": 1,
                     "max_completion_tokens": 1024,
                     "top_p": 1,
                     "stream": false,
                     "stop": null
                    }
                    """.stripIndent());

            var jsonPayload = jsonSb.toString();
            System.out.println("JSON Payload: " + jsonPayload);


            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (var reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = reader.readLine()) != null) {
                        response.append(responseLine.trim());
                    }

                    var entireResponse = response.toString();
                    String split = entireResponse.split("content\":")[1].split("}")[0];
                    // Remove the quotations around strings
                    var responseContent = split.trim().substring(1, split.length() - 1);


                    System.out.println("Response: " + responseContent);
                    return Optional.of(responseContent);

                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    private void appendMessageToLLMContext(String role, String content) {
        if (!llmContext.isEmpty()) {
            llmContext.append(",\n");
        }
        llmContext.append("{\n\"role\":\"");
        llmContext.append(role);
        llmContext.append("\",\n\"content\":\"");
        llmContext.append(content);
        llmContext.append("\"\n}");
    }


    // See: https://console.groq.com/docs/overview
    private Move chooseMove(GameSnapshot gs) {
        appendMessageToLLMContext("user", "Hi! Please reply with either Hi or Hello. Use a 50% chance to choose");
        var responseMaybe = sendRestMessage();
        if (responseMaybe.isPresent()) {
            appendMessageToLLMContext("assistant", responseMaybe.get());
            appendMessageToLLMContext("user", "Now, if the response you gave me was Hi, reply with LOL. If it was Hello, reply with XD");
            var secondResponseMaybe = sendRestMessage();

            secondResponseMaybe.ifPresent(str -> System.out.println("PARSED: " + str));
        }

        return null;


    }

    @Override
    public void PlayMove(GameSnapshot gs) {
        chooseMove(gs);

    }
}
