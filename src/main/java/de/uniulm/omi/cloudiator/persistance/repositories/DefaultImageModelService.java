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

package de.uniulm.omi.cloudiator.persistance.repositories;

import com.google.inject.Inject;

import de.uniulm.omi.cloudiator.persistance.entities.Image;

/**
 * Created by daniel on 03.11.14.
 */
@Deprecated public class DefaultImageModelService extends BaseRemoteModelService<Image>
    implements ImageModelService {

    @Inject public DefaultImageModelService(RemoteResourceRepository<Image> imageRepository) {
        super(imageRepository);
    }

}
