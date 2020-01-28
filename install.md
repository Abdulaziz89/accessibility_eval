Prerequsite: You must the have `Android SDK` installed. 

To start using the framwork you need to do the follwoing: 
1. Create an Android Virtual Device (or use an actual Android device).
   - You can create one in Android Studio using `Tools > AVD Manager`.
2. Install the `listener.apk` app under `Artifact\Eval_tools\Listener` on the emualtor.
   - Within the same folder as the app APK run: `adb install listener.apk`
   - Run the app within the emulator, click on `Start now` and `Don't show again`
   - Go to `Settings > Accessiblity > ` and activate `Accessibility Evaluation Service`
3. Run the `access_eval` tool under `Artifact\Eval_tools\Access_runner` to start:
   - Edit the `config.properties` located in `src\main\Java\com\access` accordingly.
     - `avd.name` must be the name of the emulator created above.
   - Place any APKs you want to run in `apks\test_apps` directory.
   - Run the command `mvn package` to generate the jar file along with its dependncy. 
   - Run the command `- java -jar target/Access_runner-1.0-SNAPSHOT.jar`
4. After running the tool, you can find the results in these files under `\Access_runner\`:
   - `Final_results.csv` The results of all accessiblity issues for the apps.
   - `Final_results_for_activities.csv` The results of all accessiblity issues for the apps, for each activity.
   - `Apk_run_list.csv` The list of app that were ran successfully.






