/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * https://github.com/CILEA/dspace-cris/wiki/License
 */
package org.dspace.app.cris.util;

import java.beans.PropertyEditor;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.activation.MimetypesFileTypeMap;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.dspace.app.cris.importexport.ExcelBulkChanges;
import org.dspace.app.cris.importexport.IBulkChange;
import org.dspace.app.cris.importexport.IBulkChangeField;
import org.dspace.app.cris.importexport.IBulkChangeFieldFile;
import org.dspace.app.cris.importexport.IBulkChangeFieldFileValue;
import org.dspace.app.cris.importexport.IBulkChangeFieldLink;
import org.dspace.app.cris.importexport.IBulkChangeFieldLinkValue;
import org.dspace.app.cris.importexport.IBulkChangeFieldPointer;
import org.dspace.app.cris.importexport.IBulkChangeFieldPointerValue;
import org.dspace.app.cris.importexport.IBulkChangeFieldValue;
import org.dspace.app.cris.importexport.IBulkChangeNested;
import org.dspace.app.cris.importexport.IBulkChanges;
import org.dspace.app.cris.importexport.IBulkChangesService;
import org.dspace.app.cris.model.ACrisObject;
import org.dspace.app.cris.model.CrisConstants;
import org.dspace.app.cris.model.IExportableDynamicObject;
import org.dspace.app.cris.model.OrganizationUnit;
import org.dspace.app.cris.model.Project;
import org.dspace.app.cris.model.ResearchObject;
import org.dspace.app.cris.model.ResearcherPage;
import org.dspace.app.cris.model.RestrictedField;
import org.dspace.app.cris.model.SourceReference;
import org.dspace.app.cris.model.VisibilityConstants;
import org.dspace.app.cris.model.jdyna.ACrisNestedObject;
import org.dspace.app.cris.model.jdyna.BoxDynamicObject;
import org.dspace.app.cris.model.jdyna.BoxOrganizationUnit;
import org.dspace.app.cris.model.jdyna.BoxProject;
import org.dspace.app.cris.model.jdyna.BoxResearcherPage;
import org.dspace.app.cris.model.jdyna.DecoratorRestrictedField;
import org.dspace.app.cris.model.jdyna.DynamicNestedPropertiesDefinition;
import org.dspace.app.cris.model.jdyna.DynamicObjectType;
import org.dspace.app.cris.model.jdyna.DynamicPropertiesDefinition;
import org.dspace.app.cris.model.jdyna.DynamicTypeNestedObject;
import org.dspace.app.cris.model.jdyna.EditTabDynamicObject;
import org.dspace.app.cris.model.jdyna.EditTabOrganizationUnit;
import org.dspace.app.cris.model.jdyna.EditTabProject;
import org.dspace.app.cris.model.jdyna.EditTabResearcherPage;
import org.dspace.app.cris.model.jdyna.OUNestedPropertiesDefinition;
import org.dspace.app.cris.model.jdyna.OUPropertiesDefinition;
import org.dspace.app.cris.model.jdyna.OUTypeNestedObject;
import org.dspace.app.cris.model.jdyna.ProjectNestedPropertiesDefinition;
import org.dspace.app.cris.model.jdyna.ProjectPropertiesDefinition;
import org.dspace.app.cris.model.jdyna.ProjectTypeNestedObject;
import org.dspace.app.cris.model.jdyna.RPNestedPropertiesDefinition;
import org.dspace.app.cris.model.jdyna.RPPropertiesDefinition;
import org.dspace.app.cris.model.jdyna.RPTypeNestedObject;
import org.dspace.app.cris.model.jdyna.TabDynamicObject;
import org.dspace.app.cris.model.jdyna.TabOrganizationUnit;
import org.dspace.app.cris.model.jdyna.TabProject;
import org.dspace.app.cris.model.jdyna.TabResearcherPage;
import org.dspace.app.cris.model.jdyna.VisibilityTabConstant;
import org.dspace.app.cris.model.jdyna.widget.WidgetClassificationTree;
import org.dspace.app.cris.model.jdyna.widget.WidgetEPerson;
import org.dspace.app.cris.model.jdyna.widget.WidgetGroup;
import org.dspace.app.cris.model.jdyna.widget.WidgetPointerDO;
import org.dspace.app.cris.model.jdyna.widget.WidgetPointerOU;
import org.dspace.app.cris.model.jdyna.widget.WidgetPointerPJ;
import org.dspace.app.cris.model.jdyna.widget.WidgetPointerRP;
import org.dspace.app.cris.service.ApplicationService;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.utils.DSpace;
import org.hibernate.LazyInitializationException;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import it.cilea.osd.jdyna.dto.AnagraficaObjectDTO;
import it.cilea.osd.jdyna.dto.AnagraficaObjectWithTypeDTO;
import it.cilea.osd.jdyna.dto.ValoreDTO;
import it.cilea.osd.jdyna.editor.FilePropertyEditor;
import it.cilea.osd.jdyna.model.ADecoratorNestedPropertiesDefinition;
import it.cilea.osd.jdyna.model.ADecoratorPropertiesDefinition;
import it.cilea.osd.jdyna.model.ANestedPropertiesDefinition;
import it.cilea.osd.jdyna.model.ANestedProperty;
import it.cilea.osd.jdyna.model.ATypeNestedObject;
import it.cilea.osd.jdyna.model.AWidget;
import it.cilea.osd.jdyna.model.AccessLevelConstants;
import it.cilea.osd.jdyna.model.AnagraficaObject;
import it.cilea.osd.jdyna.model.Containable;
import it.cilea.osd.jdyna.model.IContainable;
import it.cilea.osd.jdyna.model.PropertiesDefinition;
import it.cilea.osd.jdyna.model.Property;
import it.cilea.osd.jdyna.util.AnagraficaUtils;
import it.cilea.osd.jdyna.value.EmbeddedFile;
import it.cilea.osd.jdyna.web.Box;
import it.cilea.osd.jdyna.web.Tab;
import it.cilea.osd.jdyna.widget.Size;
import it.cilea.osd.jdyna.widget.WidgetBoolean;
import it.cilea.osd.jdyna.widget.WidgetCheckRadio;
import it.cilea.osd.jdyna.widget.WidgetCustomPointer;
import it.cilea.osd.jdyna.widget.WidgetDate;
import it.cilea.osd.jdyna.widget.WidgetFile;
import it.cilea.osd.jdyna.widget.WidgetLink;
import it.cilea.osd.jdyna.widget.WidgetPointer;
import it.cilea.osd.jdyna.widget.WidgetTesto;

/**
 * Static class that provides export functionalities from the RPs database to
 * Excel. It defines also some constants useful for the import functionalities.
 * 
 * @author cilea
 * 
 */
public class ImportExportUtils {

	private static final DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss");

	/** log4j logger */
	private static Logger log = Logger.getLogger(ImportExportUtils.class);

	private static final String XPATH_ATTRIBUTE_SRC = "@" + UtilsXML.NAMEATTRIBUTE_SRC_LINK;

	private static final String XPATH_ATTRIBUTE_VIS = "@" + UtilsXML.NAMEATTRIBUTE_VISIBILITY;

	private static final String XPATH_ATTRIBUTE_RPID = "@" + UtilsXML.NAMEATTRIBUTE_CRISID;

	private static final String XPATH_ATTRIBUTE_STAFFNO = "@" + UtilsXML.NAMEATTRIBUTE_SOURCEID;

	private static final String XPATH_ELEMENT_ROOT = UtilsXML.ROOT_RESEARCHERS;

	private static final String XPATH_ELEMENT_RESEARCHER = UtilsXML.ELEMENT_RESEARCHER;

	private static final String XPATH_ATTRIBUTE_REMOTESRC = "@" + UtilsXML.NAMEATTRIBUTE_REMOTEURL;

	private static final String XPATH_ATTRIBUTE_MIME = "@" + UtilsXML.NAMEATTRIBUTE_MIMETYPE;

	public static final String[] XPATH_RULES = { "/" + XPATH_ELEMENT_ROOT + "/" + XPATH_ELEMENT_RESEARCHER,
			XPATH_ATTRIBUTE_STAFFNO, XPATH_ATTRIBUTE_VIS, XPATH_ATTRIBUTE_RPID, XPATH_ATTRIBUTE_SRC,
			XPATH_ATTRIBUTE_REMOTESRC, XPATH_ATTRIBUTE_MIME };

	/**
	 * Defaul visibility, it is used when no visibility attribute and old value
	 * founded
	 */
	public static final String DEFAULT_VISIBILITY = "1";

	public static final String LABELCAPTION_VISIBILITY_SUFFIX = " visibility";

	public static final String IMAGE_SUBFOLDER = "image";

	public static final String CV_SUBFOLDER = "cv";

	/**
	 * Default absolute path where find the contact data excel file to import
	 */
	public static final String PATH_DEFAULT_XML = ConfigurationManager.getProperty(CrisConstants.CFG_MODULE, "file.import.path")
			+ "cris-data.csv";

    public static final String PATH_EXPORT_EXCEL_DEFAULT = ConfigurationManager.getProperty(CrisConstants.CFG_MODULE, "file.export.path")
            + "cris-data.xls";

	/**
	 * Write in the output stream the researcher pages contact data as an excel
	 * file. The format of the exported Excel file is suitable for re-import in
	 * the system.
	 * 
	 * @param rps
	 *            the researcher pages list to export
	 * @param applicationService
	 *            the applicationService
	 * @param os
	 *            the output stream, it will close directly when the method exit
	 * @throws IOException
	 * @throws WriteException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	public static <ACO extends ACrisObject<P, TP, NP, NTP, ACNO, ATNO>, P extends Property<TP>, TP extends PropertiesDefinition, NP extends ANestedProperty<NTP>, NTP extends ANestedPropertiesDefinition, ACNO extends ACrisNestedObject<NP, NTP, P, TP>, ATNO extends ATypeNestedObject<NTP>> void exportExcel(List<ACO> rps, ApplicationService applicationService, OutputStream os,
			List<IContainable> metadata, List<IContainable> metadataNestedLevel) throws IOException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {

		HSSFWorkbook workbook = new HSSFWorkbook();

		HSSFSheet sheetEntities = workbook.createSheet("main_entities");
		HSSFSheet sheetNested = workbook.createSheet("nested_entities");
        int xEntities = 0;
        int xNested = 0;
        // create initial caption (other caption could be write field together)
        for(String headerColumn : ExcelBulkChanges.HEADER_COLUMNS) {
            if (xEntities != 0)
            {
            	UtilsXLS.addCell(sheetEntities, xEntities-1, 0, headerColumn);
            }
            xEntities++;
        }
		
        for(String headerColumn : ExcelBulkChanges.HEADER_NESTED_COLUMNS) {
        	UtilsXLS.addCell(sheetNested, xNested, 0, headerColumn);
            xNested++;
        }
        
        
		// row index
		int i = 1;
		int ii = 1;
		for (ACO rp : rps) {
		    if(rp!=null) {	
    	        //HEADER_CRISID,HEADER_UUID,HEADER_SOURCEREF,HEADER_SOURCEID
    		    int y = 0;
    	        UtilsXLS.addCell(sheetEntities, 0, i, rp.getCrisID());
    	        y++;
                UtilsXLS.addCell(sheetEntities, 1, i, rp.getUuid());
                y++;
                UtilsXLS.addCell(sheetEntities, 2, i, rp.getSourceRef());
                y++;
    			UtilsXLS.addCell(sheetEntities, 3, i, rp.getSourceID());
    			
    			for (IContainable containable : metadata) {
    				if (containable instanceof ADecoratorPropertiesDefinition) {
    					y = UtilsXLS.createCell(applicationService, y, i, (ADecoratorPropertiesDefinition) containable,
    							rp, sheetEntities);
    				}
    				if (containable instanceof DecoratorRestrictedField) {
    					y = UtilsXLS
    							.createCell(applicationService, y, i, (DecoratorRestrictedField) containable, rp, sheetEntities);
    				}
    			}
    
                for (IContainable nestedContainable : metadataNestedLevel)
                {
                    List<ACNO> nestedObject = applicationService
                            .getNestedObjectsByParentIDAndShortname(rp.getId(),
                                    nestedContainable.getShortName(),
                                    rp.getClassNested());                
                    for (ACNO rpn : nestedObject)
                    {
    
                      // HEADER_CRISID(parent object), HEADER_SOURCEREF(parent object), HEADER_SOURCEID(parent object), HEADER_UUID,HEADER_SOURCEREF,HEADER_SOURCEID
                        int yy = 0;
                        UtilsXLS.addCell(sheetNested, 0, ii, rp.getCrisID());
                        yy++;
                        UtilsXLS.addCell(sheetNested, 1, ii, rp.getSourceRef());
                        yy++;
                        UtilsXLS.addCell(sheetNested, 2, ii, rp.getSourceID());
                        yy++;
                        UtilsXLS.addCell(sheetNested, 3, ii, rpn.getUuid());
                        yy++;
                        UtilsXLS.addCell(sheetNested, 4, ii, rpn.getSourceReference().getSourceRef());
                        yy++;
                        UtilsXLS.addCell(sheetNested, 5, ii, rpn.getSourceReference().getSourceID());
    
                        try
                        {
                            for (IContainable containable : applicationService.newFindAllContainables(rpn.getClassPropertiesDefinition()))
                            {
                                if (containable instanceof ADecoratorNestedPropertiesDefinition)
                                {
                                    yy = UtilsXLS.createCell(applicationService, yy, ii,
                                            (ADecoratorNestedPropertiesDefinition) containable,
                                            rpn, sheetNested);
                                }
                            }
                        }
                        catch (InstantiationException e)
                        {
                            log.error(e.getMessage(), e);
                        }
                        ii++;
                    }
    			}
    			i++;
		    }
		}
		// All sheets and cells added. Now write out the workbook
		workbook.write(os);
	}

	private static <P extends Property<TP>, TP extends PropertiesDefinition, NP extends ANestedProperty<NTP>, NTP extends ANestedPropertiesDefinition, ACNO extends ACrisNestedObject<NP, NTP, P, TP>, ATNO extends ATypeNestedObject<NTP>, ACO extends ACrisObject<P, TP, NP, NTP, ACNO, ATNO>> ACO getCrisObject(
			ApplicationService applicationService, Class<ACO> objectTypeClass, ACO template, IBulkChange change, boolean status)
			throws InstantiationException, IllegalAccessException {
		String action = change.getAction();
		String crisID = change.getCrisID();
		String uuid = change.getUUID();
		String sourceRef = change.getSourceRef();
		String sourceId = change.getSourceID();
		boolean buildAsNew = false;
		
		ACO crisObject = null;

		// check if entity exist
		if (StringUtils.isNotBlank(crisID)) {
			crisObject = applicationService.getEntityByCrisId(crisID, objectTypeClass);
		} else if (crisObject == null && StringUtils.isNotBlank(uuid)) {
			crisObject = (ACO) applicationService.getEntityByUUID(uuid);
		} else if (crisObject == null && StringUtils.isNotBlank(sourceId) && StringUtils.isNotBlank(sourceRef)) {
			crisObject = applicationService.getEntityBySourceId(sourceRef, sourceId, objectTypeClass);
		}

		// if action=create then we build the object, notify if already exist
		if (StringUtils.equalsIgnoreCase(IBulkChange.ACTION_CREATE, action)) {
			if (crisObject != null) {
				log.warn("the object is already here... found a CREATE action on an existent entity, should we skip it?");
			} else {
				crisObject = internalCrisObjectBuilder(objectTypeClass,
                        template);
			}
		} else if (!StringUtils.equalsIgnoreCase(IBulkChange.ACTION_DELETE, action)) {
			// we create the new entity also in case of the main field are empty
			// (except the delete and create signal)

			if (StringUtils.isBlank(crisID) && StringUtils.isBlank(uuid) && StringUtils.isBlank(sourceRef)
					&& StringUtils.isBlank(sourceId)) {
				crisObject = internalCrisObjectBuilder(objectTypeClass,
                        template);
				buildAsNew = true;
			} else {
				if (crisObject != null) {
					log.info("found a "+ action +" action on entity");
				} else {
					log.warn("the object is not found on database... found a "+ action +" action on an unexistent entity, should we skip it?");
					crisObject = internalCrisObjectBuilder(objectTypeClass,
                            template);
					buildAsNew = true;
				}
			}
		}

		if (StringUtils.isNotBlank(crisID)) {
			crisObject.setCrisID(crisID);
		}

		if (StringUtils.isNotBlank(uuid)) {
			crisObject.setUuid(uuid);
		}

		if (StringUtils.isNotBlank(sourceRef)) {
			crisObject.setSourceRef(sourceRef);
		}
		if (StringUtils.isNotBlank(sourceId)) {
			crisObject.setSourceID(sourceId);
		}
		if (StringUtils.isNotBlank(action)) {
			if (action.equalsIgnoreCase(IBulkChange.ACTION_HIDE)) {
				crisObject.setStatus(false);
			} else if (action.equalsIgnoreCase(IBulkChange.ACTION_SHOW)) {
				crisObject.setStatus(true);
			} else {
			    if(buildAsNew) {
			        crisObject.setStatus(status);
			    }
			}
			
			if (action.equalsIgnoreCase(IBulkChange.ACTION_CREATE) || buildAsNew) {
                applicationService.saveOrUpdate(objectTypeClass, crisObject);
            }
		}
		return crisObject;
	}

    private static <P extends Property<TP>, TP extends PropertiesDefinition, NP extends ANestedProperty<NTP>, NTP extends ANestedPropertiesDefinition, ACNO extends ACrisNestedObject<NP, NTP, P, TP>, ATNO extends ATypeNestedObject<NTP>, ACO extends ACrisObject<P, TP, NP, NTP, ACNO, ATNO>> ACO internalCrisObjectBuilder(
            Class<ACO> objectTypeClass, ACO template)
                    throws InstantiationException, IllegalAccessException
    {
        ACO crisObject = objectTypeClass.newInstance();
        if (crisObject instanceof ResearchObject) {
            ((ResearchObject) crisObject).setTypo(((ResearchObject) template).getTypo());
        }
        return crisObject;
    }

	/**
	 * 
	 * Import xml files, matching validation with xsd builded at runtime
	 * execution associate to list of dynamic fields and structural fields
	 * 
	 * @param input
	 *            - XML file stream
	 * @param dir
	 *            - directory from read image/cv and write temporaries xsd and
	 *            xml (this xsd validate actual xml)
	 * @param applicationService
	 *            - service
	 * @param appendMode
	 *            TODO
	 * @throws Exception
	 */
	public static <ACO extends ACrisObject<P, TP, NP, NTP, ACNO, ATNO>, P extends Property<TP>, TP extends PropertiesDefinition, NP extends ANestedProperty<NTP>, NTP extends ANestedPropertiesDefinition, ACNO extends ACrisNestedObject<NP, NTP, P, TP>, ATNO extends ATypeNestedObject<NTP>> void process(
			String format, InputStream input, File dir, ApplicationService applicationService, Context dspaceContext,
			boolean status, Class<TP> propDefClazz, Class<ACO> crisObjectClazz, ACO template, Class<ACNO> crisNestedObjectClazz,
			Class<ATNO> ttpClass, Class<NTP> propNestedDefClazz) throws Exception {

        List<IContainable> metadataFirstLevel = new ArrayList<IContainable>();
        List<IContainable> metadataNestedLevel = new LinkedList<IContainable>();
        
        if (template.getType() > CrisConstants.CRIS_DYNAMIC_TYPE_ID_START)
        {
            DynamicObjectType type = applicationService.get(DynamicObjectType.class, (template.getType()-CrisConstants.CRIS_DYNAMIC_TYPE_ID_START));
            List<DynamicPropertiesDefinition> tps = applicationService.findMaskById(DynamicObjectType.class, type.getId());            
            for (DynamicPropertiesDefinition tp : tps)
            {
                IContainable ic = applicationService.findContainableByDecorable(
                        tp.getDecoratorClass(), tp.getId());
                if (ic != null)
                {
                    metadataFirstLevel.add(ic);
                }
            }
            
            List<DynamicTypeNestedObject> ttps = applicationService.findNestedMaskById(DynamicObjectType.class, type.getId());
            for (DynamicTypeNestedObject ttp : ttps)
            {
                IContainable ic = applicationService.findContainableByDecorable(
                        ttp.getDecoratorClass(), ttp.getId());
                if (ic != null)
                {
                    metadataNestedLevel.add(ic);
                }
            }
        }
        else
        {
            metadataFirstLevel = applicationService
                    .findAllContainables(propDefClazz);
            List<ATNO> ttps = applicationService
                    .getList(ttpClass);
            
            for (ATNO ttp : ttps)
            {
                IContainable ic = applicationService.findContainableByDecorable(
                        ttp.getDecoratorClass(), ttp.getId());
                if (ic != null)
                {
                    metadataNestedLevel.add(ic);
                }
            }
        }


		DSpace dspace = new DSpace();
		IBulkChangesService importer = dspace.getServiceManager().getServiceByName(format, IBulkChangesService.class);
		IBulkChanges bulkChanges = importer.getBulkChanges(input, dir, crisObjectClazz, propDefClazz,
				metadataFirstLevel, metadataNestedLevel);

		processBulkChanges(applicationService, template, metadataFirstLevel, metadataNestedLevel,
				bulkChanges, status, propDefClazz, propNestedDefClazz, crisObjectClazz, crisNestedObjectClazz, ttpClass, importer.getFormat());
	}

	private static <ACO extends ACrisObject<P, TP, NP, NTP, ACNO, ATNO>, P extends Property<TP>, TP extends PropertiesDefinition, NP extends ANestedProperty<NTP>, NTP extends ANestedPropertiesDefinition, ACNO extends ACrisNestedObject<NP, NTP, P, TP>, ATNO extends ATypeNestedObject<NTP>> void processBulkChanges(
			ApplicationService applicationService, ACO template,
			List<IContainable> metadataFirstLevel, List<IContainable> metadataNestedLevel, IBulkChanges bulkChanges, boolean status,
			Class<TP> propDefClazz, Class<NTP> propNestedDefClazz, Class<ACO> crisObjectClazz, Class<ACNO> crisObjectNestedClazz, Class<ATNO> crisTypeObjectNestedClazz, String format)
			throws InstantiationException, IllegalAccessException, CloneNotSupportedException,
			InvocationTargetException, NoSuchMethodException {

	    // get from list of metadata dynamic field and structural field (note this is only the first level metadata - no nested)
		List<TP> realTPS = new LinkedList<TP>();
		List<IContainable> structuralField = new LinkedList<IContainable>();
		for (IContainable c : metadataFirstLevel) {
			TP rpPd = applicationService.findPropertiesDefinitionByShortName(propDefClazz, c.getShortName());
			if (rpPd != null) {
				realTPS.add((TP) ((ADecoratorPropertiesDefinition<TP>) applicationService.findContainableByDecorable(
						propDefClazz.newInstance().getDecoratorClass(), c.getShortName())).getReal());
			} else {
				structuralField.add(c);
			}
		}

		//build the list of property definition used into import
		List<TP> realFillTPS = new LinkedList<TP>();
		for (TP r : realTPS) {
			if (bulkChanges.hasPropertyDefinition(r.getShortName())) {
				realFillTPS.add(r);
			}
		}

		List<IContainable> structuralFillField = new LinkedList<IContainable>();
		for (IContainable r : structuralField) {
			if (bulkChanges.hasPropertyDefinition(r.getShortName())) {
				structuralFillField.add(r);
			}
		}

		//if forceNestedRemove is false works in append mode
		boolean forceNestedRemove = ConfigurationManager.getBooleanProperty("cris", "script.bulk.import.force.nested.delete", false);		
        if (forceNestedRemove)
        {
            //foreach bulkchange of a nested object try to clean old nested data
            for (int i = 1; i <= bulkChanges.size(); i++)
            {
                try
                {
                    IBulkChange bulkChange = bulkChanges.getChanges(i);
                    if (bulkChange.isANestedBulkChange())
                    {
                        log.info("Try to remove nested " + i + " of " + bulkChanges.size());
                        IBulkChangeNested bulkChangenested = (IBulkChangeNested) bulkChange;
                        String crisID = bulkChangenested.getParentCrisID();
                        String sourceRefParent = bulkChangenested
                                .getParentSourceRef();
                        String sourceIDParent = bulkChangenested
                                .getParentSourceID();

                        ACO object = applicationService
                                .getEntityByCrisId(crisID, crisObjectClazz);
                        if (object == null)
                        {
                            object = applicationService.getEntityBySourceId(
                                    sourceRefParent, sourceIDParent,
                                    crisObjectClazz);
                        }

                        //try to remove all nested object from the parent object if exists
                        if (object!=null)
                        {
                            for (IContainable cont : metadataNestedLevel)
                            {
                                ATNO typo = applicationService
                                        .findTypoByShortName(
                                                crisTypeObjectNestedClazz,
                                                cont.getShortName());
                                List<ACNO> nesteds = applicationService
                                        .getNestedObjectsByParentIDAndTypoID(
                                                object.getId(), typo.getId(),
                                                crisObjectNestedClazz);
                                for (ACNO n : nesteds)
                                {
                                    applicationService.delete(
                                            crisObjectNestedClazz, n.getId());
                                }
                            }
                        }
                    }
                }
                catch (RuntimeException e)
                {
                    log.error("DELETE NESTED ENTITY - FAILED " + e.getMessage(),
                            e);
                }
            }
        }
		
		//foreach bulkchange (a bulk change is an abstraction of the element that contains the entity in the file e.g row for csv format file)
		int rows_discarded = 0;
		int rows_imported = 0;
		log.info("Start import " + new Date());
		// foreach researcher element in xml
		for (int i = 1; i <= bulkChanges.size(); i++) {
			log.info("Number " + i + " of " + bulkChanges.size());
			ACO crisObject = null;
			try {
				IBulkChange bulkChange = bulkChanges.getChanges(i);
                String action = bulkChange.getAction();
                String sourceId = bulkChange.getSourceID();
                String sourceRef = bulkChange.getSourceRef();
                String crisID = bulkChange.getCrisID();
                String uuid = bulkChange.getUUID();
                
                boolean delete = false;
                boolean update = false;
                boolean actionStatus = false;
                if (action.equalsIgnoreCase(IBulkChange.ACTION_DELETE))
                {
                        delete = true;
                }
                else
                {
                    if (action.equalsIgnoreCase(IBulkChange.ACTION_UPDATE))
                    {
                        update = true;
                    }
                    else {
                        if (action.equalsIgnoreCase(IBulkChange.ACTION_SHOW) || action.equalsIgnoreCase(IBulkChange.ACTION_HIDE)) {
                            actionStatus = true;
                        }
                    }
                }
                
                if (bulkChange.isANestedBulkChange())
                {
                    importNested(applicationService, metadataNestedLevel, (IBulkChangeNested)bulkChange, update, crisObjectClazz, crisObjectNestedClazz, crisTypeObjectNestedClazz, format);
                }
                else
                {

                    ACrisObject<P, TP, NP, NTP, ACNO, ATNO> clone = null;
                    // use dto to fill dynamic metadata
                    AnagraficaObjectDTO dto = new AnagraficaObjectDTO();
                    AnagraficaObjectDTO clonedto = new AnagraficaObjectDTO();

                    log.info("Entity sourceRef: " + sourceRef + " sourceID: "
                            + sourceId + " / crisID : " + crisID + " uuid: "
                            + uuid);

                    // if ACTION_CREATE then build object internally to the
                    // getCrisObject (this need to the widgetfile to build the
                    // correct path)
                    crisObject = getCrisObject(applicationService,
                            crisObjectClazz, template, bulkChange, status);

                    if (delete)
                    {
                        for (IContainable metadataNested : metadataNestedLevel)
                        {
                            List<ACNO> toDelete = applicationService
                                    .getNestedObjectsByParentIDAndShortname(
                                            crisObject.getId(),
                                            metadataNested.getShortName(),
                                            crisObjectNestedClazz);
                            for (ACNO deleteNested : toDelete)
                            {
                                applicationService.delete(crisObjectNestedClazz,
                                        deleteNested.getId());
                            }
                        }
                        applicationService.delete(crisObjectClazz,
                                crisObject.getId());
                    }
                    else
                    {
                        clone = (ACrisObject<P, TP, NP, NTP, ACNO, ATNO>) crisObject
                                .clone();
                        dto.setParentId(crisObject.getId());
                        clonedto.setParentId(crisObject.getId());

                        AnagraficaUtils.fillDTO(dto, crisObject, realFillTPS);

                        // one-shot fill and reverse to well-format clonedto and
                        // clean
                        // empty
                        // data
                        AnagraficaUtils.fillDTO(clonedto, clone, realFillTPS);

                        AnagraficaUtils.reverseDTO(clonedto, clone,
                                realFillTPS);

                        AnagraficaUtils.fillDTO(clonedto, clone, realFillTPS);
                        boolean toSaveOrUpdate = importDynA(applicationService, realFillTPS, bulkChange,
                                dto, clonedto, update, format);

                        importStructuralField(applicationService,
                                structuralFillField, crisObject, bulkChange,
                                update);

                        AnagraficaUtils.reverseDTO(dto, crisObject,
                                realFillTPS);

                        if(toSaveOrUpdate || actionStatus) {
                            applicationService.saveOrUpdate(crisObjectClazz,
                                crisObject);
                        }
                    }
                    
                    log.info("Import entity: CRISID: " + crisObject.getCrisID() + " SOURCEID/SOURCEREF: "
                            + crisObject.getSourceID() + "/" + crisObject.getSourceRef() + " ACTION "
                            + action + " -- " + crisObject.getId() + " (id) - SUCCESS");
				}
				rows_imported++;
			} catch (RuntimeException e) {
				log.error("Import entity - FAILED " + e.getMessage(), e);
				rows_discarded++;
			}

		}

		log.info("Import researchers - end import additional files");

		log.info("Statistics: row ingested " + rows_imported + " on total of " + (bulkChanges.size()) + " ("
				+ rows_discarded + " row discarded)");
	}

    private static <ACO extends ACrisObject<P, TP, NP, NTP, ACNO, ATNO>, P extends Property<TP>, TP extends PropertiesDefinition, NP extends ANestedProperty<NTP>, NTP extends ANestedPropertiesDefinition, ACNO extends ACrisNestedObject<NP, NTP, P, TP>, ATNO extends ATypeNestedObject<NTP>> ACNO importNested(
            ApplicationService applicationService,
            List<IContainable> typeNested, IBulkChangeNested bulkChange,
            boolean update, Class<ACO> crisObjectClazz,
            Class<ACNO> crisNestedObjectClazz,
            Class<ATNO> crisTypeNestedObjectClazz, String format)
                    throws CloneNotSupportedException
    {
        String crisID = bulkChange.getParentCrisID();
        String sourceRef = bulkChange.getSourceRef();
        String sourceID = bulkChange.getSourceID();
        String uuid = bulkChange.getUUID();
        String sourceRefParent = bulkChange.getParentSourceRef();
        String sourceIDParent = bulkChange.getParentSourceID();
        // check if nestedobject exist
        ACNO nested = null;

        ACO object = applicationService.getEntityByCrisId(crisID,
                crisObjectClazz);
        if (object == null)
        {
            object = applicationService.getEntityBySourceId(sourceRefParent,
                    sourceIDParent, crisObjectClazz);
        }

        if (object != null)
        {
            boolean remove = false;
            //if forceNestedRemove works in append mode
            boolean forceNestedRemove = ConfigurationManager.getBooleanProperty("cris", "script.bulk.import.force.nested.delete", false);
            if (!forceNestedRemove && (StringUtils.isNotBlank(uuid) || (StringUtils.isNotBlank(sourceRef)
                    && StringUtils.isNotBlank(sourceID))))
            {
                remove = true;
                update = true;
                check_removal: for (IContainable cont : typeNested)
                {

                    ATNO typo = applicationService.findTypoByShortName(
                            crisTypeNestedObjectClazz, cont.getShortName());

                    List<NTP> ntps = null;
                    try {
                        ntps = typo.getMask();
                        remove = checkNestedRemoval(bulkChange, ntps);    
                    }
                    catch (LazyInitializationException e) {
                        ntps = applicationService.findMaskByShortName(typo.getClass(), typo.getShortName());
                        remove = checkNestedRemoval(bulkChange, ntps);
                    }
                    
                    if(!remove) {
                        break;
                    }
                    
                }
                if (remove)
                {
                    if(StringUtils.isNotBlank(uuid)) {
                        nested = applicationService.findNestedObjectByUUID(crisNestedObjectClazz, uuid);
                    }
                    else {
                        for (IContainable cont : typeNested)
                        {

                            ATNO typo = applicationService.findTypoByShortName(
                                    crisTypeNestedObjectClazz,
                                    cont.getShortName());
                            nested = applicationService
                                    .getNestedObjectsByParentIdAndTypoIDAndNestedSourceReference(
                                            object.getId(), typo.getId(),
                                            sourceRef, sourceID,
                                            crisNestedObjectClazz);
                            if (nested != null)
                            {
                                break;
                            }
                        }
                    }
                    if (nested != null)
                    {
                        log.info("Import nested entity: SOURCEID/SOURCEREF: "
                                + sourceID + "/" + sourceRef + " ACTION DELETE" 
                                + " -- " + nested.getId() + " (id) - SUCCESS");
                        applicationService.delete(crisNestedObjectClazz,
                                nested.getId());
                    }
                }
            }

            if (!remove)
            {
                for (IContainable cont : typeNested)
                {
                    String shortNameTypo = cont.getShortName();

                    ATNO typo = applicationService.findTypoByShortName(
                            crisTypeNestedObjectClazz, shortNameTypo);

                    try
                    {
                        if (update)
                        {
                            if (StringUtils.isNotBlank(uuid))
                            {
                                nested = applicationService
                                        .findNestedObjectByUUID(
                                                crisNestedObjectClazz, uuid);
                            }
                            else 
                            {
                                nested = applicationService
                                        .getNestedObjectsByParentIdAndTypoIDAndNestedSourceReference(
                                                object.getId(), typo.getId(),
                                                sourceRef, sourceID,
                                                crisNestedObjectClazz);
                            }
                        }
                        if (nested == null)
                        {
                            nested = crisNestedObjectClazz.newInstance();
                            nested.inizializza();
                        }
                    }
                    catch (InstantiationException | IllegalAccessException e)
                    {
                        log.error(e);
                    }

                    AnagraficaObjectWithTypeDTO anagraficaObjectDTO = new AnagraficaObjectWithTypeDTO();
                    anagraficaObjectDTO.setTipologiaId(typo.getId());
                    anagraficaObjectDTO.setParentId(object.getId());
                    anagraficaObjectDTO.setObjectId(nested.getId());

                    AnagraficaObjectWithTypeDTO cloneanagraficaObjectDTO = new AnagraficaObjectWithTypeDTO();
                    cloneanagraficaObjectDTO.setTipologiaId(typo.getId());
                    cloneanagraficaObjectDTO.setParentId(object.getId());
                    cloneanagraficaObjectDTO.setObjectId(nested.getId());

                    // ACNO clone = (ACNO)nested.clone();

                    List<NTP> mask = new ArrayList<NTP>();
                    try
                    {
                        mask = typo.getMask();
                        AnagraficaUtils.fillDTO(anagraficaObjectDTO, nested,
                                mask);
                    }
                    catch (LazyInitializationException ex)
                    {
                        mask = applicationService.findMaskByShortName(
                                typo.getClass(), typo.getShortName());
                    }

                    AnagraficaUtils.fillDTO(anagraficaObjectDTO, nested, mask);
                    // one-shot fill and reverse to well-format clonedto and
                    // clean
                    // empty
                    // data
                    AnagraficaUtils.fillDTO(cloneanagraficaObjectDTO, null,
                            mask);

                    // AnagraficaUtils.reverseDTO(cloneanagraficaObjectDTO,
                    // clone,
                    // mask);
                    //
                    // AnagraficaUtils.fillDTO(cloneanagraficaObjectDTO, clone,
                    // mask);
                    boolean toAdd = importDynA(applicationService, mask,
                            bulkChange, anagraficaObjectDTO,
                            cloneanagraficaObjectDTO, update, format);
                    if (toAdd)
                    {
                        boolean visible = AnagraficaUtils
                                .checkIsVisible(anagraficaObjectDTO, mask);
                        AnagraficaUtils.reverseDTO(anagraficaObjectDTO, nested,
                                mask);

                        SourceReference sourceReference = new SourceReference();
                        sourceReference.setSourceID(sourceID);
                        sourceReference.setSourceRef(sourceRef);
                        nested.pulisciAnagrafica();
                        nested.setSourceReference(sourceReference);
                        nested.setParent(object);
                        nested.setTypo(typo);
                        nested.setStatus(visible);

                        applicationService.saveOrUpdate(crisNestedObjectClazz,
                                nested);
                    }

                    if (nested != null)
                    {
                        log.info("Import nested entity: SOURCEID/SOURCEREF: "
                                + sourceID + "/" + sourceRef + " ACTION "
                                + " -- " + nested.getId() + " (id) - SUCCESS");
                    }
                }
            }
        }
        return nested;
    }

    private static <NTP extends ANestedPropertiesDefinition> boolean checkNestedRemoval(
            IBulkChangeNested bulkChange, List<NTP> ntps)
    {
        for (NTP ntp : ntps)
        {
            if (ntp.isRepeatable())
            {

                IBulkChangeField nodeslist = bulkChange
                        .getFieldChanges(ntp.getShortName());

                for (int y = 0; y < nodeslist.size(); y++)
                {
                    IBulkChangeFieldValue nodetext = nodeslist
                            .get(y);
                    String control_value = nodetext.getValue();
                    if (StringUtils.isNotBlank(control_value))
                    {
                        return false;
                    }
                }
            }
            else
            {
                IBulkChangeField nodeslist = bulkChange
                        .getFieldChanges(ntp.getShortName());
                IBulkChangeFieldValue nodeText = nodeslist.get(0);
                String control_value = null;
                try
                {
                    control_value = nodeText.getValue();
                }
                catch (NullPointerException exc)
                {
                    // nothing
                }
                if (StringUtils.isNotBlank(control_value))
                {
                    return false;
                }
            }
        }
        return true;
    }

    private static <ACO extends ACrisObject<P, TP, NP, NTP, ACNO, ATNO>, P extends Property<TP>, TP extends PropertiesDefinition, NP extends ANestedProperty<NTP>, NTP extends ANestedPropertiesDefinition, ACNO extends ACrisNestedObject<NP, NTP, P, TP>, ATNO extends ATypeNestedObject<NTP>> void importStructuralField(
            ApplicationService applicationService,
            List<IContainable> structuralFillField, ACO crisObject,
            IBulkChange bulkChange, boolean update)
                    throws IllegalAccessException, InvocationTargetException,
                    NoSuchMethodException
    {
        for (IContainable containable : structuralFillField) {
        	String shortName = containable.getShortName();
        	// xpathExpression = containable.getShortName();
        	if (containable instanceof DecoratorRestrictedField) {
        		Method[] methods = crisObject.getClass().getMethods();
        		Object field = null;
        		Method method = null;
        		Method setter = null;
        		for (Method m : methods) {
        			if (m.getName().toLowerCase().equals("get" + shortName.toLowerCase())) {
        				field = m.invoke(crisObject, null);
        				method = m;
        				String nameSetter = m.getName().replaceFirst("g", "s");
        				setter = crisObject.getClass().getMethod(nameSetter, method.getReturnType());
        				break;
        			}
        		}
        		if (method.getReturnType().isAssignableFrom(List.class)) {

        			// NodeList nodeslist = (NodeList)
        			// xpath.evaluate(
        			// xpathExpression, node,
        			// XPathConstants.NODESET);
        			IBulkChangeField changeField = bulkChange.getFieldChanges(shortName);

        			List<RestrictedField> object = (List<RestrictedField>) field;
        			List<RestrictedField> objectclone = new LinkedList<RestrictedField>();
        			objectclone.addAll(object);

        			if (update == true) {
        				object.clear();
        			}
        			for (int y = 0; y < changeField.size(); y++) {

        				IBulkChangeFieldValue nsublist = changeField.get(y);
        				String value = nsublist.getValue();

        				// String visibilityString = xpath.evaluate(
        				// XPATH_RULES[2], nsublist);
        				String visibilityString = nsublist.getVisibility();

        				if (value != null && !value.isEmpty()) {

        					RestrictedField itemInCollection = new RestrictedField();

        					Integer visibility = null;
        					if (visibilityString != null && !visibilityString.isEmpty()) {
        						visibility = Integer.parseInt(visibilityString);
        					} else if (update == false) {
        						visibility = VisibilityConstants.PUBLIC;

        					} else {
        						visibility = checkOldVisibility(applicationService, value, objectclone,
        								visibility);

        					}

        					// RestrictedField old = checkOldValue(
        					// applicationService, value, object,
        					// visibility);
        					// if (old == null) {
        					itemInCollection.setValue(value);
        					if (visibility != null) {
        						itemInCollection.setVisibility(visibility);
        					}
        					object.add(itemInCollection);
        					setter.invoke(crisObject, object);
        					// }
        				}
        				// else {
        				// if (update == true
        				// && nodeslist.getLength() == 1) {
        				// setter.invoke(researcher,
        				// (List<RestrictedField>) null);
        				// }
        				// }

        			}

        		} else {
        			IBulkChangeField changeField = bulkChange.getFieldChanges(shortName);
        			if (changeField.size() == 1)

        			{
        				IBulkChangeFieldValue changeFieldValue = changeField.get(0);
        				// String value =
        				// xpath.evaluate(xpathExpression,
        				// node);
        				// String visibilityString = xpath.evaluate(
        				// xpathExpression + "/" + XPATH_RULES[2],
        				// node);

        				String value = changeFieldValue.getValue();
        				String visibilityString = changeFieldValue.getVisibility();

        				if (!value.isEmpty()) {

        					if (method.getReturnType().equals(String.class)) {

        						setter.invoke(crisObject, value);
        					} else {

        						Integer visibility = null;
        						if (visibilityString != null && !visibilityString.isEmpty()) {
        							visibility = Integer.parseInt(visibilityString);
        						} else if (!update) {
        							visibility = VisibilityConstants.PUBLIC;

        						} else {
        							visibility = checkOldVisibility(applicationService, value,
        									(RestrictedField) field, visibility);

        						}

        						if (RestrictedField.class.equals(method.getReturnType())) {

        							RestrictedField object = (RestrictedField) field;
        							object.setValue(value);
        							if (visibility != null) {
        								object.setVisibility(visibility);
        							}
        							setter.invoke(crisObject, object);
        						}
        					}

        				} else {
        					if (update) {

        						if (RestrictedField.class.equals(method.getReturnType())) {
        							setter.invoke(crisObject, (RestrictedField) null);
        						}

        					}
        				}
        			}

        		}
        	}

        }
    }

	private static <TP extends PropertiesDefinition> boolean importDynA(ApplicationService applicationService,
			List<TP> realFillTPS, IBulkChange bulkChange, AnagraficaObjectDTO dto, AnagraficaObjectDTO clonedto,
			boolean update, String format) {
	    boolean modifiedTmp = false;
	    boolean modifiedReturn = false;
		// foreach dynamic field read bulkchange and fill on dto
		for (TP rpPD : realFillTPS) {

			// xpathExpression = rpPD.getShortName();
			String shortName = rpPD.getShortName();
			List<ValoreDTO> values = dto.getAnagraficaProperties().get(shortName);
			List<ValoreDTO> oldValues = clonedto.getAnagraficaProperties().get(shortName);
			if (update == true) {
				dto.getAnagraficaProperties().get(shortName).clear();
			}
			if (rpPD.getRendering() instanceof WidgetTesto) {
				if (rpPD.isRepeatable()) {

					// NodeList nodeslist = (NodeList) xpath.evaluate(
					// xpathExpression, node,
					// XPathConstants.NODESET);

					IBulkChangeField nodeslist = bulkChange.getFieldChanges(shortName);

					for (int y = 0; y < nodeslist.size(); y++) {
						IBulkChangeFieldValue nodetext = nodeslist.get(y);
						String control_value = nodetext.getValue();
						if (control_value != null && !control_value.isEmpty()) {
						    modifiedTmp = workOnText(applicationService, rpPD, values, oldValues, nodetext);
						}
						// else {
						// if (update == true
						// && nodeslist.getLength() == 1) {
						// dto.getAnagraficaProperties()
						// .get(shortName).clear();
						// }
						// }
					}
				} else {
					// Node nodeText = (Node) xpath.evaluate(
					// xpathExpression, node, XPathConstants.NODE);
					IBulkChangeField nodeslist = bulkChange.getFieldChanges(shortName);
					IBulkChangeFieldValue nodeText = nodeslist.get(0);
					String control_value = null;
					try {
						control_value = nodeText.getValue();
					} catch (NullPointerException exc) {
						// nothing
					}
					if (control_value != null) {
					    modifiedTmp = workOnText(applicationService, rpPD, values, oldValues, nodeText);
					}
				}
				if(modifiedTmp) {
				    modifiedReturn = modifiedTmp;
				}
			}
			if (rpPD.getRendering() instanceof WidgetDate) {
				if (rpPD.isRepeatable()) {
					// NodeList nodeslist = (NodeList) xpath.evaluate(
					// xpathExpression, node,
					// XPathConstants.NODESET);
					IBulkChangeField nodeslist = bulkChange.getFieldChanges(shortName);

					for (int y = 0; y < nodeslist.size(); y++) {
						if (update == true && y == 0) {
							dto.getAnagraficaProperties().get(shortName).clear();
						}
						IBulkChangeFieldValue nodeDate = nodeslist.get(y);
						String control_value = nodeDate.getValue();
						if (StringUtils.isNotBlank(control_value)) {
						    modifiedTmp = workOnDate(applicationService, nodeDate, rpPD, values, oldValues);
						}
						// else {
						// if (update == true
						// && nodeslist.getLength() == 1) {
						// dto.getAnagraficaProperties()
						// .get(shortName).clear();
						// }
						// }
					}
				} else {
					// Node nodeDate = (Node) xpath.evaluate(
					// xpathExpression, node, XPathConstants.NODE);
					IBulkChangeField nodeslist = bulkChange.getFieldChanges(shortName);
					IBulkChangeFieldValue nodeDate = nodeslist.get(0);

					String control_value = null;
					try {
						control_value = nodeDate.getValue();
					} catch (NullPointerException exc) {
						// nothing
					}

					modifiedTmp = workOnDate(applicationService, nodeDate, rpPD, values, oldValues);
				}
                if(modifiedTmp) {
                    modifiedReturn = modifiedTmp;
                }
			}
			if (rpPD.getRendering() instanceof WidgetLink) {

				if (rpPD.isRepeatable()) {
					// NodeList nodeslist = (NodeList) xpath.evaluate(
					// xpathExpression, node,
					// XPathConstants.NODESET);
					IBulkChangeFieldLink nodeslist = bulkChange.getFieldLinkChanges(shortName);

					for (int y = 0; y < nodeslist.size(); y++) {
						if (update == true && y == 0) {
							dto.getAnagraficaProperties().get(shortName).clear();
						}
						IBulkChangeFieldLinkValue nodeLink = nodeslist.get(y);
						String control_value = nodeLink.getValue();
						if (StringUtils.isNotBlank(control_value)) {
						    modifiedTmp = workOnLink(applicationService, rpPD, values, oldValues, nodeLink, format);
						}
					}
				} else {
					// Node nodeLink = (Node) xpath.evaluate(
					// xpathExpression, node, XPathConstants.NODE);
					IBulkChangeFieldLink nodeslist = bulkChange.getFieldLinkChanges(shortName);
					IBulkChangeFieldLinkValue nodeLink = nodeslist.get(0);
					String control_value = nodeLink.getValue();
					if (StringUtils.isNotBlank(control_value)) {
					    modifiedTmp = workOnLink(applicationService, rpPD, values, oldValues, nodeLink, format);
					}
				}
                if(modifiedTmp) {
                    modifiedReturn = modifiedTmp;
                }
			}

			if (rpPD.getRendering() instanceof WidgetFile) {
                if (rpPD.isRepeatable()) {
                    // NodeList nodeslist = (NodeList) xpath.evaluate(
                    // xpathExpression, node,
                    // XPathConstants.NODESET);
                    IBulkChangeFieldFile nodeslist = bulkChange.getFieldFileChanges(shortName);

                    for (int y = 0; y < nodeslist.size(); y++) {
                        if (update == true && y == 0) {
                            dto.getAnagraficaProperties().get(shortName).clear();
                        }
                        IBulkChangeFieldFileValue nodeFile = nodeslist.get(y);
                        String control_value = nodeFile.getValue();
                        if (StringUtils.isNotBlank(control_value)) {
                            modifiedTmp = workOnFile(applicationService, rpPD, values, oldValues, nodeFile, dto.getParentId());
                        }
                    }
                } else {
                    // Node nodeLink = (Node) xpath.evaluate(
                    // xpathExpression, node, XPathConstants.NODE);
                    IBulkChangeFieldFile nodeslist = bulkChange.getFieldFileChanges(shortName);
                    IBulkChangeFieldFileValue nodeFile = nodeslist.get(0);
                    String control_value = nodeFile.getValue();
                    if (StringUtils.isNotBlank(control_value)) {
                        modifiedTmp = workOnFile(applicationService, rpPD, values, oldValues, nodeFile, dto.getParentId());
                    }
                }
                if(modifiedTmp) {
                    modifiedReturn = modifiedTmp;
                }
			}
			
            if (rpPD.getRendering() instanceof WidgetCheckRadio)
            {
                if (rpPD.isRepeatable())
                {
                    // NodeList nodeslist = (NodeList) xpath.evaluate(
                    // xpathExpression, node,
                    // XPathConstants.NODESET);
                    IBulkChangeField nodeslist = bulkChange
                            .getFieldChanges(shortName);

                    for (int y = 0; y < nodeslist.size(); y++)
                    {
                        if (update == true && y == 0)
                        {
                            dto.getAnagraficaProperties().get(shortName)
                                    .clear();
                        }
                        IBulkChangeFieldValue nodeFile = nodeslist.get(y);
                        String control_value = nodeFile.getValue();
                        if (StringUtils.isNotBlank(control_value))
                        {
                            modifiedTmp = workOnCheckRadio(applicationService, rpPD, values,
                                    oldValues, nodeFile);
                        }
                    }
                }
                else
                {
                    // Node nodeLink = (Node) xpath.evaluate(
                    // xpathExpression, node, XPathConstants.NODE);
                    IBulkChangeField nodeslist = bulkChange
                            .getFieldChanges(shortName);
                    IBulkChangeFieldValue nodeFile = nodeslist.get(0);
                    String control_value = nodeFile.getValue();
                    if (StringUtils.isNotBlank(control_value))
                    {
                        modifiedTmp = workOnCheckRadio(applicationService, rpPD, values, oldValues,
                                nodeFile);
                    }
                }
                if(modifiedTmp) {
                    modifiedReturn = modifiedTmp;
                }
            }
            
            if (rpPD.getRendering() instanceof WidgetPointer || rpPD.getRendering() instanceof WidgetClassificationTree)
            {
                if (rpPD.isRepeatable())
                {
                    IBulkChangeFieldPointer nodeslist = bulkChange
                            .getFieldPointerChanges(shortName);

                    for (int y = 0; y < nodeslist.size(); y++)
                    {
                        if (update == true && y == 0)
                        {
                            dto.getAnagraficaProperties().get(shortName)
                                    .clear();
                        }
                        IBulkChangeFieldPointerValue nodeFile = nodeslist.get(y);
                        String control_value = nodeFile.getValue();
                        if (StringUtils.isNotBlank(control_value))
                        {
                            modifiedTmp = workOnPointer(applicationService, rpPD, values,
                                    oldValues, nodeFile);
                        }
                    }
                }
                else
                {
                    IBulkChangeFieldPointer nodeslist = bulkChange
                            .getFieldPointerChanges(shortName);
                    IBulkChangeFieldPointerValue nodeFile = nodeslist.get(0);
                    String control_value = nodeFile.getValue();
                    if (StringUtils.isNotBlank(control_value))
                    {
                        modifiedTmp = workOnPointer(applicationService, rpPD, values, oldValues,
                                nodeFile);
                    }
                }
                if(modifiedTmp) {
                    modifiedReturn = modifiedTmp;
                }
            }
            
            if (rpPD.getRendering() instanceof WidgetBoolean)
            {
                if (rpPD.isRepeatable())
                {
                    // NodeList nodeslist = (NodeList) xpath.evaluate(
                    // xpathExpression, node,
                    // XPathConstants.NODESET);
                    IBulkChangeField nodeslist = bulkChange
                            .getFieldChanges(shortName);

                    for (int y = 0; y < nodeslist.size(); y++)
                    {
                        if (update == true && y == 0)
                        {
                            dto.getAnagraficaProperties().get(shortName)
                                    .clear();
                        }
                        IBulkChangeFieldValue nodeFile = nodeslist.get(y);
                        String control_value = nodeFile.getValue();
                        if (StringUtils.isNotBlank(control_value))
                        {
                            modifiedTmp = workOnBoolean(applicationService, rpPD, values,
                                    oldValues, nodeFile);
                        }
                    }
                }
                else
                {
                    // Node nodeLink = (Node) xpath.evaluate(
                    // xpathExpression, node, XPathConstants.NODE);
                    IBulkChangeField nodeslist = bulkChange
                            .getFieldChanges(shortName);
                    IBulkChangeFieldValue nodeFile = nodeslist.get(0);
                    String control_value = nodeFile.getValue();
                    if (StringUtils.isNotBlank(control_value))
                    {
                        modifiedTmp = workOnBoolean(applicationService, rpPD, values, oldValues,
                                nodeFile);
                    }
                }
                if(modifiedTmp) {
                    modifiedReturn = modifiedTmp;
                }
            }

            if (rpPD.getRendering() instanceof WidgetCustomPointer)
            {
                if (rpPD.isRepeatable())
                {
                    // NodeList nodeslist = (NodeList) xpath.evaluate(
                    // xpathExpression, node,
                    // XPathConstants.NODESET);
                    IBulkChangeField nodeslist = bulkChange
                            .getFieldChanges(shortName);

                    for (int y = 0; y < nodeslist.size(); y++)
                    {
                        if (update == true && y == 0)
                        {
                            dto.getAnagraficaProperties().get(shortName)
                                    .clear();
                        }
                        IBulkChangeFieldValue nodeFile = nodeslist.get(y);
                        String control_value = nodeFile.getValue();
                        if (StringUtils.isNotBlank(control_value))
                        {
                            modifiedTmp = workOnCustomPointer(applicationService, rpPD, values,
                                    oldValues, nodeFile);
                        }
                    }
                }
                else
                {
                    // Node nodeLink = (Node) xpath.evaluate(
                    // xpathExpression, node, XPathConstants.NODE);
                    IBulkChangeField nodeslist = bulkChange
                            .getFieldChanges(shortName);
                    IBulkChangeFieldValue nodeFile = nodeslist.get(0);
                    String control_value = nodeFile.getValue();
                    if (StringUtils.isNotBlank(control_value))
                    {
                        modifiedTmp = workOnCustomPointer(applicationService, rpPD, values, oldValues,
                                nodeFile);
                    }
                }
                if(modifiedTmp) {
                    modifiedReturn = modifiedTmp;
                }
            }
        }
		return modifiedReturn;
	}

    private static <TP extends PropertiesDefinition> boolean workOnFile(ApplicationService applicationService, TP rpPD,
            List<ValoreDTO> values, List<ValoreDTO> old, IBulkChangeFieldFileValue nodeLink, Integer dtoParentId) {
        if (nodeLink != null) {

            // String nodetext = nodeLink.getTextContent();
            String nodetext = nodeLink.getValue();
            boolean isLocal = nodeLink.isLocal();
            boolean isDelete = nodeLink.isDelete();

            if (isLocal && isDelete)
            {
                removeWidgetFile(rpPD, dtoParentId, nodetext);

            }
            else
            {
                if (!isDelete)
                {
                    if (nodetext != null && !nodetext.isEmpty())
                    {

                        String vis = nodeLink.getVisibility();
                        File src = new File(nodetext);

                        FilePropertyEditor pe = (FilePropertyEditor<WidgetFile>) rpPD
                                .getRendering()
                                .getPropertyEditor(applicationService);
                        pe.setExternalAuthority("" + dtoParentId);
                        pe.setInternalAuthority("" + rpPD.getId());
                        pe.setValue(src);
                        ValoreDTO valueDTO = new ValoreDTO(pe.getValue());
                        remapVisibility(applicationService, rpPD, old, nodetext,
                                vis, valueDTO);

                        // ValoreDTO oldValue =
                        // checkOldValue(applicationService,
                        // rpPD,
                        // old, nodetext, valueDTO.getVisibility());
                        // if(oldValue==null) {
                        values.add(valueDTO);
                        log.debug("Write link field " + rpPD.getShortName()
                                + " with value" + nodetext + " visibility: "
                                + valueDTO.getVisibility());
                        return true;
                        // }
                    }
                }
            }

        }
        return false;
    }

    private static <TP extends PropertiesDefinition> void removeWidgetFile(
            TP rpPD, Integer dtoParentId, String nodetext)
    {
        WidgetFile widgetFile = (WidgetFile) (rpPD.getRendering());
        File filedisk = new File(nodetext);
        
            String mime = new MimetypesFileTypeMap()
                    .getContentType(filedisk);
            EmbeddedFile file = new EmbeddedFile();
            String originalFilename = filedisk.getName();
            String filename = originalFilename.substring(0,
                    originalFilename.lastIndexOf("."));
             String   ext = originalFilename.substring(
                        originalFilename.lastIndexOf(".") + 1);
            file.setExtFile(ext);
            file.setValueFile(filename);
            file.setMimeFile(mime);
            
            String intAuthority = ""+rpPD.getId();
            String extAuthority = ""+dtoParentId;
            String folder = intAuthority;
            if(extAuthority!=null && extAuthority.length()>0) {
                folder = widgetFile.getCustomFolderByAuthority(extAuthority, intAuthority);
            }
            file.setFolderFile(folder);
            widgetFile.remove(file);
    }
    
    
    private static <TP extends PropertiesDefinition> boolean workOnPointer(ApplicationService applicationService, TP rpPD,
            List<ValoreDTO> values, List<ValoreDTO> old, IBulkChangeFieldPointerValue nodeLink) {
        if (nodeLink != null) {

            // String nodetext = nodeLink.getTextContent();
            String nodetext = nodeLink.getValue();

            if (nodetext != null && !nodetext.isEmpty()) {
                
                String crisID = nodeLink.getCrisID();
                String sourceRef = nodeLink.getSourceRef();
                String sourceID = nodeLink.getSourceID();
                String uuid = nodeLink.getUuid();
                
                ACrisObject crisObj = null;
                
                if(StringUtils.isNotBlank(uuid)) {
                    crisObj = applicationService.getEntityByUUID(uuid);
                }
                
                if(StringUtils.isNotBlank(crisID)) {
                    if(crisID.startsWith("rp")) {
                        crisObj = applicationService.getEntityByCrisId(crisID, ResearcherPage.class);        
                    }
                    else if(crisID.startsWith("pj")) {
                        crisObj = applicationService.getEntityByCrisId(crisID, Project.class);
                    }
                    else if(crisID.startsWith("ou")) {
                        crisObj = applicationService.getEntityByCrisId(crisID, OrganizationUnit.class);
                    } else {
                        crisObj = applicationService.getEntityByCrisId(crisID, ResearchObject.class);
                    }
                }
                
                if(StringUtils.isNotBlank(sourceID) && StringUtils.isNotBlank(sourceRef)) {
                    if (rpPD.getRendering() instanceof WidgetClassificationTree)
                    {
                        crisObj = applicationService.getEntityBySourceId(
                                sourceRef, sourceID, ResearchObject.class);
                    }
                    else {
                        WidgetPointer widget = (WidgetPointer) rpPD
                                .getRendering();
                        Class target = widget.getTargetValoreClass();
                        if (target.equals(ResearcherPage.class))
                        {
                            crisObj = applicationService.getEntityBySourceId(
                                    sourceRef, sourceID, ResearcherPage.class);
                        }
                        else if (target.equals(Project.class))
                        {
                            crisObj = applicationService.getEntityBySourceId(
                                    sourceRef, sourceID, Project.class);
                        }
                        else if (target.equals(OrganizationUnit.class))
                        {
                            crisObj = applicationService.getEntityBySourceId(
                                    sourceRef, sourceID,
                                    OrganizationUnit.class);
                        }
                        else if (target.equals(ResearchObject.class))
                        {
                            crisObj = applicationService.getEntityBySourceId(
                                    sourceRef, sourceID, ResearchObject.class);
                        }
                    }
                    
                }
                
                if(crisObj == null) {
                    //maybe need to build the new object???
                    return false;
                }
                
                String vis = nodeLink.getVisibility();

                PropertyEditor pe = rpPD.getRendering().getPropertyEditor(applicationService);
                pe.setValue(crisObj);

                ValoreDTO valueDTO = new ValoreDTO(pe.getValue());
                
                remapVisibility(applicationService, rpPD, old, nodetext, vis,
                        valueDTO);

                // ValoreDTO oldValue = checkOldValue(applicationService, rpPD,
                // old, nodetext, valueDTO.getVisibility());
                // if(oldValue==null) {
                values.add(valueDTO);
                log.debug("Write link field " + rpPD.getShortName() + " with value" + nodetext + " visibility: "
                        + valueDTO.getVisibility());
                return true;
            }
        }
        return false;
    }
    
    public static File generateSimpleTypeWithListOfAllMetadata(Writer writer, List<IContainable> metadata,
			File filexsd, String namespace, String fullNamespace, String name) throws IOException, SecurityException,
			NoSuchFieldException {
		UtilsXSD xsd = new UtilsXSD(writer);
		xsd.createSimpleTypeFor(metadata, namespace, fullNamespace, name);
		return filexsd;
	}

	private static <TP extends PropertiesDefinition> boolean workOnText(ApplicationService applicationService,
			TP rpPD, List<ValoreDTO> values, List<ValoreDTO> old, IBulkChangeFieldValue node) {
		if (node != null) {

			// String nodetext = node.getTextContent();
			// String vis = xpath.evaluate(XPATH_RULES[2], node);
			String nodetext = node.getValue();
			String vis = node.getVisibility();

			if (nodetext != null && !nodetext.isEmpty()) {
				ValoreDTO valueDTO = new ValoreDTO(nodetext);
				remapVisibility(applicationService, rpPD, old, nodetext, vis,
                        valueDTO);

				// ValoreDTO oldValue = checkOldValue(applicationService, rpPD,
				// old, nodetext, valueDTO.getVisibility());
				// if(oldValue==null) {
				values.add(valueDTO);
				log.debug("Write text field " + rpPD.getShortName() + " with value: " + nodetext + " visibility: "
						+ valueDTO.getVisibility());
				return true;
			}
		}
		return false;
	}

    private static <TP extends PropertiesDefinition> void remapVisibility(
            ApplicationService applicationService, TP rpPD, List<ValoreDTO> old,
            String nodetext, String vis, ValoreDTO valueDTO)
    {
        if (vis == null || vis.isEmpty()) {
        	// check old value
        	vis = checkOldVisibility(applicationService, rpPD, old, nodetext, vis);
        }

        if (StringUtils.isNotBlank(vis)) {
            int intVis = VisibilityConstants.getIDfromDescription(vis);
        	valueDTO.setVisibility(intVis==1?true:false);
        }
    }

    private static <TP extends PropertiesDefinition> boolean workOnBoolean(
            ApplicationService applicationService, TP rpPD,
            List<ValoreDTO> values, List<ValoreDTO> old,
            IBulkChangeFieldValue node)
    {
        if (node != null)
        {

            String nodetext = node.getValue();
            String vis = node.getVisibility();

            if (nodetext != null && !nodetext.isEmpty())
            {
                PropertyEditor pe = rpPD.getRendering().getPropertyEditor(applicationService);
                pe.setAsText(nodetext);
                ValoreDTO valueDTO = new ValoreDTO(pe.getValue());
                remapVisibility(applicationService, rpPD, old, nodetext, vis, valueDTO);
                values.add(valueDTO);
                log.debug("Write boolean field " + rpPD.getShortName()
                        + " with value: " + nodetext + " visibility: "
                        + valueDTO.getVisibility());
                return true;
            }
        }
        return false;
    }

    private static <TP extends PropertiesDefinition> boolean workOnCheckRadio(
            ApplicationService applicationService, TP rpPD, List<ValoreDTO> values, List<ValoreDTO> old, IBulkChangeFieldValue node)
    {
        if (node != null)
        {

            // String nodetext = node.getTextContent();
            // String vis = xpath.evaluate(XPATH_RULES[2], node);
            String nodetext = node.getValue();
            String vis = node.getVisibility();

            if (nodetext != null && !nodetext.isEmpty())
            {
                ValoreDTO valueDTO = new ValoreDTO(nodetext);
                remapVisibility(applicationService, rpPD, old, nodetext, vis, valueDTO);
                
//                TODO validation? or a PropertyEditor custom to retrieve the correct value?
//                WidgetCheckRadio widgetCheckRadio = (WidgetCheckRadio)(rpPD.getRendering());
//                widgetCheckRadio.getStaticValues();
                
                // ValoreDTO oldValue = checkOldValue(applicationService, rpPD,
                // old, nodetext, valueDTO.getVisibility());
                // if(oldValue==null) {
                values.add(valueDTO);
                log.debug("Write checkradio field " + rpPD.getShortName()
                        + " with value: " + nodetext + " visibility: "
                        + valueDTO.getVisibility());
                return true;
            }
        }
        return false;
    }

	private static <TP extends PropertiesDefinition> boolean workOnLink(ApplicationService applicationService, TP rpPD,
			List<ValoreDTO> values, List<ValoreDTO> old, IBulkChangeFieldLinkValue nodeLink, String format) {
		if (nodeLink != null) {

			// String nodetext = nodeLink.getTextContent();
			String nodetext = nodeLink.getValue();

			if (nodetext != null && !nodetext.isEmpty()) {
				// String vis = xpath.evaluate(XPATH_RULES[2], node);
				String vis = nodeLink.getVisibility();
				// if (vis != null && vis.isEmpty()) {
				// vis = xpath.evaluate(XPATH_RULES[2], nodeLink);
				// }
				// String src = xpath.evaluate(XPATH_RULES[4], node);
				String src = nodeLink.getLinkURL();
				// if (src != null && src.isEmpty()) {
				// src = xpath.evaluate(XPATH_RULES[4], nodeLink);
				// }

				nodetext += "|||" + src;

				PropertyEditor pe = rpPD.getRendering().getImportPropertyEditor(applicationService, format);
				pe.setAsText(nodetext);
				ValoreDTO valueDTO = new ValoreDTO(pe.getValue());
				remapVisibility(applicationService, rpPD, old, nodetext, vis, valueDTO);

				// ValoreDTO oldValue = checkOldValue(applicationService, rpPD,
				// old, nodetext, valueDTO.getVisibility());
				// if(oldValue==null) {
				values.add(valueDTO);
				log.debug("Write link field " + rpPD.getShortName() + " with value" + nodetext + " visibility: "
						+ valueDTO.getVisibility());
				return true;
				// }
			}			
		}
		return false;
	}

	private static <TP extends PropertiesDefinition> boolean workOnDate(ApplicationService applicationService,
			IBulkChangeFieldValue node, TP rpPD, List<ValoreDTO> values, List<ValoreDTO> old) {
		if (node != null) {
			// String nodetext = nodeDate.getTextContent();
			String nodetext = node.getValue();

			if (nodetext != null && !nodetext.isEmpty()) {
				// String vis = xpath.evaluate(XPATH_RULES[2], node);
				String vis = node.getVisibility();
				// if (vis != null) {
				// if (vis.isEmpty()) {
				// vis = xpath.evaluate(XPATH_RULES[2], nodeDate);
				// }
				// }

				PropertyEditor pe = rpPD.getRendering().getPropertyEditor(applicationService);
				pe.setAsText(nodetext);
				ValoreDTO valueDTO = new ValoreDTO(pe.getValue());
				remapVisibility(applicationService, rpPD, old, nodetext, vis, valueDTO);
				
				// ValoreDTO oldValue = checkOldValue(applicationService, rpPD,
				// old, nodetext, valueDTO.getVisibility());
				// if(oldValue==null) {
				values.add(valueDTO);
				log.debug("Write date field " + rpPD.getShortName() + " with value: " + nodetext + " visibility: "
						+ valueDTO.getVisibility());
				return true;
			}
		}
		return false;
	}

    private static <TP extends PropertiesDefinition> boolean workOnCustomPointer(
            ApplicationService applicationService, TP rpPD,
            List<ValoreDTO> values, List<ValoreDTO> old,
            IBulkChangeFieldValue node)
    {
        if (node != null)
        {

            String nodetext = node.getValue();
            String vis = node.getVisibility();

            if (nodetext != null && !nodetext.isEmpty())
            {
                PropertyEditor pe = rpPD.getRendering().getPropertyEditor(applicationService);
                pe.setAsText(nodetext);
                ValoreDTO valueDTO = new ValoreDTO(pe.getValue());
                remapVisibility(applicationService, rpPD, old, nodetext, vis, valueDTO);
                values.add(valueDTO);
                log.debug("Write custom pointer field " + rpPD.getShortName()
                        + " with value: " + nodetext + " visibility: "
                        + valueDTO.getVisibility());
                return true;
            }
        }
        return false;
    }
    
	/**
	 * 
	 * Check old visibility on dynamic field
	 * 
	 * @param applicationService
	 * @param rpPD
	 * @param old
	 * @param nodetext
	 * @param vis
	 * @return
	 */
	private static <TP extends PropertiesDefinition> String checkOldVisibility(ApplicationService applicationService,
			TP rpPD, List<ValoreDTO> old, String nodetext, String vis) {
		PropertyEditor pe = rpPD.getRendering().getPropertyEditor(applicationService);

		boolean founded = false;
		for (ValoreDTO temp : old) {
			pe.setValue(temp.getObject());
			if (pe.getAsText().equals(nodetext)) {
				vis = temp.getVisibility() ? "1" : "0";
				founded = true;
				break;
			}
		}
		return founded == true ? vis : DEFAULT_VISIBILITY;
	}

	private static Integer checkOldVisibility(ApplicationService applicationService, String value,
			List<RestrictedField> object, Integer vis) {
		boolean founded = false;
		for (RestrictedField f : object) {
			if (f.getValue().equals(value)) {
				vis = f.getVisibility();
				founded = true;
				break;
			}
		}
		return founded == true ? vis : VisibilityConstants.PUBLIC;
	}

	private static Integer checkOldVisibility(ApplicationService applicationService, String value,
			RestrictedField field, Integer visibility) {

		return field.getValue().equals(value) ? field.getVisibility() : VisibilityConstants.PUBLIC;
	}

	/**
	 * Export xml, it don't close or flush writer, format with
	 * {@link XMLOutputter}, use use jdom for it.
	 * 
	 * @param writer
	 * @param applicationService
	 * @param metadata
	 * @param researchers
	 * @throws IOException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 */
	@Deprecated
	public static void exportXML(Writer writer, ApplicationService applicationService, List<IContainable> metadata, List<IContainable> metadataNestedLevel, 
			List<ResearcherPage> researchers) throws IOException, SecurityException, IllegalArgumentException,
			NoSuchFieldException, IllegalAccessException, InvocationTargetException, ParserConfigurationException,
			TransformerException {
	    //TODO manage nested
		UtilsXML xml = new UtilsXML(writer, applicationService);
		org.jdom.Document xmldoc = xml.createRoot(UtilsXSD.RP_DEFAULT_ELEMENT[0], UtilsXSD.NAMESPACE_PREFIX_RP,
				UtilsXSD.NAMESPACE_RP);
		if (researchers != null) {
			for (ResearcherPage rp : researchers) {
				xml.writeRP(rp, metadata, xmldoc.getRootElement());
			}
			// Serialisation through XMLOutputter
			XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
			out.output(xmldoc, writer);
		}
	}

	public static <TP extends PropertiesDefinition, P extends Property<TP>, AO extends AnagraficaObject<P, TP>, I extends IExportableDynamicObject<TP, P, AO>> void newExportXML(
			Writer writer, ApplicationService applicationService, List<IContainable> metadata, List<I> objects,
			String prefixNamespace, String namespace, String rootName) throws IOException, SecurityException,
			IllegalArgumentException, NoSuchFieldException, IllegalAccessException, InvocationTargetException,
			ParserConfigurationException, TransformerException {

		UtilsXML xml = new UtilsXML(writer, applicationService);
		org.jdom.Document xmldoc = xml.createRoot(rootName, prefixNamespace, namespace);
		if (objects != null) {
			for (I rp : objects) {
				xml.write(rp, metadata, xmldoc.getRootElement());
			}
			// Serialisation through XMLOutputter
			XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
			out.output(xmldoc, writer);
		}
	}

	public static void exportConfiguration(ApplicationService applicationService, OutputStream os) throws IOException,
			IllegalArgumentException, IllegalAccessException, InvocationTargetException {

		HSSFWorkbook workbook = new HSSFWorkbook();

		HSSFSheet propertiesdefinitionSheet = workbook.createSheet("propertiesdefinition");
		HSSFSheet nesteddefinitionSheet = workbook.createSheet("nesteddefinition");
		HSSFSheet tabSheet = workbook.createSheet("tab");
		HSSFSheet etabSheet = workbook.createSheet("etab");
        HSSFSheet boxSheet = workbook.createSheet("box");
        HSSFSheet tab2boxSheet = workbook.createSheet("tab2box");
        HSSFSheet etab2boxSheet = workbook.createSheet("etab2box");
        HSSFSheet box2metadataSheet = workbook.createSheet("box2metadata");
        HSSFSheet utilsdataSheet = workbook.createSheet("utilsdata");
        HSSFSheet controlledlistSheet = workbook.createSheet("controlledlist");
        HSSFSheet tabpolicySheet = workbook.createSheet("tabpolicy");
        HSSFSheet etabpolicySheet = workbook.createSheet("etabpolicy");
        HSSFSheet boxpolicySheet = workbook.createSheet("boxpolicy");
        
        Map<String, Class<? extends PropertiesDefinition>> propDefTypes = new HashMap<String, Class<? extends PropertiesDefinition>>();
        propDefTypes.put("rp", RPPropertiesDefinition.class);
        propDefTypes.put("pj", ProjectPropertiesDefinition.class);
        propDefTypes.put("ou", OUPropertiesDefinition.class);
        
        Map<String, Class<? extends ATypeNestedObject>> nestedDefTypes = new HashMap<String, Class<? extends ATypeNestedObject>>();
        nestedDefTypes.put("rp", RPTypeNestedObject.class);
        nestedDefTypes.put("pj", ProjectTypeNestedObject.class);
        nestedDefTypes.put("ou", OUTypeNestedObject.class);
        
        Map<String, Class<? extends PropertiesDefinition>> nestedpropDefTypes = new HashMap<String, Class<? extends PropertiesDefinition>>();
        nestedpropDefTypes.put("rp", RPNestedPropertiesDefinition.class);
        nestedpropDefTypes.put("pj", ProjectNestedPropertiesDefinition.class);
        nestedpropDefTypes.put("ou", OUNestedPropertiesDefinition.class);
        
        Map<Integer, String> accessLevels = new HashMap<Integer, String>();
        accessLevels.put(VisibilityTabConstant.ADMIN, "ADMIN_ACCESS");
        accessLevels.put(VisibilityTabConstant.STANDARD, "STANDARD_ACCESS");
        accessLevels.put(VisibilityTabConstant.HIGH, "HIGH_ACCESS");
        accessLevels.put(VisibilityTabConstant.LOW, "LOW_ACCESS");
        accessLevels.put(VisibilityTabConstant.POLICY, "POLICY_ACCESS");

        Map<String, Class<? extends Tab>> tabTypes = new HashMap<String, Class<? extends Tab>>();
        tabTypes.put("rp", TabResearcherPage.class);
        tabTypes.put("pj", TabProject.class);
        tabTypes.put("ou", TabOrganizationUnit.class);

        Map<String, Class<? extends Tab>> etabTypes = new HashMap<String, Class<? extends Tab>>();
        etabTypes.put("rp", EditTabResearcherPage.class);
        etabTypes.put("pj", EditTabProject.class);
        etabTypes.put("ou", EditTabOrganizationUnit.class);

        Map<String, Class<? extends Box>> boxTypes = new HashMap<String, Class<? extends Box>>();
        boxTypes.put("rp", BoxResearcherPage.class);
        boxTypes.put("pj", BoxProject.class);
        boxTypes.put("ou", BoxOrganizationUnit.class);
        
        int colIdx = 0;
        for (String pDefHeader : new String[]{
        		"TARGET", "SHORTNAME", "LABEL", "REPEATABLE", "PRIORITY", "HELP",
				"ACCESS LEVEL", "MANDATORY", "WIDGET", "POINTER CLASS", "RENDERING", "LABEL-SIZE", "FIELD-WIDTH",   
				"FIELD-HEIGHT", "NEW-LINE","TEXTROWS","TEXTCOLS", "NONE" }) {
        	UtilsXLS.addCell(propertiesdefinitionSheet, colIdx, 0, pDefHeader);
        	colIdx++;
        }
        
        colIdx = 0;
		for (String pDefHeader : new String[] { "TARGET", "SHORTNAME", "LABEL", "REPEATABLE", "PRIORITY", "HELP",
				"ACCESS LEVEL", "MANDATORY", "WIDGET", "POINTER CLASS", "ANCESTOR", "RENDERING", "LABEL-SIZE",
				"FIELD-WIDTH", "FIELD-HEIGHT", "NEW-LINE","TEXTROWS","TEXTCOLS", "NONE" }) {
			UtilsXLS.addCell(nesteddefinitionSheet, colIdx, 0, pDefHeader);
        	colIdx++;
        }
		
		colIdx = 0;
		for (String pDefHeader : new String[] { "TARGET", "SHORTNAME", "LABEL", "MANDATORY", "PRIORITY", "VISIBILITY",
				"EXT", "MIME", "NONE" }) {
			UtilsXLS.addCell(tabSheet, colIdx, 0, pDefHeader);
			UtilsXLS.addCell(etabSheet, colIdx, 0, pDefHeader);
        	colIdx++;
        }
		
		colIdx = 0;
		for (String pDefHeader : new String[] { "TARGET", "COLLAPSED", "EXTERNALJSP", "PRIORITY", "SHORTNAME", "LABEL",
				"UNRELEVANT", "VISIBILITY", "NONE"
		}) {
			UtilsXLS.addCell(boxSheet, colIdx, 0, pDefHeader);
        	colIdx++;
        }
		
		colIdx = 0;
		for (String pDefHeader : new String[] { "TARGET", "TAB", "BOX"}) {
			UtilsXLS.addCell(tab2boxSheet, colIdx, 0, pDefHeader);
			UtilsXLS.addCell(etab2boxSheet, colIdx, 0, pDefHeader);
			colIdx++;
		}

		colIdx = 0;
		for (String pDefHeader : new String[] { "TARGET", "BOX", "METADATA"}) {
			UtilsXLS.addCell(box2metadataSheet, colIdx, 0, pDefHeader);
			colIdx++;
		}

        colIdx = 0;
        for (String pDefHeader : new String[] { "TARGET", "SHORTNAME", "METADATA", "TYPE" })
        {
        	UtilsXLS.addCell(tabpolicySheet, colIdx, 0, pDefHeader);
            UtilsXLS.addCell(etabpolicySheet, colIdx, 0, pDefHeader);
            UtilsXLS.addCell(boxpolicySheet, colIdx, 0, pDefHeader); 
            colIdx++;
        }
	        
        UtilsXLS.addCell(utilsdataSheet, 0, 0, "nested");
        UtilsXLS.addCell(utilsdataSheet, 0, 1, "text");
        UtilsXLS.addCell(utilsdataSheet, 0, 2, "date");
        UtilsXLS.addCell(utilsdataSheet, 0, 3, "link");
        UtilsXLS.addCell(utilsdataSheet, 0, 4, "file");
        UtilsXLS.addCell(utilsdataSheet, 0, 5, "pointer");
        UtilsXLS.addCell(utilsdataSheet, 0, 6, "image");
        UtilsXLS.addCell(utilsdataSheet, 0, 7, "boolean");
        UtilsXLS.addCell(utilsdataSheet, 0, 8, "radio");
        UtilsXLS.addCell(utilsdataSheet, 0, 9, "checkbox");
		
        UtilsXLS.addCell(utilsdataSheet, 1, 0, "y");
        UtilsXLS.addCell(utilsdataSheet, 1, 1, "n");
		
        UtilsXLS.addCell(utilsdataSheet, 2, 0, "###");
        UtilsXLS.addCell(utilsdataSheet, 2, 1, "rp");
        UtilsXLS.addCell(utilsdataSheet, 2, 2, "pj");
        UtilsXLS.addCell(utilsdataSheet, 2, 3, "ou");
		
        UtilsXLS.addCell(utilsdataSheet, 3, 0, "HIGH_ACCESS");
        UtilsXLS.addCell(utilsdataSheet, 3, 1, "STANDARD_ACCESS");
        UtilsXLS.addCell(utilsdataSheet, 3, 2, "ADMIN_ACCESS");
        UtilsXLS.addCell(utilsdataSheet, 3, 3, "LOW_ACCESS");
        UtilsXLS.addCell(utilsdataSheet, 3, 4, "POLICY_ACCESS");
		
        int rowIdx = 1;
        int rowNestedIdx = 1;
        int rowTabIdx = 1;
        int rowETabIdx = 1;
        int rowTab2boxIdx = 1;
        int rowETab2boxIdx = 1;
        int rowBoxIdx = 1;
        int rowBox2metadataIdx = 1;
        int rowUtilsDataDynObjectsIdx = 4;
        int colControlledList = 0;
        
        for (String oType : propDefTypes.keySet()) {
        	List<? extends PropertiesDefinition> propDefs = applicationService.getList(propDefTypes.get(oType));
            for (PropertiesDefinition propDef : propDefs) {
	        		UtilsXLS.addCell(propertiesdefinitionSheet, 0, rowIdx, oType);
	        		UtilsXLS.addCell(propertiesdefinitionSheet, 1, rowIdx, propDef.getShortName());
	        		UtilsXLS.addCell(propertiesdefinitionSheet, 2, rowIdx, propDef.getLabel());
	        		UtilsXLS.addCell(propertiesdefinitionSheet, 3, rowIdx, propDef.isRepeatable()?"y":"n");
	        		UtilsXLS.addCell(propertiesdefinitionSheet, 4, rowIdx, propDef.getPriority()+"");
	        		UtilsXLS.addCell(propertiesdefinitionSheet, 5, rowIdx, propDef.getHelp());
	        		UtilsXLS.addCell(propertiesdefinitionSheet, 6, rowIdx, accessLevels.get(propDef.getAccessLevel()));
	        		UtilsXLS.addCell(propertiesdefinitionSheet, 7, rowIdx, propDef.isMandatory()?"y":"n");
	        		UtilsXLS.addCell(propertiesdefinitionSheet, 8, rowIdx, getWidgetDescription(propDef.getRendering(), propDef.isRepeatable()));
	        		UtilsXLS.addCell(propertiesdefinitionSheet, 9, rowIdx, getPointerClassDescription(applicationService, propDef.getRendering()));
	        		UtilsXLS.addCell(propertiesdefinitionSheet, 10, rowIdx, getWidgetTextRenderingDescription(propDef.getRendering()));
	            	if (propDef.getLabelMinSize() != null && propDef.getLabelMinSize() > 0) {
	            		UtilsXLS.addCell(propertiesdefinitionSheet, 11, rowIdx, propDef.getLabelMinSize()+"");
	            	}
	            	if (propDef.getFieldMinSize() != null && propDef.getFieldMinSize().getCol() > 0) {
	            		UtilsXLS.addCell(propertiesdefinitionSheet, 12, rowIdx, propDef.getFieldMinSize().getCol()+"");
	            	}
	            	if (propDef.getFieldMinSize() != null && propDef.getFieldMinSize().getRow() > 0) {
	            		UtilsXLS.addCell(propertiesdefinitionSheet, 13, rowIdx, propDef.getFieldMinSize().getRow()+"");
	            	}
	            	UtilsXLS.addCell(propertiesdefinitionSheet, 14, rowIdx, propDef.isNewline()?"y":"n");
	            	
	            	UtilsXLS.addCell(propertiesdefinitionSheet, 15,rowIdx,getWidgetTextSizeRow(propDef.getRendering()));
	            	
	            	UtilsXLS.addCell(propertiesdefinitionSheet, 16,rowIdx,getWidgetTextSizeCol(propDef.getRendering()));
	            	
	            	UtilsXLS.addCell(propertiesdefinitionSheet, 17, rowIdx, "#");
	            	
	            	if (propDef.getRendering() instanceof WidgetCheckRadio) {
	            		WidgetCheckRadio widget = (WidgetCheckRadio) propDef.getRendering();
	            		String[] cLists = widget.getStaticValues().split("\\|\\|\\|");
						// FIXME this make impossible to have the same property
						// name in different objects associated with a
						// controlled list
	            		UtilsXLS.addCell(controlledlistSheet, colControlledList, 0, propDef.getShortName());
	            		int tmpIdx = 1;
	            		for (String c : cLists) {
	            			UtilsXLS.addCell(controlledlistSheet, colControlledList, tmpIdx, c);
	            			tmpIdx++;
	            		}
	            		colControlledList++;
	            	}
	        	rowIdx++;
            }
            
            List<? extends ATypeNestedObject> nestedDefs = applicationService.getList(nestedDefTypes.get(oType));
            for (ATypeNestedObject propDef : nestedDefs) {
	        		UtilsXLS.addCell(propertiesdefinitionSheet, 0, rowIdx, oType);
	        		UtilsXLS.addCell(propertiesdefinitionSheet, 1, rowIdx, propDef.getShortName());
	        		UtilsXLS.addCell(propertiesdefinitionSheet, 2, rowIdx, propDef.getLabel());
	        		UtilsXLS.addCell(propertiesdefinitionSheet, 3, rowIdx, propDef.isRepeatable()?"y":"n");
	        		UtilsXLS.addCell(propertiesdefinitionSheet, 4, rowIdx, propDef.getPriority()+"");
	        		UtilsXLS.addCell(propertiesdefinitionSheet, 5, rowIdx, propDef.getHelp());
	        		UtilsXLS.addCell(propertiesdefinitionSheet, 6, rowIdx, accessLevels.get(propDef.getAccessLevel()));
	        		UtilsXLS.addCell(propertiesdefinitionSheet, 7, rowIdx, propDef.isMandatory()?"y":"n");
	        		UtilsXLS.addCell(propertiesdefinitionSheet, 8, rowIdx, "nested");
	            	UtilsXLS.addCell(propertiesdefinitionSheet, 9, rowIdx, "n");
	            	UtilsXLS.addCell(propertiesdefinitionSheet, 10, rowIdx, "");
//	            	not defined for nested
//	            	if (propDef.getLabelMinSize() != null && propDef.getLabelMinSize() > 0) {
//	            		propertiesdefinitionSheet.addCell(new Label(11, rowIdx, propDef.getLabelMinSize()+""));
//	            	}
//	            	if (propDef.getFieldMinSize() != null && propDef.getFieldMinSize().getCol() > 0) {
//	            		propertiesdefinitionSheet.addCell(new Label(12, rowIdx, propDef.getFieldMinSize().getCol()+""));
//	            	}
//	            	if (propDef.getFieldMinSize() != null && propDef.getFieldMinSize().getRow() > 0) {
//	            		propertiesdefinitionSheet.addCell(new Label(13, rowIdx, propDef.getFieldMinSize().getRow()+""));
//	            	}
	            	UtilsXLS.addCell(propertiesdefinitionSheet, 14, rowIdx, propDef.isNewline()?"y":"n");
	            	UtilsXLS.addCell(propertiesdefinitionSheet, 15,rowIdx,getWidgetTextSizeRow(propDef.getRendering()));
	            	UtilsXLS.addCell(propertiesdefinitionSheet, 16,rowIdx,getWidgetTextSizeCol(propDef.getRendering()));	            	
	            	UtilsXLS.addCell(propertiesdefinitionSheet, 17, rowIdx, "#");
	            	rowIdx++;
	            	
					List<? extends PropertiesDefinition> nestedpropDefs = applicationService
							.findMaskByShortName(nestedDefTypes.get(oType),
									propDef.getShortName());
	                for (PropertiesDefinition npropDef : nestedpropDefs) {
	    	        		// shortname of the nested group "the propdef" of the parent
	    	        		UtilsXLS.addCell(nesteddefinitionSheet, 0, rowNestedIdx, oType);
	    	        		UtilsXLS.addCell(nesteddefinitionSheet, 1, rowNestedIdx, npropDef.getShortName());
	    	        		UtilsXLS.addCell(nesteddefinitionSheet, 2, rowNestedIdx, npropDef.getLabel());
	    	        		UtilsXLS.addCell(nesteddefinitionSheet, 3, rowNestedIdx, npropDef.isRepeatable()?"y":"n");
	    	        		UtilsXLS.addCell(nesteddefinitionSheet, 4, rowNestedIdx, npropDef.getPriority()+"");
	    	            	UtilsXLS.addCell(nesteddefinitionSheet, 5, rowNestedIdx, npropDef.getHelp());
	    	            	UtilsXLS.addCell(nesteddefinitionSheet, 6, rowNestedIdx, accessLevels.get(npropDef.getAccessLevel()));
	    	            	UtilsXLS.addCell(nesteddefinitionSheet, 7, rowNestedIdx, npropDef.isMandatory()?"y":"n");
	    	            	UtilsXLS.addCell(nesteddefinitionSheet, 8, rowNestedIdx, getWidgetDescription(npropDef.getRendering(), npropDef.isRepeatable()));
	    	            	UtilsXLS.addCell(nesteddefinitionSheet, 9, rowNestedIdx, getPointerClassDescription(applicationService, npropDef.getRendering()));
	    	            	UtilsXLS.addCell(nesteddefinitionSheet, 10, rowNestedIdx, propDef.getShortName());
	    	            	UtilsXLS.addCell(nesteddefinitionSheet, 11, rowNestedIdx, getWidgetTextRenderingDescription(npropDef.getRendering()));
	    	            	if (npropDef.getLabelMinSize() != null && npropDef.getLabelMinSize() > 0) {
	    	            		UtilsXLS.addCell(nesteddefinitionSheet, 12, rowNestedIdx, npropDef.getLabelMinSize()+"");
	    	            	}
	    	            	if (npropDef.getFieldMinSize() != null && npropDef.getFieldMinSize().getCol() > 0) {
	    	            		UtilsXLS.addCell(nesteddefinitionSheet, 13, rowNestedIdx, npropDef.getFieldMinSize().getCol()+"");
	    	            	}
	    	            	if (npropDef.getFieldMinSize() != null && npropDef.getFieldMinSize().getRow() > 0) {
	    	            		UtilsXLS.addCell(nesteddefinitionSheet, 14, rowNestedIdx, npropDef.getFieldMinSize().getRow()+"");
	    	            	}
	    	            	UtilsXLS.addCell(nesteddefinitionSheet, 15, rowNestedIdx, npropDef.isNewline()?"y":"n");
	    	            	
	    	            	UtilsXLS.addCell(nesteddefinitionSheet, 16,rowNestedIdx,getWidgetTextSizeRow(npropDef.getRendering()));
	    	            	
	    	            	UtilsXLS.addCell(nesteddefinitionSheet, 17,rowNestedIdx,getWidgetTextSizeCol(npropDef.getRendering()));
	    	            	
	    	            	UtilsXLS.addCell(nesteddefinitionSheet, 18, rowNestedIdx, "#");
	    	            	
	    	            	if (npropDef.getRendering() instanceof WidgetCheckRadio) {
	    	            		WidgetCheckRadio widget = (WidgetCheckRadio) npropDef.getRendering();
	    	            		String[] cLists = widget.getStaticValues().split("\\|\\|\\|");
	    						// FIXME this make impossible to have the same property
	    						// name in different objects associated with a
	    						// controlled list
	    	            		UtilsXLS.addCell(controlledlistSheet, colControlledList, 0, npropDef.getShortName());
	    	            		int tmpIdx = 1;
	    	            		for (String c : cLists) {
	    	            			UtilsXLS.addCell(controlledlistSheet, colControlledList, tmpIdx, c);
	    	            			tmpIdx++;
	    	            		}
	    	            		colControlledList++;
	    	            	}
	    	        	rowNestedIdx++;
	                }
            }
            
            List<? extends Tab> tabs = applicationService
					.getList(tabTypes.get(oType));
            for (Tab tab : tabs) {
            	UtilsXLS.addCell(tabSheet, 0, rowTabIdx, oType);
            	UtilsXLS.addCell(tabSheet, 1, rowTabIdx, tab.getShortName());
            	UtilsXLS.addCell(tabSheet, 2, rowTabIdx, tab.getTitle());
            	UtilsXLS.addCell(tabSheet, 3, rowTabIdx, tab.isMandatory()?"y":"n");
            	UtilsXLS.addCell(tabSheet, 4, rowTabIdx, tab.getPriority()+"");
            	UtilsXLS.addCell(tabSheet, 5, rowTabIdx, accessLevels.get(tab.getVisibility()));
				if (tab.getExt() != null) {
					UtilsXLS.addCell(tabSheet, 6, rowTabIdx, tab.getExt());
				}
				if (tab.getMime() != null) {
					UtilsXLS.addCell(tabSheet, 7, rowTabIdx, tab.getMime());
				}
				UtilsXLS.addCell(tabSheet, 8, rowTabIdx, "#");				
				rowTabIdx++;
				
				for (Object boxObj : applicationService.findPropertyHolderInTab(tabTypes.get(oType), tab.getId())) {
					Box box = (Box) boxObj;
					UtilsXLS.addCell(tab2boxSheet, 0, rowTab2boxIdx, oType);
					UtilsXLS.addCell(tab2boxSheet, 1, rowTab2boxIdx, tab.getShortName());
					UtilsXLS.addCell(tab2boxSheet, 2, rowTab2boxIdx, box.getShortName());
					rowTab2boxIdx++;
				}
				
				int idxPolicy = 1;
				List<PropertiesDefinition> policies = applicationService.findPDAuthorizationGroupInTab(tabTypes.get(oType), tab.getId());
				for(PropertiesDefinition policy : policies) {
					UtilsXLS.addCell(tabpolicySheet, 0, idxPolicy, oType);
					UtilsXLS.addCell(tabpolicySheet, 1, idxPolicy, tab.getShortName());
					UtilsXLS.addCell(tabpolicySheet, 2, idxPolicy, policy.getShortName());
					UtilsXLS.addCell(tabpolicySheet, 3, idxPolicy, "group");
				    idxPolicy++;
				}
				policies = applicationService.findPDAuthorizationSingleInTab(tabTypes.get(oType), tab.getId());
                for(PropertiesDefinition policy : policies) {
                	UtilsXLS.addCell(tabpolicySheet, 0, idxPolicy, oType);
                	UtilsXLS.addCell(tabpolicySheet, 1, idxPolicy, tab.getShortName());
                	UtilsXLS.addCell(tabpolicySheet, 2, idxPolicy, policy.getShortName());
                	UtilsXLS.addCell(tabpolicySheet, 3, idxPolicy, "eperson");
                    idxPolicy++;
                }
            }
            
            List<? extends Tab> etabs = applicationService
					.getList(etabTypes.get(oType));
            for (Tab tab : etabs) {
            	UtilsXLS.addCell(etabSheet, 0, rowETabIdx, oType);
            	UtilsXLS.addCell(etabSheet, 1, rowETabIdx, tab.getShortName());
            	UtilsXLS.addCell(etabSheet, 2, rowETabIdx, tab.getTitle());
            	UtilsXLS.addCell(etabSheet, 3, rowETabIdx, tab.isMandatory()?"y":"n");
            	UtilsXLS.addCell(etabSheet, 4, rowETabIdx, tab.getPriority()+"");
            	UtilsXLS.addCell(etabSheet, 5, rowETabIdx, accessLevels.get(tab.getVisibility()));
				if (tab.getExt() != null) {
					UtilsXLS.addCell(etabSheet, 6, rowETabIdx, tab.getExt());
				}
				if (tab.getMime() != null) {
					UtilsXLS.addCell(etabSheet, 7, rowETabIdx, tab.getMime());
				}
				UtilsXLS.addCell(etabSheet, 8, rowETabIdx, "#");				
				rowETabIdx++;
				
				for (Object boxObj : applicationService.findPropertyHolderInTab(etabTypes.get(oType), tab.getId())) {
					Box box = (Box) boxObj;
					UtilsXLS.addCell(etab2boxSheet, 0, rowETab2boxIdx, oType);
					UtilsXLS.addCell(etab2boxSheet, 1, rowETab2boxIdx, tab.getShortName());
					UtilsXLS.addCell(etab2boxSheet, 2, rowETab2boxIdx, box.getShortName());
					rowETab2boxIdx++;
				}
				
                int idxPolicy = 1;
                
                List<PropertiesDefinition> policies = applicationService.findPDAuthorizationGroupInTab(tabTypes.get(oType), tab.getId());
                for(PropertiesDefinition policy : policies) {
                	UtilsXLS.addCell(etabpolicySheet, 0, idxPolicy, oType);
                	UtilsXLS.addCell(etabpolicySheet, 1, idxPolicy, tab.getShortName());
                	UtilsXLS.addCell(etabpolicySheet, 2, idxPolicy, policy.getShortName());
                	UtilsXLS.addCell(etabpolicySheet, 3, idxPolicy, "group");
                    idxPolicy++;
                }
                
                policies = applicationService.findPDAuthorizationSingleInTab(tabTypes.get(oType), tab.getId());
                for(PropertiesDefinition policy : policies) {
                	UtilsXLS.addCell(etabpolicySheet, 0, idxPolicy, oType);
                	UtilsXLS.addCell(etabpolicySheet, 1, idxPolicy, tab.getShortName());
                	UtilsXLS.addCell(etabpolicySheet, 2, idxPolicy, policy.getShortName());
                	UtilsXLS.addCell(etabpolicySheet, 3, idxPolicy, "eperson");
                    idxPolicy++;
                }
            }
            
            List<? extends Box> boxes = applicationService
					.getList(boxTypes.get(oType));
            for (Box box : boxes) {
            	UtilsXLS.addCell(boxSheet, 0, rowBoxIdx, oType);
            	UtilsXLS.addCell(boxSheet, 1, rowBoxIdx, box.isCollapsed()?"y":"n");
            	UtilsXLS.addCell(boxSheet, 2, rowBoxIdx, box.getExternalJSP());
            	UtilsXLS.addCell(boxSheet, 3, rowBoxIdx, box.getPriority()+"");
            	UtilsXLS.addCell(boxSheet, 4, rowBoxIdx, box.getShortName());
            	UtilsXLS.addCell(boxSheet, 5, rowBoxIdx, box.getTitle());
            	UtilsXLS.addCell(boxSheet, 6, rowBoxIdx, box.isUnrelevant()?"y":"n");
            	UtilsXLS.addCell(boxSheet, 7, rowBoxIdx, accessLevels.get(box.getVisibility()));
            	UtilsXLS.addCell(boxSheet, 8, rowBoxIdx, "#");            	
				rowBoxIdx++;
				
				for (Object pdefObj : applicationService.findContainableInPropertyHolder(boxTypes.get(oType),
						box.getId())) {
					Containable pdef = (Containable) pdefObj;
					UtilsXLS.addCell(box2metadataSheet, 0, rowBox2metadataIdx, oType);
					UtilsXLS.addCell(box2metadataSheet, 1, rowBox2metadataIdx, box.getShortName());
					UtilsXLS.addCell(box2metadataSheet, 2, rowBox2metadataIdx, pdef.getShortName());
					rowBox2metadataIdx++;
				}
				
                int idxPolicy = 1;
                List<PropertiesDefinition> policies = applicationService.findPDAuthorizationGroupInBox(boxTypes.get(oType),
                        box.getId());
                for(PropertiesDefinition policy : policies) {
                	UtilsXLS.addCell(boxpolicySheet, 0, idxPolicy, oType);
                	UtilsXLS.addCell(boxpolicySheet, 1, idxPolicy, box.getShortName());
                	UtilsXLS.addCell(boxpolicySheet, 2, idxPolicy, policy.getShortName());
                	UtilsXLS.addCell(boxpolicySheet, 3, idxPolicy, "group");
                    idxPolicy++;
                }
                policies = applicationService.findPDAuthorizationSingleInBox(boxTypes.get(oType),
                        box.getId());
                for(PropertiesDefinition policy : policies) {
                	UtilsXLS.addCell(boxpolicySheet, 0, idxPolicy, oType);
                	UtilsXLS.addCell(boxpolicySheet, 1, idxPolicy, box.getShortName());
                	UtilsXLS.addCell(boxpolicySheet, 2, idxPolicy, policy.getShortName());
                	UtilsXLS.addCell(boxpolicySheet, 3, idxPolicy, "eperson");
                    idxPolicy++;
                }
            }
        }
        

        //FIXME ARGH!!! copy and paste from the above code
        
        List<DynamicObjectType> dynTypes = applicationService.getList(DynamicObjectType.class);
        for (DynamicObjectType dyn : dynTypes) {
        	String oType = dyn.getShortName();

        	UtilsXLS.addCell(utilsdataSheet, 2, rowUtilsDataDynObjectsIdx, oType);
        	rowUtilsDataDynObjectsIdx++;
        	
			List<? extends PropertiesDefinition> propDefs = applicationService.findMaskByShortName(DynamicObjectType.class,
                    oType);
            for (PropertiesDefinition propDef : propDefs) {
	        		UtilsXLS.addCell(propertiesdefinitionSheet, 0, rowIdx, oType);
	        		UtilsXLS.addCell(propertiesdefinitionSheet, 1, rowIdx, propDef.getShortName().substring(oType.length()));
	        		UtilsXLS.addCell(propertiesdefinitionSheet, 2, rowIdx, propDef.getLabel());
	        		UtilsXLS.addCell(propertiesdefinitionSheet, 3, rowIdx, propDef.isRepeatable()?"y":"n");
	        		UtilsXLS.addCell(propertiesdefinitionSheet, 4, rowIdx, propDef.getPriority()+"");
	        		UtilsXLS.addCell(propertiesdefinitionSheet, 5, rowIdx, propDef.getHelp());
	        		UtilsXLS.addCell(propertiesdefinitionSheet, 6, rowIdx, accessLevels.get(propDef.getAccessLevel()));
	        		UtilsXLS.addCell(propertiesdefinitionSheet, 7, rowIdx, propDef.isMandatory()?"y":"n");
	            	UtilsXLS.addCell(propertiesdefinitionSheet, 8, rowIdx, getWidgetDescription(propDef.getRendering(), propDef.isRepeatable()));
	            	UtilsXLS.addCell(propertiesdefinitionSheet, 9, rowIdx, getPointerClassDescription(applicationService, propDef.getRendering()));
	            	UtilsXLS.addCell(propertiesdefinitionSheet, 10, rowIdx, getWidgetTextRenderingDescription(propDef.getRendering()));
	            	if (propDef.getLabelMinSize() != null && propDef.getLabelMinSize() > 0) {
	            		UtilsXLS.addCell(propertiesdefinitionSheet, 11, rowIdx, propDef.getLabelMinSize()+"");
	            	}
	            	if (propDef.getFieldMinSize() != null && propDef.getFieldMinSize().getCol() > 0) {
	            		UtilsXLS.addCell(propertiesdefinitionSheet, 12, rowIdx, propDef.getFieldMinSize().getCol()+"");
	            	}
	            	if (propDef.getFieldMinSize() != null && propDef.getFieldMinSize().getRow() > 0) {
	            		UtilsXLS.addCell(propertiesdefinitionSheet, 13, rowIdx, propDef.getFieldMinSize().getRow()+"");
	            	}
	            	UtilsXLS.addCell(propertiesdefinitionSheet, 14, rowIdx, propDef.isNewline()?"y":"n");
	            	UtilsXLS.addCell(propertiesdefinitionSheet, 15,rowIdx,getWidgetTextSizeRow(propDef.getRendering()));
	            	UtilsXLS.addCell(propertiesdefinitionSheet, 16,rowIdx,getWidgetTextSizeCol(propDef.getRendering()));
	            	UtilsXLS.addCell(propertiesdefinitionSheet, 17, rowIdx, "#");
	            	
	            	if (propDef.getRendering() instanceof WidgetCheckRadio) {
	            		WidgetCheckRadio widget = (WidgetCheckRadio) propDef.getRendering();
	            		String[] cLists = widget.getStaticValues().split("\\|\\|\\|");
						// FIXME this make impossible to have the same property
						// name in different objects associated with a
						// controlled list
	            		UtilsXLS.addCell(controlledlistSheet, colControlledList, 0, propDef.getShortName());
	            		int tmpIdx = 1;
	            		for (String c : cLists) {
	            			UtilsXLS.addCell(controlledlistSheet, colControlledList, tmpIdx, c);
	            			tmpIdx++;
	            		}
	            		colControlledList++;
	            	}
	        	rowIdx++;
            }
            
            List<? extends ATypeNestedObject> nestedDefs = applicationService.getList(DynamicTypeNestedObject.class);
            for (ATypeNestedObject propDef : nestedDefs) {
	        		if (!StringUtils.startsWith(propDef.getShortName(), oType)) continue;
	        		UtilsXLS.addCell(propertiesdefinitionSheet, 0, rowIdx, oType);
	        		UtilsXLS.addCell(propertiesdefinitionSheet, 1, rowIdx, propDef.getShortName().substring(oType.length()));
	        		UtilsXLS.addCell(propertiesdefinitionSheet, 2, rowIdx, propDef.getLabel());
	        		UtilsXLS.addCell(propertiesdefinitionSheet, 3, rowIdx, propDef.isRepeatable()?"y":"n");
	        		UtilsXLS.addCell(propertiesdefinitionSheet, 4, rowIdx, propDef.getPriority()+"");
	        		UtilsXLS.addCell(propertiesdefinitionSheet, 5, rowIdx, propDef.getHelp());
	        		UtilsXLS.addCell(propertiesdefinitionSheet, 6, rowIdx, accessLevels.get(propDef.getAccessLevel()));
	        		UtilsXLS.addCell(propertiesdefinitionSheet, 7, rowIdx, propDef.isMandatory()?"y":"n");
	        		UtilsXLS.addCell(propertiesdefinitionSheet, 8, rowIdx, "nested");
	        		UtilsXLS.addCell(propertiesdefinitionSheet, 9, rowIdx, "n");
	        		UtilsXLS.addCell(propertiesdefinitionSheet, 10, rowIdx, "");
//	            	not defined for nested
//	            	if (propDef.getLabelMinSize() != null && propDef.getLabelMinSize() > 0) {
//	            		propertiesdefinitionSheet.addCell(new Label(11, rowIdx, propDef.getLabelMinSize()+""));
//	            	}
//	            	if (propDef.getFieldMinSize() != null && propDef.getFieldMinSize().getCol() > 0) {
//	            		propertiesdefinitionSheet.addCell(new Label(12, rowIdx, propDef.getFieldMinSize().getCol()+""));
//	            	}
//	            	if (propDef.getFieldMinSize() != null && propDef.getFieldMinSize().getRow() > 0) {
//	            		propertiesdefinitionSheet.addCell(new Label(13, rowIdx, propDef.getFieldMinSize().getRow()+""));
//	            	}
	        		UtilsXLS.addCell(propertiesdefinitionSheet, 14, rowIdx, propDef.isNewline()?"y":"n");
	        		UtilsXLS.addCell(propertiesdefinitionSheet, 15,rowIdx,getWidgetTextSizeRow(propDef.getRendering()));
	        		UtilsXLS.addCell(propertiesdefinitionSheet, 16,rowIdx,getWidgetTextSizeCol(propDef.getRendering()));
	        		UtilsXLS.addCell(propertiesdefinitionSheet, 17, rowIdx, "#");
	            	rowIdx++;
	            	
					List<? extends PropertiesDefinition> nestedpropDefs = applicationService
							.likePropertiesDefinitionsByShortName(DynamicNestedPropertiesDefinition.class,
									propDef.getShortName());
	                for (PropertiesDefinition npropDef : nestedpropDefs) {
	    	        		// shortname of the nested group "the propdef" of the parent
	    	        		UtilsXLS.addCell(nesteddefinitionSheet, 0, rowNestedIdx, oType);
	    	        		UtilsXLS.addCell(nesteddefinitionSheet, 1, rowNestedIdx, npropDef.getShortName());
	    	        		UtilsXLS.addCell(nesteddefinitionSheet, 2, rowNestedIdx, npropDef.getLabel());
	    	        		UtilsXLS.addCell(nesteddefinitionSheet, 3, rowNestedIdx, npropDef.isRepeatable()?"y":"n");
	    	        		UtilsXLS.addCell(nesteddefinitionSheet, 4, rowNestedIdx, npropDef.getPriority()+"");
	    	        		UtilsXLS.addCell(nesteddefinitionSheet, 5, rowNestedIdx, npropDef.getHelp());
	    	        		UtilsXLS.addCell(nesteddefinitionSheet, 6, rowNestedIdx, accessLevels.get(npropDef.getAccessLevel()));
	    	        		UtilsXLS.addCell(nesteddefinitionSheet, 7, rowNestedIdx, npropDef.isMandatory()?"y":"n");
	    	        		UtilsXLS.addCell(nesteddefinitionSheet, 8, rowNestedIdx, getWidgetDescription(npropDef.getRendering(), npropDef.isRepeatable()));
	    	        		UtilsXLS.addCell(nesteddefinitionSheet, 9, rowNestedIdx, getPointerClassDescription(applicationService, npropDef.getRendering()));
							UtilsXLS.addCell(nesteddefinitionSheet, 10, rowNestedIdx, propDef.getShortName());
							UtilsXLS.addCell(nesteddefinitionSheet, 11, rowNestedIdx, getWidgetTextRenderingDescription(npropDef.getRendering()));
	    	            	if (npropDef.getLabelMinSize() != null && npropDef.getLabelMinSize() > 0) {
	    	            		UtilsXLS.addCell(nesteddefinitionSheet, 12, rowNestedIdx, npropDef.getLabelMinSize()+"");
	    	            	}
	    	            	if (npropDef.getFieldMinSize() != null && npropDef.getFieldMinSize().getCol() > 0) {
	    	            		UtilsXLS.addCell(nesteddefinitionSheet, 13, rowNestedIdx, npropDef.getFieldMinSize().getCol()+"");
	    	            	}
	    	            	if (npropDef.getFieldMinSize() != null && npropDef.getFieldMinSize().getRow() > 0) {
	    	            		UtilsXLS.addCell(nesteddefinitionSheet, 14, rowNestedIdx, npropDef.getFieldMinSize().getRow()+"");
	    	            	}
	    	            	UtilsXLS.addCell(nesteddefinitionSheet, 15, rowNestedIdx, npropDef.isNewline()?"y":"n");
	    	            	UtilsXLS.addCell(nesteddefinitionSheet, 16,rowNestedIdx,getWidgetTextSizeRow(npropDef.getRendering()));
	    	            	UtilsXLS.addCell(nesteddefinitionSheet, 17,rowNestedIdx,getWidgetTextSizeCol(npropDef.getRendering()));
	    	            	UtilsXLS.addCell(nesteddefinitionSheet, 18, rowNestedIdx, "#");

	    	            	if (npropDef.getRendering() instanceof WidgetCheckRadio) {
	    	            		WidgetCheckRadio widget = (WidgetCheckRadio) npropDef.getRendering();
	    	            		String[] cLists = widget.getStaticValues().split("\\|\\|\\|");
	    						// FIXME this make impossible to have the same property
	    						// name in different objects associated with a
	    						// controlled list
	    	            		UtilsXLS.addCell(controlledlistSheet, colControlledList, 0, npropDef.getShortName());
	    	            		int tmpIdx = 1;
	    	            		for (String c : cLists) {
	    	            			UtilsXLS.addCell(controlledlistSheet, colControlledList, tmpIdx, c);
	    	            			tmpIdx++;
	    	            		}
	    	            		colControlledList++;
	    	            	}
	    	        	rowNestedIdx++;
	                }
            }
            
            List<? extends Tab> tabs = applicationService
					.findTabByType(TabDynamicObject.class, dyn);
            for (Tab tab : tabs) {
            	UtilsXLS.addCell(tabSheet, 0, rowTabIdx, oType);
            	UtilsXLS.addCell(tabSheet, 1, rowTabIdx, tab.getShortName());
            	UtilsXLS.addCell(tabSheet, 2, rowTabIdx, tab.getTitle());
            	UtilsXLS.addCell(tabSheet, 3, rowTabIdx, tab.isMandatory()?"y":"n");
            	UtilsXLS.addCell(tabSheet, 4, rowTabIdx, tab.getPriority()+"");
            	UtilsXLS.addCell(tabSheet, 5, rowTabIdx, accessLevels.get(tab.getVisibility()));
				if (tab.getExt() != null) {
					UtilsXLS.addCell(tabSheet, 6, rowTabIdx, tab.getExt());
				}
				if (tab.getMime() != null) {
					UtilsXLS.addCell(tabSheet, 7, rowTabIdx, tab.getMime());
				}
				UtilsXLS.addCell(tabSheet, 8, rowTabIdx, "#");				
				rowTabIdx++;
				
				for (Object boxObj : applicationService.findPropertyHolderInTab(TabDynamicObject.class, tab.getId())) {
					Box box = (Box) boxObj;
					UtilsXLS.addCell(tab2boxSheet, 0, rowTab2boxIdx, oType);
					UtilsXLS.addCell(tab2boxSheet, 1, rowTab2boxIdx, tab.getShortName());
					UtilsXLS.addCell(tab2boxSheet, 2, rowTab2boxIdx, box.getShortName());
					rowTab2boxIdx++;
				}
            }
            
            List<? extends Tab> etabs = applicationService
            		.findEditTabByType(EditTabDynamicObject.class, dyn);
            for (Tab tab : etabs) {
            	UtilsXLS.addCell(etabSheet, 0, rowETabIdx, oType);
            	UtilsXLS.addCell(etabSheet, 1, rowETabIdx, tab.getShortName());
            	UtilsXLS.addCell(etabSheet, 2, rowETabIdx, tab.getTitle());
            	UtilsXLS.addCell(etabSheet, 3, rowETabIdx, tab.isMandatory()?"y":"n");
            	UtilsXLS.addCell(etabSheet, 4, rowETabIdx, tab.getPriority()+"");
            	UtilsXLS.addCell(etabSheet, 5, rowETabIdx, accessLevels.get(tab.getVisibility()));
				if (tab.getExt() != null) {
					UtilsXLS.addCell(etabSheet, 6, rowETabIdx, tab.getExt());
				}
				if (tab.getMime() != null) {
					UtilsXLS.addCell(etabSheet, 7, rowETabIdx, tab.getMime());
				}
				UtilsXLS.addCell(etabSheet, 8, rowETabIdx, "#");				
				rowETabIdx++;
				
				for (Object boxObj : applicationService.findPropertyHolderInTab(EditTabDynamicObject.class, tab.getId())) {
					Box box = (Box) boxObj;
					UtilsXLS.addCell(etab2boxSheet, 0, rowETab2boxIdx, oType);
					UtilsXLS.addCell(etab2boxSheet, 1, rowETab2boxIdx, tab.getShortName());
					UtilsXLS.addCell(etab2boxSheet, 2, rowETab2boxIdx, box.getShortName());
					rowETab2boxIdx++;
				}
            }
            
            List<? extends Box> boxes = applicationService
					.findBoxByType(BoxDynamicObject.class, dyn);
            for (Box box : boxes) {
            	UtilsXLS.addCell(boxSheet, 0, rowBoxIdx, oType);
            	UtilsXLS.addCell(boxSheet, 1, rowBoxIdx, box.isCollapsed()?"y":"n");
            	UtilsXLS.addCell(boxSheet, 2, rowBoxIdx, box.getExternalJSP());
            	UtilsXLS.addCell(boxSheet, 3, rowBoxIdx, box.getPriority()+"");
            	UtilsXLS.addCell(boxSheet, 4, rowBoxIdx, box.getShortName());
            	UtilsXLS.addCell(boxSheet, 5, rowBoxIdx, box.getTitle());
            	UtilsXLS.addCell(boxSheet, 6, rowBoxIdx, box.isUnrelevant()?"y":"n");
            	UtilsXLS.addCell(boxSheet, 7, rowBoxIdx, accessLevels.get(box.getVisibility()));
            	UtilsXLS.addCell(boxSheet, 8, rowBoxIdx, "#");            	
				rowBoxIdx++;
				
				for (Object pdefObj : applicationService.findContainableInPropertyHolder(BoxDynamicObject.class,
						box.getId())) {
					Containable pdef = (Containable) pdefObj;
					UtilsXLS.addCell(box2metadataSheet, 0, rowBox2metadataIdx, oType);
					UtilsXLS.addCell(box2metadataSheet, 1, rowBox2metadataIdx, box.getShortName());
					UtilsXLS.addCell(box2metadataSheet, 2, rowBox2metadataIdx, pdef.getShortName());
					rowBox2metadataIdx++;
				}
            }
        }

        // All sheets and cells added. Now write out the workbook
        workbook.write(os);
	}

	private static String getWidgetTextRenderingDescription(AWidget widget) {
		if (widget instanceof WidgetTesto) {
			return ((WidgetTesto) widget).getDisplayFormat();
		}
		else if (widget instanceof WidgetPointer) {
			return ((WidgetPointer) widget).getUrlPath();
		}
		return "";
	}

	private static String getWidgetTextSizeCol(AWidget widget) {
		if (widget instanceof WidgetTesto) {
			Integer col = ((WidgetTesto) widget).getDimensione().getCol();
			if(col != null) {
				return Integer.toString(col);
			}
		}
		return "";
	}
	private static String getWidgetTextSizeRow(AWidget widget) {
		if (widget instanceof WidgetTesto) {
			Integer row = ((WidgetTesto) widget).getDimensione().getRow();
			if(row != null) {
				return Integer.toString(row);
			}
		}
		return "";
	}	
	
	private static String getWidgetDescription(AWidget widget, boolean repeatable) {
		if (widget instanceof WidgetTesto) return "text";
		else if (widget instanceof WidgetDate) return "date";
		else if (widget instanceof WidgetLink) return "link";
		else if (widget instanceof WidgetPointer) return "pointer";
		else if (widget instanceof WidgetBoolean) return "boolean";
		else if (widget instanceof WidgetEPerson) return "eperson";
		else if (widget instanceof WidgetGroup) return "group";
		else if (widget instanceof WidgetFile) {
			return ((WidgetFile) widget).isShowPreview()?"image":"file";
		}
		else if (widget instanceof WidgetCheckRadio) {
			return ((WidgetCheckRadio) widget).isDropdown() != null && ((WidgetCheckRadio) widget).isDropdown()?
					"dropdown": repeatable?"checkbox":"radio";
		}
		throw new IllegalArgumentException("Widget unknown "+widget.getClass().getSimpleName());
	}
	
	private static String getPointerClassDescription(ApplicationService applicationService, AWidget widget) {
		if (widget instanceof WidgetPointerOU) {
			return "ou";
		} else if (widget instanceof WidgetPointerRP) {
			return "rp";
		} else if (widget instanceof WidgetPointerPJ) {
			return "pj";
		} else if (widget instanceof WidgetPointerDO) {
			String idString = ((WidgetPointerDO) widget).getFilterExtended();
			return applicationService.get(DynamicObjectType.class,
					Integer.valueOf(idString.substring("search.resourcetype:".length())) - 1000).getShortName();
		}
		return "n";
	}
}
