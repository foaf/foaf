package org.desire.rudolf.util;

import java.util.*;

public class SearchReplace
{
    public static Vector splitByString( String s, String ch )
    {
        Vector split = new Vector();
        StringTokenizer st = new StringTokenizer( s, ch );
        boolean doneFirst = false;
        while ( st.hasMoreTokens() )
        {
            String sss = st.nextToken();
            //            System.out.println("next token is "+sss);
            split.addElement( sss.toString().trim() );
            doneFirst = true;
        }
        return split;
    }

    /** version for ignoring the split until the spcified character appears */
    public static Vector splitByString( String s, String ch,
        String ignoreStart, String ignoreFinish )
        {
            boolean ignore = false;
            Vector split = new Vector();
            boolean doneFirst = false;
            String tmp = new String();
            while ( s.indexOf( ch ) != -1 )
            {
                System.out.println( "S[1] ..." + s.toString() + "..." );
                String sss = nextChunk( s, ch );
                int i = sss.indexOf( ignoreStart );
                int j = sss.indexOf( ignoreFinish );
                s = s.substring( s.indexOf( ch ) + 1 );
                if ( i != -1 )
                {
                    ignore = true;
                }
                if ( !ignore )
                {
                    split.addElement( sss.toString().trim() );
                }
                else
                {
                    if ( j == -1 )
                    {
                        //add to bigger string
                        tmp = tmp + ch + sss.toString().trim();
                        if ( s.indexOf( ch ) == -1 )
                        {
                            tmp = tmp + ch + s;
                            if ( !tmp.trim().equals( "" ) )
                            {
                                split.addElement( tmp.trim() );
                            }
                            ignore = false;
                            tmp = "";
                        }
                    }
                    else
                    {
                        tmp = tmp + ch + sss.toString().trim();
                        if ( !tmp.trim().equals( "" ) )
                        {
                            split.addElement( tmp.trim() );
                        }
                        tmp = "";
                        ignore = false;
                    }
                }
            }
            return split;
    }

    public static String nextChunk( String s, String ch )
    {
        int j = s.indexOf( ch );
        //        System.out.println("j is now "+j);
        //        System.out.println("S is first "+s);
        if ( j != -1 )
        {
            //s=s.substring(j+1);
            //            System.out.println("S is now "+s);
            return s.substring( 0, j );
        }
        else
        {
            return s;
        }
    }

    public static Vector splitByStringNoChomp( String s, String ch )
    {
        Vector split = new Vector();
        boolean doneFirst = false;
        String tmp = new String();
        while ( s.indexOf( ch ) != -1 )
        {
            String sss = nextChunk( s, ch );
            s = s.substring( s.indexOf( ch ) + ch.length() );
            split.addElement( sss.trim() );
        }
        split.addElement( s.trim() );
        return split;
    }

    /** utility method to replace tofind in s with toreplace */
    public static String replace( String s, String tofind, String toreplace )
    {
        StringBuffer result = new StringBuffer();
        boolean donef = false;
        if ( s.indexOf( tofind ) != -1 )
        {
            while ( s.indexOf( tofind ) != -1 )
            {
                if ( donef )
                {
                    result.append( toreplace );
                }
                int i = s.indexOf( tofind );
                result.append( getNextPart( s, i ) );
                donef = true;
                s = s.substring( i + tofind.length() );
                if ( s.indexOf( tofind ) == -1 )
                {
                    result.append( toreplace + s );
                }
            }
        }
        else
        {
            result.append( s );
        }
        return result.toString();
    }

    public static String getNextPart( String s, int i )
    {
        //        System.out.println("NP: s is "+s);
        if ( i != -1 )
        {
            //            System.out.println("NP: returning[1] "+s.substring(0, i));
            return s.substring( 0, i );
        }
        else
        {
            //          System.out.println("NP: returning[2] "+s);
            return s;
        }
    }

    public static void main( String[] args )
    {
        System.out.println( "\n" +
            SearchReplace.replace( "<bla>?x</bla>?x</ccc>", "?x",
            "xxx" ) + "\n" );
    }
} //end class

