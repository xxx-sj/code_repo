package main.socketServer.thread;

import main.socketServer.Utils.HtmlPageBuilder;
import main.socketServer.error.BadRequest;
import main.socketServer.error.ForbiddenRequest;
import main.socketServer.server.SocketServer;

import java.io.*;
import java.net.Socket;
import java.net.SocketOption;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskWorkerThread extends Thread {

    private final LinkedBlockingQueue requestQueue;

    private AtomicInteger count = new AtomicInteger(0);
    private final Object lock;

    public TaskWorkerThread(LinkedBlockingQueue requestQueue) {
        this.setName("task-worker-thread");
        this.setDaemon(true);
        this.requestQueue = requestQueue;
        this.lock = new Object();
    }


    @Override
    public void run() {
        PrintStream out = null;
        BufferedReader in = null;
        Socket client = null;

        while(true) {
            try {

                client = (Socket) requestQueue.take();
                count.incrementAndGet();

                out = new PrintStream(client.getOutputStream());
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                String line = in.readLine();
//                System.out.println("line first = " + line);

                if (Objects.isNull(line)) {
                    continue;
                }



                this.validate(line);

                String request = line.substring(4, line.length() - 9).trim();

                request = URLDecoder.decode(request, "UTF-8");

                if (request.endsWith("/")) {
                    request = request.substring(0, request.length() - 1);
                }

                if (Objects.equals(request, "") || Objects.equals(request, "/index")) {
                    request = "/index.html";
                }

//                System.out.println("take = " + count);

                if (request.indexOf(".html") > -1) {
                    this.handleHtmlRequest(request, out);
                    continue;
                }

                if (request.indexOf(".") > -1) {
                    this.handleFileRequest(request, out);
                    continue;
                }


//                String page = HtmlPageBuilder.buildErrorPage("404", "not found", "bad request page not exist");
//                out.println(page);

            } catch (BadRequest e) {
                String page = HtmlPageBuilder.buildErrorPage("400", "bad request", e.getMessage());
                out.println(page);
            } catch (ForbiddenRequest e) {
                String page = HtmlPageBuilder.buildErrorPage("403", "forbidden request", "wrong request");
                out.println(page);
            } catch (FileNotFoundException e) {
                String page = HtmlPageBuilder.buildErrorPage("404", "not found", "wrong request");
                out.println(page);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                    if (in != null) {
                        in.close();
                    }
                    if(!client.isClosed()) {
                        client.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
        String projectRootDir = this.getRootFolder();

        Path filePath = Paths.get(projectRootDir, "/src/resources/templates", request);

        File file = this.hasFile(filePath.toString());

        String htmlHeader = buildHttpHeader(filePath.toString(), file.length());
        printer.println(htmlHeader);

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

    private void handleFileRequest(String request, PrintStream printer) throws FileNotFoundException {
        String projectRootDir = this.getRootFolder();

        Path filePath = Paths.get(projectRootDir, "/src/resources/images", request);

        File file = this.hasFile(filePath.toString());

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

    private File hasFile(String filePath) throws FileNotFoundException {

        if (filePath == null) {
            throw new FileNotFoundException("no such file");
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
//                MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
//                String contentType = mimeTypesMap.getContentType(filePath.toString());

//                String s1 = Files.probeContentType(filePath);
        return mimeType;
    }
}
