package rfjava;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import weka.core.Instance;
import weka.core.Instances;

public class BestConfigurator implements Serializable{

    DataPreprocessor dp = null;
    ClusterMain cm = null;
    ClusterMain cWorkflow = null;

    int attribWorkflowNo;
    HashSet<String> allConfigurations = new HashSet<String>(); // all workflows
    ArrayList<HashSet<String>> configurations = new ArrayList<HashSet<String>>();
    ArrayList<HashMap<String,Double>> evalConfigurationsMetric= new ArrayList<HashMap<String, Double>>();
    ArrayList<String> bestConfigurations = new ArrayList<String>();

    String [] predAttribName = {"QueryResultSizeInCharacters","QueryTotalResponseTime","QueryProcessTotalCPUTime",
    "QueryThreadTotalCPUTime","QueryThreadUserCPUTime","QueryThreadSystemCPUTime","QueryThreadCount",
    "QueryThreadBlockCount","QueryThreadBlockTime","QueryThreadWaitCount","QueryThreadWaitTime","QueryThreadGccCount","QueryThreadGccTime",
    "WorkflowTotalResponseTime","WorkflowProcessTotalCPUTime","WorkflowThreadTotalCPUTime","WorkflowThreadUserCPUTime",
    "WorkflowThreadSystemCPUTime","WorkflowThreadCount","WorkflowThreadBlockCount","WorkflowThreadBlockTime","WorkflowThreadWaitCount","WorkflowThreadWaitTime",
    "WorkflowThreadGccCount","WorkflowThreadGccTime"};
                       
    int noPredAttr = predAttribName.length;
    List<List<List<Predictor>>> allPredictors; //list indexes: queryCluster, predictedAttribute, workflowConfig

    boolean isTrained = false;

    public void setDataPreprocessor(DataPreprocessor dp)
    {
        this.dp=dp;
    }

    public void initialClustering(String attributesToRemoveIndices, int noClusters, int attribWorkflowNo)
    {
        dp.resetInitialData();
//      dp.tokenizeAttribute("QueryNamespaceValues","[,] ", "ns_");
//      dp.tokenizeAttribute("QueryDataSetSourcesValues","[,] ", "ds_");
        try {
                cm = new ClusterMain(dp.getTransformedData(), attributesToRemoveIndices);
                cm.buildClusterEM(noClusters);
//              cm.saveData("D:/ddd");

        } catch (Exception ex) {
                System.out.println(ex.getMessage());
        }

        this.attribWorkflowNo=attribWorkflowNo-1;
        buildWorkflowsIdsSet();
    }


    public void secondClusteringOnWorkflowInsideCluster(int clusterNo, String attributesToRemoveIndices)
    {
        if (configurations.get(clusterNo).size()>0)
        {
            Instances subset=cm.getClusteredInstancesList().get(clusterNo);
            try {
                    cWorkflow = new ClusterMain(subset, attributesToRemoveIndices);
                    cWorkflow.buildClusterKMeans(allConfigurations.size()+1);
//                    cWorkflow.buildClusterCLOPE();
//                    cWorkflow.saveData("D:/inside"+clusterNo);
            } catch (Exception ex) {
                    System.out.println(ex.getMessage());
            }
        }
        else
            cWorkflow=null;
    }

    public List<List<Predictor>> buildPredictorsInsideCluster(int clusterNo, String queryConsideredAttributes)
    {
        if (cWorkflow!=null)
        {
            List<List<Predictor>> pAttrib = new ArrayList<List<Predictor>>();

            for (int k=0; k<noPredAttr; k++)
            {
                    List<Instances> aInstances = cWorkflow.getClusteredInstancesList();
                    List<Predictor> pClusterConfig = new ArrayList<Predictor>();
                    for (int i=0; i<aInstances.size(); i++)
                    {
                            if (aInstances.get(i).numInstances()>0)
                            {
                                pClusterConfig.add(new Predictor());
                                pClusterConfig.get(i).setDetails(aInstances.get(i).instance(0).stringValue(attribWorkflowNo));
                                pClusterConfig.get(i).setTrainingInstances(aInstances.get(i));
                                pClusterConfig.get(i).setConsideredAttributes(queryConsideredAttributes+","+(aInstances.get(i).attribute(predAttribName[k]).index()+1));
                                pClusterConfig.get(i).setClassAttribute(predAttribName[k]);
                                try{
                                    pClusterConfig.get(i).buildRegressionModel();
                                }catch (Exception e){
                                    e.printStackTrace();
                                    pClusterConfig.set(i, null);
                                }
                            }
                            else
                                pClusterConfig.add(null);
                    }
                    pAttrib.add(pClusterConfig);
            }
            return pAttrib;
        }
        else
            return null;
    }

    public void buildAllPredictors(String attributesToRemoveIndices,String queryConsideredAttributes)
    {
       allPredictors = new ArrayList<List<List<Predictor>>>();
        for (int i=0;i<cm.noClusters;i++)
        {
            System.out.println("--->>> Clusteri de configuratii inauntrul clusterului "+i);
            secondClusteringOnWorkflowInsideCluster(i, attributesToRemoveIndices);
            List<List<Predictor>> currentPred=  buildPredictorsInsideCluster(i,queryConsideredAttributes);
            allPredictors.add(currentPred);
        }
    }


    public String predictQuery(Instances qInst, int instanceNo, String attributesToRemoveFromQuery, String wf)
    {
        int clusterNo=cm.getClusterIdForQuery(qInst, instanceNo, attributesToRemoveFromQuery);
        System.out.println("***Prediction***\nTest instance is in the cluster: "+clusterNo+"\n"+"Best configuration: "+bestConfigurations.get(clusterNo)+"\n");

        
        String outputResult="Best configuration workflow: "+bestConfigurations.get(clusterNo)+"\n\n";
        if (wf.trim().isEmpty())
            wf=bestConfigurations.get(clusterNo);

        System.out.println("Prediction for the workflow: "+wf+"\n\n");
        outputResult+="Prediction for the workflow: "+wf+"\n\n";
        
        for (int k=0; k<noPredAttr; k++)
        {
            List<Predictor> lp=allPredictors.get(clusterNo).get(k);
            Predictor applyPredictor=null;
            for(int t=0;t<lp.size();t++)
                if (lp.get(t)!=null && lp.get(t).getDetails().equals(wf))
                {
                    applyPredictor=lp.get(t);
                    break;
                }

            Instances filteredAttr=DataPreprocessor.selectSubsetAttributes(qInst, applyPredictor.consideredAttributes);
            
            double value=applyPredictor.predictTestInstance(filteredAttr.instance(instanceNo));
            if (value<0)
                value=0;
            outputResult+="Predicted "+predAttribName[k]+": "+value+"\n";
        }
        return outputResult;
    }

    public String predictQuery(String query, String attributesToRemoveFromQuery, String wf)
    {
        SPARQLQueryParser parser = new SPARQLQueryParser();
	parser.parseQuery(query);
	Instances outInstances=parser.generateInstances(dp.getTransformedData());

        return predictQuery(outInstances, 0, attributesToRemoveFromQuery,wf);

/*       Instances qInst = new Instances (dp.getTransformedData(), 0);

        Instance t = new Instance(dp.getTransformedData().numAttributes());
        for (int a=lowQueryAttrNo-1;a<=upQueryAttrNo-1;a++)
            t.setValue(a, 0);

        Attribute a_QuerySizeInCharacters=qInst.attribute("QuerySizeInCharacters");
        t.setValue(a_QuerySizeInCharacters, query.length());

        qInst.add(t);
 *
        return predictQuery(qInst, 0, attributesToRemoveFromQuery);
 */
    }


    public void buildWorkflowsIdsSet()
    {
        for (int k=0;k<cm.noClusters;k++)
        {
            Instances iCluster=cm.getClusteredInstancesList().get(k);

            HashSet<String> currentConfigs=new HashSet<String>();
        
            for (int i=0; i<iCluster.numInstances();i++)
            {
                String currentValue=iCluster.instance(i).stringValue(attribWorkflowNo);
                if (!currentValue.equals("-1")) //-1 represents a workflow with errors
                {
                    currentConfigs.add(currentValue);
                    allConfigurations.add(currentValue);
                }
            }

            configurations.add(k, currentConfigs);

            System.out.println("\nConfigurations in cluster " + k +": ");
            Iterator it=currentConfigs.iterator();
            while (it.hasNext()){
                System.out.print(it.next() + " ");
            }
             System.out.println("\n");
        }
    }

    public int findClusterInstance(Instances instancesSet, int instanceNo, String attributesToRemoveFromQuery)
    {
        return cm.getClusterIdForQuery(instancesSet, instanceNo, attributesToRemoveFromQuery);
    }


    public double evalMetric(Instance i)
    {
        double v1=i.isMissing(17)?0:i.value(17);
//        double v2=i.isMissing(51)?0:i.value(51);
        return Math.sqrt(v1*v1);  //+v2*v2); //QueryTotalResponseTime and PlatformUnallocatedMemory
    }

    public String computeClusterBestConfiguration(int clusterNo)
    {
        System.out.println("In clusterul: "+clusterNo);

        HashMap<String,Double> evalClusterConfigurationsMetric=new HashMap<String,Double>();
        int i;

        Iterator it=configurations.get(clusterNo).iterator();
        while (it.hasNext()){
            ArrayList<Integer> vCluster=cm.getClusterInstancesIdxs(clusterNo);
            String clusterWorkflowValue=it.next().toString();
            double metricValueConfig=0;
            int noConsideredValues=0;
            for (int j=0;j<vCluster.size();j++)
            {
                String attribWorkflowValue=dp.getTransformedData().instance(vCluster.get(j)).stringValue(attribWorkflowNo);
                if (attribWorkflowValue.equals(clusterWorkflowValue))
                {
                    metricValueConfig+=evalMetric(dp.getTransformedData().instance(vCluster.get(j)));
                    noConsideredValues++;
                }
            }
            metricValueConfig/=noConsideredValues;
            System.out.println("\tConfiguratia: "+clusterWorkflowValue+" metrica medie evaluata: "+metricValueConfig);
            
            evalClusterConfigurationsMetric.put(clusterWorkflowValue, metricValueConfig);
        }

        evalConfigurationsMetric.add(clusterNo, evalClusterConfigurationsMetric);

        String result="";
        double min=Double.MAX_VALUE;
        Set s=evalClusterConfigurationsMetric.entrySet();
        it=s.iterator();
        while(it.hasNext())
        {
            Map.Entry m =(Map.Entry)it.next();

            String key=(String)m.getKey();
            Double value=(Double)m.getValue();

            if (value<min)
            {
                min=value;
                result=key;
            }
        }
        return result;
    }


    public void computeAllClusterBestConfigurations()
    {
        bestConfigurations=new ArrayList<String>();
        for (int i=0;i<cm.noClusters;i++)
        {
            String bestConfig=computeClusterBestConfiguration(i);
            bestConfigurations.add(i,bestConfig);
        }
    }

    public void saveModel(String fileName)
    {
          try {
                ObjectOutput out = new ObjectOutputStream(new FileOutputStream(fileName));
                out.writeObject(this);
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public static BestConfigurator loadModel(String fileName)
    {
      try {
              File file = new File(fileName);
              ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
              BestConfigurator r = (BestConfigurator) in.readObject();
              in.close();
              return r;
        } catch (ClassNotFoundException ex) {
               return null;
        } catch (IOException e) {
                return null;
        }
    }

    public static String testQuery()
    {
        return "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns/>  PREFIX lud: <http://www.linkingurbandata.org/onto/ama/>  SELECT ?p ?w ?n1 ?l ?n2  WHERE {  ?p rdf:type lud:Paths.  ?p lud:pathFrom <http://seip.cefriel.it/ama/resource/nodes/node8252>.  ?p lud:pathTo <http://seip.cefriel.it/ama/resource/nodes/node8251>.  ?p lud:contain ?l.  ?l lud:from ?n1.  ?l lud:to ?n2.  ?p lud:pathWeight ?w.  }ORDER BY ?w";
        //return "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> PREFIX gwas: <http://www.gate.ac.uk/gwas#> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT * WHERE {  gwas:x rdf:type gwas:Experiment .  gwas:x gwas:hasName \"experiment1\" .   gwas:x gwas:hasKeywordGroup gwas:g1 .   gwas:g1 gwas:hasKeyword \"breast\" .   gwas:g1 gwas:hasKeyword \"cancer\" .  gwas:x gwas:searchInRif \"false\" . gwas:x gwas:useUMLS \"false\" .  gwas:x gwas:searchMode \"1\" .  gwas:x gwas:dateConstraint \"20110412\" .gwas:x gwas:hasSnpId \"rs10000179\" . }";
    }
 }
