//
//  RDFAuthorUtilities.java
//  RDFAuthor
//
//  Created by pldms on Wed Nov 07 2001.
//

/*
    Copyright 2001 Damian Steer <dm_steer@hotmail.com>

    This file is part of RDFAuthor.

    RDFAuthor is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    RDFAuthor is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with RDFAuthor; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

*/

// This class is just a bunch of useful methods

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

import java.lang.Math;

public class RDFAuthorUtilities {
    
    static final int Normal = 1;
    static final int Critical = 2;
    static final int Informational = 3;
    
    // The following is for error panels so that I can have cool sheets
    // (the advantage being that they don't interupt the user too much)
    // If there is no window (eg when loading fails) or the window is not visible
    // I use a modal panel.
    
    public static void ShowError(String errorTitle, String errorText, int type, NSWindow window)
    {
        if ((window == null) || (!window.isVisible()))
        {
            switch (type)
            {
                case Normal:		NSAlertPanel.runAlert(errorTitle, errorText, null, null, null);
                                        break;
                case Critical:		NSAlertPanel.runCriticalAlert(errorTitle, errorText, null, null, null);
                                        break;
                case Informational: 	NSAlertPanel.runInformationalAlert(errorTitle, errorText, null, null, null);
            }
        }
        else
        {
            switch (type)
            {
                case Normal:
                    NSAlertPanel.beginAlertSheet(errorTitle, null, null, null,
                                                    window, null, null, null, window, errorText);
                    break;
                case Critical:
                    NSAlertPanel.beginCriticalAlertSheet(errorTitle, null, null, null,
                                                    window, null, null, null, window, errorText);
                    break;
                case Informational:
                    NSAlertPanel.beginInformationalAlertSheet(errorTitle, null, null, null,
                                                    window, null, null, null, window, errorText);
            }
        }
    }
    
    /*
        this is a first stab at auto-layout. it isn't very good :-(
    */
    
    public static void layoutModel(ArcNodeList model, float minX, float minY, float maxX, float maxY)
    {
        float margin = 50;
        float springConstant = 0.01f;
        float springExtension = 150;
        int numberOfNodes = model.size(true); // 'true' means get number of nodes, 'false' for arcs
        
        // Adjust for margins
        
        minX += margin;
        maxX -= margin;
        minY += margin;
        maxY -= margin;
        
        float edgeResistance = 9; // This is for the edge resistance stuff which (hopefully) keeps the model in the frame
        float xCentreConstant = 2 * edgeResistance / (minX + maxX);
        float yCentreConstant = 2 * edgeResistance / (minY + maxY);
        
        Node[] nodes = new Node[numberOfNodes];
        float[] x = new float[numberOfNodes];
        float[] y = new float[numberOfNodes];
        float[] xVel = new float[numberOfNodes];
        float[] yVel = new float[numberOfNodes];
        boolean[][] connected = new boolean[numberOfNodes][numberOfNodes];
        
        // Set nodes
        int i = 0;
        
        for (ArcNodeListIterator iterator = model.getNodes(); iterator.hasNext();)
        {
            Node node = (Node) iterator.next();
            nodes[i] = node;
            x[i] = node.x();
            y[i] = node.y();
            xVel[i] = 0;
            yVel[i] = 0;
            i++;
        }
        
        for (i = 0; i < numberOfNodes - 1; i++)
        {
            for (int j = i + 1; j < numberOfNodes; j++)
            {
                // This is silly - improve me
                
                connected[i][j] = false;
                
                for (ArcNodeListIterator iterator = model.getArcs(); iterator.hasNext();)
                {
                    Arc arc = (Arc) iterator.next();
                    if ((arc.toNode() == nodes[i]) && (arc.fromNode() == nodes[j]))
                    {
                        connected[i][j] = true;
                        break;
                    }
                    else if ((arc.toNode() == nodes[j]) && (arc.fromNode() == nodes[i]))
                    {
                        connected[i][j] = true;
                        break;
                    }
                }
            }
        }
        
        // let's try this a few times
        
        for (int repeating = 0; repeating < 100; repeating++)
        {
        
            for (i = 0; i < numberOfNodes - 1; i++)
            {
                for (int j = i + 1; j < numberOfNodes; j++)
                {
                    // this is for calculating interactions between nodes
                    float dx = x[i] - x[j];
                    float dy = y[i] - y[j];
                    
                    float dist = (float) Math.sqrt(dx*dx + dy*dy);
                    
                    float force;
                    if (connected[i][j]) // if connected then work out springy force
                    {
                        force = - springConstant * (dist - springExtension);
                    }
                    else force = 100/dist; // else purely repulsive (but falls off with distance)
                    
                    float xForce = force * dx / dist; // x (y) components of the force
                    float yForce = force * dy / dist;
                    
                    xVel[i] += xForce;
                    xVel[j] -= xForce;
                    
                    yVel[i] += yForce;
                    yVel[j] -= yForce;
                }
            }
            
            //float totalVel = 0;
            
            for (i = 0; i < numberOfNodes; i++)
            {
                // edge adjustment
                // Roughly this uses a very strong repulsive force in the margins
                // and a weaker force which tends to centre the model.
                
                if (x[i] < minX) 
                xVel[i] += 0.001 * (minX - x[i])*(minX - x[i]) + edgeResistance;
                else xVel[i] += xCentreConstant * (minX - x[i]);
                if (x[i] > maxX)
                xVel[i] -= 0.001 * (x[i] - maxX) * (x[i] - maxX) + edgeResistance;
                else xVel[i] += - xCentreConstant * (x[i] - maxX);
                if (y[i] < minY)
                yVel[i] += 0.001 * (minY - y[i]) * (minY - y[i]) + edgeResistance;
                else yVel[i] += yCentreConstant * (minY - y[i]);
                if (y[i] > maxY) 
                yVel[i] -= 0.001 * (y[i] - maxY) * (y[i] - maxY) + edgeResistance;
                else yVel[i] += - yCentreConstant * (y[i] - maxY);
                
                xVel[i] *= 0.9; // these 'damp' the motions
                yVel[i] *= 0.9;
                
                x[i] += xVel[i];
                y[i] += yVel[i];
                
                //totalVel += Math.abs(xVel[i]);
                //totalVel += Math.abs(yVel[i]);
            }
            //System.out.println("Total velocities: " + totalVel);
        }
        
        // finished - so set postitions
        
        for (i = 0; i < numberOfNodes; i++)
        {
            nodes[i].setPosition( x[i], y[i] );
        }
    }


}
