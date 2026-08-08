package com.price_tracker.saas.service;
import org.springframework.beans.factory.annotation.Value;
import com.price_tracker.saas.model.Usuario;
import com.price_tracker.saas.repository.UsuarioRepository;
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

    private final UsuarioRepository usuarioRepository;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.username}")
    private String botUsername;


    // =========================================================
    // CONSTRUTOR
    // =========================================================

    public TelegramBotService(
            UsuarioRepository usuarioRepository
    ) {
        this.usuarioRepository = usuarioRepository;
    }


    // =========================================================
    // INICIAR BOT
    // =========================================================

    @PostConstruct
    public void iniciarBot() {

        try {

            TelegramBotsApi botsApi =
                    new TelegramBotsApi(
                            DefaultBotSession.class
                    );

            botsApi.registerBot(this);

            System.out.println(
                    "✅ [TELEGRAM] Bot conectado e escutando!"
            );

        } catch (TelegramApiException e) {

            System.out.println(
                    "❌ Erro ao conectar o Bot: "
                            + e.getMessage()
            );
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


    // =========================================================
    // RECEBER MENSAGENS
    // =========================================================

    @Override
    public void onUpdateReceived(Update update) {

        if (!update.hasMessage()) {
            return;
        }

        if (!update.getMessage().hasText()) {
            return;
        }


        String mensagem =
                update.getMessage().getText().trim();

        String chatId =
                update.getMessage()
                        .getChatId()
                        .toString();


        System.out.println(
                "💬 Telegram recebeu: "
                        + mensagem
                        + " | Chat ID: "
                        + chatId
        );


        // =====================================================
        // /start
        // =====================================================

        if (mensagem.equals("/start")) {

            enviarMensagem(
                    chatId,
                    "🚀 Olá! Sou o Bot do Price Tracker.\n\n"
                            + "Para conectar sua conta, "
                            + "use o botão \"Conectar Telegram\" "
                            + "dentro do Price Tracker."
            );

            return;
        }


        // =====================================================
        // /start CODIGO
        // =====================================================

        if (mensagem.startsWith("/start ")) {

            String codigo =
                    mensagem
                            .substring(7)
                            .trim()
                            .toUpperCase();


            System.out.println(
                    "🔐 Código recebido: "
                            + codigo
            );


            Usuario usuario =
                    usuarioRepository
                            .findByTelegramCodigo(codigo)
                            .orElse(null);


            // =================================================
            // CÓDIGO INVÁLIDO
            // =================================================

            if (usuario == null) {

                System.out.println(
                        "❌ Código Telegram não encontrado: "
                                + codigo
                );

                enviarMensagem(
                        chatId,
                        "❌ Código inválido ou expirado.\n\n"
                                + "Volte ao Price Tracker e "
                                + "clique novamente em "
                                + "\"Conectar Telegram\"."
                );

                return;
            }


            // =================================================
            // VINCULAR TELEGRAM
            // =================================================

            usuario.setTelegramChatId(chatId);

            usuario.setTelegramCodigo(null);

            usuarioRepository.save(usuario);


            System.out.println(
                    "✅ Telegram conectado para: "
                            + usuario.getEmail()
            );


            enviarMensagem(
                    chatId,
                    "✅ Telegram conectado com sucesso!\n\n"
                            + "Agora você receberá os alertas "
                            + "de queda de preço dos seus produtos "
                            + "diretamente aqui."
            );
        }
    }


    // =========================================================
    // ENVIAR ALERTA DE PREÇO
    // =========================================================

    public void enviarAlerta(
            String chatId,
            String nomeProduto,
            String link,
            String preco
    ) {

        String texto =
                "🚨 PREÇO CAIU! 🚨\n\n"
                        + "Produto: "
                        + nomeProduto
                        + "\n\n"
                        + "Atingiu: R$ "
                        + preco
                        + "\n\n"
                        + "🛒 Comprar:\n"
                        + link;


        enviarMensagem(
                chatId,
                texto
        );
    }


    // =========================================================
    // ENVIAR MENSAGEM
    // =========================================================

    private void enviarMensagem(
            String chatId,
            String texto
    ) {

        SendMessage message =
                new SendMessage();

        message.setChatId(chatId);
        message.setText(texto);


        try {

            execute(message);

        } catch (TelegramApiException e) {

            System.out.println(
                    "❌ Erro ao enviar mensagem no Telegram: "
                            + e.getMessage()
            );
        }
    }
}