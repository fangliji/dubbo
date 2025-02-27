/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.dubbo.rpc.model;

import org.apache.dubbo.common.extension.ExtensionScope;

/**
 * Model of a service module
 */
public class ModuleModel extends ScopeModel {

    private String id;
    private final ApplicationModel applicationModel;
    private ModuleServiceRepository serviceRepository;

    public ModuleModel(ApplicationModel applicationModel) {
        super(applicationModel, ExtensionScope.MODULE);
        this.applicationModel = applicationModel;
        applicationModel.addModule(this);
        initialize();
    }

    protected void initialize() {
        super.initialize();
        this.serviceRepository = new ModuleServiceRepository(this);
        postProcessAfterCreated();
    }

    @Override
    public void destroy() {
        super.destroy();
        // TODO destroy module resources
        applicationModel.removeModule(this);
    }

    public ApplicationModel getApplicationModel() {
        return applicationModel;
    }

    public ModuleServiceRepository getServiceRepository() {
        return serviceRepository;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "ModuleModel";
    }
}
