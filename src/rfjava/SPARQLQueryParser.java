package rfjava;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryException;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.SortCondition;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.syntax.Element;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementOptional;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import java.util.ArrayList;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;

public class SPARQLQueryParser {
	
	private String QueryContent;
	
	private int QuerySizeInBytes;
	
	private int QueryNamespaceNb;	
	
	//for namespaces we will store for the moment the keys and the values in the PREFIX list; 
	// for example PREFIX foaf: <http://xmlns.com/foaf/0.1/> will result in QueryNamespaceKeys = foaf and 
	// QueryNamespaceValues = http://xmlns.com/foaf/0.1/
	private Set<String> QueryNamespaceValues;
	
	private int QueryVariablesNb;
	
	private int QueryDataSetSourcesNb;
	private List<String> QueryDataSetSources;

	private int QueryOperatorsNb;
	
	private int QueryLiteralsNb;
	
	private int QueryResultOrderingNb;
	
	private int QueryResultLimitNb;
	
	private int QueryResultOffsetNb;
	
	private int QuerySizeInTriples;
	
	private int QuerySizeInCharacters;
	
	public SPARQLQueryParser(){
		this.setQueryVariablesNb(0);		
		this.setQueryDataSetSourcesNb(0);
		this.setQueryOperatorsNb(0);
		this.setQueryResultOrderingNb(0);
		this.setQueryResultLimitNb(0);
		this.setQueryResultOffsetNb(0);
		this.setQuerySizeInTriples(0);
		this.setQuerySizeInCharacters(0);
		this.setQueryLiteralsNb(0);
	}

	/**
	 * @return the queryContent
	 */
	public String getQueryContent() {
		return QueryContent;
	}

	public void setQuerySizeInCharacters(int val){
		QuerySizeInCharacters = val;
	}
	
	public int getQuerySizeInCharacters(){
		return QuerySizeInCharacters;
	}
	
	public void setQueryLiteralsNb(int val){
		QueryLiteralsNb = val;
	}
	
	public int getQueryLiteralsNb(){
		return QueryLiteralsNb;
	}
	
	/**
	 * @param queryContent the queryContent to set
	 */
	public void setQueryContent(String queryContent) {
		QueryContent = queryContent;
	}

	/**
	 * @return the querySizeInBytes
	 */
	public int getQuerySizeInBytes() {
		return QuerySizeInBytes;
	}

	/**
	 * @param querySizeInBytes the querySizeInBytes to set
	 */
	public void setQuerySizeInBytes(int querySizeInBytes) {
		QuerySizeInBytes = querySizeInBytes;
	}

	/**
	 * @return the queryNamespaceNb
	 */
	public int getQueryNamespaceNb() {
		return QueryNamespaceNb;
	}

	/**
	 * @param queryNamespaceNb the queryNamespaceNb to set
	 */
	public void setQueryNamespaceNb(int queryNamespaceNb) {
		QueryNamespaceNb = queryNamespaceNb;
	}

	/**
	 * @return the queryVariablesNb
	 */
	public int getQueryVariablesNb() {
		return QueryVariablesNb;
	}

	/**
	 * @param queryVariablesNb the queryVariablesNb to set
	 */
	public void setQueryVariablesNb(int queryVariablesNb) {
		QueryVariablesNb = queryVariablesNb;
	}

	/**
	 * @return the queryDataSetSourcesNb
	 */
	public int getQueryDataSetSourcesNb() {
		return QueryDataSetSourcesNb;
	}

	/**
	 * @param queryDataSetSourcesNb the queryDataSetSourcesNb to set
	 */
	public void setQueryDataSetSourcesNb(int queryDataSetSourcesNb) {
		QueryDataSetSourcesNb = queryDataSetSourcesNb;
	}

	/**
	 * @return the queryDataSetSources
	 */
	public List<String> getQueryDataSetSources() {
		return QueryDataSetSources;
	}

	/**
	 * @param queryDataSetSources the queryDataSetSources to set
	 */
	public void setQueryDataSetSources(List<String> queryDataSetSources) {
		QueryDataSetSources = queryDataSetSources;
	}

	/**
	 * @return the queryOperatorsNb
	 */
	public int getQueryOperatorsNb() {
		return QueryOperatorsNb;
	}

	/**
	 * @param queryOperatorsNb the queryOperatorsNb to set
	 */
	public void setQueryOperatorsNb(int queryOperatorsNb) {
		QueryOperatorsNb = queryOperatorsNb;
	}

	/**
	 * @return the queryResultOrderingNb
	 */
	public int getQueryResultOrderingNb() {
		return QueryResultOrderingNb;
	}

	/**
	 * @param queryResultOrderingNb the queryResultOrderingNb to set
	 */
	public void setQueryResultOrderingNb(int queryResultOrderingNb) {
		QueryResultOrderingNb = queryResultOrderingNb;
	}

	/**
	 * @return the queryResultLimitNb
	 */
	public int getQueryResultLimitNb() {
		return QueryResultLimitNb;
	}

	/**
	 * @param queryResultLimitNb the queryResultLimitNb to set
	 */
	public void setQueryResultLimitNb(int queryResultLimitNb) {
		QueryResultLimitNb = queryResultLimitNb;
	}

	/**
	 * @return the queryResultOffsetNb
	 */
	public int getQueryResultOffsetNb() {
		return QueryResultOffsetNb;
	}

	/**
	 * @param queryResultOffsetNb the queryResultOffsetNb to set
	 */
	public void setQueryResultOffsetNb(int queryResultOffsetNb) {
		QueryResultOffsetNb = queryResultOffsetNb;
	}
	
	public int parseQuery(String QueryContent){
		//parses the QueryContent and fills up all the fields in this class
		
		this.setQueryContent(QueryContent);
		this.setQuerySizeInCharacters(QueryContent.length());
		try {
			Query query = QueryFactory.create(QueryContent);
			parsePrefixInformation(query);
			
			parseVariablesInformation(query);
			
			parseDataSetSourcesInformation(query);
			
			parseOrderByStatement(query);
			
			parseLimitStatement(query);
			
			parseOffsetStatement(query);
			
			parseQueryOperatorsNb(query);
			
			getModel(query);
		} catch (QueryException qe) {
			System.out.println("Query creation returned an exception " + qe.getMessage());
			return -1;
		}
		catch (Exception ex){
			System.out.println("Query creation returned an exception " + ex.getMessage());
			return -1;
		}
		
		return 0;				
	}
	
	public void parsePrefixInformation(Query query) throws Exception{
		// gets the information related to the prefixes, i.e QueryDataSetSourcesNb and QueryDataSetSources
		
		//display the number of prefix statements along with the namespaces they represent
		PrefixMapping prefixMapping = query.getPrefixMapping();
		
		Map<String,String> prefixEquivalents = prefixMapping.getNsPrefixMap();
		
		System.out.println("I have "+ prefixEquivalents.size() + " prefixes and these are:");
		
		//get only the prefixes that are used in the query; 
		
		Set <String> keySetTmp = prefixEquivalents.keySet();
		Collection <String> entrySetTmp = prefixEquivalents.values();
		
		//find what keys are used in the query; 
		// some prefixes may appear in the prefix declaration but they are not effectively used in the corpus of the query
		//hence further parsing is needed to the the exact list of prefixes;
		
		if (!query.isSelectType()) {
			System.out.println("ONLY WORKS FOR SELECT QUERIES");
			throw new Exception("NOT A SELECT Query!");			
		}				
		String QueryBody = query.toString().toLowerCase();		
	
		int selectIndex = QueryBody.indexOf("select");
		if (selectIndex < 0){
			System.out.println("ONLY WORKS FOR SELECT QUERIES");
			throw new Exception("NOT A SELECT Query!");			
		}
	
		String selectBody = QueryBody.substring(selectIndex); 
		Iterator<String> it = keySetTmp.iterator();
		int keyValueIndex = -1;
		Set<String> keys = new HashSet();
		Set<String> values = new HashSet();
		
		while (it.hasNext()) {
			// Return the value to which this map maps the specified key.
			String crtKey = it.next();
			String crtValue = prefixEquivalents.get(crtKey);			
			System.out.println(crtKey + " - " + crtValue);
			keyValueIndex = selectBody.indexOf(crtKey);
			if ( keyValueIndex > 0) {
				keys.add(crtKey);
				values.add(crtValue);
			}				
			else {
				keyValueIndex = selectBody.indexOf(crtValue);
				if ( keyValueIndex > 0) {
					keys.add(crtKey);
					values.add(crtValue);
				}
			}
		}
		this.setQueryNamespaceNb(keys.size());
		this.setQueryNamespaceValues(values);
		System.out.println("\n-----------------------------------------------------------\n");		
	}
	
	public void parseVariablesInformation(Query query){
		//finds all the information related to variables in the query
		
		/*
		/*display the number of variables contained in the query and the variables
		*/
		Element queryBlock = query.getQueryPattern();
		
		Set<String> variablesSet = new HashSet<String>();
		
		//save the variables from where clause - they might not contain all the variables from select
		Set<Var> variablesInWhereClause = queryBlock.varsMentioned();
		
		//save the variables from where clause to the list containing all the variables
		Iterator<Var> its =  variablesInWhereClause.iterator();
		while (its.hasNext())
			variablesSet.add(its.next().toString().substring(1));
		
		//save the variables from select clause to the list containing all the variables
		for (int i = 0; i < query.getResultVars().size(); i++)
			variablesSet.add(query.getResultVars().get(i));
		
		System.out.println("I have " + variablesSet.size() + " variables and these are:");
		Iterator<String> itss =  variablesSet.iterator();
		while (itss.hasNext())
			System.out.println(itss.next());
		
		System.out.println("\n-----------------------------------------------------------\n");
		
		this.setQueryVariablesNb(variablesSet.size());

	}
	
	public void parseDataSetSourcesInformation(Query query){
		//extract the from clauses
		
		/*
		/*display the number of FROM sources contained in the query and the sources
		*/
		
		if (query.hasDatasetDescription()) {
			List<String> graphUris = query.getGraphURIs();
		
			System.out.println("I have " + graphUris.size() + " data sources and these are:");
			for (int i = 0; i < graphUris.size(); i++)
				System.out.println(graphUris.get(i));
		
			System.out.println("\n-----------------------------------------------------------\n");

			this.setQueryDataSetSourcesNb(graphUris.size());
			this.setQueryDataSetSources(graphUris);
		}
		else this.setQueryDataSetSources(new ArrayList<String>());
	}
	public void parseOrderByStatement (Query query){
		
		/*
		/*display the number of fields in the ORDER BY statement and the fields
		*/
		
		if (query.hasOrderBy()) {
			List<SortCondition> orderByFields =  query.getOrderBy();
			System.out.println("I have " + orderByFields.size() + " fields in my ORDER BY clause and these are:");
		
			for (int i = 0; i < orderByFields.size(); i++)
				System.out.println(orderByFields.get(i));
		
			System.out.println("\n-----------------------------------------------------------\n");
		
			this.setQueryResultOrderingNb(orderByFields.size());
		}
	}
	
	public void parseLimitStatement (Query query){
		/*
		/*display the value of n from the LIMIT n statement
		*/
		if (query.hasLimit()) {
				long limit = query.getLimit();
				System.out.println("The value of n from my LIMIT n statement is: "+limit);
		
				System.out.println("\n-----------------------------------------------------------\n");
		
				this.setQueryResultLimitNb((int)limit);
		}
	}

	public void parseOffsetStatement (Query query){
		/*
		*display the value of m from the OFFSET m statement
		*/
		if (query.hasOffset()) {
			long offset = query.getOffset();
			System.out.println("The value of m from my OFFSET m statement is: "+offset);
		
			System.out.println("\n-----------------------------------------------------------\n");

			this.setQueryResultOffsetNb((int) offset);
		}
	}
	
	public void getModel (Query query){
		Model model = ModelFactory.createDefaultModel() ;
		QueryExecution qExec = QueryExecutionFactory.create(query, model) ;
		
		System.out.println( "Size of the model " + model.size() + " QueryExecution " + qExec.toString());
	}

	public void parseQueryOperatorsNb(Query query) {
		/*
		*display the literals from the query
		*/
		//operators appear only in FILTER blocks => number of operators = number of filter blocks
		Set<String> operators = new HashSet<String>();
	
		//save the literals in a set so that they don't appear twice
		Set<String> literals = new HashSet<String>();
		
		Element queryBlock = query.getQueryPattern();		
		ElementGroup eg = (ElementGroup) queryBlock;
		
		//the query elements can be TRIPLE blocks, OPTIONAL blocks or FILTER blocks
		List<Element> queryElements = eg.getElements();	
		
		for (int i = 0; i < queryElements.size(); i++)
		{
			//if it is a triple, test if the object of the triple is a literal and save it
			if (queryElements.get(i).getClass().getName().equals("com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock"))
			{
				ElementTriplesBlock etb = (ElementTriplesBlock) queryElements.get(i);
				Iterator<Triple> tripleIt = etb.patternElts();
				while (tripleIt.hasNext())
				{
					Node nod = tripleIt.next().getObject();					
					if (nod.isLiteral())
						literals.add(nod.toString());
				}
			}			
			//if it is an OPTIONAL block, parse all the building blocks and check as above
			if (queryElements.get(i).getClass().getName().equals("com.hp.hpl.jena.sparql.syntax.ElementOptional"))
			{
				ElementOptional opt = (ElementOptional) queryElements.get(i);
				Element exo = opt.getOptionalElement();
				ElementGroup ego = (ElementGroup) exo;
				
				List<Element> optElements = ego.getElements();
				
				for (int j = 0; j < optElements.size(); j++)
				{
					ElementTriplesBlock etbo = (ElementTriplesBlock) optElements.get(j);
					Iterator<Triple> optIt = etbo.patternElts();
					while (optIt.hasNext())
					{
						Node nod = optIt.next().getObject();
						if (nod.isLiteral())
							literals.add(nod.toString());
					}
				}
			}
			
			//if it is a FILTER block, check as above
			if (queryElements.get(i).getClass().getName().equals("com.hp.hpl.jena.sparql.syntax.ElementFilter"))
			{
				ElementFilter ef = (ElementFilter) queryElements.get(i);
				Expr ex = ef.getExpr();

				ExprFunction vr = ex.getFunction();
				List<Expr> t = vr.getArgs();
				
				operators.add(vr.getFunctionSymbol().toString());
				
				for (int z = 0; z < t.size(); z++)
					if (t.get(z).isConstant())
						literals.add(t.get(z).toString());
			}
		}
		
		this.setQueryOperatorsNb(operators.size());
		/*
		System.out.println("I have " + operators.size() + " operators and they are:");
		Iterator<String> setItOp = operators.iterator();
		
		while (setItOp.hasNext())
			System.out.println(setItOp.next());
		
		System.out.println("\n-----------------------------------------------------------\n");		
		*/
		this.setQueryLiteralsNb(literals.size());
		
		/*
		System.out.println("I have " + literals.size() + " literals and they are:");
		Iterator<String> setIt = literals.iterator();
		
		while (setIt.hasNext())
			System.out.println(setIt.next());		
		System.out.println("\n-----------------------------------------------------------\n");
		*/		
	}
	/**
	 * @return the querySizeInTriples
	 */
	public int getQuerySizeInTriples() {
		return QuerySizeInTriples;
	}

	/**
	 * @param querySizeInTriples the querySizeInTriples to set
	 */
	public void setQuerySizeInTriples(int querySizeInTriples) {
		QuerySizeInTriples = querySizeInTriples;
	}

	/**
	 * @return the queryNamespaceValues
	 */
	public Set<String> getQueryNamespaceValues() {
		return QueryNamespaceValues;
	}

	/**
	 * @param queryNamespaceValues the queryNamespaceValues to set
	 */
	public void setQueryNamespaceValues(Set<String> queryNamespaceValues) {
		QueryNamespaceValues = queryNamespaceValues;
	}
/*
	public Instances generateArff (String arff_file_name, boolean save) {
		//creates an Instance corresponding to the fields stored in the parser
		// if save is true then the instance is saved in the arff_file_name
		
		// step 1: set-up the attributes
		FastVector attributes = new FastVector();
		
		//add the attributes to the vector
		
		//attribute 1: QueryTimestamp of type date
		attributes.addElement(new Attribute("QueryTimestamp","yyyy-MM-dd HH:mm:ss"));
		
		//attribute 2: QueryContent of type string
		attributes.addElement(new Attribute("QueryContent",(FastVector)null));
		
		//attribute 3: QueryNamespaceNb type NUMERIC
		attributes.addElement(new Attribute("QueryNamespaceNb"));
		
		//attribute 4: QueryNamespaceKeys of type STRING
		attributes.addElement(new Attribute("QueryNamespaceKeys", (FastVector)null));
		
		//attribute 5: QueryNamespaceValues of type STRING - this attribute is not added anymore because we will parse it and add an attribute for each namespace
		// if a certain namespace in the query is not present in the namespaces[] list, it will be ignored
		//IDEA- probably we could add an attribute that tells us how many namespaces have been ignored ? :D
		
		attributes.addElement(new Attribute("QueryNamespaceValues", (FastVector)null));
		
		//attribute 6: QueryVariablesNb of type NUMERIC
		attributes.addElement(new Attribute("QueryVariablesNb"));
		
		//attribute 7: QueryDataSetSourcesNb of type NUMERIC
		attributes.addElement(new Attribute("QueryDataSetSourcesNb"));
		
		//attribute 8: QueryResultOrderingNb of type NUMERIC
		attributes.addElement(new Attribute("QueryResultOrderingNb"));
		
		//attribute 9: QueryResultLimitNb of type NUMERIC
		attributes.addElement(new Attribute("QueryResultLimitNb"));
		
		//attribute 10: QueryResultOffsetNb of type NUMERIC
		attributes.addElement(new Attribute("QueryResultOffsetNb"));
		
		//attribute 11: QuerySizeInTriples of type NUMERIC
		attributes.addElement(new Attribute("QuerySizeInTriples"));
		
		//attribute 11: QuerySizeInTriples of type NUMERIC
		attributes.addElement(new Attribute("class"));
		
		
		// attribute 12-to 23 will be the query namespaces
		for (int i = 0; i < namespaces.length; i++){
			attributes.addElement(new Attribute("ns_"+namespaces[i]));
		}
				 
		//step 2: create Instances object
		Instances queryInstances = new Instances ("QueryInstancesRelation", attributes, 0);
		
		// step3: fill with data
		
		double vals[] = new double [queryInstances.numAttributes()];
		int attributeIndex = 0;
		
		try {
			//attribute 1: QueryTimestamp of type date
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date = new Date();		
			vals[attributeIndex] = queryInstances.attribute(attributeIndex++).parseDate(dateFormat.format(date));
		
			//attribute 2: QueryContent of type string
			vals[attributeIndex] = queryInstances.attribute(attributeIndex++).addStringValue(this.QueryContent);
		
			//attribute 3: QueryNamespaceNb type NUMERIC
			vals[attributeIndex++] = this.getQueryNamespaceNb();
		
//			//attribute 4: QueryNamespaceKeys of type STRING
//			vals[attributeIndex] = queryInstances.attribute(attributeIndex++).addStringValue(this.getQueryNamespaceKeys().toString());
		
			//attribute 5: QueryNamespaceValues of type STRING - elliminated because we add the splitted namespaces 
			vals[attributeIndex] = queryInstances.attribute(attributeIndex++).addStringValue(this.getQueryNamespaceValues().toString());
		
			//attribute 6: QueryVariablesNb of type NUMERIC
			vals[attributeIndex++] = this.getQueryVariablesNb();
		
			//attribute 7: QueryDataSetSourcesNb of type NUMERIC
			vals[attributeIndex++] = this.getQueryDataSetSourcesNb();
		
			//attribute 8: QueryResultOrderingNb of type NUMERIC
			vals[attributeIndex++] = this.getQueryResultOrderingNb();
		
			//attribute 9: QueryResultLimitNb of type NUMERIC
			vals[attributeIndex++] = this.getQueryResultLimitNb();
		
			//attribute 10: QueryResultOffsetNb of type NUMERIC
			vals[attributeIndex++] = this.getQueryResultOffsetNb();
		
			//attribute 11: QuerySizeInTriples of type NUMERIC
			vals[attributeIndex++] = this.getQuerySizeInTriples();
			
			//attribute 11: class -  of type NUMERIC
			vals[attributeIndex++] = 0;
			
			
			for(int i = 0; i < namespaces.length; i++){
				if (this.QueryNamespaceValues.contains(namespaces[i]))
					vals[attributeIndex++] = 1;
				else
					vals[attributeIndex++] = 0;
			}			
			queryInstances.add(new Instance(1.0, vals));
			
			if(save) {
				//save queryInstances to the arff_file_name
				ArffSaver saver = new ArffSaver();
				FileOutputStream fs = new FileOutputStream(arff_file_name);
				saver.setInstances(queryInstances);
				saver.setDestination(fs);				
				saver.writeBatch();
				fs.close();
			}

		} catch (ParseException pe){
			System.out.println(pe.getMessage());
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
		}				
		return queryInstances;
	}
*/

/*
	public Instances generateInstances () {
		//creates an Instance corresponding to the fields stored in the parser
		// if save is true then the instance is saved in the arff_file_name
		
		// step 1: set-up the attributes //
		FastVector attributes = new FastVector();
		
		//add the attributes to the vector
		
		//attribute 1: QueryTimestamp of type date
		attributes.addElement(new Attribute("QueryTimestamp","yyyy-MM-dd HH:mm:ss"));
		
		//attribute 2: QueryContent of type string
		attributes.addElement(new Attribute("QueryContent",(FastVector)null));
		
		//attribute 3: QueryNamespaceNb type NUMERIC
		attributes.addElement(new Attribute("QueryNamespaceNb"));
		
		//attribute 4: QueryNamespaceKeys of type STRING
		attributes.addElement(new Attribute("QueryNamespaceKeys", (FastVector)null));
		
		//attribute 5: QueryNamespaceValues of type STRING - this attribute is not added anymore because we will parse it and add an attribute for each namespace
		// if a certain namespace in the query is not present in the namespaces[] list, it will be ignored
		//IDEA- probably we could add an attribute that tells us how many namespaces have been ignored ? :D
		
		attributes.addElement(new Attribute("QueryNamespaceValues", (FastVector)null));
		
		//attribute 6: QueryVariablesNb of type NUMERIC
		attributes.addElement(new Attribute("QueryVariablesNb"));
		
		//attribute 7: QueryDataSetSourcesNb of type NUMERIC
		attributes.addElement(new Attribute("QueryDataSetSourcesNb"));
		
		//attribute 8: QueryResultOrderingNb of type NUMERIC
		attributes.addElement(new Attribute("QueryResultOrderingNb"));
		
		//attribute 9: QueryResultLimitNb of type NUMERIC
		attributes.addElement(new Attribute("QueryResultLimitNb"));
		
		//attribute 10: QueryResultOffsetNb of type NUMERIC
		attributes.addElement(new Attribute("QueryResultOffsetNb"));
		
		//attribute 11: QuerySizeInTriples of type NUMERIC
		attributes.addElement(new Attribute("QuerySizeInTriples"));
		
		//attribute 11: QuerySizeInTriples of type NUMERIC
		attributes.addElement(new Attribute("class"));
		
		
		// attribute 12-to 23 will be the query namespaces
		for (int i = 0; i < namespaces.length; i++){
			attributes.addElement(new Attribute("ns_"+namespaces[i]));
		}
				 
		//step 2: create Instances object
		Instances queryInstances = new Instances ("QueryInstancesRelation", attributes, 0);
		
		// step3: fill with data
		
		double vals[] = new double [queryInstances.numAttributes()];
		int attributeIndex = 0;
		
		try {
			//attribute 1: QueryTimestamp of type date
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date = new Date();		
			vals[attributeIndex] = queryInstances.attribute(attributeIndex++).parseDate(dateFormat.format(date));
		
			//attribute 2: QueryContent of type string
			vals[attributeIndex] = queryInstances.attribute(attributeIndex++).addStringValue(this.QueryContent);
		
			//attribute 3: QueryNamespaceNb type NUMERIC
			vals[attributeIndex++] = this.getQueryNamespaceNb();
		
			//attribute 4: QueryNamespaceKeys of type STRING
			vals[attributeIndex] = queryInstances.attribute(attributeIndex++).addStringValue(this.getQueryNamespaceKeys().toString());
		
			//attribute 5: QueryNamespaceValues of type STRING - elliminated because we add the splitted namespaces 
			vals[attributeIndex] = queryInstances.attribute(attributeIndex++).addStringValue(this.getQueryNamespaceValues().toString());
		
			//attribute 6: QueryVariablesNb of type NUMERIC
			vals[attributeIndex++] = this.getQueryVariablesNb();
		
			//attribute 7: QueryDataSetSourcesNb of type NUMERIC
			vals[attributeIndex++] = this.getQueryDataSetSourcesNb();
		
			//attribute 8: QueryResultOrderingNb of type NUMERIC
			vals[attributeIndex++] = this.getQueryResultOrderingNb();
		
			//attribute 9: QueryResultLimitNb of type NUMERIC
			vals[attributeIndex++] = this.getQueryResultLimitNb();
		
			//attribute 10: QueryResultOffsetNb of type NUMERIC
			vals[attributeIndex++] = this.getQueryResultOffsetNb();
		
			//attribute 11: QuerySizeInTriples of type NUMERIC
			vals[attributeIndex++] = this.getQuerySizeInTriples();
			
			//attribute 11: class -  of type NUMERIC
			vals[attributeIndex++] = 0;
			
			
			for(int i = 0; i < namespaces.length; i++){
				if (this.QueryNamespaceValues.contains(namespaces[i]))
					vals[attributeIndex++] = 1;
				else
					vals[attributeIndex++] = 0;
			}			
			queryInstances.add(new Instance(1.0, vals));
			
		} catch (ParseException pe){
			System.out.println(pe.getMessage());
		}				
		return queryInstances;
	}
*/

        private void setInstanceAttributeValue(Instance i,Instances dataSet,String attributeName,String value)
        {
            try
            {
                i.setValue(dataSet.attribute(attributeName), value);
            }
            catch (Exception e)
            {
            }
        }

        private void setInstanceAttributeValue(Instance i,Instances dataSet,String attributeName,double value)
        {
            try
            {
                i.setValue(dataSet.attribute(attributeName), value);
            }
            catch (Exception e)
            {
            }
        }

	public Instances generateInstances (Instances formatInstances) {
                 Instances newSet = new Instances (formatInstances, 1);
                 Instance tInstance=new Instance(newSet.numAttributes());
                 tInstance.setDataset(formatInstances);

                 setInstanceAttributeValue(tInstance,formatInstances,"QueryContent", this.QueryContent);
                 setInstanceAttributeValue(tInstance,formatInstances,"QuerySizeInCharacters", this.getQuerySizeInCharacters());
                 setInstanceAttributeValue(tInstance,formatInstances,"QueryNamespaceNb",this.getQueryNamespaceNb());
                 setInstanceAttributeValue(tInstance,formatInstances,"QueryVariablesNb",this.getQueryVariablesNb());
                 setInstanceAttributeValue(tInstance,formatInstances,"QueryDataSetSourcesNb",this.getQueryDataSetSourcesNb());
                 setInstanceAttributeValue(tInstance,formatInstances,"QueryOperatorsNb",this.getQueryOperatorsNb());
                 setInstanceAttributeValue(tInstance,formatInstances,"QueryLiteralsNb",this.getQueryLiteralsNb());
                 setInstanceAttributeValue(tInstance,formatInstances,"QueryResultOrderingNb",this.getQueryResultOrderingNb());
                 setInstanceAttributeValue(tInstance,formatInstances,"QueryResultLimitNb",this.getQueryResultLimitNb());
                 setInstanceAttributeValue(tInstance,formatInstances,"QueryResultOffsetNb",this.getQueryResultOffsetNb());
                 
/*               tInstance.setValue(formatInstances.attribute("QueryContent"), this.QueryContent);
                 tInstance.setValue(formatInstances.attribute("QuerySizeInCharacters"), this.getQuerySizeInBytes());
                 tInstance.setValue(formatInstances.attribute("QueryNamespaceNb"), this.getQueryNamespaceNb());
                 tInstance.setValue(formatInstances.attribute("QueryVariablesNb"), this.getQueryVariablesNb());
                 tInstance.setValue(formatInstances.attribute("QueryDataSetSourcesNb"), this.getQueryDataSetSourcesNb());
                 tInstance.setValue(formatInstances.attribute("QueryOperatorsNb"), this.getQueryOperatorsNb());
                 tInstance.setValue(formatInstances.attribute("QueryResultOrderingNb"), this.getQueryResultOrderingNb());
                 tInstance.setValue(formatInstances.attribute("QueryResultLimitNb"), this.getQueryResultLimitNb());
                 tInstance.setValue(formatInstances.attribute("QueryResultOffsetNb"), this.getQueryResultOffsetNb());
*/
                 Iterator it;
                 Set<String> QueryNamespaceValuesL=null;

                 if (QueryNamespaceValues!=null)
                 {
                     QueryNamespaceValuesL = new HashSet();
                     it=QueryNamespaceValues.iterator();
                     while (it.hasNext())
                         QueryNamespaceValuesL.add(((String)it.next()).toLowerCase());
                 }

                 List<String> QueryDataSetSourcesL=null;
                 if (QueryDataSetSources!=null)
                 {
                     QueryDataSetSourcesL = new ArrayList<String>();
                     it=QueryDataSetSources.iterator();
                     while (it.hasNext())
                         QueryDataSetSourcesL.add(((String)it.next()).toLowerCase());
                 }

 		 for(int i = 0; i < formatInstances.numAttributes(); i++)
                     {
                         String attrI=formatInstances.attribute(i).name();
                         if (attrI.substring(0,3).equals("ns_"))
                         {
                             String nameSpace=attrI.substring(3);
                             if (this.QueryNamespaceValues!=null && QueryNamespaceValuesL.contains(nameSpace.toLowerCase()))
                                  tInstance.setValue(formatInstances.attribute(attrI), 1);
                             else
                                  tInstance.setValue(formatInstances.attribute(attrI), 0);
                         }
                         else
                             if (attrI.substring(0,3).equals("ds_"))
                             {
                                 String nameSpace=attrI.substring(3);
                                 if (this.QueryDataSetSources!=null && QueryDataSetSourcesL.contains(nameSpace.toLowerCase()))
                                      tInstance.setValue(formatInstances.attribute(attrI), 1);
                                 else
                                      tInstance.setValue(formatInstances.attribute(attrI), 0);
                             }
                     }


                 newSet.add(tInstance);
                 return newSet;
        }

        public double[] generateParseResults()
        {
            double[] values = new double[9];
            
            values [0] = this.getQuerySizeInCharacters();
            values[1] = this.getQueryNamespaceNb();
            values[2] = this.getQueryVariablesNb();
            values[3] = this.getQueryDataSetSourcesNb();
            values[4] = this.getQueryOperatorsNb();
            values[5] = this.getQueryLiteralsNb();
            values[6] = this.getQueryResultOrderingNb();
            values[7] = this.getQueryResultLimitNb();
            values[8] = this.getQueryResultOffsetNb();
            
            return values;
        }

	public void printParserFields() {
		System.out.println("QueryContent: " + this.getQueryContent());
		System.out.println("QuerySizeInCharacters: " + this.getQuerySizeInCharacters());
		System.out.println("QueryNamespaceNb: "+ this.getQueryNamespaceNb());
		System.out.println("QueryNamespaceValues: " + this.getQueryNamespaceValues());
		System.out.println("QueryVariablesNb: " + this.getQueryVariablesNb());
		System.out.println("QueryDataSetSourcesNb: " + this.getQueryDataSetSourcesNb());
		if (! this.getQueryDataSetSources().isEmpty() )
			System.out.println("QueryDataSetSourcesValues: " + this.getQueryDataSetSources());
		else System.out.println("QueryDataSetSourcesValues: []" );
		System.out.println("QueryOperatorsNb: " + this.getQueryOperatorsNb());
		System.out.println("QueryLiteralsNb: " + this.getQueryOperatorsNb());
		System.out.println("QueryResultOrderingNb: " + this.getQueryResultOrderingNb());
		System.out.println("QueryResultLimitNb: " + this.getQueryResultLimitNb()); 
		System.out.println("QueryResultOffsetNb: " + this.getQueryResultOffsetNb());
	}
}