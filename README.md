# d3sample

This will generate cloc files automatically, though you have to edit the source to set root directory!

Checkstyle files have to be inserted into the "samples" directory by hand for now :)

Generating checkstyle:
java -jar checkstyle-6.11.2-all.jar -c configs/metrics-zero.xml -o spring-boot.xml -f xml src



