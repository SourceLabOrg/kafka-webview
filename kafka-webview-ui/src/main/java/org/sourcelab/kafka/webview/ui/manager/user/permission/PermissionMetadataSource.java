/**
 * MIT License
 *
 * Copyright (c) 2017, 2018, 2019 SourceLab.org (https://github.com/SourceLabOrg/kafka-webview/)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.sourcelab.kafka.webview.ui.manager.user.permission;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.method.AbstractMethodSecurityMetadataSource;
import org.springframework.util.ClassUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * This implementation allows for pulling required security roles for a method tagged with @Secured or @RequiresPermission.
 */
public class PermissionMetadataSource extends AbstractMethodSecurityMetadataSource {
    @Override
    public Collection<ConfigAttribute> getAttributes(Method method, Class<?> targetClass) {
        // Look for anotation.
        final RequirePermission annotation = findAnnotation(method, targetClass, RequirePermission.class);
        if (annotation != null) {
            final List<PermissionAttribute> attributes = new ArrayList<>();
            for (Permissions permission : annotation.value()) {
                attributes.add(new PermissionAttribute(permission));
            }
            return Collections.unmodifiableCollection(attributes);
        }
        return Collections.emptyList();
    }

    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        return null;
    }

    /**
     * See
     * {@link org.springframework.security.access.method.AbstractFallbackMethodSecurityMetadataSource#getAttributes(Method, Class)}
     * for the logic of this method. The ordering here is slightly different in that we
     * consider method-specific annotations on an interface before class-level ones.
     */
    private <A extends Annotation> A findAnnotation(Method method, Class<?> targetClass, Class<A> annotationClass) {
        // The method may be on an interface, but we need attributes from the target
        // class.
        // If the target class is null, the method will be unchanged.
        Method specificMethod = ClassUtils.getMostSpecificMethod(method, targetClass);
        A annotation = AnnotationUtils.findAnnotation(specificMethod, annotationClass);

        if (annotation != null) {
            logger.debug(annotation + " found on specific method: " + specificMethod);
            return annotation;
        }

        // Check the original (e.g. interface) method
        if (specificMethod != method) {
            annotation = AnnotationUtils.findAnnotation(method, annotationClass);

            if (annotation != null) {
                logger.debug(annotation + " found on: " + method);
                return annotation;
            }
        }

        // Check the class-level (note declaringClass, not targetClass, which may not
        // actually implement the method)
        annotation = AnnotationUtils.findAnnotation(specificMethod.getDeclaringClass(),
            annotationClass);

        if (annotation != null) {
            logger.debug(annotation + " found on: " + specificMethod.getDeclaringClass().getName());
            return annotation;
        }

        return null;
    }
}
