//
// Celsius Library System
// (w) by C. Saemann
//
// ThreadRegistrar.java
//
// This class reflects a bibtex record
//
// typesafe
//
// checked:
// 11/2009

package celsius.Threads;

import celsius.Item;
import celsius.ItemTable;
import java.util.ArrayList;
import javax.swing.SwingUtilities;

/**
 *
 * @author cnsaeman
 */
public class ThreadRegistrar extends Thread {

        final ItemTable DT;
        private final ArrayList<Item> Queue;
        public boolean end;

        public ThreadRegistrar(ItemTable dt) {
            super();
            DT=dt;
            Queue=new ArrayList<Item>();
            this.setPriority(Thread.MIN_PRIORITY);
            end=false;
        }

        public void add(Item doc) {
            if (!isInterrupted()) Queue.add(doc);
        }

        public void clear() {
            Queue.clear();
        }

    @Override
        public void run() {
            // Clear DT
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    DT.clear();
                }
            });

            while ((!isInterrupted()) && (!(end && Queue.isEmpty()))) {
                if (Queue.isEmpty()) {
                    try {
                        sleep(50);
                    } catch (InterruptedException ex) { }
                } else {
                    synchronized(Queue) {
                        if (!Queue.isEmpty()) {
                            final Item doc = Queue.get(0);
                            Queue.remove(0);
                            if (doc != null) {
                                SwingUtilities.invokeLater(new Runnable() {
                                    public void run() {
                                        DT.addItem(doc);
                                        DT.resizeTable(false);
                                    }
                                });
                            } else {
                                SwingUtilities.invokeLater(new Runnable() {
                                    public void run() {
                                        DT.resizeTable(true);
                                    }
                                });
                            }
                        }
                    }
                }
            }
        }

}
