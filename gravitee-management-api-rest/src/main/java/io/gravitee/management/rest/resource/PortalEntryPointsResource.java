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
import io.gravitee.management.service.EntryPointService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * @author Azize ELAMRANI (azize.elamrani at graviteesource.com)
 * @author GraviteeSource Team
 */
@Api(tags = {"Portal entry points"})
@Path("/entry-points")
public class PortalEntryPointsResource extends AbstractResource  {

    @Autowired
    private EntryPointService entryPointService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<EntryPointEntity> list()  {
        return entryPointService.findAll().stream()
                .peek(entryPointEntity -> entryPointEntity.setId(null))
                .collect(toList());
    }
}
