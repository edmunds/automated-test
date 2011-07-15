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

import com.edmunds.autotest.badsample.BadGetterAssignment;
import com.edmunds.autotest.badsample.BadIntGetter;
import com.edmunds.autotest.badsample.BadIntSetter;
import com.edmunds.autotest.badsample.BadSetterAssignment;
import com.edmunds.autotest.sample.NoConstructor;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

public class AutoTestGetterSetterTest {

    private AutoTestGetterSetter autoTestGetterSetter;

    @BeforeMethod
    public void setUp() {
        autoTestGetterSetter = new AutoTestGetterSetter(
                getClass().getClassLoader(),
                "com.edmunds.autotest.sample");
    }

    @AfterMethod
    public void tearDown() {
        autoTestGetterSetter = null;
    }

    @Test
    public void testValidateAll() {
        autoTestGetterSetter.validateAll();
    }

    @Test
    public void testValidateClass() {
        autoTestGetterSetter.validate(NoConstructor.class);
    }

    @Test
    public void testBadGetterAssignment() {
        boolean exceptionThrown = true;

        autoTestGetterSetter.setValidateMethodsOutsideRootPackage(true);

        try {
            autoTestGetterSetter.validate(BadGetterAssignment.class);
            exceptionThrown = false;
        } catch(AssertionError e) {
            assertEquals(e.getMessage(), "Failed to validate Getter: com.edmunds.autotest.badsample.BadGetterAssignment.getData variable and getter have different types (int -> String): ");
        }

        if (!exceptionThrown) {
            fail("Bad case should have thrown an exception");
        }
    }

    @Test
    public void testBadSetterAssignment() {
        boolean exceptionThrown = true;

        autoTestGetterSetter.setValidateMethodsOutsideRootPackage(true);

        try {
            autoTestGetterSetter.validate(BadSetterAssignment.class);
            exceptionThrown = false;
        } catch(AssertionError e) {
            assertEquals(e.getMessage(), "Failed to validate Setter: com.edmunds.autotest.badsample.BadSetterAssignment.setData variable and setter have different types (String -> int): ");
        }

        if (!exceptionThrown) {
            fail("Bad case should have thrown an exception");
        }
    }

    @Test
    public void testBadIntGetter() {
        boolean exceptionThrown = true;

        autoTestGetterSetter.setValidateMethodsOutsideRootPackage(true);

        try {
            autoTestGetterSetter.validate(BadIntGetter.class);
            exceptionThrown = false;
        } catch(AssertionError e) {
            assertEquals(e.getMessage(), "Failed to validate Getter: com.edmunds.autotest.badsample.BadIntGetter.getData expected:<42> but was:<0>");
        }

        if (!exceptionThrown) {
            fail("Bad case should have thrown an exception");
        }
    }

    @Test
    public void testBadIntSetter() {
        boolean exceptionThrown = true;

        autoTestGetterSetter.setValidateMethodsOutsideRootPackage(true);

        try {
            autoTestGetterSetter.validate(BadIntSetter.class);
            exceptionThrown = false;
        } catch(AssertionError e) {
            assertEquals(e.getMessage(), "Failed to validate Setter: com.edmunds.autotest.badsample.BadIntSetter.setData expected:<42> but was:<0>");
        }

        if (!exceptionThrown) {
            fail("Bad case should have thrown an exception");
        }
    }
}
