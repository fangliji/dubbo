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

import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.dubbo.config.ReferenceConfigBase;
import org.apache.dubbo.config.ServiceConfigBase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Service repository for module
 */
public class ModuleServiceRepository {

    private ModuleModel moduleModel;

    // services
    private ConcurrentMap<String, ServiceDescriptor> services = new ConcurrentHashMap<>();

    // consumers
    private ConcurrentMap<String, ConsumerModel> consumers = new ConcurrentHashMap<>();

    // providers
    private ConcurrentMap<String, ProviderModel> providers = new ConcurrentHashMap<>();
    private final FrameworkServiceRepository frameworkServiceRepository;

    public ModuleServiceRepository(ModuleModel moduleModel) {
        this.moduleModel = moduleModel;
        frameworkServiceRepository = ScopeModelUtil.getFrameworkModel(moduleModel).getServiceRepository();
    }

    public ModuleModel getModuleModel() {
        return moduleModel;
    }

    public void registerConsumer(String serviceKey,
                                 ServiceDescriptor serviceDescriptor,
                                 ReferenceConfigBase<?> rc,
                                 Object proxy,
                                 ServiceMetadata serviceMetadata) {
        ConsumerModel consumerModel = new ConsumerModel(serviceMetadata.getServiceKey(), proxy, serviceDescriptor, rc,
            serviceMetadata, null);
        this.registerConsumer(consumerModel);
    }

    public void registerConsumer(ConsumerModel consumerModel) {
        consumers.putIfAbsent(consumerModel.getServiceKey(), consumerModel);
    }

    public void registerProvider(String serviceKey,
                                 Object serviceInstance,
                                 ServiceDescriptor serviceModel,
                                 ServiceConfigBase<?> serviceConfig,
                                 ServiceMetadata serviceMetadata) {
        ProviderModel providerModel = new ProviderModel(serviceKey, serviceInstance, serviceModel,
            serviceConfig, serviceMetadata);
        this.registerProvider(providerModel);
    }

    public void registerProvider(ProviderModel providerModel) {
        providers.putIfAbsent(providerModel.getServiceKey(), providerModel);
        frameworkServiceRepository.registerProvider(providerModel);
    }

    public ServiceDescriptor registerService(Class<?> interfaceClazz) {
        return services.computeIfAbsent(interfaceClazz.getName(),
            _k -> new ServiceDescriptor(interfaceClazz));
    }

    /**
     * See {@link #registerService(Class)}
     * <p>
     * we assume:
     * 1. services with different interfaces are not allowed to have the same path.
     * 2. services share the same interface but has different group/version can share the same path.
     * 3. path's default value is the name of the interface.
     *
     * @param path
     * @param interfaceClass
     * @return
     */
    public ServiceDescriptor registerService(String path, Class<?> interfaceClass) {
        ServiceDescriptor serviceDescriptor = registerService(interfaceClass);
        // if path is different with interface name, add extra path mapping
        if (!interfaceClass.getName().equals(path)) {
            services.putIfAbsent(path, serviceDescriptor);
        }
        return serviceDescriptor;
    }


    public void unregisterService(Class<?> interfaceClazz) {
        unregisterService(interfaceClazz.getName());
    }

    public void unregisterService(String path) {
        services.remove(path);
    }

    public List<ServiceDescriptor> getAllServices() {
        return Collections.unmodifiableList(new ArrayList<>(services.values()));
    }

    public ServiceDescriptor lookupService(String interfaceName) {
        return services.get(interfaceName);
    }

    public MethodDescriptor lookupMethod(String interfaceName, String methodName) {
        ServiceDescriptor serviceDescriptor = lookupService(interfaceName);
        if (serviceDescriptor == null) {
            return null;
        }

        List<MethodDescriptor> methods = serviceDescriptor.getMethods(methodName);
        if (CollectionUtils.isEmpty(methods)) {
            return null;
        }
        return methods.iterator().next();
    }

    public List<ProviderModel> getExportedServices() {
        return Collections.unmodifiableList(new ArrayList<>(providers.values()));
    }

    public ProviderModel lookupExportedService(String serviceKey) {
        return providers.get(serviceKey);
    }

    public List<ConsumerModel> getReferredServices() {
        return Collections.unmodifiableList(new ArrayList<>(consumers.values()));
    }

    public ConsumerModel lookupReferredService(String serviceKey) {
        return consumers.get(serviceKey);
    }

    public void destroy() {
        for (ProviderModel providerModel : providers.values()) {
            frameworkServiceRepository.unregisterProvider(providerModel);
        }
    }
}
