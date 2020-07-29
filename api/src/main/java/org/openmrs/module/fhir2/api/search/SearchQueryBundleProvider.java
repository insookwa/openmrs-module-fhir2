/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.search;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IPrimitiveType;
import org.hl7.fhir.r4.model.InstantType;
import org.openmrs.Auditable;
import org.openmrs.OpenmrsObject;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.dao.FhirDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.ToFhirTranslator;
import org.springframework.transaction.annotation.Transactional;

public class SearchQueryBundleProvider<T extends OpenmrsObject & Auditable, U extends IBaseResource> implements IBundleProvider, Serializable {
	
	private static final long serialVersionUID = 3L;
	
	private final FhirDao<T> dao;
	
	private final Date datePublished;
	
	private final SearchParameterMap theParams;
	
	private final ToFhirTranslator<T, U> translator;
	
	private final UUID uuid;
	
	private final FhirGlobalPropertyService globalPropertyService;
	
	private transient Integer count;
	
	private transient Integer pageSize;
	
	private transient List<String> matchingResourceUuids;
	
	public SearchQueryBundleProvider(SearchParameterMap theParams, FhirDao<T> dao, ToFhirTranslator<T, U> translator,
	    FhirGlobalPropertyService globalPropertyService) {
		this.dao = dao;
		this.datePublished = new Date();
		this.theParams = theParams;
		this.translator = translator;
		this.uuid = UUID.randomUUID();
		this.globalPropertyService = globalPropertyService;
	}
	
	@Override
	public IPrimitiveType<Date> getPublished() {
		return new InstantType(datePublished);
	}
	
	@Nonnull
	@Transactional(readOnly = true)
	@Override
	public List<IBaseResource> getResources(int fromIndex, int toIndex) {
		if (matchingResourceUuids == null) {
			matchingResourceUuids = dao.getSearchResultUuids(theParams);
		}
		
		if (matchingResourceUuids.isEmpty()) {
			return Collections.emptyList();
		}
		
		int firstResult = 0;
		if (fromIndex >= 0) {
			firstResult = fromIndex;
		}
		
		// NPE-safe unboxing
		int lastResult = Integer.MAX_VALUE;
		Integer lastResultHolder = this.size();
		lastResult = lastResultHolder == null ? lastResult : lastResultHolder;
		
		if (toIndex - firstResult > 0) {
			lastResult = Math.min(lastResult, toIndex);
		}
		
		return dao.getSearchResults(this.theParams, this.matchingResourceUuids, firstResult, lastResult).stream()
		        .map(translator::toFhirResource).filter(Objects::nonNull).collect(Collectors.toList());
	}
	
	@Nullable
	@Override
	public String getUuid() {
		return uuid.toString();
	}
	
	@Override
	public Integer preferredPageSize() {
		if (pageSize == null) {
			pageSize = globalPropertyService.getGlobalProperty(FhirConstants.OPENMRS_FHIR_DEFAULT_PAGE_SIZE, 10);
		}
		
		return pageSize;
	}
	
	@Nullable
	@Override
	public Integer size() {
		if (matchingResourceUuids == null) {
			matchingResourceUuids = dao.getSearchResultUuids(theParams);
		}
		
		if (count == null) {
			count = matchingResourceUuids.size();
		}
		
		return count;
	}
}
