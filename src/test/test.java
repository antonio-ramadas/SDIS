/*package test;

import java.io.*;

public class Test{
    String menssage;
    String address;
    int port;

    public static void main(String[] args) throws IOException {
        boolean done = true;

        if (!validateArgs(args)) {
            return;
        }
        while(!done) {
            Thread t1 = new Thread(new MyPrintThread(mensage));
            t1.start();
            //Thread t2 = new Thread(new MyWriteThread(addres, port, mensage));
            //t2.start();
        }

    }

    private class MyPrintThread implements Runnable {

        String menssage;

        public MyPrintThread(String menssage1) {
            menssage = menssage1;
        }

        public void run() {
            System.out.println(menssage);
        }
    }

    private class MyWriteThread implements Runnable {

        String menssage;

        public MyWriteThread(String menssage1) {
            menssage = menssage1;
        }

        public void run() {
            // Enviar thread
        }
    }

    private static boolean validateArgs(String[] args) {
        if (args.length != 3){
            System.out.println("Argument parsing error!");
            return false;
        }
        else{
            menssage = args[0];
            address = args[1];
            port = Integer.parseInt(args[2]);
        }
        return true;
    }
}*/