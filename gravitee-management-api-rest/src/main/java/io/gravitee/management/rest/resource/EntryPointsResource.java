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
package io.gravitee.management.rest.resource;

import io.gravitee.common.http.MediaType;
import io.gravitee.management.model.EntryPointEntity;
import io.gravitee.management.model.NewEntryPointEntity;
import io.gravitee.management.model.UpdateEntryPointEntity;
import io.gravitee.management.model.permissions.RolePermission;
import io.gravitee.management.model.permissions.RolePermissionAction;
import io.gravitee.management.rest.security.Permission;
import io.gravitee.management.rest.security.Permissions;
import io.gravitee.management.service.EntryPointService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * @author Azize ELAMRANI (azize.elamrani at graviteesource.com)
 * @author GraviteeSource Team
 */
@Api(tags = {"Entry points"})
public class EntryPointsResource extends AbstractResource  {

    @Autowired
    private EntryPointService entryPointService;

    @GET
    @Path("{entryPointId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Permissions({
            @Permission(value = RolePermission.MANAGEMENT_ENTRY_POINT, acls = RolePermissionAction.READ)
    })
    public EntryPointEntity get(final @PathParam("entryPointId") String entryPointId)  {
        return entryPointService.findById(entryPointId);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Permissions({
            @Permission(value = RolePermission.MANAGEMENT_ENTRY_POINT, acls = RolePermissionAction.CREATE)
    })
    public List<EntryPointEntity> list()  {
        return entryPointService.findAll()
                .stream()
                .sorted((o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.getValue(), o2.getValue()))
                .collect(toList());
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Permissions({
            @Permission(value = RolePermission.MANAGEMENT_ENTRY_POINT, acls = RolePermissionAction.CREATE)
    })
    public EntryPointEntity create(@Valid @NotNull final NewEntryPointEntity entryPoint) {
        return entryPointService.create(entryPoint);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Permissions({
            @Permission(value = RolePermission.MANAGEMENT_ENTRY_POINT, acls = RolePermissionAction.UPDATE)
    })
    public EntryPointEntity update(@Valid @NotNull final UpdateEntryPointEntity entryPoint) {
        return entryPointService.update(entryPoint);
    }

    @Path("{entryPoint}")
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Permissions({
            @Permission(value = RolePermission.MANAGEMENT_ENTRY_POINT, acls = RolePermissionAction.DELETE)
    })
    public void delete(@PathParam("entryPoint") String entryPoint) {
        entryPointService.delete(entryPoint);
    }
}
