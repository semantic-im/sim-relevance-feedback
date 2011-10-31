package rfjava;

import java.io.FileOutputStream;
import java.io.Serializable;
import weka.core.Instance;
import weka.core.Instances;
import weka.clusterers.EM;
import weka.clusterers.ClusterEvaluation;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
//import weka.core.converters.ArffSaver;

//import java.io.BufferedReader;
//import java.io.FileOutputStream;
//import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import weka.clusterers.AbstractClusterer;
import weka.clusterers.CLOPE;
import weka.clusterers.SimpleKMeans;
import weka.core.converters.ArffSaver;


public class ClusterMain implements Serializable{
	
	private String arff_input_file_name;
	private String arff_output_path;
	
	private Instances trainingData; //original data file
	private Instances filteredData; //data in which some attributes have been ignored
	
	private Instances testDataFiltered;
	
	private List<Instances> clusteredInstancesList;
	private String attributesToRemoveFromClustering;
	
	AbstractClusterer cluster; // the current clustering method

        int noClusters=0;
        List<ArrayList<Integer>> clustersInstancesIdxs;

	ClusterEvaluation eval;
	
	public ClusterMain(Instances inputInstances, String attributesToRemove) throws Exception {
		super();
		
		this.attributesToRemoveFromClustering = attributesToRemove;
		
		clusteredInstancesList = new ArrayList<Instances>();
		
		trainingData = null;
	
		trainingData = new Instances(inputInstances);

		eval = new ClusterEvaluation();
	}
/*	
	public ClusterMain(String arff_input_file_name, String attributesToRemove) throws Exception {
		super();
		
		this.arff_input_file_name = arff_input_file_name;
		
		//the output will be stored in files that will have the name arff_input_file_name_cluster_i.arff, where i is the cluster id 
		int arffPosition = arff_input_file_name.indexOf('.');
		if (arffPosition < 0) {
			System.out.println("ERROR - bad input file name");
			throw new Exception("Bad Name of Input arff file !");
		}
		String input_file_name_without_extension = arff_input_file_name.substring(0,arffPosition);		
		this.arff_output_path = input_file_name_without_extension;
		
		this.attributesToRemoveFromClustering = attributesToRemove;
		
		clusteredInstancesList = new ArrayList<Instances>();
		
		trainingData = null;
		
		try {
			BufferedReader reader = new BufferedReader( new FileReader(arff_input_file_name));
			trainingData = new Instances(reader);
			reader.close();
		}catch (Exception ex) {
			System.out.println("Excepton " + ex.toString());
		} 
		
		emCluster = new EM();
		eval = new ClusterEvaluation();
	}
*/
	
	public String getArff_input_file_name() {
		return arff_input_file_name;
	}

	public void setArff_input_file_name(String arff_input_file_name) {
		this.arff_input_file_name = arff_input_file_name;
	}

	public String getArff_output_path() {
		return arff_output_path;
	}

	public void setArff_output_path(String arff_output_path) {
		this.arff_output_path = arff_output_path;
	}

	public Instances getTrainingData() {
		return trainingData;
	}

	public void setTrainingData(Instances data) {
		this.trainingData = data;
	}

	public void buildClusterKMeans(int noClusters) throws Exception {

                cluster=new SimpleKMeans();
                ((SimpleKMeans)cluster).setNumClusters(noClusters);

		Remove filter = new Remove(); //generate data for cluster
		filter.setAttributeIndices(this.attributesToRemoveFromClustering);
		try {
			filter.setInputFormat(trainingData);
			filteredData = Filter.useFilter(trainingData, filter);
			cluster.buildClusterer(filteredData); // build the cluster

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
                        throw e;
		} // set the options

		//evaluate the cluster
		eval.setClusterer(cluster);

		try {
			eval.evaluateClusterer(filteredData);

                        buildClustersInstancesList();

                        setClustersComponentInstancesIdxs();
                        //displayAllClusterInstancesIdxs();
                        //System.out.println(eval.clusterResultsToString());


		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void buildClusterCLOPE() throws Exception {
		// uses the last attribute - QueryCompletionStatus as class attribute
		//if (data.classIndex() == -1) data.setClassIndex(data.numAttributes() - 1);

		//build a filtered cluster to ignore some attributes

                cluster=new CLOPE();

		Remove filter = new Remove(); //generate data for cluster
		filter.setAttributeIndices(this.attributesToRemoveFromClustering);
		try {
			filter.setInputFormat(trainingData);
			filteredData = Filter.useFilter(trainingData, filter);
			cluster.buildClusterer(filteredData); // build the cluster

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
                        throw e;
		} // set the options

		//evaluate the cluster
		eval.setClusterer(cluster);

		try {
			eval.evaluateClusterer(filteredData);

                        buildClustersInstancesList();

                        setClustersComponentInstancesIdxs();
                        //displayAllClusterInstancesIdxs();
                        //System.out.println(eval.clusterResultsToString());


		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void buildClusterEM(int noClusters) throws Exception {
		// uses the last attribute - QueryCompletionStatus as class attribute
		//if (data.classIndex() == -1) data.setClassIndex(data.numAttributes() - 1);
		
		cluster = new EM();

                String[] options = new String[2];
		options[0] = "-I"; // max. iterations
		options[1] = "100";
						
		//build a filtered cluster to ignore some attributes	
		Remove filter = new Remove(); //generate data for cluster
		filter.setAttributeIndices(this.attributesToRemoveFromClustering);			
		try {			
			filter.setInputFormat(trainingData);			
			filteredData = Filter.useFilter(trainingData, filter);
			
			((EM)cluster).setOptions(options);
			((EM)cluster).setSeed(100);
			((EM)cluster).setMinStdDev(1.0E-6);
			((EM)cluster).setNumClusters(noClusters);
			((EM)cluster).buildClusterer(filteredData); // build the cluster
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
                        throw e;
		} // set the options		
		
		//evaluate the cluster		
		eval.setClusterer(cluster);
		
		try {
			eval.evaluateClusterer(filteredData);

                        buildClustersInstancesList();

                        setClustersComponentInstancesIdxs();
                        //displayAllClusterInstancesIdxs();
                        //System.out.println(eval.clusterResultsToString());

						
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
/*	
	public double [] getClusterIdForQuery(String arffFileQuery, String attributesToRemoveFromQuery, String classAttributeToRemoveFromQuery, String outFileName){
		//evaluates the clustering method on the file arffFileQuery and returns the id of the cluster to which the arff is assigned
		// The file can contain several queries 
		double clusterIndices [] = null;
		
		//Step 1: create the instances for the arffFileQuery
		BufferedReader reader;
		Instances testData = null;
		Remove filter = new Remove(); //generate data for cluster
		filter.setAttributeIndices(attributesToRemoveFromQuery+","+classAttributeToRemoveFromQuery);
		
		try {
			reader = new BufferedReader( new FileReader(arffFileQuery));
			testData = new Instances(reader);
			reader.close();					
			
			filter.setInputFormat(testData);
			testDataFiltered = Filter.useFilter(testData, filter); //new instances with the attributes ignored
			
			eval.evaluateClusterer(testDataFiltered);		
			//Evaluate the clusterer on a set of instances. 
			//Calculates clustering statistics and stores cluster assigments for the instances in m_clusterAssignments			
			
			//System.out.println(eval.clusterResultsToString());
			
			clusterIndices = new double[eval.getNumClusters()];
			clusterIndices = eval.getClusterAssignments();	
			//the length of a should be 1
			//System.out.println("The cluster indices for the given queries belong to: " + clusterIndices.toString());			
			
		}catch (Exception ex) {
			System.out.println("Excepton " + ex.toString());
		} 		
		
		filter.setAttributeIndices(attributesToRemoveFromQuery);
		try {
			filter.setInputFormat(testData);
			testDataFiltered = Filter.useFilter(testData, filter);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		DataPreprocessor.outputArffInstances(testDataFiltered, outFileName);
		
		return clusterIndices;
	}
*/
	
	public double [] getClusterIdForQuerys(Instances inputInstances, String attributesToRemoveFromQuery, String classAttributeToRemoveFromQuery){
		//evaluates the clustering method on the file arffFileQuery and returns the id of the cluster to which the arff is assigned
		// The file can contain several queries 
		double clusterIndices [] = null;
		
		//Step 1: create the instances for the arffFileQuery
		Instances testData = null;
		Remove filter = new Remove(); //generate data for cluster
		filter.setAttributeIndices(attributesToRemoveFromQuery+","+classAttributeToRemoveFromQuery);
		
		try {
			testData = new Instances(inputInstances);
			
			filter.setInputFormat(testData);
			testDataFiltered = Filter.useFilter(testData, filter); //new instances with the attributes ignored
			
			eval.evaluateClusterer(testDataFiltered);
			//Evaluate the clusterer on a set of instances. 
			//Calculates clustering statistics and stores cluster assigments for the instances in m_clusterAssignments			
			
			//System.out.println(eval.clusterResultsToString());
			
			clusterIndices = new double[eval.getNumClusters()];
			clusterIndices = eval.getClusterAssignments();
			//the length of a should be 1
			//System.out.println("The cluster indices for the given queries belong to: " + clusterIndices.toString());			
			
		}catch (Exception ex) {
			System.out.println("Excepton " + ex.toString());
		} 		
		
		filter.setAttributeIndices(attributesToRemoveFromQuery);
		try {
			filter.setInputFormat(testData);
			testDataFiltered = Filter.useFilter(testData, filter);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return clusterIndices;
	}

	public int getClusterIdForQuery(Instances inputInstances, int instanceNo, String attributesToRemoveFromQuery){
 		double clusterIndices [] = null;

		Instances testData = null;
		Remove filter = new Remove(); //generate data for cluster
		filter.setAttributeIndices(attributesToRemoveFromQuery);

		try {
			testData = new Instances(inputInstances);
			filter.setInputFormat(testData);
			testDataFiltered = Filter.useFilter(testData, filter); //new instances with the attributes ignored

			eval.evaluateClusterer(testDataFiltered);

			clusterIndices = new double[eval.getNumClusters()];
			clusterIndices = eval.getClusterAssignments();

		}catch (Exception ex) {
			System.out.println("Excepton " + ex.toString());
		}

		return (int)clusterIndices[instanceNo];
        }


	private void buildClustersInstancesList(){
		double[] a = eval.getClusterAssignments();
		
		System.out.println("Clustering Results...");
		
		Instances clusterInstances = new Instances(trainingData);			
		Instance crtInstance = null;
		
		//at first copy the original data file to all clusterInstances and then remove the instances that are not part of the cluster.		
		for(int j = 0; j < eval.getNumClusters(); j++) {
			//create an array of instances having a number of instances equal to the number of clusters.
			Instances outputClusterInstances = new Instances(trainingData);
			outputClusterInstances.delete();
			String rangeList = "";

			System.out.print("# Instances in cluster " + j + ": ");	
			//need to start from last to first, because if we delete from first to last the indices will not be the same.
			for(int i = 0; i < a.length ; i++){
				if(j == (int)a[i]) {
					//the cluster is evaluated on dataEval that contains a subset of the original attributes, on which the clustering was made.														
					System.out.print(" " + i);
					crtInstance = clusterInstances.instance(i);
					outputClusterInstances.add(crtInstance);					
				}
				else {					
					rangeList += "" + (i) + ", ";					
				}				
			}
                        System.out.println();
			// end checking the instances that belong to cluster j
			clusteredInstancesList.add(j, outputClusterInstances);
		}
	}	


	public void saveData(String fileName) {
		// save the list of clustered output instances in an arff file
		String outFileName ;
		ArffSaver saver = new ArffSaver();				
		Instances out_cluster;
		try {			
			//save into arff output
			for (int i = 0; i < clusteredInstancesList.size(); i++) {
				outFileName = fileName + "_cluster_" + i + ".arff";
				out_cluster = clusteredInstancesList.get(i);
				FileOutputStream fs = new FileOutputStream(outFileName);
				saver.setInstances(out_cluster);
				saver.setDestination(fs);				
				saver.writeBatch();
				fs.close();
			}			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public List<Instances> getClusteredInstancesList() {
		return clusteredInstancesList;
	}

	public void setClusteredInstancesList(List<Instances> clusteredInstancesList) {
		this.clusteredInstancesList = clusteredInstancesList;
	}

	public String getAttributesToRemoveFromClustering() {
		return attributesToRemoveFromClustering;
	}

	public Instances getFilteredTestInstances() {
		return testDataFiltered;
	}
	
	public void setAttributesToRemoveFromClustering(
			String attributesToRemoveFromClustering) {
		this.attributesToRemoveFromClustering = attributesToRemoveFromClustering;
	}

        public void setClustersComponentInstancesIdxs()
        {
            noClusters=eval.getNumClusters();
            double[] a = eval.getClusterAssignments();
            clustersInstancesIdxs=new ArrayList<ArrayList<Integer>>(noClusters);
            for(int i = 0; i < noClusters ; i++)
            {
                clustersInstancesIdxs.add(i, new ArrayList<Integer>());
            }

            for(int i = 0; i < a.length ; i++){
                clustersInstancesIdxs.get((int)a[i]).add(i);
            }
        }

        public ArrayList<Integer> getClusterInstancesIdxs(int clusterNo)
        {
            return clustersInstancesIdxs.get(clusterNo);
        }

        public void displayAllClusterInstancesIdxs()
        {
            for (int i=0;i<noClusters;i++)
            {
                System.out.print("Cluster no "+i+" :");
                ArrayList<Integer> curr=getClusterInstancesIdxs(i);
                for (int j=0; j<curr.size();j++)
                    System.out.print(curr.get(j)+" ");
                System.out.println();
            }
        }
}
