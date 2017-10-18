# v0.2 (10/20/2017)
# Release Notes

## Notable Changes
The Barcelona Release (v 0.2) of the SNMP micro service includes the following:
* POM changes for appropriate repository information for distribution/repos management, checkstyle plugins, etc.
* Removed all references to unfinished DeviceManager work as part of Dell Fuse
* Added Dockerfile for creation of micro service targeted for ARM64
* Consolidated Docker properties files to common directory

## Bug Fixes
* Fixed Consul configuration properties
* Fixed Device equality logic
* Added check for service existence after initialization to Base Service

 - [#13](https://github.com/edgexfoundry/device-snmp/pull/13) - Remove staging plugin contributed by Jeremy Phelps ([JPWKU](https://github.com/JPWKU))
 - [#12](https://github.com/edgexfoundry/device-snmp/pull/12) - Adds null check in BaseService contributed by Tyler Cox ([trcox](https://github.com/trcox))
 - [#11](https://github.com/edgexfoundry/device-snmp/pull/11) - Fixes Maven artifact dependency path contributed by Tyler Cox ([trcox](https://github.com/trcox))
 - [#10](https://github.com/edgexfoundry/device-snmp/pull/10) - added staging and snapshots repos to pom along with nexus staging mav… contributed by Jim White ([jpwhitemn](https://github.com/jpwhitemn))
 - [#9](https://github.com/edgexfoundry/device-snmp/pull/9) - removed device manager from URLs contributed by Jim White ([jpwhitemn](https://github.com/jpwhitemn))
 - [#8](https://github.com/edgexfoundry/device-snmp/pull/8) - Added support for aarch64 arch contributed by ([feclare](https://github.com/feclare))
 - [#7](https://github.com/edgexfoundry/device-snmp/pull/7) - Fixes device comparison logic contributed by Tyler Cox ([trcox](https://github.com/trcox))
 - [#6](https://github.com/edgexfoundry/device-snmp/pull/6) - Consolidates Docker properties files contributed by Tyler Cox ([trcox](https://github.com/trcox))
 - [#5](https://github.com/edgexfoundry/device-snmp/pull/5) - Fixes Consul Properties contributed by Tyler Cox ([trcox](https://github.com/trcox))
 - [#4](https://github.com/edgexfoundry/device-snmp/pull/4) - Adds Docker build capability contributed by Tyler Cox ([trcox](https://github.com/trcox))
 - [#3](https://github.com/edgexfoundry/device-snmp/pull/3) - Add distributionManagement for artifact storage contributed by Tyler Cox ([trcox](https://github.com/trcox))
 - [#2](https://github.com/edgexfoundry/device-snmp/pull/2) - fix change of packaging for schedule clients contributed by Jim White ([jpwhitemn](https://github.com/jpwhitemn))
 - [#1](https://github.com/edgexfoundry/device-snmp/pull/1) - Contributed Project Fuse source code contributed by Tyler Cox ([trcox](https://github.com/trcox))
