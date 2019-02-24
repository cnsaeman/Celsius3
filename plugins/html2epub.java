import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import celsius.tools.TextFile;
import celsius.tools.Parser;

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
public class html2epub {

    public final static String EOP = String.valueOf((char) 12);   // EndOfPage signal
    public final static String filesep = System.getProperty("file.separator");  // EndOfLine signal
    public final static String linesep = System.getProperty("line.separator");  // EndOfLine signal
    public static ZipFile ZF;
    public static TextFile out;
    public static String md;
    public static String bd; // basedir

    public static String CutTags(String s1,String t) {
      while (s1.indexOf("<"+t)>0) {
	s1=Parser.CutTill(s1,"<"+t)+Parser.CutFrom(Parser.CutFrom(s1,"<"+t),">");
      }
      while (s1.indexOf("</"+t)>0) {
	s1=Parser.CutTill(s1,"</"+t)+Parser.CutFrom(Parser.CutFrom(s1,"</"+t),">");
      }
      return(s1);
    }

    public static String LowerTags(String s) {
        if (s.indexOf("<")>-1) {
            String tag=Parser.CutTill(Parser.CutFrom(s,"<"),">");
            s=Parser.Substitute(s,"<"+tag+">","<"+tag.toLowerCase()+">");
        }
        return(s);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length!=2) {
            System.out.println("Call: java html2epub 'in-title (in-author, in-author-prenome).html' out.epub");
            System.exit(0);
        }
        try {
            String in=args[0];
            String author=args[0];
            if (author.indexOf(filesep)>-1) author=Parser.CutFromLast(author,filesep);
            String title=Parser.CutTill(author,"(").trim();
            author=Parser.CutTill(Parser.CutFrom(author,"("),")").trim();
            System.out.println("Using title: "+title);
            System.out.println("Using author: "+author);
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(args[1]));
            out.putNextEntry(new ZipEntry("mimetype"));
            out.write(new String("application/epub+zip").getBytes());
            out.closeEntry();
            out.putNextEntry(new ZipEntry("META-INF/container.xml"));
            out.write(new String("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
                    "<container xmlns=\"urn:oasis:names:tc:opendocument:xmlns:container\" version=\"1.0\">\n"+
                    "<rootfiles>\n"+
                    "<rootfile full-path=\"content.opf\" media-type=\"application/oebps-package+xml\" />\n"+
                    "</rootfiles>\n</container>\n").getBytes());
            out.closeEntry();
            int no=0;
            int pos=0;
            TextFile sin=new TextFile(in);
            String r;
            boolean write=false;
            boolean close=false;
            out.putNextEntry(new ZipEntry("text"+String.valueOf(no)+".html"));
            out.write(new String("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
                    "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \n"+
                    "  \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\"> \n"+
                    "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\">").getBytes());
            while (sin.ready()) {
                r=sin.getString();
                r=LowerTags(r);
                r=Parser.Substitute(r, "<br>", "<br/>");
		if (r.indexOf("<body ")>0) {
		    r=Parser.CutTill(r,"<body")+"<body>"+Parser.CutFrom(Parser.CutFrom(r,"<body"),">");
		}
		r=CutTags(r,"font");
                if (write) {
		    System.out.println(r);
                    out.write(new String(r+"\n").getBytes());
                    pos+=r.length()+1;
                }
                if (r.trim().startsWith("<html")) write=true;
                if (r.trim().startsWith("<HTML")) write=true;
                if (pos>100000) close=true;
                if (close && r.endsWith("</p>")) {
                    out.write(new String("</body></html>").getBytes());
                    out.closeEntry();
                    no++;
                    out.putNextEntry(new ZipEntry("text"+String.valueOf(no)+".html"));
                    out.write(new String("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
                            "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \n"+
                            "  \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\"> \n"+
                            "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\"><body>").getBytes());
                    close=false;
                    pos=0;
                }
            }
            out.closeEntry();
            sin.close();
            out.putNextEntry(new ZipEntry("content.opf"));
            out.write(new String("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
                    "<package xmlns=\"http://www.idpf.org/2007/opf\"\n" +
                    "   version=\"2.0\"\n    unique-identifier=\"etextno\">\n" +
                    "   <metadata xmlns:opf=\"http://www.idpf.org/2007/opf\" xmlns:dcterms=\"http://purl.org/dc/terms/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                    "     <dc:creator opf:file-as=\""+author+"\">"+author+"</dc:creator>\n" +
                    "     <dc:language xsi:type=\"dcterms:RFC3066\">Language</dc:language>\n" +
                    "     <dc:title>"+title+"</dc:title>\n" +
                    "     <dc:identifier id=\"BookId\">id_Hello_World</dc:identifier>\n" +
                    "     <dc:date opf:event=\"conversion\">conversion date</dc:date>\n" +
                    "   </metadata>\n" +
                    "   <manifest>\n" +
                    "     <item id=\"ncx\" href=\"toc.ncx\" media-type=\"application/x-dtbncx+xml\"/>\n").getBytes());
            no++;
            for (int i=0;i<no;i++) {
                out.write(new String("     <item id=\"file"+String.valueOf(i)+"\" href=\"text"+String.valueOf(i)+".html\" media-type=\"application/xhtml+xml\"/>\n").getBytes());
            }
            out.write(new String("   </manifest>\n" +
                    "   <spine toc=\"ncx\">\n").getBytes());
            for (int i=0;i<no;i++) {
                out.write(new String("     <itemref idref=\"file"+String.valueOf(i)+"\"  linear=\"yes\" />\n").getBytes());
            }
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
                    "      <meta name=\"dc:Title\" content=\""+title+"\"/>\n" +
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
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println(ex.toString());
        } 
    }
}
