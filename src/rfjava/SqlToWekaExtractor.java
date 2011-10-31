package rfjava;

import java.io.File;

import java.io.IOException;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.experiment.InstanceQuery;
import java.util.Date;


public class SqlToWekaExtractor {
	public final long tx = 25000; // 5000 ms; -- used for platform metrics

        public String dbName, userName, password, url , driver;

    public SqlToWekaExtractor(){}
    
	// connects to sql and extracts instances, based on the given query
	public SqlToWekaExtractor(String url, String driver, String dbName, String userName, String password) {
            this.url=url;
            this.driver=driver;
            this.dbName=dbName;
            this.userName=userName;
            this.password=password;
	}

	public Connection getConnection() {
		Connection c=null;
                try {
			Class.forName(driver).newInstance();
			c = DriverManager.getConnection(url + dbName, userName, password);
			// System.out.println("Connected to the database");
		} catch (Exception e) {
			c=null;
		}
		return c;
	}

	public String transformDateInMilis(String timeStamp, String datePattern) {
		// transforms a given timeStamp having a given pattern into miliseconds
		// since January 1, 1970, 00:00:00 GMT :
		// pattern could be "dd-MM-yyyy hh:mm:ss"
		SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
		Date date;
		String dateMs = null;
		try {
			date = sdf.parse(timeStamp);
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			long t1 = cal.getTimeInMillis();
			dateMs = Long.toString(t1);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return dateMs;
	}

	public String transformMilisInDate(String timeInMilis, String datePattern) {
		// date pattern is of the form: yyyy-mm-dd hh:mm:ss
		// DateFormat formatter = new
		// SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");
		DateFormat formatter = new SimpleDateFormat(datePattern);
		long t = Long.parseLong(timeInMilis);
		// Create a calendar object that will convert the date and time value
		// in milliseconds to date. We use the setTimeInMillis() method of the
		// Calendar object.
		//
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(t);

		String dateVal = formatter.format(calendar.getTime());
		// System.out.println(t + " = " + dateVal);

		return dateVal;
	}


	public ArrayList<String> getAllQueryIds() {

		ArrayList<String> qid = new ArrayList<String>();
		String query = "select queries.idQuery from queries";

                Connection conn=getConnection();
		try {
			PreparedStatement stmt = conn.prepareStatement(query);
			ResultSet rsMetrics = stmt.executeQuery();

			while (rsMetrics.next()) {
				String idQuery = rsMetrics.getString(1);
				qid.add(idQuery);
			}
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return qid;
	}


	public ArrayList<String> getQueryIds(String beginExecutionTime,
			String endExecutionTime) {
		// returns a set containing the query Ids that have been run in a given
		// time interval
		// this means that QueryBeginExecutionTime is in the given time interval
		// other variations can be included;

		// the time interval should be transformed in ms from EPOCH
		// Milliseconds since January 1, 1970, 00:00:00 GMT :

		String qbeMs = transformDateInMilis(beginExecutionTime,
				"dd-MM-yyyy HH:mm:ss");
		String qeeMs = transformDateInMilis(endExecutionTime,
				"dd-MM-yyyy HH:mm:ss");

		ArrayList<String> qid = new ArrayList<String>();
		String query = "select queries_metrics.idQuery, queries_metrics.Value from queries_metrics "
				+ " where queries_metrics.idMetric = 1 and  "
				+ " queries_metrics.Value >= '"
				+ qbeMs
				+ "' and queries_metrics.Value <= '"
				+ qeeMs
				+ "' order by queries_metrics.Value";

                Connection conn=getConnection();
		try {
			// PreparedStatement stmt =
			// conn.prepareStatement("select metrics.Name, mtypes.Name from metrics, mtypes where metrics.MetricType = mtypes.idType and metrics.Name like '%query%'");
			PreparedStatement stmt = conn.prepareStatement(query);
			ResultSet rsMetrics = stmt.executeQuery();

			while (rsMetrics.next()) {
				String idQuery = rsMetrics.getString(1);
				qid.add(idQuery);
			}
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return qid;
	}

	public ArrayList<String> getQueryIds(long beginExecutionTime,
			long endExecutionTime) {
		// returns a set containing the query Ids that have been run in a given
		// time interval
		// this means that QueryBeginExecutionTime is in the given time interval
		// other variations can be included;

		// the time interval should be transformed in ms from EPOCH
		// Milliseconds since January 1, 1970, 00:00:00 GMT :

		String qbeMs = String.valueOf(beginExecutionTime);
		String qeeMs = String.valueOf(endExecutionTime);

		ArrayList<String> qid = new ArrayList<String>();
		String query = "select queries_metrics.idQuery, queries_metrics.Value from queries_metrics "
				+ " where queries_metrics.idMetric = 1 and  "
				+ " queries_metrics.Value >= '"
				+ qbeMs
				+ "' and queries_metrics.Value <= '"
				+ qeeMs
				+ "' order by queries_metrics.Value";

                Connection conn=getConnection();
		try {
			// PreparedStatement stmt =
			// conn.prepareStatement("select metrics.Name, mtypes.Name from metrics, mtypes where metrics.MetricType = mtypes.idType and metrics.Name like '%query%'");
			PreparedStatement stmt = conn.prepareStatement(query);
			ResultSet rsMetrics = stmt.executeQuery();

			while (rsMetrics.next()) {
				String idQuery = rsMetrics.getString(1);
				qid.add(idQuery);
			}
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return qid;
	}


	public Map<String, String> getQueryIds_PlatformIds(ArrayList<String> qIds) {
		Map<String, String> queryIds_PlatformIds = new HashMap<String, String>();
                Connection conn=getConnection();

                String query = "select platforms_workflows.idPlatform, platforms_workflows.idWorkflow from platforms_workflows, queries_workflows "
                + " where platforms_workflows.idWorkflow = queries_workflows.idWorkflow and "
                + " queries_workflows.idQuery = ?";
                PreparedStatement stmt=null;
                try {
                    stmt = conn.prepareStatement(query);
                } catch (SQLException ex) {
                    Logger.getLogger(SqlToWekaExtractor.class.getName()).log(Level.SEVERE, null, ex);
                }

		for (int i = 0; i < qIds.size(); i++) {
			String crtQid = qIds.get(i);

			// this returns two results for a query id; we should put only one,
			// and if the platformIds are different for the two workflow ids,
			// then we should signal an error

			try {
                                stmt.setString(1, crtQid);
				ResultSet rsMetrics = stmt.executeQuery();

				// for a query Id we should have just one workflowDescription
				// I use a temporary map to check if errors occur,
				// that is check if for a query id to which two different
				// workflow ids correspond we have two different workflow
				// descriptions -- THIS would be wrong
				ArrayList<String> platformIds = new ArrayList<String>();
				while (rsMetrics.next()) {
					String idPlatform = rsMetrics.getString(1);
					String idWorkflow = rsMetrics.getString(2);
					if (!platformIds.contains(idPlatform))
						platformIds.add(idPlatform);
				}
				if (platformIds.isEmpty()) {
					queryIds_PlatformIds.put(crtQid, "?");// put a -1 if to the
															// queryId there
															// corresponds no
															// platform id
				}
				// check if any platformId have been recorded for this query
				else if (platformIds.size() > 1) {
					System.out.println("The query having as id " + crtQid
							+ " has been run on different platform ids "
							+ platformIds);
					System.out.println("we will use only one platform id for the current query");
				} else
					// (platformIds.size() == 1)
					queryIds_PlatformIds.put(crtQid, platformIds.get(0));

                                rsMetrics.close();

			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

                try {
                    stmt.close();
                } catch (SQLException ex) {
                    Logger.getLogger(SqlToWekaExtractor.class.getName()).log(Level.SEVERE, null, ex);
                }

		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return queryIds_PlatformIds;
	}


        public ArrayList<String> getPlatformMetricsForPlatformIDQueryID(String platformId, String qId, ArrayList<String> desiredPlatformMetrics, Connection conn) {
        String query = "select queries_metrics.Value from queries_metrics where "
                + " queries_metrics.idQuery = '"
                + qId
                + "' and queries_metrics.idMetric = 1";

        ArrayList<String> platformM = new ArrayList<String>();
        String desiredPlatformMetricsValues = Configuration.convertArrayOfMetricsToString(desiredPlatformMetrics);

       for (int k = 0; k < desiredPlatformMetrics.size(); k++) {
           platformM.add("-1");
       }
        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rsMetrics = stmt.executeQuery();
            String qbe = "";
            while (rsMetrics.next()) {
                qbe = rsMetrics.getString(1);
            }

            rsMetrics.close();
            stmt.close();

            long d = Long.parseLong(qbe);

            String ds = transformMilisInDate(Long.toString(d), "yyyy-MM-dd HH:mm:ss");   
            //find the timestamp we are interested in just for the first metric
           
             query = " select platforms_metrics.Timestamp, platforms_metrics.Value, metrics.MetricName "
                    + " from  platforms_metrics, metrics where  platforms_metrics.idPlatform = '"
                    + platformId
                + "' and metrics.MetricName = '"  + desiredPlatformMetrics.get(0)
                + "' and platforms_metrics.idMetric = metrics.idMetric  "                 
                + " and (TIMESTAMPDIFF(SECOND,platforms_metrics.Timestamp,'" + ds + "')-5)<=0 " +
                "  order by platforms_metrics.Timestamp limit 1";
            
            stmt = conn.prepareStatement(query);
            rsMetrics = stmt.executeQuery();
            String timestamp = "";
            while (rsMetrics.next()) {
            	timestamp = rsMetrics.getString(1);
            }

            rsMetrics.close();
            stmt.close();
            //find the timestamp for the first metric and use it for the rest !
       
           query = "  select platforms_metrics.Timestamp, platforms_metrics.Value, metrics.MetricName " +
           		"  from  platforms_metrics, metrics where " +
           		"  platforms_metrics.idPlatform = '" + platformId +
           		"' and " +
           		"  metrics.MetricName in  "  + desiredPlatformMetricsValues +
           		" and  platforms_metrics.idMetric = metrics.idMetric  and " +
           		"  platforms_metrics.Timestamp = '" + timestamp + "'";
            
            PreparedStatement stmt2 = conn.prepareStatement(query);                  
            ResultSet rsMetrics2 = stmt2.executeQuery();
            String mValue = "";
            String mName = "";
            while (rsMetrics2.next()) {            
                mValue = rsMetrics2.getString(2);
                mName = rsMetrics2.getString(3);
                int index = desiredPlatformMetrics.indexOf(mName);
                platformM.set(index, mValue);
            }                        
                       
            rsMetrics2.close();            
            stmt2.close();
               
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return platformM;
    }

	public Map<String, ArrayList<String>> getQueryIds_PlatformMetrics(
			Map<String, String> queryIds_PlatformIds,		
			ArrayList<String> desiredPlatformMetrics) {

		Map<String, ArrayList<String>> platformMetrics = new HashMap<String, ArrayList<String>>();

		Set s = queryIds_PlatformIds.entrySet();

		// Move next key and value of Map by iterator
		Iterator it = s.iterator();
                Connection conn=getConnection();
		while (it.hasNext()) {
			Map.Entry m = (Map.Entry) it.next();
			String crtQid = (String) m.getKey();
			String platformId = (String) m.getValue();

                        ArrayList<String> platformM = getPlatformMetricsForPlatformIDQueryID(platformId, crtQid, desiredPlatformMetrics, conn);

			platformMetrics.put(crtQid, platformM);
		}
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return platformMetrics;
	}
        
        public ArrayList<String> getSystemMetricsForQueryIDSystemID(String systemId, String qId, ArrayList<String> desiredSystemMetrics, Connection conn) {
        String query = "select queries_metrics.Value from queries_metrics where "
                + " queries_metrics.idQuery = '"
                + qId
                + "' and queries_metrics.idMetric = 1";

        ArrayList<String> systemM = new ArrayList<String>();
        String desiredSystemMetricsValues = Configuration.convertArrayOfMetricsToString(desiredSystemMetrics);
        for (int k = 0; k < desiredSystemMetrics.size(); k++) {
            systemM.add("-1");
        }

        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rsMetrics = stmt.executeQuery();

            // for a query Id we should have just one workflowDescription
            // I use a temporary map to check if errors occur,
            // that is check if for a query id to which two different
            // workflow ids correspond we have two different workflow
            // descriptions -- THIS would be wrong
            String qbe = "";
            while (rsMetrics.next()) {
                qbe = rsMetrics.getString(1);
            }

            rsMetrics.close();
            stmt.close();
          
            long d = Long.parseLong(qbe);
            String ds = transformMilisInDate(Long.toString(d), "yyyy-MM-dd HH:mm:ss");     
            
             query = " select systems_metrics.Timestamp, systems_metrics.Value, metrics.MetricName "
                    + " from  systems_metrics, metrics where  systems_metrics.idSystem = '"
                    + systemId
                + "' and systems_metrics.idMetric = metrics.idMetric and  metrics.MetricName = '"
                + desiredSystemMetrics.get(0)
                + "' and (TIMESTAMPDIFF(SECOND,systems_metrics.Timestamp,'" + ds + "')-5)<=0 "
                + "order by systems_metrics.Timestamp limit 1" ;
            
            stmt = conn.prepareStatement(query);
            rsMetrics = stmt.executeQuery();

            String timestamp = "";
            while (rsMetrics.next()) {
            	timestamp = rsMetrics.getString(1);
            }

            rsMetrics.close();
            stmt.close();
            //we have found the timestamp; extract all metrics having that timestamp
            
            query = " select systems_metrics.Timestamp, systems_metrics.Value, metrics.MetricName " +
            		" from  systems_metrics, metrics where " +
            		" systems_metrics.idSystem = '" + systemId +
            		"' and " +
            		" metrics.MetricName in " + desiredSystemMetricsValues + 
            		" and systems_metrics.idMetric = metrics.idMetric  and " +
            		" systems_metrics.Timestamp = '" + timestamp + "'";

            PreparedStatement stmt2 = conn.prepareStatement(query);
            ResultSet rsMetrics2 = stmt2.executeQuery();
            String mValue = "";
            String mName = "";
            while (rsMetrics2.next()) {            
                mValue = rsMetrics2.getString(2);
                mName = rsMetrics2.getString(3);
                int index = desiredSystemMetrics.indexOf(mName);
                systemM.set(index, mValue);
            }
                        
            rsMetrics2.close();            
            stmt2.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return systemM;
    }

	public Map<String, ArrayList<String>> getQueryIds_SystemMetrics(
			Map<String, String> queryIds_PlatformIds,

			ArrayList<String> desiredSystemMetrics) {

		Map<String, ArrayList<String>> systemMetrics = new HashMap<String, ArrayList<String>>();

		Set s = queryIds_PlatformIds.entrySet();

		// Move next key and value of Map by iterator
		Iterator it = s.iterator();
                Connection conn=getConnection();
		while (it.hasNext()) {
			Map.Entry m = (Map.Entry) it.next();
			String crtQid = (String) m.getKey();
			String platformId = (String) m.getValue();

			ArrayList<String> systemM = getSystemMetricsForQueryIDSystemID(platformId,crtQid, desiredSystemMetrics, conn); 	                        
                        systemMetrics.put(crtQid, systemM);
                }
		

	try {
		conn.close();
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	return systemMetrics;
}

	public Map<String, ArrayList<String>> getQueryIds_PluginIds(
			ArrayList<String> qIds) {
		// reuturns a map that has as key the queryId and as values the
		// pluginIds associated to the query

		Map<String, ArrayList<String>> queryIds_PluginIds = new HashMap<String, ArrayList<String>>();
                Connection conn=getConnection();
                PreparedStatement stmt = null;
                
                String query = "select queries_workflows.idWorkflow, workflows_plugins.idPlugin from queries_workflows, workflows_plugins where "
				+ " queries_workflows.idQuery = ?"				
				+ " and workflows_plugins.idWorkflow = queries_workflows.idWorkflow";
                try {
                     stmt = conn.prepareStatement(query);			
                     for (String crtQid : qIds) {
                        stmt.setString(1, crtQid);
                    	ResultSet rsMetrics = stmt.executeQuery();
			// for a query Id we should have just one workflowDescription
			// I use a temporary map to check if errors occur,
			// that is check if for a query id to which two different
			// workflow ids correspond we have two different workflow
			// descriptions -- THIS would be wrong
			ArrayList<String> pluginIds = new ArrayList<String>();
			while (rsMetrics.next()) {
				String idWorkflow = rsMetrics.getString(1);
				String idPlugin = rsMetrics.getString(2);
				pluginIds.add(idPlugin);
			}
			if (pluginIds.isEmpty())
			{
				pluginIds.add("-1");
			}
			queryIds_PluginIds.put(crtQid, pluginIds);
                        rsMetrics.close();
                    } 
                     stmt.close();
		}catch (SQLException e) {
			e.printStackTrace();
		}

	try {
		conn.close();
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	return queryIds_PluginIds;
}

	public Map<String, Map<String, String>> getQueryIds_PluginNames_PluginIds(
			ArrayList<String> qIds) {
		// reuturns a map that has as key the queryId and as values the
		// pluginIds associated to the query

		// Map <String, ArrayList<String>> queryIds_PluginNames = new
		// HashMap<String, ArrayList<String>>();
		Map<String, Map<String, String>> queryIds_PluginNames_PluginIds = new HashMap<String, Map<String, String>>();
                Connection conn=getConnection();
                String query = "select workflows_plugins.idPlugin, plugins.PluginName "
					+ " from queries_workflows, workflows_plugins, plugins where "
					+ " queries_workflows.idQuery = ?"					
					+ " and "
					+ " workflows_plugins.idWorkflow = queries_workflows.idWorkflow and "
					+ " workflows_plugins.idPlugin = plugins.idPlugin";
		try {
                    PreparedStatement stmt = conn.prepareStatement(query);
                    for (String crtQid : qIds) {			
                        stmt.setString(1, crtQid);
                        ResultSet rsMetrics = stmt.executeQuery();

			// for a query Id we should have just one workflowDescription
			// I use a temporary map to check if errors occur,
			// that is check if for a query id to which two different
			// workflow ids correspond we have two different workflow
			// descriptions -- THIS would be wrong
			Map<String, String> pluginNames_pluginIds = new HashMap<String, String>();

			while (rsMetrics.next()) {
				String idplugin = rsMetrics.getString(1);
				String pluginName = rsMetrics.getString(2);
				pluginNames_pluginIds.put(pluginName, idplugin);
			}
			if (pluginNames_pluginIds.isEmpty()) {
				pluginNames_pluginIds.put("-1", "-1");
			}
			queryIds_PluginNames_PluginIds.put(crtQid,pluginNames_pluginIds);
                        rsMetrics.close();
			} 
                    stmt.close();
                }catch (SQLException e) {
			e.printStackTrace();
		}		

		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return queryIds_PluginNames_PluginIds;
	}


	public String getPluginName(String pluginID, Connection conn) {              
                String query = "select pluginName from plugins where idPlugin='"+pluginID+"'";
                String pluginName="-1";
		try {
                    PreparedStatement stmt = conn.prepareStatement(query);
                    ResultSet rsMetrics=stmt.executeQuery();
                    while (rsMetrics.next()) {
                            pluginName = rsMetrics.getString(1);
                        }
                    rsMetrics.close();
                    stmt.close();
            	}
                catch (SQLException e) {
			e.printStackTrace();
		}
		return pluginName;
	}


        public  HashMap<String, ArrayList<String>> getPluginMetricsForQueryID(String qId, ArrayList<String> pluginIds, 
                ArrayList<String> desiredPluginMetrics, Connection conn)
        {
            String desiredPluginMetricsValues = Configuration.convertArrayOfMetricsToString(desiredPluginMetrics);
            String pluginIdentifiers = Configuration.convertArrayOfMetricsToString(pluginIds);
            
            PreparedStatement stmt=null;
            ResultSet rsMetrics;
            /*
            String query = "select plugins_metrics.Value, metrics.MetricName from plugins_metrics, metrics where "
                + "plugins_metrics.idMetric = " + metricType 
                + " and plugins_metrics.idPlugin = '"
		+ pId
		+ "' "
		+ " and plugins_metrics.idMetric = metrics.idMetric";
            */
            HashMap<String, ArrayList<String>> pluginMetrics = new HashMap<String, ArrayList<String>>();
            //initialize, suppose all values are missing
            for (String s: pluginIds){
                ArrayList<String> mvals = new ArrayList<String>();
                for (int i = 0; i < desiredPluginMetrics.size(); i++)
                    mvals.add("-1");
                pluginMetrics.put(s, mvals);
            }
            
            String query = "select plugins_metrics.idPlugin, metrics.MetricName, plugins_metrics.Value, mtypes.TypeName"
                    + " from plugins_metrics, metrics, mtypes where "
                + " metrics.MetricName  in " 
                + desiredPluginMetricsValues
                + " and plugins_metrics.idPlugin in"
		+ pluginIdentifiers		
		+ " and plugins_metrics.idMetric = metrics.idMetric"
                + " and metrics.MetricType = mtypes.idType";
            
        String metricType = null;		
	String metricValue = null;
        String idPlugin = null;
        String metricName = null;
         
        ArrayList <String> mvals ;
        try {
            stmt = conn.prepareStatement(query);
            rsMetrics = stmt.executeQuery();
            while (rsMetrics.next()) {
                idPlugin = rsMetrics.getString(1);
                metricName = rsMetrics.getString(2);
                metricValue = rsMetrics.getString(3);
                metricType = rsMetrics.getString(4);
                mvals = pluginMetrics.get(idPlugin);
                if (mvals != null) {
                    int index = desiredPluginMetrics.indexOf(metricName);
                    mvals.set(index, metricValue);
                    pluginMetrics.put(idPlugin, mvals);
                }                
            }            
            rsMetrics.close();
            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(SqlToWekaExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }                                                    			
        return pluginMetrics;
       }
	
	public Map<String, HashMap<String, ArrayList<String>>> getQueryIds_pluginMetrics(
			Map<String, ArrayList<String>> qIdsPluginsMap,
			Map<String, ArrayList<String>> metricsNamesTypesIds,
			ArrayList<String> desiredPluginMetrics) {
		// get the given plugin metrics for a plugin id
		/*
		 * In the example below we extract the values for the metrics having the
		 * id 39, 40 and 50 for the plugin having the id
		 * 85b05efa-269f-4a8c-a339-45f7087be5a1 select plugins_metrics.Value,
		 * metrics.MetricName, metrics.idMetric from plugins_metrics, metrics
		 * where (plugins_metrics.idMetric = 39 or plugins_metrics.idMetric = 40
		 * or plugins_metrics.idMetric = 50 ) and plugins_metrics.idPlugin =
		 * '85b05efa-269f-4a8c-a339-45f7087be5a1' and plugins_metrics.idMetric =
		 * metrics.idMetric;
		 */

		Map<String, HashMap<String, ArrayList<String>>> queryIds_pluginsMetrics = new HashMap<String, HashMap<String, ArrayList<String>>>();
		
		String qId;
		ArrayList<String> pluginIds;
		// iterate over the map that stores the queryIds and the plugins
		Set s = qIdsPluginsMap.entrySet();
		Iterator it = s.iterator();
		int nbQueries = 0;
                Connection conn=getConnection();
		while (it.hasNext()) {		
		//	System.out.println("Processing query " + nbQueries++);
			Map.Entry m = (Map.Entry) it.next();
			qId = (String) m.getKey();
			pluginIds = (ArrayList<String>) m.getValue();

			// for each plugin id get the values of the metrics and put them in a map
			HashMap<String, ArrayList<String>> pluginMetrics = getPluginMetricsForQueryID(qId, pluginIds, desiredPluginMetrics, conn);
			queryIds_pluginsMetrics.put(qId, pluginMetrics);
		}
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return queryIds_pluginsMetrics;
	}
	
	public Map<String, String> getQueryIds_WorkflowDescription(
			ArrayList<String> qIds) {
		// returns a map containing as key the queryId and as values an
		// ArrayList
		// that has on the first position the WorklowDescription and on the next
		// positions the PluginIds that compose the workflow;


		Map<String, String> queryIds_workflowDescription = new HashMap<String, String>();                
                Connection conn=getConnection();
                PreparedStatement stmt=null;
                
                String query = "select queries_workflows.idWorkflow, workflows.WorkflowDescription from queries_workflows, workflows where "
			+ " queries_workflows.idQuery = ?"			
			+ " and "
			+ " queries_workflows.idWorkflow = workflows.idWorkflow ";
                try {
			stmt = conn.prepareStatement(query);                                             
			// for a query Id we should have just one workflowDescription
			// I use a temporary map to check if errors occur,
			// that is check if for a query id to which two different
			// workflow ids correspond we have two different workflow
			// descriptions -- THIS would be wrong
                        for (String crtQid: qIds) {
                            queryIds_workflowDescription.put(crtQid, "-1");
                            
                            stmt.setString(1, crtQid);                            
                            ResultSet rsMetrics = stmt.executeQuery();
                                                        
			    int nbDescriptions = 0;
			    String workflowDescription = "-1";
				while (rsMetrics.next()) {
					String idWorkflow = rsMetrics.getString(1);
					workflowDescription = rsMetrics.getString(2);
					nbDescriptions++;
				}				
				if (nbDescriptions > 1) {
					System.out.println("Too many descriptions for the current query !!!!");
				}
				queryIds_workflowDescription.put(crtQid, workflowDescription.replace(';','_').replaceAll(" ",""));
                                rsMetrics.close();
                         
                        }   
                        stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
                
	try {
		conn.close();
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	return queryIds_workflowDescription;
}

	public Map<String, String> getAttributeNamesAndTypes(String query) {
		// returns the names of the attributes and their types
		Map<String, String> attributesNamesTypesMap = new HashMap<String, String>();
                Connection conn=getConnection();

		try {
			// PreparedStatement stmt =
			// conn.prepareStatement("select metrics.Name, mtypes.Name from metrics, mtypes where metrics.MetricType = mtypes.idType and metrics.Name like '%query%'");
			PreparedStatement stmt = conn.prepareStatement(query);
			ResultSet rsMetrics = stmt.executeQuery();

			while (rsMetrics.next()) {
				String metricName = rsMetrics.getString(1);
				String metricType = rsMetrics.getString(2);
				attributesNamesTypesMap.put(metricName, metricType);
			}
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return attributesNamesTypesMap;
	}

	public Map<String, ArrayList<String>> getAttributeNamesTypesMetricsIds(
			String query) {
		// returns the names of the attributes and their types
		// the key of the map is the metric name and the values are the id of
		// the metric and the type of the metric
		Map<String, ArrayList<String>> attributesNamesTypesMap = new HashMap<String, ArrayList<String>>();

                Connection conn=getConnection();
		try {
			PreparedStatement stmt = conn.prepareStatement(query);
			ResultSet rsMetrics = stmt.executeQuery();

			while (rsMetrics.next()) {
				String metricName = rsMetrics.getString(1);
				ArrayList<String> typesIds = new ArrayList<String>();
				typesIds.add(rsMetrics.getString(2));
				typesIds.add(rsMetrics.getString(3));
				attributesNamesTypesMap.put(metricName, typesIds);
			}
			conn.close();
		} catch (SQLException e) {
		}
		return attributesNamesTypesMap;
	}

        public ArrayList<String> getQueryMetricsForID(String qId, ArrayList<String> desiredQueryMetrics, Connection conn)
        {
            String desiredQueryMetricsValues = Configuration.convertArrayOfMetricsToString(desiredQueryMetrics);           

            ArrayList<String> mvals = new ArrayList<String>(desiredQueryMetrics.size());
            for (int i=0;i<desiredQueryMetrics.size();i++)
                mvals.add("-1");

            String query = "select metrics.MetricName, queries_metrics.Value from queries_metrics, metrics where "
                            + " queries_metrics.idQuery = '"
                            + qId
                            + "' and "
                            + " metrics.idMetric = queries_metrics.idMetric "
                            + " and metrics.MetricName in "+desiredQueryMetricsValues;


            PreparedStatement stmt=null;
            try {
                stmt = conn.prepareStatement(query);
                ResultSet rsMetrics = stmt.executeQuery();
                while (rsMetrics.next())
                {
                    String metricName = rsMetrics.getString(1);
                    String metricValue = rsMetrics.getString(2);
                    int index=desiredQueryMetrics.indexOf(metricName);
                    mvals.set(index,metricValue);
            }
            } catch (SQLException ex) {
                Logger.getLogger(SqlToWekaExtractor.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                stmt.close();
            } catch (SQLException ex) {
                Logger.getLogger(SqlToWekaExtractor.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            return mvals;
        }

	public Map<String, ArrayList<String>> getQueryMetrics(
			ArrayList<String> qIds,
			Map<String, ArrayList<String>> metricsNamesTypesIds,
			ArrayList<String> desiredQueryMetrics) {
		// returns the metrics for the attributes listed in the map and for the
		// queries having the qIds

		Map<String, ArrayList<String>> queryMetrics = new HashMap<String, ArrayList<String>>();
                Connection conn=getConnection();

		for (int i = 0; i < qIds.size(); i++) {
			String crtQId = qIds.get(i);
                        ArrayList<String> mvals=getQueryMetricsForID(crtQId,desiredQueryMetrics,conn);
			queryMetrics.put(crtQId, mvals);
		}

		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return queryMetrics;
	}

        
        public Instances getQueryMetricsToInstances(Map<String, ArrayList<String>> metricsNamesTypesIds,
                                                    ArrayList<String> desiredQueryMetrics,
                                                    ArrayList<String> qIDs)
        {
            Connection conn=getConnection();
            
            Instances iSet=null;
            FastVector bool=new FastVector();
            bool.addElement("false");
            bool.addElement("true");

            FastVector vAttrib = new FastVector();
            vAttrib.addElement(new Attribute("QueryID",(FastVector) null)); //QueryID
            for (String metric: desiredQueryMetrics)
            {
                String type=metricsNamesTypesIds.get(metric).get(1);
                if (type!=null)
                {
                    if (type.equals("STRING"))
                        vAttrib.addElement(new Attribute(metric,(FastVector) null));
                    if (type.equals("CLASS"))
                        vAttrib.addElement(new Attribute(metric, bool));
                    if (type.equals("NUMERIC"))
                        vAttrib.addElement(new Attribute(metric));
                }
            }

            iSet=new Instances("relationARFF",vAttrib,0);
            double [] vals = new double[iSet.numAttributes()];

            for (String qID: qIDs)
            {
                List<Integer> attrWithValues = new ArrayList<Integer>();

                vals[0]=iSet.attribute(0).addStringValue(qID);
                attrWithValues.add(0);

                int count=0;
                ArrayList<String> result=getQueryMetricsForID(qID, desiredQueryMetrics, conn);
   
                for (String metric: desiredQueryMetrics)
                {
                    String type=metricsNamesTypesIds.get(metric).get(1);
                    if (type!=null)
                    {
                        int position=iSet.attribute(metric).index();
                        String attrValue=result.get(count);
                        if (!(attrValue.equals("-1")||attrValue.equals("?")))
                        {
                            attrWithValues.add(position);
                            if (iSet.attribute(metric).isNominal())
                                vals[position] = bool.indexOf(attrValue);
                            if (iSet.attribute(metric).isString())
                                vals[position]=iSet.attribute(metric).addStringValue(attrValue);
                            if (iSet.attribute(metric).isNumeric())
                                vals[position]=Double.parseDouble(attrValue);
                        }
                    }
                    count++;
                }

                Instance in=new Instance(1.0, vals);
                for (int i=0;i<iSet.numAttributes();i++)
                    if (!attrWithValues.contains(i))
                       in.setMissing(i);
                iSet.add(in);

            }

            return iSet;
        }
        
      public Instances getPluginMetricsToInstances(Map<String, ArrayList<String>> metricsNamesTypesIds,
                                                    ArrayList<String> desiredPluginMetrics,
                                                    ArrayList<String> qIDs, 
                                                    Map <String, ArrayList<String>> queryIds_PluginIds,
                                                    Map <String,String> queryIds_workflowDescription
                                                    )
        {
            Connection conn=getConnection();
            
            Instances iSet=null;
            FastVector bool=new FastVector();
            bool.addElement("false");
            bool.addElement("true");

            Set<String> wfclassSet=new HashSet<String>();
            for (String v: queryIds_workflowDescription.values())
                wfclassSet.add(v);

            FastVector wfclass=new FastVector();
            for (String v: wfclassSet)
                wfclass.addElement(v);

            FastVector vAttrib = new FastVector();
            vAttrib.addElement(new Attribute("QueryID",(FastVector) null)); //QueryID
            vAttrib.addElement(new Attribute("PluginID",(FastVector) null)); //PluginID
            vAttrib.addElement(new Attribute("PluginName",(FastVector) null)); //PluginID
            vAttrib.addElement(new Attribute("WorkflowClass", wfclass));

            for (String metric: desiredPluginMetrics)
            {
                String type=metricsNamesTypesIds.get(metric).get(1);
                if (type!=null)
                {
                    if (type.equals("STRING"))
                        vAttrib.addElement(new Attribute(metric,(FastVector) null));
                    if (type.equals("CLASS"))
                        vAttrib.addElement(new Attribute(metric, bool));
                    if (type.equals("NUMERIC"))
                        vAttrib.addElement(new Attribute(metric));
                }
            }

            iSet=new Instances("relationARFF",vAttrib,0);
         //   double [] vals = new double[iSet.numAttributes()];
            double [] vals;
            int crtqid = 0;
            for (String qID: qIDs)
            {
//                System.out.println(crtqid++);
                ArrayList<String> pluginIds = queryIds_PluginIds.get(qID);
                HashMap<String, ArrayList<String>> result = getPluginMetricsForQueryID(qID, pluginIds, desiredPluginMetrics, conn) ;

                for (String pluginId : result.keySet()) {
                    if (pluginId.equals("-1"))
                        continue;

                    vals = new double[iSet.numAttributes()];

                    List<Integer> attrWithValues = new ArrayList<Integer>();

                    vals[0]=iSet.attribute(0).addStringValue(qID);
                    attrWithValues.add(0);
                    vals[1]=iSet.attribute(1).addStringValue(pluginId);
                    attrWithValues.add(1);
                    vals[2]=iSet.attribute(2).addStringValue(getPluginName(pluginId, conn));
                    attrWithValues.add(2);
                    vals[3]=wfclass.indexOf(queryIds_workflowDescription.get(qID));
                    attrWithValues.add(3);

                    int count=0;
                    
                    for (String metric : desiredPluginMetrics) {
                        String type = metricsNamesTypesIds.get(metric).get(1);
                        if (type != null) {
                            int position = iSet.attribute(metric).index();
                            String attrValue = result.get(pluginId).get(count);
                            if (!(attrValue.equals("-1") || attrValue.equals("?"))) {
                                attrWithValues.add(position);
                                if (iSet.attribute(metric).isNominal()) {
                                    vals[position] = bool.indexOf(attrValue);
                                }
                                if (iSet.attribute(metric).isString()) {
                                    vals[position] = iSet.attribute(metric).addStringValue(attrValue);
                                }
                                if (iSet.attribute(metric).isNumeric()) {
                                    vals[position] = Double.parseDouble(attrValue);
                                }
                            }
                        }
                        count++;
                    }
                    Instance in = new Instance(1.0, vals);
                    for (int i = 0; i < iSet.numAttributes(); i++) {
                        if (!attrWithValues.contains(i)) {
                            in.setMissing(i);
                        }
                    }
                  //   System.out.println("Instance \n -----\n " + in + " ----- \n");
                    iSet.add(in);
                 //   System.out.println("Iset \n -----\n " + iSet + " ----- \n");
                }
            }
            return iSet;
        }


        public Instances getQueryWorkflowsPlatformSystemMetricsToInstances(Map<String, ArrayList<String>> queryMetricsNamesTypesIds,
                                                                ArrayList<String> desiredQueryMetrics,
                                                                Map<String, ArrayList<String>> workflowMetricsNamesTypesIds,
                                                                ArrayList<String> desiredWorkflowMetrics,
                                                                Map<String,String> queryIds_workflowDescription,
                                                                Map<String, ArrayList<String>> platformMetricsNamesTypesIds,
                                                                ArrayList<String> desiredPlatformMetrics,
                                                                Map<String, String> queryIds_PlatformIds,
                                                                Map<String, ArrayList<String>> systemMetricsNamesTypesIds,
                                                                ArrayList<String> desiredSystemMetrics,
                                                                Map<String, String> queryIds_SystemIds,
                                                                ArrayList<String> qIDs) 
                
        {

            Connection conn=getConnection();
            
            Instances iSet=null;
            FastVector bool=new FastVector();
            bool.addElement("false");
            bool.addElement("true");

            Set<String> wfclassSet=new HashSet<String>();
            for (String v: queryIds_workflowDescription.values())
                wfclassSet.add(v);

            FastVector wfclass=new FastVector();
            for (String v: wfclassSet)
                wfclass.addElement(v);

            FastVector vAttrib = new FastVector();
            vAttrib.addElement(new Attribute("QueryID",(FastVector) null)); //QueryID
            vAttrib.addElement(new Attribute("WorkflowClass", wfclass));
            
            for (String metric: desiredQueryMetrics)
            {
                String type=queryMetricsNamesTypesIds.get(metric).get(1);
                if (type!=null)
                {
                    if (type.equals("STRING"))
                        vAttrib.addElement(new Attribute(metric,(FastVector) null));
                    if (type.equals("CLASS"))
                        vAttrib.addElement(new Attribute(metric, bool));
                    if (type.equals("NUMERIC"))
                        vAttrib.addElement(new Attribute(metric));
                }
            }
            for (String metric: desiredWorkflowMetrics)
            {
                String type=workflowMetricsNamesTypesIds.get(metric).get(1);
                if (type!=null)
                {
                    if (type.equals("STRING"))
                        vAttrib.addElement(new Attribute(metric,(FastVector) null));
                    if (type.equals("CLASS"))
                        vAttrib.addElement(new Attribute(metric, bool));
                    if (type.equals("NUMERIC"))
                        vAttrib.addElement(new Attribute(metric));
                }
            }
            for (String metric: desiredPlatformMetrics)
            {
                String type=platformMetricsNamesTypesIds.get(metric).get(1);
                if (type!=null)
                {
                    if (type.equals("STRING"))
                        vAttrib.addElement(new Attribute(metric,(FastVector) null));
                    if (type.equals("CLASS"))
                        vAttrib.addElement(new Attribute(metric, bool));
                    if (type.equals("NUMERIC"))
                        vAttrib.addElement(new Attribute(metric));
                }
            }

             for (String metric: desiredSystemMetrics)
            {
                String type=systemMetricsNamesTypesIds.get(metric).get(1);
                if (type!=null)
                {
                    if (type.equals("STRING"))
                        vAttrib.addElement(new Attribute(metric,(FastVector) null));
                    if (type.equals("CLASS"))
                        vAttrib.addElement(new Attribute(metric, bool));
                    if (type.equals("NUMERIC"))
                        vAttrib.addElement(new Attribute(metric));
                }
            }
            
            iSet=new Instances("relationARFF",vAttrib,0);
            //double [] vals = new double[iSet.numAttributes()];
            double [] vals ;
            int crtQid = 0;
            for (String qID: qIDs)
            {
//                System.out.println(crtQid++);
                vals = new double[iSet.numAttributes()];
                List<Integer> attrWithValues = new ArrayList<Integer>();

                vals[0]=iSet.attribute(0).addStringValue(qID);
                attrWithValues.add(0);
                vals[1]=wfclass.indexOf(queryIds_workflowDescription.get(qID));
                attrWithValues.add(1);

                int count=0;
                ArrayList<String> result=getQueryMetricsForID(qID, desiredQueryMetrics, conn);
                for (String metric: desiredQueryMetrics)
                {
                    String type=queryMetricsNamesTypesIds.get(metric).get(1);
                    if (type!=null)
                    {
                        int position=iSet.attribute(metric).index();
                        String attrValue= result.get(count);
                        if (!(attrValue.equals("-1")||attrValue.equals("?")))
                        {
                            attrWithValues.add(position);
                            if (iSet.attribute(metric).isNominal())
                                vals[position] = bool.indexOf(attrValue);
                            if (iSet.attribute(metric).isString())
                                vals[position]=iSet.attribute(metric).addStringValue(attrValue);
                            if (iSet.attribute(metric).isNumeric())
                                vals[position]=Double.parseDouble(attrValue);
                        }
                    }
                    count++;
                }

                count=0;
                result=getWorkflowMetricsUnifiedForQueryID(qID, desiredWorkflowMetrics, conn);

                for (String metric: desiredWorkflowMetrics)
                {
                    String type=workflowMetricsNamesTypesIds.get(metric).get(1);
                    if (type!=null)
                    {
                        int position=iSet.attribute(metric).index();
                        String attrValue= result.get(count);
                        if (!(attrValue.equals("-1")||attrValue.equals("?")))
                        {
                            attrWithValues.add(position);
                            if (iSet.attribute(metric).isNominal())
                                vals[position] = bool.indexOf(attrValue);
                            if (iSet.attribute(metric).isString())
                                vals[position]=iSet.attribute(metric).addStringValue(attrValue);
                            if (iSet.attribute(metric).isNumeric())
                                vals[position]=Double.parseDouble(attrValue);
                        }
                    }
                    count++;
                }


                count=0;
                result=getPlatformMetricsForPlatformIDQueryID(queryIds_PlatformIds.get(qID),qID, desiredPlatformMetrics, conn);

                for (String metric: desiredPlatformMetrics)
                {
                    String type=platformMetricsNamesTypesIds.get(metric).get(1);
                    if (type!=null)
                    {
                        int position=iSet.attribute(metric).index();
                        String attrValue= result.get(count);
                        if (!(attrValue.equals("-1")||attrValue.equals("?")))
                        {
                            attrWithValues.add(position);
                            if (iSet.attribute(metric).isNominal())
                                vals[position] = bool.indexOf(attrValue);
                            if (iSet.attribute(metric).isString())
                                vals[position]=iSet.attribute(metric).addStringValue(attrValue);
                            if (iSet.attribute(metric).isNumeric())
                                vals[position]=Double.parseDouble(attrValue);
                        }
                    }
                    count++;
                }
                
                count=0;
                result=getSystemMetricsForQueryIDSystemID(queryIds_SystemIds.get(qID),qID, desiredSystemMetrics, conn);

                for (String metric: desiredSystemMetrics)
                {
                    String type=systemMetricsNamesTypesIds.get(metric).get(1);
                    if (type!=null)
                    {
                        int position=iSet.attribute(metric).index();
                        String attrValue= result.get(count);
                        if (!(attrValue.equals("-1")||attrValue.equals("?")))
                        {
                            attrWithValues.add(position);
                            if (iSet.attribute(metric).isNominal())
                                vals[position] = bool.indexOf(attrValue);
                            if (iSet.attribute(metric).isString())
                                vals[position]=iSet.attribute(metric).addStringValue(attrValue);
                            if (iSet.attribute(metric).isNumeric())
                                vals[position]=Double.parseDouble(attrValue);
                        }
                    }
                    count++;
                }

                Instance in=new Instance(1.0, vals);
                for (int i=0;i<iSet.numAttributes();i++)
                    if (!attrWithValues.contains(i))
                        in.setMissing(i);
                iSet.add(in);

            }

            return iSet;
        }


	public Map<String, HashMap<String, ArrayList<String>>> getWorkflowMetrics(
			ArrayList<String> qIds,
			Map<String, ArrayList<String>> metricsNamesTypesIds,
			ArrayList<String> desiredWorkflowMetrics) {

		// returns the metrics for the metrics listed in the
		// desiredWorkflowMetrics for the queries having the qIds
		// the structure is the following: the first key (outer map) contains
		// the queryid
		// to each queryId we associate another map (inner map), that contains a
		// maximum of two elements;
		// the key of the inner map is the workflowId ;
		// to a queryId there correspond two different workflowIds because for a
		// workflow we measure two different methods

		Map<String, HashMap<String, ArrayList<String>>> workflowMetrics = new HashMap<String, HashMap<String, ArrayList<String>>>();
		// import org.apache.commons.collections.MultiHashMap;
		// MultiHashMap<String, ArrayList<String>> mm = new MultiHashMap<String,
		// ArrayList<String>>;

                Connection conn=getConnection();
		for (int i = 0; i < qIds.size(); i++) {
			String crtQId = qIds.get(i);

			// find the workflowIds corresponding to this query:
			String queryWorkflowIds = "select queries_workflows.idWorkflow from queries_workflows where "
					+ "queries_workflows.idQuery = '" + crtQId + "' ;";

			ArrayList<String> workflowIdsForAQueryId = new ArrayList<String>();
			try {
				PreparedStatement stmt = conn
						.prepareStatement(queryWorkflowIds);
				ResultSet rsMetrics = stmt.executeQuery();

				while (rsMetrics.next()) {
					String wId = rsMetrics.getString(1);
					workflowIdsForAQueryId.add(wId);
				}
				// for the current queryId and for each of the workflowIds find
				// the needed metrics
				// for a queryId find all the workflowIds, and for each
				// workflowId find the desired metrics

				HashMap<String, ArrayList<String>> mapWorkflows = new HashMap<String, ArrayList<String>>();

				for (int k = 0; k < workflowIdsForAQueryId.size(); k++) {
					String wId = workflowIdsForAQueryId.get(k);

					ArrayList<String> metricsForWid = new ArrayList<String>();

					for (int j = 0; j < desiredWorkflowMetrics.size(); j++) {
						String workflowMetricName = desiredWorkflowMetrics
								.get(j);

						if (metricsNamesTypesIds
								.containsKey(workflowMetricName)) {
							String workflowMetricId = metricsNamesTypesIds.get(
									workflowMetricName).get(0);

							String query = "select metrics.MetricName, workflows_metrics.Value, workflows_metrics.idWorkflow from "
									+ " workflows_metrics, metrics, queries_workflows where "
									+ " queries_workflows.idQuery = '"
									+ crtQId
									+ "' "
									+ " and queries_workflows.idWorkflow = workflows_metrics.idWorkflow and "
									+ " workflows_metrics.idMetric = metrics.idMetric "
									+ " and metrics.idMetric = "
									+ workflowMetricId
									+ " and workflows_metrics.idWorkflow = '"
									+ wId + "'";

							// execute the query and add all the metrics to the
							// metricsForWid
							stmt = conn.prepareStatement(query);
							rsMetrics = stmt.executeQuery();

							while (rsMetrics.next()) {
								String mName = rsMetrics.getString(1);
								String mValue = rsMetrics.getString(2);
								metricsForWid.add(mValue);
							}
						} else {
							// the metric name is not in the list of available
							// metrics, hence add a '?' or don't add it at all
							metricsForWid.add("?"); // the metric name was not
													// found in the list of
													// metrics -- could be a
													// wrong name
						}
					}
					mapWorkflows.put(wId, metricsForWid);
				}
				workflowMetrics.put(crtQId, mapWorkflows);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return workflowMetrics;
	}



    public ArrayList<String> getWorkflowMetricsUnifiedForQueryID(String qId, ArrayList<String> desiredWorkflowMetrics, Connection conn)
    {
        String desiredWorkflowMetricsValues = Configuration.convertArrayOfMetricsToString(desiredWorkflowMetrics);           

        ArrayList<String> mvals = new ArrayList<String>(desiredWorkflowMetrics.size());
        ArrayList<String> mvals2 = new ArrayList<String>(desiredWorkflowMetrics.size());
        ArrayList<String> mtypes = new ArrayList<String>(desiredWorkflowMetrics.size());
        for (int i=0;i<desiredWorkflowMetrics.size();i++) {
             mvals.add("-1");            
             mvals2.add("-1");            
             mtypes.add("-1");            
        }
           
       String query = "select metrics.MetricName, workflows_metrics.Value, mtypes.typename from "
                  + " workflows_metrics, metrics, queries_workflows, mtypes where "
                  + " queries_workflows.idQuery = '"
                  + qId
                  + "' "
                  + " and queries_workflows.idWorkflow = workflows_metrics.idWorkflow and "
                  + " workflows_metrics.idMetric = metrics.idMetric "
                  + " and metrics.MetricName in " + desiredWorkflowMetricsValues
                  + " and metrics.MetricType = mtypes.idtype";

        try {
                PreparedStatement stmt=null;
                ResultSet rsMetrics;

                stmt = conn.prepareStatement(query);
                rsMetrics = stmt.executeQuery();
                
                 String mType=null;
                 while (rsMetrics.next()) {
                       String mName = rsMetrics.getString(1);
                       String mValue = rsMetrics.getString(2);
                       mType=rsMetrics.getString(3);                       
                       int index = desiredWorkflowMetrics.indexOf(mName);     
                       
                       //check what type of metric do we have:
                       if (mType!=null && mType.equalsIgnoreCase("NUMERIC")) {
                           if (mName.equalsIgnoreCase("WorkflowNumberOfPlugins"))
                               mvals.set(index, mValue);
                           else if (mName.equalsIgnoreCase("WorkflowAllocatedMemoryBefore")
                                   || mName.equalsIgnoreCase("WorkflowAllocatedMemoryAfter")
                                   || mName.equalsIgnoreCase("WorkflowUsedMemoryBefore")
                                   || mName.equalsIgnoreCase("WorkflowUsedMemoryAfter")
                                   || mName.equalsIgnoreCase("WorkflowFreeMemoryBefore")
                                   || mName.equalsIgnoreCase("WorkflowFreeMemoryAfter")
                                   || mName.equalsIgnoreCase("WorkflowUnallocatedMemoryBefore")
                                   || mName.equalsIgnoreCase("WorkflowUnallocatedMemoryAfter")) {
                               // take the maximum of these metrics
                               double v1 = Double.parseDouble(mvals.get(index));
                               double v2 = Double.parseDouble(mValue);
                               double maxv = v1 > v2 ? v1 : v2;
                               mvals.set(index, Double.toString(maxv));
                           } else {
                               // add the values
                               double v1 = Double.parseDouble(mvals.get(index));
                               double v2 = Double.parseDouble(mValue);
                               if (v1 == -1) v1 = 0;
                               double sum = v1 + v2;
                               mvals.set(index, Double.toString(sum));
                           }
                       }
                       else { //not numeric, leave it as it is, put one of the values
                           mvals.set(index, mValue);                           
                       }                       
                  }                  
                rsMetrics.close();               
                stmt.close();
        } catch (SQLException e) {
                e.printStackTrace();
        }
        return mvals;
    }


	public Map<String, ArrayList<String>> getWorkflowMetricsUnified(
			ArrayList<String> qIds,
			Map<String, ArrayList<String>> metricsNamesTypesIds,
			ArrayList<String> desiredWorkflowMetrics) {

		// returns the metrics for the metrics listed in the
		// desiredWorkflowMetrics for the queries having the qIds
		// the structure is the following: the key contains the queryid
		// and to each queryId we associate an arrayList of String that contain
		// the metrics associated to a workflow;
		// We know that to each queryId there corresponds two different workflow
		// ids; in this function, the values of the metrics
		// associated to the two workflow ids are unified (we add all the
		// values, except the ones that refer to memory for which we take the
		// maximum

		Map<String, ArrayList<String>> workflowMetrics = new HashMap<String, ArrayList<String>>();

                Connection conn=getConnection();

		for (int i = 0; i < qIds.size(); i++) {
			String crtQId = qIds.get(i);

			// find the workflowIds corresponding to this query:
			// String queryWorkflowIds =
			// "select queries_workflows.idWorkflow from queries_workflows where "
			// +
			// "queries_workflows.idQuery = '"+ crtQId +"' ;";

                        ArrayList<String> crtQIdWorklowMetrics=getWorkflowMetricsUnifiedForQueryID(crtQId, desiredWorkflowMetrics, conn);
                        workflowMetrics.put(crtQId, crtQIdWorklowMetrics);
                }
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return workflowMetrics;
	}

	/*
	 * public Map<String, HashMap<String, ArrayList<String>>>
	 * getPluginMetrics(ArrayList<String> qIds, Map<String, ArrayList<String>>
	 * metricsNamesTypesIds, ArrayList<String> desiredPluginMetrics){
	 * 
	 * //returns the metrics listed in the desiredPluginMetrics list for the
	 * queries having the qIds
	 * 
	 * //the structure is the following: the first key (outer map) contains the
	 * queryId
	 * 
	 * // to each queryId we associate another map (inner map), that contains a
	 * number of elements equal to the number of plugins in the // the key of
	 * the inner map is the workflowId ; // to a queryId there correspond two
	 * different workflowIds because for a workflow we measure two different
	 * methods
	 * 
	 * Map<String, HashMap<String, ArrayList<String>>> pluginMetrics = new
	 * HashMap<String, HashMap<String, ArrayList<String>>>();
	 * 
	 * for (int i = 0; i < qIds.size(); i++){ String crtQId = qIds.get(i);
	 * 
	 * //find the workflowIds corresponding to this query: String
	 * queryWorkflowIds =
	 * "select queries_workflows.idWorkflow from queries_workflows where " +
	 * "queries_workflows.idQuery = '"+ crtQId +"' ;";
	 * 
	 * Connection conn = getConnection("larkc", "root", "raluca"); ArrayList
	 * <String> workflowIdsForAQueryId = new ArrayList<String>(); try {
	 * PreparedStatement stmt = conn.prepareStatement(queryWorkflowIds);
	 * ResultSet rsMetrics = stmt.executeQuery();
	 * 
	 * while (rsMetrics.next()) { String wId = rsMetrics.getString(1);
	 * workflowIdsForAQueryId.add(wId); } //for the current queryId and for each
	 * of the workflowIds find the needed metrics //for a queryId find all the
	 * workflowIds, and for each workflowId find the desired metrics
	 * 
	 * HashMap<String, ArrayList<String>> mapWorkflows = new HashMap<String,
	 * ArrayList<String>>();
	 * 
	 * for (int k = 0; k < workflowIdsForAQueryId.size(); k++) { String wId =
	 * workflowIdsForAQueryId.get(k);
	 * 
	 * ArrayList <String> metricsForWid = new ArrayList<String>();
	 * 
	 * for (int j = 0; j < desiredPluginMetrics.size(); j++) { String
	 * workflowMetricName = desiredPluginMetrics.get(j);
	 * 
	 * if (metricsNamesTypesIds.containsKey(workflowMetricName)) { String
	 * workflowMetricId = metricsNamesTypesIds.get(workflowMetricName).get(0);
	 * 
	 * String query =
	 * "select metrics.MetricName, workflows_metrics.Value, workflows_metrics.idWorkflow from "
	 * + " workflows_metrics, metrics, queries_workflows where " +
	 * " queries_workflows.idQuery = '" + crtQId + "' " +
	 * " and queries_workflows.idWorkflow = workflows_metrics.idWorkflow and " +
	 * " workflows_metrics.idMetric = metrics.idMetric " +
	 * " and metrics.idMetric = " + workflowMetricId +
	 * " and workflows_metrics.idWorkflow = '" + wId + "'";
	 * 
	 * //execute the query and add all the metrics to the metricsForWid stmt =
	 * conn.prepareStatement(query); rsMetrics = stmt.executeQuery();
	 * 
	 * while (rsMetrics.next()) { String mName = rsMetrics.getString(1); String
	 * mValue = rsMetrics.getString(2); metricsForWid.add(mValue); } } else {
	 * //the metric name is not in the list of available metrics, hence add a
	 * '?' or don't add it at all metricsForWid.add("?"); //the metric name was
	 * not found in the list of metrics -- could be a wrong name } }
	 * mapWorkflows.put(wId, metricsForWid); } conn.close();
	 * workflowMetrics.put(crtQId, mapWorkflows); } catch (SQLException e) {
	 * e.printStackTrace(); } } return workflowMetrics; }
	 */

	public void test() {

/*        db=new MySqlConnect(jTextFieldURL.getText(),jTextFieldDriver.getText(),jTextFieldDBName.getText(),jTextFieldUserName.getText(),new String(jPasswordField.getPassword()));

        if (db.conn==null)
            jTextStatusDB.setText("Connection failed!");
        else
            jTextStatusDB.setText("Connected successfully!");

        System.out.println((dfStart.getDate().getYear()+1900)+"-"+(dfStart.getDate().getMonth()+1)+"-"+(dfStart.getDate().getDate()));
        System.out.println((dfEnd.getDate().getYear()+1900)+"-"+(dfEnd.getDate().getMonth()+1)+"-"+(dfEnd.getDate().getDate()));

        Instances qInst=db.getQueryMetrics();

        DataPreprocessor.outputArffInstances(qInst, "D:/testdb.arff");
 */

            Connection conn=getConnection();
		try {
			PreparedStatement stmt = conn.prepareStatement("select metrics.metricname, mtypes.typename from metrics, mtypes where metrics.metrictype=mtypes.idType");
			ResultSet rsMetrics = stmt.executeQuery();

			FastVector bool = new FastVector();
			bool.addElement("false");
			bool.addElement("true");

			FastVector vAttrib = new FastVector();
			vAttrib.addElement(new Attribute("QueryID", (FastVector) null)); // QueryID
			while (rsMetrics.next()) {
				String s = rsMetrics.getString(1);
				if (s.startsWith("Query")) {
					String t = rsMetrics.getString(2); // attribute type
					if (t.equals("STRING"))
						vAttrib.addElement(new Attribute(s, (FastVector) null));
					if (t.equals("CLASS"))
						vAttrib.addElement(new Attribute(s, bool));
					if (t.equals("NUMERIC"))
						vAttrib.addElement(new Attribute(s));
				}
			}
                        rsMetrics.close();

			Instances iSet = new Instances("relationARFF", vAttrib, 0);
			// DataPreprocessor.outputArffInstances(iSet, "D:/attr.arff");

			stmt = conn.prepareStatement("select idQuery from queries");
			ResultSet rsIdQueries = stmt.executeQuery();

			stmt = conn.prepareStatement("select queries.idQuery, metrics.metricname, value from queries, queries_metrics,metrics where queries.idQuery=queries_metrics.idQuery and queries_metrics.idMetric=metrics.idMetric and queries.idQuery=?");
			while (rsIdQueries.next()) {
				String id = rsIdQueries.getString(1);
				stmt.setString(1, id);
				ResultSet rsData = stmt.executeQuery();

				double[] vals = new double[iSet.numAttributes()];
				List<Integer> attrWithValues = new ArrayList<Integer>();

				vals[0] = iSet.attribute(0).addStringValue(id);
				attrWithValues.add(0);
				while (rsData.next()) {
					String attrName = rsData.getString(2);
					String attrValue = rsData.getString(3);
					int position = iSet.attribute(attrName).index();
					attrWithValues.add(position);
					if (iSet.attribute(attrName).isNominal())
						vals[position] = bool.indexOf(attrValue);
					if (iSet.attribute(attrName).isString())
						vals[position] = iSet.attribute(attrName)
								.addStringValue(attrValue);
					if (iSet.attribute(attrName).isNumeric())
						vals[position] = Double.parseDouble(attrValue);

				}

				Instance in = new Instance(1.0, vals);
				for (int i = 0; i < iSet.numAttributes(); i++)
					if (!attrWithValues.contains(i))
						in.setMissing(i);

				iSet.add(in);

                                rsData.close();
			}

                        rsIdQueries.close();
                        rsIdQueries.close();
                        stmt.close();
                        
			//System.out.println(iSet);
			DataPreprocessor.outputArffInstances(iSet, "D:/attr.arff");

			conn.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		/*
		 * try { InstanceQuery query = new InstanceQuery();
		 * query.setUsername("root"); query.setPassword("root");
		 * query.setQuery("select * from queries"); // if your data is sparse,
		 * then you can say so too // query.setSparseData(true); Instances data
		 * = query.retrieveInstances();
		 * DataPreprocessor.outputArffInstances(data, "D:/ttt.arff"); } catch
		 * (Exception ex) {
		 * Logger.getLogger(MySqlConnect.class.getName()).log(Level.SEVERE,
		 * null, ex); }
		 */

	}

        public boolean tryConnection()
        {
            Connection c=getConnection();
            boolean success;
            if (c==null)
                success=false;
            else
                success=true;

            try {
                c.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            return success;

        }
        
        public Map<String, String> getQueryIds_ApplicationName(
		ArrayList<String> qIds) {
	// reuturns a map that has as key the queryId and as values the
	// applicationName associated to the query

	Map<String, String> queryIds_ApplicationName = new HashMap<String, String>();
            Connection conn=getConnection();
            PreparedStatement stmt = null;
            
            String query = "select distinct queries_workflows.idQuery, platforms.idPlatform, " +
            		" platforms.ApplicationName from " +
            		" queries_workflows, platforms_workflows, platforms " +
            		" where " +
            		" queries_workflows.idQuery = ? and " +
            		" queries_workflows.idWorkflow = platforms_workflows.idWorkflow and" +
            		" platforms_workflows.idPlatform = platforms.idPlatform" ;      		;
            
            try {
                 stmt = conn.prepareStatement(query);			
                 for (String crtQid : qIds) {
                    stmt.setString(1, crtQid);
                	ResultSet rsMetrics = stmt.executeQuery();
                	String appName = "-1";
                	while (rsMetrics.next()) {
                		String idQuery = rsMetrics.getString(1);
                		appName = rsMetrics.getString(3);
                	}
                	queryIds_ApplicationName.put(crtQid, appName);
                	rsMetrics.close(); 
                 } 
                 stmt.close();
            }catch (SQLException e) {
            	e.printStackTrace();
            }

            try {
            	conn.close();
            } catch (SQLException e) {
            	e.printStackTrace();
            }
            return queryIds_ApplicationName;
	}
        
        public ArrayList<Double> getLatestPlatformMetrics(ArrayList<String> whichMetrics)
        {
            ArrayList<Double> platformM = new ArrayList<Double>();
            for (int i = 0; i < whichMetrics.size(); i++) 
                platformM.add(-1.0);
            
            Connection conn = getConnection();
            
            String whichMetricsModif =  Configuration.convertArrayOfMetricsToString(whichMetrics);
            
            String query = "select platforms_metrics.Value, platforms_metrics.Timestamp, metrics.MetricName from platforms_metrics, metrics "+
                            "where platforms_metrics.idMetric = metrics.idMetric "+
                            "and metrics.MetricName in "+ whichMetricsModif +
                            " order by platforms_metrics.Timestamp desc "+
                            "limit " + 16;
            
            try
            {
                PreparedStatement stmt = conn.prepareStatement(query);
                ResultSet rsMetrics = stmt.executeQuery();
                
                String name = "";
                
                while (rsMetrics.next())
                {
                    name = rsMetrics.getString(3);
                    platformM.set(whichMetrics.indexOf(name), Double.parseDouble(rsMetrics.getString(1)));
                }
                
                rsMetrics.close();
                stmt.close();
            }
            catch(SQLException e)
            {
                e.printStackTrace();
            }
            
            return platformM;
        }
        
        public ArrayList<Double> getLatestSystemMetrics(ArrayList<String> whichMetrics)
        {
            ArrayList<Double> systemM = new ArrayList<Double>();
            for (int i = 0; i < whichMetrics.size(); i++) 
                systemM.add(-1.0);
            
            Connection conn = getConnection();
            
            String whichMetricsModif =  Configuration.convertArrayOfMetricsToString(whichMetrics);
            
            String query = "select systems_metrics.Value, systems_metrics.Timestamp, metrics.MetricName from systems_metrics, metrics "+
                            "where systems_metrics.idMetric = metrics.idMetric "+
                            "and metrics.MetricName in "+ whichMetricsModif +
                            "order by systems_metrics.Timestamp desc "+
                            "limit " + 28;
            
            try
            {
                PreparedStatement stmt = conn.prepareStatement(query);
                ResultSet rsMetrics = stmt.executeQuery();
                
                String name = "";
                
                while (rsMetrics.next())
                {
                    name = rsMetrics.getString(3);
                    systemM.set(whichMetrics.indexOf(name), Double.parseDouble(rsMetrics.getString(1)));
                }
                
                rsMetrics.close();
                stmt.close();
            }
            catch(SQLException e)
            {
                e.printStackTrace();
            }
            
            return systemM;
        }
}
