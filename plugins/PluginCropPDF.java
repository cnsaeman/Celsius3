/*
 * PluginArXiv.java
 *
 * Created on 14. Juli 2007, 12:20
 *
 * complete, testing
 */

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import celsius.tools.Parser;
import celsius.tools.TextFile;
import celsius.tools.toolbox;
import celsius.tools.ExecutionShell;


/**
 * @author cnsaeman
 */
public class PluginCropPDF extends Thread {

    public static final HashMap<String,String> metaData=new HashMap<String,String>() {
        {
            put("title"             ,"CropPDF/eReader");
            put("author"            ,"Christian Saemann");
            put("version"           ,"1.0");
            put("help"              ,"This plugin crops the pdf files located on an ebook reader. The parameter is the folder for the ebook reader.");
            put("needsFirstPage"    ,"no");
            put("longRunTime"       ,"yes");
            put("requiredFields"    ,"fullpath");
            put("type"              ,"manual");
            put("defaultParameters" ,"");
            put("parameter-help"    ,"none.");
        }
    };

    public final static String EOP = String.valueOf((char) 12);   // EndOfPage signal
    public final static String filesep = System.getProperty("file.separator");  // EndOfLine signal
    public final static String linesep = System.getProperty("line.separator");  // EndOfLine signal

    private final String TI="P:Crp>";
    
    private String oai;
    
    public celsius.MProperties Information;
    public ArrayList<String> Msgs;


    public void Initialize(celsius.MProperties i, ArrayList<String> m){
        Information=i; Msgs=m;
    }
  
    public void run() {
        String basedir=Information.get("");
        if (!basedir.endsWith(filesep)) basedir+=filesep;
        String fn=Parser.CutFromLast(Information.get("fullpath"),filesep);
        if (fn.endsWith(".pdf")) {
            try {
                TextFile.CopyFile(basedir + fn, "tmpin.pdf");
                String cropstr = "perl plugins/pdfcrop.pl tmpin.pdf tmpout.pdf";
                ExecutionShell ES = new ExecutionShell(cropstr, 0, false);
                ES.start();
                ES.join();
                if (ES.errorflag) {
                    Msgs.add(TI + "Error Message: " + ES.errorMsg);
                }
                if (new File("tmpout.pdf").exists()) {
                    TextFile.Delete(basedir+fn);
                    TextFile.CopyFile("tmpout.pdf", basedir+fn);
                    toolbox.Information(null,"File cropped and replaced", "All done:");
                } else Msgs.add("pdfcrop did not produce a file.");
                TextFile.Delete("tmpin.pdf");
                TextFile.Delete("tmpout.pdf");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
  
                      
}
