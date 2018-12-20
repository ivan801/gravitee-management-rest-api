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

import io.gravitee.common.data.domain.Page;
import io.gravitee.management.model.NewSubscriptionEntity;
import io.gravitee.management.model.ProcessSubscriptionEntity;
import io.gravitee.management.model.SubscriptionEntity;
import io.gravitee.management.model.UpdateSubscriptionEntity;
import io.gravitee.management.model.common.Pageable;
import io.gravitee.management.model.pagedresult.Metadata;
import io.gravitee.management.model.subscription.SubscriptionQuery;

import java.util.Collection;
import java.util.List;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface SubscriptionService {

    /**
     * Get a subscription from its ID.
     * @param subscription
     * @return
     */
    SubscriptionEntity findById(String subscription);

    Collection<SubscriptionEntity> findByApplicationAndPlan(String application, String plan);

    Collection<SubscriptionEntity> findByApi(String api);

    Collection<SubscriptionEntity> findByPlan(String plan);

    SubscriptionEntity create(NewSubscriptionEntity newSubscriptionEntity);

    SubscriptionEntity update(UpdateSubscriptionEntity subscription);

    SubscriptionEntity update(UpdateSubscriptionEntity subscription, String clientId);

    SubscriptionEntity process(ProcessSubscriptionEntity processSubscription, String validator);

    SubscriptionEntity pause(String subscription);

    SubscriptionEntity resume(String subscription);

    SubscriptionEntity close(String subscription);

    void delete(String subscription);

    Collection<SubscriptionEntity> search(SubscriptionQuery query);

    Page<SubscriptionEntity> search(SubscriptionQuery query, Pageable pageable);

    Metadata getMetadata(List<SubscriptionEntity> subscriptions);
}
