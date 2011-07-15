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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ValidBeanFilter {
    public List<Class> filter(Collection<Class> classes, AutoTestConfig config) {
        List<Class> results = new ArrayList<Class>();

        for(Class cls : classes) {
            if (ClassUtil.isStandardClass(cls)) {
                if (isCandidate(cls, config)) {
                    results.add(cls);
                }
            }
        }
        return results;
    }

    private boolean isCandidate(Class cls, AutoTestConfig config) {
        boolean hasState = !ClassUtil.getAllDeclaredFields(cls, config).isEmpty();

        return hasState && ClassUtil.hasDefaultConstructor(cls);
    }
}
