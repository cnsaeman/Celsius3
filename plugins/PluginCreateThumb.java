/*
 * PluginArXiv.java
 *
 * Created on 14. Juli 2007, 12:20
 *
 * complete, testing
 */

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;
import celsius.tools.Parser;
import celsius.tools.TextFile;
import celsius.tools.toolbox;
import celsius.tools.StreamGobbler;


/**
 * @author cnsaeman
 */
public class PluginCreateThumb extends Thread {

    public static final HashMap<String,String> metaData=new HashMap<String,String>() {
        {
            put("title"             ,"Create Thumbnails (epub,djvu,pdf)");
            put("author"            ,"Christian Saemann");
            put("version"           ,"1.0");
            put("help"              ,"This plugin creates a thumbnail from the first page of the pdf file.\nThe following programs are needed:\n"+
                                     "for epub: nothing\n"+
                                     "for pdf: convert (from ImageMagick)\n"+
                                     "for ps.gz: convert (from ImageMagick)\n"+
                                     "for djvu: ddjvu.");
            put("needsFirstPage"    ,"no");
            put("longRunTime"       ,"no");
            put("requiredFields"    ,"fullpath");
            put("type"              ,"manual");
            put("defaultParameters" ,"");
            put("parameter-help"    ,"none.");
        }
    };
    
    public String source;
    
    public celsius.MProperties Information;
    public ArrayList<String> Msgs;

    public void Initialize(celsius.MProperties i, ArrayList<String> m){
        Information=i; Msgs=m;
    }

    public void pdfThumb() {
        String target=Information.get("fullpath")+".jpg";
	ArrayList<String> args=new ArrayList<String>();
	args.add("convert");
	args.add("-thumbnail");
	args.add("240x240");
	args.add(source+"[0]");
	args.add(target);
        String cmdln = "-thumbnail 240x240 \""+source+"\"[0] \""+target+"\"";
        Msgs.add("MAIN>Converter command: " + cmdln);
        boolean completed=false;
        try {
	    Process p = (new ProcessBuilder(args)).start();
            //Process p = Runtime.getRuntime().exec(cmdln);
            StreamGobbler SGout=new StreamGobbler(p.getInputStream(),"Output");
            StreamGobbler SGerr=new StreamGobbler(p.getErrorStream(),"Error");
            SGout.start();
            SGerr.start();
            int m=0;
            int ret=0;
            // go through loop at most 150 times=30 secs o
            while ((!completed) && (m<100)) {
                try {
                    sleep(200); ret=p.exitValue(); completed=true;
                } catch (IllegalThreadStateException e) { m++;  }
            }
	    System.out.println("Return from external program:");
	    System.out.println(m);
            System.out.println(SGout.getOutput());
            System.out.println(SGerr.getOutput());
            System.out.println(ret);
            System.out.println(completed);
            System.out.println("-----------");
            System.out.println(SGout.getOutput());
            System.out.println(SGerr.getOutput());
        } catch (Exception e) {
            e.printStackTrace();
        }
	if (completed) 
	   if ((new File(target)).exists()) Information.put("thumbnail","/$"+target+"/$.jpg");
    }

    public void djvuThumb() {
        String target=Information.get("fullpath")+".jpg";
	ArrayList<String> args=new ArrayList<String>();
	args.add("ddjvu");
	args.add("-format=tiff");
	args.add("-page=1");
	args.add("-size=240x240");
	args.add(source);
	args.add(target+".tiff");
        Msgs.add("MAIN>Converter command: " + args.toString());
        boolean completed=false;
        try {
	    Process p = (new ProcessBuilder(args)).start();
            //Process p = Runtime.getRuntime().exec(cmdln);
            StreamGobbler SGout=new StreamGobbler(p.getInputStream(),"Output");
            StreamGobbler SGerr=new StreamGobbler(p.getErrorStream(),"Error");
            SGout.start();
            SGerr.start();
            int m=0;
            int ret=0;
            // go through loop at most 150 times=30 secs o
            while ((!completed) && (m<100)) {
                try {
                    sleep(200); ret=p.exitValue(); completed=true;
                } catch (IllegalThreadStateException e) { m++;  }
            }
	    System.out.println("Return from external program:");
	    System.out.println(m);
            System.out.println(SGout.getOutput());
            System.out.println(SGerr.getOutput());
            System.out.println(ret);
            System.out.println(completed);
            System.out.println("-----------");
            System.out.println(SGout.getOutput());
            System.out.println(SGerr.getOutput());
        } catch (Exception e) {
            e.printStackTrace();
        }
	args.clear();
	args.add("convert");
	args.add(target+".tiff");
	args.add(target);
        Msgs.add("MAIN>Converter command: " + args.toString());
        boolean completed2=false;
        try {
	    Process p = (new ProcessBuilder(args)).start();
            //Process p = Runtime.getRuntime().exec(cmdln);
	    System.out.println(args.toString());
            StreamGobbler SGout=new StreamGobbler(p.getInputStream(),"Output");
            StreamGobbler SGerr=new StreamGobbler(p.getErrorStream(),"Error");
            SGout.start();
            SGerr.start();
            int m=0;
            int ret=0;
            // go through loop at most 150 times=30 secs o
            while ((!completed2) && (m<100)) {
                try {
                    sleep(200); ret=p.exitValue(); completed2=true;
                } catch (IllegalThreadStateException e) { m++;  }
            }
	    System.out.println("Return from external program:");
	    System.out.println(m);
            System.out.println(SGout.getOutput());
            System.out.println(SGerr.getOutput());
            System.out.println(ret);
            System.out.println(completed);
            System.out.println("-----------");
            System.out.println(SGout.getOutput());
            System.out.println(SGerr.getOutput());
	    TextFile.Delete(target+".tiff");
        } catch (Exception e) {
            e.printStackTrace();
        }
	if (completed && completed2) 
	   if ((new File(target)).exists()) Information.put("thumbnail","/$"+target+"/$.jpg");
    }

    public void epubThumb() {
        try {
            boolean found=false;
            String mark=null;
            ZipFile ZF=new ZipFile(source);
            for (Enumeration<? extends ZipEntry> e=ZF.entries();e.hasMoreElements();) {
                String s=e.nextElement().getName();
                if (s.toLowerCase().startsWith("cover.")) {
                    found=true;
                    mark=s;
                } else {
                    if (!found) {
                        if (s.toLowerCase().endsWith(".png") || s.toLowerCase().endsWith(".jpg")) {
                            mark=s;
                        }
                    }
                }
            }
            if (mark!=null) {
                String type=Parser.CutFromLast(mark,".");
                String target=Information.get("fullpath")+"."+type;
                ZipEntry ze=ZF.getEntry(mark);
                InputStream fis=ZF.getInputStream(ze);
                FileOutputStream fos = new FileOutputStream(new File(target));
                byte[] buf = new byte[4096];
                int i = 0;
                while((i=fis.read(buf))!=-1) {
                    fos.write(buf, 0, i);
                }
                fis.close();
                fos.close();
                /* Rescale to 240x240
                BufferedImage bf = ImageIO.read(new File(target));
                int w = bf.getWidth();
                int h = bf.getHeight();
                double rx = 240/ w;
                double ry = 240/ h;
                double r = rx;
                if (ry < rx) {
                    r = ry;
                }
                bf = bf.getScaledInstance((int) (r * w), (int) (r * h), Image.SCALE_DEFAULT);
                ImageIO.write(scaled, type, new File(target));*/
                if ((new File(target)).exists()) Information.put("thumbnail","/$"+target+"/$.jpg");
            }
            ZF.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
  
    public void run() {
        boolean afterwards=false;
        source=Information.get("fullpath");
	if (source.endsWith("pdf")) pdfThumb();
	if (source.endsWith("ps.gz")) {
            try {
                TextFile.CopyFile(source, source+".ps.gz");
                TextFile.GUnZip(source+".ps.gz");
                source+=".ps";
                pdfThumb();
                TextFile.Delete(source);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
	if (source.endsWith("djvu")) djvuThumb();
	if (source.endsWith("epub")) epubThumb();
    }
  
                      
}
