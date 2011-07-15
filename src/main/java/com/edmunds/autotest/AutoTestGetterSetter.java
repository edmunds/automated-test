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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

public class AutoTestGetterSetter {

    private final static Log log = LogFactory.getLog(AutoTestGetterSetter.class);
    private final static String[] GETTER_PREFIXES = {"has", "is", "get"};
    private final static String[] SETTER_PREFIXES = {"set"};

    private final static Map<Class<?>, Object> valueMap = createValueMap();
    private final static Map<Class<?>, Object> defaultValueMap = createDefaultValueMap();

    private final ClassResolver classResolver;
    private final AutoTestConfig config;

    public AutoTestGetterSetter(ClassLoader classLoader, String rootPackage) {
        this.classResolver = new ClassResolver(classLoader, rootPackage);
        this.config = new AutoTestConfig(rootPackage);
    }

    public void setFailOnFieldOverride(boolean failOnFieldOverride) {
        this.config.setFailOnFieldOverride(failOnFieldOverride);
    }

    public void setFailOnBadAssignment(boolean failOnBadAssignment) {
        this.config.setFailOnBadAssignment(failOnBadAssignment);
    }

    public void setValidateMethodsOutsideRootPackage(boolean validateMethodsOutsideRootPackage) {
        this.config.setValidateMethodsOutsideRootPackage(validateMethodsOutsideRootPackage);
    }

    public void validateAll() {
        final ValidBeanFilter validBeanFilter = new ValidBeanFilter();
        final Set<Class> clsSet = classResolver.resolveClasses();
        final List<Class> classes = validBeanFilter.filter(clsSet, config);

        for (Class cls : classes) {
            validate(cls);
        }
    }

    void validate(Class cls) {
        final Collection<Field> fields = ClassUtil.getAllDeclaredFields(cls, config);
        final Collection<Method> methods = ClassUtil.getAllDeclaredMethods(cls);

        final Map<String, Method> getters = getGetters(methods);
        final Map<String, Method> setters = getSetters(methods);

        Object bean = ClassUtil.instanceClass(cls, "Failed to create class : " + cls.getName());

        for (Field field : fields) {
            final String name = field.getName().toLowerCase();

            validateGetter(bean, getters.get(name), field);
            validateSetter(bean, setters.get(name), field);
        }
    }

    private void validateGetter(Object bean, Method method, Field field) {
        if (method == null) {
            return;
        }

        final String errorMsg = "Failed to validate Getter: " +
                bean.getClass().getName() + "." + method.getName();

        final Class<?> fieldType = field.getType();
        final Class<?> returnType = method.getReturnType();

        if (!isTypeSafeAssignment(fieldType, returnType, "getter", errorMsg)) {
            return;
        }

        Object value = createValue(fieldType, errorMsg);
        Object defaultValue = createDefaultValue(fieldType);

        try {
            field.setAccessible(true);
            method.setAccessible(true);

            field.set(bean, value);
            assertEquals(method.invoke(bean), value, errorMsg);

            field.set(bean, defaultValue);
            final Object actualValue = method.invoke(bean);

            if (defaultValue == null && actualValue != null) {
                validateDefaultingGetter(bean, method, field, actualValue);
            } else {
                assertEquals(actualValue, defaultValue, errorMsg);
            }
        } catch (IllegalAccessException e) {
            fail(errorMsg + " : " + e.getMessage());
        } catch (InvocationTargetException e) {
            fail(errorMsg + " : " + e.getMessage());
        }
    }

    /**
     * Some getter methods set a default value when the field is null.
     * <p/>
     * This method checks for that special case.
     *
     * @param bean        the bean being tested.
     * @param method      the getter method being tested.
     * @param field       the field being tested.
     * @param actualValue the actual value returned from the getter method.
     * @throws IllegalAccessException if a problem occurs accessing the field.
     */
    private void validateDefaultingGetter(Object bean, Method method, Field field, Object actualValue) throws IllegalAccessException {
        final String errorMsg = "Failed to validate Getter (Defaulted Value Check): " +
                bean.getClass().getName() + "." + method.getName();

        Object defaultedValue = field.get(bean);
        assertEquals(actualValue, defaultedValue, errorMsg);
    }

    private void validateSetter(Object bean, Method method, Field field) {
        if (method == null) {
            return;
        }

        final String errorMsg = "Failed to validate Setter: " +
                bean.getClass().getName() + "." + method.getName();

        final Class<?>[] params = method.getParameterTypes();
        final Class<?> fieldType = field.getType();

        assertEquals(params.length, 1, errorMsg + " - Setter must take one parameter");

        final Class<?> paramType = params[0];
        if (!isTypeSafeAssignment(paramType, fieldType, "setter", errorMsg)) {
            return;
        }

        Object value = createValue(paramType, errorMsg);
        Object defaultValue = createDefaultValue(paramType);

        try {
            field.setAccessible(true);
            method.setAccessible(true);

            method.invoke(bean, value);
            assertEquals(field.get(bean), value, errorMsg);

            method.invoke(bean, defaultValue);
            assertEquals(field.get(bean), defaultValue, errorMsg);
        } catch (IllegalAccessException e) {
            fail(errorMsg + " : " + e.getMessage());
        } catch (InvocationTargetException e) {
            fail(errorMsg + " : " + e.getMessage());
        }
    }

    private Map<String, Method> getGetters(Collection<Method> methods) {
        return mapPrefixedMethods(methods, GETTER_PREFIXES, 0);
    }

    private Map<String, Method> getSetters(Collection<Method> methods) {
        return mapPrefixedMethods(methods, SETTER_PREFIXES, 1);
    }

    private Map<String, Method> mapPrefixedMethods(Collection<Method> methods, String[] prefixes, int paramCount) {
        Map<String, Method> result = new HashMap<String, Method>();

        for (Method method : methods) {
            if (method.getParameterTypes().length == paramCount &&
                    methodDeclaredUnderRootPackage(method)) {
                String baseName = getBaseName(method.getName().toLowerCase(), prefixes);

                if (baseName != null) {
                    result.put(baseName, method);
                }
            }
        }
        return result;
    }

    private boolean methodDeclaredUnderRootPackage(Method method) {
        if (config.isValidateMethodsOutsideRootPackage()) {
            return true;
        }

        return ClassUtil.isDeclaredUnderRootPackage(config, method);
    }

    private String getBaseName(String name, String[] prefixes) {
        for (String prefix : prefixes) {
            if (name.startsWith(prefix)) {
                return StringUtils.substringAfter(name, prefix);
            }
        }
        return null;
    }

    private Object createValue(Class<?> type, String errorMsg) {
        Object value = valueMap.get(type);

        if (value == null && !type.isPrimitive()) {
            value = ClassUtil.instanceClass(type, errorMsg);
        }

        return value;
    }

    private static Map<Class<?>, Object> createValueMap() {
        Map<Class<?>, Object> valueMap = new HashMap<Class<?>, Object>();

        valueMap.put(byte.class, new Byte((byte) 40));
        valueMap.put(short.class, new Short((short) 41));
        valueMap.put(int.class, new Integer(42));
        valueMap.put(long.class, new Long(43));
        valueMap.put(float.class, new Float(44));
        valueMap.put(double.class, new Double(45));
        valueMap.put(boolean.class, Boolean.TRUE);
        valueMap.put(char.class, new Character((char) 46));

        valueMap.put(Byte.class, new Byte((byte) 40));
        valueMap.put(Short.class, new Short((short) 41));
        valueMap.put(Integer.class, new Integer(42));
        valueMap.put(Long.class, new Long(43));
        valueMap.put(Float.class, new Float(44));
        valueMap.put(Double.class, new Double(45));
        valueMap.put(Boolean.class, Boolean.TRUE);
        valueMap.put(Character.class, new Character((char) 46));

        return valueMap;
    }

    private Object createDefaultValue(Class<?> type) {
        return defaultValueMap.get(type);
    }

    private static Map<Class<?>, Object> createDefaultValueMap() {
        Map<Class<?>, Object> valueMap = new HashMap<Class<?>, Object>();

        valueMap.put(byte.class, new Byte((byte) 0));
        valueMap.put(short.class, new Short((short) 0));
        valueMap.put(int.class, new Integer(0));
        valueMap.put(long.class, new Long(0));
        valueMap.put(float.class, new Float(0));
        valueMap.put(double.class, new Double(0));
        valueMap.put(boolean.class, Boolean.FALSE);
        valueMap.put(char.class, new Character((char) 0));

        valueMap.put(Byte.class, new Byte((byte) 0));
        valueMap.put(Short.class, new Short((short) 0));
        valueMap.put(Integer.class, new Integer(0));
        valueMap.put(Long.class, new Long(0));
        valueMap.put(Float.class, new Float(0));
        valueMap.put(Double.class, new Double(0));
        valueMap.put(Boolean.class, Boolean.FALSE);
        valueMap.put(Character.class, new Character((char) 0));

        return valueMap;
    }

    private boolean isTypeSafeAssignment(Class<?> sourceType, Class<?> targetType, String methodType, String errorMsg) {
        if (targetType.isAssignableFrom(sourceType)) {
            // All ok
            return true;
        }

        String msg = errorMsg + " variable and " +
                methodType + " have different types (" +
                sourceType.getSimpleName() + " -> " +
                targetType.getSimpleName() + "): ";
        log.warn(msg);

        if (config.isFailOnBadAssignment()) {
            fail(msg);
        }

        return false;
    }
}

