package com.iclr.storage;

import gnu.io.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

/**
 * Created by Edward on 20/03/2019.
 */
public class COMPortSerial {
    private String comIdentifier;
    private int baudRate = 57600;
    private String appName;
    private volatile SerialPort openPort = null;
    private volatile OutputStream outputStream = null;
    private volatile InputStream inputStream = null;
    private InputHandler<String> inputHandler;
    private Thread readThread = null;

    public COMPortSerial(String comIdentifier, int baudRate, String appName, InputHandler<String> inputHandler){
        this.comIdentifier = comIdentifier;
        this.baudRate = baudRate;
        this.appName = appName;
        this.inputHandler = inputHandler;
    }

    public void openPort() throws PortInUseException, IOException, UnsupportedCommOperationException {
        CommPortIdentifier identifier = findComPort();
        if(identifier == null){
            throw new IOException("COM Port not found");
        }
        this.openPort = (SerialPort)
                identifier.open(this.appName, 2000);
        outputStream = openPort.getOutputStream();
        inputStream = openPort.getInputStream();
        openPort.setSerialPortParams(this.baudRate,
                SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE);
        readThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (COMPortSerial.this.openPort != null){
                    try {
                        COMPortSerial.this.read();
                    } catch (PortInUseException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (UnsupportedCommOperationException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        readThread.start();
    }

    private String readBuffer = "";
    public void read() throws PortInUseException, IOException, UnsupportedCommOperationException {
        if (this.inputStream == null){
            openPort();
        }
        char read = (char) inputStream.read();
        if ((int)read != 0 && (int)read != 65535 && (int)read != 10 && (int)read != 13) {
            readBuffer += read;
        }
        if (read == ("\n".toCharArray()[0])){
            inputHandler.handle(readBuffer);
            readBuffer = "";
        }
    }

    public void writeToPort(byte[] bytes) throws PortInUseException, IOException, UnsupportedCommOperationException {
        if (this.outputStream == null) {
            openPort();
        }
        this.outputStream.write(bytes);
        this.outputStream.flush();
    }

    public void close(){
        if (this.outputStream != null) {
            try {
                this.outputStream.close();
                this.inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.readThread.interrupt();
            this.readThread.stop();
            this.readThread = null;
            this.openPort.close();
            this.outputStream = null;
            this.openPort = null;
            inputStream = null;
        }
    }

    public SerialPort getCurrentlyOpenPort(){
        return this.openPort;
    }

    public CommPortIdentifier findComPort(){
        try {
            return CommPortIdentifier.getPortIdentifier(this.comIdentifier);
        } catch (NoSuchPortException e) {
            //Oh well, just search instead
        }
        Enumeration portList = CommPortIdentifier.getPortIdentifiers();
        while (portList.hasMoreElements()) {
            CommPortIdentifier portId = (CommPortIdentifier) portList.nextElement();
            if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                if (portId.getName().equals(this.comIdentifier)) {
                    return portId;
                }
            }
        }
        return null;
    }

    public String getComIdentifier() {
        return comIdentifier;
    }

    public void setComIdentifier(String comIdentifier) {
        this.comIdentifier = comIdentifier;
    }

    public int getBaudRate() {
        return baudRate;
    }

    public void setBaudRate(int baudRate) {
        this.baudRate = baudRate;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }
}
