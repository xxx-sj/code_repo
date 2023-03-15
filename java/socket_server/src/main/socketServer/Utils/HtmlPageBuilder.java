package main.socketServer.Utils;

import java.time.LocalDateTime;

public class HtmlPageBuilder {

    public static String buildErrorPage(String code, String title, String msg) {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 ").append(code).append(" ").append(title);
        sb.append(System.lineSeparator());
        sb.append(System.lineSeparator());
        sb.append("<!DOCTYPE html>");
        sb.append("<html>");
        sb.append("<head>");
        sb.append("<title>").append(code).append(" ").append(title).append("</title>");
        sb.append("</head>");
        sb.append("<body>");
        sb.append("<h1>").append(code).append("").append(title).append("</h1>");
        sb.append("<p>").append(msg).append("</p>");
        sb.append("<p>").append(LocalDateTime.now()).append("</p>");
        sb.append("<hr>");
        sb.append("<p> this page is returned by web server </p>");
        sb.append("</body>");
        sb.append("</html>");

        return sb.toString();
    }
}
