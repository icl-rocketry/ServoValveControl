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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Edward on 19/03/2019.
 */
public class ServoController implements InputHandler<String> {
    private volatile boolean ready = false;
    private COMPortSerial port;

    public ServoController(String comPort, int baudRate){
        this.port = new COMPortSerial(comPort, baudRate,"ServoController",this);
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

    public void receiveAngleUpdate(int servonum, double newAngle){
        //Do nothing
    }

    @Override
    public void handle(String input) {
        if (input.toString().equals("Ready")){
            ready = true;
        }
        Matcher m = Pattern.compile("s([\\d]+)a([\\d\\.]+)").matcher(input.trim());
        if (m.matches()){ //Have been sent an updated servo angle
            int servoNum = Integer.parseInt(m.group(1));
            double d = Double.parseDouble(m.group(2));
            receiveAngleUpdate(servoNum,d);
        }
        else {
            System.out.println(input);
        }
    }
}
