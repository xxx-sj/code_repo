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
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskWorkerThreadWithGPT extends Thread {

    private final Queue<Socket> requestQueue;
    private AtomicInteger count = new AtomicInteger(0);

    public TaskWorkerThreadWithGPT(Queue<Socket> requestQueue) {
        this.setName("task-worker-thread");
        this.setDaemon(true);
        this.requestQueue = requestQueue;
    }

    @Override
    public void run() {
        PrintStream out = null;
        BufferedReader in = null;
        Socket client = null;

        while(true) {
            try {
                synchronized (requestQueue) {
                    while(requestQueue.isEmpty()) {
                        try {
                            requestQueue.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    client = requestQueue.poll();
                }

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

                count.incrementAndGet();
                System.out.println("finish count = " + count);

                if (request.indexOf(".html") > -1) {
                    this.handleHtmlRequest(request, out);
                    continue;
                }

                if (request.indexOf(".") > -1) {
                    this.handleFileRequest(request, out);
                    continue;
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
            } catch (IOException e) {
                e.printStackTrace();
            } finally {

                try {
                    if (out != null) {
                        out.close();
                    }
                    if (in != null) {
                        in.close();
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
//        String projectRootDir = this.getRootFolder();
        if(request.equals("favicon.ico")) {
            return;
        }
//        Path filePath = Paths.get(projectRootDir, "/src/resources/templates", request);

        String filepath = "/resources/templates" + request;
//        System.out.println("filepath = " + filepath);
        InputStream in = getClass().getResourceAsStream(filepath);
//        System.out.println("file is null" + (in == null));
        String htmlHeader = buildHttpHeader(filepath, 235);
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
        if(request.equals("favicon.ico")) {
            return;
        }

//        String projectRootDir = this.getRootFolder();

//        Path filePath = Paths.get(projectRootDir, "/src/resources/images", request);

//        File file = this.hasFile(filePath.toString());

        String filepath = "/resources/templates" + request;

        InputStream in = getClass().getResourceAsStream(filepath);
//        System.out.println("filepath = " + filepath);

        String htmlHeader = buildHttpHeader(filepath, 235);
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
//        ClassLoader classLoader = getClass().getClassLoader();
//        File file = new File(classLoader.getResource("file/test.xml").getFile());
//        ClassPathResource classPathResource = new ClassPathResource("config/" + path);
//        ClassLoader나 ClassPathResource클래스를 통해 원하는 리소스를 찾을 수 있다.

        System.out.println(filePath);
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