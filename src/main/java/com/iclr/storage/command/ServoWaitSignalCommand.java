package com.iclr.storage.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Edward on 01/06/2021
 */
public class ServoWaitSignalCommand extends ServoCommand<ServoWaitSignalCommand.SignalTrigger> {
    public static class SignalTrigger {
        private final int pinNumber;
        private final boolean triggerOnPinHigh;

        public SignalTrigger(int pinNumber, boolean triggerOnPinHigh) {
            this.pinNumber = pinNumber;
            this.triggerOnPinHigh = triggerOnPinHigh;
        }

        public int getPinNumber() {
            return pinNumber;
        }

        public boolean isTriggerOnPinHigh() {
            return triggerOnPinHigh;
        }
    }

    public ServoWaitSignalCommand(int servoNum, SignalTrigger waitSignalCommand){
        super(new ServoCommandInterpreter<ServoWaitSignalCommand.SignalTrigger>() {
            @Override
            public ServoCommand<ServoWaitSignalCommand.SignalTrigger> interpretCommand(int servoNum, String commandArgRaw, Object... otherParams) throws ServoCommandSyntaxException {
                System.out.println("Arg: "+commandArgRaw);
                Pattern p = Pattern.compile("(\\d+)-(.+)");
                Matcher m = p.matcher(commandArgRaw);
                if (!m.matches()){
                    throw new ServoCommandSyntaxException("Specified signal '"+commandArgRaw+"' is not a valid! Must be <pinNum>-[high/low]. Eg. '7-high'");
                }
                int pinNumber = Integer.parseInt(m.group(1));
                if(pinNumber < 0){
                    throw new NumberFormatException();
                }
                if(pinNumber > 32767){
                    throw new ServoCommandSyntaxException("Specified pin number '"+pinNumber+"' is too large to express with a 15 bits!");
                }
                String highOrLow = m.group(2);
                boolean triggerOnHigh = false;
                if(highOrLow.equalsIgnoreCase("high")){
                    triggerOnHigh = true;
                }
                else if(!highOrLow.equalsIgnoreCase("low")){
                    throw new ServoCommandSyntaxException("Specified signal '"+commandArgRaw+"' is not a valid! Must be <pinNum>-[high/low]. Eg. '7-high'");
                }
                return new ServoWaitSignalCommand(servoNum, new ServoWaitSignalCommand.SignalTrigger(pinNumber, triggerOnHigh));
            }
        });
        this.servonum = servoNum;
        this.commandArg = waitSignalCommand;
    }

    @Override
    protected String getCommandLabel() {
        return "waitSignal";
    }

    @Override
    protected byte getCommandID() {
        return 0x04;
    }

    @Override
    protected int encodeCommandArgTo16BitInt() {
        int i = 0; //left most bit = 0 for trigger on pin low
        if(commandArg.triggerOnPinHigh){
            i = 0x8000; //Left most bit = 1 for trigger on pin high
        }
        i = i | (commandArg.pinNumber & 0x7FFF); //Write pin number into last 15 bits
        return i;
    }
}
