[![Java CI with Maven](https://github.com/TheMinefighter/StsLauncher/actions/workflows/maven.yml/badge.svg)](https://github.com/TheMinefighter/StsLauncher/actions/workflows/maven.yml)
# StsLauncher
StsLauncher is an openjdk compatible jnlp (javaws) launcher build specifically to fulfill the needs of stellwerksim.de
# How to use
Get the jar from [the release page](https://github.com/TheMinefighter/StsLauncher/releases/)
Install an openjdk from 8 to 21 (Version codes have to be cleared by STS devs, for whatever reasons).
Download a jnlp file to run.
Run `java -jar PathToStsLauncher.jar PathToJnlp.jnlp`
If you have multiple Java versions on your computer you may have to specify the full path for java.

A shortcut for this will be created at the first launch
(if the jnlp requests it, sts does so; under linux only)

Files are stored in a subfolder called STSLauncher in your `userdir`
## Security and goals of the project
Except allowing accept, listen and resolve on port 1024+on localhost, your standard java security policy will be used. 
(SEE DEFAULT_POLICY in JnlpLauncher class, if you need something else edit `xsMRPV5vhtNOOHLq7vcd55ZokCJfMUduc1oMsTp7t2k` in your cache)

Further permission management as envisioned by the jnlp spec is not implemented.
The standard jnlp of STS just requests all permissions, therefore there is no security deficit, when using this project over javaws in this case.

It is hypothetically possible to implement the security specified by the JNLP spec on top of openjdk.
You can submit a merge request for that, I do not plan to do that.

There is no validation of jar signatures implmented as of now, and I do not plan to change that, due to the fact that any attacker which is able to manipulate the cache of sts-launcher would be able to manipulate its desktop shortcut as well. Also Stellwerksim jars are not correctly signed anyways.
# License
This project is published under the MIT-License.
This does not apply to all test resources (STSlauncher jnlp file), nor to all libaries used at runtime.
Build results are distributed under MIT license, as they only use MIT licensed code.
License files of libraries can be displayed using the `--show-license-files` argument.
# How it works / Developers Guide
Starting v1.2 the program is mainly downloading dependencies of the jnlp and starting a java process based on the jnlp.
## Structure
The `de.theminefighter.stslauncher`-package contains the core of the project including the main class.
The `de.theminefighter.stslauncher.caching`-package contains the caching infrastructure for improved load times.

The `javax.jnlp`-package contains mostly a basic mock of some jnlp functionality to prevent STS from showing graphical errors.
For linux it implements the IntegrationService interface to provide shortcut functionality.
## Launch procedure
First of all the program arguments (jnlp file and possibly `--show-license-files`-option) will be read.
Then the jnlp file will be parsed and the jars referenced in it will be cached.
If executed on JREs > 1.8 a list of maven packages in `src/main/resources/de/theminefighter/stslauncher/MavenList.csv` will be cached an loaded into the new classpath.

If the `--show-license-files` option was used,
all jar archives will be loaded and any license files in them will be printed to console.
Then the arguments of the new JVM to be launched are being build, based on all of this.
In the end the new JVM process will be launched with the launched class being the main class specified in the jnlp.
## CI 
This project has tests using junit.
Most of them are automatically executed with any merge/push to master. 

(two of them can't because it tests the full launch procedure of STS, which is not possible on a system w/o GUI support)
This is realized using github actions.

Builds are also done using maven package, although not (yet) automated.
