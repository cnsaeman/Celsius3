/*
 * tester.java
 *
 * Created on 13. Oktober 2007, 20:52
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

import celsius.tools.TextFile;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author cnsaeman
 */
public class tester {

    /** Creates a new instance of tester */
    public tester() {
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        TextFile TF=new TextFile("/home/cnsaeman/Desktop/test.bib");
        StringBuffer SB=new StringBuffer();
        while (TF.ready())
            SB.append(TF.getString());
        TF.close();
        /*String cont=SB.toString();
        Pattern p=Pattern.compile("@(.+?){S\\-\\s?11\\s+?(\\S.+\\d\\d)\\s\\s\\s<a.+?href=\"(.+?)\">(.+?)</a>");
        Matcher m = p.matcher(FileName);
        int i=0;
        while (m.find(i)) {
            System.out.println(m.groupCount());
            System.out.println(m.group(1));
            i=m.end();
        }


            String FileName="       S-11                24 Dec 96   <a target=\"_blank\" href=\"http://www.tv.com/father-ted/a-christmassy-ted/episode/52897/summary.html\">A Christmassy Ted</a>";
            //Pattern p = Pattern.compile("<strong>(.+)</strong></a> as (.+)</li>");
            //        Pattern p = Pattern.compile("2"+"\\-\\s?"+"5"+"\\s+?\\d+\\s+(.+)\\s\\s\\s<a target=\"_blank\" href=\"(.+?)\">(.+?)</a>");
            //Pattern p = Pattern.compile("1"+"\\-.?"+"4"+".+?\\d+\\s+(.+)\\s\\s\\s<a target=\"_blank\" href=\"(.+?)\">(.+?)</a>");
            Matcher m = p.matcher(FileName);
            int i=0;
            while (m.find(i)) {
                System.out.println(m.groupCount());
                System.out.println(m.group(1));
                System.out.println(m.group(2));
                System.out.println(m.group(3));
                i=m.end();
            }
            /*String FileName="       S-11                24 Dec 96   <a target=\"_blank\" href=\"http://www.tv.com/father-ted/a-christmassy-ted/episode/52897/summary.html\">A Christmassy Ted</a>";
            //Pattern p = Pattern.compile("<strong>(.+)</strong></a> as (.+)</li>");
            //        Pattern p = Pattern.compile("2"+"\\-\\s?"+"5"+"\\s+?\\d+\\s+(.+)\\s\\s\\s<a target=\"_blank\" href=\"(.+?)\">(.+?)</a>");
            //Pattern p = Pattern.compile("1"+"\\-.?"+"4"+".+?\\d+\\s+(.+)\\s\\s\\s<a target=\"_blank\" href=\"(.+?)\">(.+?)</a>");
                        Pattern p=Pattern.compile("S\\-\\s?11\\s+?(\\S.+\\d\\d)\\s\\s\\s<a.+?href=\"(.+?)\">(.+?)</a>");
            Matcher m = p.matcher(FileName);
            int i=0;
            while (m.find(i)) {
                System.out.println(m.groupCount());
                System.out.println(m.group(1));
                System.out.println(m.group(2));
                System.out.println(m.group(3));
                i=m.end();
            }
             */
            /*System.out.println("NO:");
            System.out.println(m.find());
            System.out.println(m.start());
            System.out.println("ONO:");*/

        /*try {
            Socket server = new Socket("www.imdb.com", 80);
            OutputStream out = server.getOutputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader( server.getInputStream()) );

            //GET-Kommando senden
            String req = "GET /find?s=all&q=neverending+story HTTP/1.0\r\n";
            req+= "Host: www.imdb.com\r\n";
            req+= "User-Agent: Mozilla/5.0 (Windows; U; Windows NT 5.1; de; rv:1.8.0.7) Gecko/20060909 Firefox/1.5.0.7" + "\r\n" + "\r\n";

            out.write(req.getBytes());

            int len;
            StringBuffer output=new StringBuffer("");
            try {
                for ( String line; (line = in.readLine()) != null; ) {
                    output.append(line);
                    output.append('\n');
                }
            } catch (Exception e) {
                System.out.println(e.toString());
            }
            System.out.println(output.toString());

            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }
}
