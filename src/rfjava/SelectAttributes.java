/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rfjava;

import java.util.ArrayList;
import java.util.Enumeration;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.attribute.Reorder;

/**
 *
 * @author User
 */
public class SelectAttributes {
    
    private double[][] inputAttributes;
    private double[][] outputAttributes;
    
    private Instances dataInstances;
    
    private DataPreprocessor dpQuery;
    private DataPreprocessor dpWorkflow;
    private DataPreprocessor dpPlatform;
    private DataPreprocessor dpPlugin;
    private DataPreprocessor dpSystem;
    
    //number of input attributes
    private int inputSize;
    //number of output attributes
    private int outputSize;
    //number of training instances
    private int instanceNo;
    
    //the workflow name corresponding to each training instance
    //used for optimal workflow prediction
    private String[] instanceWorkflow;
    
    //distinct plugins used
    private ArrayList<String> plugins;
    
    //distinct workflows used
    private ArrayList<String> workflows;
    
    //the names of the output attributes - for the output in the GUI
    private ArrayList<String> outputAttrNames;
    
    /*
     * Constructor
     */
    public SelectAttributes(DataPreprocessor dpQuery, DataPreprocessor dpWorkflow,
    DataPreprocessor dpPlatform, DataPreprocessor dpPlugin, DataPreprocessor dpSystem)
    {
        this.dpQuery = dpQuery;
        this.dpWorkflow = dpWorkflow;
        this.dpPlatform = dpPlatform;
        this.dpPlugin = dpPlugin;
        this.dpSystem = dpSystem;
    }
    
     /**
     * @return the input attributes
     */
    public double[][] getInputAttributes() {
        return inputAttributes;
    }

    /**
     * @return the output attributes
     */
    public double[][] getOutputAttributes() {
        return outputAttributes;
    }
    
    /**
     * @return the number of input attributes
     */
    public int getInputSize() {
        return inputSize;
    }

    /**
     * @return the number of output attributes
     */
    public int getOutputSize() {
        return outputSize;
    }

    /**
     * @return the number of training instances
     */
    public int getInstanceNo() {
        return instanceNo;
    }
    
     /**
     * @return an array of workflows corresponding to each training instance
     */
    public String[] getInstanceWorkflow() {
        return instanceWorkflow;
    }
    
    /**
     * @return the number of distinct plugins
     */
    public ArrayList<String> getPlugins() {
        return plugins;
    }

    /**
     * @return the number of distinct workflows
     */
    public ArrayList<String> getWorkflows() {
        return workflows;
    }

    /**
     * @return the names of the output attributes
     */
    public ArrayList<String> getOutputAttrNames() {
        return outputAttrNames;
    }
    
    
    /**
     * @return the dataInstances
     */
    public Instances getDataInstances() {
        return dataInstances;
    }
    
    /*
     * Scenario 1 - optimal workflow prediction
     */
    public void selectFirstScenario() throws Exception
    {
        /*
         * Query attributes
         */
        Remove rm = new Remove();
        String attrIndices = new String("1,3-7,10,13");
        rm.setAttributeIndices(attrIndices);
        rm.setInputFormat(dpQuery.getTransformedData());
        Instances dataAfterRemove = Filter.useFilter(dpQuery.getTransformedData(), rm);
        
        Reorder rd = new Reorder();
        rd.setAttributeIndices("37,1-10,24,26,28,30,11-23,25,27,29,31-36");
        rd.setInputFormat(dataAfterRemove);
        Instances finalQueryData = Filter.useFilter(dataAfterRemove, rd);
        
        dataInstances = new Instances(finalQueryData, finalQueryData.numInstances());
        
        /*
         * Workflow attributes
         */
        attrIndices = new String("1,3-9,15-22,24,26,28,30-35");
        rm.setAttributeIndices(attrIndices);
        rm.setInputFormat(dpWorkflow.getTransformedData());
        Instances finalWfData = Filter.useFilter(dpWorkflow.getTransformedData(), rm);
        //ignore query context instance
        for (int i = 1; i < finalWfData.numAttributes(); i++)
            getDataInstances().insertAttributeAt(finalWfData.attribute(i), getDataInstances().numAttributes());
           
        /*
         * Plugin attributes
         */
        attrIndices = new String("1,7-8,10,36");
        rm.setAttributeIndices(attrIndices);
        rm.setInputFormat(dpPlugin.getTransformedData());
        Instances finalPluginData = Filter.useFilter(dpPlugin.getTransformedData(), rm);
        
        /*
         * Count the number of distinct plugins
         */
        plugins = new ArrayList<String>();
        Enumeration enumVal = finalPluginData.attribute(1).enumerateValues();
        while(enumVal.hasMoreElements())
            getPlugins().add(enumVal.nextElement().toString());
        int numberOfPlugins = getPlugins().size();
        
        /*
         * Populate the input & output attributes
         */
        inputSize = 13;
        outputSize = 31 + 29*numberOfPlugins;
        instanceNo = finalQueryData.numInstances();
        inputAttributes = new double[finalQueryData.numInstances()][inputSize];
        outputAttributes = new double[finalQueryData.numInstances()][outputSize];
        instanceWorkflow = new String[finalQueryData.numInstances()];
        outputAttrNames = new ArrayList<String>();
        
        //info about execute and getNextResults methods
        Instances workflowMethods = new Instances(finalWfData,2);
        
        Instance currentInstance = new Instance(getDataInstances().numAttributes()-1);
        int currentInstanceIndex;
        
        for (int i = 0; i < finalQueryData.numInstances(); i++)
        {      
            currentInstanceIndex = 0;
            currentInstance = new Instance(getDataInstances().numAttributes());
            
            workflowMethods = new Instances(finalWfData,2);
            
            //save the workflow corresponding to the current instance
            instanceWorkflow[i] = finalQueryData.instance(i).stringValue(0);        
            currentInstance.setValue(currentInstanceIndex++, finalQueryData.instance(i).value(0));
            
            //get the query context instance
            String queryContextInstance = finalQueryData.instance(i).stringValue(1);
            currentInstance.setValue(currentInstanceIndex++, finalQueryData.instance(i).value(0));
            
            //save the query input values
            int nri = 0;
            for (int j = 2; j < 15; j++)
            {
                inputAttributes[i][nri++] = finalQueryData.instance(i).value(j);
                currentInstance.setValue(currentInstanceIndex++, finalQueryData.instance(i).value(j));
            }
            
            //save the query output values
            int nro = 0;
            for (int j = 15; j < 37; j++)
            {
                outputAttributes[i][nro++] = finalQueryData.instance(i).value(j);
                currentInstance.setValue(currentInstanceIndex++, finalQueryData.instance(i).value(j));
                
                //attribute names - save only once
                if (i == 0)
                    getOutputAttrNames().add(finalQueryData.instance(i).attribute(j).name());
            }
            
            //save the workflow output attributes
            for (int a = 0; a < finalWfData.numInstances(); a++)
            {
                if (!queryContextInstance.equals(finalWfData.instance(a).stringValue(0)))
                    continue;
                else
                    workflowMethods.add(finalWfData.instance(a));
            }
            
            //currentInstance.setValue(currentInstanceIndex++, workflowMethods.instance(0).value(0));
            
            //attributes for 2 methods which can be summed 
            for (int j = 1; j < 6; j++)
            {
                if (workflowMethods.instance(1) != null)
                {
                    outputAttributes[i][nro++] = workflowMethods.instance(0).value(j) + workflowMethods.instance(1).value(j);
                    currentInstance.setValue(currentInstanceIndex++, workflowMethods.instance(0).value(j) + workflowMethods.instance(1).value(j));
                }
                else
                {
                    outputAttributes[i][nro++] = workflowMethods.instance(0).value(j);
                    currentInstance.setValue(currentInstanceIndex++, workflowMethods.instance(0).value(j));
                }
                
                //attribute names - save only once
                if (i == 0)
                    getOutputAttrNames().add(workflowMethods.instance(0).attribute(j).name());
            }
            //attributes for 2 methods related to memory which can't be summed
            for (int j = 6; j < 10; j++)
            {
                if (workflowMethods.instance(1) != null)
                {
                    outputAttributes[i][nro++] = workflowMethods.instance(1).value(j);
                    currentInstance.setValue(currentInstanceIndex++, workflowMethods.instance(1).value(j));
                }
                else
                {
                    outputAttributes[i][nro++] = workflowMethods.instance(0).value(j);
                    currentInstance.setValue(currentInstanceIndex++, workflowMethods.instance(0).value(j));
                }
                
                //attribute names - save only once
                if (i == 0)
                    getOutputAttrNames().add(workflowMethods.instance(0).attribute(j).name());
            }
            
            getDataInstances().add(currentInstance);
            
            //save the plugin output attributes
            for (int a = nro; a < nro + numberOfPlugins*29; a++)
                outputAttributes[i][a] = 0.0;
            for (int a = 0; a < finalPluginData.numInstances(); a++)
            {
                if(!queryContextInstance.equals(finalPluginData.instance(a).stringValue(0)))
                    continue;
                else
                {
                    String pluginName = finalPluginData.instance(a).stringValue(1);
                    //position of the plugin
                    int whichPlugin = getPlugins().indexOf(pluginName);
                    //start position for output attributes
                    int startPosition = nro + whichPlugin*29;
                    //attributes until error status
                    for (int j = 2; j < 5; j++)
                    {
                        outputAttributes[i][startPosition++] = finalPluginData.instance(a).value(j);
                        
                        //attribute names - save only once
                        if (i == 0)
                            getOutputAttrNames().add(finalPluginData.instance(0).attribute(j).name());
                    }
                    //error status
                    if (finalPluginData.instance(a).value(5) == 0)
                        outputAttributes[i][startPosition++] = 1;
                    else
                        outputAttributes[i][startPosition++] = 0;
                    //attribute names - save only once
                        if (i == 0)
                            getOutputAttrNames().add(finalPluginData.instance(0).attribute(5).name());
                    //attributes after error status
                    for (int j = 6 ; j < 31; j++)
                    {
                        outputAttributes[i][startPosition++] = finalPluginData.instance(a).value(j);
                        
                        //attribute names - save only once
                        if (i == 0)
                            getOutputAttrNames().add(finalPluginData.instance(0).attribute(j).name());
                    }
                }
            }
           
        }
        
    }
    
    /*
     * Scenario 2 - workflow parameters prediction
     */
    public void selectSecondScenario() throws Exception
    {
        /*
         * Query attributes
         */
        Remove rm = new Remove();
        String attrIndices = new String("1,3-7,10,13");
        rm.setAttributeIndices(attrIndices);
        rm.setInputFormat(dpQuery.getTransformedData());
        Instances dataAfterRemoveQuery = Filter.useFilter(dpQuery.getTransformedData(), rm);
        
        Reorder rd = new Reorder();
        rd.setAttributeIndices("37,1-10,24,26,28,30,11-23,25,27,29,31-36");
        rd.setInputFormat(dataAfterRemoveQuery);
        Instances finalQueryData = Filter.useFilter(dataAfterRemoveQuery, rd);
        
        dataInstances = new Instances(finalQueryData, finalQueryData.numInstances());
        
        /*
         * Workflow attributes
         */
        attrIndices = new String("1,3-9,30-35");
        rm.setAttributeIndices(attrIndices);
        rm.setInputFormat(dpWorkflow.getTransformedData());
        Instances dataAfterRemoveWorkflow = Filter.useFilter(dpWorkflow.getTransformedData(), rm);
        
        rd = new Reorder();
        rd.setAttributeIndices("1,14,16,18,20,2-13,15,17,19,21");
        rd.setInputFormat(dataAfterRemoveWorkflow);
        Instances finalWfData = Filter.useFilter(dataAfterRemoveWorkflow, rd);
        //ignore query context instance
        for (int i = 1; i < finalWfData.numAttributes(); i++)
            getDataInstances().insertAttributeAt(finalWfData.attribute(i), getDataInstances().numAttributes());
        
        /*
         * Plugin attributes
         */
        attrIndices = new String("1,7-8,10,36");
        rm.setAttributeIndices(attrIndices);
        rm.setInputFormat(dpPlugin.getTransformedData());
        Instances finalPluginData = Filter.useFilter(dpPlugin.getTransformedData(), rm);
        
        /*
         * Count the number of distinct plugins
         */
        plugins = new ArrayList<String>();
        Enumeration enumVal = finalPluginData.attribute(1).enumerateValues();
        while(enumVal.hasMoreElements())
            getPlugins().add(enumVal.nextElement().toString());
        int numberOfPlugins = getPlugins().size();
        
        /*
         * Count the number of distinct workflows
         * workflow - considered as input attribute
         */
        workflows = new ArrayList<String>();
        enumVal = finalQueryData.attribute(0).enumerateValues();
        while(enumVal.hasMoreElements())
            getWorkflows().add(enumVal.nextElement().toString());
        
        /*
         * Populate the input & output attributes
         */
        inputSize = 18;
        outputSize = 38 + 29*numberOfPlugins;
        instanceNo = finalQueryData.numInstances();
        inputAttributes = new double[finalQueryData.numInstances()][inputSize];
        outputAttributes = new double[finalQueryData.numInstances()][outputSize];
        instanceWorkflow = new String[finalQueryData.numInstances()];
        outputAttrNames = new ArrayList<String>();
        
        //info about execute and getNextResults methods
        Instances workflowMethods = new Instances(finalWfData,2);
        
        Instance currentInstance = new Instance(getDataInstances().numAttributes());
        int currentInstanceIndex;
        
        for (int i = 0; i < finalQueryData.numInstances(); i++)
        {
            currentInstanceIndex = 0;
            currentInstance = new Instance(getDataInstances().numAttributes());
            
            workflowMethods = new Instances(finalWfData,2);
            
            //save the workflow corresponding to the current instance
            instanceWorkflow[i] = finalQueryData.instance(i).stringValue(0);
            inputAttributes[i][0] = getWorkflows().indexOf(instanceWorkflow[i]) + 1;
            
            //get the query context instance and workflow class
            String queryContextInstance = finalQueryData.instance(i).stringValue(1);
            currentInstance.setValue(currentInstanceIndex++, finalQueryData.instance(i).value(0));
            currentInstance.setValue(currentInstanceIndex++, finalQueryData.instance(i).value(1));
            
            //save the query input values
            //nri = 1 because the first attribute is the workflow
            int nri = 1;
            for (int j = 2; j < 15; j++)
            {
                inputAttributes[i][nri++] = finalQueryData.instance(i).value(j);
                currentInstance.setValue(currentInstanceIndex++, finalQueryData.instance(i).value(j));
            }
            
            //save the query output values
            int nro = 0;
            for (int j = 15; j < 37; j++)
            {
                outputAttributes[i][nro++] = finalQueryData.instance(i).value(j);
                currentInstance.setValue(currentInstanceIndex++, finalQueryData.instance(i).value(j));
                
                //attribute names - save only once
                if (i == 0)
                    getOutputAttrNames().add(finalQueryData.instance(0).attribute(j).name());
            }
            
            //save the workflow input and output attributes
            for (int a = 0; a < finalWfData.numInstances(); a++)
            {
                if (!queryContextInstance.equals(finalWfData.instance(a).stringValue(0)))
                    continue;
                else
                    workflowMethods.add(finalWfData.instance(a));
            }
            //input
            for (int j = 1; j < 5; j++)
            {
                inputAttributes[i][nri++] = workflowMethods.instance(0).value(j);
                currentInstance.setValue(currentInstanceIndex++, workflowMethods.instance(0).value(j));
            }
            //output attributes for 2 methods which can be summed 
            for (int j = 5; j < 17; j++)
            {
                if (workflowMethods.instance(1) != null)
                {
                    outputAttributes[i][nro++] = workflowMethods.instance(0).value(j) + workflowMethods.instance(1).value(j);
                    currentInstance.setValue(currentInstanceIndex++, workflowMethods.instance(0).value(j) + workflowMethods.instance(1).value(j));
                }
                else
                {
                    outputAttributes[i][nro++] = workflowMethods.instance(0).value(j);
                    currentInstance.setValue(currentInstanceIndex++, workflowMethods.instance(0).value(j));
                }
                
                //attribute names - save only once
                if (i == 0)
                    getOutputAttrNames().add(workflowMethods.instance(0).attribute(j).name());
            }
            //output attributes for 2 methods related to memory which can't be summed
            for (int j = 17; j < 21; j++)
            {
                if (workflowMethods.instance(1) != null)
                {
                    outputAttributes[i][nro++] = workflowMethods.instance(1).value(j);
                    currentInstance.setValue(currentInstanceIndex++, workflowMethods.instance(1).value(j));
                }
                else
                {
                    outputAttributes[i][nro++] = workflowMethods.instance(0).value(j);
                    currentInstance.setValue(currentInstanceIndex++, workflowMethods.instance(0).value(j));
                }
                
                //attribute names - save only once
                    if (i == 0)
                        getOutputAttrNames().add(workflowMethods.instance(0).attribute(j).name());
            }
            
            getDataInstances().add(currentInstance);
            
            //save the plugin output attributes
            for (int a = nro; a < nro + numberOfPlugins*29; a++)
                outputAttributes[i][a] = 0.0;
            for (int a = 0; a < finalPluginData.numInstances(); a++)
            {
                if(!queryContextInstance.equals(finalPluginData.instance(a).stringValue(0)))
                    continue;
                else
                {
                    String pluginName = finalPluginData.instance(a).stringValue(1);
                    //position of the plugin
                    int whichPlugin = getPlugins().indexOf(pluginName);
                    //start position for output attributes
                    int startPosition = nro + whichPlugin*29;
                    //attributes until error status
                    for (int j = 2; j < 5; j++)
                    {
                        outputAttributes[i][startPosition++] = finalPluginData.instance(a).value(j);
                        
                        //attribute names - save only once
                        if (i == 0)
                            getOutputAttrNames().add(finalPluginData.instance(0).attribute(j).name());
                    }
                    //error status
                    if (finalPluginData.instance(a).value(5) == 0)
                        outputAttributes[i][startPosition++] = 1;
                    else
                        outputAttributes[i][startPosition++] = 0;
                    //attribute names - save only once
                    if (i == 0)
                        getOutputAttrNames().add(finalPluginData.instance(0).attribute(5).name());
                    //attributes after error status
                    for (int j = 6 ; j < 31; j++)
                    {
                        outputAttributes[i][startPosition++] = finalPluginData.instance(a).value(j);
                        
                        //attribute names - save only once
                        if (i == 0)
                            getOutputAttrNames().add(finalPluginData.instance(0).attribute(j).name());
                    }
                }
            }
           
        }
    }
}
