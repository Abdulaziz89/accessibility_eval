package main.Java.com.access;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ReadWriteHelper {
    static void writeFinalResults(String appName, String appNumOfActivities, int timeLimitInMinutes, double coverage, ArrayList<Integer> numberOfFaultsFound, ArrayList<Double> appNumAndRateOfUiElements, String appVersion) {

        double totalUiElements = (appNumAndRateOfUiElements.get(14) * 100);

        try (

                FileWriter fileWriter = new FileWriter("./Final_results.csv", true);
                CSVPrinter csvPrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT);
        ) {

            csvPrinter.printRecord(appName, appNumOfActivities, coverage, timeLimitInMinutes, numberOfFaultsFound.get(0), totalUiElements, appNumAndRateOfUiElements.get(1),
                    numberOfFaultsFound.get(1), appNumAndRateOfUiElements.get(2), numberOfFaultsFound.get(2), appNumAndRateOfUiElements.get(3), numberOfFaultsFound.get(3), appNumAndRateOfUiElements.get(4),
                    numberOfFaultsFound.get(4), appNumAndRateOfUiElements.get(5), numberOfFaultsFound.get(5), appNumAndRateOfUiElements.get(6), numberOfFaultsFound.get(6), appNumAndRateOfUiElements.get(7),
                    numberOfFaultsFound.get(7), appNumAndRateOfUiElements.get(8), numberOfFaultsFound.get(8), appNumAndRateOfUiElements.get(9), numberOfFaultsFound.get(9), appNumAndRateOfUiElements.get(10),
                    numberOfFaultsFound.get(10), appNumAndRateOfUiElements.get(11), numberOfFaultsFound.get(11), appNumAndRateOfUiElements.get(12), appVersion);
            csvPrinter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    static void pullResultFilesFromDevice() {

        AccessEval.execAdbCommand(null, "pull /data/data/com.aziz.accessibilityEval/files/ACCESS_CHECKS.csv files/ACCESS_CHECKS.csv", false);
        AccessEval.execAdbCommand(null, "pull /data/data/com.aziz.accessibilityEval/files/ACCESS_ISSUES.csv files/ACCESS_ISSUES.csv", false);
//        AccessEval.execAdbCommand(null, "pull /storage/emulated/0/Android/data/com.aziz.accessibilityEval/files/access_test/Activity_complexity.csv files/ACCESS_CHECKS.csv", false);
        AccessEval.execAdbCommand(null, "pull /data/data/com.aziz.accessibilityEval/files/aUi_elements.csv files/aUi_elements.csv", false);


    }

    public static void writeFinalResultsForEachActivity(String appName, String activity, ArrayList<Integer> numberOfFaultsFound, String appVersion) {

        ArrayList<Integer> numberOfUiElement = getNumberOfUiElementForActivity(appName, activity);

        //double uiTotal = 0
        double rateOfFaultsFound = 0;

        double rateOfClassName = 0;
        double rateOfclickableSpan = 0;
        double rateOfduplicateClickableBounds = 0;
        double rateOfduplicateSpeakable = 0;
        double rateOfeditableContent = 0;
        double rateOfimageContrast = 0;
        double rateOfredundantDesc = 0;
        double rateOfspeakableText = 0;
        double rateOftextContrast = 0;
        double rateOftouchTarget = 0;
        double rateOftraversalOrder = 0;


        if (numberOfUiElement.get(0) != 0)
            rateOfFaultsFound = ((double) numberOfFaultsFound.get(0) / ((double) numberOfUiElement.get(0))) * 100;
        if (numberOfUiElement.get(2) != 0)
            rateOfClassName = ((double) numberOfFaultsFound.get(2) / (double) numberOfUiElement.get(2)) * 100;
        if (numberOfUiElement.get(5) != 0)
            rateOfclickableSpan = ((double) numberOfFaultsFound.get(5) / (double) numberOfUiElement.get(5)) * 100;
        if (numberOfUiElement.get(6) != 0)
            rateOfduplicateClickableBounds = ((double) numberOfFaultsFound.get(6) / (double) numberOfUiElement.get(6)) * 100;
        if (numberOfUiElement.get(7) != 0)
            rateOfduplicateSpeakable = ((double) numberOfFaultsFound.get(7) / (double) numberOfUiElement.get(7)) * 100;
        if (numberOfUiElement.get(8) != 0)
            rateOfeditableContent = ((double) numberOfFaultsFound.get(8) / (double) numberOfUiElement.get(8)) * 100;
        if (numberOfUiElement.get(3) != 0)
            rateOfimageContrast = ((double) numberOfFaultsFound.get(3) / (double) numberOfUiElement.get(3)) * 100;
        if (numberOfUiElement.get(10) != 0)
            rateOfredundantDesc = ((double) numberOfFaultsFound.get(10) / (double) numberOfUiElement.get(10)) * 100;
        if (numberOfUiElement.get(1) != 0)
            rateOfspeakableText = ((double) numberOfFaultsFound.get(1) / (double) numberOfUiElement.get(1)) * 100;
        if (numberOfUiElement.get(4) != 0)
            rateOftextContrast = ((double) numberOfFaultsFound.get(4) / (double) numberOfUiElement.get(4)) * 100;
        if (numberOfUiElement.get(9) != 0)
            rateOftouchTarget = ((double) numberOfFaultsFound.get(9) / (double) numberOfUiElement.get(9)) * 100;
        if (numberOfUiElement.get(11) != 0)
            rateOftraversalOrder = ((double) numberOfFaultsFound.get(11) / (double) numberOfUiElement.get(11)) * 100;

        try (

                FileWriter fileWriter = new FileWriter("./Final_results_for_activities.csv", true);
                CSVPrinter csvPrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT);
        ) {


            csvPrinter.printRecord(appName, activity, numberOfFaultsFound.get(0), rateOfFaultsFound,
                    numberOfFaultsFound.get(1), rateOfspeakableText, numberOfFaultsFound.get(2), rateOfClassName,
                    numberOfFaultsFound.get(3), rateOfimageContrast, numberOfFaultsFound.get(4), rateOftextContrast,
                    numberOfFaultsFound.get(5), rateOfclickableSpan, numberOfFaultsFound.get(6), rateOfduplicateClickableBounds,
                    numberOfFaultsFound.get(7), rateOfduplicateSpeakable, numberOfFaultsFound.get(8), rateOfeditableContent,
                    numberOfFaultsFound.get(9), rateOftouchTarget, numberOfFaultsFound.get(10), rateOfredundantDesc,
                    numberOfFaultsFound.get(11), rateOftraversalOrder, appVersion);
            csvPrinter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static ArrayList getNumberOfUiElementForActivity(String appPackageName, String activity) {

        ArrayList numberOfUiElement = new ArrayList();

        int uiTotal = 0;
        int classNameTotal = 0;
        int clickableSpanTotal = 0;
        int duplicateClickableBoundsTotal = 0;
        int duplicateSpeakableTotal = 0;
        int editableContentTotal = 0;
        int imageContrastTotal = 0;
        int redundantDescTotal = 0;
        int speakableTextTotal = 0;
        int textContrastTotal = 0;
        int touchTargetTotal = 0;
        int traversalOrderTotal = 0;

        try (
                Reader reader = Files.newBufferedReader(Paths.get("files/Activity_complexity.csv"));
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);
        ) {
            for (CSVRecord csvRecord : csvParser) {
                if (csvRecord.getRecordNumber() != 0 || true) {
                    // Accessing Values by Column Index
                    String packageName = csvRecord.get(0);
                    String activityName = csvRecord.get(1);

                    if (packageName.contains(appPackageName) && activityName.contains(activity) && (Integer.parseInt(csvRecord.get(2)) > uiTotal)) {


                        uiTotal = Integer.parseInt(csvRecord.get(2));
                        classNameTotal = Integer.parseInt(csvRecord.get(3));
                        clickableSpanTotal = Integer.parseInt(csvRecord.get(4));
                        duplicateClickableBoundsTotal = Integer.parseInt(csvRecord.get(5));
                        duplicateSpeakableTotal = Integer.parseInt(csvRecord.get(6));
                        editableContentTotal = Integer.parseInt(csvRecord.get(7));
                        imageContrastTotal = Integer.parseInt(csvRecord.get(8));
                        redundantDescTotal = Integer.parseInt(csvRecord.get(9));
                        speakableTextTotal = Integer.parseInt(csvRecord.get(10));
                        textContrastTotal = Integer.parseInt(csvRecord.get(11));
                        touchTargetTotal = Integer.parseInt(csvRecord.get(12));
                        traversalOrderTotal = Integer.parseInt(csvRecord.get(13));


                    }
                }
            }

            numberOfUiElement.add(uiTotal);
            numberOfUiElement.add(speakableTextTotal);
            numberOfUiElement.add(classNameTotal);
            numberOfUiElement.add(imageContrastTotal);
            numberOfUiElement.add(textContrastTotal);
            numberOfUiElement.add(clickableSpanTotal);
            numberOfUiElement.add(duplicateClickableBoundsTotal);
            numberOfUiElement.add(duplicateSpeakableTotal);
            numberOfUiElement.add(editableContentTotal);
            numberOfUiElement.add(touchTargetTotal);
            numberOfUiElement.add(redundantDescTotal);
            numberOfUiElement.add(traversalOrderTotal);


        } catch (IOException e) {
            e.printStackTrace();
        }


        return numberOfUiElement;
    }

    static void prepareUiComplexityFile(String appPackageName, String appVersion) {


        Set<String> uiElementFound = new HashSet<>();
        Set<String> activitiesList = getActiviesListForUi(appPackageName);


        String packageName = " ";
        String activityName = " ";
        String FaultType = " ";
        String uniqeId = " ";
        String elmentType = " ";
        String resourceName = " ";
        String index;
        String mVersion = "";
        for (String activity : activitiesList) {

            int SpeakableTextPresentUI = 0;
            int ClassNameUI = 0;
            int ImageContrastUI = 0;
            int TextContrastUI = 0;
            int ClickableSpanUI = 0;
            int DuplicateClickableBoundsUI = 0;
            int DuplicateSpeakableTextUI = 0;
            int EditableContentDescUI = 0;
            int TouchTargetSizeUI = 0;
            int RedundantDescriptionUI = 0;
            int TraversalOrderUI = 0;
            int totalFaults = 0;

            try (
                    Reader reader = Files.newBufferedReader(Paths.get("files/aUi_elements.csv"));
                    CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);
            ) {


                for (CSVRecord csvRecord : csvParser) {
                    if (csvRecord.getRecordNumber() != 0 || true) {

                        // Accessing Values by Column Index
                        packageName = csvRecord.get(0);
                        activityName = csvRecord.get(1);
                        FaultType = csvRecord.get(2);
                        uniqeId = csvRecord.get(3);
                        elmentType = csvRecord.get(4);
                        resourceName = csvRecord.get(5);
                        index = csvRecord.get(6);
//                        mVersion = csvRecord.get(7);x1989

//                        if ( packageName.contains(appPackageName) && activityName.contains(activity) && mVersion.contains(appVersion)) {
                        if (packageName.contains(appPackageName) && activityName.contains(activity)) {
                            uiElementFound.add(packageName + ", " + activityName + ", " + FaultType + ", " + uniqeId + ", " + elmentType + ", " + resourceName + ", " + index);
                        }
                    }
                }


            } catch (IOException e) {
                e.printStackTrace();
            }

            for (String str : uiElementFound) {

                if (str.contains("speakableText"))
                    SpeakableTextPresentUI++;

                if (str.contains("imageContrast"))
                    ImageContrastUI++;

                if (str.contains("className"))
                    ClassNameUI++;

                if (str.contains("textContrast"))
                    TextContrastUI++;

                if (str.contains("clickableSpan"))
                    ClickableSpanUI++;

                if (str.contains("duplicateClickable"))
                    DuplicateClickableBoundsUI++;

                if (str.contains("duplicateSpeakable"))
                    DuplicateSpeakableTextUI++;

                if (str.contains("editableContent"))
                    EditableContentDescUI++;

                if (str.contains("touchTarget"))
                    TouchTargetSizeUI++;

                if (str.contains("redundantDesc"))
                    RedundantDescriptionUI++;

                if (str.contains("traversalOrder"))
                    TraversalOrderUI++;
            }


            totalFaults = SpeakableTextPresentUI
                    + ClassNameUI
                    + ImageContrastUI
                    + TextContrastUI
                    + ClickableSpanUI
                    + DuplicateClickableBoundsUI
                    + DuplicateSpeakableTextUI
                    + EditableContentDescUI
                    + TouchTargetSizeUI
                    + RedundantDescriptionUI
                    + TraversalOrderUI;


            uiElementFound.clear();


            try (
                    FileWriter fileWriter = new FileWriter("files/Activity_complexity.csv", true);
                    CSVPrinter csvPrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT);
            ) {

                csvPrinter.printRecord(appPackageName, activity, totalFaults, ClassNameUI, ClickableSpanUI,
                        DuplicateClickableBoundsUI, DuplicateSpeakableTextUI, EditableContentDescUI, ImageContrastUI,
                        RedundantDescriptionUI, SpeakableTextPresentUI, TextContrastUI, TouchTargetSizeUI, TraversalOrderUI, mVersion);
                csvPrinter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }


    }

    private static Set<String> getActiviesListForUi(String appPackageName) {

        String packageName = " ";
        String activityName = " ";
        Set<String> activitiesList = new HashSet<>();


        try (
                Reader reader = Files.newBufferedReader(Paths.get("files/aUi_elements.csv"));
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);
        ) {

            for (CSVRecord csvRecord : csvParser) {
                if (csvRecord.getRecordNumber() != 0 || true) {

                    // Accessing Values by Column Index
                    packageName = csvRecord.get(0);
                    activityName = csvRecord.get(1);

                    if (packageName.contains(appPackageName)) {
                        activitiesList.add(activityName);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return activitiesList;
    }
}
