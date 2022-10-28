package org.example;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Processor of HTTP request.
 */
public class Processor {
    private final Socket socket;
    private final HttpRequest request;

    public Processor(Socket socket, HttpRequest request) {
        this.socket = socket;
        this.request = request;
    }
    int toInt(String num){
        int result = 0;
        for(int i = 0; i < num.length(); ++i){
            result *= 10;
            result += (int)(num.charAt(i) - 48);
        }
        return result;
    }

    public void process(String[] args) throws IOException {
        int numOfThreads = (args.length > 1 ? Integer.parseInt(args[1]) : 4);
        int numOfItems = (args.length > 2 ? Integer.parseInt(args[2]) : 100);

        ThreadSafeQueue<String> queue = new ThreadSafeQueue<>();

        // Starting consumer threads.
        for (int i = 0; i < numOfThreads; i++) {
            Consumer<String> cons = new Consumer<>(i, queue);
            cons.start();
        }

        // Adding items in the queue for consumers.
        for (int i = 0; i < numOfItems; i++) {
            queue.add("item " + i);
        }

        // Stopping consumers by sending them null values.
        for (int i = 0; i < numOfThreads; i++) {
            queue.add(null);
        }
        System.out.println("Got request:");
        System.out.println(request.toString());
        System.out.flush();

        String s = request.toString();
        String response = "";
        if(s.contains("create")){
            String filename = "";
            for(int i = 12; s.charAt(i) != ' '; ++i){
                filename += s.charAt(i);
            }
            File myFile = new File(filename);
            if(myFile.createNewFile()){
                response += "File successfully created!<br>:)";
            }
            else{
                response += "File not created!<br>:(";
            }
        }
        else if(s.contains("delete")){
            String filename = "";
            for(int i = 12; s.charAt(i) != ' '; ++i){
                filename += s.charAt(i);
            }
            File myFile = new File(filename);
            if(myFile.delete()){
                response += "File successfully deleted!<br>:)";
            }
            else{
                response += "File not deleted!<br>:(";
            }
        }
        else if(s.contains("divider_list")){
            String num = "";
            for(int i = 18; s.charAt(i) != ' '; ++i){
                num += s.charAt(i);
            }
            response += "Dividers of a number " + num + " are: ";
            int value = toInt(num);
            for(int i = 1; i <= value; ++i){
                if(value % i == 0){
                    response += " " + i;
                }
            }
        }

        PrintWriter output = new PrintWriter(socket.getOutputStream());

        // We are returning a simple web page now.
        output.println("HTTP/1.1 200 OK");
        output.println("Content-Type: text/html; charset=utf-8");
        output.println();
        output.println("<html>");
        output.println("<head><title>Hello</title></head>");
        output.println("<body><p>" + response + "</p></body>");
        output.println("</html>");
        output.flush();
        socket.close();
    }
}
