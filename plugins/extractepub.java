import java.io.IOException;
import celsius.tools.*;

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
public class extractepub {

    public final static String EOP = String.valueOf((char) 12);   // EndOfPage signal
    public final static String filesep = System.getProperty("file.separator");  // EndOfLine signal
    public final static String linesep = System.getProperty("line.separator");  // EndOfLine signal
    public static TextFile out;

    public static void safeWrite(String k,String v) throws IOException {
        if ((v!=null) && (v.length()>0)) {
            v=Parser.decodeHTML(v);
            v=Parser.CutTags(v);
            out.putString(k+":"+Parser.Substitute(v,linesep," "));
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            epubfile ef=new epubfile(args[0]);
            out = new TextFile(args[1], false);
            if (ef.MetaData.size()>0) {
                out.putString("$$metadata");
                out.putString("type:eBook");
                for(epubfile.XMLentry ent : ef.MetaData) {
                    if (ent.tag.equals("dc:title")) safeWrite("title",ent.value);
                    if (ent.tag.equals("dc:date")) safeWrite("date",ent.value);
                    if (ent.tag.equals("dc:publisher")) safeWrite("publisher",ent.value);
                    if (ent.tag.equals("dc:language")) safeWrite("language",ent.value);
                    if (ent.tag.equals("dc:subject")) safeWrite("subject",ent.value);
                    if (ent.tag.equals("dc:abstract")) safeWrite("abstract",ent.value);
                    if (ent.tag.equals("dc:identifier")) safeWrite("identifier",ent.value);
                    if (ent.tag.equals("dc:description")) safeWrite("abstract",ent.value);
                    if (ent.tag.equals("dc:creator")) {
                        String aut=ent.getProperty("file-as");
                        if ((aut==null) || (aut.equals(""))) aut=ent.value;
                        safeWrite("authors",aut);
                    }
                }
                out.putString("metadata$$");
            }
            out.putString(ef.containedText());
            out.close();
        } catch (Exception ex) {
	    ex.printStackTrace();
            System.out.println(ex.toString());
        }
    }
}
