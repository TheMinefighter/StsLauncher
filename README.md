[![Java CI with Maven](https://github.com/TheMinefighter/StsLauncher/actions/workflows/maven.yml/badge.svg)](https://github.com/TheMinefighter/StsLauncher/actions/workflows/maven.yml)
# StsLauncher
StsLauncher is an openjdk compatible jnlp (javaws) launcher build specifically to fulfill the needs of stellwerksim.de
# How to use
Get the jar from [the release page](https://github.com/TheMinefighter/StsLauncher/releases/)
Install an openjdk from 8 to 17 (Version codes have to be cleared by STS devs, for whatever reasons).
Download a jnlp file to run.
Run `java -jar PathToStsLauncher.jar PathToJnlp.jnlp`
If you have multiple Java versions on your computer you will have to specify the full path for java.
You can create a shortcut to make this easier. The automatic shortcut creation through javax.jnlp will currently fail.
# Problems
None that I am aware of as of version 1.2.0
# Other stuff
> If I have too much time in the future I might build a CI pipeline for this.
Pretty much done
I know that RCE attacks are possible, if an attacker gains control over the server referenced in the jnlp. The standard jnlp of STS just requests all permissions, therefore there is no security difference, between launching the jnlp in javaws or using this project.
# License
This project is published under the MIT-License.
