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
package io.gravitee.management.service.impl;

import io.gravitee.common.utils.UUID;
import io.gravitee.management.model.EntryPointEntity;
import io.gravitee.management.model.NewEntryPointEntity;
import io.gravitee.management.model.UpdateEntryPointEntity;
import io.gravitee.management.service.AuditService;
import io.gravitee.management.service.EntryPointService;
import io.gravitee.management.service.exceptions.EntryPointNotFoundException;
import io.gravitee.management.service.exceptions.EntryPointTagsAlreadyExistsException;
import io.gravitee.management.service.exceptions.TechnicalManagementException;
import io.gravitee.repository.exceptions.TechnicalException;
import io.gravitee.repository.management.api.EntryPointRepository;
import io.gravitee.repository.management.model.EntryPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static io.gravitee.repository.management.model.Audit.AuditProperties.ENTRY_POINT;
import static io.gravitee.repository.management.model.EntryPoint.AuditEvent.*;
import static java.util.Arrays.sort;

/**
 * @author Azize ELAMRANI (azize at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class EntryPointServiceImpl extends TransactionalService implements EntryPointService {

    private final Logger LOGGER = LoggerFactory.getLogger(EntryPointServiceImpl.class);
    private final static String SEPARATOR = ";";

    @Autowired
    private AuditService auditService;
    @Autowired
    private EntryPointRepository entryPointRepository;

    @Override
    public EntryPointEntity findById(final String entryPointId) {
        try {
            LOGGER.debug("Find by id {}", entryPointId);
            final Optional<EntryPoint> optionalEntryPoint = entryPointRepository.findById(entryPointId);
            if (!optionalEntryPoint.isPresent()) {
                throw new EntryPointNotFoundException(entryPointId);
            }
            return convert(optionalEntryPoint.get());
        } catch (TechnicalException ex) {
            LOGGER.error("An error occurs while trying to find all entryPoints", ex);
            throw new TechnicalManagementException("An error occurs while trying to find all entryPoints", ex);
        }
    }

    @Override
    public List<EntryPointEntity> findAll() {
        try {
            LOGGER.debug("Find all APIs");
            return entryPointRepository.findAll()
                    .stream()
                    .map(this::convert).collect(Collectors.toList());
        } catch (TechnicalException ex) {
            LOGGER.error("An error occurs while trying to find all entryPoints", ex);
            throw new TechnicalManagementException("An error occurs while trying to find all entryPoints", ex);
        }
    }

    @Override
    public EntryPointEntity create(final NewEntryPointEntity entryPointEntity) {
        try {
            checkTagsOnExistingEntryPoints(entryPointEntity.getTags(), null);
            final EntryPoint entryPoint = convert(entryPointEntity);
            final EntryPointEntity savedEntryPoint = convert(entryPointRepository.create(entryPoint));
            auditService.createPortalAuditLog(
                    Collections.singletonMap(ENTRY_POINT, entryPoint.getId()),
                    ENTRY_POINT_CREATED,
                    new Date(),
                    null,
                    entryPoint);
            return savedEntryPoint;
        } catch (TechnicalException ex) {
            LOGGER.error("An error occurs while trying to create entryPoint {}", entryPointEntity.getValue(), ex);
            throw new TechnicalManagementException("An error occurs while trying to create entryPoint " + entryPointEntity.getValue(), ex);
        }
    }

    @Override
    public EntryPointEntity update(final UpdateEntryPointEntity entryPointEntity) {
        try {
            checkTagsOnExistingEntryPoints(entryPointEntity.getTags(), entryPointEntity.getId());
            final Optional<EntryPoint> entryPointOptional = entryPointRepository.findById(entryPointEntity.getId());
            if (entryPointOptional.isPresent()) {
                final EntryPoint entryPoint = convert(entryPointEntity);
                final EntryPointEntity savedEntryPoint = convert(entryPointRepository.update(entryPoint));
                auditService.createPortalAuditLog(
                        Collections.singletonMap(ENTRY_POINT, entryPoint.getId()),
                        ENTRY_POINT_UPDATED,
                        new Date(),
                        entryPointOptional.get(),
                        entryPoint);
                return savedEntryPoint;
            } else {
                throw new EntryPointNotFoundException(entryPointEntity.getId());
            }
        } catch (TechnicalException ex) {
            LOGGER.error("An error occurs while trying to update entryPoint {}", entryPointEntity.getValue(), ex);
            throw new TechnicalManagementException("An error occurs while trying to update entryPoint " + entryPointEntity.getValue(), ex);
        }
    }

    private void checkTagsOnExistingEntryPoints(final String[] tags, final String entryPointIdToIgnore) throws TechnicalException {
        // first check for existing entry point with same tags
        final boolean tagsAlreadyDefined = entryPointRepository.findAll().stream()
                .filter(entryPoint -> entryPointIdToIgnore == null || !entryPoint.getId().equals(entryPointIdToIgnore))
                .anyMatch(entryPoint -> {
                    final String[] entryPointTags = entryPoint.getTags().split(SEPARATOR);
                    sort(entryPointTags);
                    sort(tags);
                    return Arrays.equals(entryPointTags, tags);
                });
        if (tagsAlreadyDefined) {
            throw new EntryPointTagsAlreadyExistsException();
        }
    }

    @Override
    public void delete(final String entryPointId) {
        try {
            Optional<EntryPoint> entryPointOptional = entryPointRepository.findById(entryPointId);
            if (entryPointOptional.isPresent()) {
                entryPointRepository.delete(entryPointId);
                auditService.createPortalAuditLog(
                        Collections.singletonMap(ENTRY_POINT, entryPointId),
                        ENTRY_POINT_DELETED,
                        new Date(),
                        null,
                        entryPointOptional.get());
            } else {
                throw new EntryPointNotFoundException(entryPointId);
            }
        } catch (TechnicalException ex) {
            LOGGER.error("An error occurs while trying to delete entryPoint {}", entryPointId, ex);
            throw new TechnicalManagementException("An error occurs while trying to delete entryPoint " + entryPointId, ex);
        }
    }

    private EntryPoint convert(final NewEntryPointEntity entryPointEntity) {
        final EntryPoint entryPoint = new EntryPoint();
        entryPoint.setId(UUID.toString(UUID.random()));
        entryPoint.setValue(entryPointEntity.getValue());
        entryPoint.setTags(String.join(SEPARATOR, entryPointEntity.getTags()));
        return entryPoint;
    }

    private EntryPoint convert(final UpdateEntryPointEntity entryPointEntity) {
        final EntryPoint entryPoint = new EntryPoint();
        entryPoint.setId(entryPointEntity.getId());
        entryPoint.setValue(entryPointEntity.getValue());
        entryPoint.setTags(String.join(SEPARATOR, entryPointEntity.getTags()));
        return entryPoint;
    }

    private EntryPointEntity convert(final EntryPoint entryPoint) {
        final EntryPointEntity entryPointEntity = new EntryPointEntity();
        entryPointEntity.setId(entryPoint.getId());
        entryPointEntity.setValue(entryPoint.getValue());
        entryPointEntity.setTags(entryPoint.getTags().split(SEPARATOR));
        return entryPointEntity;
    }
}
