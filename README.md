# Show-Your-Work
This plugin allows users to log their coding activity.

To use the plugin, download the 'Show Your Work.jar' file, open a Pycharm Project, go to File->Settings->Plugins->Install from Disk and then select the .jar file and then restart.

The plugin tools can be found under the VCS menu in the menu bar, under the name 'Show Your Work Plugin Tools'
![Screenshot of IDE with plugin installed](locationOfPluginTools.png)

Once an editor tab is opened with a .py file, the 'Start Logging Edits' button creates or updates a CSV file named '<filename>.csv' located in the same directory as the original file. The button disappears after the first press, as to avoid multiple logging of the same event.<br />

The 'Generate Zip for Submission' button creates a zip file named '<filename>_log.zip' in the current directory, which contains a text version of the original file, as well as the CSV log file. It only appears if the file is .py that has a log running.<br />

The 'Generate Original from Log' button generates a txt file that uses the CSV log file to replicate the original into a file named '<filename>_currentVersion.txt', also located in the same directory as the original. Same as the 'Generate Zip for Submission' button, this button requires a .py file and its associated log file to be visible.<br />


