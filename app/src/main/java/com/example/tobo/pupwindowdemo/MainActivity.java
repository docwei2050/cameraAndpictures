package com.example.tobo.pupwindowdemo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.cundong.utils.PatchUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import utils.PhotoBitmapUtils;

public class MainActivity extends AppCompatActivity {
    private final int SDK_PERMISSION_REQUEST = 100;
    private static final int CAMERA_REQESUT_CODE = 0;
    public static final int REQUEST_PICK_IMAGE = 10011;
    public static final int REQUEST_KITKAT_PICK_IMAGE = 10012;

    private static String picPath;
    private String permissionInfo;
    //压缩后的图片地址
    private String mCompressPath;
    private Uri photoUri;
    private static String photoPath = Environment.getExternalStorageDirectory()
            + "/DCIM/Camera/";

    static {
        System.loadLibrary("ApkPatchLibrary");
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getPersimmions();

    }



    @TargetApi(23)
    private void getPersimmions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> permissions = new ArrayList<String>();
            /***
             * 定位权限为必须权限，用户如果禁止，则每次进入都会申请
             */
            // 相机权限
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.CAMERA);
            }
			/*
			 * 读写权限和电话状态权限非必要权限(建议授予)只会申请一次，用户同意或者禁止，只会弹一次
			 */
            // 读写权限
            if (addPermission(permissions, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissionInfo += "Manifest.permission.WRITE_EXTERNAL_STORAGE Deny \n";
            }

            if (permissions.size() > 0) {
                requestPermissions(permissions.toArray(new String[permissions.size()]), SDK_PERMISSION_REQUEST);
            }
        }
    }

    @TargetApi(23)
    private boolean addPermission(ArrayList<String> permissionsList, String permission) {
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) { // 如果应用没有获得对应权限,则添加到列表中,准备批量申请
            if (shouldShowRequestPermissionRationale(permission)) {
                return true;
            } else {
                permissionsList.add(permission);
                return false;
            }

        } else {
            return true;
        }
    }

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // TODO Auto-generated method stub
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void click(View view) {
        View rootView = View.inflate(this, R.layout.activity_main, null);
        ImageView iv = (ImageView) rootView.findViewById(R.id.iv);

        View contentView = View.inflate(this, R.layout.pupup_main, null);
        TextView camera_tv = (TextView) contentView.findViewById(R.id.camera);
        final TextView picture_tv = (TextView) contentView.findViewById(R.id.picture);
        TextView cancel_tv = (TextView) contentView.findViewById(R.id.cancel);
        final PopupWindow popupWindow = new PopupWindow(contentView);
        popupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.showAtLocation(rootView, Gravity.BOTTOM, 0, 0);
        camera_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                }
                Intent intent = null;
                try {
                    //确认相机是否能打开
                    android.hardware.Camera camera = android.hardware.Camera.open(0);
                    int num = camera.getNumberOfCameras();
                    if (camera != null) {
                        camera.setPreviewCallback(null);
                        camera.stopPreview();
                        camera.release();
                        camera = null;
                    }
                    String SDState = Environment.getExternalStorageState();
                    if (SDState.equals(Environment.MEDIA_MOUNTED)) {
                        // 初始化
                        picPath = null;
                        Date date = new Date(System.currentTimeMillis());
                        String name = new SimpleDateFormat("'IMG'_yyyyMMdd_HHmmss")
                                .format(date) + ".jpg";
                        File file = new File(photoPath);
                        if (!file.exists()) {
                            // 检查图片存放的文件夹是否存在
                            file.mkdir();
                            // 不存在的话 创建文件夹
                        }
                        picPath = photoPath + name;
                        File photo = new File(picPath);
                        photoUri = Uri.fromFile(photo);
                        intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);// "android.media.action.IMAGE_CAPTURE"
                        intent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, Configuration.ORIENTATION_PORTRAIT);//竖屏
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri); // 这样就将文件的存储方式和uri指定到了Camera应用中
                        startActivityForResult(intent, CAMERA_REQESUT_CODE);
                    } else {
                        if (MainActivity.this != null && !MainActivity.this.isFinishing()) {
                            Toast.makeText(MainActivity.this, "内存卡不存在", Toast.LENGTH_LONG).show();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "权限禁止了", Toast.LENGTH_SHORT).show();
                }

            }
        });
        picture_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                }
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                    startActivityForResult(new Intent(Intent.ACTION_GET_CONTENT).setType("image/*"),
                            REQUEST_PICK_IMAGE);
                } else {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                    startActivityForResult(intent, REQUEST_KITKAT_PICK_IMAGE);
                }
            }
        });
        cancel_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                }
                Toast.makeText(MainActivity.this, "用户取消了", Toast.LENGTH_LONG).show();
            }
        });

    }
    /**
     * 图片压缩的
     **/
    private PhotoAsynTask asynTask;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String target=Environment.getExternalStorageDirectory()+"/aa";
        File dir=new File(target);
        if(!dir.exists()){
            dir.mkdirs();
        }
        asynTask = new PhotoAsynTask();

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CAMERA_REQESUT_CODE:
                    //Uri photoUri = data.getData();
                    if(TextUtils.isEmpty(picPath)){
                        //ToastUtil.makeToast(ImageUpActivity.this,"拍照失败,照片未生成",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    asynTask.execute(target, picPath);
                    break;
                case REQUEST_PICK_IMAGE:
                    System.out.println(data+"datfgdfgdfgdfga-----------");
                    if (data != null) {
                        Uri uri = data.getData();
                        String path=getPathByUri(this,uri);
                        asynTask.execute(target, path);
                    } else {
                        Log.e("======", "========图片为空======");
                    }
                    break;
                case REQUEST_KITKAT_PICK_IMAGE:
                    System.out.println(data+"data-----------");
                    if (data != null) {
                        Uri uri = ensureUriPermission(this, data);
                        String path=getPathByUri4kitkat(this,uri);
                        asynTask.execute(target,path);
                    } else {
                        Log.e("======", "====-----==图片为空======");
                    }
                    break;
            }
        }


    }
    @SuppressWarnings("ResourceType")
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static Uri ensureUriPermission(Context context, Intent intent) {
        Uri uri = intent.getData();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            final int takeFlags = intent.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION;
            context.getContentResolver().takePersistableUriPermission(uri, takeFlags);
        }
        return uri;
    }
    /**
     * 对返回的图片进行压缩处理内部类
     **/
    private class PhotoAsynTask extends AsyncTask<Object, Object, Object> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Object doInBackground(Object... params) {

            String savePath = params[0].toString();
            String path = params[1].toString();
            System.out.println(savePath+"---savePath---"+path+"-----path");
            PhotoBitmapUtils.compressBitmap(savePath, path, 1080, 1920, new PhotoBitmapUtils.CompressBitmapCallback() {
                @Override
                public void compress(String imagePath) {
                    if (imagePath != null) {
                        mCompressPath = imagePath;
                    }
                }
            });
            return mCompressPath;
        }
    }


    // 专为Android4.4设计的从Uri获取文件绝对路径，以前的方法已不好使
    @SuppressLint("NewApi")
    public static String getPathByUri4kitkat(final Context context, final Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {// ExternalStorageProvider
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if (isDownloadsDocument(uri)) {// DownloadsProvider
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            } else if (isMediaDocument(uri)) {// MediaProvider
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                final String selection = "_id=?";
                final String[] selectionArgs = new String[] { split[1] };
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {// MediaStore
            // (and
            // general)
            return getDataColumn(context, uri, null, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {// File
            return uri.getPath();
        }
        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context
     *            The context.
     * @param uri
     *            The Uri to query.
     * @param selection
     *            (Optional) Filter used in the query.
     * @param selectionArgs
     *            (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = { column };
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
    public static String getPathByUri(Context context, Uri data) {
        String filename=null;
        if (data.getScheme().toString().compareTo("content") == 0) {
            Cursor cursor = context.getContentResolver().query(data, new String[] { "_data" }, null, null, null);
            if (cursor.moveToFirst()) {
                filename = cursor.getString(0);
            }
        } else if (data.getScheme().toString().compareTo("file") == 0) {// file:///开头的uri
            filename = data.toString();
            filename = data.toString().replace("file://", "");// 替换file://
            if (!filename.startsWith("/mnt")) {// 加上"/mnt"头
                filename += "/mnt";
            }
        }
        return filename;
    }
    public void upClick(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //1.获取差分包所在路径
                String patchPath = Environment.getExternalStorageDirectory() + File.separator + "app-debug.patch";//差分包
                //2.获取旧版本apk路径
                String oldApkPath = ApkUtils.getSourceApkPath(MainActivity.this, getPackageName());
                //3.合并成新版本：旧版本apk+差分包 = 新版本apk
                String newApkPath = Environment.getExternalStorageDirectory() + File.separator + "app-debug-new.apk";
                //参数说明：第1个参数-旧版本，第2个参数-新版本，第3个参数-差分包
                int result = PatchUtils.patch(oldApkPath, newApkPath, patchPath);
//                showLog(""+result);
                if(result == 0){//合并成功
                    //4.安装新版本apk
                    ApkUtils.installApk(MainActivity.this, newApkPath);
                }
            }
        }).start();
    }
}