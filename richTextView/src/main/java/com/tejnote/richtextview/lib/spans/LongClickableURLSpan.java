package com.tejnote.richtextview.lib.spans;

import android.annotation.SuppressLint;
import android.text.TextPaint;
import android.text.style.URLSpan;
import android.view.View;

import com.tejnote.richtextview.lib.LinkHolder;
import com.tejnote.richtextview.lib.callback.OnUrlClickListener;
import com.tejnote.richtextview.lib.callback.OnUrlLongClickListener;

/**
 * Created by zhou on 16-5-28.
 * LongClickableURLSpan
 */
@SuppressLint("ParcelCreator")
public class LongClickableURLSpan extends URLSpan implements LongClickableSpan {


	private final OnUrlClickListener onUrlClickListener;
	private final OnUrlLongClickListener onUrlLongClickListener;
	private final LinkHolder linkHolder;

	@SuppressWarnings("unused")
	public LongClickableURLSpan(LinkHolder linkHolder) {
		this(linkHolder, null, null);
	}

	public LongClickableURLSpan(LinkHolder linkHolder, OnUrlClickListener onUrlClickListener, OnUrlLongClickListener onUrlLongClickListener) {
		super(linkHolder.getUrl());
		this.onUrlClickListener = onUrlClickListener;
		this.onUrlLongClickListener = onUrlLongClickListener;
		this.linkHolder = linkHolder;
	}

	@Override
	public void updateDrawState(TextPaint ds) {
		ds.setColor(linkHolder.getColor());
		ds.setUnderlineText(linkHolder.isUnderLine());
	}

	@Override
	public void onClick(View widget) {
		if (onUrlClickListener != null && onUrlClickListener.urlClicked(getURL())) {
			return;
		}
		super.onClick(widget);
	}

	@Override
	public boolean onLongClick(View widget) {
		return onUrlLongClickListener != null && onUrlLongClickListener.urlLongClick(getURL());
	}

	public LongClickableURLSpan copy() {
		return new LongClickableURLSpan(linkHolder, null, null);
	}

}
