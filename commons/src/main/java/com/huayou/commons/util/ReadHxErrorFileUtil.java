package com.huayou.commons.util;

import com.huayou.commons.logger.LogMgr;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.File;
import java.io.IOException;

/**
 * @Author : hugo
 * @Date : 15/2/6 下午3:42.
 */
public class ReadHxErrorFileUtil {

    public static String hxErrorFilePath = "/";

    public String SynchronizedHxErrorData2Hx() {
        LineIterator it = null;
        try {
            it = FileUtils.lineIterator(new File(hxErrorFilePath), "UTF-8");
            while (it.hasNext()) {
                String line = it.nextLine();

            }
        } catch (IOException e) {
            LogMgr.getLogger(ReadHxErrorFileUtil.class).error("SynchronizedHxErrorData2Hx error[{}]", e);
        } finally {
            if (null != it) LineIterator.closeQuietly(it);
        }

        return null;
    }
}
