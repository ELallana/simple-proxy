package com.carlosprados.lab.simpleproxy;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class ProxyManager {
    public enum ConnectionStatus {
        QUIT, DISCONNECT, CONNECT
    }

    public enum ServiceBehaviour {
        PROPER, DIREGARDER, REBEL
    }

    private String fileName;
    protected String localAddress;
    protected int localport;
    protected ConnectionCollection connectionCollection;
    protected ConnectionStatus status = ConnectionStatus.DISCONNECT;
    protected ServiceBehaviour serviceBehaviour = ServiceBehaviour.PROPER;
    protected ServiceConnection serviceConnection;
    protected Thread thisThread;
    protected boolean resetSchedule = false;

    // protected FileInputStream fstream;
    // protected DataInputStream in;
    // protected BufferedReader br;

    public ProxyManager(String _fileName, ConnectionCollection _connectionCollection, String _localAddress,
            int _localPort) {
        this.fileName = _fileName;
        this.connectionCollection = _connectionCollection;
        this.localAddress = _localAddress;
        this.localport = _localPort;
    }

    public void work() {
        BufferedReader br = null;
        try {
            thisThread = Thread.currentThread();
            br = createBufferedReaderFromFile();

            Proxy.display("Reading schedule file");
            String line = null;
            // Read File Line By Line
            while ((status != ConnectionStatus.QUIT) && (line = br.readLine()) != null) {
                if (!line.isEmpty() && !line.startsWith("#")) {
                    Proxy.display("new csv line: " + line);
                    // retrieve csv line data
                    String[] data = line.split(";");
                    long time = Long.parseLong(data[0]);
                    String connectionState = data[1].trim().toLowerCase();
                    String serverBehaviourStr = null;
                    long delay = -1;
                    int transferFaultsRate = -1;
                    if (data.length > 2) {
                        serverBehaviourStr = data[2].trim().toLowerCase();
                        if (data.length > 3) {
                            delay = Long.parseLong(data[3]);
                            if (data.length > 4) {
                                transferFaultsRate = Integer.parseInt(data[4]);
                            }
                        }
                    }

                    // interpret data
                    if ((connectionState.equals("c")) && (this.status == ConnectionStatus.DISCONNECT)) {
                        createServiceConnection();
                        this.status = ConnectionStatus.CONNECT;
                    } else if ((connectionState.equals("d")) && (this.status == ConnectionStatus.CONNECT)) {
                        serviceConnection.close();
                        connectionCollection.closeAll();
                        this.status = ConnectionStatus.DISCONNECT;
                    }

                    if ((this.status == ConnectionStatus.CONNECT) && (serverBehaviourStr != null)) {
                        if (serverBehaviourStr.equals("p")) {
                            this.serviceBehaviour = ServiceBehaviour.PROPER;
                            if (serviceConnection.isRunning()) {
                                serviceConnection.setIgnoreConnections(false);
                            } else {
                                createServiceConnection();
                            }
                        } else if (serverBehaviourStr.equals("d")) {
                            this.serviceBehaviour = ServiceBehaviour.DIREGARDER;
                            if (!serviceConnection.isRunning()) {
                                createServiceConnection();
                            }
                            serviceConnection.setIgnoreConnections(true);
                        } else if (serverBehaviourStr.equals("r")) {
                            this.serviceBehaviour = ServiceBehaviour.REBEL;
                            if (serviceConnection.isRunning()) {
                                serviceConnection.close();
                            }
                        }
                    }// serverBehaviourStr != null
                    connectionCollection.setHandicaps(delay, transferFaultsRate);

                    if (status != ConnectionStatus.QUIT) {
                        try {
                            Proxy.display("Scheduler sleeping for: " + time);
                            Thread.sleep(time * 1000);
                        } catch (InterruptedException e) {
                            Proxy.display("Manager thread interrupted");
                        }
                    }
                }
                if (resetSchedule == true) {
                    br.close();
                    resetSchedule = false;
                    br = createBufferedReaderFromFile();
                }
            }// while
             // } catch (FileNotFoundException e) {
             // } catch (NumberFormatException e) {
             // } catch (IOException e) {
        } catch (Throwable e) {
            Proxy.display("Exception in manager bound: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                }
            }
        }

        if (status != ConnectionStatus.QUIT) {
            // Close all;
            serviceConnection.close();
            connectionCollection.closeAll();
            this.status = ConnectionStatus.QUIT;
        }
        Proxy.display("Bye manager bound");
    }

    public synchronized void quit() {
        Proxy.display("Quit manager");
        this.status = ConnectionStatus.QUIT;
        serviceConnection.close();
        connectionCollection.closeAll();
        thisThread.interrupt();
    }

    public synchronized void resetSchedule() {
        Proxy.display("Reset manager");
        resetSchedule = true;
        thisThread.interrupt();
    }

    private void createServiceConnection() throws IOException {
        if (serviceConnection != null && serviceConnection.isRunning()) {
            serviceConnection.close();
        }
        serviceConnection = new ServiceConnection(localAddress, localport, connectionCollection);
        serviceConnection.setDaemon(true);
        serviceConnection.start();
    }

    private BufferedReader createBufferedReaderFromFile() throws FileNotFoundException {
        FileInputStream fstream = new FileInputStream(fileName);
        DataInputStream in = new DataInputStream(fstream);
        return new BufferedReader(new InputStreamReader(in));
    }

}
