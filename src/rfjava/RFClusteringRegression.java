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
import java.util.List;
import weka.core.Attribute;
import weka.core.Instance;

import weka.core.Instances;

public class RFClusteringRegression implements Serializable{

        String [] predAttribName = {"QueryResultSizeInCharacters","QueryTotalResponseTime","QueryProcessTotalCPUTime",
        "QueryThreadTotalCPUTime","QueryThreadUserCPUTime","QueryThreadSystemCPUTime","QueryThreadCount",
        "QueryThreadBlockCount","QueryThreadBlockTime","QueryThreadWaitCount","QueryThreadWaitTime","QueryThreadGccCount","QueryThreadGccTime","QueryNumberOfOutOfMemoryExceptions"};

        int noPredAttr = predAttribName.length;

        double [][] testRealValues;
        double [][] testPredValues;
        double [] rSquaredError;
        double [] MAPE;
        
        double [] currentPredictedValues;

	DataPreprocessor dp = null;
        ClusterMain cm = null;
        boolean isTrained = false;
	List<List<Predictor>> pAttrib = new ArrayList<List<Predictor>>();	


        public void setDataPreprocessor(DataPreprocessor dp)
        {
            this.dp=dp;
        }

	public void initClusteringPrediction() throws Exception
	{
                if (dp==null) return;

                dp.resetInitialData();
/*		dp.tokenizeAttribute("QueryNamespaceValues","[,] ", "ns_");
                dp.tokenizeAttribute("QueryDataSetSources","[,] ", "ds_");*/
		
                String attributesToRemoveIndices = "1,2,12-last"; //rez_ns_ds.arff

		try {			
			cm = new ClusterMain(dp.getTransformedData(), attributesToRemoveIndices);
//                      cm.buildClusterKMeans(100);
                        cm.buildClusterEM(-1);
//			cm.saveData();
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}		
				
		//build the predicted attributes string list
		
		for (int k=0; k<noPredAttr; k++)
		{
			List<Instances> aInstances = cm.getClusteredInstancesList();
			List<Predictor> pCluster = new ArrayList<Predictor>();
			for (int i=0; i<aInstances.size(); i++)
				pCluster.add(new Predictor());
			
			for (int i=0; i<pCluster.size(); i++)
			{
				pCluster.get(i).setTrainingInstances(aInstances.get(i));
//                              pCluster.get(i).setConsideredAttributes("8-16,"+(aInstances.get(i).attribute(predAttribName[k]).index()+1)+",44-last");
                                pCluster.get(i).setConsideredAttributes("3-11,"+(aInstances.get(i).attribute(predAttribName[k]).index()+1));
				pCluster.get(i).setClassAttribute(predAttribName[k]);
				pCluster.get(i).buildRegressionModel();
				
				/*
				for (int j=0;j<pCluster.get(i).getCoefficients().length;j++)
        			System.out.print(pCluster.get(i).getCoefficients()[j]+" ");
				System.out.println();
				
				DataPreprocessor.outputArffInstances(pCluster.get(i).getFilteredData(),"arff/outpred"+i+".arff");
				*/
			}
			pAttrib.add(pCluster);
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

        public static RFClusteringRegression loadModel(String fileName)
        {
          try {
                  File file = new File(fileName);
                  ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
                  RFClusteringRegression r = (RFClusteringRegression) in.readObject();
                  in.close();
                  return r;
            } catch (ClassNotFoundException ex) {
                   return null;
            } catch (IOException e) {
                    return null;
            }
        }


        public String predictAttrForQuery(String query)
	{
		/* ******************** */
		/* 	TEST MODULE			*/
		/* ******************** */		

		
		/* STEP 1: TEST - GIVEN A QUERY GENERATE THE ARFF CORRESPONDING TO IT; */
		//Given a Query perform the parsing of it an try to predict the cluster to which it belongs
		
		String QueryContent1 = query;
		SPARQLQueryParser parser = new SPARQLQueryParser();
		parser.parseQuery(QueryContent1);
//		parser.generateArff("arff/outQuery.arff", true);
//              DataPreprocessor.outputArffInstances(dp.getTransformedData(),"D:/dp.arff");
		Instances outInstances=parser.generateInstances(dp.getTransformedData());

		/* STEP 2: MANUALLY SPLIT THE NAMESPACE VALUES !!! */
		/* was done by the parser */
		
		/* STEP 3: FIND THE CLUSTER TO WHICH THE ARFF / INSTANCE CORRESPONDING TO THE QUERY BELONGS */
	
                String attributesToRemoveFromQuery = "1,2,13-last"; //for the file metricsMethod.arff
                String classAttributeToRemoveFromQuery = "12"; //for the file metricsMethod.arff */
		
//		double clusterIndex [] = cm.getClusterIdForQuery("arff/outQuery.arff", attributesToRemoveFromQuery,classAttributeToRemoveFromQuery,"arff/testPredQuery.arff");
		double clusterIndex [] = cm.getClusterIdForQuerys(outInstances, attributesToRemoveFromQuery,classAttributeToRemoveFromQuery);
		Instances testInstances = cm.getFilteredTestInstances();

//              DataPreprocessor.outputArffInstances(testInstances,"D:/test.arff");

		System.out.print("\n The query belongs to cluster ");
		for (int i = 0; i < clusterIndex.length; i++)
			System.out.print(" " + clusterIndex[i]);

		/* STEP 4: GIVEN THE CLUSTER INDEX PERFORM LINEAR REGRESSION TO PREDICT THE OTHER PARAMETERS OF THE QUERY */

		String outputResult="";

                currentPredictedValues = new double[noPredAttr];

		for (int k=0; k<noPredAttr; k++)
		{
			//double value=pAttrib.get(k).get((int)(clusterIndex[0])).predictTestInstance("arff/testPredQuery.arff");
			double value=pAttrib.get(k).get((int)(clusterIndex[0])).predictTestInstance(testInstances.firstInstance());
                        if (value==-1)
                        {
                            outputResult+="Predicted "+predAttribName[k]+" : N/A\n";
                            currentPredictedValues[k]=-1;
                        }
                        else
                        {
                            if (value<0) value=0;
                            currentPredictedValues[k]=value;
                            outputResult+="Predicted "+predAttribName[k]+" : "+value+"\n";
                        }
 		}

		Instances iCluster=cm.getClusteredInstancesList().get((int)clusterIndex[0]);
		int numSuccessful=0, numFail=0;
		for (int j=0; j<iCluster.numInstances();j++)
		{
			String vStatus=iCluster.instance(j).stringValue(iCluster.attribute("QueryErrorStatus"));
			if (vStatus.equals("true")) numSuccessful++;
			if (vStatus.equals("false")) numFail++;
		}
		outputResult+="Predicted "+"QueryErrorStatus probability"+" : "+ ((double)numSuccessful/(numSuccessful+numFail))+"\n";

//              System.out.println();
// 		System.out.println(outputResult);
		return outputResult;
	}

        public String predictAttrForQuery(Instances iTest, int instanceNo)
        {
                Instances outInstances = new Instances (iTest, 1);
                Instance tInstance=iTest.instance(instanceNo);
                outInstances.add(tInstance);

                String attributesToRemoveFromQuery = "1,2,13-last"; //for the file metricsMethod.arff
                String classAttributeToRemoveFromQuery = "12"; //for the file metricsMethod.arff */

//		double clusterIndex [] = cm.getClusterIdForQuery("arff/outQuery.arff", attributesToRemoveFromQuery,classAttributeToRemoveFromQuery,"arff/testPredQuery.arff");
		double clusterIndex [] = cm.getClusterIdForQuerys(outInstances, attributesToRemoveFromQuery,classAttributeToRemoveFromQuery);
		Instances testInstances = cm.getFilteredTestInstances();

//                DataPreprocessor.outputArffInstances(testInstances,"D:/test.arff");

		System.out.print("\n The query belongs to cluster ");
		for (int i = 0; i < clusterIndex.length; i++)
			System.out.print(" " + clusterIndex[i]);

		/* STEP 4: GIVEN THE CLUSTER INDEX PERFORM LINEAR REGRESSION TO PREDICT THE OTHER PARAMETERS OF THE QUERY */

		String outputResult="";

                currentPredictedValues = new double[noPredAttr];

		for (int k=0; k<noPredAttr; k++)
		{
			//double value=pAttrib.get(k).get((int)(clusterIndex[0])).predictTestInstance("arff/testPredQuery.arff");
			double value=pAttrib.get(k).get((int)(clusterIndex[0])).predictTestInstance(testInstances.firstInstance());
                        if (value==-1)
                        {
                            outputResult+="Predicted "+predAttribName[k]+" : N/A\n";
                            currentPredictedValues[k]=-1;
                        }
                        else
                        {
                            if (value<0) value=0;
                            currentPredictedValues[k]=value;
                            outputResult+="Predicted "+predAttribName[k]+" : "+value+"\n";
                        }

		}

		Instances iCluster=cm.getClusteredInstancesList().get((int)clusterIndex[0]);
		int numSuccessful=0, numFail=0;
		for (int j=0; j<iCluster.numInstances();j++)
		{
			String vStatus=iCluster.instance(j).stringValue(iCluster.attribute("QueryErrorStatus"));
			if (vStatus.equals("true")) numSuccessful++;
			if (vStatus.equals("false")) numFail++;
		}
		outputResult+="Predicted "+"QueryErrorStatus probability"+" : "+ ((double)numSuccessful/(numSuccessful+numFail))+"\n";
 
//		System.out.println();
//		System.out.println(outputResult);
		return outputResult;
	}


        public static String testQuery()
        {
           // return "PREFIX foaf: <http://xmlns.com/foaf/0.1/> SELECT ?name1 WHERE { ?person1 foaf:knows ?person2 . ?person1 foaf:name  ?name1 . ?person2 foaf:name  \"Ionel Giosan\" .   } ORDER BY ?name1";
           // return "PREFIX rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX rdfs:     <http://www.w3.org/2000/01/rdf-schema#> PREFIX ub:       <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#> PREFIX owl:      <http://www.w3.org/2002/07/owl#> PREFIX sparqldl: <http://pellet.owldl.com/ns/sdle#> SELECT ?X ?Y ?Z ?EA ?T ?RI WHERE {?X rdf:type ub:FullProfessor. ?X ub:name ?Y. ?X ub:telephone ?Z. ?X ub:emailAddress ?EA. ?X ub:teacherOf ?T. ?X ub:researchInterest ?RI . ?T rdf:type ub:Course .}";
              return "PREFIX rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX rdfs:     <http://www.w3.org/2000/01/rdf-schema#> PREFIX ub:       <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#> PREFIX owl:      <http://www.w3.org/2002/07/owl#> PREFIX sparqldl: <http://pellet.owldl.com/ns/sdle#> SELECT ?X WHERE {?X rdf:type ub:Publication . ?X ub:publicationAuthor ?author. ?author rdf:type ub:AssociateProfessor .}";
        }

        public void automaticTest(DataPreprocessor dTest)
        {
/*
                Instances iTest=dTest.getTransformedData();
                int queryAttrNo=6;
                double [][] allPredValues=new double[iTest.numInstances()][noPredAttr];
                for (int i=0;i<iTest.numInstances();i++)
                {
                    predictAttrForQuery(iTest.instance(i).stringValue(queryAttrNo-1));
                    allPredValues[i]=currentPredictedValues;
                }
*/
/////*******
                Instances iTest=generateAutomaticTestInstances(dp.getTransformedData(), dTest);
//                DataPreprocessor.outputArffInstances(iTest, "D:/test.arff");
//                DataPreprocessor.outputArffInstances(dp.getTransformedData(), "D:/trans.arff");
                double [][] allPredValues=new double[iTest.numInstances()][noPredAttr];
                for (int i=0;i<iTest.numInstances();i++)
                {
                    predictAttrForQuery(iTest,i);
                    allPredValues[i]=currentPredictedValues;
                }

  ////*********
                testPredValues=allPredValues;
                testRealValues=new double[iTest.numInstances()][noPredAttr];

                rSquaredError = new double[noPredAttr];
                MAPE = new double[noPredAttr];

                for (int k=0;k<noPredAttr;k++)
                {
                    Attribute attr=iTest.attribute(predAttribName[k]);

                    double [] predValues= new double[iTest.numInstances()];
                    double [] realValues= new double[iTest.numInstances()];

                    System.out.println("\n****ATTRIBUTE: "+predAttribName[k]+" ****");

                    double MAPEk=0;
                    int count=0;

                    double numerator=0;

                    double avgreal=0;
                    for (int i=0;i<iTest.numInstances();i++)
                    {
                        predValues[i]=allPredValues[i][k];
                        realValues[i]=iTest.instance(i).value(attr);

                        avgreal+=realValues[i];
                        numerator+=(predValues[i]-realValues[i])*(predValues[i]-realValues[i]);

                        if (realValues[i]!=0)
                        {
                            count++;
                            double err=Math.abs((predValues[i]-realValues[i])/realValues[i]);
                            MAPEk+=(err>1)?1:err;
                        }

                        testRealValues[i][k]=realValues[i];
                        System.out.print("("+realValues[i]+";"+predValues[i]+")");
                    }
                    avgreal/=iTest.numInstances();

                    if (count>0)
                        MAPEk/=count;
                    else
                        MAPEk=0;

                    double denominator=0;
                    for (int i=0;i<iTest.numInstances();i++)
                    {
                        denominator+=(realValues[i]-avgreal)*(realValues[i]-avgreal);
                    }
                    
                    rSquaredError[k]=1-numerator/denominator;
                    MAPE[k]=MAPEk;

                }
            }

	public Instances generateAutomaticTestInstances (Instances formatInstances, DataPreprocessor dTest) {

                 Instances newSet = new Instances (formatInstances, dTest.getNoInstances());

                 for (int  k=0; k<dTest.getNoInstances(); k++)
                 {
                     Instance tInstance=new Instance(newSet.numAttributes());
                     tInstance.setDataset(formatInstances);
                     for (int i=0; i<formatInstances.numAttributes();i++)
                     {
                         String attrI=formatInstances.attribute(i).name();

                         if (attrI.substring(0,3).equals("ns_"))
                         {
                             String nameSpace=attrI.substring(3);
                             Attribute a=dTest.getInitialData().attribute("QueryNamespaceValues");
                             if (dTest.getInitialData().instance(k).stringValue(a).toLowerCase().contains(nameSpace.toLowerCase()))
                                  tInstance.setValue(formatInstances.attribute(attrI), 1);
                             else
                                  tInstance.setValue(formatInstances.attribute(attrI), 0);
                         }
                         else
                             if (attrI.substring(0,3).equals("ds_"))
                             {
                                 String nameSpace=attrI.substring(3);
                                 Attribute a=dTest.getInitialData().attribute("QueryDataSetSourcesValues");
                                 if (dTest.getInitialData().instance(k).stringValue(a).toLowerCase().contains(nameSpace.toLowerCase()))
                                      tInstance.setValue(formatInstances.attribute(attrI), 1);
                                 else
                                      tInstance.setValue(formatInstances.attribute(attrI), 0);
                             }
                             else
                             {
                                Attribute a=dTest.getInitialData().attribute(attrI);
                                Attribute b=formatInstances.attribute(attrI);
                                if (a!=null && a.isString())
                                {
                                    String s="";
                                    try{
                                        s = dTest.getInitialData().instance(k).stringValue(a);
                                    }catch (Exception e){}
                                    finally{
                                        tInstance.setValue(b, s);
                                    }
                                }
                                 else
                                    tInstance.setValue(b, dTest.getInitialData().instance(k).value(a));
                             }
                     }

                     newSet.add(tInstance);
                 }
                 return newSet;
        }

        public void plotPredictedVsActual(DataPreprocessor dTest, int index)
        {
            double[] x = new double[dTest.getNoInstances()];
            double[] y = new double[dTest.getNoInstances()];
            double[] z = new double[dTest.getNoInstances()];
            
            for (int i = 0; i < dTest.getNoInstances(); i++)
            {
                x[i] = i + 1;  
                y[i] = testPredValues[i][index];
                z[i] = testRealValues[i][index];
            }
            Plot myPlot = new Plot(x,y,z);
            myPlot.draw2DPlot(predAttribName[index],rSquaredError[index],MAPE[index]);
        }

}
