//
// Celsius Library System
// (w) by C. Saemann
//
// DialogRegister.java
//
// This class contains the dialog for autoregistering documents.
//
// checked 16.09.2007


package celsius.Dialogs;

import celsius.Item;
import celsius.Library;
import celsius.MainFrame;
import celsius.Resources;
import celsius.Threads.ThreadRegister;
import celsius.tools.toolbox;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.*;

public class RegisterItems extends JDialog implements MouseListener,ActionListener {

    private final Resources RSC;
    private MainFrame                   MF;           // MF
    private ArrayList<ThreadRegister>   RTs;              // Register threads, die gerade laufen
    private Library                     Lib;              // Library to be used  

    private JProgressBar                JPB;               // Progress Label
    private JLabel                      jLDisplay;         // Progress Label
    private JButton                     jBtnCancel;          // Cancel Button
    
    private int         done;               // number of processed documents
    private int         newRegistrations;   // number of new registrations
    private boolean     running;            // Cancel not yet pressed
    
    private int         maxsubthreads;      // Maximal number of subthreads

    private Timer t;                        // Timer
    
    // Constructor
    public RegisterItems(MainFrame parent,Resources rsc) throws IOException {
        super(parent,"Registering documents ...",true);
        RSC=rsc;
        setIconImage(RSC.getOriginalIcon("arrow_switch_bluegreen"));
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        
        MF=parent;
        Lib=RSC.getCurrentSelectedLib();

        // Create Frame
        JPanel Pnl1=new JPanel();
        JPB=new JProgressBar();
        JPB.setMaximum(Lib.getSize());
        jLDisplay=new JLabel("New registrations: 0",JLabel.CENTER);
        jLDisplay.setMinimumSize(new Dimension(RSC.guiScale(250),RSC.guiScale(20)));
        jLDisplay.setPreferredSize(new Dimension(RSC.guiScale(250),RSC.guiScale(20)));
        jBtnCancel=new JButton("Cancel");
        jBtnCancel.addMouseListener(this);
        Pnl1.add(JPB); Pnl1.add(jLDisplay); Pnl1.add(jBtnCancel);
        add(Pnl1);
        
        // Center frame
        setSize(246,120);
        toolbox.centerDialog(this,parent);
        
        // Initialize values and start timer
        running=true;
        done=0;
        newRegistrations=0;
        Lib.Index.toLastElement();
        
        maxsubthreads=toolbox.intvalue(RSC.Configuration.maxthreads2);
        if (maxsubthreads<1) maxsubthreads=1;

        
        RTs=new ArrayList<ThreadRegister>();
        AddRT();
        t=new Timer(50,this);
        t.start();
    }
    
    // Add new Registerthread
    private void AddRT() {
        while ((RTs.size()<3) && (!Lib.Index.endReached) && running) {
            JPB.setValue(Lib.Index.position);
            ThreadRegister RT=new ThreadRegister(Lib,new Item(Lib,Lib.Index.position),RSC.Msg1);
            RT.run(); RTs.add(RT); Lib.Index.previousElement();
        }
    }
    
    // Reagiere auf MouseClicks.
    public void mouseClicked(MouseEvent ev) {
        Object object = ev.getSource();
        // Stop timer and wait for all threads to finish, if Cancel pressed
        if (object == jBtnCancel) {
            try {
                t.stop();
                for (ThreadRegister TR : RTs)
                    TR.join(5000);
            } catch (InterruptedException IE) {}
            this.setVisible(false);
            this.dispose();
        }
    }
    
    // React on timer: add new registrationthreads
    public void actionPerformed(ActionEvent ev) {
        Object object = ev.getSource();
        if  (object==t) {
            synchronized(jLDisplay) {
                // Go through threads, clean up the finished ones and add new ones
                for (int i=0;i<RTs.size();i++) {
                    ThreadRegister RT=RTs.get(i);
                    if (RT.done) {
                        RTs.remove(RT);
                        done++;
                        //if (done%1000==0) System.out.print("M"); else if (done%100==0) System.out.print("C"); else if (done%10==0) System.out.print("X"); else System.out.print(".");
                        newRegistrations+=RT.newRegistrations;
                        jLDisplay.setText("New registrations: "+Integer.toString(newRegistrations));
                        AddRT();
                    }
                }
                // If finished, wait for threads and dispose
                if (Lib.Index.endReached) {
                    try {
                        t.stop();
                        for (ThreadRegister RT : RTs)
                            RT.join(5000);
                    } catch (InterruptedException IE) {}
                    this.setVisible(false);
                    this.dispose();
                }
            }
        }
    }
    
    // leere MouseListener-Routinen
    public void mousePressed(MouseEvent e)      {;}
    public void mouseDragged(MouseEvent e)      {;}
    public void mouseReleased(MouseEvent e)     {;}
    public void mouseEntered(MouseEvent e)      {;}
    public void mouseExited(MouseEvent e)       {;}
}