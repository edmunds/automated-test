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

import com.edmunds.autotest.sample.SimpleBean;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Set;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class ClassResolverTest {

    private ClassResolver classResolver;

    @BeforeMethod
    public void setUp() {
        classResolver = new ClassResolver(
                getClass().getClassLoader(),
                "com.edmunds.autotest.sample");
    }

    @Test
    public void testResolve() {
        final Set<Class> classes = classResolver.resolveClasses();

        assertNotNull(classes);
        assertTrue(classes.contains(SimpleBean.class));
    }
}
