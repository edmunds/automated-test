/*
 * Copyright 2011 Edmunds.com, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.edmunds.autotest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ClassResolver {

    private final static Log log = LogFactory.getLog(ClassResolver.class);

    private static final String FILTER_PREFIX = "classpath*:";
    private static final String FILTER_POSTFIX = "/**/*";
    private static final String CLASS_POSTFIX = ".class";

    private ClassLoader classLoader;
    private PathMatchingResourcePatternResolver pathResolver;
    private String rootPackageName;

    public ClassResolver(ClassLoader classLoader, String rootPackageName) {
        assert StringUtils.isNotBlank(rootPackageName) : "Root package is blank";

        this.classLoader = classLoader != null ? classLoader : getClass().getClassLoader();
        this.rootPackageName = rootPackageName.replace('.', '/');
        this.pathResolver = new PathMatchingResourcePatternResolver();

        assert packageExists(this.rootPackageName) : "Root Package does not exists: " + rootPackageName;
    }

    private static boolean packageExists(String packageName) {
        // Originally I used Package.getPackage("") but this only works if the package
        // has already been loaded into memory.

        return ClassResolver.class.getClassLoader().getResource(packageName) != null;
    }

    public Set<Class> resolveClasses() {
        Set<Class> classes = new HashSet<Class>();

        resolveClasses(classes);
        return classes;
    }

    public void resolveClasses(Collection<Class> classes) {
        try {
            final String filter = FILTER_PREFIX + this.rootPackageName + FILTER_POSTFIX;

            processResources(pathResolver.getResources(filter), classes);
        } catch(IOException e) {
            String msg = "Failed to get resources for package: " + rootPackageName;
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

    private void processResources(Resource[] resources, Collection<Class> classes) {
        for(Resource resource : resources) {
            processResource(resource, classes);
        }
    }

    private void processResource(Resource resource, Collection<Class> classes) {
        final String uri = getURI(resource);
        if (StringUtils.isBlank(uri)) {
            return;
        }

        int startIdx = uri.lastIndexOf(rootPackageName);
        int endIndex = uri.lastIndexOf(CLASS_POSTFIX);

        if (startIdx > -1 && endIndex > -1) {
            String clsName = uri.substring(startIdx, endIndex).replace('/', '.');
            instantiateClass(clsName, classes);
        }
    }

    private String getURI(Resource resource) {
        try {
            final URI uri = resource.getURI();
            if (uri != null) {
                return uri.toString();
            }

        } catch(IOException e) {
            log.info(e);
        }
        return null;
    }

    private void instantiateClass(String clsName, Collection<Class> classes) {
        try {
            Class cls = classLoader.loadClass(clsName);
            classes.add(cls);
        } catch(ClassNotFoundException e) {
            log.error("Failed to instantiate class: " + clsName, e);
        }
    }
}
