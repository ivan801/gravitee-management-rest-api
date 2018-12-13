/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.management.service;

import io.gravitee.management.model.EntryPointEntity;
import io.gravitee.management.model.NewEntryPointEntity;
import io.gravitee.management.model.UpdateEntryPointEntity;
import io.gravitee.management.service.exceptions.EntryPointNotFoundException;
import io.gravitee.management.service.exceptions.EntryPointTagsAlreadyExistsException;
import io.gravitee.management.service.impl.EntryPointServiceImpl;
import io.gravitee.repository.management.api.EntryPointRepository;
import io.gravitee.repository.management.model.EntryPoint;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.util.Sets.newHashSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Azize ELAMRANI (azize at graviteesource.com)
 * @author GraviteeSource Team
 */
@RunWith(MockitoJUnitRunner.class)
public class EntryPointServiceTest {

    private static final String ID = "123";
    private static final String VALUE = "https://api.mycompany.com";
    private static final String TAG = "private;product";
    private static final String[] TAGS = new String[]{"private", "product"};

    private static final String NEW_VALUE = "https://public-api.mycompany.com";
    private static final String NEW_TAG = "public;product";
    private static final String[] NEW_TAGS = new String[]{"public", "product"};

    private static final String UNKNOWN_ID = "unknown";

    @InjectMocks
    private EntryPointService entryPointService = new EntryPointServiceImpl();

    @Mock
    private AuditService auditService;
    @Mock
    private EntryPointRepository entryPointRepository;
    private final EntryPoint entryPointCreated = new EntryPoint();
    private final EntryPoint entryPointUpdated = new EntryPoint();

    @Before
    public void init() throws Exception {
        entryPointCreated.setId(ID);
        entryPointCreated.setValue(VALUE);
        entryPointCreated.setTags(TAG);
        when(entryPointRepository.create(any())).thenReturn(entryPointCreated);
        when(entryPointRepository.findById(ID)).thenReturn(of(entryPointCreated));

        entryPointUpdated.setId(ID);
        entryPointUpdated.setValue(NEW_VALUE);
        entryPointUpdated.setTags(NEW_TAG);
        when(entryPointRepository.update(any())).thenReturn(entryPointUpdated);

        when(entryPointRepository.findById(UNKNOWN_ID)).thenReturn(empty());
    }

    @Test
    public void shouldCreate() {
        final NewEntryPointEntity entryPoint = new NewEntryPointEntity();
        entryPoint.setValue(VALUE);
        entryPoint.setTags(TAGS);
        final EntryPointEntity entryPointEntity = entryPointService.create(entryPoint);
        assertEquals(ID, entryPointEntity.getId());
        assertEquals(VALUE, entryPointEntity.getValue());
        assertNotNull(entryPointEntity.getTags());
        assertEquals(2, entryPointEntity.getTags().length);
    }

    @Test
    public void shouldUpdate() {
        final UpdateEntryPointEntity entryPoint = new UpdateEntryPointEntity();
        entryPoint.setId(ID);
        entryPoint.setValue(NEW_VALUE);
        entryPoint.setTags(NEW_TAGS);
        final EntryPointEntity entryPointEntity = entryPointService.update(entryPoint);
        assertEquals(ID, entryPointEntity.getId());
        assertEquals(NEW_VALUE, entryPointEntity.getValue());
        assertNotNull(entryPointEntity.getTags());
        assertEquals(2, entryPointEntity.getTags().length);
    }

    @Test
    public void shouldUpdateWithSameTags() throws Exception {
        // use to check existing tags excluding current entry point
        when(entryPointRepository.findAll()).thenReturn(newHashSet(singletonList(entryPointUpdated)));

        final UpdateEntryPointEntity entryPoint = new UpdateEntryPointEntity();
        entryPoint.setId(ID);
        entryPoint.setValue(NEW_VALUE);
        entryPoint.setTags(TAGS);
        final EntryPointEntity entryPointEntity = entryPointService.update(entryPoint);
        assertEquals(ID, entryPointEntity.getId());
        assertEquals(NEW_VALUE, entryPointEntity.getValue());
        assertNotNull(entryPointEntity.getTags());
        assertEquals(2, entryPointEntity.getTags().length);
    }

    @Test
    public void shouldDelete() throws Exception {
        entryPointService.delete(ID);
        verify(entryPointRepository).delete(ID);
    }

    @Test
    public void shouldFindAll() throws Exception {
        when(entryPointRepository.findAll()).thenReturn(newHashSet(singletonList(entryPointCreated)));
        final List<EntryPointEntity> entryPoints = entryPointService.findAll();
        assertNotNull(entryPoints);
        assertEquals(1, entryPoints.size());
    }

    @Test(expected = EntryPointNotFoundException.class)
    public void shouldNotUpdate() {
        final UpdateEntryPointEntity entryPoint = new UpdateEntryPointEntity();
        entryPoint.setId(UNKNOWN_ID);
        entryPointService.update(entryPoint);
    }

    @Test(expected = EntryPointNotFoundException.class)
    public void shouldNotDelete() {
        entryPointService.delete(UNKNOWN_ID);
    }

    @Test(expected = EntryPointTagsAlreadyExistsException.class)
    public void shouldNotCreateWithSameTags() throws Exception {
        when(entryPointRepository.findAll()).thenReturn(newHashSet(singletonList(entryPointCreated)));

        final NewEntryPointEntity entryPoint = new NewEntryPointEntity();
        entryPoint.setTags(new String[]{"product", "private"});
        entryPointService.create(entryPoint);
    }

    @Test(expected = EntryPointTagsAlreadyExistsException.class)
    public void shouldNotUpdateWithSameTags() throws Exception {
        when(entryPointRepository.findAll()).thenReturn(newHashSet(singletonList(entryPointUpdated)));

        final UpdateEntryPointEntity entryPoint = new UpdateEntryPointEntity();
        entryPoint.setId("new ID");
        entryPoint.setValue(VALUE);
        entryPoint.setTags(NEW_TAGS);
        entryPointService.update(entryPoint);
    }
}
