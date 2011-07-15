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

import com.edmunds.autotest.sample.AbstractSimpleBean;
import com.edmunds.autotest.sample.NoConstructor;
import com.edmunds.autotest.sample.ParameterizedConstructor;
import com.edmunds.autotest.sample.PrivateConstructor;
import com.edmunds.autotest.sample.SimpleBean;
import com.edmunds.autotest.sample.SimpleExtension;
import com.edmunds.autotest.sample.Stateless;
import com.edmunds.autotest.sample.sub.SubBean;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Set;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class ValidBeanFilterTest {

    private ClassResolver classResolver;
    private Set<Class> resolvedClasses;
    private ValidBeanFilter validBeanFilter;

    @BeforeMethod
    public void setUp() {
        classResolver = new ClassResolver(
                getClass().getClassLoader(),
                "com.edmunds.autotest.sample");
        resolvedClasses = classResolver.resolveClasses();
        validBeanFilter = new ValidBeanFilter();
    }

    @AfterMethod
    public void tearDown() {
        classResolver = null;
        resolvedClasses = null;
        validBeanFilter = null;
    }

    @Test
    public void testFind() {
        final List<Class> filtered = validBeanFilter.filter(
                resolvedClasses, new AutoTestConfig("com.edmunds.autotest.sample"));

        assertTrue(filtered.contains(SubBean.class));

        // Abstract Classes cannot be instanced hence they caned be tested.
        assertFalse(filtered.contains(AbstractSimpleBean.class));
        assertTrue(filtered.contains(NoConstructor.class));

        // Using a hack to get at the package protected class.
        assertTrue(filtered.contains(Stateless.getPackageBeanClass()));

        // If the only constructor is parameterized we don't know what to pass.
        assertFalse(filtered.contains(ParameterizedConstructor.class));
        assertTrue(filtered.contains(PrivateConstructor.class));
        assertTrue(filtered.contains(SimpleBean.class));
        assertTrue(filtered.contains(SimpleExtension.class));

        // If a class doesn't have any instance variables don't test it.
        assertFalse(filtered.contains(Stateless.class));
    }
}
