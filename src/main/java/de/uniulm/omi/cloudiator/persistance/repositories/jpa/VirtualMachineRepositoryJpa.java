/*
 * Copyright 2017 University of Ulm
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

package de.uniulm.omi.cloudiator.persistance.repositories.jpa;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import de.uniulm.omi.cloudiator.persistance.entities.VirtualMachine;
import de.uniulm.omi.cloudiator.persistance.repositories.VirtualMachineRepository;

import javax.persistence.EntityManager;

/**
 * Created by daniel on 31.10.14.
 */
class VirtualMachineRepositoryJpa extends BaseRemoteResourceRepositoryJpa<VirtualMachine>
    implements VirtualMachineRepository {

    @Inject public VirtualMachineRepositoryJpa(Provider<EntityManager> entityManager,
        TypeLiteral<VirtualMachine> type) {
        super(entityManager, type);
    }
}
