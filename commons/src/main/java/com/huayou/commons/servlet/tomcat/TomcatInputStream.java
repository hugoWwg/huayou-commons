package com.huayou.commons.servlet.tomcat;

import javax.servlet.ServletInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by wuqiang on 14-12-23.
 *
 * @author wuqiang
 */
public class TomcatInputStream extends org.apache.catalina.connector.CoyoteInputStream{
    private InputStream input;
    private ServletInputStream originalInputStream;
    protected TomcatInputStream(InputStream newInputStream, ServletInputStream originalInputStream) {
        super(null);
        this.input = newInputStream;
        this.originalInputStream = originalInputStream;
    }
    @Override
    public int read()
            throws IOException {
        return this.input.read();
    }

    @Override
    public int available() throws IOException {
        return this.input.available();
    }

    @Override
    public int read(final byte[] b) throws IOException {
        return this.input.read(b);
    }


    @Override
    public int read(final byte[] b, final int off, final int len)
            throws IOException {
        return this.input.read(b,off,len);
    }


    @Override
    public int readLine(byte[] b, int off, int len) throws IOException {
        return super.readLine(b, off, len);
    }


    /**
     * Close the stream
     * Since we re-cycle, we can't allow the call to super.close()
     * which would permanently disable us.
     */
    @Override
    public void close() throws IOException {
        this.input.close();
        if(this.originalInputStream != null){
            try {
                this.originalInputStream.close();
            } catch (Exception e) {
            }
        }
    }
}
