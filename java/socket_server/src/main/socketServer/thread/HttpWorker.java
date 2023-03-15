package main.socketServer.thread;

import main.socketServer.Utils.HtmlPageBuilder;
import main.socketServer.error.BadRequest;
import main.socketServer.error.ForbiddenRequest;

import java.io.*;
import java.net.Socket;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;

public class HttpWorker implements Runnable {
    private final Socket clientSocket;

    public HttpWorker(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        PrintStream out = null;
        BufferedReader in = null;

        try {
            out = new PrintStream(clientSocket.getOutputStream());
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

//                String line;
//                while(true) {
//                    line = in.readLine();
//                    System.out.println("line = " + line);
//                    if (line.equals("")) {
//                        break;
//                    }
//                }

            String line = in.readLine();

            if (Objects.isNull(line)) {
                return;
            }

            this.validate(line);
            String request = line.substring(4, line.length() - 9).trim();

            request = URLDecoder.decode(request, "UTF-8");

            if (request.endsWith("/")) {
                request = request.substring(0, request.length() - 1);
            }


            if (request.indexOf(".") > -1) {
                // request dot이 있다면 file
                this.handleFileRequest(request, out);


            } else {
                // 일반 html 파일 request
            }

//            String page = HtmlPageBuilder.buildErrorPage("200", "bad request", "bad request page not exist");
//            out.println(page);

//            while (true) {
//                line = in.readLine();
//                if (line.equals("")) {
//                    break;
//                }
//            }

        } catch (BadRequest e) {
            String page = HtmlPageBuilder.buildErrorPage("400", "bad request", "bad request page not exist");
            out.println(page);
        } catch (ForbiddenRequest e) {
            String page = HtmlPageBuilder.buildErrorPage("403", "forbidden request", "wrong request");
            out.println(page);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                    clientSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void validate(String httpRequest) {
        this.validateHttpRequest(httpRequest);
        this.validateIsForbidden(httpRequest);
    }

    private void validateHttpRequest(String httpRequest) {
        if (this.validateHttpMethod(httpRequest) || httpRequest.length() < 14 ||
                !(httpRequest.endsWith("HTTP/1.0") || httpRequest.endsWith("HTTP/1.1"))) {
            //bac request
            throw new BadRequest("Bad request: is not http request");
        }
    }

    private void validateIsForbidden(String httpRequest) {
        String req = httpRequest.substring(4, httpRequest.length() - 9).trim();
        if (req.indexOf("..") > -1 || req.indexOf("/.ht") > -1 || req.endsWith("~")) {

            throw new ForbiddenRequest("is Forbidden request");
        }
    }

    private Boolean validateHttpMethod(String request) {
        return !(request.startsWith("GET") ||
                request.startsWith("POST") ||
                request.startsWith("PUT"));

    }

    private void handleFileRequest(String request, PrintStream printer) {
        String projectRootDir = this.getRootFolder();

        Path filePath = Paths.get(projectRootDir, "/src/resources/images", request);

        File file = new File(filePath.toString());

        if (!file.exists() || !file.isFile()) {
            printer.println("No such resource" + request);
        } else {
            String htmlHeader = buildHttpHeader(filePath.toString(), file.length());
            printer.println(htmlHeader);

            //open file to input stream
            InputStream fs = null;
            try {
                fs = new FileInputStream(file);
                byte[] buffer = new byte[1000];
                while (fs.available()>0) {
                    printer.write(buffer, 0, fs.read(buffer));
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fs != null) {
                    try {
                        fs.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }

    private String getRootFolder() {
        String root = "";

        try {
            //System.getProperty("user.dir");
            //new File(" "); => socket_server/
            File f = new File(".");
            root = f.getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return root;
    }

    private String buildHttpHeader(String path, long length) {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 200 ok");
        sb.append(System.lineSeparator());
        sb.append("Content-Length: ").append(length);
        sb.append(System.lineSeparator());
        sb.append("Content-Type: " ).append(getContentType(path));
        sb.append(System.lineSeparator());

        return sb.toString();
    }

    private static String getContentType(String path) {
        if (path == null || path.equals("") || path.lastIndexOf(".") < 0) {
            return "text/html";
        }

        String mimeType = URLConnection.guessContentTypeFromName(path);
//                String s1 = Files.probeContentType(filePath);
//                MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
//                String contentType = mimeTypesMap.getContentType(filePath.toString());

        return mimeType;
    }
}
