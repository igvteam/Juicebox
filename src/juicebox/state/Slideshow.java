/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2011-2015 Broad Institute, Aiden Lab
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package juicebox.state;
import juicebox.HiCGlobals;
import juicebox.MainWindow;
import juicebox.HiC;
import juicebox.windowui.RecentMenu;
import org.lwjgl.Sys;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import sun.applet.Main;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;


/**
 * Created by Zulkifl on 7/31/2015.
 */


public class Slideshow {

    private static String statesForSlideshow = HiCGlobals.xmlSavedStatesFileName;
    final static JFrame carouselFrame = new JFrame();
    final static JPanel nextPanel = new JPanel(new BorderLayout());
    final static JPanel prevPanel = new JPanel(new BorderLayout());
    final static JPanel labelPanel = new JPanel(new BorderLayout());
    final static JButton nextButton = new JButton("\u25BA");
    final static JButton prevButton = new JButton("\u25C4");



    public static void viewShow(final MainWindow mainWindow, final HiC hiC) {
         try {
             final ArrayList<String> savedStatePaths = new ArrayList<String>();
            Document dom;
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = null;
            db = dbf.newDocumentBuilder();
            dom = db.parse(statesForSlideshow);
            NodeList nodeList = dom.getElementsByTagName("STATE");
            for (int i = 0; i < nodeList.getLength(); i++) {
                savedStatePaths.add(nodeList.item(i).getAttributes().getNamedItem("SelectedPath").getNodeValue());
            }
             System.out.println(savedStatePaths);
             final int numSlides = savedStatePaths.size();

             final JLabel slideLabel = new JLabel(savedStatePaths.get(0));

             carouselFrame.setLayout(new FlowLayout());
             carouselFrame.setResizable(true);
             carouselFrame.setVisible(true);
             carouselFrame.setSize(400, 100);
             carouselFrame.add(prevPanel);
             carouselFrame.add(labelPanel);
             carouselFrame.add(nextPanel);

             prevPanel.add(prevButton,BorderLayout.EAST);
             prevPanel.setVisible(true);

             labelPanel.add(slideLabel, BorderLayout.CENTER);
             labelPanel.setVisible(true);

             nextPanel.add(nextButton,BorderLayout.WEST);
             nextPanel.setVisible(true);


             prevButton.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     int counter = savedStatePaths.indexOf(slideLabel.getText());
                     if(counter >= 0 ) {
                         counter = ((counter-1) + numSlides)%numSlides;
                         slideLabel.setText(savedStatePaths.get(counter));
                         LoadStateFromXMLFile.reloadSelectedState(savedStatePaths.get(counter),mainWindow,hiC);
                     }
                 }
             });


             nextButton.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     int counter = savedStatePaths.indexOf(slideLabel.getText());
                     if(counter < numSlides){
                         counter = (counter+1)%numSlides;
                         slideLabel.setText(savedStatePaths.get(counter));
                         LoadStateFromXMLFile.reloadSelectedState(savedStatePaths.get(counter),mainWindow,hiC);
                     }
                 }
             });


        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}