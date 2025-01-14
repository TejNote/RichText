package com.tejnote.richtextview.lib.cache;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.LruCache;

import com.jakewharton.disklrucache.DiskLruCache;
import com.tejnote.richtextview.lib.drawable.DrawableSizeHolder;
import com.tejnote.richtextview.lib.ext.Debug;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by zhou on 2017/3/25.
 * Bitmap cache pool
 */
public class BitmapPool {

	private static final String RICH_TEXT_DIR_NAME = "_rt";
	private static final int MAX_SIZE_LOCAL_CACHE_SIZE = 1024 * 1024;
	private static final int MAX_TEMP_LOCAL_CACHE_SIZE = 1024 * 1024 * 500;


	private static final int bitmapCacheSize = (int) (Runtime.getRuntime().maxMemory() / 4);
	private static final int MAX_SIZE_CACHE_SIZE = 100;
	private static final String SIZE_DIR_NAME = "_s";
	private static final String TEMP_DIR_NAME = "_t";
	private static final int version = 1;
	private static File cacheDir;
	private static DiskLruCache sizeDiskLruCache;
	private static DiskLruCache tempDiskLruCache;
	private static File sizeDir;
	private static File tempDir;
	private LruCache<String, Bitmap> bitmapCache;
	private LruCache<String, DrawableSizeHolder> sizeHolderCache;

	private BitmapPool() {
		bitmapCache = new LruCache<String, Bitmap>(bitmapCacheSize) {
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				return bitmap == null ? 0 : bitmap.getRowBytes() * bitmap.getHeight();
			}
		};
		sizeHolderCache = new LruCache<>(MAX_SIZE_CACHE_SIZE);
	}

	public static BitmapPool getPool() {
		return BitmapPoolHolder.BITMAP_POOL;
	}

	public static void setCacheDir(File cacheDir) {
		if (BitmapPool.cacheDir == null && cacheDir != null) {
			BitmapPool.cacheDir = cacheDir;
			File richTextDir = new File(cacheDir, RICH_TEXT_DIR_NAME);
			if (!richTextDir.exists()) {
                //noinspection ResultOfMethodCallIgnored
                richTextDir.mkdir();
            }
            sizeDir = new File(richTextDir, SIZE_DIR_NAME);
            if (!sizeDir.exists()) {
                //noinspection ResultOfMethodCallIgnored
                sizeDir.mkdir();
            }
            tempDir = new File(richTextDir, TEMP_DIR_NAME);
            if (!tempDir.exists()) {
                //noinspection ResultOfMethodCallIgnored
                tempDir.mkdir();
            }
        }
    }

    public static int getVersion() {
        return version;
    }

    private static DiskLruCache getSizeDiskLruCache() {

        if (sizeDiskLruCache == null && cacheDir != null) {
			try {
				sizeDiskLruCache = DiskLruCache.open(sizeDir, version, 1, MAX_SIZE_LOCAL_CACHE_SIZE);
			} catch (IOException e) {
				Debug.e(e);
			}
		}

		return sizeDiskLruCache;
	}

	private static DiskLruCache getTempDiskLruCache() {
		if (tempDiskLruCache == null && cacheDir != null) {
			try {
				tempDiskLruCache = DiskLruCache.open(tempDir, version, 1, MAX_TEMP_LOCAL_CACHE_SIZE);
			} catch (IOException e) {
				Debug.e(e);
			}
		}
		return tempDiskLruCache;
	}

	public void cacheBitmap(String key, Bitmap bitmap) {
		bitmapCache.put(key, bitmap);
	}

	public void cacheSize(String key, DrawableSizeHolder drawableSizeHolder) {
		sizeHolderCache.put(key, drawableSizeHolder);
		CacheIOHelper.SIZE_CACHE_IO_HELPER.writeToCache(key, drawableSizeHolder, getSizeDiskLruCache());
	}

	public void cache(String key, Bitmap bitmap, DrawableSizeHolder drawableSizeHolder) {
		cacheBitmap(key, bitmap);
		cacheSize(key, drawableSizeHolder);
	}

	public Bitmap getBitmap(String key) {
		return bitmapCache.get(key);
	}

	public DrawableSizeHolder getSizeHolder(String key) {
		DrawableSizeHolder drawableSizeHolder = sizeHolderCache.get(key);
		if (drawableSizeHolder == null) {
			drawableSizeHolder = CacheIOHelper.SIZE_CACHE_IO_HELPER.readFromCache(key, getSizeDiskLruCache());
		}
		return drawableSizeHolder;
	}

	public void writeBitmapToTemp(String key, InputStream inputStream) {
		CacheIOHelper.REMOTE_IMAGE_CACHE_IO_HELPER.writeToCache(key, inputStream, getTempDiskLruCache());
	}

	public InputStream readBitmapFromTemp(String key) {
		InputStream test = CacheIOHelper.REMOTE_IMAGE_CACHE_IO_HELPER.readFromCache(key, getTempDiskLruCache());
		try {
			ExifInterface exifInterface = new ExifInterface(test);
			int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

			Bitmap bitmap = BitmapFactory.decodeStream(CacheIOHelper.REMOTE_IMAGE_CACHE_IO_HELPER.readFromCache(key, getTempDiskLruCache()));

			Matrix matrix = new Matrix();

			if (ExifInterface.ORIENTATION_ROTATE_90 == orientation) {
				matrix.postRotate(90);
			} else if (ExifInterface.ORIENTATION_ROTATE_270 == orientation) {
				matrix.postRotate(270);
			}
			Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			rotatedBitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
			byte[] bitmapData = bos.toByteArray();
			ByteArrayInputStream bs = new ByteArrayInputStream(bitmapData);
			return bs;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean hasBitmapLocalCache(String key) {
		return CacheIOHelper.REMOTE_IMAGE_CACHE_IO_HELPER.hasCache(key, getTempDiskLruCache());
	}

	public void clear() {
		bitmapCache.evictAll();
		sizeHolderCache.evictAll();
	}

	@SuppressWarnings("unused")
	public void clearLocalDiskCache() {
		try {
			DiskLruCache sizeDiskLruCache = getSizeDiskLruCache();
			if (sizeDiskLruCache != null) {
				sizeDiskLruCache.delete();
			}
		} catch (IOException e) {
			Debug.e(e);
		}
	}

	private static class BitmapPoolHolder {
		private static final BitmapPool BITMAP_POOL = new BitmapPool();
	}
}
