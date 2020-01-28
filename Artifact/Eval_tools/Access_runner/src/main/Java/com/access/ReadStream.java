package main.Java.com.access;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ReadStream implements Runnable {
    String name;
    InputStream is;
    Thread thread;
    int isSuccess = -1;

    public ReadStream(String name, InputStream is) {
        this.name = name;
        this.is = is;
    }
    public void start () {
        thread = new Thread (this);
        thread.start ();
    }
    public void run () {
        try {
            InputStreamReader isr = new InputStreamReader (is);
            BufferedReader br = new BufferedReader (isr);
            while (true) {
                String s = br.readLine ();
                if (s == null) break;
                System.out.println ("[" + name + "] " + s);
                if (s.contains("INSTALL_FAILED_NO_MATCHING_ABIS: Failed to extract native libraries")) {
                    ApkHelper.writeFailedApk(s);
                    isSuccess = 0;
                }
            }
            is.close ();
            if(isSuccess == -1)
                isSuccess = 1;
        } catch (Exception ex) {
            System.out.println ("Problem reading stream " + name + "... :" + ex);
            ex.printStackTrace ();
        }
    }

    public int getValue() {
        System.out.println("isSuccess = " + isSuccess);
        return isSuccess;
    }
}