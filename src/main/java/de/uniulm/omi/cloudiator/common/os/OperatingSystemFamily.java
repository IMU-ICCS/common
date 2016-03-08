/*
 * Copyright 2016 University of Ulm
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

package de.uniulm.omi.cloudiator.common.os;

/**
 * Created by daniel on 08.03.16.
 */
public enum OperatingSystemFamily {

    OTHER,

    /**
     * IBM AIX
     */
    AIX,
    /**
     * Arch Linux
     */
    ARCH,
    /**
     * Centos
     */
    CENTOS,
    /**
     * Darwin OS
     */
    DARWIN,
    DEBIAN,
    /**
     * VMWare ESX
     */
    ESX,
    FEDORA,
    FREEBSD,
    GENTOO,
    /**
     * Hewlett Packard Unix
     */
    HPUX,
    COREOS,
    /**
     * Amazon Linux
     */
    AMZN_LINUX,
    MANDRIVA,
    NETBSD,
    /**
     * Oracle Linux OS
     */
    OEL,
    OPENBSD,
    /**
     * Red Hat Enterprise Linux
     */
    RHEL,
    SCIENTIFIC,
    CEL,
    SLACKWARE,
    SOLARIS,
    SUSE,
    TURBOLINUX,
    CLOUD_LINUX,
    UBUNTU,
    WINDOWS;

    private static final OperatingSystemFamily DEFAULT = OTHER;

}
