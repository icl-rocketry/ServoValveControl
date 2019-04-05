package com.iclr.storage.command;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Edward on 23/03/2019.
 */
public class CommandInterpreter {
    static {
        init();
    }

    public static void init(){
        //Create an instance of all the servo commands so they are added to the map needed to encode and decode them
        new ServoNullCommand(0);
        new ServoPositionCommand(0,0,180);
        new ServoWaitMillisCommand(0,0);
        new ServoWaitSecCommand(0,0);
        new ValvePositionCommand(0,0);
    }

    public static ServoCommand[] extractCommands(File f) throws IOException, ServoCommand.ServoCommandSyntaxException {
        return extractCommands(Files.readAllLines(f.toPath()).toArray(new String[]{}));
    }

    public static ServoCommand[] extractCommands(String... lines) throws ServoCommand.ServoCommandSyntaxException {
        Pattern commandLinePattern = Pattern.compile("([^\\s%]+)[\\s]+([^\\s%]+)[\\s]*([^\\s%]*).*");
        List<ServoCommand> cmds = new ArrayList<>();
        int servoNum = -1;
        for(int i=0;i<lines.length;i++){
            String line = lines[i];
            if(line.trim().startsWith("%")){
                continue; //Line is a comment, ignore
            }
            if(line.trim().isEmpty()){
                continue;
            }
            Matcher m = commandLinePattern.matcher(line.trim());
            if(!m.matches()){
                throw new ServoCommand.ServoCommandSyntaxException("Line "+(i+1)+" does not match correct formatting!");
            }
            String cmdLabel = m.group(1);
            String operandRaw = m.group(2);
            if(cmdLabel.equals("servoNum")){
                try {
                    servoNum = Integer.parseInt(operandRaw);
                    if (servoNum < 0 || servoNum > 15){
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException e) {
                    throw new ServoCommand.ServoCommandSyntaxException("Invalid servo number '"+servoNum+"' specified on line "+(i+1));
                }
                continue;
            }
            if (servoNum < 0){
                throw new ServoCommand.ServoCommandSyntaxException("Commands are specified before specifying a servo for them to apply to!");
            }
            if(!ServoCommand.commandExists(cmdLabel)){
                throw new ServoCommand.ServoCommandSyntaxException("Error command '"+cmdLabel+"' specified on line "+(i+1)+" does not exist!");
            }
            ServoCommand sc = null;
            try {
                sc = ServoCommand.getCommandInterpreter(cmdLabel).interpretCommand(servoNum,operandRaw,180.0d);
            } catch (ServoCommand.ServoCommandSyntaxException e) {
                throw new ServoCommand.ServoCommandSyntaxException("Error interpreting command on line "+(i+1)+": \n"+e.getMessage());
            }
            cmds.add(sc);
        }
        return cmds.toArray(new ServoCommand[]{});
    }
}
