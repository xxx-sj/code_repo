package main.socketServer.thread;

import main.socketServer.Utils.HtmlPageBuilder;
import main.socketServer.error.BadRequest;
import main.socketServer.error.ForbiddenRequest;

import java.io.*;
import java.net.Socket;
import java.net.URLConnection;
import java.net.URLDecoder;
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

            String line = in.readLine();
            System.out.println("line first = " + line);

            if (Objects.isNull(line)) {
                return;
            }
//            do {
//                test = in.readLine();
//                System.out.println("test = " + test);
//            }while(test != null && !test.equals(""));


//            while (true) {
//                test = in.readLine();
//
//                if (!test.equals("")) {
//                    System.out.println("line = " + test);
//                }
//                count++;
//
//                if (count > 1000) break;
//            }

            //https://stackoverflow.com/questions/30901173/handling-post-request-via-socket-in-java
            //https://okky.kr/questions/420777

            this.validate(line);
            String request = line.substring(4, line.length() - 9).trim();

            request = URLDecoder.decode(request, "UTF-8");

            if (request.endsWith("/")) {
                request = request.substring(0, request.length() - 1);
            }

            if (Objects.equals(request, "") || Objects.equals(request, "/index")) {
                request = "/index.html";
            }

            if (request.indexOf(".html") > -1) {
                this.handleHtmlRequest(request, out);
                return;
            }

            if (request.indexOf(".") > -1) {
                this.handleFileRequest(request, out);
                return;
            }


            String page = HtmlPageBuilder.buildErrorPage("404", "not found", "bad request page not exist");
            out.println(page);


        } catch (BadRequest e) {
            String page = HtmlPageBuilder.buildErrorPage("400", "bad request", e.getMessage());
            out.println(page);
        } catch (ForbiddenRequest e) {
            String page = HtmlPageBuilder.buildErrorPage("403", "forbidden request", "wrong request");
            out.println(page);
        } catch (FileNotFoundException e) {
            String page = HtmlPageBuilder.buildErrorPage("404", "not found", "wrong request");
            out.println(page);
        }
        catch (IOException e) {
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

    private void handleHtmlRequest(String request, PrintStream printer) throws FileNotFoundException {
//        String projectRootDir = this.getRootFolder();

//        Path filePath = Paths.get(projectRootDir, "/src/resources/templates", request);

        String filepath = "/resources/templates" + request;
        System.out.println("filepath = " + filepath);
        InputStream in = getClass().getResourceAsStream(filepath);
        System.out.println("file is null" + (in == null));
        String htmlHeader = buildHttpHeader(filepath, 0);
        printer.println(htmlHeader);

        try {
            if (in == null) {
                throw new FileNotFoundException();
            }
            byte[] buffer = new byte[1000];
            while (in.available()>0) {
                printer.write(buffer, 0, in.read(buffer));
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void handleFileRequest(String request, PrintStream printer) throws FileNotFoundException {
//        String projectRootDir = this.getRootFolder();

//        Path filePath = Paths.get(projectRootDir, "/src/resources/images", request);

//        File file = this.hasFile(filePath.toString());

        String filepath = "/resources/templates" + request;

        InputStream in = getClass().getResourceAsStream(filepath);
        System.out.println("filepath = " + filepath);

        String htmlHeader = buildHttpHeader(filepath, 0);
        printer.println(htmlHeader);

        //open file to input stream
//        InputStream fs = null;
        try {
            if (in == null) {
                throw new FileNotFoundException();
            }
            byte[] buffer = new byte[1000];
            while (in.available()>0) {
                printer.write(buffer, 0, in.read(buffer));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private File hasFile(String filePath) throws FileNotFoundException {

        if (filePath == null) {
            throw new FileNotFoundException("no such file");
        }

        InputStream is = getClass().getResourceAsStream("file-name");

        if (is == null) {
            throw new FileNotFoundException("no such" + filePath);
        }

        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            throw new FileNotFoundException("no such " + file.getName());
        }

        return file;
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
