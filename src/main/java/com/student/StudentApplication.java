
package com.student;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class StudentApplication {

    public static void main(String[] args) throws IOException {

        HttpServer server = HttpServer.create(
            new InetSocketAddress(8080), 0
        );

        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();

            if (exchange.getRequestMethod().equals("GET")
                    && (path.equals("/") || path.equals("/index.html"))) {

                try (InputStream input =
                    StudentApplication.class.getResourceAsStream("/static/index.html")) {

                    if (input == null) {
                        send(exchange, 404, "HTML file not found");
                        return;
                    }

                    byte[] page = input.readAllBytes();
                    exchange.getResponseHeaders().set(
                        "Content-Type", "text/html; charset=UTF-8"
                    );
                    exchange.sendResponseHeaders(200, page.length);
                    exchange.getResponseBody().write(page);
                    exchange.close();
                }

            } else if (exchange.getRequestMethod().equals("POST")
                    && path.equals("/register")) {

                String body = new String(
                    exchange.getRequestBody().readAllBytes(),
                    StandardCharsets.UTF_8
                );

                String name = getValue(body, "name");
                String roll = getValue(body, "roll");
                String email = getValue(body, "email");
                String course = getValue(body, "course");

                String response = """
                    <!DOCTYPE html>
                    <html>
                    <head><title>Registration Successful</title></head>
                    <body style="font-family:Arial;text-align:center;padding:50px">
                    <h1>Registration Successful!</h1>
                    <p>Student Name: %s</p>
                    <p>Roll Number: %s</p>
                    <p>Email: %s</p>
                    <p>Course: %s</p>
                    <a href="/">Go Back</a>
                    </body>
                    </html>
                    """.formatted(
                        escape(name), escape(roll),
                        escape(email), escape(course)
                    );

                send(exchange, 200, response);

            } else {
                send(exchange, 404, "Page Not Found");
            }
        });

        server.start();
        System.out.println("Student Registration App running at http://localhost:8080");
    }

    private static String getValue(String body, String key) {
        for (String pair : body.split("&")) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2 && parts[0].equals(key)) {
                return URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
            }
        }
        return "";
    }

    private static String escape(String value) {
        return value.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }

    private static void send(HttpExchange exchange, int status, String response)
            throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set(
            "Content-Type", "text/html; charset=UTF-8"
        );
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }
}
