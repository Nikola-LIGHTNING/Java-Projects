package bg.tsvetkov;

import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import javax.sound.midi.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class BeatBox implements Serializable {
    JPanel mainPanel;
    JFrame theFrame;
    JList incomingList;
    JTextField userMessage;
    ArrayList<JCheckBox> checkBoxList;
    int nextNum;
    Vector<String> listVector = new Vector<String>();
    String userName;
    ObjectOutputStream out;
    ObjectInputStream in;
    HashMap<String, boolean[]> otherSeqsMap = new HashMap<String, boolean[]>();
    
    Sequencer sequencer;
    Sequence sequence;
    Sequence mySequence = null;
    Track track;
    
    
    String[] instrumentNames = {
        "Bass Drum", "Closed Hi-Hat", "Open Hi-Hat", "Acoustic Snare",
        "Crash Cymbal", "Hand Clap", "High Tom", "Hi Bongo",
        "Maracas", "Whistle", "Low Conga", "Cowbell", 
        "Vibraslap", "Low-mid Tom", "High Agogo", "Open Hi Conga"
    };
    int[] instruments = { 35, 42, 46, 38, 49,
        39, 50, 60, 70, 72, 64, 56, 58, 47, 67, 63};
    
    public static void main(String[] args) {
        String userN = validateUserName(args);
        new BeatBox().startUp(userN); // args[0] is your user ID/screen name
    }
    
    private static String validateUserName(String[] arg) {
        if(arg.length == 0) {
            return "Unnamed ";            
        }
        return arg[0];
    }
    
    public void startUp(String name) {
        userName = name;        
        try {
            Socket sock = new Socket("127.0.0.1", 4242);
            out = new ObjectOutputStream(sock.getOutputStream());
            in = new ObjectInputStream(sock.getInputStream());
            
            Thread remote = new Thread(new RemoteReader());
            remote.start();
            
            System.out.println("Conection Established");
        } catch(Exception ex) {
            ex.printStackTrace();
        }        
        setUpMidi();
        buildGUI();               
    }
    
    public void buildGUI() {
        theFrame = new JFrame("Cyber BeatBox");
        theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        BorderLayout layout = new BorderLayout();
        JPanel background = new JPanel(layout);
        background.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        checkBoxList = new ArrayList<JCheckBox>();
        Box buttonBox = new Box(BoxLayout.Y_AXIS);
        
        JButton start = new JButton("Start");
        start.addActionListener(new MyStartListener());
        buttonBox.add(start);
        
        JButton stop = new JButton("Stop");
        stop.addActionListener(new MyStopListener());
        buttonBox.add(stop);
        
        JButton upTempo = new JButton("Tempo Up");
        upTempo.addActionListener(new MyUpTempoListener());
        buttonBox.add(upTempo);
        
        JButton downTempo = new JButton("Tempo Down");
        downTempo.addActionListener(new MyDownTempoListener());
        buttonBox.add(downTempo);
        
        JButton buttonSend = new JButton("Send");
        buttonSend.addActionListener(new MySendListener());
        buttonBox.add(buttonSend);
        
        JButton buttonLoad = new JButton("Load");
        buttonLoad.addActionListener(new MyReadInListener());
        buttonBox.add(buttonLoad);
        
        userMessage = new JTextField();
        buttonBox.add(userMessage);
        
        incomingList = new JList();
        incomingList.addListSelectionListener(new MyListSelectionListener());
        incomingList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane theList = new JScrollPane(incomingList);
        buttonBox.add(theList);
        incomingList.setListData(listVector);
        
        Box nameBox = new Box(BoxLayout.Y_AXIS);
        for(int i = 0; i < 16; i++) {
            nameBox.add(new Label(instrumentNames[i]));
        }
        
        background.add(BorderLayout.EAST, buttonBox);
        background.add(BorderLayout.WEST, nameBox);
        
        theFrame.getContentPane().add(background);
        
        GridLayout grid = new GridLayout(16, 16);
        grid.setVgap(1);
        grid.setHgap(2);
        mainPanel = new JPanel(grid);
        background.add(BorderLayout.CENTER, mainPanel);
        
        for(int i = 0; i < 256; i++) {
            JCheckBox c = new JCheckBox();
            c.setSelected(false);
            checkBoxList.add(c);
            mainPanel.add(c);
        }
        
        setUpMidi();
        
        theFrame.setBounds(50, 50, 300, 300);
        theFrame.pack();
        theFrame.setVisible(true);
    }
    
    public void setUpMidi() {
        try {
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequence = new Sequence(Sequence.PPQ, 4);
            track = sequence.createTrack();
            sequencer.setTempoInBPM(120);
        } catch(Exception e) {
            e.printStackTrace();
        }            
    }
    
    public void buildTrackAndStart() {
        ArrayList<Integer> trackList = null; 
        
        // Deleting the old track(if there is one), making a new one
        sequence.deleteTrack(track);
        track = sequence.createTrack();
        
        for(int i = 0; i < 16; i++) {
            // for each of the 16 instruments (i.e. Bass, Congo, etc.)
            trackList = new ArrayList<Integer>();
            
           
            
            for(int j = 0; j < 16; j++) {
                JCheckBox jc = (JCheckBox) checkBoxList.get(j + (16 * i));
                if(jc.isSelected()) {
                    // key that represents which instrument this is
                    // the instruments array hold the actual MIDI number for each instrument
                    int key = instruments[i];
                    trackList.add(new Integer(key));
                } else {
                    trackList.add(null); // because this slot should be empty in the track
                }
            }  
            
            makeTracks(trackList); 
        }
        
        track.add(makeEvent(192, 9, 1, 0, 15)); // so we always go full 16 beats
        try {
            sequencer.setSequence(sequence);
            sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);
            sequencer.start();
            sequencer.setTempoInBPM(120);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public class MyStartListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            buildTrackAndStart();
        }
    }
    public class MyStopListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            sequencer.stop();
        }
    }
    public class MyUpTempoListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float)(tempoFactor * 1.03));
        }
    }
    public class MyDownTempoListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float)(tempoFactor * 0.97));
        }
    }
    
    public void makeTracks(ArrayList list) {
        Iterator it = list.iterator();
        for(int i = 0; i < 16; i++) {
            Integer num = (Integer) it.next();
            
            if(num != null) {
                int numKey = num.intValue();
                track.add(makeEvent(144, 9, numKey, 100, i));
                track.add(makeEvent(128, 9, numKey, 100, i + 1));
            }
        }
    }
    
    public MidiEvent makeEvent(int comd, int chan, int one, int two, int tick) {
        MidiEvent event = null;
        try {
            ShortMessage a = new ShortMessage();
            a.setMessage(comd, chan, one, two); 
            event = new MidiEvent(a, tick);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return event;
    }
    
    public class MySendListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            //array list of the state of the checkBoxes
            boolean[] checkBoxState = new boolean[256];
            for(int i = 0; i < 256; i++) {
                JCheckBox check = (JCheckBox) checkBoxList.get(i);
                if(check.isSelected()) {
                    checkBoxState[i] = true;
                }          
            }
            String messageToSend = null;
            try {                
                out.writeObject(userName + nextNum++ + ": " + userMessage.getText());
                out.writeObject(checkBoxState);
            } catch (Exception ex) {
                System.out.println("Sorry, dude. Could not send this to the server!");
                ex.printStackTrace();
            }
            userMessage.setText("");
        }
    }
    
    public class MyListSelectionListener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent event) {
            if(!event.getValueIsAdjusting()) {
                String selected = (String) incomingList.getSelectedValue();
                if(selected != null) {
                    boolean[] selectedState = (boolean[]) otherSeqsMap.get(userName); // BIG MISTAKE - it must be "userName" instead of "selected"
                    changeSequence(selectedState);
                    sequencer.stop();
                    buildTrackAndStart();
                }
            }
                
        }
        
    }
    
    public class RemoteReader implements Runnable {
        boolean[] checkBoxState = null;
        String nameToShow = null;
        Object obj = null;
        @Override
        public void run() {
            try {
                while((obj = in.readObject()) != null) {
                    System.out.println("got an object from the server!");
                    System.out.println(obj.getClass());
                    String nameToShow = (String) obj;
                    checkBoxState = (boolean[]) in.readObject();
                    otherSeqsMap.put(userName, checkBoxState);
                    listVector.add(nameToShow);
                    incomingList.setListData(listVector);                    
                } 
            } catch (Exception ex) {
                ex.printStackTrace();                
            }
        }
    }
    
    public class MyPlayMineListener {
        public void actionPerformed(ActionEvent ev) {
            if(mySequence != null) {
                sequence = mySequence; // restore to my original
            }
        }
    }
    
    public void changeSequence(boolean[] checkBoxState) {
        for(int i = 0; i < 256; i++) {
            JCheckBox check = (JCheckBox) checkBoxList.get(i);
            check.setSelected(checkBoxState[i]);            
        }        
    }
    
   
    
    private void saveFile(File file) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            
            for(JCheckBox check : checkBoxList) {            
                writer.write(check.isSelected() + "/");
            }
            writer.close();
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public class MyReadInListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            boolean[] checkBoxState = null;
            try {
                JFileChooser loadF = new JFileChooser();
                loadF.showOpenDialog(theFrame);
                loadFile(loadF.getSelectedFile());                                        
            } catch (Exception ex) {
                ex.printStackTrace();
            }                        
        }
    }
    
    private void loadFile(File file) {
        String line;
        String[] result = null;
        try {            
            BufferedReader reader = new BufferedReader(new FileReader(file));     
            while((line = reader.readLine()) != null) {
                result = line.split("/");
            }
            
            for(int i = 0; i < 256; i++) {                
                if(result[i].equals("true")) {
                    checkBoxList.get(i).setSelected(true);                    
                } else {
                    checkBoxList.get(i).setSelected(false);
                }                    
            }
            reader.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
