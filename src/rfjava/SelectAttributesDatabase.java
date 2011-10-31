/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rfjava;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author User
 */
public class SelectAttributesDatabase {
    
    private float[][] inputAttributes;
    private float outputAttributes[][];
    
    //number of input attributes
    private int inputSize;
    //number of output attributes
    private int outputSize;
    //number of training instances
    private int instanceNo;
    
    //the names and types of attributes
    Map <String, ArrayList<String>> queryAttributesAndTypes;		
    Map <String, ArrayList<String>> workflowAttributesAndTypes;
    Map <String, ArrayList<String>> pluginAttributesAndTypes;
    Map <String, ArrayList<String>> platformAttributesAndTypes;
    Map <String, ArrayList<String>> systemAttributesAndTypes;
    
    //database interface
    SqlToWekaExtractor sw;
    
    //plugin names
    private ArrayList<String> pluginNames;
    
    //application names
    private ArrayList<String> appNames;
    
    //workflow names
    private ArrayList<String> workflowNames;
    
    private ArrayList<String> desiredPlatformAttributes;
    private ArrayList<String> desiredSystemAttributes;
    
    //query - workflow correspondence for scenario 1
    private String[] queryWorkflow;
    
    //names of output attributes - for attribute selection for plot in GUI
    private ArrayList<String> outputAttributeNames;
    
     boolean test = false;
    {
        desiredPlatformAttributes = new ArrayList<String>();
        desiredSystemAttributes = new ArrayList<String>();
        
        getDesiredPlatformAttributes().addAll(Arrays.asList(/*"PlatformAvgCPUUsage",*/"PlatformCPUTime","PlatformTotalCPUTime",
                "PlatformCPUUsage"/*,"PlatformGccCount","PlatformGccTime","PlatformTotalGccCount","PlatformTotalGccTime",
                "PlatformAllocatedMemory","PlatformUsedMemory","PlatformFreeMemory","PlatformUnallocatedMemory",
                "PlatformThreadsCount","PlatformThreadsStarted","PlatformTotalThreadsStarted"*/));
        
        getDesiredSystemAttributes().addAll(Arrays.asList(/*"SystemLoadAverage","SystemTotalFreeMemory","SystemTotalUsedMemory",
                "SystemTotalUsedSwap","SystemOpenFileDescrCnt","SystemSwapIn","SystemSwapOut","SystemIORead","SystemIOWrite",*/
                "SystemUserCPULoad","SystemCPULoad","SystemIdleCPULoad","SystemWaitCPULoad","SystemIrqCPULoad","SystemUserCPUTime",
                "SystemCPUTime","SystemIdleCPUTime","SystemWaitCPUTime","SystemIrqCPUTime"/*,"SystemProcessesCount",
                "SystemRunningProcessesCount","SystemThreadsCount","SystemTcpInbound","SystemTcpOutbound","SystemNetworkReceived",
                "SystemNetworkSent","SystemLoopbackNetworkReceived","SystemLoopbackNetworkSent"*/));
    }
    
    public SelectAttributesDatabase(SqlToWekaExtractor sw)
    {
        this.sw=sw;

        //find query metrics names, ids and types
        String sqlQueryQueries = "select metrics.MetricName, metrics.idMetric, mtypes.TypeName from metrics, mtypes where metrics.MetricType = mtypes.idType and metrics.MetricName like 'Query%'";

        //find workflow metrics names, ids and types
        String sqlQueryWorkflow = "select metrics.MetricName, metrics.idMetric, mtypes.TypeName from metrics, mtypes where metrics.MetricType = mtypes.idType and metrics.MetricName like 'Workflow%'";

        //find plugin metrics names, ids and types
        String sqlQueryPlugins = "select metrics.MetricName, metrics.idMetric, mtypes.TypeName from metrics, mtypes where metrics.MetricType = mtypes.idType and metrics.MetricName like 'Plugin%'";

        //find platform metrics names, ids and types
        String sqlQueryPlatform = "select metrics.MetricName, metrics.idMetric, mtypes.TypeName from metrics, mtypes where metrics.MetricType = mtypes.idType and metrics.MetricName like 'Platform%'";

        //find system metrics names, ids and types
        String sqlQuerySystem = "select metrics.MetricName, metrics.idMetric, mtypes.TypeName from metrics, mtypes where metrics.MetricType = mtypes.idType and metrics.MetricName like 'System%'";

        queryAttributesAndTypes = sw.getAttributeNamesTypesMetricsIds(sqlQueryQueries);		
        workflowAttributesAndTypes = sw.getAttributeNamesTypesMetricsIds(sqlQueryWorkflow);
        pluginAttributesAndTypes = sw.getAttributeNamesTypesMetricsIds(sqlQueryPlugins);
        platformAttributesAndTypes = sw.getAttributeNamesTypesMetricsIds(sqlQueryPlatform);
        systemAttributesAndTypes = sw.getAttributeNamesTypesMetricsIds(sqlQuerySystem);
        
        //save the names of the distinct plugins
       System.out.println("Done with constructor");     
    }
        
public SelectAttributesDatabase(SqlToWekaExtractor sw,ArrayList<String> pNames)
    {
        this.sw = sw;
        pluginNames = pNames;
        test = true;
        
        //find query metrics names, ids and types
        String sqlQueryQueries = "select metrics.MetricName, metrics.idMetric, mtypes.TypeName from metrics, mtypes where metrics.MetricType = mtypes.idType and metrics.MetricName like 'Query%'";

        //find workflow metrics names, ids and types
        String sqlQueryWorkflow = "select metrics.MetricName, metrics.idMetric, mtypes.TypeName from metrics, mtypes where metrics.MetricType = mtypes.idType and metrics.MetricName like 'Workflow%'";

        //find plugin metrics names, ids and types
        String sqlQueryPlugins = "select metrics.MetricName, metrics.idMetric, mtypes.TypeName from metrics, mtypes where metrics.MetricType = mtypes.idType and metrics.MetricName like 'Plugin%'";

        //find platform metrics names, ids and types
        String sqlQueryPlatform = "select metrics.MetricName, metrics.idMetric, mtypes.TypeName from metrics, mtypes where metrics.MetricType = mtypes.idType and metrics.MetricName like 'Platform%'";

        //find system metrics names, ids and types
        String sqlQuerySystem = "select metrics.MetricName, metrics.idMetric, mtypes.TypeName from metrics, mtypes where metrics.MetricType = mtypes.idType and metrics.MetricName like 'System%'";

        queryAttributesAndTypes = sw.getAttributeNamesTypesMetricsIds(sqlQueryQueries);		
        workflowAttributesAndTypes = sw.getAttributeNamesTypesMetricsIds(sqlQueryWorkflow);
        pluginAttributesAndTypes = sw.getAttributeNamesTypesMetricsIds(sqlQueryPlugins);
        platformAttributesAndTypes = sw.getAttributeNamesTypesMetricsIds(sqlQueryPlatform);
        systemAttributesAndTypes = sw.getAttributeNamesTypesMetricsIds(sqlQuerySystem);
        
        //save the names of the distinct plugins
        System.out.println("Done with constructor");     
    }

    /**
     * @return the input attributes
     */
    public float[][] getInputAttributes() {
        return inputAttributes;
    }

    /**
     * @return the output attributes
     */
    public float[][] getOutputAttributes() {
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
     * @return the number of loaded instances
     */
    public int getInstanceNo() {
        return instanceNo;
    }

    /**
     * @return the outputAttrNames
     */
    public ArrayList<String> getOutputAttrNames() {
        return outputAttributeNames;
    }
    
    /**
     * @return the correspondence between queries and workflow names
     */
    public String[] getQueryWorkflow() {
        return queryWorkflow;
    }
    
     /**
     * @return the desiredPlatformAttributes
     */
    public ArrayList<String> getDesiredPlatformAttributes() {
        return desiredPlatformAttributes;
    }

    /**
     * @return the desiredSystemAttributes
     */
    public ArrayList<String> getDesiredSystemAttributes() {
        return desiredSystemAttributes;
    }
    
    /**
     * @return the workflowNames
     */
    public ArrayList<String> getWorkflowNames() {
        return workflowNames;
    }
    
    /**
     * @return the appNames
     */
    public ArrayList<String> getAppNames() {
        return appNames;
    }
    
    /**
     * Scenario 1 - optimal workflow prediction
     */
    public void selectFirstScenario(String beginExecutionTime, String endExecutionTime)
    {
        /*
         * Select the queries that have been run in the given time interval
         */
        ArrayList<String> queryIds = sw.getQueryIds(beginExecutionTime, endExecutionTime);
        System.out.println(queryIds.size() + " queries");
        System.out.println("Done with query ids");     
        if (test == false)
            selectPluginNames(queryIds);
        selectAppNames(queryIds);
        System.out.println("Done with plugin & platform names"); 
        
        queryWorkflow = new String[queryIds.size()];
        
        outputAttributeNames = new ArrayList<String>();
        
        /*
         * Query attributes
         */
        ArrayList<String> desiredQueryMetrics = new ArrayList<String>();
        String[] inputQAttributes = new String[]{"QuerySizeInCharacters","QueryNamespaceNb","QueryVariablesNb",
        "QueryDataSetSourcesNb","QueryOperatorsNb","QueryLiteralsNb","QueryResultOrderingNb","QueryResultLimitNb","QueryResultOffsetNb",
        };
        String[] outputQAttributes = new String[]{"QueryResultSizeInCharacters","QueryTotalResponseTime","QueryProcessTotalCPUTime",
        "QueryThreadTotalCPUTime","QueryThreadUserCPUTime","QueryThreadSystemCPUTime","QueryThreadCount","QueryThreadBlockCount",
        "QueryThreadBlockTime","QueryThreadWaitCount","QueryThreadWaitTime","QueryThreadGccCount","QueryThreadGccTime",
        "QueryDataLayerInserts","QueryDataLayerSelects","QueryNumberOfExceptions",
        "QueryNumberOfOutOfMemoryExceptions","QueryNumberOfMalformedSparqlQueryExceptions"};
        
        desiredQueryMetrics.addAll(Arrays.asList(inputQAttributes));
        desiredQueryMetrics.addAll(Arrays.asList(outputQAttributes));     
        Map<String, ArrayList<String>> queryMetrics = sw.getQueryMetrics(queryIds, queryAttributesAndTypes, desiredQueryMetrics);
        
        outputAttributeNames.addAll(Arrays.asList(outputQAttributes));
        
        System.out.println("Done with query ");
        /*
         * Workflow attributes
         */
        ArrayList<String> desiredWorkflowMetrics = new ArrayList<String>();
        String[] outputWAttributes = new String[]{"WorkflowTotalResponseTime","WorkflowProcessTotalCPUTime",
        "WorkflowThreadTotalCPUTime","WorkflowThreadUserCPUTime","WorkflowThreadSystemCPUTime"};
        
        desiredWorkflowMetrics.addAll(Arrays.asList(outputWAttributes));       
        Map<String, ArrayList<String>> workflowMetrics = sw.getWorkflowMetricsUnified(queryIds, workflowAttributesAndTypes, desiredWorkflowMetrics);
        Map<String, String> query_workflow = sw.getQueryIds_WorkflowDescription(queryIds);
        
        outputAttributeNames.addAll(Arrays.asList(outputWAttributes));
         
        System.out.println("Done with workflow ");
        /*
         * Plugin attributes
         */
        ArrayList<String> desiredPluginMetrics = new ArrayList<String>();
        String[] outputPAttributes = new String[]{"PluginInputSizeInTriples","PluginOutputSizeInTriples","PluginCacheHit",
        "PluginErrorStatus","PluginTotalResponseTime","PluginProcessTotalCPUTime","PluginThreadTotalCPUTime","PluginThreadUserCPUTime",
        "PluginThreadSystemCPUTime","PluginThreadCount","PluginThreadBlockCount","PluginThreadBlockTime","PluginThreadWaitCount",
        "PluginThreadWaitTime","PluginThreadGccCount","PluginThreadGccTime","PluginDataLayerInserts","PluginDataLayerSelects","PluginNumberOfExceptions",
        "PluginNumberOfOutOfMemoryExceptions","PluginNumberOfMalformedSparqlQueryExceptions"};
        
        desiredPluginMetrics.addAll(Arrays.asList(outputPAttributes));
        Map<String, HashMap<String, ArrayList<String>>> pluginMetrics = sw.getQueryIds_pluginMetrics(sw.getQueryIds_PluginIds(queryIds), pluginAttributesAndTypes, desiredPluginMetrics);
        
        for (int i = 0; i < pluginNames.size(); i++)
        {
            for (int j = 0; j < outputPAttributes.length; j++)
                outputAttributeNames.add(outputPAttributes[j] + " " + pluginNames.get(i));
        }
        System.out.println("Done with plugin ");
        
        /*
         * Platform attributes
         */
        //for each query, get the platform id on which it has been run
        Map<String, String> queryIds_appNames = sw.getQueryIds_ApplicationName(queryIds);
        Map<String, String> queryIds_platformIds = sw.getQueryIds_PlatformIds(queryIds);
        Map<String, ArrayList<String>> platformMetrics = sw.getQueryIds_PlatformMetrics(queryIds_platformIds, getDesiredPlatformAttributes());
        System.out.println("Done with platform ");
        
        /*
         * System attributes
         */
        Map<String, ArrayList<String>> systemMetrics = sw.getQueryIds_SystemMetrics(queryIds_platformIds, getDesiredSystemAttributes());
        System.out.println("Done with system ");
        
        /*
         * Populate the input & output attributes
         */
        inputSize = /*getDesiredPlatformAttributes().size() + getDesiredSystemAttributes().size() +*/ inputQAttributes.length;
        outputSize = outputQAttributes.length + outputWAttributes.length + outputPAttributes.length*pluginNames.size();
        instanceNo = queryMetrics.size();
        inputAttributes = new float[instanceNo][inputSize];
        outputAttributes = new float[instanceNo][outputSize];
            
        int currentInstanceIndex = 0;
        int nri = 0, nro = 0;
        
        //plugins used by all the queries
        Map<String, Map<String,String>> pnames = sw.getQueryIds_PluginNames_PluginIds(queryIds);
        
        for (String key : queryMetrics.keySet())
        {
            nri = nro = 0;
            
            /*
             * Query-related metrics
             */
            ArrayList<String> values = queryMetrics.get(key);
            //save the query input attributes
            for (int i = 0; i < inputQAttributes.length; i++)
                inputAttributes[currentInstanceIndex][nri++] = Float.parseFloat(values.get(i));
            //save the query output attributes
            for (int i = nri; i < nri + outputQAttributes.length; i++)
                outputAttributes[currentInstanceIndex][nro++] = Float.parseFloat(values.get(i));
            
            /*
             * Workflow-related metrics
             */
            values = workflowMetrics.get(key);
            //save the workflow output attributes
            for (int i = 0; i < outputWAttributes.length; i++)
                outputAttributes[currentInstanceIndex][nro++] = Float.parseFloat(values.get(i));
            
            /*
             * Plugin-related metrics
             */
            //plugins used by the current query
            Map<String,String> currentPluginsNames = pnames.get(key);
            //set all plugin values to zero
            for (int i = nro; i < nro + pluginNames.size()*outputPAttributes.length; i++)
                outputAttributes[currentInstanceIndex][i] = 0.0f;
            //get the plugins used by the current query
            HashMap<String, ArrayList<String>> currentPlugins = pluginMetrics.get(key);
            //name of the current plugin
            String currentPluginName = "";
            
            //save the values for each plugin in the corresponding locations
            for (String pkey : currentPlugins.keySet())
            {
                ArrayList<String> list = currentPlugins.get(pkey);
                
                //find the name of the current plugin
                for(String keyCurrent : currentPluginsNames.keySet())
                {
                    if (currentPluginsNames.get(keyCurrent).equals(pkey))
                    {
                        currentPluginName = keyCurrent;
                        break;
                    }                  
                }
                
                int startPosition = nro + pluginNames.indexOf(currentPluginName)*outputPAttributes.length;
                //attributes before error status
                for (int i = 0; i < 3; i++)
                    outputAttributes[currentInstanceIndex][startPosition++] = Float.parseFloat(list.get(i));
                //error status
                if (list.get(3).equals("false"))
                    outputAttributes[currentInstanceIndex][startPosition++] = 1;
                else
                    outputAttributes[currentInstanceIndex][startPosition++] = 0;
                //attributes after error status
                for (int i = 4; i < outputPAttributes.length; i++)
                    outputAttributes[currentInstanceIndex][startPosition++] = Float.parseFloat(list.get(i));
            }
            
            /*
             * Platform - related metrics
             */
            /*values = platformMetrics.get(key);
            //save the platform input attributes
            for (int i = 0; i < getDesiredPlatformAttributes().size(); i++)
                inputAttributes[currentInstanceIndex][nri++] = Float.parseFloat(values.get(i));*/
            
            /*
             * System - related metrics
             */
            /*values = systemMetrics.get(key);
            //save the system input attributes
            for (int i = 0; i < getDesiredSystemAttributes().size(); i++)
                inputAttributes[currentInstanceIndex][nri++] = Float.parseFloat(values.get(i));*/
            
            /*
             * Application name is the last attribute - transf. to numerical
             */
            /*String aname = queryIds_appNames.get(key);
            inputAttributes[currentInstanceIndex][nri++] = getAppNames().indexOf(aname) + 1;*/
            
            //save the workflow name in the query - wf correspondence array
            queryWorkflow[currentInstanceIndex] = query_workflow.get(key);
            
            currentInstanceIndex++;
        }
    }
    
    /**
     * Scenario 2 - 
     */
    public void selectSecondScenario(String beginExecutionTime, String endExecutionTime)
    {
        /*
         * Select the queries that have been run in the given time interval
         */
        ArrayList<String> queryIds = sw.getQueryIds(beginExecutionTime, endExecutionTime);
        if (test == false)
            selectPluginNames(queryIds);
        selectAppNames(queryIds);
        selectWorkflowNames(queryIds);
        System.out.println(queryIds.size() + " queries");
        outputAttributeNames = new ArrayList<String>();
        
        /*
         * Query attributes
         */
        ArrayList<String> desiredQueryMetrics = new ArrayList<String>();
        String[] inputQAttributes = new String[]{"QuerySizeInCharacters","QueryNamespaceNb","QueryVariablesNb",
        "QueryDataSetSourcesNb","QueryOperatorsNb","QueryLiteralsNb","QueryResultOrderingNb","QueryResultLimitNb","QueryResultOffsetNb",
        };
        String[] outputQAttributes = new String[]{"QueryResultSizeInCharacters","QueryTotalResponseTime","QueryProcessTotalCPUTime",
        "QueryThreadTotalCPUTime","QueryThreadUserCPUTime","QueryThreadSystemCPUTime","QueryThreadCount","QueryThreadBlockCount",
        "QueryThreadBlockTime","QueryThreadWaitCount","QueryThreadWaitTime","QueryThreadGccCount","QueryThreadGccTime",
        "QueryDataLayerInserts", "QueryDataLayerSelects","QueryNumberOfExceptions",
        "QueryNumberOfOutOfMemoryExceptions","QueryNumberOfMalformedSparqlQueryExceptions"};
        
        desiredQueryMetrics.addAll(Arrays.asList(inputQAttributes));
        desiredQueryMetrics.addAll(Arrays.asList(outputQAttributes));
        Map<String, ArrayList<String>> queryMetrics = sw.getQueryMetrics(queryIds, queryAttributesAndTypes, desiredQueryMetrics);
        
        outputAttributeNames.addAll(Arrays.asList(outputQAttributes));
        
        /*
         * Workflow attributes
         */
        ArrayList<String> desiredWorkflowMetrics = new ArrayList<String>();
        String[] outputWAttributes = new String[]{"WorkflowTotalResponseTime","WorkflowProcessTotalCPUTime",
        "WorkflowThreadTotalCPUTime","WorkflowThreadUserCPUTime","WorkflowThreadSystemCPUTime","WorkflowThreadCount",
        "WorkflowThreadBlockCount","WorkflowThreadBlockTime","WorkflowThreadWaitCount","WorkflowThreadWaitTime",
        "WorkflowThreadGccCount","WorkflowThreadGccTime"};
        
        desiredWorkflowMetrics.addAll(Arrays.asList(outputWAttributes));    
        Map<String, ArrayList<String>> workflowMetrics = sw.getWorkflowMetricsUnified(queryIds, workflowAttributesAndTypes, desiredWorkflowMetrics);
        Map<String, String> query_workflow = sw.getQueryIds_WorkflowDescription(queryIds);
        
        outputAttributeNames.addAll(Arrays.asList(outputWAttributes));
        
        /*
         * Plugin attributes
         */
        ArrayList<String> desiredPluginMetrics = new ArrayList<String>();
        String[] outputPAttributes = new String[]{"PluginInputSizeInTriples","PluginOutputSizeInTriples","PluginCacheHit",
        "PluginErrorStatus","PluginTotalResponseTime","PluginProcessTotalCPUTime","PluginThreadTotalCPUTime","PluginThreadUserCPUTime",
        "PluginThreadSystemCPUTime","PluginThreadCount","PluginThreadBlockCount","PluginThreadBlockTime","PluginThreadWaitCount",
        "PluginThreadWaitTime","PluginThreadGccCount","PluginThreadGccTime","PluginDataLayerInserts","PluginDataLayerSelects","PluginNumberOfExceptions",
        "PluginNumberOfOutOfMemoryExceptions","PluginNumberOfMalformedSparqlQueryExceptions"};
        
        desiredPluginMetrics.addAll(Arrays.asList(outputPAttributes));
        Map<String, HashMap<String, ArrayList<String>>> pluginMetrics = sw.getQueryIds_pluginMetrics(sw.getQueryIds_PluginIds(queryIds), pluginAttributesAndTypes, desiredPluginMetrics);
        
        for (int i = 0; i < pluginNames.size(); i++)
        {
            for (int j = 0; j < outputPAttributes.length; j++)
                outputAttributeNames.add(outputPAttributes[j] + " " + pluginNames.get(i));
        }
        
        /*
         * Platform attributes
         */
        //for each query, get the platform id on which it has been run
        Map<String, String> queryIds_platformIds = sw.getQueryIds_PlatformIds(queryIds);
        Map<String, String> queryIds_appNames = sw.getQueryIds_ApplicationName(queryIds);
        Map<String, ArrayList<String>> platformMetrics = sw.getQueryIds_PlatformMetrics(queryIds_platformIds, getDesiredPlatformAttributes());
        
        /*
         * System attributes
         */
        Map<String, ArrayList<String>> systemMetrics = sw.getQueryIds_SystemMetrics(queryIds_platformIds, getDesiredSystemAttributes());
        
        /*
         * Populate the input & output attributes
         */
        inputSize = /*getDesiredPlatformAttributes().size() + getDesiredSystemAttributes().size() +*/ inputQAttributes.length /*+ 2*/;
        outputSize = outputQAttributes.length + outputWAttributes.length + outputPAttributes.length*pluginNames.size();
        instanceNo = queryMetrics.size();
        inputAttributes = new float[instanceNo][inputSize];
        outputAttributes = new float[instanceNo][outputSize];
            
        int currentInstanceIndex = 0;
        int nri = 0, nro = 0;
        
        //plugins used by all the queries
        Map<String, Map<String, String>> pnames = sw.getQueryIds_PluginNames_PluginIds(queryIds);
        
        for (String key : queryMetrics.keySet())
        {
            nri = nro = 0;
            
            /*
             * Query-related metrics
             */
            ArrayList<String> values = queryMetrics.get(key);
            //save the query input attributes
            for (int i = 0; i < inputQAttributes.length; i++)
                inputAttributes[currentInstanceIndex][nri++] = Float.parseFloat(values.get(i));
            //save the query output attributes
            for (int i = nri; i < nri + outputQAttributes.length; i++)
                outputAttributes[currentInstanceIndex][nro++] = Float.parseFloat(values.get(i));
            
            /*
             * Workflow-related metrics
             */
            values = workflowMetrics.get(key);
            //save the workflow input attributes
            /*for (int i = 0; i < inputWAttributes.length; i++)
                inputAttributes[currentInstanceIndex][nri++] = Float.parseFloat(values.get(i));*/
            //save the workflow output attributes
            for (int i = 0; i < outputWAttributes.length; i++)
                outputAttributes[currentInstanceIndex][nro++] = Float.parseFloat(values.get(i));
            
            /*
             * Plugin-related metrics
             */
            //plugins used by the current query
            Map<String, String> currentPluginsNames = pnames.get(key);
            //set all plugin values to zero
            for (int i = nro; i < nro + pluginNames.size()*outputPAttributes.length; i++)
                outputAttributes[currentInstanceIndex][i] = 0.0f;
            //get the plugins used by the current query
            HashMap<String, ArrayList<String>> currentPlugins = pluginMetrics.get(key);
            //name of the current plugin
            String currentPluginName = "";
            
            //save the values for each plugin in the corresponding locations
            for (String pkey : currentPlugins.keySet())
            {
                ArrayList<String> list = currentPlugins.get(pkey);
                
                //find the name of the current plugin
                for(String keyCurrent : currentPluginsNames.keySet())
                {
                    if (currentPluginsNames.get(keyCurrent).equals(pkey))
                    {
                        currentPluginName = keyCurrent;
                        break;
                    }                  
                }
                
                int startPosition = nro + pluginNames.indexOf(currentPluginName)*outputPAttributes.length;
                //attributes before error status
                for (int i = 0; i < 3; i++)
                    outputAttributes[currentInstanceIndex][startPosition++] = Float.parseFloat(list.get(i));
                //error status
                if (list.get(3).equals("false"))
                    outputAttributes[currentInstanceIndex][startPosition++] = 1;
                else
                    outputAttributes[currentInstanceIndex][startPosition++] = 0;
                //attributes after error status
                for (int i = 4; i < outputPAttributes.length; i++)
                    outputAttributes[currentInstanceIndex][startPosition++] = Float.parseFloat(list.get(i));
            }
            
            /*
             * Platform - related metrics
             */
            /*values = platformMetrics.get(key);
            //save the platform input attributes
            for (int i = 0; i < getDesiredPlatformAttributes().size(); i++)
                inputAttributes[currentInstanceIndex][nri++] = Float.parseFloat(values.get(i));*/
            
            /*
             * System - related metrics
             */
            /*values = systemMetrics.get(key);
            //save the system input attributes
            for (int i = 0; i < getDesiredSystemAttributes().size(); i++)
                inputAttributes[currentInstanceIndex][nri++] = Float.parseFloat(values.get(i));*/
            
            /*
             * Application name is the last attribute - transf. to numerical
             */
            /*String aname = queryIds_appNames.get(key);
            inputAttributes[currentInstanceIndex][nri++] = getAppNames().indexOf(aname) + 1;
            
            /*
             * Workflow id is the last attributes - transf. to numerical
             */
            /*String wname = query_workflow.get(key);
            inputAttributes[currentInstanceIndex][nri++] = getWorkflowNames().indexOf(wname) + 1;*/
            
            currentInstanceIndex++;
        }
    }
    
    /**
     * Save the distinct plugin names for a set of queries
     */
    private void selectPluginNames(ArrayList<String> queryIds)
    {
        Map<String, Map<String, String>> pnames = sw.getQueryIds_PluginNames_PluginIds(queryIds);
        
        pluginNames = new ArrayList<String>();
        
        for(Map<String, String> list : pnames.values())
        {
            for (String name : list.keySet())
                if (!pluginNames.contains(name))
                    pluginNames.add(name);
        }
    }
    
    /**
     * Save the distinct platform ids for a set of queries
     */
    private void selectAppNames(ArrayList<String> queryIds)
    {
        Map<String, String> pnames = sw.getQueryIds_ApplicationName(queryIds);
        
        appNames = new ArrayList<String>();
        
        for (String id : pnames.values())
        {
            if (!appNames.contains(id))
                getAppNames().add(id);
        }
    }
    
    /**
     * Save the distinct workflow names for a set of queries
     */
    private void selectWorkflowNames(ArrayList<String> queryIds)
    {
        Map<String, String> wnames = sw.getQueryIds_WorkflowDescription(queryIds);
        
        workflowNames = new ArrayList<String>();
        
        for (String name: wnames.values())
        {
            if (!workflowNames.contains(name))
                getWorkflowNames().add(name);
        }
    }
    /**
     * @return the pluginNames
     */
    public ArrayList<String> getPluginNames() {
        return pluginNames;
    }
}