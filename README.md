# Show-Your-Work
First you have to get SDKs set up. https://www.jetbrains.org/intellij/sdk/docs/basics/getting_started/setting_up_environment.html has some guidelines. Still trying to work out the details. 

To build and run the plugin, to to the drop down menu beside the "Build Project" button in the upper right corner. Press Edit Configuration, and then use the + button to add a new "Plugin" configuration. And then choose the classname. After that you should be able to successfully run the plugin. This will trigger a new window, where you can test the plugin.

Currently the 'Start Tracking' option that initializes the tracking is located under VCS->Local History. Clicking it will start the csv file writing task.

The time data field has to be added. 

Currently the biggest problem seems to be with find and replace. Otherwise, it works very well with the 'scanner.java' class. 

