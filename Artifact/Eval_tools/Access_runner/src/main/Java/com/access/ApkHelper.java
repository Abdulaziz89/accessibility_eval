package main.Java.com.access;

import com.testinium.deviceinformation.helper.ProcessHelper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static main.Java.com.access.AccessEval.config;

public class ApkHelper {


    static Set<String> allApkList = new HashSet<>();

    public static List<String> getApkActivities(String apkPath) throws IOException {

        Process process = null;
        String commandString;
        List<String> activitiesList = new ArrayList<>();

        commandString = String.format(config.getProperty("aapt.path") + " dump xmltree " + apkPath + " AndroidManifest.xml");
        //aapt dump xmltree <APK> AndroidManifest.xml
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


        String activityName;
        while ((line = reader.readLine()) != null) {
            //System.out.println(line);
            boolean aFlag = false;
            if (line.contains("activity")) {
                while ((line = reader.readLine()) != null) {
                    if (line.contains("android:name")) {
                        activityName = line.substring(line.indexOf("\"") + 1, line.indexOf("\"", line.indexOf("\"") + 1));
                        activitiesList.add(activityName);
                        break;
                    }

                }
            }
        }

        while ((line2 = reader2.readLine()) != null) {
            System.out.println(line2);
        }

        process.destroy();
        return activitiesList;

    }

    static void writeFailedApk(String failedApkLine) {
        try (

                FileWriter fileWriter = new FileWriter("./Apk_failed_install_list.csv", true);
                CSVPrinter csvPrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT);
        ) {

            String[] tokens = failedApkLine.split("\\/");
            String apkToWrite = tokens[tokens.length - 1].split(":")[0];
            csvPrinter.printRecord(apkToWrite);
            csvPrinter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void writeApkInfo(String apkFile, String appPackageName, int size, String appVersion) {
        try (

                FileWriter fileWriter = new FileWriter("./Apk_run_list.csv", true);
                //BufferedWriter writer = Files.newBufferedWriter(Paths.get("./Apk_run_list.csv"),StandardCharsets.UTF_8,);
                CSVPrinter csvPrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT);
        ) {
            csvPrinter.printRecord(appPackageName, apkFile, size, appVersion);
            csvPrinter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    static List<String> getApkFromDirectory() {

        File folder = new File(config.getProperty("apks.path"));
        File[] listOfFiles = folder.listFiles();
        List<String> apkList = new ArrayList<>();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].getName().contains(".apk")) {
//                System.out.println(listOfFiles[i].getName());
                apkList.add(listOfFiles[i].getName());
            }
        }
        return apkList;
    }

    static String getApkPackage(String apkPath) throws IOException {

        Process process = null;
        String commandString;
        String appPackageName = null;
        commandString = String.format(config.getProperty("aapt.path") + " dump badging " + apkPath);
//        System.out.println("==========================");
//        System.out.print("Command is " + commandString + "\n");

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
            //System.out.println(line);
            if (line.contains("package:")) {
                appPackageName = line.substring(line.indexOf("name=") + 6, line.indexOf("versionCode") - 2);
                //, line.indexOf("'")
            }
        }

        while ((line2 = reader2.readLine()) != null) {
            System.out.println(line2);
        }

        process.destroy();
        return appPackageName;

    }

    static String getApkVersion(String apkPath) throws IOException {

        Process process = null;
        String commandString;
        String appPackageName = null;
        commandString = String.format(config.getProperty("aapt.path") + " dump badging " + apkPath);
//        System.out.println("==========================");
//        System.out.print("Command is " + commandString + "\n");

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
            if (line.contains("versionName=")) {

                String versionLine = line.substring(line.indexOf("versionName='"));

                String[] insideBrackets = versionLine.split("'");
                appPackageName = insideBrackets[1];
            }
        }

        while ((line2 = reader2.readLine()) != null) {
            System.out.println(line2);
        }

        process.destroy();
        return appPackageName;

    }


    private static List<String> lookAtApk(String apkPath) throws IOException {
        Process process = null;
        String commandString;
        List<String> activitiesList = new ArrayList<>();

        commandString = String.format(config.getProperty("aapt.path") + " dump xmltree " + apkPath + " AndroidManifest.xml");
        //aapt dump xmltree <APK> AndroidManifest.xml
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


        while ((line = reader.readLine()) != null) {
            System.out.println(line);

        }
        while ((line2 = reader2.readLine()) != null) {
            System.out.println(line2);
        }

        process.destroy();
        return activitiesList;
    }


}
