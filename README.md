## DEPRECATED - moving this stuff to code-sniff, lots of little utilities rather than a server.

# Korny's Metrics Server

Serves up some code metrics to a D3 server for playing with viz stuff

This will generate cloc files automatically, though you have to edit the source to set root directory!

Checkstyle files have to be inserted into the "samples" directory by hand for now :)

Generating checkstyle:
java -jar checkstyle-6.11.2-all.jar -c configs/metrics-zero.xml -o spring-boot.xml -f xml src


This code is licensed using the Eclipse Public License V 1.0 - see license.txt
