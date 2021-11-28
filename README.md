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
# How it works / Developers Guide
Starting v1.2 the only thing this program does is downloading stuff and starting a java commandline based on the jnlp. Nothing less and nothing more.
## Structure
The de.theminefighter.stslauncher-package contains the core of the project including the main class.
The de.theminefighter.stslauncher.caching-package contains the caching infrastructure for improved load times.
The javax.jnlp-package contains a basic mock of some jnlp functionality to prevent STS from showing graphical errors. It has no real functionality.
## Launch procedure
First of all the program arguments (jnlp file and show-license-files option) will be read.
Then the jnlp file will be parsed and the jars referenced in it will be cached.
If executed on JREs > 1.8 a list of maven packages in src/main/resources/de/theminefighter/stslauncher/MavenList.csv will be cached an loaded into the new classpath.
If the --show-license-files option was used, all jar archives will be loaded and any license files in them will be printed to console.
Then the arguments of the new JVM to be launched are being build, based on all of this.
In the end the new JVM process will be launched.
## CI 
This project has tests using junit. Most of them are automatically executed with any merge/push to master (one of them can;t because it tests the full launch procedure of STS, which is not possible on a system w/o GUI support).
This is realized with github actions.

Build are also done using maven package, although not (yet) automated.