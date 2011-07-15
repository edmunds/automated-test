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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class AutoTestConfig {
    private final String[] DEFAULT_FIELD_OVERRIDE_EXCEPTIONS = {"serialVersionUID", "log", "LOG", "JiBX_bindingList"};

    private final String rootPackage;
    private boolean failOnBadAssignment;
    private boolean validateMethodsOutsideRootPackage;
    private boolean failOnFieldOverride;
    private Set<String> fieldOverrideExceptions;

    public AutoTestConfig(String rootPackage) {
        this.rootPackage = rootPackage;

        this.failOnBadAssignment = true;
        this.failOnFieldOverride = true;

        this.validateMethodsOutsideRootPackage = false;

        this.fieldOverrideExceptions = new HashSet<String>();

        Collections.addAll(this.fieldOverrideExceptions,
                DEFAULT_FIELD_OVERRIDE_EXCEPTIONS);
    }

    public String getRootPackage() {
        return rootPackage;
    }

    public boolean isFailOnBadAssignment() {
        return failOnBadAssignment;
    }

    public void setFailOnBadAssignment(boolean failOnBadAssignment) {
        this.failOnBadAssignment = failOnBadAssignment;
    }

    public boolean isValidateMethodsOutsideRootPackage() {
        return validateMethodsOutsideRootPackage;
    }

    public void setValidateMethodsOutsideRootPackage(boolean validateMethodsOutsideRootPackage) {
        this.validateMethodsOutsideRootPackage = validateMethodsOutsideRootPackage;
    }

    public boolean isFailOnFieldOverride() {
        return failOnFieldOverride;
    }

    public void setFailOnFieldOverride(boolean failOnFieldOverride) {
        this.failOnFieldOverride = failOnFieldOverride;
    }

    public Set<String> getFieldOverrideExceptions() {
        return fieldOverrideExceptions;
    }

    public void setFieldOverrideExceptions(Set<String> fieldOverrideExceptions) {
        this.fieldOverrideExceptions = fieldOverrideExceptions;
    }
}
