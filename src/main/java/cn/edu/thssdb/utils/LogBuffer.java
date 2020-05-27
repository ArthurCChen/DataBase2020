package cn.edu.thssdb.utils;

import com.sun.org.apache.xpath.internal.functions.FuncFalse;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import sun.util.locale.provider.FallbackLocaleProviderAdapter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

public class LogBuffer extends BaseErrorListener {
    private ArrayList<String> buffer;
    private BufferedWriter output;
    public boolean hasSyntaxError = false;

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
                            String msg, RecognitionException e) {
        String exception = "Syntax Error: " + line + ":" + charPositionInLine + " " + msg;
        buffer.add(exception);
        hasSyntaxError = true;
    }

    public LogBuffer(BufferedWriter output) {
        buffer = new ArrayList<>();
        this.output = output;
    }

    public void write(String message) {
        buffer.add(message);
        hasSyntaxError = true;
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

    public String get() {
        StringBuilder result = new StringBuilder("");
        for (String message : buffer) {
            result.append(message);
        }
        buffer.clear();
        hasSyntaxError = false;
        return result.toString();
    }
}
