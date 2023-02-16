package com.tejnote.richtextview.lib.ig;

import android.widget.TextView;

import com.tejnote.richtextview.lib.ImageHolder;
import com.tejnote.richtextview.lib.RichTextConfig;
import com.tejnote.richtextview.lib.callback.ImageLoadNotify;
import com.tejnote.richtextview.lib.drawable.DrawableWrapper;
import com.tejnote.richtextview.lib.ext.Debug;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by zhou on 2017/7/23.
 * Assets目录图片的加载器
 */

class AssetsImageLoader extends InputStreamImageLoader implements Runnable {

	private static final String ASSETS_PREFIX = "file:///android_asset/";


	AssetsImageLoader(ImageHolder holder, RichTextConfig config, TextView textView, DrawableWrapper drawableWrapper, ImageLoadNotify iln) {
		super(holder, config, textView, drawableWrapper, iln, openAssetInputStream(holder, textView));
	}

	private static InputStream openAssetInputStream(ImageHolder holder, TextView textView) {
		try {
			String fileName = getAssetFileName(holder.getSource());
			InputStream inputStream;
			inputStream = textView.getContext().getAssets().open(fileName);
			return inputStream;
		} catch (IOException e) {
			Debug.e(e);
		}
		return null;
	}

	private static String getAssetFileName(String path) {
		if (path != null && path.startsWith(ASSETS_PREFIX)) {
			return path.replace(ASSETS_PREFIX, "");
		}
		return null;
	}

}
