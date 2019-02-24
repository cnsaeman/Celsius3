//
// Celsius Library System
// (w) by C. Saemann
//
// eprintwebData.java
//
// This class retrieves data from eprintweb
//
// typesafe

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import celsius.tools.*;

public class eprintwebData {

    public HashMap<String, String> Info;
    public Exception except;
    public String pureAnswer1;
    public String pureAnswer2;
    public boolean retrieved;  // flag true, if everything went well

    /**
     * Constructor, give arxiv-name and arxiv-number
     */
    public eprintwebData(String arxiv, String number) {
        Info = new HashMap<String, String>();

        if (arxiv.indexOf(".") > -1) {
            arxiv = Parser.CutTill(arxiv, ".");
        }

        retrieved = false;

        try {

            pureAnswer1 = TextFile.ReadOutURL(arxivTools.eprintWebEntry(arxiv, number));
            if (!pureAnswer1.startsWith("##??")) {
                retrieved = true;
                int i1, i2, i3;
                i1 = pureAnswer1.indexOf("<td class=\"txt\"><b>");
                i2 = pureAnswer1.indexOf("<td class=\"ti\">", i1) + 15;
                i3 = pureAnswer1.indexOf("<", i2);
                String title = Parser.Substitute(pureAnswer1.substring(i2, i3), "  ", " ");
                title = Parser.Substitute(title, "  ", " ");
                Info.put("title", title);

                i1 = pureAnswer1.indexOf("<td class=\"txt\">", i3);
                i2 = pureAnswer1.indexOf("<b>Received.</b>", i1);
                String authors = pureAnswer1.substring(i1, i2);
                //System.out.println(authors);
                String a = new String("");
                String ta,tb;
                while (authors.indexOf("<a href") > -1) {
                    tb=Parser.CutFromLast(Parser.CutTill(authors, "</a>"),">");
                    ta="|"+Parser.CutFromLast(tb, " ").trim()+", "+Parser.CutTillLast(tb, " ").trim();
                    if (ta.indexOf(">") > -1) {
                        ta = Parser.CutFromLast(ta, ">");
                    }
                    a += ta;
                    authors = Parser.CutFrom(authors, "</a>");
                }
                a = a.substring(1);
                Info.put("authors", a);

                i1 = pureAnswer1.indexOf("<b>Last updated.</b>", i2) + 22;
                i2 = pureAnswer1.indexOf("</td>", i1);
                String date = pureAnswer1.substring(i1, i2).trim();
                Info.put("date", date);
                HashMap<String, String> SM = new HashMap<String, String>();
                SM.put("January", "01");
                SM.put("February", "02");
                SM.put("March", "03");
                SM.put("April", "04");
                SM.put("May", "05");
                SM.put("June", "06");
                SM.put("July", "07");
                SM.put("August", "08");
                SM.put("September", "09");
                SM.put("October", "10");
                SM.put("November", "11");
                SM.put("December", "12");
                for (String key : SM.keySet()) {
                    date = date.replaceAll(key, SM.get(key));
                }
                //System.out.println(date);
                SimpleDateFormat SDFin = new SimpleDateFormat("dd MM yyyy");
                SimpleDateFormat SDFout = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    Date d = SDFin.parse(date);
                    //System.out.println(d.toString());
                    //System.out.println(SDFout.format(d));
                    Info.put("date", SDFout.format(d));
                } catch (ParseException ex) {
                    ex.printStackTrace();
                }

                i1 = pureAnswer1.indexOf("<b>Abstract.</b>", i3) + 17;
                i2 = pureAnswer1.indexOf("</td>", i1);
                if (i1 > 16) {
                    Info.put("abstract", pureAnswer1.substring(i1, i2).trim());
                }
                i1 = pureAnswer1.indexOf("<b>Comment.</b>", i2) + 16;
                i2 = pureAnswer1.indexOf("</td>", i1);
                if (i1 > 15) {
                    Info.put("remarks", "Comments: " + pureAnswer1.substring(i1, i2).trim());
                }
                i1 = pureAnswer1.indexOf("<b>Journal-ref.</b>", i2) + 20;
                i2 = pureAnswer1.indexOf("</td>", i1);
                if (i1 > 19) {
                    Info.put("publicationinfo", pureAnswer1.substring(i1, i2).trim());
                }
            } else {
                except = new Exception("eprintweb refused answer");
            }
        } catch (Exception e) {
            except = e;
        }
    }
}