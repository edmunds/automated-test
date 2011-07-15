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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.fail;

public final class ClassUtil {
    private static final Log log = LogFactory.getLog(ClassUtil.class);
    private static final InvocationHandler NOOP_HANDLER = new NoOpInvocationHandler();

    private ClassUtil() {
    }


    public static Collection<Field> getAllDeclaredFields(Class cls, AutoTestConfig config) {
        return getAllDeclaredFieldsMap(cls, false, config).values();
    }

    public static Map<String, Field> getAllDeclaredFieldsMap(Class originalCls, boolean lowercase, AutoTestConfig config) {
        Map<String, Field> fields = new HashMap<String, Field>();

        Class cls = originalCls;

        do {
            for(Field field : cls.getDeclaredFields()) {
                String fieldName = field.getName();

                if (lowercase) {
                    fieldName = fieldName.toLowerCase();
                }

                if (fields.containsKey(fieldName)) {

                    if (isDeclaredUnderRootPackage(config, field) &&
                            !config.getFieldOverrideExceptions().contains(field.getName())) {

                        String msg = "Instance variable (" + field.getName() +
                                ") has been overridden: " + originalCls.getName();
                        log.warn(msg);

                        if (config.isFailOnFieldOverride()) {
                            fail(msg);
                        }
                    }
                } else {
                    fields.put(fieldName, field);
                }
            }
            cls = cls.getSuperclass();
        } while(cls != null);
        return fields;
    }

    public static Collection<Method> getAllDeclaredMethods(Class cls) {
        List<Method> methods = new ArrayList<Method>();

        do {
            Collections.addAll(methods, cls.getDeclaredMethods());
            cls = cls.getSuperclass();
        } while(cls != null);

        return methods;
    }

    public static Constructor getDefaultConstructor(Class cls) {
        final Constructor[] constructors = cls.getDeclaredConstructors();

        for(Constructor constructor : constructors) {
            if (constructor.getParameterTypes().length == 0) {
                return constructor;
            }
        }

        return null;
    }

    public static boolean hasDefaultConstructor(Class cls) {
        boolean defaultConstructor = false;

        final Constructor[] constructors = cls.getDeclaredConstructors();

        for(Constructor constructor : constructors) {
            if (constructor.getParameterTypes().length == 0) {
                defaultConstructor = true;
                break;
            }
        }
        return defaultConstructor;
    }

    public static boolean isStandardClass(Class cls) {
        return !(
                ((cls.getModifiers() & Modifier.ABSTRACT) > 0) ||
                        cls.isInterface() ||
                        cls.isMemberClass() ||
                        cls.isLocalClass() ||
                        cls.isSynthetic());
    }

    public static Object instanceClass(Class cls, String msg) {
        if (cls.isInterface()) {
            return Proxy.newProxyInstance(cls.getClassLoader(), new Class[]{cls}, NOOP_HANDLER);
        } else if (isStandardClass(cls) && hasDefaultConstructor(cls)) {
            try {
                Constructor constructor = getDefaultConstructor(cls);
                constructor.setAccessible(true);

                return constructor.newInstance();

            } catch(InvocationTargetException e) {
                log.error(msg, e);
                throw new RuntimeException(msg, e);
            } catch(InstantiationException e) {
                log.error(msg, e);
                throw new RuntimeException(msg, e);
            } catch(IllegalAccessException e) {
                log.error(msg, e);
                throw new RuntimeException(msg, e);
            }
        }
        return null;
    }

    public static boolean isDeclaredUnderRootPackage(AutoTestConfig config, Method method) {
        return isClassUnderRootPackage(config, method.getDeclaringClass());
    }

    public static boolean isDeclaredUnderRootPackage(AutoTestConfig config, Field field) {
        return isClassUnderRootPackage(config, field.getDeclaringClass());
    }

    private static boolean isClassUnderRootPackage(AutoTestConfig config, Class<?> cls) {
        return cls.getPackage().getName().startsWith(config.getRootPackage());
    }
}
