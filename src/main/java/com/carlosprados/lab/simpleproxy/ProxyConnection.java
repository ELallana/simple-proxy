package com.carlosprados.lab.simpleproxy;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ProxyConnection {

    SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss");

    Socket m_fromClientSocket;
    String m_host;
    int m_port;
    long m_timeout;
    long m_id;

    Socket toServer = null;
    Date timeStart = new Date();
    long time0 = new Date().getTime();
    long time1 = new Date().getTime();
    long bytesFromClient = 0;
    long bytesFromServer = 0;

    protected ConnectionStream toServerStream = null;
    protected ConnectionStream toClientStream = null;
    protected boolean isClosed = true;
    
    protected long delay = 0;
    protected int transferFaultsRate = 0; //in 1/1000

    public long getId() {
        return this.m_id;
    }

    public ProxyConnection(Socket _socket, String _host, int _port, long _timeout, long _id)
            throws UnknownHostException, IOException {
        m_fromClientSocket = _socket;
        m_host = _host;
        m_port = _port;
        m_timeout = _timeout;
        m_id = _id;
        Proxy.display("Open proxy connection: " + m_id);
        try {
            createStreams();
            isClosed = false;
        } catch (IOException e) {
            close();
            throw e;
        }
    }

    // Call this function from ConnectionCollection if you please
    public synchronized void close() {
        if (!isClosed) {
            try {
                isClosed = true;
                Proxy.display("Closing proxy connection: " + this.toString());
                if (toClientStream != null) {
                    toClientStream.close();
                }
                if (toServerStream != null) {
                    toServerStream.close();
                }
                m_fromClientSocket.close();
                toServer.close();
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("id: ").append(m_id);
        sb.append(", Client: ").append(m_fromClientSocket.getInetAddress().getHostAddress()).append(":")
                .append(m_fromClientSocket.getPort());
        sb.append(", Server: ").append(toServer.getInetAddress().getHostAddress()).append(":")
                .append(toServer.getPort());
        sb.append(", Bytes received: from client=").append(this.bytesFromClient).append(", from server: ")
                .append(this.bytesFromServer);
        sb.append(", Connection started at: ").append(dateFormatter.format(timeStart)).append(", last used at: ")
                .append(dateFormatter.format(new Date(time0)));
        return sb.toString();
    }

    public void notifyClosing() {
        if (!isClosed) {
            Proxy.quitConnexion(this.m_id);
        }
    }

    private void createStreams() throws UnknownHostException, IOException {
        toServer = new Socket(m_host, m_port);
        toClientStream = new ConnectionStream(this, toServer, m_fromClientSocket, false);
        toServerStream = new ConnectionStream(this, m_fromClientSocket, toServer, true);
        Proxy.display("open connection to:" + toServer);// + "(timeout=" + m_timeout + " ms)");
        toClientStream.setDaemon(true);
        toServerStream.setDaemon(true);
        toClientStream.start();
        toServerStream.start();
    }
    
    public boolean transferHandicap() throws InterruptedException{
        if (this.transferFaultsRate > 0){
            double randomNumber = (Math.random() * 1000) + 1;
            if (randomNumber< this.transferFaultsRate){
                return false;
            }
        }
        if (this.delay > 0){
            Thread.sleep(this.delay);
        }
        return true;
    }
    
    public void setDelay(long _delay){
        this.delay = _delay;
    }
    
    public void setTransferFaultsRate(int _transferFaultsRate){
        this.transferFaultsRate = _transferFaultsRate;
    }
    
    
}
