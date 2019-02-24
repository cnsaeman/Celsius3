/*
 * audioreader.java
 *
 * javac -classpath "jaudiotagger-2.0.3.jar:." audiowriter.java
 *
 * Created on 4. August 2011, 22:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

import java.io.File;
import java.util.Iterator;
import org.jaudiotagger.audio.*;
import org.jaudiotagger.tag.*;

/**
 *
 * @author cnsaeman
 */
public class audiowriter {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        AudioFile f=AudioFileIO.read(new File(args[0]));
        Tag tag=f.getTag();
        if (tag!=null) {
            for (int i=1;i<args.length;i++) {
                String t=celsius.tools.Parser.CutTill(args[i], ":");
                String v=celsius.tools.Parser.CutTill(celsius.tools.Parser.CutFrom(args[i], "\""),"\"");
                if (t.equals("artists")) tag.setField(FieldKey.ARTIST,v);
                if (t.equals("composer")) tag.setField(FieldKey.COMPOSER,v);
                if (t.equals("fulltitle")) tag.setField(FieldKey.TITLE,v);
                if (t.equals("album")) tag.setField(FieldKey.ALBUM,v);
                if (t.equals("comment")) tag.setField(FieldKey.COMMENT,v);
                if (t.equals("year")) tag.setField(FieldKey.YEAR,v);
                if (t.equals("genre")) tag.setField(FieldKey.GENRE,v);
                if (t.equals("number")) tag.setField(FieldKey.TRACK,v);
                if (t.equals("encoder")) tag.setField(FieldKey.ENCODER,v);
            }
            f.commit();
       }
    }
}
