package com.android.internal.util.custom;

import android.util.Log;
import java.io.*;

public class ReadFileData {
    private static final String TAG = "ReadKeybox";
    private static final String FILE_PATH = "/data/local/tmp/";

    public static String readFile(String file_name) {
        File file = new File(FILE_PATH + file_name);
        if (!file.exists()) {
            Log.e(TAG, "File not found: " + FILE_PATH + file_name);
            return "";
        }

        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            Log.e(TAG, "Error reading file", e);
            return "";
        }
        return content.toString();
    }
}
