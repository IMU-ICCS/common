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

package de.uniulm.omi.cloudiator.domain;

import com.google.common.base.MoreObjects;

import javax.annotation.Nullable;

/**
 * Created by daniel on 18.08.15.
 */
public class CredentialsBuilder {

    @Nullable private String user;
    @Nullable private String password;

    private CredentialsBuilder() {

    }

    private CredentialsBuilder(Credentials credentials) {
        this.user = credentials.user();
        this.password = credentials.password();
    }

    public static CredentialsBuilder newBuilder() {
        return new CredentialsBuilder();
    }

    public static CredentialsBuilder of(Credentials credentials) {
        return new CredentialsBuilder(credentials);
    }


    public CredentialsBuilder user(String user) {
        this.user = user;
        return this;
    }

    public CredentialsBuilder password(String password) {
        this.password = password;
        return this;
    }

    public Credentials build() {
        return new CredentialsImpl(user, password);
    }

    @Override public String toString() {
        return MoreObjects.toStringHelper(this).toString();
    }


}
