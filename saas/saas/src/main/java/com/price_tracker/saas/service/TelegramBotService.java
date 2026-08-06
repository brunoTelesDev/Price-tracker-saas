package com.price_tracker.saas.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Service
public class TelegramBotService extends TelegramLongPollingBot {

    // A identidade oficial do seu Bot
    private final String botToken = "8830479004:AAHqYnrvVmxMBPH-0bCBHLNdyYUr0AV78Eg";
    private final String botUsername = "MeuPriceTracker_bot";

    // Quando o Spring Boot ligar, ele avisa os servidores do Telegram que estamos online!
    @PostConstruct
    public void iniciarBot() {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(this);
            System.out.println("✅ [TELEGRAM] Bot conectado e escutando!");
        } catch (TelegramApiException e) {
            System.out.println("❌ Erro ao conectar o Bot: " + e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    // 🎧 O Bot fica escutando as mensagens que chegam no celular
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String mensagemUsuario = update.getMessage().getText();
            String chatId = update.getMessage().getChatId().toString(); // O SEU ID SECRETO!

            System.out.println("💬 Mensagem recebida no Telegram! ChatID: " + chatId);

            // Se você mandar /start, ele te responde com o seu ID
            if (mensagemUsuario.equals("/start")) {
                enviarMensagem(chatId, "🚀 Olá! Sou o Bot do seu Price Tracker.\n\nO seu Chat ID secreto é: " + chatId + "\n\nGuarde este número, nós vamos usar ele no banco de dados!");
            }
        }
    }

    // 🚨 Esse é o método que o nosso Despertador vai usar para mandar o alerta de preço baixo
    public void enviarAlerta(String chatId, String nomeProduto, String link, String preco) {
        String texto = "🚨 PREÇO CAIU! 🚨\n\n" +
                "O produto: " + nomeProduto + "\n" +
                "Atingiu o valor de: R$ " + preco + "\n\n" +
                "Corra para comprar: " + link;
        enviarMensagem(chatId, texto);
    }

    // Método interno para disparar a mensagem
    private void enviarMensagem(String chatId, String texto) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(texto);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.out.println("❌ Erro ao enviar mensagem no Telegram: " + e.getMessage());
        }
    }
}