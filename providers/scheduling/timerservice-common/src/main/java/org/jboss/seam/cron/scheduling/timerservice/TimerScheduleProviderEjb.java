/**
 * JBoss, Home of Professional Open Source Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual contributors by the @authors
 * tag. See the copyright.txt in the distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.seam.cron.scheduling.timerservice;

import javax.annotation.PostConstruct;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import org.jboss.seam.cron.api.exception.CronProviderInitialisationException;
import org.jboss.seam.cron.util.PropertyResolver;
import org.slf4j.Logger;

/**
 * If the HA singleton version is successfully started (eg: via the HATimeServiceActivator) then it will set the
 * TimerScheduleConfig.haServiceStarted flag to true which will cause this default, non-ha @Startup version to skip its initialization when
 * it starts up.
 *
 * @author peteroyle
 */
@Startup
@Singleton
@Lock(LockType.READ) // serialise backed-up jobs. Use @AccessTimeout(value = 1, unit = TimeUnit.MINUTES) on @Observes methods to specify a finite wait time when jobs back up.
public class TimerScheduleProviderEjb extends TimerScheduleProviderBase {

    @Inject
    private Logger log;

    @PostConstruct
    public void initUnlessHAIsPresent() {
        log.info("initializing the default @Startup TimerScheduleProviderEjb");
        // give the HA singleton time to start up
        long maxTimetoWait = 5000;
        long waitPart = 500;
        while (maxTimetoWait > 0) {
            log.info("Waiting for HA Singleton SchedulerBean to come online ({} left)", maxTimetoWait);
            try {
                Thread.sleep(waitPart);
            } catch (InterruptedException ex) {
                throw new CronProviderInitialisationException("Woken while waiting for HA scheduler to activate, bailing out.", ex);
            }
            if (scheduleConfigs.isHaServiceStarted()) {
                // stop waiting
                maxTimetoWait = -1;
            } else {
                maxTimetoWait -= waitPart;
            }
        }
        // check our status agains the configuration and decide whether to start up the non-HA version or bail out with an error
        final String haSingletonMode = PropertyResolver.resolve("ha.singleton.mode", false);
        final boolean haMandatory = haSingletonMode != null && haSingletonMode.equalsIgnoreCase("mandatory");
        log.info("HA Singleton Mode: {}", haSingletonMode);
        log.info("HA Is Mandatory: {}", haMandatory);
        if (haMandatory) {
            log.info("Non-HA " + TimerScheduleProviderEjb.class.getSimpleName() + " is disabled since HA mode is set to mandatory. Skipping initialization");
            if (!scheduleConfigs.isHaServiceStarted()) {
                throw new CronProviderInitialisationException("HA Service specified as mandatory, but it failed to start in the time allocated");
            }
        } else {
            if (scheduleConfigs.isHaServiceStarted()) {
                log.info("HA SchedulerBean already started, skipping initialization of " + TimerScheduleProviderEjb.class.getSimpleName());
            } else {
                log.info("Starting the default, non-HA timer service bean " + TimerScheduleProviderEjb.class.getSimpleName());
                super.initScheduledTriggers();
            }
        }
    }
}
