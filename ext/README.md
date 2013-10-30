# Extra Modules

This directory is a space to link to pending modules that aren't yet part of 
the main jeo project. To setup:

* symlink to the module directory
* invoke builds with the various profile

For example:

    ln -sf ../../jeo-android android
    cd ..
    mvn -o -P ext,android eclipse:eclipse
    
