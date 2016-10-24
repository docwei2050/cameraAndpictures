package utils;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * User: 李永昌(ex-liyongchang001@pingan.com.cn)
 * Date: 2016-02-25
 * Time: 09:48
 * 图片文件处理工具类
 */
public class PhotoFileUtils {

    private static final String TAG = PhotoFileUtils.class.getSimpleName();
    private static final Object mLockObject = new Object();
    /**创建文件**/
    public static boolean createFolder(String folderPath){
        File file=new File(folderPath);
        if(!file.exists()){
            return file.mkdirs();
        }
        return true;
    }
    /**获取bitmap的字节长度**/
    public static long getBitmapLength(Bitmap bitmap){
        return bitmap.getRowBytes()*bitmap.getHeight();
    }
    /**获取SD卡剩余空间**/
    public static long getSDFreeSize(){
        return getSDFreeSize(Environment.getExternalStorageDirectory().toString());
    }
    /**获取指定目录剩余空间**/
    public static long getSDFreeSize(String filePath){
        try{
            File file = new File(filePath);
            if(!file.exists()){
                file.mkdirs();
            }
            android.os.StatFs statfs = new android.os.StatFs(filePath);
            long nBlocSize = statfs.getBlockSize(); // 获取SDCard上每个block的SIZE
            long nAvailaBlock = statfs.getAvailableBlocks(); // 获取可供程序使用的Block的数量
            long nSDFreeSize = nAvailaBlock * nBlocSize; // 计算 SDCard
            // 剩余大小B
            return nSDFreeSize;
        }catch(Exception ex){
            ex.printStackTrace();
            Log.d(TAG, "httpFrame threadName:" + Thread.currentThread().getName() + " getSDFreeSize  无法计算文件夹大小 folderPath:" + filePath);
        }
        return -1;
    }
    /**时间**/
    public static String getTimeMillisFileName(){
        synchronized (mLockObject) {
            long curTime = System.currentTimeMillis();
            while(true){
                long time = System.currentTimeMillis();
                if(time-curTime>0){
                    return ""+time;
                }
            }
        }
    }

    /**
     * 保存文件
     * @param fileRootPath
     * @param bm
     * @param format
     * @param extension
     * @return
     */
    public static String saveBitmap(String fileRootPath, Bitmap bm, CompressFormat format, String extension){
        if(createFolder(fileRootPath)){
            long bitmapSize=getBitmapLength(bm);
            FileOutputStream fos = null;
            if(getSDFreeSize(fileRootPath)<=bitmapSize){
                Log.d(TAG, "httpFrame  threadName:"+ Thread.currentThread().getName()+" saveBitmap  没有足够的空间 "+fileRootPath);
                return null;
            }
            try{
                String filePath = fileRootPath + File.separator+getTimeMillisFileName();
                if(extension!=null&&extension.length()>0){
                    filePath = filePath+"."+extension;
                }
                File file=new File(filePath);
                file.createNewFile();
                fos=new FileOutputStream(file);
                bm.compress(format, 80, fos);
                return filePath;

            }catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "httpFrame  threadName:"+ Thread.currentThread().getName()+" saveBitmap  保存图片出错 "+e.getMessage());
            }finally{
                try {
                    if(fos != null){
                        fos.flush();
                        fos.close();
                    }
                    if(bm != null && !bm.isRecycled()){
                        bm.recycle();
                    }
                    System.gc();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }else{
            Log.d(TAG, "httpFrame  threadName:"+ Thread.currentThread().getName()+" saveBitmap  创建文件夹失败");
        }
        return null;
    }

    /**
     * 保存文件
     * @param savePath
     * @param bm
     * @param format
     * @return
     */
    public static String savePhoto(String savePath, Bitmap bm, CompressFormat format){
            FileOutputStream fos = null;
            try{
                File file=new File(savePath);
                file.createNewFile();
                fos=new FileOutputStream(file);
                bm.compress(format, 80, fos);
                return file.getPath();
            }catch (Exception e) {
                e.printStackTrace();
            }finally{
                try {
                    if(fos != null){
                        fos.flush();
                        fos.close();
                    }
                    if(bm != null && !bm.isRecycled()){
                        bm.recycle();
                    }
                    System.gc();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        return null;
    }

}
