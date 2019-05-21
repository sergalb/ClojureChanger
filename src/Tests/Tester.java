package Tests;

import main.ClojureTransformer;
import org.junit.*;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class Tester {

    @Test
    public void checkSimpleJSFile(){
        String simpleJSFile = "C:\\Users\\Sergey\\Desktop\\JavaScriptChanger\\src\\Tests\\SimpleTest.js";
        String programResult = "C:\\Users\\Sergey\\Desktop\\JavaScriptChanger\\src\\Tests\\SimpleAns.js";
        ClojureTransformer.transformFile(simpleJSFile, programResult);

        runFile(new File(simpleJSFile));
        runFile(new File(programResult));
    }


    //put print block before each return
    //it lets to watch for calculating of programs
    //if calculating of 2 are equals it means(with good probability)
    //that both programs behavior are equals
    private File prepareFile(File file) {
        File out = new File(file.getParent(), file.getName().replace(".js", "Prepared.js"));
        try (PrintWriter writer = new PrintWriter(new FileOutputStream(out))){
            try (BufferedReader reader = new BufferedReader(new FileReader(file))){
                while (reader.ready()) {
                    String line = reader.readLine();
                    if (line.contains("return ")) {

                        writer.println("print(" +
                                line.substring(line.indexOf("return ") + 7, line.length() - 1)
                                + ");\n");
                    }
                    writer.println(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return out;
    }

    private String runFile(File file) {
        File runedFile = prepareFile(file);
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        Object result = null;
        try {
            result = engine.eval(new FileReader(runedFile));
        } catch (ScriptException e) {
            System.err.println(e.getMessage());
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
        }
        if (result == null) {
            throw new NullPointerException("Error while eval file: " + file.getName());
        }
        System.out.println(result + "v");
        return result.toString();
    }
}
