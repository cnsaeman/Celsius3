/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package celsius;

import java.util.Iterator;

/**
 *
 * @author cnsaeman
 */
public class LibraryIterator implements Iterator<Item> {

    int pos;
    int max;
    Library Lib;

    public LibraryIterator (Library lib) {
        pos=-1;
        Lib=lib;
        max=lib.getSize()-1;
    }

    public boolean hasNext() {
        return(pos<max);
    }

    public Item next() {
        pos++;
        return(new Item(Lib,pos));
    }

    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
