package utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * User: 李永昌(ex-liyongchang001@pingan.com.cn)
 * Date: 2016-02-23
 * Time: 10:18
 * 图片处理类
 */
public class PhotoBitmapUtils {
	private static final String TAG = PhotoBitmapUtils.class.getSimpleName();
	
	private static final Object OBJECT_LOCKED = new Object();
	
	public static final String EXTENSION_IMG_PNG = "png";
	
	public static final String EXTENSION_IMG_JPEG = "jpg";
	
	
	public static Bitmap changeBitmapSize(int maxWidth, int maxHeight, Bitmap oldBitmap){
		if(oldBitmap==null||oldBitmap.isRecycled()){
			Log.d(TAG, "httpFrame 不能改变一个已经被释放的图片的大小");
			return null;
		}
		int[] measureArrary = PhotoBitmapUtils.getMeasureSize(oldBitmap.getWidth(), oldBitmap.getHeight(), maxWidth,maxHeight);
		if(measureArrary!=null && measureArrary.length > 1){
			oldBitmap = PhotoBitmapUtils.changeBitmapSizeFixed(measureArrary[0], measureArrary[1], oldBitmap);
		}
		return oldBitmap;
	}
	
	/**
	 * 同步 线程获取图片
	 * @param imgPath	图片本地路径
	 * @param maxWidth	最大宽度
	 * @param maxHeight	最大高度
	 * @return	bitmap
	 */
	public static Bitmap getBitmap(String imgPath, int maxWidth, int maxHeight){
		return getBitmap(imgPath,  maxWidth,maxHeight, null);
	}
	
	/**
	 * 同步 线程获取图片
	 * @param maxWidth	最大宽度
	 * @param maxHeight	最大高度
	 * @return	bitmap
	 */
	public static Bitmap getBitmap(byte[] date, int maxWidth, int maxHeight){
		return getBitmap(date, maxWidth,maxHeight , null);
	}
	
	/**
	 * 同步 线程获取图片
	 * @param imgPath	图片本地路径
	 * @param maxWidth	最大宽度
	 * @param maxHeight	最大高度
	 * @return	bitmap
	 */
	public static Bitmap getBitmap(String imgPath, int maxWidth, int maxHeight, BitmapFactory.Options options){
		//避免内存匿出  同步处理
		synchronized (OBJECT_LOCKED) {
			Bitmap bm = PhotoBitmapUtils.getBitmapBySampleSize(imgPath, maxWidth,maxHeight,options);
			System.gc();
			return PhotoBitmapUtils.changeBitmapSize(maxWidth,maxHeight, bm);
		}
	}
	
	/**
	 * 同步 线程获取图片
	 * @param maxWidth	最大宽度
	 * @param maxHeight	最大高度
	 * @return	bitmap
	 */
	public static Bitmap getBitmap(byte[] date, int maxWidth, int maxHeight, BitmapFactory.Options options){
		Bitmap bm = PhotoBitmapUtils.getBitmapBySampleSize(date, maxWidth,maxHeight,options);
		return PhotoBitmapUtils.changeBitmapSize(maxWidth,maxHeight, bm);
	}
	
	/**
	 * 同步 线程获取图片
	 * @param maxWidth	最大宽度
	 * @param maxHeight	最大高度
	 * @return	bitmap
	 */
	public static Bitmap getBitmap(int resource, Context context, int maxWidth, int maxHeight, BitmapFactory.Options options){
		Bitmap bm = PhotoBitmapUtils.getBitmapBySampleSize(context, resource, maxWidth, maxHeight, options);
		return PhotoBitmapUtils.changeBitmapSize(maxWidth,maxHeight, bm);
	}
	
	/**
	 * 获取图片
	 * @param imgPath	本地路径
	 * @param maxWidth	最大宽度
	 * @param maxHeight	最大高度
	 * @return	bitmap
	 */
	private static Bitmap getBitmapBySampleSize(String imgPath, int maxWidth, int maxHeight, BitmapFactory.Options options){
		BitmapFactory.Options tempOptions = new BitmapFactory.Options();
		tempOptions.inJustDecodeBounds = true;
		tempOptions.inPreferredConfig = Config.RGB_565;
		BitmapFactory.decodeFile(imgPath, tempOptions);
		// Calculate inSampleSize
		int inSampleSize = calculateInSampleSize(tempOptions, maxWidth, maxHeight);
		
		if(options!=null){
			tempOptions = options;
		}
		tempOptions.inSampleSize = inSampleSize;
		// Decode bitmap with inSampleSize set
		tempOptions.inJustDecodeBounds = false;
		
		return BitmapFactory.decodeFile(imgPath, tempOptions);
	}
	
	/**
	 * 获取图片
	 * @param maxWidth	最大宽度
	 * @param maxHeight	最大高度
	 * @return	bitmap
	 */
	private static Bitmap getBitmapBySampleSize(byte[] data, int maxWidth, int maxHeight, BitmapFactory.Options options){
		BitmapFactory.Options tempOptions = new BitmapFactory.Options();
		tempOptions.inJustDecodeBounds = true;
		
		BitmapFactory.decodeByteArray(data, 0, data.length, tempOptions);
		// Calculate inSampleSize
		int inSampleSize = calculateInSampleSize(tempOptions, maxWidth, maxHeight);
		
		if(options!=null){
			tempOptions = options;
		}
		tempOptions.inSampleSize = inSampleSize;
		// Decode bitmap with inSampleSize set
		tempOptions.inJustDecodeBounds = false;

		return BitmapFactory.decodeByteArray(data, 0, data.length, tempOptions);
	}
	
	public static int[] getBitmapSize(String path){
		int[] size = new int[2];
		BitmapFactory.Options tempOptions = new BitmapFactory.Options();
		tempOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, tempOptions);
		size[0] = tempOptions.outWidth;
		size[1] = tempOptions.outHeight;
		return size;
	}
	
	/**
	 * 获取图片
	 * @param resource	本地路径
	 * @param maxWidth	最大宽度
	 * @param maxHeight	最大高度
	 * @return	bitmap
	 */
	private static Bitmap getBitmapBySampleSize(Context context, int resource, int maxWidth, int maxHeight, BitmapFactory.Options options){
		BitmapFactory.Options tempOptions = new BitmapFactory.Options();
		tempOptions.inJustDecodeBounds = true;
		
		BitmapFactory.decodeResource(context.getResources(), resource, tempOptions);
		// Calculate inSampleSize
		int inSampleSize =  calculateInSampleSize(tempOptions, maxWidth,maxHeight);
		
		if(options!=null){
			tempOptions = options;
		}
		tempOptions.inSampleSize = inSampleSize;
		// Decode bitmap with inSampleSize set
		tempOptions.inJustDecodeBounds = false;

		return BitmapFactory.decodeResource(context.getResources(), resource, tempOptions);
	}
	
	/**
	 * 计算图片的缩放值
	 * 
	 * @param options
	 * @return
	 */
	private static int calculateInSampleSize(BitmapFactory.Options options,
			int maxWidth,int maxHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		
		if(height>=width){
			final int heightRatio = Math.round((float) height / (float) maxHeight);
			return heightRatio;
		}else{
			final int widthRatio = Math.round((float) width / (float) maxWidth);
			return widthRatio;
		}
	}
	
	
	/**
	 * @param newWidth
	 * @param newHight
	 * @param oldBitmap
	 * @return
	 */
	private static Bitmap changeBitmapSizeFixed(float newWidth, float newHight, Bitmap oldBitmap){
		if(oldBitmap==null||oldBitmap.isRecycled()){
			Log.d(TAG, "httpFrame 不能改变一个已经被释放的图片的大小");
			return null;
		}
		float bmpWidth = oldBitmap.getWidth();   
        float bmpHeight = oldBitmap.getHeight();   
        
        if(bmpWidth==newWidth&&newHight==bmpHeight){
        	return oldBitmap;
        }
//        float scaleWidth = newWidth/bmpWidth;   
//        float scaleHeight = newHight/bmpHeight;   
//        
//        Matrix matrix = new Matrix();   
//        matrix.postScale(scaleWidth, scaleHeight);   
//        Bitmap bm=Bitmap.createBitmap(oldBitmap, 0, 0, oldBitmap.getWidth(), oldBitmap.getHeight(),   
//                matrix, true);   
//        Bitmap.createBitmap(source, x, y, width, height)
        Bitmap newBitmap = Bitmap.createScaledBitmap(oldBitmap, (int)newWidth,  (int)newHight, true);
        oldBitmap.recycle();
        System.gc();
        return newBitmap;
	}
	
	public static int[] getMeasureSize(int width,int height,int maxWidth,int maxHeight){
		if(width<=maxWidth||height<=maxHeight){
			return null;
		}
		int newHeight = 0;
		int newWidth = 0;
		if(width>height){
			newHeight = (int) getHeightByNewWidth(maxWidth, width, height);
			newWidth = maxWidth;
		}else{
			newWidth = (int) getWidthByNewHeight(maxHeight, width, height);
			newHeight = maxHeight;
		}
		return new int[]{newWidth,newHeight};
	}
	
	/**
     * 根据新的宽度  获取等比的新的高度
     * @param newWidth   新的宽度
     * @param oldWidth   旧的宽度
     * @param oldHeight  旧的高度
     * @return
     */
    public static float getHeightByNewWidth(float newWidth,float oldWidth,float oldHeight){
    	//640 * 101
		float scale = oldHeight/oldWidth;
		return newWidth*scale;
    }
    
    
    /**
     * 根据新的宽度  获取等比的新的高度
     * @param oldWidth   旧的宽度
     * @param oldHeight  旧的高度
     * @return
     */
    public static float getWidthByNewHeight(float newHeight,float oldWidth,float oldHeight){
    	//640 * 101
		float scale = oldHeight/oldWidth;
		return newHeight/scale;
    }
    
    
    /** 
	 * 读取图片属性：旋转的角度 
	 * @param path 图片绝对路径 
	 * @return degree旋转的角度 
	 */  
	public static int readPictureDegree(String path) {
		if(StringUtil.isEmpty(path)||!isFile( path)){
			return 0;
		}
		int degree  = 0;  
	    try {  
	    	ExifInterface exifInterface = new ExifInterface(path);
	     	int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
	     	switch (orientation) {  
	        	case ExifInterface.ORIENTATION_ROTATE_90:
	            	degree = 90;  
	             	break;  
	        	case ExifInterface.ORIENTATION_ROTATE_180:
	              	degree = 180;  
	             	break;  
	        	case ExifInterface.ORIENTATION_ROTATE_270:
	              	degree = 270;  
	             	break;  
	     	}  
		} catch (IOException e) {
			e.printStackTrace();  
		}
		return degree;  
	}  
	
	public static boolean isFile(String path){
		try{
			if(new File(path).isFile()){
				return true;
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return false;
		
	}
	

	/**
	 * 旋转图片
	 * @param angle
	 * @param bitmap
	 * @return Bitmap
	 */
	public static Bitmap rotaingImageView(int angle , Bitmap bitmap) {
	      //旋转图片 动作
		Matrix matrix = new Matrix();
	    matrix.postRotate(angle);
	    // 创建新的图片
	    Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
	      		bitmap.getWidth(), bitmap.getHeight(), matrix, true);
	    bitmap.recycle();
		return resizedBitmap;
	}

	
	/**
	 * 图片转换成base64
	 * @param imagePath	图片地址
	 * @param callback	callback
	 */
	public static void getBitmapBase64(final String imagePath,
			final CompressBase64Callback callback){
		String[] imgaePathArrary = new String[]{imagePath};
		getBitmapBase64(imgaePathArrary, callback, 600, 800, 100);
	}
	/**
	 * 图片转换成base64
	 * @param imgaePathArrary	图片地址
	 * @param callback			callback
	 * @param maxWidth			最大宽度
	 * @param maxHeight			最大高度
	 * @param quality			压缩质量 建议 70 - 100之间调整
	 * @return
	 */
	public static String getBitmapBase64(final String[] imgaePathArrary, final CompressBase64Callback callback,
										 final int maxWidth, final int maxHeight, final int quality){
		new Thread(){
			@Override
			public void run() {
				String[] resultBase64 = null;
				if(imgaePathArrary!=null&&imgaePathArrary.length>0){
					resultBase64 = new String[imgaePathArrary.length];
					for(int i=0;i<imgaePathArrary.length;i++){
						resultBase64[i] = bitmapToBase64(imgaePathArrary[i],maxWidth,maxHeight,quality);
					}
					callback.finish(resultBase64);
				}else{
					callback.finish(null);
				}
			}
		}.start();
		return null;
	}
	
	
	private static String bitmapToBase64(final String imagePath,
										 final int maxWidth, final int maxHeight, final int quality){
		
		if(!TextUtils.isEmpty(imagePath)){
			Bitmap bm = null;
			if(imagePath!=null){
				if(maxWidth>0&&maxHeight>0){
					bm = PhotoBitmapUtils.getBitmap(imagePath, maxWidth,maxHeight,null);
				}else{
					bm = BitmapFactory.decodeFile(imagePath);
				}
			}
			
			if(bm!=null&&!bm.isRecycled()){
				int angle = PhotoBitmapUtils.readPictureDegree(imagePath);
				if(angle>0){
					bm = PhotoBitmapUtils.rotaingImageView(angle, bm);
				}
				
			}
			
			return bitmapToBase64(bm,quality);
			
		}
		
		return null;
	}
	
	/**
	 * bitmap转为base64
	 * @param bitmap
	 * @return
	 */
	private static String bitmapToBase64(Bitmap bitmap, final int quality) {

		String result = null;
		ByteArrayOutputStream baos = null;
		try {
			if (bitmap != null) {
				baos = new ByteArrayOutputStream();
				bitmap.compress(CompressFormat.JPEG, quality, baos);

				byte[] bitmapBytes = baos.toByteArray();
				result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
				
				result = result.replaceAll("\n", "");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (baos != null) {
					baos.flush();
					baos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * base64转为bitmap
	 * @param base64Data
	 * @return
	 */
	public static Bitmap base64ToBitmap(String base64Data) {
		byte[] bytes = Base64.decode(base64Data, Base64.DEFAULT);
		return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
	}

	/**
	 * 压缩图片异步处理
	 * @param savePath	保存文件夹路径
	 * @param imagePath	被压缩的图片地址
	 * @param maxWidth	最大宽度
	 * @param maxHeight	最大高度
	 * @param callback	回调
	 */
	public static void compressBitmap(final String savePath, final String imagePath,
									  final int maxWidth, final int maxHeight, final CompressBitmapCallback callback){
		compressBitmap(savePath,null, imagePath, maxWidth, maxHeight, callback);
	}

	/**
	 * 压缩图片异步处理
	 * @param savePath	保存文件夹路径
	 * @param bitmap	被压缩的图片
	 * @param maxWidth	最大宽度
	 * @param maxHeight	最大高度
	 * @param callback	回调
	 */
	public static void compressBitmap(final String savePath, final Bitmap bitmap,
									  final int maxWidth, final int maxHeight, final CompressBitmapCallback callback){
		compressBitmap(savePath, bitmap,null, maxWidth, maxHeight, callback);
	}

	/**
	 * 图片转换
	 * @param savePath
	 * @param bitmap
	 * @param imagePath
	 * @param maxWidth
	 * @param maxHeight
	 * @param callback
	 */
	private static void compressBitmap(final String savePath, final Bitmap bitmap, final String imagePath,
									   final int maxWidth, final int maxHeight, final CompressBitmapCallback callback){
		if (!TextUtils.isEmpty(imagePath)) {
			Bitmap bm = null;
			if (imagePath != null) {
				if (maxWidth > 0 && maxHeight > 0) {
					bm = PhotoBitmapUtils.getBitmap(imagePath, maxWidth, maxHeight, null);
				} else {
					bm = BitmapFactory.decodeFile(imagePath);
				}
			} else if (bitmap != null && !bitmap.isRecycled()) {
				bm = PhotoBitmapUtils.changeBitmapSize(maxWidth, maxHeight, bitmap);
			} else {
				callback.compress(null);
				return;
			}

			if (bm != null && !bm.isRecycled()) {
				int angle = PhotoBitmapUtils.readPictureDegree(imagePath);
				if (angle > 0) {
					bm = PhotoBitmapUtils.rotaingImageView(angle, bm);
				}
				CompressFormat format = CompressFormat.JPEG;
				String localPath = PhotoFileUtils.saveBitmap(savePath, bm, format, EXTENSION_IMG_JPEG);
				callback.compress(localPath);
			}
			if (bm != null && !bm.isRecycled()) {
				bm.recycle();
			}
		}
	}


	public static String compressBitmap(final String savePath, final String imagePath, final int maxWidth, final int maxHeight){
		String path = null;
		if (!TextUtils.isEmpty(imagePath)) {
			Bitmap bm = null;
			if (maxWidth > 0 && maxHeight > 0) {
				bm = PhotoBitmapUtils.getBitmap(imagePath, maxWidth, maxHeight, null);
			} else {
				bm = BitmapFactory.decodeFile(imagePath);
			}

			if (bm != null && !bm.isRecycled()) {
				int angle = PhotoBitmapUtils.readPictureDegree(imagePath);
				if (angle > 0) {
					bm = PhotoBitmapUtils.rotaingImageView(angle, bm);
				}
				CompressFormat format = CompressFormat.JPEG;
				path = PhotoFileUtils.savePhoto(savePath, bm, format);
			}
			if (bm != null && !bm.isRecycled()) {
				bm.recycle();
			}
		}
		return path;
	}



	/**回调完成后地址**/
	public static interface CompressBitmapCallback{
		public void compress(String imagePath);
	}
	
	public static interface CompressBase64Callback{
		public void finish(String[] base64);
	}

}
