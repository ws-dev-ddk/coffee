/*-
 * #%L
 * Coffee
 * %%
 * Copyright (C) 2020 - 2022 i-Cell Mobilsoft Zrt.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package hu.icellmobilsoft.coffee.module.redis.config;

import static org.mockito.Mockito.mock;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.metrics.MetricRegistry;
import org.jboss.weld.junit.MockBean;
import org.jboss.weld.junit5.EnableWeld;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;
import org.jboss.weld.proxy.WeldClientProxy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import hu.icellmobilsoft.coffee.cdi.metric.spi.IJedisMetricsHandler;

/**
 * Test for CustomJedisMetricsHandler
 * 
 * @author czenczl
 * @since 2.2.0
 *
 */
@EnableWeld
@Tag("weld")
@ExtendWith(WeldJunit5Extension.class)
@DisplayName("Redis pool config tests")
class CustomMetricsHandlerTest {

    static final String CONFIG_KEY = "test";

    @Inject
    private IJedisMetricsHandler jedisMetricsHandler;

    @WeldSetup
    public WeldInitiator weld = WeldInitiator.from(WeldInitiator.createWeld()
            // beans.xml scan
            .enableDiscovery().addBeanClass(CustomJedisMetricsHandler.class).addAlternative(CustomJedisMetricsHandler.class))
            .addBeans(MockBean.of(mock(MetricRegistry.class), MetricRegistry.class))
            // start request scope + build
            .activate(RequestScoped.class).build();

    @Test
    @DisplayName("custom jedis metric test")
    void metricsHandler() {
        Assertions.assertNotNull(jedisMetricsHandler);

        jedisMetricsHandler.addMetric("key1", "key2", () -> 1L, () -> 2);

        Assertions.assertInstanceOf(WeldClientProxy.class, jedisMetricsHandler);
        Object instance = ((WeldClientProxy) jedisMetricsHandler).getMetadata().getContextualInstance();
        // must be JedisMpMetricsHandler
        Assertions.assertInstanceOf(CustomJedisMetricsHandler.class, instance);
    }

}
