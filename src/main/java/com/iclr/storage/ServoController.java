package com.iclr.storage;

import com.iclr.storage.command.CommandEncoder;
import com.iclr.storage.command.ServoCommand;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Edward on 19/03/2019.
 */
public class ServoController implements InputHandler<String> {
    private volatile boolean ready = false;
    private COMPortSerial port;

    public ServoController(){
        this.port = new COMPortSerial("COM4", 57600,"ServoController",this);
    }

    public boolean isReady(){
        return this.ready;
    }

    public void sendCommands(ServoCommand... commands){
        byte[] commandBytes = CommandEncoder.encodeCommandListToByteArray(commands);
        int len = commandBytes.length;
        String s = len+"\n"; //Specify length of bytes for arduino to expect, and then a newline
        byte[] b = s.getBytes(Charset.forName("ASCII")); //arduino uses ASCII
        try {
            port.writeToPort(b);
            port.writeToPort(commandBytes);
        } catch (PortInUseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnsupportedCommOperationException e) {
            e.printStackTrace();
        }
    }

    public void openPort(){
        try {
            System.out.println("Opening COM port...");
            port.openPort();
            System.out.println("Port opened");
            long l = System.currentTimeMillis();
            while (!ready && System.currentTimeMillis()-l < 10000){
                Thread.sleep(100); //Wait for servo code to init on arduino and report it is ready, or 10 sec
            }
            /*port.writeToPort("90\n".getBytes());
            Thread.sleep(2000);
            port.writeToPort("0\n".getBytes());*/
        } catch (UnsupportedCommOperationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (PortInUseException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void closePort(){
        port.close();
    }

    @Override
    public void handle(String input) {
        if (input.toString().equals("Ready")){
            ready = true;
        }
        System.out.println(input);
    }
}
