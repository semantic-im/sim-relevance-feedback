package rfjava;

import java.io.File;
//import java.io.FileOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
//import weka.core.converters.ArffSaver;
import weka.core.converters.ArffSaver;
import weka.core.tokenizers.WordTokenizer;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class DataPreprocessor implements Serializable{
        private int noInstances=0;
        private int noAttributes=0;
        private String attrNames[]=null;
	private Instances initialData;

	private Instances transformedData;
	
	public void loadInitialData(String fileName) throws IOException
	{
		 ArffLoader loader = new ArffLoader();
		 try {
			loader.setFile(new File(fileName));
			initialData = loader.getDataSet();
			initialData.setClassIndex(-1);
                        noAttributes= initialData.numAttributes();
                        noInstances = initialData.numInstances();
                        attrNames= new String[noAttributes];
                        int i;
                        for (i=0;i<noAttributes;i++)
                            attrNames[i]=initialData.attribute(i).name();

                        transformedData=new Instances(initialData);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
                        throw e;
		}
	}

	public void setInitialData(Instances initialData)
	{
		this.initialData = initialData;
		this.transformedData=new Instances(initialData);
	}

        public void resetInitialData()
        {
		this.transformedData=new Instances(initialData);
        }
	
	public Instances getTransformedData()
	{
		return transformedData;
	}	

	public Instances getInitialData()
	{
		return initialData;
	}

        public int getNoAttributes()
        {
            return noAttributes;
        }

        public int getNoInstances()
        {
            return noInstances;
        }

        public String[] getAttrNamesList()
        {
            return attrNames;
        }

        public boolean isNumericAttribute(int attrNo)
        {
            return initialData.attribute(attrNo).type()==Attribute.NUMERIC;
        }

        public static double twoDecimalDouble(double x)
        {
            DecimalFormat twoDForm = new DecimalFormat("#############");
            DecimalFormatSymbols ds = new DecimalFormatSymbols();
            ds.setDecimalSeparator('.');
            twoDForm.setDecimalFormatSymbols(ds);
            twoDForm.setMaximumFractionDigits(2);
            return Double.valueOf(twoDForm.format(x));
        }

        public String getStringInstanceValue(int instanceNo, int attrNo)
        {
            String result="";
            if (isNumericAttribute(attrNo))
                result=String.valueOf(initialData.instance(instanceNo).value(attrNo));
            else
                result=initialData.instance(instanceNo).stringValue(attrNo);
            return result;
        }


        public double computeMinValue(int attrNo)
        {
            double min=initialData.instance(0).value(attrNo);
            for (int i=1;i<initialData.numInstances();i++)
            {
                if (initialData.instance(i).value(attrNo)<min)
                    min=initialData.instance(i).value(attrNo);
            }
            return twoDecimalDouble(min);
        }

        public double computeMaxValue(int attrNo)
        {
            double max=initialData.instance(0).value(attrNo);
            for (int i=1;i<initialData.numInstances();i++)
            {
                if (initialData.instance(i).value(attrNo)>max)
                    max=initialData.instance(i).value(attrNo);
            }
            return twoDecimalDouble(max);
        }

        public double computeMeanValue(int attrNo)
        {
            double mean=0;
            for (int i=1;i<initialData.numInstances();i++)
            {
                    mean+=initialData.instance(i).value(attrNo);
            }
            mean/=initialData.numInstances();

            return twoDecimalDouble(mean);
        }

        public double computeStdDevValue(int attrNo)
        {
            double mean=computeMeanValue(attrNo);
            double stdDev=0;
            for (int i=1;i<initialData.numInstances();i++)
            {
                    stdDev+=Math.pow(initialData.instance(i).value(attrNo)-mean,2);
            }
            stdDev=Math.sqrt(stdDev/initialData.numInstances());
            return twoDecimalDouble(stdDev);
        }


	public void tokenizeAttribute(String attrName, String delimiters, String prefix)
	{
		Attribute a = transformedData.attribute(attrName);
		tokenizeAttribute(a.index()+1, delimiters, prefix);
	}
	
	public void tokenizeAttribute(int idx, String delimiters, String prefix)
	{
		if (idx<=transformedData.numAttributes()-1)
		{
			int wordsToKeep = 1000;
			
			StringToWordVector filter = new StringToWordVector(wordsToKeep);
			
			WordTokenizer wT= new WordTokenizer();
			wT.setDelimiters(delimiters);
			filter.setTokenizer(wT);
			filter.setAttributeIndices(Integer.toString(idx));
			filter.setLowerCaseTokens(true);
			filter.setOutputWordCounts(false);
			filter.setAttributeNamePrefix(prefix);
			
			try {
				filter.setInputFormat(transformedData);
				transformedData = weka.filters.Filter.useFilter(transformedData,filter); 
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void tokenizeAttributes(String[] attrNameList, String delimiters, String prefix)
	{
		String attrIndices="";
		for (int i=0; i<attrNameList.length; i++)
		{
			Attribute a = transformedData.attribute(attrNameList[i]);
			if (i!=0) attrIndices+=","; 
			attrIndices+=(a.index()+1);
		}
		tokenizeAttributes(attrIndices, delimiters, prefix);
	}
	
	public void tokenizeAttributes(String attrNoList, String delimiters, String prefix)
	{
		int wordsToKeep = 1000;
		
		StringToWordVector filter = new StringToWordVector(wordsToKeep);
		
		WordTokenizer wT= new WordTokenizer();
		wT.setDelimiters(delimiters);
		filter.setTokenizer(wT);
		filter.setAttributeIndices(attrNoList);
		filter.setLowerCaseTokens(true);
		filter.setOutputWordCounts(false);
		filter.setAttributeNamePrefix(prefix);
		
		try {
			filter.setInputFormat(transformedData);
			transformedData = weka.filters.Filter.useFilter(transformedData,filter); 
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public static void outputArffInstances(Instances in, String filename)
	{
	    ArffSaver saver = new ArffSaver();
	    saver.setInstances(in);
	    try {
	    	FileOutputStream fs = new FileOutputStream(filename);
	    	saver.setDestination(fs);
		    saver.writeBatch();
		    fs.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

        public static Instances selectSubsetAttributes(Instances in, String attrIndices)
        {
            	Instances out= new Instances(in);

                Remove rFilter = new Remove();
		rFilter.setInvertSelection(true);
		rFilter.setAttributeIndices(attrIndices);
		try {
			rFilter.setInputFormat(in);
			out = Filter.useFilter(in, rFilter);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
                
                return out;
        }


}
