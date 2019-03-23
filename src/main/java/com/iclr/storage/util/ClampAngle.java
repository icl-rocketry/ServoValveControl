package com.iclr.storage.util;

/**
 * Created by Edward on 22/03/2019.
 */
public class ClampAngle {
    public static double clampDeg(double angle){
        while (angle < 0){
            angle += 360;
        }
        while (angle > 360){
            angle -= 360;
        }
        return angle;
    }

    public static double clampRad(double angle){
        while (angle < 0){
            angle += 2*Math.PI;
        }
        while (angle > 2*Math.PI){
            angle -= 2*Math.PI;
        }
        return angle;
    }
}
