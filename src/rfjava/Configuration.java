package rfjava;

import java.util.ArrayList;

public class Configuration {

    public static String convertArrayOfMetricsToString(ArrayList<String> desiredMetrics){      
        String desiredMetricsValues="(";
            for (String m: desiredMetrics)
            {
                desiredMetricsValues+="'"+m+"',";
            }
            desiredMetricsValues=desiredMetricsValues.substring(0,desiredMetricsValues.length()-1);
            desiredMetricsValues+=")";
          return desiredMetricsValues;
    }

	public ArrayList<String> createListOfDesiredQueryMetrics (){
		ArrayList<String> desiredQueryMetrics = new ArrayList<String>();

		desiredQueryMetrics.add("QuerySizeInCharacters");
		desiredQueryMetrics.add("QueryNamespaceNb");
		desiredQueryMetrics.add("QueryVariablesNb");
		desiredQueryMetrics.add("QueryDataSetSourcesNb");
		desiredQueryMetrics.add("QueryOperatorsNb");
		desiredQueryMetrics.add("QueryLiteralsNb");
		desiredQueryMetrics.add("QueryResultOrderingNb");
		desiredQueryMetrics.add("QueryResultLimitNb");
		desiredQueryMetrics.add("QueryResultOffsetNb");
		desiredQueryMetrics.add("QueryAllocatedMemoryBefore");
		desiredQueryMetrics.add("QueryUsedMemoryBefore");
                desiredQueryMetrics.add("QueryFreeMemoryBefore");
                desiredQueryMetrics.add("QueryUnallocatedMemoryBefore");
                desiredQueryMetrics.add("QueryErrorStatus");

                //output

                desiredQueryMetrics.add("QueryResultSizeInCharacters");
                desiredQueryMetrics.add("QueryTotalResponseTime");
                desiredQueryMetrics.add("QueryProcessTotalCPUTime");
                desiredQueryMetrics.add("QueryThreadTotalCPUTime");
                desiredQueryMetrics.add("QueryThreadUserCPUTime");
                desiredQueryMetrics.add("QueryThreadSystemCPUTime");
                desiredQueryMetrics.add("QueryThreadCount");
                desiredQueryMetrics.add("QueryThreadBlockCount");
                desiredQueryMetrics.add("QueryThreadBlockTime");
                desiredQueryMetrics.add("QueryThreadWaitCount");
                desiredQueryMetrics.add("QueryThreadWaitTime");
                desiredQueryMetrics.add("QueryThreadGccCount");
                desiredQueryMetrics.add("QueryThreadGccTime");
                desiredQueryMetrics.add("QueryAllocatedMemoryAfter");
                desiredQueryMetrics.add("QueryUsedMemoryAfter");
                desiredQueryMetrics.add("QueryFreeMemoryAfter");
                desiredQueryMetrics.add("QueryUnallocatedMemoryAfter");
                desiredQueryMetrics.add("QueryDataLayerInserts");
                desiredQueryMetrics.add("QueryDataLayerSelects");
                desiredQueryMetrics.add("QueryNumberOfExceptions");
                desiredQueryMetrics.add("QueryNumberOfOutOfMemoryExceptions");
                desiredQueryMetrics.add("QueryNumberOfMalformedSparqlQueryExceptions");

		return desiredQueryMetrics;
	}
	

	public ArrayList<String> createListOfDesiredWorkflowMetrics (){
		ArrayList<String> desiredWorkflowMetrics = new ArrayList<String>();
		desiredWorkflowMetrics.add("WorkflowTotalResponseTime");
		desiredWorkflowMetrics.add("WorkflowProcessTotalCPUTime");
		desiredWorkflowMetrics.add("WorkflowThreadTotalCPUTime");
		desiredWorkflowMetrics.add("WorkflowThreadUserCPUTime");
		desiredWorkflowMetrics.add("WorkflowThreadSystemCPUTime");
		desiredWorkflowMetrics.add("WorkflowThreadCount");
                desiredWorkflowMetrics.add("WorkflowThreadBlockCount");
                desiredWorkflowMetrics.add("WorkflowThreadBlockTime");
                desiredWorkflowMetrics.add("WorkflowThreadWaitCount");
                desiredWorkflowMetrics.add("WorkflowThreadWaitTime");
                desiredWorkflowMetrics.add("WorkflowThreadGccCount");
                desiredWorkflowMetrics.add("WorkflowThreadGccTime");

		return desiredWorkflowMetrics;
	}
		
	public ArrayList<String> createListOfDesiredPluginMetrics (){
		ArrayList<String> desiredPluginMetrics = new ArrayList<String>();

		desiredPluginMetrics.add("PluginOutputSizeInTriples");
		desiredPluginMetrics.add("PluginCacheHit");
		desiredPluginMetrics.add("PluginTotalResponseTime");
		desiredPluginMetrics.add("PluginProcessTotalCPUTime");
		desiredPluginMetrics.add("PluginThreadTotalCPUTime");
		desiredPluginMetrics.add("PluginThreadUserCPUTime");
		desiredPluginMetrics.add("PluginThreadSystemCPUTime");
                desiredPluginMetrics.add("PluginThreadCount");
                desiredPluginMetrics.add("PluginThreadBlockCount");
                desiredPluginMetrics.add("PluginThreadBlockTime");
                desiredPluginMetrics.add("PluginThreadWaitCount");
                desiredPluginMetrics.add("PluginThreadWaitTime");
                desiredPluginMetrics.add("PluginThreadGccCount");
                desiredPluginMetrics.add("PluginThreadGccTime");
		
		return desiredPluginMetrics;
	}
	
	public ArrayList<String> createListOfDesiredPlatformMetrics (){
		ArrayList<String> desiredPlatformMetrics = new ArrayList<String>();
		desiredPlatformMetrics.add("PlatformGccTime");
		desiredPlatformMetrics.add("PlatformUnallocatedMemory");
		desiredPlatformMetrics.add("PlatformAvgCPUUsage");
		
		return desiredPlatformMetrics;
	}
	
	public ArrayList<String> createListOfDesiredSystemMetrics (){
		ArrayList<String> desiredPlatformMetrics = new ArrayList<String>();
		desiredPlatformMetrics.add("SystemTotalFreeMemory");
		desiredPlatformMetrics.add("SystemWaitCPUTime");
		desiredPlatformMetrics.add("SystemLoopbackNetworkSent");
		
		return desiredPlatformMetrics;
	}
	
}
