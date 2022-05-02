package net.branchandbound;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class LinkValidatorAsync {

    private static HttpClient httpClient;

    public static void main(String... args) throws IOException {
        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        var futures = Files.lines(Path.of("src/main/resources/urls.txt"))
                .map(LinkValidatorAsync::validateLink)
                .collect(Collectors.toList());
        futures.stream()
                .map(CompletableFuture::join)
                .forEach(System.out::println);
    }

    private static CompletableFuture<String> validateLink(String link) {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(link))
                .GET()
                .timeout(Duration.ofSeconds(2))
                .build();
        HttpResponse.BodyHandler<Void> bodyHandler = HttpResponse.BodyHandlers.discarding();
        CompletableFuture<HttpResponse<Void>> httpResponse = null;
        httpResponse = httpClient.sendAsync(httpRequest, bodyHandler);
        return httpResponse.thenApply(LinkValidatorAsync::responseToString).exceptionally(e -> String.format("(%s) -> (%s)", link, false));
    }

    private static String responseToString(HttpResponse<Void> response) {
        int status = response.statusCode();
        boolean isValid = status >= 200 && status <= 299;
        return String.format("(%s) -> (%s) (status %s)", response.uri(), isValid, response.statusCode());
    }

}
