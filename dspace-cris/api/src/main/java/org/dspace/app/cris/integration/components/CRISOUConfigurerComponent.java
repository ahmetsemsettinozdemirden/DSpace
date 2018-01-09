/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * https://github.com/CILEA/dspace-cris/wiki/License
 */
package org.dspace.app.cris.integration.components;

import org.apache.log4j.Logger;
import org.dspace.app.cris.integration.statistics.components.CrisStatDownloadTopObjectComponent;
import org.dspace.app.cris.integration.statistics.components.CrisStatOUDownloadTopObjectComponent;

public class CRISOUConfigurerComponent extends
        ACRISConfigurerComponent
{

    /** log4j logger */
    private static Logger log = Logger
            .getLogger(CRISOUConfigurerComponent.class);

    
    @Override
    protected CrisStatDownloadTopObjectComponent instanceNewCrisStatsDownloadComponent()
    {
        return new CrisStatOUDownloadTopObjectComponent();
    }


}
