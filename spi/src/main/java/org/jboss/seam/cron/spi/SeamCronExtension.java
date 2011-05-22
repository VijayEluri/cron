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
package org.jboss.seam.cron.spi;

import org.jboss.seam.cron.spi.asynchronous.CronAsyncMethodInvocationExtension;
import org.jboss.seam.cron.spi.scheduling.CronSchedulingExtension;
import java.util.HashSet;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.enterprise.inject.spi.ProcessObserverMethod;
import org.jboss.logging.Logger;
import org.jboss.seam.cron.spi.asynchronous.AsynchronousStrategy;
import org.jboss.seam.cron.spi.scheduling.CronScheduleProvider;
import org.jboss.seam.cron.util.CdiUtils;

/**
 *
 * @author Peter Royle
 */
@ApplicationScoped
public class SeamCronExtension implements Extension {

    private final Set<ObserverMethod> allObservers = new HashSet<ObserverMethod>();
    private Logger log = Logger.getLogger(SeamCronExtension.class);

    public void registerCronEventObserver(@Observes ProcessObserverMethod pom) {
        allObservers.add(pom.getObserverMethod());
    }

    public void initProviders(@Observes AfterDeploymentValidation afterValid, BeanManager manager,
            CronSchedulingExtension cronSchedExt, CronAsyncMethodInvocationExtension cronAsyncExt) {
        CronScheduleProvider schedulingProvider = CdiUtils.getInstanceByType(manager, CronScheduleProvider.class);
        if (schedulingProvider != null) {
            cronSchedExt.processScheduledTriggers(manager, schedulingProvider, allObservers);
        }
        AsynchronousStrategy asyncStrategy = CdiUtils.getInstanceByType(manager, AsynchronousStrategy.class);
        if (asyncStrategy != null) {
            cronAsyncExt.initProviderAsynchronous(asyncStrategy);
        }
    }

    public void stopProviders(@Observes BeforeShutdown event, BeanManager manager,
            CronSchedulingExtension cronSchedExt, CronAsyncMethodInvocationExtension cronAsyncExt) {

        CronScheduleProvider schedulingProvider = CdiUtils.getInstanceByType(manager, CronScheduleProvider.class);
        if (schedulingProvider != null) {
            cronSchedExt.stopProvidersScheduler(schedulingProvider);
        }
        AsynchronousStrategy asyncStrategy = CdiUtils.getInstanceByType(manager, AsynchronousStrategy.class);
        if (asyncStrategy != null) {
            cronAsyncExt.stopProviderAsynchronous(asyncStrategy);
        }
    }
}
