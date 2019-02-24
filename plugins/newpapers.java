import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import celsius.tools.Parser;
import celsius.tools.TextFile;
import celsius.tools.toolbox;

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
public class newpapers {

    public final static String EOP = String.valueOf((char) 12);   // EndOfPage signal
    public final static String filesep = System.getProperty("file.separator");  // EndOfLine signal
    public final static String linesep = System.getProperty("line.separator");  // EndOfLine signal
    public static ZipFile ZF;
    public static TextFile out;
    public static String md;
    public static String bd; // basedir

    public static String CutTags(String s1, String t) {
        while (s1.indexOf("<" + t) > 0) {
            s1 = Parser.CutTill(s1, "<" + t) + Parser.CutFrom(Parser.CutFrom(s1, "<" + t), ">");
        }
        while (s1.indexOf("</" + t) > 0) {
            s1 = Parser.CutTill(s1, "</" + t) + Parser.CutFrom(Parser.CutFrom(s1, "</" + t), ">");
        }
        return (s1);
    }

    public static String LowerTags(String s) {
        if (s.indexOf("<") > -1) {
            String tag = Parser.CutTill(Parser.CutFrom(s, "<"), ">");
            s = Parser.Substitute(s, "<" + tag + ">", "<" + tag.toLowerCase() + ">");
        }
        return (s);
    }

    public static String proper(String tmp) {
        tmp=Parser.Substitute(tmp, "&lt;", "<");
        tmp=Parser.Substitute(tmp, "&gt;", ">");
        return(tmp);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            String fn="New Papers - "+args[0]+".epub";
            String tmp=TextFile.ReadOutURL("http://export.arxiv.org/rss/"+args[0]);
            /*TextFile in = new TextFile("/home/cnsaeman/hep-th");
            String tmp = new String("");
            while (in.ready()) {
                tmp += in.getString() + "\n";
            }
            in.close();*/
            System.out.println("Bytes received: "+String.valueOf(tmp.length()));
            String ttitle = Parser.CutTill(fn,".");
            String author = "arXiv.org";
            System.out.println("Using title: " + ttitle);
            System.out.println("Using author: " + author);
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(fn));
            out.putNextEntry(new ZipEntry("mimetype"));
            out.write(new String("application/epub+zip").getBytes());
            out.closeEntry();
            out.putNextEntry(new ZipEntry("META-INF/container.xml"));
            out.write(new String("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<container xmlns=\"urn:oasis:names:tc:opendocument:xmlns:container\" version=\"1.0\">\n" +
                    "<rootfiles>\n" +
                    "<rootfile full-path=\"content.opf\" media-type=\"application/oebps-package+xml\" />\n" +
                    "</rootfiles>\n</container>\n").getBytes());
            out.closeEntry();
            out.putNextEntry(new ZipEntry("text1.html"));
            out.write(new String("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \n" +
                    "  \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\"> \n" +
                    "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\">").getBytes());
            out.write(new String("<body>").getBytes());
            out.write(new String("<h1>New Papers from the arXiv</h1>").getBytes());
            String subject=Parser.CutTill(Parser.CutFrom(tmp,"<dc:subject>"),"</dc:subject>");
            out.write(new String("<h2>Subject area: "+subject+"</h2>").getBytes());
            String date=Parser.CutTill(Parser.CutFrom(tmp,"<dc:date>"),"</dc:date>");
            out.write(new String("<h2>Date: "+date+"</h2>").getBytes());
            tmp=Parser.CutFrom(tmp,"<item rdf:about=\"");
            String title,authors,abs;
            while (tmp.length()>0) {
                out.write(new String("<br/><br/>").getBytes());
                title=Parser.CutTill(Parser.CutFrom(tmp,"<title>"),"</title>");
                authors=Parser.CutTill(Parser.CutFrom(tmp,"<dc:creator>"),"</dc:creator>");
                while (authors.indexOf("&lt;")>-1) {
                    authors=Parser.CutTill(authors,"&lt;")+Parser.CutFrom(authors,"&gt;");
                }
                abs=Parser.CutFrom(Parser.CutTill(Parser.CutFrom(tmp,"<description"),"</description>"),">");
                out.write(new String("<h2>"+title+"</h2>").getBytes());
                out.write(new String("<p>"+authors+"</p>").getBytes());
                out.write(new String(Parser.CutTags(proper(abs))).getBytes());
                tmp=Parser.CutFrom(tmp,"<item rdf:about=\"");
            }
            out.closeEntry();
            out.putNextEntry(new ZipEntry("content.opf"));
            out.write(new String("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<package xmlns=\"http://www.idpf.org/2007/opf\"\n" +
                    "   version=\"2.0\"\n    unique-identifier=\"etextno\">\n" +
                    "   <metadata xmlns:opf=\"http://www.idpf.org/2007/opf\" xmlns:dcterms=\"http://purl.org/dc/terms/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                    "     <dc:creator opf:file-as=\"" + author + "\">" + author + "</dc:creator>\n" +
                    "     <dc:language xsi:type=\"dcterms:RFC3066\">Language</dc:language>\n" +
                    "     <dc:title>" + ttitle + "</dc:title>\n" +
                    "     <dc:identifier id=\"BookId\">id_Hello_World</dc:identifier>\n" +
                    "     <dc:date opf:event=\"conversion\">conversion date</dc:date>\n" +
                    "   </metadata>\n" +
                    "   <manifest>\n" +
                    "     <item id=\"ncx\" href=\"toc.ncx\" media-type=\"application/x-dtbncx+xml\"/>\n").getBytes());
            out.write(new String("     <item id=\"file1\" href=\"text1.html\" media-type=\"application/xhtml+xml\"/>\n").getBytes());
            out.write(new String("   </manifest>\n" +
                    "   <spine toc=\"ncx\">\n").getBytes());
            out.write(new String("     <itemref idref=\"file1\"  linear=\"yes\" />\n").getBytes());
            out.write(new String("   </spine>\n" +
                    "</package>").getBytes());
            out.closeEntry();
            out.putNextEntry(new ZipEntry("toc.ncx"));
            out.write(new String("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "  <!DOCTYPE ncx \n" +
                    "    PUBLIC \"-//NISO//DTD ncx 2005-1//EN\" \"http://www.daisy.org/z3986/2005/ncx-2005-1.dtd\">\n" +
                    "<ncx\n" +
                    "    xmlns=\"http://www.daisy.org/z3986/2005/ncx/\"\n" +
                    "    version=\"2005-1\"\n" +
                    "    xml:lang=\"en\">\n" +
                    "    <head>\n" +
                    "      <meta name=\"dc:Title\" content=\"" + ttitle + "\"/>\n" +
                    "      <meta name=\"dtb:uid\" content=\"id_Hello_World\"/>\n" +
                    "    </head>\n" +
                    "    <docTitle>\n" +
                    "      <text>Hello World</text>\n" +
                    "    </docTitle>\n" +
                    "    <navMap>\n" +
                    "      <navPoint playOrder=\"1\" id=\"id_Hello_World_01\">\n" +
                    "        <navLabel>\n" +
                    "          <text>Start</text>\n" +
                    "        </navLabel>\n" +
                    "        <content src=\"text0.html\"/>\n" +
                    "      </navPoint>\n" +
                    "    </navMap>\n" +
                    "  </ncx>").getBytes());
            out.closeEntry();
            out.close();
            TextFile.CopyFile(fn, "/media/disk/database/media/books/"+fn);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println(ex.toString());
        }
    }
}
