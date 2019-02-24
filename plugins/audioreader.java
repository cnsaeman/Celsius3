/*
 * audioreader.java
 *
 * javac -classpath jaudiotagger-2.0.3.jar audioreader.java
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
public class audioreader {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        AudioFile f=AudioFileIO.read(new File(args[0]));
        Tag tag=f.getTag();
        AudioHeader h=f.getAudioHeader();
        System.out.println("START::-------------------");
        System.out.println("Length##"+f.getAudioHeader().getTrackLength());
        if (tag!=null) {
            System.out.println("tags##yes");
            System.out.println("artists##"+tag.getFirst(FieldKey.ARTIST));
            System.out.println("composer##"+tag.getFirst(FieldKey.COMPOSER));
            System.out.println("title##"+tag.getFirst(FieldKey.TITLE));
            System.out.println("album##"+tag.getFirst(FieldKey.ALBUM));
            System.out.println("comment##"+tag.getFirst(FieldKey.COMMENT));
            System.out.println("year##"+tag.getFirst(FieldKey.YEAR));
            System.out.println("albumtrack##"+tag.getFirst(FieldKey.TRACK));
            System.out.println("genre##"+tag.getFirst(FieldKey.GENRE));
            System.out.println("albumtrack##"+tag.getFirst(FieldKey.TRACK));
            System.out.println("number##"+tag.getFirst(FieldKey.TRACK));
            System.out.println("encoder##"+tag.getFirst(FieldKey.ENCODER));
       }
        System.out.println("START::-------------------");
    }
}
