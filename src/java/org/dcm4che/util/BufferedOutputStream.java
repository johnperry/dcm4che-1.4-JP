/*
 * Based on java.io.BufferedOutputStream
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package org.dcm4che.util;

import java.io.EOFException;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.stream.ImageInputStream;

public class BufferedOutputStream extends FilterOutputStream
{

    protected final byte[] buf;
    protected int count;

    public BufferedOutputStream(OutputStream out)
    {
        this(out, new byte[8192]);
    }

    public BufferedOutputStream(OutputStream out, byte[] buf)
    {
        super(out);
        if (buf.length == 0)
        {
            throw new IllegalArgumentException("Buffer size == 0");
        }
        this.buf = buf;
    }

    private void flushBuffer() throws IOException
    {
        if (count > 0)
        {
            out.write(buf, 0, count);
            count = 0;
        }
    }

    public synchronized void write(int b) throws IOException
    {
        if (count >= buf.length)
        {
            flushBuffer();
        }
        buf[count++] = (byte) b;
    }

    public synchronized void write(byte b[], int off, int len)
            throws IOException
    {
        if (len >= buf.length)
        {
            flushBuffer();
            out.write(b, off, len);
            return;
        }
        if (len > buf.length - count)
        {
            flushBuffer();
        }
        System.arraycopy(b, off, buf, count, len);
        count += len;
    }

    public void copyFrom(InputStream in)
    throws IOException
    {
    	copyFrom(in, -1);
    	
    }
    
    public synchronized void copyFrom(InputStream in, int len)
    throws IOException
    {
		if (in == null)
			throw new NullPointerException("in");
    	if (len < -1)
    		throw new IllegalArgumentException("len:" + len);
        for (int toWrite = len == -1 ? Integer.MAX_VALUE : len, read = 0;
        	toWrite > 0; toWrite -= read)
        {
            read = in.read(buf, count, Math.min(toWrite, buf.length - count));
            if (read == -1)
            {
            	if (len == -1)
            		return;
            	throw new EOFException();
            }
            count += read;
            if (count >= buf.length)
            {
                flushBuffer();
            }
        }
    }


    public void copyFrom(ImageInputStream in)
    throws IOException
    {
    	copyFrom(in, -1);
    	
    }
    
    public synchronized void copyFrom(ImageInputStream in, int len)
    throws IOException
    {
		if (in == null)
			throw new NullPointerException("in");
    	if (len < -1)
    		throw new IllegalArgumentException("len:" + len);
        for (int toWrite = len == -1 ? Integer.MAX_VALUE : len, read = 0;
        	toWrite > 0; toWrite -= read)
        {
            read = in.read(buf, count, Math.min(toWrite, buf.length - count));
            if (read == -1)
            {
            	if (len == -1)
            		return;
            	throw new EOFException();
            }
            count += read;
            if (count >= buf.length)
            {
                flushBuffer();
            }
        }
    }
    
    public synchronized void flush() throws IOException
    {
        flushBuffer();
        out.flush();
    }
}
