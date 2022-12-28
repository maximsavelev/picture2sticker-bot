package com.savelev.telegram.bot.core;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    @Value("${bot_token}")
    private String botToken;

    @Value("${bot_username}")
    private String botUsername;

    private final String resizedImageMessage = "Sticker %s - maintain proportions." +
                                               "\n Send @Sticker to add it to sticker pack";

    private final String plainImageMessage = "Sticker (512x512) - ultra wide." +
                                             "\n Send @Sticker to add it to sticker pack";


    private final String helpMessage = """
            Hello! Follow these steps:
            1) Send me picture
            2)Choose the most suitable sticker(there are two size options: maintain proportions and plain 512x512)
            3)Forward it to @Stickers""";

    private final String unknownMessage = "Unknown command. Type /help for help.";

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            var chatId = update.getMessage().getChatId();
            if (update.getMessage().hasText()) {
                var text = update.getMessage().getText();
                try {
                    switch (text) {
                        case "/start", "/help" -> sendMessage(chatId, helpMessage);
                        default -> sendMessage(chatId, unknownMessage);
                    }
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            } else if (update.getMessage().hasPhoto()) {
                try {
                    File file = downloadPhoto(update.getMessage().getPhoto(), "src/main/resources/saves/source.jpg");
                    ImageConverter imageConverter = new ImageConverter(file);
                    sendDocument(chatId, imageConverter.saveResized("src/main/resources/saves/resized.webp"), null);
                    sendMessage(chatId, String.format(resizedImageMessage, imageConverter.getResizedSize()));
                    sendDocument(chatId, imageConverter.save("src/main/resources/saves/wide.webp"));
                    sendMessage(chatId, plainImageMessage);
                } catch (TelegramApiException | IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

    }
    private void sendMessage(long chatId, String textToSend) throws TelegramApiException {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(textToSend).build();
        execute(message);
    }

    private File downloadPhoto(List<PhotoSize> photos, String path) throws TelegramApiException {
        var photo = photos.get(photos.size() - 1).getFileId();
        GetFile getFile = GetFile.builder()
                .fileId(photo)
                .build();
        var filePath = execute(getFile).getFilePath();
        return downloadFile(filePath, new File(path));
    }


    private void sendDocument(Long chatId, File save, String caption) throws TelegramApiException {
        SendDocument sendDocument = SendDocument.builder()
                .chatId(chatId)
                .document(new InputFile(save))
                .caption(caption)
                .build();
        execute(sendDocument);
    }

    private void sendDocument(Long chatId, File save) throws TelegramApiException {
        sendDocument(chatId, save, null);
    }
}
