# android-drawable-resources-cleaner
This project gives a method to clean unused drawable resources in android as to reduce the apk size, it's suitable for single android project as well as multi-android projects.

    This tool is suitable for multi Android projects to find out unused resources.
    The tool now only filter out drawable resources as this it's the biggest size between unused resources, and only delete file resources with format *.png or *.jpg. 
    It also can find about other 11 types of unused resources by a simple modify with the code in Processor.getResourceList(). 
    You can easily delete the unused file resources (*.xml, *.png, *.jpg and etc) by one click.
    The other types of resources you have to delete them according to the displayed paths.
