/**
 * A simple class to hold and draw multi-line text
 **/

package org.rdfweb.rdfauthor.view;

import java.awt.*;
import java.awt.geom.*;
import java.awt.font.*;
import java.util.ArrayList;

public class MultiText
{
  String text;
  TextLayout[] lines;
  Rectangle2D size;
  Font font;
  
  public MultiText(Graphics2D g, String s, Font font)
  {
    size = new Rectangle2D.Double();
    setText(g, s, font);
  }

  public void setText(Graphics2D g, String s, Font font)
  {
    Object[] strings = splitText(s);

    size.setRect(0.0, 0.0, 0.0, 0.0);

    if ((lines == null) || (lines.length != strings.length))
      lines =  new TextLayout[strings.length];

    g.setFont(font);
    FontRenderContext frc = g.getFontRenderContext();
    
    for (int i = 0; i < strings.length;
	 i++ )
      {
	lines[i] = new TextLayout((String) strings[i], font, frc);
	Rectangle2D textBounds = lines[i].getBounds();
	double width = (size.getWidth() > textBounds.getWidth())?
	  size.getWidth():textBounds.getWidth();

	size.setRect(0.0, 0.0, width, size.getHeight() +
		     textBounds.getHeight());
      }
    
  }

  public Rectangle2D getBounds()
  {
    return size;
  }
  
  /**
   * This needs lots of work - eg if s = "\n...."
   * NOT UNICODE SAFE (I think)
   **/
  
  public Object[] splitText(String s)
  {
    if (s.indexOf('\n') < 0) return new String[] 
      {
	s
	  };
    ArrayList strings = new ArrayList();

    int start = 0;
    int offset = s.indexOf('\n');

    while (offset >= 0)
      {
	strings.add(s.substring(start, offset));
	start = offset + 1;
	offset = s.indexOf('\n', start);
      }
    if (start < (s.length() - 1)) strings.add(s.substring(start));
    
    return strings.toArray();
  }

  public void draw(Graphics2D g, double x, double y)
  {
    for (int i = 0; i < lines.length; i++)
      {
	y += lines[i].getBounds().getHeight();
	lines[i].draw(g, (float) x, (float) y);
      }
  }
}

    
	
