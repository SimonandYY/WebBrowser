package com.study.zhiyang.download;

import android.app.DownloadManager;

import com.study.zhiyang.utils.ContextUtils;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhiyang on 2016/1/8.
 */
public class DownloadTools {
//    private static MultiDownloadManager downloadManager = new MultiDownloadManager(ContextUtils.getInstance());
//
//    public static MultiDownloadManager getDownloadManager() {
//        return downloadManager;
//    }

    public static String getFileName(HttpURLConnection conn) {
        String url = conn.getURL().toString();
        String filename = "";
        boolean isok = false;
        if (conn == null) {
            return null;
        }
        Map<String, List<String>> hf = conn.getHeaderFields();
        if (hf == null) {
            return null;
        }
        Set<String> key = hf.keySet();
        if (key == null) {
            return null;
        }

        for (String skey : key) {
            List<String> values = hf.get(skey);
            for (String value : values) {
                String result;
                try {
                    result = new String(value.getBytes("ISO-8859-1"), "GBK");
                    int location = result.indexOf("filename");
                    if (location >= 0) {
                        result = result.substring(location
                                + "filename".length());
                        filename = result
                                .substring(result.indexOf("=") + 1);
                        isok = true;
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }// ISO-8859-1 UTF-8 gb2312
            }
            if (isok) {
                break;
            }
        }

        // 从路径中获取
        if (filename == null || "".equals(filename)) {
//            int len = url.length();
//            int lastBiaodian = len-1;
//            for (int i = len-1;i>=0;i--){
//                if (!(Character.isLetter(url.charAt(i))||Character.isDigit(url.charAt(i)))){
//                    lastBiaodian = i;
//                }
//            }
//            if (lastBiaodian==len-1)
//                return null;
//            else if (lastBiaodian==url.lastIndexOf("."))
//                    filename = url.substring(url.lastIndexOf("/") + 1);
//            else if (lastBiaodian>url.lastIndexOf(".")){
//                filename = url.substring(url.lastIndexOf("/")+1,lastBiaodian-1);
//            }
            if (url.contains("?")) {
                if (url.lastIndexOf("?") > url.lastIndexOf("/")) {
                    filename = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("?"));
                }
            } else filename = url.substring(url.lastIndexOf("/") + 1);

        }
        filename = filename.replaceAll("\"","");
        filename =filename.replaceAll("[?]","");
        filename =filename.replaceAll("/","");
        filename =filename.replaceAll("\"","");
        return filename;
    }

    public static String convertFileSize(long size) {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;

        if (size >= gb) {
            return String.format("%.1f GB", (float) size / gb);
        } else if (size >= mb) {
            float f = (float) size / mb;
            return String.format(f > 100 ? "%.0f MB" : "%.1f MB", f);
        } else if (size >= kb) {
            float f = (float) size / kb;
            return String.format(f > 100 ? "%.0f KB" : "%.1f KB", f);
        } else
            return String.format("%d B", size);
    }

    private static MyDownloadManager myDownloadManager = new MyDownloadManager(ContextUtils.getInstance());

    public static MyDownloadManager getMyDownloadManager() {
        return myDownloadManager;
    }

}
