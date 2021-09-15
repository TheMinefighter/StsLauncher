# StsLauncher
StsLauncher is an openjdk compatible jnlp (javaws) launcher build specifically to fulfill the needs of stellwerksim.de
# How to use
Get the jar from [the release page](https://github.com/TheMinefighter/StsLauncher/releases/)
Install openjdk 8.
Download a jnlp file to run.
Run `java -jar PathToStsLauncher.jar PathToJnlp.jnlp`
If you have multiple Java versions on your computer you will have to specify the full path for java.
You can create a shortcut to make this easier. The automatic shortcut creation through javax.jnlp will currently fail.
# Problems
Currently it only works with java 8, but that should change in the future.
If you start STS you currently get an error message from STS, but the program starts anyway. You can safely ignore that error. I am planning to fix that anyway.

# Other stuff
If I have too much time in the future I might build a CI pipeline for this.

I know that RCE attacks are possible, if an attacker gains control over the serve referenced in the jnlp. The standard jnlp of STS just requests all permissions, therefore there is no security difference, between launching the jnlp in javaws or using this project.
