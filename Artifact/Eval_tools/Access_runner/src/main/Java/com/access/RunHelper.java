package main.Java.com.access;

public class RunHelper {
    static void removeOldResultFilesFromDevice() {

        AccessEval.execAdbCommand(null, "rm -f /storage/emulated/0/Android/data/com.aziz.accessibilityEval/files/access_test/ACCESS_CHECKS.csv", true);
        AccessEval.execAdbCommand(null, "rm -f /storage/emulated/0/Android/data/com.aziz.accessibilityEval/files/access_test/ACCESS_ISSUES.csv", true);
//        AccessEval.execAdbCommand(null, "rm -f /storage/emulated/0/Android/data/com.aziz.accessibilityEval/files/access_test/Activity_complexity.csv", true);
        AccessEval.execAdbCommand(null, "rm -f /storage/emulated/0/Android/data/com.aziz.accessibilityEval/files/access_test/aUi_elements.csv", true);


    }
}
