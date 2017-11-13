/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.model.hateoas;

import org.dspace.app.rest.model.DefaultAccessConditionRest;
import org.dspace.app.rest.model.LicenseRest;
import org.dspace.app.rest.model.hateoas.annotations.RelNameDSpaceResource;
import org.springframework.hateoas.ResourceSupport;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

/**
 * Access Condition Rest HAL Resource. The HAL Resource wraps the REST Resource adding
 * support for the links and embedded resources
 * 
 * @author Luigi Andrea Pascarelli (luigiandrea.pascarelli at 4science.it)
 *
 */
@RelNameDSpaceResource(DefaultAccessConditionRest.NAME)
public class DefaultAccessConditionResource extends ResourceSupport {

	@JsonUnwrapped
	private final DefaultAccessConditionRest data;

	public DefaultAccessConditionResource(DefaultAccessConditionRest entry) {
		this.data = entry;
	}

	public DefaultAccessConditionRest getData() {
		return data;
	}

}