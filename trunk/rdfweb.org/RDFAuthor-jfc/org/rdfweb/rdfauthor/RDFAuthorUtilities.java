//
//  RDFAuthorUtilities.java
//  RDFAuthor
//
//  Created by pldms on Wed Nov 07 2001.
//

/* $Id: RDFAuthorUtilities.java,v 1.1.1.1 2002-04-09 12:49:40 pldms Exp $ */

/*
    Copyright 2001, 2002 Damian Steer <dm_steer@hotmail.com>

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

/**
 * Ported from Cocoa original. Very few changes needed (this was the
 * plan, I should add).
 **/

package org.rdfweb.rdfauthor;

import javax.swing.*;
import java.awt.*;

import java.lang.Math;
import java.util.HashMap;
import java.io.*;

import org.apache.xerces.utils.URI;

import org.rdfweb.rdfauthor.model.*;

public final class RDFAuthorUtilities {
    
    static final int Normal = JOptionPane.WARNING_MESSAGE;
    static final int Critical = JOptionPane.ERROR_MESSAGE;
    static final int Informational = JOptionPane.INFORMATION_MESSAGE;

  /**
   * This method is pretty basic - indeed it just wraps JOptionPane -
   * but it was useful for cross platform porting.
   **/
    
  public static void ShowError(String errorTitle, String errorText,
			       int type, Component parent)
    {
      JOptionPane.showMessageDialog(parent, errorText, errorTitle,
				    type);
    }
    
    /*
        This uses Xerces to validate URIs - useful for input validation
    */
    
    public static boolean isValidURI(String uriToCheck)
    {
        try
        {
            URI theUri = new URI(uriToCheck);
        }
        catch (URI.MalformedURIException e)
        {
            return false;
        }
        
        return true;
    }
    
    /*
        this is a first stab at auto-layout. it isn't very good :-(
    */
    
    public static void layoutModel(ArcNodeList model, float minX, float minY, float maxX, float maxY)
    {
        long startTime = System.currentTimeMillis();
        float margin = 50;
        float springConstant = 0.01f;
        float springExtension = 150;
        int numberOfNodes = model.size(true); // 'true' means get number of nodes, 'false' for arcs
        
        // Adjust for margins
        
        minX += margin;
        maxX -= margin;
        minY += margin;
        maxY -= margin;
        
        springExtension = (float) Math.sqrt((maxX - minX) * (maxY - minY) / numberOfNodes);
        
        float edgeResistance = 6; // This is for the edge resistance stuff which (hopefully) keeps the model in the frame
        float xCentreConstant = 2 * edgeResistance / (minX + maxX);
        float yCentreConstant = 2 * edgeResistance / (minY + maxY);
        
        Node[] nodes = new Node[numberOfNodes];
        HashMap nodeToIndex = new HashMap();
        float[] x = new float[numberOfNodes];
        float[] y = new float[numberOfNodes];
        float[] xVel = new float[numberOfNodes];
        float[] yVel = new float[numberOfNodes];
        boolean[][] connected = new boolean[numberOfNodes][numberOfNodes];
        
        // Set nodes
        int i = 0;
        int j;
        
        for (ArcNodeListIterator iterator = model.getNodes(); iterator.hasNext();)
        {
            Node node = (Node) iterator.next();
            nodes[i] = node;
            nodeToIndex.put(node, new Integer(i));
            x[i] = node.x();
            y[i] = node.y();
            xVel[i] = 0;
            yVel[i] = 0;
            i++;
        }
        
        // fill in connected[][] - true if i and j are connected
        
        for (ArcNodeListIterator iterator = model.getArcs(); iterator.hasNext();)
        {
            Arc arc = (Arc) iterator.next();
            i = ((Integer) nodeToIndex.get(arc.toNode())).intValue();
            j = ((Integer) nodeToIndex.get(arc.fromNode())).intValue();
            connected[i][j] = true;
            connected[j][i] = true;
        }
        
        // let's try this a few times
        
        for (int repeating = 0; repeating < 200; repeating++)
        {
        
            for (i = 0; i < numberOfNodes - 1; i++)
            {
                for (j = i + 1; j < numberOfNodes; j++)
                {
                    // this is for calculating interactions between nodes
                    float dx = x[i] - x[j];
                    float dy = y[i] - y[j];
                    
                    float dist = (float) Math.sqrt(dx*dx + dy*dy);
                    
                    float inv = 1/dist; // this is pretty useful and speeds things a little
                    
                    float force;
                    if (connected[i][j]) // if connected then work out springy force
                    {
                        force = - springConstant * (dist - springExtension);
                    }
                    else force = 70 * inv; // else purely repulsive (but falls off with distance)
                    
                    float xForce = force * dx * inv; // x (y) components of the force
                    float yForce = force * dy * inv;
                    
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
            nodes[i].setPositionDumb( x[i], y[i] );
        }
        
        for (ArcNodeListIterator iterator = model.getArcs(); iterator.hasNext();)
        {
            Arc arc = (Arc) iterator.next();
            if (arc.graphicRep() != null)
            {
            	arc.graphicRep().boundsChanged();
            }
        }
        
        System.out.println("Took: " + (System.currentTimeMillis() - startTime)
            + " milliseconds");
    }
    
    public static byte[] contentOfUrl(java.net.URL anURL, long timeOut)
    {
        try
        {
            java.net.URLConnection conn = anURL.openConnection();
            int length = conn.getContentLength();
            byte[] content = new byte[length];
            InputStream stream = conn.getInputStream();
             
            int read = 0;
            long startTime = System.currentTimeMillis();
            long duration = 0;
            
            while ((read != length) && (duration < timeOut))
            {
                read += stream.read(content, read, stream.available());
                duration = System.currentTimeMillis() - startTime;
            }
            
            System.out.println("Read: " + read + " bytes of " + length);
             
            stream.close();
            
            if (duration >= timeOut)
            {
                ShowError(
                    "Connection Timed Out",
                    "Loading failed because the download took too long.\n" +
                    "I'm sorry.",
                    Critical, null);
                
                return null;
            }
            
            return content;
        }
        catch (Exception e)
        {
            ShowError(
                "Connection Failed",
                "The connection failed, alas.\n" +
                "Informative error message:\n" +
                e,
                Critical, null);
            
            return null;
        }
    }

}
