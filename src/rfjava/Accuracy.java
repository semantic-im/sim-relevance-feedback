/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rfjava;

/**
 *
 * @author User
 */
public class Accuracy {
    
    public static double rSquared(double[] predicted, double[] actual)
    {
        double actualMean = 0;
        double sumUp = 0;
        double sumDown = 0;
        
        //up
        for (int i = 0; i < predicted.length; i++)
        {
            sumUp += (predicted[i]-actual[i])*(predicted[i]-actual[i]);
            actualMean += actual[i];
        }
        
        actualMean /= actual.length;
        
        //down
        for (int i = 0; i < predicted.length; i++)
        {
            sumDown += (actual[i]-actualMean)*(actual[i]-actualMean);
        }
        
        return (1-sumUp/sumDown);
    }
    
    public static double MAPE(double[] predicted, double[] actual)
    {
        int n = 0;
        double sum = 0;
        
        for (int i = 0; i < actual.length; i++)
        {
            if (actual[i] == 0)
                continue;
            else
            {
                n++;
                double err = Math.abs((actual[i]-predicted[i])/actual[i]);
                sum += err>1?1:err;
            }
        }
        
        return sum/n;
    }
    
}
