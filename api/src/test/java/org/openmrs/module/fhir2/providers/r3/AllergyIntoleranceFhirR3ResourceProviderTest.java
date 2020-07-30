/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers.r3;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.MethodNotAllowedException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hamcrest.CoreMatchers;
import org.hl7.fhir.convertors.conv30_40.AllergyIntolerance30_40;
import org.hl7.fhir.dstu3.model.AllergyIntolerance;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Provenance;
import org.hl7.fhir.dstu3.model.Resource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirAllergyIntoleranceService;
import org.openmrs.module.fhir2.providers.r4.MockIBundleProvider;

@RunWith(MockitoJUnitRunner.class)
public class AllergyIntoleranceFhirR3ResourceProviderTest extends BaseFhirR3ProvenanceResourceTest<org.hl7.fhir.r4.model.AllergyIntolerance> {
	
	private static final String ALLERGY_UUID = "1085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String WRONG_ALLERGY_UUID = "2085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String CODED_ALLERGEN_UUID = "5085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String SEVERITY_CONCEPT_UUID = "5088AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String CODED_REACTION_UUID = "5087AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String LAST_UPDATED_DATE = "2020-09-03";
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	private static final int PREFERRED_PAGE_SIZE = 1;
	
	private static final int COUNT = 1;
	
	@Mock
	private FhirAllergyIntoleranceService service;
	
	private AllergyIntoleranceFhirResourceProvider resourceProvider;
	
	private org.hl7.fhir.r4.model.AllergyIntolerance allergyIntolerance;
	
	@Before
	public void setup() {
		resourceProvider = new AllergyIntoleranceFhirResourceProvider();
		resourceProvider.setAllergyIntoleranceService(service);
	}
	
	@Before
	public void initAllergyIntolerance() {
		allergyIntolerance = new org.hl7.fhir.r4.model.AllergyIntolerance();
		allergyIntolerance.setId(ALLERGY_UUID);
		allergyIntolerance.addCategory(org.hl7.fhir.r4.model.AllergyIntolerance.AllergyIntoleranceCategory.FOOD);
		setProvenanceResources(allergyIntolerance);
	}
	
	private List<AllergyIntolerance> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX).stream().filter(it -> it instanceof AllergyIntolerance)
		        .map(it -> (AllergyIntolerance) it).collect(Collectors.toList());
	}
	
	@Test
	public void getResourceType_shouldReturnResourceType() {
		assertThat(resourceProvider.getResourceType(), equalTo(AllergyIntolerance.class));
		assertThat(resourceProvider.getResourceType().getName(), equalTo(AllergyIntolerance.class.getName()));
	}
	
	@Test
	public void getAllergyIntoleranceByUuid_shouldReturnMatchingAllergy() {
		when(service.get(ALLERGY_UUID)).thenReturn(allergyIntolerance);
		
		IdType id = new IdType();
		id.setValue(ALLERGY_UUID);
		AllergyIntolerance allergy = resourceProvider.getAllergyIntoleranceById(id);
		assertThat(allergy, notNullValue());
		assertThat(allergy.getId(), notNullValue());
		assertThat(allergy.getId(), equalTo(ALLERGY_UUID));
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void getAllergyIntoleranceByUuid_shouldThrowResourceNotFoundException() {
		IdType id = new IdType();
		id.setValue(WRONG_ALLERGY_UUID);
		AllergyIntolerance result = resourceProvider.getAllergyIntoleranceById(id);
		assertThat(result, nullValue());
	}
	
	@Test
	public void getAllergyIntoleranceHistory_shouldReturnProvenanceResources() {
		IdType id = new IdType();
		id.setValue(ALLERGY_UUID);
		when(service.get(ALLERGY_UUID)).thenReturn(allergyIntolerance);
		
		List<Resource> resources = resourceProvider.getAllergyIntoleranceHistoryById(id);
		assertThat(resources, not(empty()));
		assertThat(resources.stream().findAny().isPresent(), is(true));
		assertThat(resources.stream().findAny().get().getResourceType().name(), equalTo(Provenance.class.getSimpleName()));
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void getAllergyIntoleranceHistoryByWithWrongId_shouldThrowResourceNotFoundException() {
		IdType idType = new IdType();
		idType.setValue(WRONG_ALLERGY_UUID);
		assertThat(resourceProvider.getAllergyIntoleranceHistoryById(idType).isEmpty(), is(true));
		assertThat(resourceProvider.getAllergyIntoleranceHistoryById(idType).size(), equalTo(0));
		
	}
	
	@Test
	public void searchForAllergies_shouldReturnMatchingBundleOfAllergiesByIdentifier() {
		ReferenceAndListParam patient = new ReferenceAndListParam();
		patient.addValue(
		    new ReferenceOrListParam().add(new ReferenceParam().setValue("M4001-1").setChain(Patient.SP_IDENTIFIER)));
		
		when(service.searchForAllergies(argThat(is(patient)), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull())).thenReturn(
		        new MockIBundleProvider<>(Collections.singletonList(allergyIntolerance), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchForAllergies(patient, null, null, null, null, null, null, null,
		    null, null);
		
		List<AllergyIntolerance> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.get(0).fhirType(), is(FhirConstants.ALLERGY_INTOLERANCE));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(resultList.get(0).getId(), equalTo(ALLERGY_UUID));
	}
	
	@Test
	public void searchForAllergies_shouldReturnMatchingBundleOfAllergiesByPatientGivenName() {
		ReferenceAndListParam patient = new ReferenceAndListParam();
		patient.addValue(new ReferenceOrListParam().add(new ReferenceParam().setValue("John").setChain(Patient.SP_GIVEN)));
		
		when(service.searchForAllergies(argThat(is(patient)), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull())).thenReturn(
		        new MockIBundleProvider<>(Collections.singletonList(allergyIntolerance), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchForAllergies(patient, null, null, null, null, null, null, null,
		    null, null);
		
		List<AllergyIntolerance> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.get(0).fhirType(), is(FhirConstants.ALLERGY_INTOLERANCE));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(resultList.get(0).getId(), equalTo(ALLERGY_UUID));
	}
	
	@Test
	public void searchForAllergies_shouldReturnMatchingBundleOfAllergiesByPatientFamilyName() {
		ReferenceAndListParam patient = new ReferenceAndListParam();
		patient.addValue(new ReferenceOrListParam().add(new ReferenceParam().setValue("John").setChain(Patient.SP_FAMILY)));
		
		when(service.searchForAllergies(argThat(is(patient)), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull())).thenReturn(
		        new MockIBundleProvider<>(Collections.singletonList(allergyIntolerance), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchForAllergies(patient, null, null, null, null, null, null, null,
		    null, null);
		
		List<AllergyIntolerance> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.get(0).fhirType(), is(FhirConstants.ALLERGY_INTOLERANCE));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(resultList.get(0).getId(), equalTo(ALLERGY_UUID));
	}
	
	@Test
	public void searchForAllergies_shouldReturnMatchingBundleOfAllergiesByPatientName() {
		ReferenceAndListParam patient = new ReferenceAndListParam();
		patient.addValue(
		    new ReferenceOrListParam().add(new ReferenceParam().setValue("John Doe").setChain(Patient.SP_NAME)));
		
		when(service.searchForAllergies(argThat(is(patient)), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull())).thenReturn(
		        new MockIBundleProvider<>(Collections.singletonList(allergyIntolerance), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchForAllergies(patient, null, null, null, null, null, null, null,
		    null, null);
		
		List<AllergyIntolerance> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.get(0).fhirType(), is(FhirConstants.ALLERGY_INTOLERANCE));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(resultList.get(0).getId(), equalTo(ALLERGY_UUID));
	}
	
	@Test
	public void searchForAllergies_shouldReturnMatchingBundleOfAllergiesWhenPatientGivenNameIsSpecifiedAsSubject() {
		ReferenceAndListParam subject = new ReferenceAndListParam();
		subject.addValue(new ReferenceOrListParam().add(new ReferenceParam().setValue("John").setChain(Patient.SP_GIVEN)));
		
		when(service.searchForAllergies(argThat(is(subject)), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull())).thenReturn(
		        new MockIBundleProvider<>(Collections.singletonList(allergyIntolerance), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchForAllergies(null, subject, null, null, null, null, null, null,
		    null, null);
		
		List<AllergyIntolerance> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.get(0).fhirType(), is(FhirConstants.ALLERGY_INTOLERANCE));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(resultList.get(0).getId(), equalTo(ALLERGY_UUID));
	}
	
	@Test
	public void searchForAllergies_shouldReturnMatchingBundleOfAllergiesByCategory() {
		TokenAndListParam category = new TokenAndListParam();
		category.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue("food")));
		
		when(service.searchForAllergies(isNull(), argThat(is(category)), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull())).thenReturn(
		        new MockIBundleProvider<>(Collections.singletonList(allergyIntolerance), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchForAllergies(null, null, category, null, null, null, null, null,
		    null, null);
		
		List<AllergyIntolerance> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.get(0).fhirType(), is(FhirConstants.ALLERGY_INTOLERANCE));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(resultList.get(0).getId(), equalTo(ALLERGY_UUID));
	}
	
	@Test
	public void searchForAllergies_shouldReturnMatchingBundleOfAllergiesByAllergen() {
		TokenAndListParam allergen = new TokenAndListParam();
		allergen.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue(CODED_ALLERGEN_UUID)));
		
		when(service.searchForAllergies(isNull(), isNull(), argThat(is(allergen)), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull())).thenReturn(
		        new MockIBundleProvider<>(Collections.singletonList(allergyIntolerance), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchForAllergies(null, null, null, allergen, null, null, null, null,
		    null, null);
		
		List<AllergyIntolerance> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.get(0).fhirType(), is(FhirConstants.ALLERGY_INTOLERANCE));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(resultList.get(0).getId(), equalTo(ALLERGY_UUID));
	}
	
	@Test
	public void searchForAllergies_shouldReturnMatchingBundleOfAllergiesBySeverity() {
		TokenAndListParam severity = new TokenAndListParam();
		severity.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue(SEVERITY_CONCEPT_UUID)));
		
		when(service.searchForAllergies(isNull(), isNull(), isNull(), argThat(is(severity)), isNull(), isNull(), isNull(),
		    isNull(), isNull())).thenReturn(
		        new MockIBundleProvider<>(Collections.singletonList(allergyIntolerance), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchForAllergies(null, null, null, null, severity, null, null, null,
		    null, null);
		
		List<AllergyIntolerance> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.get(0).fhirType(), is(FhirConstants.ALLERGY_INTOLERANCE));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(resultList.get(0).getId(), equalTo(ALLERGY_UUID));
	}
	
	@Test
	public void searchForAllergies_shouldReturnMatchingBundleOfAllergiesByManifestation() {
		TokenAndListParam manifestation = new TokenAndListParam();
		manifestation.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue(CODED_REACTION_UUID)));
		
		when(service.searchForAllergies(isNull(), isNull(), isNull(), isNull(), argThat(is(manifestation)), isNull(),
		    isNull(), isNull(), isNull())).thenReturn(
		        new MockIBundleProvider<>(Collections.singletonList(allergyIntolerance), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchForAllergies(null, null, null, null, null, manifestation, null,
		    null, null, null);
		
		List<AllergyIntolerance> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.get(0).fhirType(), is(FhirConstants.ALLERGY_INTOLERANCE));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(resultList.get(0).getId(), equalTo(ALLERGY_UUID));
	}
	
	@Test
	public void searchForAllergies_shouldReturnMatchingBundleOfAllergiesByStatus() {
		TokenAndListParam status = new TokenAndListParam();
		status.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue("active")));
		
		when(service.searchForAllergies(isNull(), isNull(), isNull(), isNull(), isNull(), argThat(is(status)), isNull(),
		    isNull(), isNull())).thenReturn(
		        new MockIBundleProvider<>(Collections.singletonList(allergyIntolerance), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchForAllergies(null, null, null, null, null, null, status, null, null,
		    null);
		
		List<AllergyIntolerance> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.get(0).fhirType(), is(FhirConstants.ALLERGY_INTOLERANCE));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(resultList.get(0).getId(), equalTo(ALLERGY_UUID));
	}
	
	@Test
	public void searchForAllergies_shouldReturnMatchingBundleOfAllergiesByUUID() {
		TokenAndListParam uuid = new TokenAndListParam();
		uuid.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue(ALLERGY_UUID)));
		
		when(service.searchForAllergies(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), argThat(is(uuid)),
		    isNull(), isNull())).thenReturn(
		        new MockIBundleProvider<>(Collections.singletonList(allergyIntolerance), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchForAllergies(null, null, null, null, null, null, null, uuid, null,
		    null);
		
		List<AllergyIntolerance> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.get(0).fhirType(), is(FhirConstants.ALLERGY_INTOLERANCE));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(resultList.get(0).getId(), equalTo(ALLERGY_UUID));
	}
	
	@Test
	public void searchForAllergies_shouldReturnMatchingBundleOfAllergiesByLastUpdated() {
		DateRangeParam dateRangeParam = new DateRangeParam().setLowerBound(LAST_UPDATED_DATE)
		        .setLowerBound(LAST_UPDATED_DATE);
		
		when(service.searchForAllergies(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    argThat(is(dateRangeParam)), isNull())).thenReturn(
		        new MockIBundleProvider<>(Collections.singletonList(allergyIntolerance), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchForAllergies(null, null, null, null, null, null, null, null,
		    dateRangeParam, null);
		
		List<AllergyIntolerance> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.get(0).fhirType(), is(FhirConstants.ALLERGY_INTOLERANCE));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(resultList.get(0).getId(), equalTo(ALLERGY_UUID));
	}
	
	@Test
	public void createAllergyIntolerance_shouldCreateNewAllergyIntolerance() {
		when(service.create(any(org.hl7.fhir.r4.model.AllergyIntolerance.class))).thenReturn(allergyIntolerance);
		
		MethodOutcome result = resourceProvider
		        .creatAllergyIntolerance(AllergyIntolerance30_40.convertAllergyIntolerance(allergyIntolerance));
		assertThat(result, CoreMatchers.notNullValue());
		assertThat(result.getCreated(), is(true));
		assertThat(result.getResource(), CoreMatchers.equalTo(allergyIntolerance));
	}
	
	@Test
	public void updateAllergyIntolerance_shouldUpdateAllergyIntolerance() {
		when(service.update(eq(ALLERGY_UUID), any(org.hl7.fhir.r4.model.AllergyIntolerance.class)))
		        .thenReturn(allergyIntolerance);
		
		MethodOutcome result = resourceProvider.updateAllergyIntolerance(new IdType().setValue(ALLERGY_UUID),
		    AllergyIntolerance30_40.convertAllergyIntolerance(allergyIntolerance));
		assertThat(result, CoreMatchers.notNullValue());
		assertThat(result.getResource(), CoreMatchers.equalTo(allergyIntolerance));
	}
	
	@Test(expected = InvalidRequestException.class)
	public void updateAllergyIntolerance_shouldThrowInvalidRequestForUuidMismatch() {
		when(service.update(eq(WRONG_ALLERGY_UUID), any(org.hl7.fhir.r4.model.AllergyIntolerance.class)))
		        .thenThrow(InvalidRequestException.class);
		
		resourceProvider.updateAllergyIntolerance(new IdType().setValue(WRONG_ALLERGY_UUID),
		    AllergyIntolerance30_40.convertAllergyIntolerance(allergyIntolerance));
	}
	
	@Test(expected = MethodNotAllowedException.class)
	public void updateAllergyIntolerance_shouldThrowMethodNotAllowedIfDoesNotExist() {
		when(service.update(eq(WRONG_ALLERGY_UUID), any(org.hl7.fhir.r4.model.AllergyIntolerance.class)))
		        .thenThrow(MethodNotAllowedException.class);
		
		resourceProvider.updateAllergyIntolerance(new IdType().setValue(WRONG_ALLERGY_UUID),
		    AllergyIntolerance30_40.convertAllergyIntolerance(allergyIntolerance));
	}
	
	@Test
	public void deleteAllergyIntolerance_shouldDeleteRequestedAllergyIntolerance() {
		when(service.delete(ALLERGY_UUID)).thenReturn(allergyIntolerance);
		
		OperationOutcome result = resourceProvider.deleteAllergyIntolerance(new IdType().setValue(ALLERGY_UUID));
		assertThat(result, CoreMatchers.notNullValue());
		assertThat(result.getIssue(), notNullValue());
		assertThat(result.getIssueFirstRep().getSeverity(), equalTo(OperationOutcome.IssueSeverity.INFORMATION));
		assertThat(result.getIssueFirstRep().getDetails().getCodingFirstRep().getCode(), equalTo("MSG_DELETED"));
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void deleteAllergyIntolerance_shouldThrowResourceNotFoundExceptionWhenIdRefersToNonExistantAllergyIntolerance() {
		when(service.delete(WRONG_ALLERGY_UUID)).thenReturn(null);
		resourceProvider.deleteAllergyIntolerance(new IdType().setValue(WRONG_ALLERGY_UUID));
	}
}
