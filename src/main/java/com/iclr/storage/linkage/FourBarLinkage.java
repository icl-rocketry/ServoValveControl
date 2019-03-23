package com.iclr.storage.linkage;

import com.iclr.storage.util.ClampAngle;

/**
 * Created by Edward on 22/03/2019.
 * See https://synthetica.eng.uci.edu/mechanicaldesign101/McCarthyNotes-2.pdf
 */
public class FourBarLinkage {
    private double g = 0.96; //Distance between pivots
    private double b = 0.95; //Length of bar connected to right pivot
    private double a = 0.5; //Length of bar connected to left pivot
    private double h = 1.4; //Length of bar connecting the other two bars

    public FourBarLinkage(double distanceBetweenPivots,double lengthOfRightPivotBar,double lengthOfLeftPivotBar,double lengthOfConnectingBar){
        this.g = distanceBetweenPivots;
        this.b = lengthOfRightPivotBar;
        this.a = lengthOfLeftPivotBar;
        this.h = lengthOfConnectingBar;
    }

    public double getDistanceBetweenPivots(){
        return this.g;
    }

    public double getLengthOfRightPivotBar(){
        return this.b;
    }

    public double getLengthOfLeftPivotBar(){
        return this.a;
    }

    public double getLengthOfConnectingBar(){
        return this.h;
    }

    public double[] getPossibleAnglesOfRightLinkageDegrees(double angleOfLeftLinkageDegrees){
        double theta = Math.toRadians(angleOfLeftLinkageDegrees);
        double A = getACoefficient(theta);
        double B = getBCoefficient(theta);
        double C = getCCoefficient(theta);
        //As defined in paper
        double delta = Math.atan2(B,A);
        double psi1 = delta + Math.acos(-C/Math.sqrt(Math.pow(A,2) + Math.pow(B,2)));
        double psi2 = delta - Math.acos(-C/Math.sqrt(Math.pow(A,2) + Math.pow(B,2)));
        return new double[]{ClampAngle.clampDeg(Math.toDegrees(psi1)),ClampAngle.clampDeg(Math.toDegrees(psi2))};
    }

    public double[] getPossibleAnglesOfLeftLinkageDegrees(double angleOfRightLinkageDegrees){
        //Use the mirror image of this four bar linkage to determine
        FourBarLinkage mirror = new FourBarLinkage(this.g,this.a,this.b,this.h);
        double angleOfRightLinkageDegrees2 = 180-angleOfRightLinkageDegrees;
        double[] roots = mirror.getPossibleAnglesOfRightLinkageDegrees(angleOfRightLinkageDegrees2);
        roots[0] = ClampAngle.clampDeg(180-roots[0]);
        roots[1] = ClampAngle.clampDeg(180-roots[1]);
        return roots;
    }

    //Coefficient A as defined in paper
    private double getACoefficient(double thetaRadians){
        return 2*this.b*this.g - 2*this.a*this.b*Math.cos(thetaRadians);
    }

    //Coefficient B as defined in paper
    private double getBCoefficient(double thetaRadians){
        return -(2*this.a*this.b*Math.sin(thetaRadians));
    }

    //Coefficient C as defined in paper
    private double getCCoefficient(double thetaRadians){
        return Math.pow(this.a,2) + Math.pow(this.b,2) + Math.pow(this.g,2) - Math.pow(this.h,2) - 2*this.a*this.g*Math.cos(thetaRadians);
    }
}
