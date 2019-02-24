import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import celsius.tools.Parser;
import celsius.tools.TextFile;

/*
 * tester.java
 *
 * Created on 13. Oktober 2007, 20:52
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 *
 * @author cnsaeman
 */
public class extractwar {
    
    /** Creates a new instance of tester */
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        GZIPInputStream fis = null;
        try {
            String s = args[0];
            String t = args[1];
            fis = new GZIPInputStream(new FileInputStream(new File(s)));
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            String currentLine;
            while (((currentLine = br.readLine()) != null) && (currentLine.indexOf("index.html")<0)) {
            }
            String pre=new String("");
            while (((currentLine = br.readLine()) != null) && (currentLine.toLowerCase().indexOf("<body")<0)) {
                if (currentLine.toLowerCase().indexOf("<meta name=\"description\"")>-1) {
                    pre+="$$description$$\n";
                    currentLine=Parser.CutFrom(currentLine,"\"");
                    currentLine=Parser.CutFrom(currentLine,"\"");
                    currentLine=Parser.CutFrom(currentLine,"\"");
                    while (!(currentLine.indexOf("\">")>-1)) currentLine+=" "+br.readLine().trim();
                    pre+=Parser.CutTill(currentLine,"\">")+"$$\n";
                }
                if (currentLine.toLowerCase().indexOf("<meta name=\"keywords\"")>-1) {
                    pre+="$$keywords$$\n";
                    currentLine=Parser.CutFrom(currentLine,"\"");
                    currentLine=Parser.CutFrom(currentLine,"\"");
                    currentLine=Parser.CutFrom(currentLine,"\"");
                    while (!(currentLine.indexOf("\">")>-1)) currentLine+=" "+br.readLine().trim();
                    pre+=Parser.CutTill(currentLine,"\">")+"$$\n";
                }
                if (currentLine.toLowerCase().indexOf("<title>")>-1) {
                    pre+="$$title$$\n";
                    currentLine=Parser.CutFrom(currentLine.toLowerCase(),"<title>");
                    while (!(currentLine.indexOf("</title>")>-1)) currentLine+=" "+br.readLine().trim().toLowerCase();
                    pre+=Parser.LowerEndOfWords(Parser.CutTill(currentLine,"</title>"))+"$$\n";
                }
            }
            currentLine = br.readLine();
            TextFile TD=new TextFile(t,false);
            if (!pre.equals("")) {
                TD.putString(pre);
            }
            while (((currentLine = br.readLine()) != null) && (currentLine.toLowerCase().indexOf("</html>")<0)) {
                currentLine=Parser.CutTags(currentLine).trim();
                currentLine=currentLine.replaceAll("&nbsp;", " ");
                currentLine=currentLine.replaceAll("&gt;", ">");
                currentLine=currentLine.replaceAll("&amp;", "&");
                if (!currentLine.equals("")) TD.putString(currentLine);
            }
            TD.putString(currentLine);
            TD.close();
            fis.close();
            isr.close();
            br.close();
        } catch (Exception ex) {
            Logger.getLogger(extractwar.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fis.close();
            } catch (IOException ex) {
                Logger.getLogger(extractwar.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
    
}
