/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * https://github.com/CILEA/dspace-cris/wiki/License
 */
package org.dspace.app.webui.cris.servlet;


import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.dspace.app.cris.network.AVisualizationGraph;
import org.dspace.app.cris.network.ConstantNetwork;
import org.dspace.app.cris.network.NetworkPlugin;
import org.dspace.app.cris.network.dto.JsGraph;
import org.dspace.app.cris.network.dto.JsGraphAdjacence;
import org.dspace.app.cris.network.dto.JsGraphNodeData;
import org.dspace.app.cris.service.ApplicationService;

public class RPGraphBuilderUtils
{

    /** log4j category */
    private static Logger log = Logger.getLogger(RPGraphBuilderUtils.class);
    
    public List<String> cacheID = new LinkedList<String>();
    
    public void buildGraph(ApplicationService service, String authority,
            String fullName, NetworkPlugin plugin, List<JsGraph> graph,
            Integer level, Integer maxlevel, boolean showExternal,
            boolean showSameDept, String dept) throws Exception
    {

        if (level <= maxlevel)
        {

            log.debug(" Building researcher node: " + authority);
            JsGraph rsGraph = plugin
                    .search(authority, fullName, level, showExternal,
                            showSameDept, dept, ConstantNetwork.ENTITY_RP);

            graph.add(rsGraph);
            cacheID.add(authority);
            log.debug(" End build researcher node: " + rsGraph.getId());

            for (JsGraphAdjacence adjacence : rsGraph.getAdjacencies())
            {

                log.debug(" Prepare to building researcher node from edge: from "
                        + rsGraph.getId());

                String split[] = adjacence.getSrc().split("\\|\\|\\|");

                String displayValue = "";
                String authorityValue = null;

                if (split.length > 1)
                {
                    String[] splitAuthority = split[1].split(AVisualizationGraph.splitterAuthority);
                    
                    displayValue = splitAuthority[0];
                    
                    if (splitAuthority.length > 1)
                    {
                    
                        authorityValue = splitAuthority[1];
                    
                    }                                                                               
                    
                }       

                if (!cacheID.contains(adjacence.getNodeTo()))
                {
                    buildGraph(service, authorityValue, displayValue, plugin,
                            graph, level + 1, maxlevel, showExternal, showSameDept, dept);
                }
                else
                {
                    log.debug(" Skip researcher node: has just build, node with label "
                            + adjacence.getNodeTo());
                }

            }

        }
        else
        {
            log.debug(" Build leaf node, researcher " + authority);
            JsGraph nodeLeaf = new JsGraph();
            nodeLeaf.setName(fullName);
            JsGraphNodeData dataNodeLeaf = new JsGraphNodeData();
            if (authority == null)
            {
                nodeLeaf.setId(fullName);
                dataNodeLeaf.setModeStyle("stroke");
            }
            else
            {
                nodeLeaf.setId(authority);
                dataNodeLeaf.setModeStyle("fill");
            }
            dataNodeLeaf.setColor(plugin.getNodeCustomColor());            
            dataNodeLeaf.setType(plugin.getType());
            nodeLeaf.setData(dataNodeLeaf);
            log.debug(" End build leaf node, internal researcher " + authority);
            graph.add(nodeLeaf);
        }

    }
}
