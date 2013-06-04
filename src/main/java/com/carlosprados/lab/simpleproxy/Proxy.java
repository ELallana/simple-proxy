package com.carlosprados.lab.simpleproxy;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Proxy {

    public static final String usageArgs = " <localaddress> <localport> <host> <port> <timeout_ms>";

    static Proxy proxy;
    static ConnectionCollection connectionCollection;
    static ProxyManager proxyManager;
    static SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss,SSS");
    
    static String localHost;
    static int localport;
    static String remoteHost;
    static int remotePort;
    static int timeout = 30000;

    public static synchronized void display(String s) {
        StringBuilder sb = new StringBuilder("[");
        sb.append(dateFormatter.format(new Date())).append("]").append(s);
        System.out.println(sb.toString());
    }

    public static void main(String[] _argv) {
        proxy = new Proxy();

        if (_argv.length >= 4) {
            localHost = _argv[0];
            localport = Integer.parseInt(_argv[1]);
            remoteHost = _argv[2];
            remotePort = Integer.parseInt(_argv[3]);
            try {
                timeout = Integer.parseInt(_argv[4]);
            } catch (Exception e) {
            }

            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    Proxy.display("Quitting application");
                    if (proxyManager != null) {
                        proxyManager.quit();
                    }
                }
            });
            Thread mainTh = new Thread() {
                @Override
                public void run() {
                    connectionCollection = new ConnectionCollection(remoteHost, remotePort, timeout, 200);
                    proxyManager = new ProxyManager("proxySchedule.csv", connectionCollection, localHost, localport);
                    proxyManager.work();
                }
            };
            mainTh.start();
            consoleOptions();

        } else {
            System.err.println("usage: java " + proxy.getClass().getName() + usageArgs);
        }

        Proxy.display("App exited");
    }

    public static synchronized void print(int _integer) {
        System.out.print((char) _integer);
    }

    public static synchronized void println(String _string) {
        System.out.println(_string);
    }

    public static void quitConnexion(long id) {
        Proxy.display("quitting connexion " + id);
        connectionCollection.closeProxyConnection(id);
    }

    public static void consoleOptions() {
        // >>EXPERIMENTAL
        boolean running = true;
        try {
            while (running) {
                System.out.println("press 'r' to reset, 'q' to exit");
                int key;
                key = System.in.read();
                if (key == 'r') {
                    proxyManager.resetSchedule();
                } else if (key == 'q') {
                    proxyManager.quit();
                    running = false;
                }
            }
        } catch (IOException e) {
            System.err.println("Error in consoleOptions: " + e.getMessage());
        }
        // <<EXPERIMENTAL
    }
}// class

