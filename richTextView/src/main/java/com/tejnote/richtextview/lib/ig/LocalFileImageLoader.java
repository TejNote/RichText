package com.tejnote.richtextview.lib.ig;

import android.widget.TextView;

import com.tejnote.richtextview.lib.ImageHolder;
import com.tejnote.richtextview.lib.RichTextConfig;
import com.tejnote.richtextview.lib.callback.ImageLoadNotify;
import com.tejnote.richtextview.lib.drawable.DrawableWrapper;
import com.tejnote.richtextview.lib.exceptions.ImageDecodeException;

/**
 * Created by zhou on 2017/2/20.
 * 本地图片加载器
 */

class LocalFileImageLoader extends AbstractImageLoader<String> implements Runnable {

	LocalFileImageLoader(ImageHolder holder, RichTextConfig config, TextView textView, DrawableWrapper drawableWrapper, ImageLoadNotify iln) {
		super(holder, config, textView, drawableWrapper, iln, SourceDecode.LOCAL_FILE_SOURCE_DECODE);
	}

	@Override
	public void run() {
		try {
			doLoadImage(holder.getSource());
		} catch (Exception e) {
			onFailure(new ImageDecodeException(e));
		} catch (OutOfMemoryError error) {
			onFailure(new ImageDecodeException(error));
		}
	}
}
