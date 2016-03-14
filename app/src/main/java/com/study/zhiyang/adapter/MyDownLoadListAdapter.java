package com.study.zhiyang.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.study.zhiyang.download.DownloadInfo;
import com.study.zhiyang.download.DownloadListActivity;
import com.study.zhiyang.download.DownloadTools;
import com.study.zhiyang.webbrowser.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhiyang on 2016/1/13.
 */
public class MyDownLoadListAdapter extends ArrayAdapter {

    private int itemlayoutId;
    private List<View> views;
    public Map<Integer, Drawable> iconMap;
    public Map<Integer, Boolean> checkBoxStates;

    private boolean displayMode = DownloadListActivity.NO_SELECT_MODE;

    public MyDownLoadListAdapter(Context context, int resource) {
        super(context, resource);
    }

    public MyDownLoadListAdapter(Context context, int resource, List<DownloadInfo> objects, boolean displayMode) {
        super(context, resource, objects);
        itemlayoutId = resource;
        this.displayMode = displayMode;
    }

    public MyDownLoadListAdapter(Context context, int resource, List<DownloadInfo> objects) {
        super(context, resource, objects);
        itemlayoutId = resource;
        checkBoxStates = new HashMap<>();
        iconMap = new HashMap<>();
//        this.displayMode = displayMode;
    }

    public void setDisplayMode(boolean mode) {
        this.displayMode = mode;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        DownloadInfo info = (DownloadInfo) getItem(position);
        final View view;
        final ViewHolder viewHolder;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(itemlayoutId, null);
            viewHolder = new ViewHolder();
            viewHolder.fileImage = (ImageView) view.findViewById(R.id.download_item_icon);
            viewHolder.fileName = (TextView) view.findViewById(R.id.download_item_name);
            viewHolder.fileSize = (TextView) view.findViewById(R.id.download_item_size);
            viewHolder.downloadProress = (ProgressBar) view.findViewById(R.id.download_item_progress);
            viewHolder.isDownloadingPause = (TextView) view.findViewById(R.id.download_item_pause);
            viewHolder.deleteCheckBox = (CheckBox) view.findViewById(R.id.download_item_checkbox_delete);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.deleteCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checkBoxStates.put(position, isChecked);
            }
        });
        int downloadingPause = info.pause;
        String fileNameString = info.name;
        String filePath = info.path;
        int finished = info.finished;
        if (checkBoxStates.containsKey(position)) {
            viewHolder.deleteCheckBox.setChecked(checkBoxStates.get(position));
        } else {
            checkBoxStates.put(position, false);
            viewHolder.deleteCheckBox.setChecked(false);
        }
//        Log.d("FINISHED",finished+"............");
        if (!displayMode) {
            viewHolder.deleteCheckBox.setVisibility(View.GONE);
        } else {
            viewHolder.deleteCheckBox.setVisibility(View.VISIBLE);
        }
        if (iconMap.containsKey(position)) {
            viewHolder.fileImage.setImageDrawable(iconMap.get(position));
        } else {
            if (fileNameString.endsWith(".apk")) {
                if (finished == 1) {
                    PackageInfo packageArchiveInfo = getContext().getPackageManager().getPackageArchiveInfo(filePath, PackageManager.GET_ACTIVITIES);
                    if (packageArchiveInfo != null) {
                        ApplicationInfo applicationInfo = packageArchiveInfo.applicationInfo;
                        applicationInfo.sourceDir = filePath;
                        applicationInfo.publicSourceDir = filePath;
//                Log.i("APP INFO", applicationInfo.packageName + " " + applicationInfo.name);
                        Drawable icon = getContext().getPackageManager().getApplicationIcon(applicationInfo);
                        viewHolder.fileImage.setImageDrawable(icon);
                        iconMap.put(position, icon);
                    } else {

                        viewHolder.fileImage.setImageResource(R.drawable.apk);
//                        Drawable icon = viewHolder.fileImage.getDrawable();
//                        iconMap.put(position, icon);
                    }
                } else {
                    viewHolder.fileImage.setImageResource(R.drawable.apk);
//                    Drawable icon = viewHolder.fileImage.getDrawable();
//                    iconMap.put(position, icon);
                }
            } else if (fileNameString.endsWith(".jpg")) {
                viewHolder.fileImage.setImageResource(R.drawable.jpg);
                Drawable icon = viewHolder.fileImage.getDrawable();
                iconMap.put(position, icon);
            } else if (fileNameString.endsWith(".avi")) {
                viewHolder.fileImage.setImageResource(R.drawable.avi);
                Drawable icon = viewHolder.fileImage.getDrawable();
                iconMap.put(position, icon);
            } else if (fileNameString.endsWith(".mp3")) {
                viewHolder.fileImage.setImageResource(R.drawable.mp3);
                Drawable icon = viewHolder.fileImage.getDrawable();
                iconMap.put(position, icon);
            } else if (fileNameString.endsWith(".pdf")) {
                viewHolder.fileImage.setImageResource(R.drawable.pdf);
                Drawable icon = viewHolder.fileImage.getDrawable();
                iconMap.put(position, icon);
            } else if (fileNameString.endsWith(".wma")) {
                viewHolder.fileImage.setImageResource(R.drawable.wma);
                Drawable icon = viewHolder.fileImage.getDrawable();
                iconMap.put(position, icon);
            } else if (fileNameString.endsWith(".doc")) {
                viewHolder.fileImage.setImageResource(R.drawable.doc);
                Drawable icon = viewHolder.fileImage.getDrawable();
                iconMap.put(position, icon);
            } else if (fileNameString.endsWith(".mov")) {
                viewHolder.fileImage.setImageResource(R.drawable.mov);
                Drawable icon = viewHolder.fileImage.getDrawable();
                iconMap.put(position, icon);
            } else if (fileNameString.endsWith(".rar")) {
                viewHolder.fileImage.setImageResource(R.drawable.rar);
                Drawable icon = viewHolder.fileImage.getDrawable();
                iconMap.put(position, icon);
            } else if (fileNameString.endsWith(".zip")) {
                viewHolder.fileImage.setImageResource(R.drawable.zip);
            } else if (fileNameString.endsWith(".txt")) {
                viewHolder.fileImage.setImageResource(R.drawable.txt);
                Drawable icon = viewHolder.fileImage.getDrawable();
                iconMap.put(position, icon);
            } else {
                viewHolder.fileImage.setImageResource(R.drawable.ic_action_add_active);
                Drawable icon = viewHolder.fileImage.getDrawable();
                iconMap.put(position, icon);
            }
        }

        viewHolder.downloadProress = (ProgressBar) view.findViewById(R.id.download_item_progress);
        viewHolder.fileName.setText(fileNameString);
        viewHolder.fileSize.setText(DownloadTools.convertFileSize(info.contentSize));
//        Log.i("NAME", cursor.getString(cursor.getColumnIndex(Constants.DOWNLOAD_TABLE_ITEM_NAME)));
//        Log.i("SIZE",cursor.getString(cursor.getColumnIndex(Constants.DOWNLOAD_TABLE_ITEM_SIZE)));
        viewHolder.fileSize.setVisibility(View.VISIBLE);

        if (finished == 0) {
            viewHolder.downloadProress.setVisibility(View.VISIBLE);
            viewHolder.downloadProress.setProgress((int) (info.finishedPercentage * 100));
        } else {
            viewHolder.downloadProress.setVisibility(View.INVISIBLE);
        }
        if (downloadingPause == 1) {
            viewHolder.isDownloadingPause.setVisibility(View.VISIBLE);
            viewHolder.isDownloadingPause.setText("暂停");
        } else {
            if (finished == 0) {
                viewHolder.isDownloadingPause.setVisibility(View.VISIBLE);
                viewHolder.isDownloadingPause.setText("正在下载");
            } else viewHolder.isDownloadingPause.setVisibility(View.INVISIBLE);
        }
//        view.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                for (View view1:views){
//
//                }
//                return false;
//            }
//        });
//        views.add(view);
//        view.setClickable(false);
//        view.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!displayMode) {
//                    DownloadTools.getMyDownloadManager().handleItemClickAction(position);
//                } else {
//
////                    CheckBox checkBox = (CheckBox) ((LinearLayout)((LinearLayout)view).getChildAt(0)).getChildAt(0);
////                    CheckBox checkBox = (CheckBox) v.findViewById(R.id.download_item_checkbox_delete);
//
//                    viewHolder.deleteCheckBox.setChecked(!viewHolder.deleteCheckBox.isChecked());
//                }
//            }
//        });
//        view.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                displayMode = true;
//                return true;
//            }
//        });
        return view;
    }

    class ViewHolder {
        ImageView fileImage;
        TextView fileName;
        TextView fileSize;
        ProgressBar downloadProress;
        TextView isDownloadingPause;
        CheckBox deleteCheckBox;
    }
}
