package com.carlosprados.lab.simpleproxy;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class ConnectionCollection {

    protected Map<Long, ProxyConnection> connectionList = new HashMap<Long, ProxyConnection>();
    protected String host;
    protected int port;
    protected long timeout;
    protected int maxConnections;
    
    protected long delay = 0;
    protected int transferFaultsRate = 0;

    public ConnectionCollection(String _host, int _port, long _timeout, int _maxConnections) {
        this.host = _host;
        this.port = _port;
        this.timeout = _timeout;
        this.maxConnections = _maxConnections;
    }

    public boolean thereIsSpaceEnough() {
        return this.connectionList.size() < 500;
    }

    public int size() {
        return this.connectionList.size();
    }

    public synchronized void addProxyConnection(Socket socket, long id) throws UnknownHostException, IOException {
        ProxyConnection pc = new ProxyConnection(socket, this.host, this.port, this.timeout, id);
        connectionList.put(id, pc);
        setConnectionHandicaps(pc);
    }

    public synchronized void closeProxyConnection(long id) {
        if (this.connectionList.size() > 0) {
            Proxy.display("Closing proxyConnection: " + id);
            ProxyConnection pc = this.connectionList.get(id);
            pc.close();
            this.connectionList.remove(id);
        }
    }

    public synchronized void closeAll() {
        if (this.connectionList.size() > 0) {
            Proxy.display("Closing " + connectionList.size() + " connections");
            for (ProxyConnection pc : this.connectionList.values()) {
                pc.close();
            }
            this.connectionList.clear();
        }
    }

    public void setHandicaps(long _delay, int _transferFaultsRate) {
        if (this.delay >= 0) {
            this.delay = _delay;
        }
        if (this.transferFaultsRate >= 0) {
            this.transferFaultsRate = _transferFaultsRate;
        }
        for (ProxyConnection pc : this.connectionList.values()) {
            setConnectionHandicaps(pc);
        }
    }
    
    protected void setConnectionHandicaps(ProxyConnection pc){
            pc.setDelay(this.delay);
            pc.setTransferFaultsRate(this.transferFaultsRate);
    }
}
