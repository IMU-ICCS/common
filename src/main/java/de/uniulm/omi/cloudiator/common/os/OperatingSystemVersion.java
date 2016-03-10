/*
 *  Copyright 2016 University of Ulm
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package de.uniulm.omi.cloudiator.common.os;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by daniel on 08.03.16.
 */
public class OperatingSystemVersion {

    @Nullable private final String version;

    private OperatingSystemVersion(@Nullable String version) {
        this.version = version;
    }

    public static OperatingSystemVersion of(String version) {
        checkNotNull(version);
        checkArgument(!version.isEmpty());
        return new OperatingSystemVersion(version);
    }

    public static OperatingSystemVersion unknown() {
        return new OperatingSystemVersion(null);
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof OperatingSystemVersion))
            return false;

        OperatingSystemVersion that = (OperatingSystemVersion) o;

        return version != null ? version.equals(that.version) : that.version == null;

    }

    @Override public int hashCode() {
        return version != null ? version.hashCode() : 0;
    }

    @Override public String toString() {
        return version;
    }
}