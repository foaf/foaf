package org.desire.rudolf.util;

public class ErrorLog
{
    static ErrorLog error = null;
    static StringBuffer log = new StringBuffer( "\nREPORT LOG: \n" );

    public ErrorLog()
    {
        log = new StringBuffer( "\nREPORT LOG: \n" );
    }

    public static ErrorLog getInstance()
    {
        if ( error == null )
        {
            error = new ErrorLog();
        }
        return error;
    }

    public static void write( String data )
    {
        log.append( data + "\n" );
    }

    public static String getReport()
    {
        return log.toString();
    }
}

