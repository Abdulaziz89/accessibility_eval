package main.Java.com.access;

import com.testinium.deviceinformation.helper.ProcessHelper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.TimeUnit;


public class AccessEval {

    static Properties config = loadConfig();

    public static void main(String[] args) throws IOException {

        launchEmulator(config.getProperty("avd.name"));
        // run: adb root, to  get 'root' access on the emulator.
        execAdbCommand(null, "root", false);
        // get all the APKs in the directory to run
        List<String> apkList = ApkHelper.getApkFromDirectory();
        //rename files to remove spaces, needed to avoid installation errors.
        renameFiles(apkList);

        String appPackageName;
        String appVersion;
        int index = 1;
        for (String apkFile : apkList) {

            // -- get the APK info
            appPackageName = ApkHelper.getApkPackage(config.getProperty("apks.path") + apkFile);
            appVersion = ApkHelper.getApkVersion(config.getProperty("apks.path") + apkFile);
            List<String> apkActivitiesList = ApkHelper.getApkActivities(config.getProperty("apks.path") + apkFile);

            System.out.println("===========================================================");
            System.out.println("App " + (index++) + " out of " + apkList.size());
            System.out.println("Package name:" + appPackageName);
            System.out.println("App version:" + appVersion);


            // -- install the APK file. -r for repalce, -g for grant permissions
            boolean installSuccess = execAdbCommand(null, "install -r -g " +
                    config.getProperty("apks.path") + apkFile, false);

            if (!installSuccess) {
                System.out.println(appPackageName + "App installation wasn't successful!, install error");
                continue;
            }

            // --run app
            RunApp(appPackageName, Integer.parseInt(config.getProperty("monkey.timeout")), Integer.parseInt(config.getProperty("monkey.delay")));

            // -- write apk file, apk package, number of activities
            ApkHelper.writeApkInfo(apkFile, appPackageName, apkActivitiesList.size(), appVersion);

            // -- Analyze the app, and write the final results.
            AnalyzeAppCoverage(Integer.parseInt(config.getProperty("monkey.timeout")), appPackageName, appVersion);

            // -- uninstall the app when we're done.
            execAdbCommand(null, " uninstall " + appPackageName, false);

            Files.move(Paths.get(config.getProperty("apks.path") + apkFile),
                    Paths.get(config.getProperty("apks.path") + "/done/" + apkFile), StandardCopyOption.REPLACE_EXISTING);


        }
    }

    private static void renameFiles(List<String> apkList) {

        for (String apkFile : apkList) {
            // File (or directory) with old name
            File file = new File(config.getProperty("apks.path") + apkFile);
            String apkfileNoSpaces = apkFile.replace(" ", "");
            // File (or directory) with new name
            File file2 = new File(config.getProperty("apks.path") + apkfileNoSpaces);
            // Rename file
            boolean success = file.renameTo(file2);
        }

    }


    private static void AnalyzeAppCoverage(int timeLimitInMinutes, String packageName, String appVersion) {


        // get all the files (3) from the device
        ReadWriteHelper.pullResultFilesFromDevice();

        String appName = "";
        String appNumOfActivities = "";
        double coverage = 0;


        try (
                Reader reader = Files.newBufferedReader(Paths.get("./Apk_run_list.csv"));
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);
        ) {
            for (CSVRecord csvRecord : csvParser) {
                if (csvRecord.get(0).equals(packageName)) {
                    // Accessing Values by Column Index
                    if (!(csvRecord.getRecordNumber() == 1)) {

                        appName = csvRecord.get(0);
                        appNumOfActivities = csvRecord.get(2);


                        System.out.println("Record No - " + csvRecord.getRecordNumber());
                        System.out.println("---------------");
                        System.out.println("App name : " + appName);
                        System.out.println("Number of Activities : " + appNumOfActivities);
                        System.out.println("---------------\n\n");


                        System.out.println(getListOfActivitiesRun(appName, appVersion));

                        // get UI elements for all activities in the app.
                        ReadWriteHelper.prepareUiComplexityFile(appName, appVersion);

                        // get all the activities for the app, and write the results
                        for (String activity : getListOfActivitiesRun(appName, appVersion)) {
                            ArrayList<Integer> numberOfFaultsFound = getNumberOfFaultsFoundForActivity(appName, activity, appVersion);
                            ReadWriteHelper.writeFinalResultsForEachActivity(appName, activity, numberOfFaultsFound, appVersion);
                        }


                        System.out.println(Integer.parseInt(appNumOfActivities));
                        coverage = ((double) getListOfActivitiesRun(appName, appVersion).size() / (double) Integer.parseInt(appNumOfActivities)) * 100;
                        System.out.println("Coverage: " + coverage + "%");
                        ArrayList<Integer> numberOfFaultsFound = getNumberOfFaultsFound(appName);

                        ArrayList<Double> appNumAndRateOfUiElements = getAppNumAndRateOfUiElements(appName, appVersion);
                        int numberOfUiElements = getNumberOfUiElements(appName, appVersion);

                        //getPercentageOfFaultsFound(numberOfFaultsFound, appName)

                        ReadWriteHelper.writeFinalResults(appName, appNumOfActivities, timeLimitInMinutes, coverage, numberOfFaultsFound, appNumAndRateOfUiElements, appVersion);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private static int getNumberOfUiElements(String appPackageName, String appVersion) {

        ArrayList numberOfUiElement = new ArrayList();
        int uiTotal = 0;
        try (
                Reader reader = Files.newBufferedReader(Paths.get("files/Activity_complexity.csv"));
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);
        ) {
            for (CSVRecord csvRecord : csvParser) {
                if (csvRecord.getRecordNumber() != 0) {
                    // Accessing Values by Column Index
                    String packageName = csvRecord.get(0);
                    String activityName = csvRecord.get(1);
//                    String mVersion = csvRecord.get(14);
//                    if (packageName.contains(appPackageName) && mVersion.equals(appVersion)) {
                    if (packageName.contains(appPackageName)) {

                        uiTotal = Integer.parseInt(csvRecord.get(2));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return uiTotal;
    }

    private static ArrayList getAppNumAndRateOfUiElements(String appPackageName, String appVersion) {

        ArrayList<Double> arrayList = new ArrayList<Double>();
        double countActivities = 0;
        double uiTotalFaults = 0;

        double uiTotalRate = 0;
        double classNameTotalRate = 0;
        double clickableSpanTotalRate = 0;
        double duplicateClickableBoundsTotalRate = 0;
        double duplicateSpeakableTotalRate = 0;
        double editableContentTotalRate = 0;
        double imageContrastTotalRate = 0;
        double redundantDescTotalRate = 0;
        double speakableTextTotalRate = 0;
        double textContrastTotalRate = 0;
        double touchTargetTotalRate = 0;
        double traversalOrderTotalRate = 0;

        try (
                Reader reader = Files.newBufferedReader(Paths.get("Final_results_for_activities.csv"));
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);
        ) {
            for (CSVRecord csvRecord : csvParser) {
                if (csvRecord.getRecordNumber() != 0) {
                    String packageName = csvRecord.get(0);
                    String mVersion = csvRecord.get(26);
                    if (packageName.contains(appPackageName) && mVersion.contains(appVersion)) {

                        countActivities++;
                        System.out.println("count Activities" + countActivities);

                        uiTotalFaults += Double.parseDouble(csvRecord.get(2));
                        uiTotalRate += Double.parseDouble(csvRecord.get(3));
                        classNameTotalRate += Double.parseDouble(csvRecord.get(7));
                        clickableSpanTotalRate += Double.parseDouble(csvRecord.get(13));
                        duplicateClickableBoundsTotalRate += Double.parseDouble(csvRecord.get(15));
                        duplicateSpeakableTotalRate += Double.parseDouble(csvRecord.get(17));
                        editableContentTotalRate += Double.parseDouble(csvRecord.get(19));
                        imageContrastTotalRate += Double.parseDouble(csvRecord.get(9));
                        redundantDescTotalRate += Double.parseDouble(csvRecord.get(23));
                        speakableTextTotalRate += Double.parseDouble(csvRecord.get(5));
                        textContrastTotalRate += Double.parseDouble(csvRecord.get(11));
                        touchTargetTotalRate += Double.parseDouble(csvRecord.get(21));
                        traversalOrderTotalRate += Double.parseDouble(csvRecord.get(25));
                    }
                }

            }


            arrayList.add(uiTotalFaults);
            if (countActivities != 0) {
                arrayList.add(uiTotalRate / countActivities);
                arrayList.add(speakableTextTotalRate / countActivities);
                arrayList.add(classNameTotalRate / countActivities);
                arrayList.add(imageContrastTotalRate / countActivities);
                arrayList.add(textContrastTotalRate / countActivities);
                arrayList.add(clickableSpanTotalRate / countActivities);
                arrayList.add(duplicateClickableBoundsTotalRate / countActivities);
                arrayList.add(duplicateSpeakableTotalRate / countActivities);
                arrayList.add(editableContentTotalRate / countActivities);
                arrayList.add(touchTargetTotalRate / countActivities);
                arrayList.add(redundantDescTotalRate / countActivities);
                arrayList.add(traversalOrderTotalRate / countActivities);
                arrayList.add(countActivities);
                arrayList.add(uiTotalFaults / uiTotalRate);
            } else {
                for (int j = 0; j < 14; j++) {
                    arrayList.add(0.0);
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return arrayList;
    }

    private static ArrayList<Integer> getNumberOfFaultsFoundForActivity(String appPackageName, String activity, String appVersion) {


        ArrayList<Integer> faultsFound = new ArrayList();
        int SpeakableTextPresentCheck = 0;
        int ClassNameCheck = 0;
        int ImageContrastCheck = 0;
        int TextContrastCheck = 0;
        int ClickableSpanCheck = 0;
        int DuplicateClickableBoundsCheck = 0;
        int DuplicateSpeakableTextCheck = 0;
        int EditableContentDescCheck = 0;
        int TouchTargetSizeCheck = 0;
        int RedundantDescriptionCheck = 0;
        int TraversalOrderCheck = 0;
        int totalFaults = 0;

        try (
                Reader reader = Files.newBufferedReader(Paths.get("files/ACCESS_ISSUES.csv"));
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);
        ) {
            for (CSVRecord csvRecord : csvParser) {
                if (csvRecord.getRecordNumber() != 0) {
                    // Accessing Values by Column Index
                    String packageName = csvRecord.get(0);
                    String activityName = csvRecord.get(1);
                    String FaultType = csvRecord.get(2);
//                    String mVersion = csvRecord.get(11);
                    if (packageName.contains(appPackageName) && activityName.contains(activity)) {
                        switch (FaultType) {
                            case "checks.SpeakableTextPresentCheck":
                                SpeakableTextPresentCheck++;
                                break;

                            case "checks.ImageContrastCheck":
                                ImageContrastCheck++;
                                break;


                            case "checks.ClassNameCheck":
                                ClassNameCheck++;
                                break;

                            case "checks.TextContrastCheck":
                                TextContrastCheck++;
                                break;

                            case "checks.ClickableSpanCheck":
                                ClickableSpanCheck++;
                                break;

                            case "checks.DuplicateClickableBoundsCheck":
                                DuplicateClickableBoundsCheck++;
                                break;

                            case "checks.DuplicateSpeakableTextCheck":
                                DuplicateSpeakableTextCheck++;
                                break;

                            case "checks.EditableContentDescCheck":
                                EditableContentDescCheck++;
                                break;

                            case "checks.TouchTargetSizeCheck":
                                TouchTargetSizeCheck++;
                                break;

                            case "checks.RedundantDescriptionCheck":
                                RedundantDescriptionCheck++;
                                break;

                            case "checks.TraversalOrderCheck":
                                TraversalOrderCheck++;
                                break;

                        }
                    }
                }
            }

            totalFaults = SpeakableTextPresentCheck
                    + ClassNameCheck
                    + ImageContrastCheck
                    + TextContrastCheck
                    + ClickableSpanCheck
                    + DuplicateClickableBoundsCheck
                    + DuplicateSpeakableTextCheck
                    + EditableContentDescCheck
                    + TouchTargetSizeCheck
                    + RedundantDescriptionCheck
                    + TraversalOrderCheck;

            faultsFound.add(totalFaults);
            faultsFound.add(SpeakableTextPresentCheck);
            faultsFound.add(ClassNameCheck);
            faultsFound.add(ImageContrastCheck);
            faultsFound.add(TextContrastCheck);
            faultsFound.add(ClickableSpanCheck);
            faultsFound.add(DuplicateClickableBoundsCheck);
            faultsFound.add(DuplicateSpeakableTextCheck);
            faultsFound.add(EditableContentDescCheck);
            faultsFound.add(TouchTargetSizeCheck);
            faultsFound.add(RedundantDescriptionCheck);
            faultsFound.add(TraversalOrderCheck);


        } catch (IOException e) {
            e.printStackTrace();
        }


        return faultsFound;
    }


    private static ArrayList<Integer> getNumberOfFaultsFound(String appPackageName) {

        ArrayList<Integer> faultsFound = new ArrayList<>();
        int SpeakableTextPresentCheck = 0;
        int ClassNameCheck = 0;
        int ImageContrastCheck = 0;
        int TextContrastCheck = 0;
        int ClickableSpanCheck = 0;
        int DuplicateClickableBoundsCheck = 0;
        int DuplicateSpeakableTextCheck = 0;
        int EditableContentDescCheck = 0;
        int TouchTargetSizeCheck = 0;
        int RedundantDescriptionCheck = 0;
        int TraversalOrderCheck = 0;
        int totalFaults = 0;

        try (
                Reader reader = Files.newBufferedReader(Paths.get("files/ACCESS_ISSUES.csv"));
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);
        ) {
            for (CSVRecord csvRecord : csvParser) {
                if (csvRecord.getRecordNumber() != 0) {
                    // Accessing Values by Column Index
                    String packageName = csvRecord.get(0);
                    String FaultType = csvRecord.get(2);
                    if (packageName.contains(appPackageName)) {
                        switch (FaultType) {
                            case "checks.SpeakableTextPresentCheck":
                                SpeakableTextPresentCheck++;
                                break;

                            case "checks.ImageContrastCheck":
                                ImageContrastCheck++;
                                break;


                            case "checks.ClassNameCheck":
                                ClassNameCheck++;
                                break;

                            case "checks.TextContrastCheck":
                                TextContrastCheck++;
                                break;

                            case "checks.ClickableSpanCheck":
                                ClickableSpanCheck++;
                                break;

                            case "checks.DuplicateClickableBoundsCheck":
                                DuplicateClickableBoundsCheck++;
                                break;

                            case "checks.DuplicateSpeakableTextCheck":
                                DuplicateSpeakableTextCheck++;
                                break;

                            case "checks.EditableContentDescCheck":
                                EditableContentDescCheck++;
                                break;

                            case "checks.TouchTargetSizeCheck":
                                TouchTargetSizeCheck++;
                                break;

                            case "checks.RedundantDescriptionCheck":
                                RedundantDescriptionCheck++;
                                break;

                            case "checks.TraversalOrderCheck":
                                TraversalOrderCheck++;
                                break;

                        }
                    }
                }
            }

            totalFaults = SpeakableTextPresentCheck
                    + ClassNameCheck
                    + ImageContrastCheck
                    + TextContrastCheck
                    + ClickableSpanCheck
                    + DuplicateClickableBoundsCheck
                    + DuplicateSpeakableTextCheck
                    + EditableContentDescCheck
                    + TouchTargetSizeCheck
                    + RedundantDescriptionCheck
                    + TraversalOrderCheck;

            faultsFound.add(totalFaults);
            faultsFound.add(SpeakableTextPresentCheck);
            faultsFound.add(ClassNameCheck);
            faultsFound.add(ImageContrastCheck);
            faultsFound.add(TextContrastCheck);
            faultsFound.add(ClickableSpanCheck);
            faultsFound.add(DuplicateClickableBoundsCheck);
            faultsFound.add(DuplicateSpeakableTextCheck);
            faultsFound.add(EditableContentDescCheck);
            faultsFound.add(TouchTargetSizeCheck);
            faultsFound.add(RedundantDescriptionCheck);
            faultsFound.add(TraversalOrderCheck);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return faultsFound;
    }


    private static Set<String> getListOfActivitiesRun(String appPackageName, String appVersion) {
        Set<String> activitiesList = new HashSet<>();

        try (
                Reader reader = Files.newBufferedReader(Paths.get("files/ACCESS_CHECKS.csv"));
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);
        ) {
            for (CSVRecord csvRecord : csvParser) {
                if (csvRecord.getRecordNumber() != 0) {
                    String pakcageName = csvRecord.get(0);
//                    String mVersion = csvRecord.get(3);
                    if (pakcageName.contains(appPackageName)) {
                        if (Integer.parseInt(csvRecord.get(2)) > 0 && (!csvRecord.get(1).contains("android.widget"))
                                && (!csvRecord.get(1).contains("android.view")) && (!csvRecord.get(1).contains("android.support."))
                                && (!csvRecord.get(1).contains("android.app."))) {
                            activitiesList.add(csvRecord.get(1));
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return activitiesList;
    }

    private static void RunApp(String appPackageName, int timeLimit, int delay) {
        //run monkey , to prevent system events use  '--pct-syskeys 0'
        long startTime = System.currentTimeMillis();
        int seed = Integer.parseInt(config.getProperty("monkey.seed"));
        boolean result;
        boolean firstRun = true;
        try {
            while (true) {
                if (firstRun) {

                    launchEmulator(config.getProperty("avd.name"));

                    // run: adb root, to  get 'root' access on the device.
                    execAdbCommand(null, "root", false);

                    result = execAdbCommand(null, "monkey -p " + config.getProperty("listener.app.id") + " -c android.intent.category.LAUNCHER 1", true);
                    System.out.println("running " + config.getProperty("listener.app.id") + " ...");
                    Thread.sleep(2000);
                    result = execAdbCommand(null, "monkey -p " + appPackageName + " -c android.intent.category.LAUNCHER 1", true);
                    System.out.println("sleeping...");
                    Thread.sleep(10000);
                    firstRun = false;
                }

                result = execAdbCommand(null, "\"(sleep " + config.getProperty("monkey.run.timeout") + "; kill $(pgrep monkey)) & "
                        + "monkey -p " + appPackageName + " -s " + seed + " --pct-syskeys 0  --kill-process-after-error -v -v -v --throttle  "
                        + delay + " 50000", true);
                seed = seed + 416;
                result = execAdbCommand(null, "am force-stop " + appPackageName, true);
                long estimatedTime = System.currentTimeMillis() - startTime;
                System.out.println("Time is " + estimatedTime);
                double timeLimit2 = timeLimit * 60000;

                if (estimatedTime >= timeLimit2) {
                    System.out.println("Time limit (" + timeLimit2 + ") reached...");
                    return;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static List<String> getAppPermissions(String apkPath) throws IOException {

        Process process = null;
        String commandString;
        List<String> permissionsList = new ArrayList<>();

        commandString = String.format(config.getProperty("aapt.path") + " d permissions " + apkPath);
        //aapt d permissions /path/to/com.your.package.apk
        System.out.println("==========================");
        System.out.print("Command is " + commandString + "\n");

        try {
            process = ProcessHelper.runTimeExec(commandString);
        } catch (IOException e) {
            e.printStackTrace();
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        BufferedReader reader2 = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        String line2;


        String permissionName;
        while ((line = reader.readLine()) != null) {
            //System.out.println(line);
            if (line.contains("uses-permission:")) {
                permissionName = line.substring(line.indexOf("name='") + 6, line.length() - 1);
                permissionsList.add(permissionName);
            }

        }

        while ((line2 = reader2.readLine()) != null) {
            System.out.println(line2);
        }

        process.destroy();
        return permissionsList;

    }

    public static void execCommand(String command) throws IOException {

        Process process = null;
        String commandString;
        commandString = String.format(command);
        System.out.print("==========================");
        System.out.print("Command is " + commandString + "\n");

        try {
            process = ProcessHelper.runTimeExec(commandString);
        } catch (IOException e) {

            e.printStackTrace();
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        BufferedReader reader2 = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        String line2;


        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }

        while ((line2 = reader2.readLine()) != null) {
            System.out.println(line2);
        }

    }

    static boolean execAdbCommand(String deviceId, String command, boolean useShell) {

        ReadStream s1 = null;
        ReadStream s2 = null;
        Process process = null;
        String commandString;
        System.out.println(deviceId);
        if (deviceId != null && useShell)
            commandString = String.format("%s", "adb -s " + deviceId + " shell " + command);
        else if (deviceId != null)
            commandString = String.format("%s", "adb -s " + deviceId + " " + command);
        else if (useShell)
            commandString = String.format("%s", "adb shell " + command);
        else
            commandString = String.format("%s", "adb " + command);

        try {
            process = ProcessHelper.runTimeExec(commandString);
            s1 = new ReadStream("stdin", process.getInputStream());
            s2 = new ReadStream("stderr", process.getErrorStream());
            s1.start();
            s2.start();
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (process != null)
                process.destroy();
        }
        if (commandString.contains("install -r -g"))
            while (s2.getValue() == -1) {

            }
        return s2.getValue() != 0;
    }


    private static void launchEmulator(String nameOfAVD) {
        boolean isAvdRunning = false;

        try {
            isAvdRunning = isDeviceRunning();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!isAvdRunning) {
            System.out.println("Starting emulator for '" + nameOfAVD + "' ...");
            String[] aCommand = new String[]{config.getProperty("emulator.path"), "-avd", nameOfAVD};
            try {
                Process process = new ProcessBuilder(aCommand).start();
                process.waitFor(30, TimeUnit.SECONDS);
                System.out.println("Emulator launched successfully!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean isDeviceRunning() throws IOException {
        Runtime rt = Runtime.getRuntime();
        Process pr = rt.exec("adb devices");
        BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
        String line = null;

        while ((line = input.readLine()) != null) {
            System.out.println(line);
            if (line.contains(config.getProperty("emulator.name"))) {
                System.out.println("Device is already running..");
                return true;
            }
        }
        System.out.println("Device is not running..");
        return false;
    }


    private static Properties loadConfig() {
        try (InputStream input = new FileInputStream("./src/main/Java/com/access/config.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            return prop;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
