package com.study.zhiyang.download;

/**
 * Created by zhiyang on 2016/1/13.
 */
public class DownloadInfo {
    public String name = "";
    public String path = "";
    public String url = "";
    public String useragent = "";
    public String contentDisposition = "";
    public String mimetype = "";

    public int pause = 0;
    public int finished;
    //        public float finishedPercentage;
    public long contentSize;
    public long finishedSize;
    public double finishedPercentage;

    public DownloadInfo(String name, String path, String url, int finished, long contentSize,
                        long finishedSize, int pause, String useragent, String contentDisposition,
                        String mimetype) {
        this.name = name;
        this.path = path;
        this.finished = finished;
        this.url = url;
        this.contentSize = contentSize;
        this.finishedSize = finishedSize;
        this.pause = pause;
        this.mimetype = mimetype;
        this.contentDisposition = contentDisposition;
        this.useragent = useragent;
        if (finished == 1) {
            finishedPercentage = 1;
        } else finishedPercentage = ((float) finishedSize) / contentSize;
    }
}
