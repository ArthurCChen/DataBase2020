package cn.edu.thssdb.utils;

import com.sun.org.apache.xpath.internal.functions.FuncFalse;
import sun.util.locale.provider.FallbackLocaleProviderAdapter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

public class LogBuffer {
    private ArrayList<String> buffer;
    private BufferedWriter output;

    public LogBuffer(BufferedWriter output) {
        buffer = new ArrayList<>();
        this.output = output;
    }

    public void write(String message) {
        buffer.add(message);
    }

    public void flush() {
        try {
            for (String message : buffer) {
                output.write(message);
                output.newLine();
            }
            buffer.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
