/**
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.seam.cron.asynchronous.spi;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;

/**
 * @author Peter Royle
 */
@ApplicationScoped
public class CronAsyncMethodInvocationExtension implements Extension {

    public void registerTypes(@Observes BeforeBeanDiscovery event, BeanManager manager) {
        event.addAnnotatedType(manager.createAnnotatedType(AsynchronousInterceptor.class));
    }

    /**
     * Initialises the asynchronous method invoker, if necessary.
     *
     */
    public void initProviderScheduler(@Observes AfterDeploymentValidation afterVal, BeanManager beanManager, AsynchronousStrategy asyncStrategy) throws Exception {
        asyncStrategy.initMethodInvoker();
    }

    /**
     * Shutdown the asynchronous method invoker, if necessary.
     */
    @PreDestroy
    public void stopProvidersScheduler(@Observes BeforeShutdown event, AsynchronousStrategy asyncStratey) {
        asyncStratey.shutdownMethodInvoker();
    }

}