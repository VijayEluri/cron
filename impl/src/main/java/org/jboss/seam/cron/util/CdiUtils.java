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
package org.jboss.seam.cron.util;

import java.lang.annotation.Annotation;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

/**
 *
 * @author peteroyle
 */
public class CdiUtils {

    public static Annotation getQualifier(Annotation binding, Class qualifierType) {
        Annotation qualifier = null;
        if (qualifierType.isAssignableFrom(binding.getClass())) {
            qualifier = binding;
        } else {
            // check for meta-annotation
            qualifier = binding.annotationType().getAnnotation(qualifierType);
        }
        return qualifier;
    }

    /**
     * Utility method allowing managed instances of beans to provide entry points
     * for non-managed beans (such as {@link WeldContainer}). Should only called
     * once Weld has finished booting.
     * 
     * @param manager the BeanManager to use to access the managed instance
     * @param type the type of the Bean
     * @param bindings the bean's qualifiers
     * @return a managed instance of the bean
     * @throws IllegalArgumentException if the given type represents a type
     *            variable
     * @throws IllegalArgumentException if two instances of the same qualifier
     *            type are given
     * @throws IllegalArgumentException if an instance of an annotation that is
     *            not a qualifier type is given
     * @throws UnsatisfiedResolutionException if no beans can be resolved * @throws
     *            AmbiguousResolutionException if the ambiguous dependency
     *            resolution rules fail
     * @throws IllegalArgumentException if the given type is not a bean type of
     *            the given bean
     * 
     */
    public static <T> T getInstanceByType(BeanManager manager, Class<T> type, Annotation... bindings) {
        try {
            final Bean<?> bean = manager.resolve(manager.getBeans(type));
            CreationalContext<?> cc = manager.createCreationalContext(bean);
            return type.cast(manager.getReference(bean, type, cc));
        } catch (Throwable t) {
            return null;
        }
    }
}
