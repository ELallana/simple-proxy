package com.carlosprados.lab.simpleproxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ConnectionStream extends Thread {

    protected ProxyConnection proxyConnection;

    protected InputStream inStream = null;
    protected OutputStream outStream = null;
    protected boolean toServerStream = false;
    protected boolean isClosed = false;

    public boolean isToserverStream() {
        return toServerStream;
    }

    public ConnectionStream(ProxyConnection _proxyConnection, Socket _readingSocket, Socket _writtinSocket,
            boolean _toServerStream) throws IOException {
        this.proxyConnection = _proxyConnection;
        this.inStream = _readingSocket.getInputStream();
        this.outStream = _writtinSocket.getOutputStream();
        this.toServerStream = _toServerStream;
        this.isClosed = false;
    }

    @Override
    public void run() {
        Proxy.display("Running " + idStreamVerbose());
        int numberOfBytes = -1;
        int bufferCapacity = 100;
        byte[] buffer = new byte[bufferCapacity];
        try {
            while (!isClosed) {
                numberOfBytes = inStream.read(buffer);
                if (numberOfBytes != -1) {
                    if (proxyConnection.transferHandicap()){
                        outStream.write(buffer, 0, numberOfBytes);
                        showTrafic(numberOfBytes);
                        if ((numberOfBytes == bufferCapacity) && (bufferCapacity < 2048)){
                            bufferCapacity = bufferCapacity + 100;
                            buffer = new byte[bufferCapacity];
                        } 
                    }else{
                        showTrafic(0);
                    }
                } else {
                    if (!isClosed) {
                        Proxy.display("stream closed in " + idStreamVerbose());
                        proxyConnection.notifyClosing();
                        break;
                    }
                }
                outStream.flush();
            }// while
        } catch (IOException e) {
            if (!isClosed) {
                Proxy.display("Error transfering bytes in " + idStreamVerbose());
                proxyConnection.notifyClosing();
            }
        } catch (InterruptedException e) {
            Proxy.display("Thread interrupetd " + idStreamVerbose());
        }
        Proxy.display("bye from " + idStreamVerbose());
    }

    public synchronized void close() {
        if (!isClosed) {
            isClosed = true;
            Proxy.display("Closing " + idStreamVerbose());
            try {
                inStream.close();
            } catch (IOException e) {
                Proxy.display("Exception closing instream in " + idStreamVerbose());
            }
            try {
                outStream.close();
            } catch (IOException e) {
                Proxy.display("Exception closing outstream in " + idStreamVerbose());
            }
        }
    }

    public String idStreamVerbose() {
        if (toServerStream) {
            return "serverSream of connexion " + proxyConnection.getId();
        } else {
            return "clientSream of connexion " + proxyConnection.getId();
        }
    }

    private void showTrafic(int numberOfBytes) {
        if (toServerStream) {
            Proxy.display("[" + proxyConnection.getId() + "]" + ">> " + numberOfBytes);
        } else {
            Proxy.display("[" + proxyConnection.getId() + "]" + "<< " + numberOfBytes);
        }
    }
}
