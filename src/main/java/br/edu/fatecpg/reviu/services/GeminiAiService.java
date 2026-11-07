package br.edu.fatecpg.reviu.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@Service
public class GeminiAiService {

    private final String apiKey;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public GeminiAiService(@Value("${gemini.api.key}") String apiKey, ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
    }

    public String createCards(String textFromPdf) {
        // URL Corrigida e Definitiva para o modelo Flash
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash-lite:generateContent?key=" + apiKey;

        String prompt = """
                Você é um especialista em técnicas de estudo e repetição espaçada (Anki/Flashcards).
                Sua missão é transformar o conteúdo bruto abaixo em cards de estudo otimizados.
                
                ANÁLISE DE CONTEXTO:
                Adapte o estilo do card baseando-se no tipo de informação encontrada:
                1. **Para Idiomas/Vocabulário:**
                   - Frente: A palavra ou frase no idioma original.
                   - Verso: A tradução + um exemplo curto de uso (se couber).
                2. **Para Conceitos Técnicos/Definições:**
                   - Frente: O nome do conceito ou termo chave.
                   - Verso: A definição direta e objetiva.
                3. **Para Fatos Históricos/Dados:**
                   - Frente: A data, evento ou nome.
                   - Verso: O que aconteceu ou a importância.
                4. **Para Teoria Geral:**
                   - Frente: Uma pergunta desafiadora.
                   - Verso: A resposta explicativa.

                REGRAS RÍGIDAS DE SAÍDA:
                - Gere entre 15 e 50 cards (se houver conteúdo suficiente).
                - O JSON deve ser puro, sem Markdown (```json) e sem comentários.
                - Responda EXATAMENTE neste formato:
                
                [
                    {
                        "frontText": "texto da frente (termo, pergunta ou palavra)",
                        "backText": "texto do verso (tradução, definição ou resposta)"
                    }
                ]
                
                
                CONTEÚDO PARA EXTRAÇÃO:
                %s
                """.formatted(textFromPdf);

        try {
            // Corpo da requisição
            String jsonRequest = objectMapper.writeValueAsString(Map.of(
                    "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                    "generationConfig", Map.of("responseMimeType", "application/json")
            ));

            // Envia usando Java Nativo (Sem dependência do Google)
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.err.println("ERRO GOOGLE: " + response.body());
                throw new RuntimeException("Erro na API do Google: " + response.statusCode());
            }

            return extractContent(response.body());

        } catch (Exception e) {
            throw new RuntimeException("Falha ao gerar cards: " + e.getMessage());
        }
    }

    private String extractContent(String rawResponse) throws Exception {
        JsonNode root = objectMapper.readTree(rawResponse);

        // Verifica erro no JSON
        if (root.has("error")) {
            throw new RuntimeException(root.get("error").get("message").asText());
        }

        // Extrai o texto
        return root.path("candidates")
                .get(0)
                .path("content")
                .path("parts")
                .get(0)
                .path("text")
                .asText()
                .replace("```json", "")
                .replace("```", "")
                .trim();
    }
}