/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rfjava;

import java.awt.GridLayout;
import java.awt.Rectangle;
import javax.swing.JFrame;
import javax.swing.JTextField;
import org.math.plot.Plot2DPanel;
import org.math.plot.Plot3DPanel;

/**
 *
 * @author adina
 */
public class Plot {
    
    private double[] ix;
    private double[] iy;
    private double[] iz;
    private double[] ox;
    private double[] oy;
    private double[] oz;
    
    public Plot(double[] numberOfPoints, double[] predicted, double[] actual)
    {
        this.ix = numberOfPoints;
        this.iy = predicted;
        this.iz = actual;
    }
    
    public Plot(double[] ix, double[] iy, double[] iz, double[] ox, double[] oy, double[] oz)
    {
        this.ix = ix;
        this.iy = iy;
        this.iz = iz;
        this.ox = ox;
        this.oy = oy;
        this.oz = oz;
    }
    
    
    
    public void draw3DPlot()
    {
        //input features plot
        Plot3DPanel plotInput = new Plot3DPanel();
        plotInput.addScatterPlot("Input Features", this.ix, this.iy, this.iz);
        
        //output performance measures plot
        Plot3DPanel plotOutput = new Plot3DPanel();
        plotOutput.addScatterPlot("Output Features", this.ox, this.oy, this.oz);
        
        //frame that contains both panels
        JFrame frame = new JFrame("Data Projections");
        GridLayout myLayout = new GridLayout(1,2);
        frame.setLayout(myLayout);
        frame.add(plotInput);
        frame.add(plotOutput);
        frame.setVisible(true);
    }

    public void draw2DPlot(String attrName, double rError, double MAPEError)
    {
        Plot2DPanel plot = new Plot2DPanel();
        //x axis = number of test points
        //y axis = predicted or actual value
        plot.addLinePlot("Predicted", this.ix, this.iy);
        plot.addLinePlot("Actual", this.ix, this.iz);
        plot.addLegend("SOUTH");
        plot.setAxisLabels("Query",attrName);

        JFrame frame = new JFrame("Predicted vs. Actual"+", R-squared metric="+rError+", MeanAbsolutePercentageError="+MAPEError);
        frame.setContentPane(plot);
        frame.setSize(700, 500);
        frame.setVisible(true);
    }  
}
