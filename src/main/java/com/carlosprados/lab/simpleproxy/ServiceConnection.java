package com.carlosprados.lab.simpleproxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ServiceConnection extends Thread {

    protected ServerSocket server = null;
    protected ConnectionCollection connectionCollection;
    protected boolean ignoreConnections = false;
    protected static int id = 10000;

    public ServiceConnection(String _localHost, int _localPort, ConnectionCollection _connectionCollection) throws IOException {
        id++;
        Proxy.display("Creating service connection (" + id + ") on: " + _localPort);
        this.connectionCollection = _connectionCollection;
        InetSocketAddress isa = new InetSocketAddress(_localHost, _localPort);
        server = new ServerSocket(_localPort, 200, isa.getAddress());        
    }

    public boolean isRunning() {
        if ((server == null) || server.isClosed()) {
            return false;
        } else {
            return true;
        }
    }

    public void setIgnoreConnections(boolean _ignore) {
        ignoreConnections = _ignore;
    }

    @Override
    public void run() {
        try {                    
            long i = 0;
            while (!server.isClosed()) {
                Socket socket = null;
                Proxy.display("listening...");
                socket = server.accept();
                if (socket != null) {
                    if (!ignoreConnections) {
                        try {
                            if (connectionCollection.thereIsSpaceEnough()) {
                                connectionCollection.addProxyConnection(socket, ++i);
                                Proxy.display("accepted as #" + connectionCollection.size() + ":" + socket);
                            } else {
                                Proxy.display("Too many connections. Abort last one");
                                socket.close();
                            }
                        } catch (Exception e) {
                            Proxy.display("Exception launching a new connection: " + e.getMessage());
                            e.printStackTrace(System.err);
                        }
                    } else {
                        Proxy.display("Connection ignored");
                    }
                }
            }// while
        } catch (Throwable t) {
            Proxy.display("Exception in server socket: " + t.getMessage());
            t.printStackTrace(System.err);
        }
        Proxy.display("Bye from server socket thread");
    }

    public synchronized void close() {
        if ((server != null) && !server.isClosed()) {
            try {
                Proxy.display("Closing service connection (" + id + ")");
                server.close();
            } catch (IOException e) {
                Proxy.display("Some problems closing server socket");
            }
        }
    }
}
