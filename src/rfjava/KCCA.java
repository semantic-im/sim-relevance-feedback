/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rfjava;

import Jama.Matrix;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.EVD;
import no.uib.cipr.matrix.NotConvergedException;
import edu.rit.pj.IntegerForLoop;
import edu.rit.pj.ParallelRegion;
import edu.rit.pj.ParallelTeam;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


/**
 *
 * @author adina
 */
public class KCCA implements Serializable{
    
    DataPreprocessor dp = null;
    DataPreprocessor dpp = null;
    DataPreprocessor dpw = null;
    
    private int rows;
    private int xVars;
    private int yVars;
    private float normX;
    private float normY;
    private int dimensions;
    public Matrix X;
    private Matrix Y;
    private Matrix KxBkp;
    private Matrix LyBkp;
    private Matrix result;
    private Matrix alfa;
    private Matrix beta;
    private Matrix XProj;
    private Matrix YProj;
    private Matrix firstLCoefficientsAlfa;
    public boolean isTrained = false;
    private ArrayList<String> testAttributeNames = new ArrayList<String>();
    
    private float[][] results;
    private float[][] actual;
    private int noTestInstances;

    //for parallel execution
    private Matrix Kx;
    private Matrix Ly;
    private Matrix eigenMat;
    private Matrix partialResult;
    private Matrix alfaCoefficients;
    private Matrix betaCoefficients;
    private Matrix firstLCoefficientsBeta;
    private Matrix nb1;
    private Matrix nb2;
    private Matrix nb3;
    private Matrix smallestNeighbor;
    private transient DenseMatrix eigenMatMtj;
    private int eigenMatrixRows;
    private int eigenMatrixCols;
    private float k = 1.0f/1000.0f;
    private double[] eigenVal;
    private float[] resultsTest;
    
    //know what scenario to predict
    int whichScenario;
    
    private float beforeTime;
    private float afterTime;
    
    static final long serialVersionUID = 4633755685578142038L;
    
    String[] queryWorkflowCorrespondence;
    String[] optimalWorkflow;
    ArrayList<String> pluginNames;
    private transient SelectAttributesDatabase sel;
    
    
    /**
     * @return the testAttributeNames
     */
    public ArrayList<String> getTestAttributeNames() {
        return testAttributeNames;
    }
    
    public void initPredictionModel(int scenario, SqlToWekaExtractor sw, String dateBegin, String dateEnd) throws Exception
    {
        beforeTime = System.currentTimeMillis();
        sel = new SelectAttributesDatabase(sw);
        switch(scenario)
        {
            case 1:
                sel.selectFirstScenario(dateBegin, dateEnd);
                whichScenario = 1;
                break;
            case 2:
                sel.selectSecondScenario(dateBegin,dateEnd);
                whichScenario = 2;
                break;
            default:
                break;
        }
        
        testAttributeNames = sel.getOutputAttrNames();
        
        this.xVars = sel.getInputSize();
        this.yVars = sel.getOutputSize();
        this.rows = sel.getInstanceNo();
        
        try
        {
            FileWriter fstream = new FileWriter("Input.txt");
            BufferedWriter out = new BufferedWriter(fstream);
            
            for (int i=0;i<sel.getInstanceNo();i++)
            {
                for (int j=0;j<sel.getInputSize();j++)
                {
                    out.write(sel.getInputAttributes()[i][j]+" ");
                }
                out.write("\n");
            }
            out.close();
        }
        catch(Exception e)
        {
            System.err.println("Error: " + e.getMessage());
        }
        try
        {
            FileWriter fstream = new FileWriter("Output.txt");
            BufferedWriter out = new BufferedWriter(fstream);
            
            for (int i=0;i<sel.getInstanceNo();i++)
            {
                for (int j=0;j<sel.getOutputSize();j++)
                {
                    out.write(sel.getOutputAttributes()[i][j]+" ");
                }
                out.write("\n");
            }
            out.close();
        }
        catch(Exception e)
        {
            System.err.println("Error: " + e.getMessage());
        }
        
        //X - input vars, Y - output vars
        this.X = new Matrix(sel.getInstanceNo(), sel.getInputSize());
        this.Y = new Matrix(sel.getInstanceNo(), sel.getOutputSize());
        
        //populate the X and Y matrices
        for (int i = 0; i < sel.getInstanceNo(); i++)
            for (int j = 0; j < sel.getInputSize(); j++)
                this.X.set(i, j, sel.getInputAttributes()[i][j]);
        for (int i = 0; i < sel.getInstanceNo(); i++)
            for (int j = 0; j < sel.getOutputSize(); j++)
                this.Y.set(i, j, sel.getOutputAttributes()[i][j]);
        
        this.normX = empiricalVariance(this.X);
        this.normY = empiricalVariance(this.Y);
        
        System.out.println("Norm X: "+this.normX);
        System.out.println("Norm Y: "+this.normY);
        
        queryWorkflowCorrespondence = new String[sel.getInstanceNo()];
        queryWorkflowCorrespondence = sel.getQueryWorkflow();
        
        pluginNames = sel.getPluginNames();
        System.out.println("Done with init");
    }
    
    /*
    * Compute the norm of a vector.
    * arr = row vector
    */
    private double norm(Matrix arr)
    {
        float sum = 0;
        
        for (int i = 0; i < arr.getColumnDimension(); i++)
            sum += arr.get(0, i)*arr.get(0, i);
        
        return Math.sqrt(sum);
    }
    
    /*
     * Compute the empirical variance of a dataset stored in a matrix.
     */
    private float empiricalVariance(Matrix mat)
    {
        //compute the norms of each datapoint
        //datapoints on rows
        float[] norms = new float[this.rows];
        
        Matrix arr = new Matrix(1, mat.getColumnDimension());
        
        //save the norm of each datapoint in its location
        for (int i = 0; i < mat.getRowDimension(); i++)
        {
            arr = mat.getMatrix(i, i , 0, mat.getColumnDimension()-1);
            norms[i] = (float)norm(arr);
        }
        
        //compute the mean of the norms
        float mean = 0;
        for (int i = 0; i < mat.getRowDimension(); i++)
            mean += norms[i];
        
        mean /= mat.getRowDimension();
        
        //compute the empirical variance
        float sum = 0;
        
        for (int i = 0; i < mat.getRowDimension(); i++)
            sum += (norms[i]-mean)*(norms[i]-mean);
        
        sum /= mat.getRowDimension();
        
        return sum;
        }
    
    /*
     * Gaussian kernel.
     */
    private double gaussianKernel(Matrix x1, Matrix x2, int noVars, float variance)
    {
        float norm = 0;
        
        for (int i = 0; i < noVars; i++)
            norm += (x1.get(0, i)-x2.get(0, i)) * (x1.get(0, i)-x2.get(0, i));
        
        return Math.exp(-norm/variance);
    }
    
    /*
     * Euclidean distance.
     */
    private double euclideanDistance(Matrix p, Matrix q)
    {
        float sum = 0;
        
        for (int i = 0; i < p.getRowDimension(); i++)
            sum += (p.get(i, 0)-q.get(i, 0))*(p.get(i, 0)-q.get(i, 0));
        
        return Math.sqrt(sum);
    }
    
    /*
     * Compute the simmilarity matrix for a new instance.
     */
    
    private void computeSimmilarityMatrix(Matrix simMatrix, Matrix testInstance)
    {
        float firstTerm = 0, secondTerm = 0, thirdTerm = 0, fourthTerm = 0;
        
        float tmp_val;
        
        //second term - does not depend on i (i = current row in the simm matrix)
	for (int k = 0; k < this.rows; k++)
	{
            secondTerm += gaussianKernel(X.getMatrix(k, k, 0, X.getColumnDimension()-1),testInstance,this.xVars,this.normX);
	}
	secondTerm = secondTerm/this.rows;
        
        //fourth term - does not depend on i (i = current row in the simm matrix)
	for (int k = 0; k < this.rows; k++)
		for (int l = 0; l < this.rows; l++)
		{
                    fourthTerm += gaussianKernel(X.getMatrix(k, k, 0, X.getColumnDimension()-1),X.getMatrix(l, l, 0, X.getColumnDimension()-1),this.xVars,this.normX);			
		}
	fourthTerm = fourthTerm/(this.rows*this.rows);
        
        //the simmilarity matrix has n = rows (global variable) rows
	for (int i = 0; i < this.rows; i++)
	{
            firstTerm = thirdTerm = 0;

            //first term
            firstTerm = (float)gaussianKernel(X.getMatrix(i, i, 0, X.getColumnDimension()-1),testInstance,this.xVars,this.normX);
            

            //third term
            for (int k = 0; k < this.rows; k++)
            {
                thirdTerm += gaussianKernel(X.getMatrix(i, i, 0, X.getColumnDimension()-1),X.getMatrix(k, k, 0, X.getColumnDimension()-1),this.xVars,this.normX);
            }
            thirdTerm = thirdTerm/this.rows;

            //set the value of the current row in the simmilarity matrix
            tmp_val = firstTerm-secondTerm-thirdTerm+fourthTerm;
            
            simMatrix.set(i, 0, tmp_val);
	}
    }
    
    /*
     * Compute the projection vectors for the new space.
     */
    public void computeKCCA() throws Exception
    {   
        /*
         * Compute the kernel matrices.
         */
        Kx = new Matrix(this.rows, this.rows);
        Ly = new Matrix(this.rows, this.rows);
                         
    
        //Kx
        new ParallelTeam().execute(new ParallelRegion()
        {
            public void run() throws Exception
            {
                execute(0, rows-1, new IntegerForLoop()
                {
                    public void run (int first, int last)
                    {
                        for (int i = first; i <= last; ++i)
                        {
                            for (int j = 0; j < rows; j++) 
                            {
                                Kx.set(i, j, gaussianKernel(X.getMatrix(i, i, 0, X.getColumnDimension()-1),X.getMatrix(j, j, 0, X.getColumnDimension()-1),xVars, normX));
                            }
                        }
                    }
                });
            }
        });
       
        System.out.println("Done init Kx");
        
        //Standardize Kx
        Matrix In = new Matrix(Kx.getRowDimension(), Kx.getColumnDimension());
        for (int i = 0; i < In.getRowDimension(); i++)
            for (int j = 0; j < In.getColumnDimension(); j++)
                In.set(i, j, 1.0/In.getColumnDimension());
        
        /*Matrix InKx = new Matrix(Kx.getRowDimension(), Kx.getColumnDimension());
        Matrix InKxIn = new Matrix(Kx.getRowDimension(), Kx.getColumnDimension());
        Matrix KxIn = new Matrix(Kx.getRowDimension(), Kx.getColumnDimension());*/
        Matrix partial = new Matrix(Kx.getRowDimension(), Kx.getColumnDimension());
        Matrix r = new Matrix(Kx.getRowDimension(), Kx.getColumnDimension());
        
        /*InKx = In.times(Kx);
        KxIn = Kx.times(In);
        InKxIn = InKx.times(In);
        
        partial = Kx.minus(InKx);
        r = partial.minus(KxIn);
        
        Kx = r.plus(InKxIn);*/
        
        partial = Kx.minus(In.times(Kx));
        r = partial.minus(Kx.times(In));
        partial = r.plus(In.times(Kx).times(In));
        Kx = partial.copy();
        
        System.out.println("Done with Kx");

        //Ly
        new ParallelTeam().execute(new ParallelRegion()
        {
            public void run() throws Exception
            {
                execute(0, rows-1, new IntegerForLoop()
                {
                    public void run (int first, int last)
                    {
                        for (int i = first; i <= last; ++i)
                        {
                            for (int j = 0; j < rows; j++) 
                            {
                                Ly.set(i, j, gaussianKernel(Y.getMatrix(i, i, 0, Y.getColumnDimension()-1),Y.getMatrix(j, j, 0, Y.getColumnDimension()-1),yVars, normY));
                            }
                        }
                    }
                });
            }
        });
        
        System.out.println("Done init Ly");
        
        //Standardize Ly
        /*InKx = In.times(Ly);
        KxIn = Ly.times(In);
        InKxIn = InKx.times(In);
        
        partial = Ly.minus(InKx);
        r = partial.minus(KxIn);
        
        Ly = r.plus(InKxIn);*/
        partial = Ly.minus(In.times(Ly));
        r = partial.minus(Ly.times(In));
        partial = r.plus(In.times(Ly).times(In));
        
        Ly = partial.copy();
        
        System.out.println("Done with Ly");
        
        partial = null;
        r = null;
        
        //Backups
        this.KxBkp = new Matrix(Kx.getRowDimension(), Kx.getColumnDimension());
        this.LyBkp = new Matrix(Ly.getRowDimension(), Ly.getColumnDimension());
        
        new ParallelTeam().execute(new ParallelRegion()
        {
            public void run() throws Exception
            {
                execute(0, rows-1, new IntegerForLoop()
                {
                    public void run(int first, int last)
                    {
                        for (int i = first; i <= last; ++i)
                            for (int j = 0; j < rows; j++)
                            {
                                KxBkp.set(i, j, Kx.get(i, j));
                                LyBkp.set(i, j, Ly.get(i, j));
                            }
                    }
                });
            }
        });
        
        /*
         * Shift the diagonal of Kx and Ly with a very small k
         */
        new ParallelTeam().execute(new ParallelRegion()
        {
            public void run() throws Exception
            {
                execute (0, rows-1, new IntegerForLoop()
                {
                    public void run(int first, int last)
                    {
                        for (int i = first; i <= last; ++i)
                        {
                            Kx.set(i, i, Kx.get(i, i)+k);
                            Ly.set(i, i, Ly.get(i, i)+k);
                        }
                    }
                });
            }
        });
       
        /*
         * Compute the canonical matrix.
         */
        Matrix inverseKx = new Matrix(Kx.getRowDimension(), Kx.getColumnDimension());
        Matrix inverseLy = new Matrix(Kx.getRowDimension(), Kx.getColumnDimension());
        
        inverseKx = Kx.inverse();
        inverseLy = Ly.inverse();
        
        this.result = new Matrix(Kx.getRowDimension(), Kx.getColumnDimension());
        this.result = inverseKx.times(this.LyBkp);
        
        Matrix rresult = new Matrix(this.result.getRowDimension(), this.result.getColumnDimension());
        rresult = this.result.times(inverseLy);
        this.result = rresult.times(this.KxBkp);
        
        System.out.println("Done with canonical matrix");
        
        
        /*
         * Compute the eigenvalues and eigenvectors of the canonical matrix.
         */
        //EigenvalueDecomposition eid = this.result.eig();
        //float[] eigenVal = eid.getRealEigenvalues();
        eigenMat = new Matrix(this.result.getRowDimension(), this.result.getColumnDimension());
        //eigenMat = eid.getV();
        
        /*System.out.println("Matrix eigenMat");
        displayMatrix(eigenMat);*/
        
        //ADDED
        DenseMatrix mtr = new DenseMatrix(result.getRowDimension(),result.getColumnDimension());
        
        for (int i = 0; i < this.result.getRowDimension(); i++)
            for (int j = 0; j < this.result.getColumnDimension(); j++)
                mtr.set(i, j, this.result.get(i, j));
        
        EVD ev = new EVD(result.getRowDimension());
        try {
            ev.factor(mtr);
            //END ADDED
        } catch (NotConvergedException ex) {
            Logger.getLogger(KCCA.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        eigenVal = ev.getRealEigenvalues();
        eigenMatMtj = ev.getLeftEigenvectors();
        
        eigenMatrixRows = eigenMatMtj.numRows();
        eigenMatrixCols = eigenMatMtj.numColumns();
        
        new ParallelTeam().execute(new ParallelRegion()
        {
            public void run() throws Exception
            {
                execute (0, eigenMatrixRows-1, new IntegerForLoop()
                {
                    public void run(int first, int last)
                    {
                        for (int i = first; i <= last; ++i)
                            for (int j = 0; j  < eigenMatrixCols; j++)
                                eigenMat.set(i, j, eigenMatMtj.get(i, j));
                    }
                });
            }
        });
        
        System.out.println("Done with eigen");
        
        /*
	* Sort the eigenvalues in ascending order.
	*/
	int ok = 0;
	float aux;
	float auxVec;

	while (ok == 0)
	{
            ok = 1;
            for (int i = 0; i < this.result.getRowDimension()-1; i++)
                if (eigenVal[i] > eigenVal[i+1])
                {
                    aux = (float)eigenVal[i];
                    eigenVal[i] = eigenVal[i+1];
                    eigenVal[i+1] = aux;

                    for (int j = 0; j < this.result.getRowDimension(); j++)
                    {
                            auxVec = (float)eigenMat.get(j, i);
                            eigenMat.set(j, i, eigenMat.get(j, i+1));
                            eigenMat.set(j, i+1, auxVec);
                    }
                    ok = 0;
                }
	}
        
        System.out.println("Done with sorting eigen");
        
        /*
         * Compute the coefficients of X (alfa) and Y (beta)
         */
        this.alfa = new Matrix(this.result.getRowDimension(), this.result.getColumnDimension());
        this.beta = new Matrix(this.result.getRowDimension(), this.result.getColumnDimension());
        
        //Alfa = eigenvectors
        this.alfa = eigenMat.copy();
        
        /*System.out.println("Matrix alfa");
        displayMatrix(this.alfa);*/
        
        System.out.println("Done with alfa");
        
        //Beta
	partialResult = new Matrix(this.result.getRowDimension(),this.result.getColumnDimension());
        partialResult = inverseLy.times(this.KxBkp);
        
        new ParallelTeam().execute(new ParallelRegion()
        {
            public void run() throws Exception
            {
                execute (0, result.getColumnDimension()-1, new IntegerForLoop()
                {
                    //thread-local variables
                    Matrix currentBeta = new Matrix(result.getRowDimension(),1);
                    public void run(int first, int last)
                    {
                        for (int j = first; j <= last; ++j)
                        {
                            //(L+In)^-1 * K * currentAlfa(the eigenvector from each column in alfa)
                            currentBeta = partialResult.times(alfa.getMatrix(0, alfa.getRowDimension()-1, j, j));

                            //currentBeta/current eigenvalue
                            for (int m = 0; m < currentBeta.getRowDimension(); m++)
                                for (int n = 0; n < currentBeta.getColumnDimension(); n++)
                                {
                                    currentBeta.set(m, n, currentBeta.get(m, n)/eigenVal[j]);
                                    beta.set(m, j, currentBeta.get(m, 0));
                                }            
                        }
                    }
                });
            }
        });
        
        System.out.println("Done with beta");
        
        /*System.out.println("Matrix beta");
        displayMatrix(this.beta); */
               
        /*
         * Project the initial data onto the new dimensions.
         */
        
        //Projection of X
        this.dimensions = 3;
        this.XProj = new Matrix(this.dimensions, this.result.getRowDimension());
        
        //Save the coefficients on rows (columns of alfa -> rows of alfaCoefficients)
        alfaCoefficients = new Matrix(this.alfa.getRowDimension(), this.alfa.getColumnDimension());
        alfaCoefficients = this.alfa.transpose();
        this.firstLCoefficientsAlfa = new Matrix(this.dimensions, this.alfa.getColumnDimension());
        
        new ParallelTeam().execute(new ParallelRegion()
        {
            public void run() throws Exception
            {
                for (int i = alfaCoefficients.getRowDimension()-1; i>=alfaCoefficients.getRowDimension()-dimensions; i--)
                {  
                    final int ii = i;
                    execute (0, firstLCoefficientsAlfa.getColumnDimension() - 1, new IntegerForLoop()
                    {
                        public void run (int first, int last)
                        {
                            for (int j = first; j <= last; ++j)
                                firstLCoefficientsAlfa.set(alfaCoefficients.getRowDimension()-ii-1, j, alfaCoefficients.get(ii, j));
                        }
                    });
                }
            }
        });
        
        this.XProj = this.firstLCoefficientsAlfa.times(this.KxBkp);
        
        System.out.println("Done with X proj");
        
        //Projection of Y
        this.YProj = new Matrix(this.dimensions, this.result.getRowDimension());
        
        //Save the coefficients on rows (columns of beta -> rows of betaCoefficients)
        betaCoefficients = new Matrix(this.beta.getRowDimension(), this.beta.getColumnDimension());
        betaCoefficients = this.beta.transpose();
        
        firstLCoefficientsBeta = new Matrix(this.dimensions, this.beta.getColumnDimension());
        
        new ParallelTeam().execute(new ParallelRegion()
        {
            public void run() throws Exception
            {
                for (int i = betaCoefficients.getRowDimension()-1; i>=betaCoefficients.getRowDimension()-dimensions; i--)
                {
                    final int ii = i;
                    execute(0, firstLCoefficientsBeta.getColumnDimension() - 1, new IntegerForLoop()
                    {
                        public void run(int first, int last)
                        {
                            for (int j = first; j <= last; ++j)
                                firstLCoefficientsBeta.set(betaCoefficients.getRowDimension()-ii-1, j, betaCoefficients.get(ii, j));
                        }
                    });
                }
            }
        });
        
        this.YProj = firstLCoefficientsBeta.times(this.LyBkp);
        
        System.out.println("Done with Y proj");
        
        try
        {
            FileWriter fstream = new FileWriter("out.txt",true);
            BufferedWriter out = new BufferedWriter(fstream);
            
            for (int i=0;i<XProj.getRowDimension();i++)
            {
                for (int j=0;j<XProj.getColumnDimension();j++)
                {
                    out.write(XProj.get(i, j)+" ");
                }
                out.write("\n");
            }
            
            for (int i=0;i<YProj.getRowDimension();i++)
            {
                for (int j=0;j<YProj.getColumnDimension();j++)
                {
                    out.write(YProj.get(i, j)+" ");
                }
                out.write("\n");
            }      
            out.close();
        }
        catch(Exception e)
        {
            System.err.println("Error: " + e.getMessage());
        }
        
        
        afterTime = System.currentTimeMillis();
        System.out.println("Total training time:" + (afterTime-beforeTime));
    }   
        
    /*
     * Save the predictor info and input/projected data to a file.
     */
    public void saveModel(String fileName)
    {
        try {
                ObjectOutput out = new ObjectOutputStream(new FileOutputStream(fileName));
                out.writeObject(this);
                out.close();
            } catch (IOException e) 
            {
                e.printStackTrace();
            }
        
    }
    
    /*
     * Load the predictor info from a file.
     */
    public static KCCA loadModel(String fileName)
    {
        try {
              File file = new File(fileName);
              ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
              KCCA myKCCA = (KCCA) in.readObject();
              in.close();
              return myKCCA;
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
               return null;
        } catch (IOException e) {
            e.printStackTrace();
                return null;
        }
    }
    
    public String predictTestQuery(String query, SqlToWekaExtractor sw, String application, String workflow) throws Exception
    {
        Matrix test = new Matrix(1, this.xVars);
        SPARQLQueryParser parser = new SPARQLQueryParser();
        parser.parseQuery(query);
        
        int noFeature = 0;
        
        /*
         * Save features extracted from the text of the query
         */
        double[] values = parser.generateParseResults();
        for (int i = 0; i < values.length; i++)
            test.set(0, noFeature++, values[i]);
        
        /*
         * Save platform metrics
         */
        if (X.getColumnDimension() == 12)
        {
            sel = new SelectAttributesDatabase(sw);
            ArrayList<Double> pm = sw.getLatestPlatformMetrics(sel.getDesiredPlatformAttributes());
            for (int i = 0; i < pm.size(); i++)
                test.set(0, noFeature++, pm.get(i));
        }
        
        /*
         * Save system metrics
         */
        if (X.getColumnDimension() == 19)
        {
            sel = new SelectAttributesDatabase(sw);
            ArrayList<Double> sm = sw.getLatestSystemMetrics(sel.getDesiredSystemAttributes());
            for (int i = 0; i < sm.size(); i++)
                test.set(0, noFeature++, sm.get(i));
        }
        
        /*
         * Save application id
         */
        //test.set(0, noFeature++, sel.getAppNames().indexOf(application) + 1);
        
        /*
         * If second scenario - save workflow id
         */
         //test.set(0, noFeature++, sel.getWorkflowNames().indexOf(workflow)+1);
        
        float[] predResults = predictKCCA(test);
        
        String ret = new String();
        for (int i = 0; i < testAttributeNames.size(); i++)
            ret = ret.concat(testAttributeNames.get(i)+" "+predResults[i]+"\n");
        
        return ret;
    }
    
    public String configurationTestQuery(String query, SqlToWekaExtractor sw, String application) throws Exception
    {
        Matrix test = new Matrix(1, this.xVars);
        SPARQLQueryParser parser = new SPARQLQueryParser();
        parser.parseQuery(query);
        
        int noFeature = 0;
        
        /*
         * Save features extracted from the text of the query
         */
        double[] values = parser.generateParseResults();
        for (int i = 0; i < values.length; i++)
            test.set(0, noFeature++, values[i]);
        
        /*
         * Save platform metrics
         */
        if (X.getColumnDimension() == 12)
        {
            sel = new SelectAttributesDatabase(sw);
            ArrayList<Double> pm = sw.getLatestPlatformMetrics(sel.getDesiredPlatformAttributes());
            for (int i = 0; i < pm.size(); i++)
                test.set(0, noFeature++, pm.get(i));
        }
        
        /*
         * Save system metrics
         */
        if (X.getColumnDimension() == 19)
        {
            sel = new SelectAttributesDatabase(sw);
            ArrayList<Double> sm = sw.getLatestSystemMetrics(sel.getDesiredSystemAttributes());
            for (int i = 0; i < sm.size(); i++)
                test.set(0, noFeature++, sm.get(i));
        }
        
        /*
         * Save application id
         */
        //test.set(0, noFeature++, sel.getAppNames().indexOf(application) + 1);
        
        optimalWorkflow = new String[1];
        
        float[] predResults = optimalConfiguration(test, 0);
        
        String ret = new String();
        ret = ret.concat("The optimal workflow is: "+optimalWorkflow[0]+"\n\n");
        for (int i = 0; i < testAttributeNames.size(); i++)
            ret = ret.concat(testAttributeNames.get(i)+" "+predResults[i]+"\n");
        
        return ret;
    }
    
    /*
     * Predict the value for a new query - classical scenario.
     */
    public float[] predictKCCA(Matrix testInstance) throws Exception
    {
        resultsTest = new float[this.yVars];

        //distances from the test point to 
        //each of the points in the training set
        Map distancesMap = new TreeMap();      

        //three nearest neighbors
        nb1 = new Matrix(1, this.yVars);
        nb2 = new Matrix(1, this.yVars);
        nb3 = new Matrix(1, this.yVars);
        
        smallestNeighbor = new Matrix(1,this.yVars); 

        Matrix similarityMatrix = new Matrix(this.rows, 1);
        Matrix testInstanceProj = new Matrix(this.dimensions, 1);

        //compute the projection of the test point
        this.computeSimmilarityMatrix(similarityMatrix, testInstance);
        testInstanceProj = this.firstLCoefficientsAlfa.times(similarityMatrix);
        
        //compute the distance from the test instance to each of the datapoints
        Matrix currentColumn = new Matrix(this.XProj.getRowDimension(), 1);

        for (int i = 0; i < this.XProj.getColumnDimension(); i++)
        {
            currentColumn = this.XProj.getMatrix(0, this.XProj.getRowDimension()-1, i, i);
            distancesMap.put(euclideanDistance(currentColumn, testInstanceProj), i);
        }
        
        //first 3 keys - smallest distances
        Set keySet = distancesMap.keySet();
        Iterator it = keySet.iterator();
        Double[] val = new Double[3];
        val[0] = (Double)it.next();
        val[1] = (Double)it.next();
        val[2] = (Double)it.next();
        
        //find the lines in Y that correspond to the 3 nearest neighbors
        nb1 = this.Y.getMatrix((Integer)distancesMap.get(val[0]), (Integer)distancesMap.get(val[0]), 0, this.Y.getColumnDimension()-1);
        nb2 = this.Y.getMatrix((Integer)distancesMap.get(val[1]), (Integer)distancesMap.get(val[1]), 0, this.Y.getColumnDimension()-1);
        nb3 = this.Y.getMatrix((Integer)distancesMap.get(val[2]), (Integer)distancesMap.get(val[2]), 0, this.Y.getColumnDimension()-1);
        
        double min = nb1.get(0, 1);
        smallestNeighbor = nb1.copy();
        if (nb2.get(0, 1) < min)
        {
            min = nb2.get(0, 1);
            smallestNeighbor = nb2.copy();
        }
        if (nb3.get(0, 1) < min)
        {
            min = nb3.get(0, 1);
            smallestNeighbor = nb3.copy();
        }
        
        //compute the mean of the corresponding elements and store the partial result
        new ParallelTeam().execute(new ParallelRegion()
        {
            public void run() throws Exception
            {
                execute(0, nb1.getColumnDimension() - 1, new IntegerForLoop()
                {
                    public void run(int first, int last)
                    {
                        for (int i = first; i <= last; ++i)
                        {
                            resultsTest[i] = (float)(smallestNeighbor.get(0, i));/*+nb2.get(0, i))/2;/*+nb3.get(0, i))/3;*/
                        }
                    }
                });
            }
        });
        
        return resultsTest;
    }
    
    /*
     * Predict the value for a new query and propose an optimal configuration
     */
    public float[] optimalConfiguration(Matrix testInstance, int wfIndex) throws Exception
    {
         resultsTest = new float[this.yVars];

        //distances from the test point to 
        //each of the points in the training set
        Map distancesMap = new TreeMap();      

        //three nearest neighbors
        nb1 = new Matrix(1, this.yVars);
        nb2 = new Matrix(1, this.yVars);
        nb3 = new Matrix(1, this.yVars);
        
        smallestNeighbor = new Matrix(1,this.yVars); 

        Matrix similarityMatrix = new Matrix(this.rows, 1);
        Matrix testInstanceProj = new Matrix(this.dimensions, 1);

        //compute the projection of the test point
        this.computeSimmilarityMatrix(similarityMatrix, testInstance);
        testInstanceProj = this.firstLCoefficientsAlfa.times(similarityMatrix);
        
        //compute the distance from the test instance to each of the datapoints
        Matrix currentColumn = new Matrix(this.XProj.getRowDimension(), 1);

        for (int i = 0; i < this.XProj.getColumnDimension(); i++)
        {
            currentColumn = this.XProj.getMatrix(0, this.XProj.getRowDimension()-1, i, i);
            distancesMap.put(euclideanDistance(currentColumn, testInstanceProj), i);
        }
        
        //first 3 keys - smallest distances
        Set keySet = distancesMap.keySet();
        Iterator it = keySet.iterator();
        Double[] val = new Double[3];
        val[0] = (Double)it.next();
        val[1] = (Double)it.next();
        val[2] = (Double)it.next();
        
        //find the lines in Y that correspond to the 3 nearest neighbors
        nb1 = this.Y.getMatrix((Integer)distancesMap.get(val[0]), (Integer)distancesMap.get(val[0]), 0, this.Y.getColumnDimension()-1);
        nb2 = this.Y.getMatrix((Integer)distancesMap.get(val[1]), (Integer)distancesMap.get(val[1]), 0, this.Y.getColumnDimension()-1);
        nb3 = this.Y.getMatrix((Integer)distancesMap.get(val[2]), (Integer)distancesMap.get(val[2]), 0, this.Y.getColumnDimension()-1);
        
        double min = nb1.get(0, 1);
        smallestNeighbor = nb1.copy();
        int index = (Integer)distancesMap.get(val[0]);
        if (nb2.get(0, 1) < min)
        {
            min = nb2.get(0, 1);
            smallestNeighbor = nb2.copy();
            index = (Integer)distancesMap.get(val[1]);
        }
        if (nb3.get(0, 1) < min)
        {
            min = nb3.get(0, 1);
            smallestNeighbor = nb3.copy();
            index = (Integer)distancesMap.get(val[2]);
        }
        
        //compute the mean of the corresponding elements and store the partial result
        new ParallelTeam().execute(new ParallelRegion()
        {
            public void run() throws Exception
            {
                execute(0, nb1.getColumnDimension() - 1, new IntegerForLoop()
                {
                    public void run(int first, int last)
                    {
                        for (int i = first; i <= last; ++i)
                        {
                            resultsTest[i] = (float)(smallestNeighbor.get(0, i));/*+nb2.get(0, i))/2;/*+nb3.get(0, i))/3;*/
                        }
                    }
                });
            }
        });
        
        optimalWorkflow[wfIndex] = queryWorkflowCorrespondence[index];
        
        return resultsTest;
    }
    
    public void setDataPreprocessor(DataPreprocessor dp)
    {
        this.dp=dp;
    }
    
    public void setPluginDataPreprocessor(DataPreprocessor dpp)
    {
        this.dpp = dpp;
    }
    
     public void setWorkflowDataPreprocessor(DataPreprocessor dpw)
    {
        this.dpw = dpw;
    }
    
    
    public static String testQuery()
    {
        return "PREFIX foaf: <http://xmlns.com/foaf/0.1/> SELECT ?name1 WHERE { ?person1 foaf:knows ?person2 . ?person1 foaf:name  ?name1 . ?person2 foaf:name  \"Ionel Giosan\" .   } ORDER BY ?name1";
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
    
    public void plotProjections()
    {
        //get the input projections
        double[][] matrixX = this.X.getArray();
        
        //get the output projections
        double[][] matrixY = this.Y.getArray();
        
        Plot myPlot = new Plot(matrixX[0], matrixX[1], matrixX[2],
                               matrixY[0], matrixY[1], matrixY[2]);
        
        myPlot.draw3DPlot();
    }
    
    public void plotPredictedVsActual(int index)
    {
        double[] x = new double[this.noTestInstances];
        double[] y = new double[this.noTestInstances];
        double[] z = new double[this.noTestInstances];
        
        for (int i = 0; i < this.noTestInstances; i++)
        {
            x[i] = i + 1;
            y[i] = getResults()[i][index];
            z[i] = getActual()[i][index];
        }
        
        Plot myPlot = new Plot(x,y,z);
        myPlot.draw2DPlot(testAttributeNames.get(index),Accuracy.rSquared(y, z), Accuracy.MAPE(y, z));
    }
    
    public void autoTest(SqlToWekaExtractor sw, String dateBegin, String dateEnd) throws Exception
    {
        sel = new SelectAttributesDatabase(sw, pluginNames);
        
        switch (whichScenario)
        {
            case 1:
                sel.selectFirstScenario(dateBegin, dateEnd);
                break;
            case 2:
                sel.selectSecondScenario(dateBegin, dateEnd);
                break;
            default:
                break;
        }
        
        this.noTestInstances = sel.getInstanceNo();
        this.results = new float[sel.getInstanceNo()][sel.getOutputSize()];
        this.actual = sel.getOutputAttributes();
  
        Matrix test = new Matrix(1, sel.getInputSize());
        
        //store the optimal workflow for each test point
        optimalWorkflow = new String[sel.getInstanceNo()];
        
        switch(whichScenario)
        {
            case 1:
                for (int i = 0; i < sel.getInstanceNo(); i++)
                {
                    for (int j = 0; j < sel.getInputSize(); j++)
                        test.set(0, j, sel.getInputAttributes()[i][j]);

                    results[i] = optimalConfiguration(test, i);
                    System.out.println("Processed instance " + i + " from "+this.noTestInstances + " opt config");
                }
                break;
                
            case 2:
                for (int i = 0; i < sel.getInstanceNo(); i++)
                {
                    for (int j = 0; j < sel.getInputSize(); j++)
                        test.set(0, j, sel.getInputAttributes()[i][j]);

                    results[i] = predictKCCA(test);
                    System.out.println("Processed instance " + i + " from "+this.noTestInstances+ " numerical pred");
                }
                break;          
        }
        
        try
        {   FileWriter fstream = new FileWriter("resultsWorkflow.txt");
            BufferedWriter out = new BufferedWriter(fstream);
            
            for (int i=0;i<sel.getInstanceNo();i++)
            {
                for (int j=0;j<sel.getOutputSize();j++)
                {
                    out.write(results[i][j]+" ");
                }
                out.write("Optimal workflow: "+optimalWorkflow[i]);
                out.write("\n");
            }      
            out.close();
        }
        catch(Exception e)
        {
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    /**
     * @return the results
     */
    public float[][] getResults() {
        return results;
    }

    /**
     * @return the actual
     */
    public float[][] getActual() {
        return actual;
    }
    
     public void printMatrixFile(Matrix mat, String file)
    {
        try
        {   FileWriter fstream = new FileWriter(file);
            BufferedWriter out = new BufferedWriter(fstream);
            
            for (int i=0;i<mat.getRowDimension();i++)
            {
                for (int j=0;j<mat.getColumnDimension();j++)
                {
                    out.write(mat.get(i, j)+" ");
                }
                out.write("\n");
            }      
            out.close();
        }
        catch(Exception e)
        {
            System.err.println("Error: " + e.getMessage());
        }
    }
     
     public void printMatrixDoubleFile(float[][] mat, String file, int h, int w)
    {
        try
        {   FileWriter fstream = new FileWriter(file);
            BufferedWriter out = new BufferedWriter(fstream);
            
            for (int i=0;i<h;i++)
            {
                for (int j=0;j<w;j++)
                {
                    out.write(mat[i][j]+" ");
                }
                out.write("\n");
            }      
            out.close();
        }
        catch(Exception e)
        {
            System.err.println("Error: " + e.getMessage());
        }
    }
     
     public boolean checkIfEqual(String fileA, String fileB) throws IOException
     {
        InputStream a = new BufferedInputStream(new FileInputStream(fileA)) ;
        InputStream b = new BufferedInputStream(new FileInputStream(fileB)) ;
        int byteA = 0 ;
        int byteB = 0 ;
        while ((byteA | byteB) >= 0) {
         byteA = a.read() ;
         byteB = b.read() ;
        if (byteA != byteB) return false ;
        }
        return byteA == byteB ;
     }
}
