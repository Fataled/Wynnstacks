package net.fataled.wynnstacks.client;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.concurrent.CompletableFuture;

import java.time.Instant;

public class ChatReader {
    private static final Logger LOGGER = LogManager.getLogger("ShadestepperHUD");

    public static CompletableFuture<String> detectGameChat() {
        CompletableFuture<String> future = new CompletableFuture<>();
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String Cleaned1 = stripColors(message.getString());
             Cleaned1 = removeUnrenderableChars(Cleaned1);

            if(!future.isDone()){
                future.complete(Cleaned1);
            }
        });

        return future;
    }

    public static CompletableFuture<String> detectChat(){
        CompletableFuture<String> future = new CompletableFuture<>();
        ClientReceiveMessageEvents.CHAT.register((Text chat, SignedMessage signedmessage, GameProfile sender, MessageType.Parameters params, Instant receptionTimeStamp) -> {
            String message = stripColors(chat.toString().toLowerCase());
            message = removeUnrenderableChars(message);

            if(!future.isDone()){
                future.complete(message);
            }
        });
        return future;
    }

    public static String stripColors(String input) {
        return input.replaceAll("§[0-9a-fk-or]", "");
    }

    public static String removeUnrenderableChars(String input) {
        return input.codePoints().filter((cp) -> cp >= 32 && cp <= 126 || Character.isWhitespace(cp)).collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
    }
}
