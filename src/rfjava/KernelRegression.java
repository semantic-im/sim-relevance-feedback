package rfjava;

import Jama.Matrix;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

public class KernelRegression implements Serializable
{
    DataPreprocessor dp = null;

    private int rows;
    private int xVars;
    private int noFeatures;
    private int [] trainFeatures;
    private int classFeature;
    String className;
    private Matrix X;
    private Matrix Y;
    private Matrix alfa;
    public boolean isTrained=false;
  
    public void initPredictionModel(int [] trainFeatures, int classFeature)
    {
	this.noFeatures=trainFeatures.length;

        xVars=noFeatures;
	rows=dp.getNoInstances();

	this.trainFeatures=trainFeatures;
	this.classFeature=classFeature;
	className=dp.getAttrNamesList()[classFeature-1];

	X = new Matrix(rows,xVars);
	Y = new Matrix(rows,1);
	alfa = new Matrix(rows,1);

	for (int i = 0; i < rows; i++)
	{
		for (int j = 0; j<noFeatures; j++)
		{
			double crtV = dp.getTransformedData().instance(i).value(trainFeatures[j]-1);
			X.set(i, j, crtV);
		}
		
		double crtV = dp.getTransformedData().instance(i).value(classFeature-1);
		Y.set(i, 0, crtV);
	}
        displayMatrix(X);
     }


    private void displayMatrix(Matrix X)
    {
        for (int i=0;i<X.getRowDimension();i++)
        {
            for (int j=0;j<X.getColumnDimension();j++)
            {
                System.out.print(X.get(i, j)+" ");
            }
            System.out.println();
        }
    }

    private void displayInstance(Instance t)
    {
        System.out.println("No attributes: "+t.numAttributes());
        for (int i=0;i<t.numAttributes();i++)
            System.out.print(t.value(i)+" ");
        System.out.println();
    }

    
    public void setDataPreprocessor(DataPreprocessor dp)
    {
        this.dp=dp;
    }

    public Instance selectAttributesFirstInstance(Instances inputInstances, int[] trainFeatures)
    {
            Instances filteredData = null;
            Remove rFilter = new Remove();
            rFilter.setInvertSelection(true);
            String fString="";
            for (int j = 0; j<trainFeatures.length; j++)
                fString=fString+String.valueOf(trainFeatures[j])+",";

            rFilter.setAttributeIndices(fString);
            try {
                    rFilter.setInputFormat(inputInstances);
                    filteredData = Filter.useFilter(inputInstances, rFilter);
            } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
            }
            displayInstance(filteredData.firstInstance());
            return filteredData.firstInstance();
    }

    public void computeKernelAndWeights()
    {
	/*
	* Compute the kernel matrix.
	*/
        Matrix G = new Matrix(rows, rows);
        Matrix arrX1 = new Matrix(1, X.getColumnDimension());
        Matrix arrX2 = new Matrix(1, X.getColumnDimension());

	//G
	for (int i = 0; i < rows; i++)
	{
		for (int j = i; j < rows; j++) 
		{
                    for (int k=0; k<X.getColumnDimension(); k++)
                    {
                        arrX1.set(0, k, X.get(i, k));
                        arrX2.set(0, k, X.get(j, k));
                    }
			
			double dist=vectorsDistance(arrX1,arrX2);
			G.set(i,j,dist);
      			G.set(j,i,dist);
		}
	}

	/*
	* Shift the diagonal of the kernel matrix.
	*/
	double k = 1.0/1000.0;
	for (int i = 0; i < G.getRowDimension(); i++)
            G.set(i,i,G.get(i,i)+k);

	/*
	* Find the transpose of the shifted kernel matrix.
	*/
        Matrix GInverse = G.inverse();

	/*
	* Compute alfa.
	*/

        alfa=GInverse.times(Y);

        displayMatrix(alfa);
    }

    public double predictTestInstance(Instance testData)
    {
	Matrix testInstance = new Matrix(1,xVars);

        for (int j = 0; j<noFeatures; j++)
        {
            double crtV = testData.value(j);
            testInstance.set(0, j, crtV);
        }

        double r = computeEstimatedValue(testInstance);

        return DataPreprocessor.twoDecimalDouble(r);
    }

    public String predictTestQuery(String query)
    {
        /*
        String QueryContent1 = query;
        SPARQLQueryParser parser = new SPARQLQueryParser();
        parser.parseQuery(QueryContent1);
        Instances outInstances=parser.generateInstances();
        */

        Instance test=selectAttributesFirstInstance(dp.getTransformedData(), trainFeatures);
        String result = "Predicted "+dp.getTransformedData().attribute(classFeature-1).name()+": "+predictTestInstance(test);
        
        return result;
    }


    private double vectorsDistance(Matrix x1, Matrix x2)
    {
	double norm = 0;
	for (int i = 0; i < x1.getColumnDimension(); i++)
	{
		double v1=x1.get(0,i);
		double v2=x2.get(0,i);
		norm += (v1 - v2)*(v1 - v2);
	}
	return Math.exp((double)(-norm)/1000000);
    }


    public double computeEstimatedValue(Matrix testInstance)
    {
	double val = 0.0;

	//compute the similarity matrix between the new instance and each datapoint
	double[] k = new double[rows];
        Matrix arrX = new Matrix(1,X.getColumnDimension());

	for (int i = 0; i < rows; i++)
	{
                for (int t=0; t<X.getColumnDimension(); t++)
                       arrX.set(0, t, X.get(i, t));
		k[i] = vectorsDistance(testInstance,arrX);
	}

	for (int i = 0; i < rows; i++)
	{
		val = val + alfa.get(i,0)*k[i];
	}
	return val;
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

    public static KernelRegression loadModel(String fileName)
    {
      try {
              File file = new File(fileName);
              ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
              KernelRegression r = (KernelRegression) in.readObject();
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
      // return "PREFIX foaf: <http://xmlns.com/foaf/0.1/> SELECT ?name1 WHERE { ?person1 foaf:knows ?person2 . ?person1 foaf:name  ?name1 . ?person2 foaf:name  \"Ionel Giosan\" .   } ORDER BY ?name1";
      // return "PREFIX rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX rdfs:     <http://www.w3.org/2000/01/rdf-schema#> PREFIX ub:       <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#> PREFIX owl:      <http://www.w3.org/2002/07/owl#> PREFIX sparqldl: <http://pellet.owldl.com/ns/sdle#> SELECT ?X ?Y ?Z ?EA ?T ?RI WHERE {?X rdf:type ub:FullProfessor. ?X ub:name ?Y. ?X ub:telephone ?Z. ?X ub:emailAddress ?EA. ?X ub:teacherOf ?T. ?X ub:researchInterest ?RI . ?T rdf:type ub:Course .}";
         return "PREFIX rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX rdfs:     <http://www.w3.org/2000/01/rdf-schema#> PREFIX ub:       <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#> PREFIX owl:      <http://www.w3.org/2002/07/owl#> PREFIX sparqldl: <http://pellet.owldl.com/ns/sdle#> SELECT ?X WHERE {?X rdf:type ub:Publication . ?X ub:publicationAuthor ?author. ?author rdf:type ub:AssociateProfessor .}";
    }

}