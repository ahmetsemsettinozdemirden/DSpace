/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.statistics.content;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.dspace.content.DCDate;
import org.dspace.content.DSpaceObject;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.services.ConfigurationService;
import org.dspace.statistics.Dataset;
import org.dspace.statistics.ObjectCount;
import org.dspace.statistics.SolrLogger;
import org.dspace.statistics.content.filter.StatisticsFilter;
import org.dspace.utils.DSpace;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;

/**
 * A workflow data implementation that will query the statistics backend for workflow information
 *
 * @author Kevin Van de Velde (kevin at atmire dot com)
 * @author Ben Bosman (ben at atmire dot com)
 * @author Mark Diggory (markd at atmire dot com)
 */
public class StatisticsDataWorkflow extends StatisticsData {

    private static final Logger log = Logger.getLogger(StatisticsDataWorkflow.class);

    /** Current DSpaceObject for which to generate the statistics. */
    private DSpaceObject currentDso;
    /** Variable used to indicate of how many months an average is required (-1 is inactive) **/
    private long averageMonths = -1;
    
    DSpace dspace = new DSpace();

    SolrLogger searcher = dspace.getServiceManager().getServiceByName(SolrLogger.class.getName(),SolrLogger.class);

    public StatisticsDataWorkflow(DSpaceObject dso, int averageMonths) {
        super();
        this.currentDso = dso;
        this.averageMonths = averageMonths;
    }


    @Override
    public Dataset createDataset(Context context) throws SQLException, SolrServerException, IOException, ParseException {
                // Check if we already have one.
        // If we do then give it back.
        if(getDataset() != null)
        {
            return getDataset();
        }

        List<StatisticsFilter> filters = getFilters();
        List<String> defaultFilters = new ArrayList<String>();
        for (StatisticsFilter statisticsFilter : filters) {
            defaultFilters.add(statisticsFilter.toQuery());
        }

        String defaultFilterQuery = StringUtils.join(defaultFilters.iterator(), " AND ");

        String query = getQuery();

        Dataset dataset = new Dataset(0,0);
        List<DatasetGenerator> datasetGenerators = getDatasetGenerators();
        if(0 < datasetGenerators.size()){
            //At the moment we can only have one dataset generator
            DatasetGenerator datasetGenerator = datasetGenerators.get(0);
            if(datasetGenerator instanceof DatasetTypeGenerator){
                DatasetTypeGenerator typeGenerator = (DatasetTypeGenerator) datasetGenerator;
                ObjectCount[] topCounts = searcher.queryFacetField(query, defaultFilterQuery, typeGenerator.getType(), typeGenerator.getMax(), typeGenerator.isIncludeTotal(), null);

                //Retrieve our total field counts
                Map<String, Long> totalFieldCounts = new HashMap<String, Long>();
                if(averageMonths != -1){
                    totalFieldCounts = getTotalFacetCounts(typeGenerator);
                }
                long monthDifference = 1;
                if(getOldestWorkflowItemDate() != null){
                    monthDifference = getMonthsDifference(new Date(), getOldestWorkflowItemDate());
                }

                dataset = new Dataset(topCounts.length, (averageMonths != -1 ? 3 : 2));
                dataset.setColLabel(0, "step");
                dataset.setColLabel(1, "count");
                if(averageMonths != -1){
                    dataset.setColLabel(2, "average");
                }
                for (int i = 0; i < topCounts.length; i++) {
                    ObjectCount topCount = topCounts[i];
                    dataset.setRowLabel(i, String.valueOf(i + 1));
                    dataset.addValueToMatrix(i, 0, topCount.getValue());
                    dataset.addValueToMatrix(i, 1, topCount.getCount());
                    if(averageMonths != -1){
                        //Calculate the average of one month
                        float monthlyAverage = 0;
                        if(totalFieldCounts.get(topCount.getValue()) != null){
                            monthlyAverage = totalFieldCounts.get(topCount.getValue()) / monthDifference;
                        }
                        //We multiple our average for one month by the number of
                        dataset.addValueToMatrix(i, 2, (monthlyAverage));
                    }

                }
            }
        }

        return dataset;
    }

    /**
     * Returns the query to be used in solr
     * in case of a dso a scopeDso query will be returned otherwise the default *:* query will be used
     * @return the query as a string
     */
    protected String getQuery() {
        String query = "statistics_type:" + SolrLogger.StatisticsType.WORKFLOW.text();
        query += " AND NOT(previousWorkflowStep: SUBMIT)";
        if(currentDso != null){
            if(currentDso.getType() == Constants.COMMUNITY){
                query += " AND owningComm:";

            }else
            if(currentDso.getType() == Constants.COLLECTION){
                query += " AND owningColl:";
            }
            query += currentDso.getID();
        }
        return query;
    }

    private int getMonthsDifference(Date date1, Date date2) {
		Calendar cal = Calendar.getInstance();
		// default will be Gregorian in US Locales
		cal.setTime(date1);
		int minuendMonth =  cal.get(Calendar.MONTH);
		int minuendYear = cal.get(Calendar.YEAR);
		cal.setTime(date2);
		int subtrahendMonth =  cal.get(Calendar.MONTH);
		int subtrahendYear = cal.get(Calendar.YEAR);
		 
		// the following will work okay for Gregorian but will not
		// work correctly in a Calendar where the number of months 
		// in a year is not constant
		return ((minuendYear - subtrahendYear) * cal.getMaximum(Calendar.MONTH)) +  
		(minuendMonth - subtrahendMonth);
    }


    /**
     * Retrieve the total counts for the facets (total count is same query but none of the filter queries
     * @param typeGenerator the type generator
     * @return as a key the
     * @throws org.apache.solr.client.solrj.SolrServerException
     */
    protected Map<String, Long> getTotalFacetCounts(DatasetTypeGenerator typeGenerator) throws SolrServerException {
        ObjectCount[] objectCounts = searcher.queryFacetField(getQuery(), null, typeGenerator.getType(), -1, false, null);
        Map<String, Long> result = new HashMap<String, Long>();
        for (ObjectCount objectCount : objectCounts) {
            result.put(objectCount.getValue(), objectCount.getCount());
        }
        return result;
    }



    protected Date getOldestWorkflowItemDate() throws SolrServerException {
        ConfigurationService configurationService = new DSpace().getConfigurationService();
        String workflowStartDate = configurationService.getProperty("usage-statistics.workflow-start-date");
        if(workflowStartDate == null){
            //Query our solr for it !
            QueryResponse oldestRecord = searcher.query(getQuery(), null, null, 1, 0, null, null, null, null, "time", true);
            if(0 < oldestRecord.getResults().getNumFound()){
                SolrDocument solrDocument = oldestRecord.getResults().get(0);
                Date oldestDate = (Date) solrDocument.getFieldValue("time");
                //Store the date, we only need to retrieve this once !
                try {
                    //Also store it in the solr-statics configuration file, the reason for this being that the sort query
                    //can be very time consuming & we do not want this delay each time we want to see workflow statistics
                    String solrConfigDir = configurationService.getProperty("dspace.dir") + File.separator + "config"
                            + File.separator + "modules" + File.separator + "usage-statistics.cfg";
                    PropertiesConfiguration config = new PropertiesConfiguration(solrConfigDir);
                    config.setProperty("workflow-start-date", new DCDate(oldestDate));
                    config.save();
                } catch (ConfigurationException e) {
                    log.error("Error while storing workflow start date", e);
                }
                //ALso store it in our local config !
                configurationService.setProperty("usage-statistics.workflow-start-date", new DCDate(oldestDate).toString());

                //Write to file
                return oldestDate;
            }else{
                return null;
            }

        }else{
            return new DCDate(workflowStartDate).toDate();
        }
    }
}
