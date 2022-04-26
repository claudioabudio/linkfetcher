package net.branchandbound;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;

public class LinkValidatorSync {

    private static HttpClient httpClient;

    public static void main(String... args) throws IOException {
        httpClient = HttpClient.newHttpClient();
        Files.lines(Path.of("src/main/resources/urls.txt"))
                .map(LinkValidatorSync::validateLink)
                .forEach(System.out::println);

    }

    private static String validateLink(String link) {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(link))
                .GET()
                .build();
        HttpResponse.BodyHandler<Void> bodyHandler = HttpResponse.BodyHandlers.discarding();
        HttpResponse<Void> httpResponse = null;
        try {
            httpResponse = httpClient.send(httpRequest, bodyHandler);
        } catch (IOException | InterruptedException e ) {
            return String.format("(%s) -> (%s)", link, false);
        }
        return responseToString(httpResponse);
    }

    private static String responseToString(HttpResponse<Void> response) {
        int status = response.statusCode();
        boolean isValid = status >= 200 && status <= 299;
        return String.format("(%s) -> (%s) (status %s)", response.uri(), isValid, response.statusCode());
    }

}
