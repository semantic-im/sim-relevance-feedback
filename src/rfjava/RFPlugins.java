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
import weka.core.Instance;
import weka.core.Instances;

public class RFPlugins implements Serializable{

    DataPreprocessor dp = null;
    DataPreprocessor dpP = null;

    ClusterMain cm = null;
    ClusterMain cWorkflow = null;
    ClusterMain cPlugin = null;

    int attribQueryNo1;
    int attribQueryNo2;
    
    int attribWorkflowNo1;
    ArrayList<HashSet<String>> configurations = new ArrayList<HashSet<String>>(); //workflows in each query cluster
    
    int attribWorkflowNo2;
    int attribPluginID2;
    HashSet<String> allConfigurations = new HashSet<String>(); // all workflows
    HashMap<String,HashSet<String>> plugins= new HashMap<String,HashSet<String>>(); //plugins IDs for each workflow

    String [] predAttribName = {"PluginOutputSizeInTriples","PluginTotalResponseTime","PluginProcessTotalCPUTime",
    "PluginThreadTotalCPUTime","PluginThreadUserCPUTime","PluginThreadSystemCPUTime","PluginThreadCount","PluginThreadBlockCount","PluginThreadBlockTime",
    "PluginThreadWaitCount","PluginThreadWaitTime","PluginThreadGccCount","PluginThreadGccTime"};
    int noPredAttr = predAttribName.length;

    int [] queryMetricsInputArray=new int[]{3,4,5,6,7,8,9,10,11};
    int queryMetricsInputArray2LowLimit=73;
    
    //after merging with plugins metrics
    String trainingAttr="19-last";
    int lowAttrNo=19;
    int upAttrNo=27;

    List<List<List<List<Predictor>>>> allPredictors; //list indexes: queryCluster, predictedAttribute, workflowConfig, plugin

    boolean isTrained = false;

    public void setDataPreprocessorQueryWorkflow(DataPreprocessor dp)
    {
        this.dp=dp;
    }

    public void setDataPreprocessorPlugins(DataPreprocessor dp)
    {
        this.dpP=dp;
    }

    public void initialClusteringOnQuery(String attributesToRemoveIndices, int noClusters, int attribWorkflowNo1, int attribQueryNo1, int attribPluginID2, int attribWorkflowNo2, int attribQueryNo2) throws Exception
    {
        dp.resetInitialData();
//        dp.tokenizeAttribute("QueryNamespaceValues","[,] ", "ns_");
//        dp.tokenizeAttribute("QueryDataSetSourcesValues","[,] ", "ds_");
        try {
                cm = new ClusterMain(dp.getTransformedData(), attributesToRemoveIndices);
                cm.buildClusterEM(noClusters);
//              cm.saveData("D:/ddd");

        } catch (Exception ex) {
                System.out.println(ex.getMessage());
                throw ex;
        }

        this.attribWorkflowNo1=attribWorkflowNo1-1;
        this.attribQueryNo1=attribQueryNo1-1;
        
        this.attribWorkflowNo2=attribWorkflowNo2-1;
        this.attribPluginID2=attribPluginID2-1;
          this.attribQueryNo2=attribQueryNo2-1;
        
        buildWorkflowsIdsAndPluginsSet();
    }


    public void secondClusteringOnWorkflowInsideCluster(int clusterNo, String attributesToRemoveIndices)
    {
        if (configurations.get(clusterNo).size()>0)
        {
            Instances subset=cm.getClusteredInstancesList().get(clusterNo);
            try {
                    cWorkflow = new ClusterMain(subset, attributesToRemoveIndices);
//                  cWorkflow.buildClusterCLOPE();
                    cWorkflow.buildClusterKMeans(allConfigurations.size()+1);
//                  cWorkflow.saveData("D:/inside"+clusterNo);
            } catch (Exception ex) {
                    System.out.println(ex.getMessage());
            }
        }
        else
            cWorkflow=null;
    }

    private Instances getPluginARFFInstancesRelatedToQuerys(Instances dpSubset,int[] addAttrib, int lowLimitNSDSAttributes, String pluginID1)
    {
        Instances q2 = new Instances (dpP.getTransformedData(), 0);
        for (int k=0;k<addAttrib.length;k++)
        {
            q2.insertAttributeAt(dpSubset.attribute(addAttrib[k]-1), q2.numAttributes());    //add query/workflow attributes
        }
        int l1=addAttrib.length;

/*      for (int k=lowLimitNSDSAttributes;k<=dp.getTransformedData().numAttributes();k++)
        {
            q2.insertAttributeAt(dpSubset.attribute(k-1), q2.numAttributes());    //add query/workflow attributes
        }
        int l2=dp.getTransformedData().numAttributes()-lowLimitNSDSAttributes+1;
*/
        
        for(int i=0;i<dpSubset.numInstances();i++)
        {
            String queryID1=dpSubset.instance(i).stringValue(attribQueryNo1);
            for (int j=0;j<dpP.getTransformedData().numInstances();j++)
            {
                String queryID2=dpP.getTransformedData().instance(j).stringValue(attribQueryNo2);
                String pluginID2=dpP.getTransformedData().instance(j).stringValue(attribPluginID2);
                if (queryID1.equals(queryID2)&&pluginID1.equals(pluginID2))
                {
                    Instance cInstance=new Instance(dpP.getTransformedData().instance(j));
                    for (int k=0;k<addAttrib.length;k++)
                    {
                        cInstance.insertAttributeAt(cInstance.numAttributes());    //add query/workflow attributes
                    }

/*                  for (int k=lowLimitNSDSAttributes;k<=dp.getTransformedData().numAttributes();k++)
                    {
                        cInstance.insertAttributeAt(cInstance.numAttributes());    //add query/workflow attributes
                    }
 */
                    cInstance.setDataset(q2);
                    
                    for (int k=0;k<l1;k++)
                        if (dpSubset.instance(i).isMissing(addAttrib[k]-1))
                            cInstance.setMissing(cInstance.numAttributes()-l1+k);
                        else
                            cInstance.setValue(cInstance.numAttributes()-l1+k, dpSubset.instance(i).value(addAttrib[k]-1));

/*                  cInstance.setValue(cInstance.numAttributes()-l1-l2+k, dpSubset.instance(i).value(addAttrib[k]-1));
                    for (int k=0;k<l2;k++)
                        cInstance.setValue(cInstance.numAttributes()-l2+k, dpSubset.instance(i).value(lowLimitNSDSAttributes+k-1));
 */
                    q2.add(cInstance);
                    
                    break;
                }
            }
        }
        return q2;
    }



    public List<List<List<Predictor>>> buildPredictorsInsideCluster(int clusterNo)
    {
        if (cWorkflow!=null)
        {
            List<List<List<Predictor>>> pAttrib = new ArrayList<List<List<Predictor>>>();

            Map<String,Instances> pluginsInstances = new HashMap<String,Instances>();

            List<Instances> aInstances = cWorkflow.getClusteredInstancesList();
            for (int i=0; i<aInstances.size(); i++) //workflows
            {
                    if (aInstances.get(i).numInstances()>0)
                    {
                        String workflow=aInstances.get(i).instance(0).stringValue(attribWorkflowNo1);
                        HashSet<String> currPlugins=plugins.get(workflow);
                        if (currPlugins!=null)
                        {
                            Iterator it=currPlugins.iterator();
                            while(it.hasNext())
                            {
                                String plugin=(String)it.next();
                                Instances iPlugin=getPluginARFFInstancesRelatedToQuerys(aInstances.get(i),queryMetricsInputArray, queryMetricsInputArray2LowLimit, plugin);
                                pluginsInstances.put(workflow+plugin, iPlugin);
//                              DataPreprocessor.outputArffInstances(iPlugin,"D:/p"+clusterNo+workflow.replace('.','_').replace(':','_').substring(0, 50)+"---"+plugin.replace('.','_').replace(':','_').substring(0,50)+".arff");
                                System.out.println("D:/p"+clusterNo+workflow.replace('.','_').replace(':','_')+plugin.replace('.','_').replace(':','_')+".arff"+" instante:"+iPlugin.numInstances());
                            }
                        }
                    }
            }


            for (int k=0; k<noPredAttr; k++)
            {
                    List<List<Predictor>> pClusterConfig = new ArrayList<List<Predictor>>();
                    for (int i=0; i<aInstances.size(); i++) //workflows
                    {
                            if (aInstances.get(i).numInstances()>0)
                            {
                                String workflow=aInstances.get(i).instance(0).stringValue(attribWorkflowNo1);

                                HashSet<String> currPlugins=plugins.get(workflow);

                                List<Predictor> pPlugins = new ArrayList<Predictor>();
                                
                                if (currPlugins==null)
                                    pPlugins=null;
                                else
                                {
                                    Iterator it=currPlugins.iterator();
                                    while(it.hasNext())
                                    {
                                        String plugin=(String)it.next();
                                        Instances iPlugin=pluginsInstances.get(workflow+plugin);
                                        if (iPlugin.numInstances()>0)
                                        {
                                            Predictor p=new Predictor();
                                            p.setDetails(plugin);
                                            p.setWorkflow(workflow);
                                            p.setTrainingInstances(iPlugin);
//                                          DataPreprocessor.outputArffInstances(iPlugin, "D:/iplugin.arff");
                                            p.setConsideredAttributes(trainingAttr+","+(iPlugin.attribute(predAttribName[k]).index()+1));
                                            p.setClassAttribute(predAttribName[k]);
                                            try{
                                                p.buildRegressionModel();
                                                pPlugins.add(p);
                                            }catch (Exception e){
                                                e.printStackTrace();
                                                pPlugins.add(null);
                                            }
                                        }
                                        else
                                            pPlugins.add(null);

                                    }
                                }
                                pClusterConfig.add(pPlugins);
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

    public void buildAllPredictors(String attributesToRemoveIndices) throws Exception
    {
       allPredictors = new ArrayList<List<List<List<Predictor>>>>();
        for (int i=0;i<cm.noClusters;i++)
        {
            System.out.println("--->>> Clusteri de configuratii inauntrul clusterului "+i);
            secondClusteringOnWorkflowInsideCluster(i, attributesToRemoveIndices);
            List<List<List<Predictor>>> currentPred=  buildPredictorsInsideCluster(i);
            allPredictors.add(currentPred);
        }
    }


    public String predictQuery(String query, String attributesToRemoveFromQuery, String workflowID)
    {
/*        Instances qInstQ = new Instances (dp.getTransformedData(), 0);
        Instance tInstQ = new Instance(dp.getTransformedData().numAttributes());
        for (int a=0;a<queryMetricsInputArray.length;a++)
            tInstQ.setValue(queryMetricsInputArray[a]-1, 0);

        Attribute a_QuerySizeInCharactersQ=qInstQ.attribute("QuerySizeInCharacters");
        tInstQ.setValue(a_QuerySizeInCharactersQ, query.length());

        qInstQ.add(tInstQ);
*/
        SPARQLQueryParser parser = new SPARQLQueryParser();
	parser.parseQuery(query);
	Instances qInstQ=parser.generateInstances(dp.getTransformedData());

        int clusterNo=cm.getClusterIdForQuery(qInstQ, 0, attributesToRemoveFromQuery);
        System.out.println("***Prediction***\nTest instance is in the cluster: "+clusterNo);
        String outputResult="Workflow "+workflowID+" plugins parameters prediction:\n\n";
        for (int k=0; k<noPredAttr; k++)
        {
            List<List<Predictor>> lwp=allPredictors.get(clusterNo).get(k);
            
            List<Predictor> applyWorflowPredictors=null;
            Predictor applyPredictor=null;
            for(int t=0;t<lwp.size();t++)
            {
                applyWorflowPredictors=lwp.get(t);
                for(int m=0;applyWorflowPredictors!=null && m<applyWorflowPredictors.size();m++)
                {
                    if (applyWorflowPredictors.get(m)!=null && applyWorflowPredictors.get(m).getWorkflow().equals(workflowID))
                    {
                        Iterator it= plugins.get(workflowID).iterator();
                        for (int y=0;y<m;y++)
                            it.next();
                        String currentPlugin=(String)it.next();
//                      outputResult+="***"+it.next()+"***";

                        applyPredictor=applyWorflowPredictors.get(m);
                        if (applyPredictor!=null)
                        {
                            String plugin=applyPredictor.getDetails();

//                          Instances qInst = new Instances (applyPredictor.getTrainingData(), 0);
//                          DataPreprocessor.outputArffInstances(applyPredictor.getTrainingData(), "D:/pred.arff");
//                          Instance tInst = new Instance(applyPredictor.getTrainingData().numAttributes());
/*                          for (int a=lowAttrNo-1;a<=upAttrNo-1;a++)
                                tInst.setValue(a, 0);

                            Attribute a_QuerySizeInCharacters=qInst.attribute("QuerySizeInCharacters");
                            tInst.setValue(a_QuerySizeInCharacters, query.length());
*/
//                          qInst.add(tInst);
                            Instances qInst=parser.generateInstances(applyPredictor.getTrainingData());
//                          DataPreprocessor.outputArffInstances(qInst, "D:/predict"+plugin.replace('.','_').replace(':','_')+predAttribName[k]+".arff");

                            Instances filteredAttr=DataPreprocessor.selectSubsetAttributes(qInst, applyPredictor.consideredAttributes);
                            double value=applyPredictor.predictTestInstance(filteredAttr.instance(0));
                            if (value<0) value=0;

                            outputResult+="Predicted "+ predAttribName[k]+"("+plugin+ "): "+value+"\n";

//                          DataPreprocessor.outputArffInstances(filteredAttr, "D:/predict"+plugin.replace('.','_').replace(':','_')+predAttribName[k]+"_f.arff");
                        }
                        else
                        {
                            outputResult+="Predicted "+ predAttribName[k]+"("+currentPlugin+ "): "+": N/A\n";
                        }
                    }
                }
            }
            outputResult+="\n";
        }
        return outputResult;
    }

    public HashSet<String> getPluginsIDList(String configuration)
    {
        HashSet<String> qPlugins = new HashSet<String>();
        qPlugins.clear();

        Instances idpP=dpP.getTransformedData();
        for (int i=0; i<idpP.numInstances();i++)
        {
            if (idpP.instance(i).stringValue(attribWorkflowNo2).equals(configuration))
            {
                qPlugins.add(idpP.instance(i).stringValue(attribPluginID2));
            }
        }
        return qPlugins;
    }

    public void buildWorkflowsIdsAndPluginsSet()
    {

        for (int k=0;k<cm.noClusters;k++)
        {
            Instances iCluster=cm.getClusteredInstancesList().get(k);

            HashSet<String> currentConfigs=new HashSet<String>();

            for (int i=0; i<iCluster.numInstances();i++)
            {
                String currentValue=iCluster.instance(i).stringValue(attribWorkflowNo1);
                if (!currentValue.equals("-1"))
                {
                    currentConfigs.add(currentValue);
                    allConfigurations.add(currentValue);
                }

            }

            configurations.add(k, currentConfigs);

            System.out.println("\nConfigurations in cluster " + k +": ");
            Iterator it=currentConfigs.iterator();
            while (it.hasNext()){
                String cfg=(String)it.next();
                System.out.print(cfg + " ");
            }
           
        }

        Iterator it=allConfigurations.iterator();
        while (it.hasNext()){
            String cfg=(String)it.next();
            HashSet<String> currentPluginsIDsValues=getPluginsIDList(cfg);
            plugins.put(cfg, currentPluginsIDsValues);
       }
        displayAllPlugins();
    }

    private void displayAllPlugins()
    {
        System.out.println("\nAll workflows' plugins:");
        Iterator it=allConfigurations.iterator();
        while (it.hasNext()){
            String cfg=(String)it.next();
            HashSet<String> cPlugins = plugins.get(cfg);
            System.out.print(cfg+"=====");
            Iterator it2=cPlugins.iterator();
            while (it2.hasNext())
                System.out.print(it2.next()+"+++++");
            System.out.println();
        }
    }

    public int findClusterInstance(Instances instancesSet, int instanceNo, String attributesToRemoveFromQuery)
    {
        return cm.getClusterIdForQuery(instancesSet, instanceNo, attributesToRemoveFromQuery);
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

    public static RFPlugins loadModel(String fileName)
    {
      try {
              File file = new File(fileName);
              ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
              RFPlugins r = (RFPlugins) in.readObject();
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
//      return "SELECT ?s ?p ?o WHERE { { ?s ?p ?o . ?s ?p \"trauma\"}  UNION {?s ?p ?o . ?s ?p \"yellow fever\"} UNION {?s ?p ?o . ?s ?p \"APRAXIAS\" } UNION {?s ?p ?o . ?s ?p \"level;\" } UNION {?s ?p ?o . ?s ?p \"superoxide\" } UNION {?s ?p ?o . ?s ?p \"envenomations\" } }";
//      return "PREFIX rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX rdfs:     <http://www.w3.org/2000/01/rdf-schema#> PREFIX ub:       <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#> PREFIX owl:      <http://www.w3.org/2002/07/owl#> PREFIX sparqldl: <http://pellet.owldl.com/ns/sdle#> SELECT ?X ?Y ?Z ?EA ?T ?RI WHERE {?X rdf:type ub:FullProfessor. ?X ub:name ?Y. ?X ub:telephone ?Z. ?X ub:emailAddress ?EA. ?X ub:teacherOf ?T. ?X ub:researchInterest ?RI . ?T rdf:type ub:Course .}";
//      return "PREFIX rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX rdfs:     <http://www.w3.org/2000/01/rdf-schema#> PREFIX ub:       <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#> PREFIX owl:      <http://www.w3.org/2002/07/owl#> PREFIX sparqldl: <http://pellet.owldl.com/ns/sdle#> SELECT ?X WHERE {?X rdf:type ub:Publication . ?X ub:publicationAuthor ?author. ?author rdf:type ub:AssociateProfessor .}";
        return "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns/>  PREFIX lud: <http://www.linkingurbandata.org/onto/ama/>  SELECT ?p ?w ?n1 ?l ?n2  WHERE {  ?p rdf:type lud:Paths.  ?p lud:pathFrom <http://seip.cefriel.it/ama/resource/nodes/node8252>.  ?p lud:pathTo <http://seip.cefriel.it/ama/resource/nodes/node8251>.  ?p lud:contain ?l.  ?l lud:from ?n1.  ?l lud:to ?n2.  ?p lud:pathWeight ?w.  }ORDER BY ?w";
    }

    public static String testWorkflow()
    {
        return "urn:eu.larkc.plugin.decider.SourceSplitter_urn:eu.larkc.plugin.identify.urbancomputing.ubl.RemoteGraphLoaderIdentifier_urn:eu.larkc.plugin.reason.urbancomputing.ubl.OpResPathFinderReasoner_";
    }
 }
