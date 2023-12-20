# foaf

These files are currently a (problematic but indicative) 2015 reflection of the main FOAF project Subversion repository into Github.

Historically we had two repos: foaf/ which was the core specs, and foaftown/ which was a more random scratchpad.

We aim to ultimately treat this git repo as primary, and the svn as either frozen or (if someone does the work) something that can be sync'd with latest state of git master branch. 

As of late 2023, these repos should not be considered authoritative. We are still slowly working through a screwup dating from a rushed cleanup in 2022. The problem stems from having files in AWS Cloud including a collection of snapshots and volumes, where important data (the Subversion database with filesystem histories) ended up being recorded only in AWS EC2 snapshots. Unfortunately a migration out to GCP hosting (long story short) made the situation worse rather than better. As a stopgap the currently serving sites are slightly rolled back to the versions recorded in this repository. libbymiller and danbri are working to improve the situation, and to get us to a more modern environment where sysadmin chores can more easily be shared amongst collaborators. 
