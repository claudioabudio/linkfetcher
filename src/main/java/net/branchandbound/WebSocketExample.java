package net.branchandbound;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;

public class WebSocketExample {
    public static void main(String... args) throws InterruptedException {
        int msgCount = 6;
        CountDownLatch countDownLatch = new CountDownLatch(msgCount);

        CompletableFuture<WebSocket> webSocketCompletableFuture =
                HttpClient.newHttpClient().newWebSocketBuilder()
                        .connectTimeout(Duration.ofSeconds(2))
                        .buildAsync(URI.create("ws://echo.websocket.events"), new EchoListener(countDownLatch));

        webSocketCompletableFuture.thenAccept(webSocket -> {
                webSocket.request(msgCount);
                for (int i=0 ; i < msgCount; i++)
                    webSocket.sendText("Message: "+i, true);
        });

        countDownLatch.await();
    }

    static class EchoListener implements WebSocket.Listener {


        CountDownLatch countDownLatch;

        EchoListener(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void onOpen(WebSocket webSocket) {
            System.out.println("WebSocket opened !");
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            System.out.println("onText received !: "+data);
            countDownLatch.countDown();
            return null;
        }
    }

    ;
}
