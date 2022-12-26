package com.savelev.telegram.bot.core;

import lombok.SneakyThrows;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@PropertySource("/application.properties")
public class TelegramBot extends TelegramLongPollingBot {

    @Value("${bot_token}")
    private  String botToken;

    @Value("${bot_username}")
    private  String botUsername;


    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {return botToken;}

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage()) {
            if (update.getMessage().getDocument().getMimeType().startsWith("image")) {
                try {
                    String name = "downloaded";
                    System.out.println("photo msg is " + update.getMessage().getMessageId());
                    List<InputMedia> medias = new ArrayList<>();
                    File file = downloadDocument(update.getMessage().getDocument(), name);
                    ImageChanger imageChanger = new ImageChanger(file);
                    InputMediaPhoto photo = new InputMediaPhoto();
                    photo.setMedia(imageChanger.saveImage(Scalr.Mode.FIT_EXACT), "photo4ka");
                    photo.setCaption("512x512");
                    medias.add(photo);
                    medias.add(photo);
                    sendMediaGroupPhotos(update.getMessage().getChatId(), medias);
                    //sendDocUploadingAFile(update.getMessage().getChatId(),imageChanger.saveImage(Scalr.Mode.FIT_EXACT),"Обработанное фото!\n(512x512)");
                    //sendDocUploadingAFile(update.getMessage().getChatId(),imageChanger.saveImage(Scalr.Mode.AUTOMATIC),"Обработанное фото!");
                    boolean delete = file.delete();
                } catch (TelegramApiException | IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } else if (update.hasCallbackQuery()) {
            String answer = update.getCallbackQuery().getData();
            int messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            if (answer.equals("SAVE1_BUTTON")) {
                System.out.println("save");
                editTextMessage(messageId, chatId, "Вы нажали кнопочку!");
            }
            if (answer.equals("SAVE2_BUTTON")) {
                System.out.println("save2");
                editTextMessage(messageId, chatId, "Вы нажали кнопочку!");

            }
            System.out.println("callback msg " + messageId);
        }

    }

    private void editTextMessage(int messageId, long chatId, String message) throws TelegramApiException {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId);
        editMessageText.setMessageId(messageId);
        editMessageText.setText(message);
        execute(editMessageText);
    }

    private void sendMessage(long chatId, String textToSend) throws TelegramApiException {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(textToSend).build();
        execute(message);
    }

    private File downloadDocument(Document document, String path) throws TelegramApiException {
        GetFile getFile = GetFile.builder().fileId(document.getFileId()).build();
        var filePath = execute(getFile).getFilePath();
        var extension = "." + document.getMimeType().split("/")[1];
        File file = new File(path + extension);
        return downloadFile(filePath, file);
    }


    private void sendMediaGroupPhotos(Long chatId, List<InputMedia> medias) throws TelegramApiException {
        SendMediaGroup sendMediaGroup = SendMediaGroup.builder()
                .chatId(chatId)
                .medias(medias)
                .build();
        SendMessage message = new SendMessage();
        message.setText("Кнопочки");
        message.setChatId(chatId);
        execute(sendMediaGroup);
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton();
        InlineKeyboardButton button2 = new InlineKeyboardButton();
        button.setText("Скачать 1 фоту");
        button.setCallbackData("SAVE1_BUTTON");
        button2.setText("Скачать 2 фоту");
        button2.setCallbackData("SAVE2_BUTTON");
        rowInline.add(button);
        rowInline.add(button2);
        rowsInline.add(rowInline);
        inlineKeyboardMarkup.setKeyboard(rowsInline);
        message.setReplyMarkup(inlineKeyboardMarkup);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }


    private void sendDocUploadingAFile(Long chatId, File save, String caption) throws TelegramApiException {
        SendDocument sendDocumentRequest = new SendDocument();
        sendDocumentRequest.setChatId(chatId);
        sendDocumentRequest.setDocument(new InputFile(save));
        sendDocumentRequest.setCaption(caption);
        execute(sendDocumentRequest);
    }

    /*if (update.getMessage().hasText()) {
            message = update.getMessage().getText();
            switch (message) {
                case "/help": {
                    try {
                        sendMessage(update.getMessage().getChatId(),"I'll help you brother");
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                }
                default: {
                    try {
                        sendMessage(update.getMessage().getChatId(), "I don't understand you!!!");
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        } else {
            try {
                sendMessage(update.getMessage().getChatId(), "Sorry, I can't recognize your message,pal");
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }*/

}
