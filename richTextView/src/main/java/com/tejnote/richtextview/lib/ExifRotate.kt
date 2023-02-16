package com.tejnote.richtextview.lib

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.*


object ExifRotate {
	/**
	 * Flip the image horizontally.
	 */
	private const val FLIP_H = 1

	/**
	 * Flip the image vertically.
	 */
	private const val FLIP_V = 2

	/**
	 * Flip the image horizontally and vertically.
	 */
	private const val FLIP_HV = 3

	/**
	 * Rotate the image 90 degrees clockwise.
	 */
	private const val FLIP_90CW = 4

	/**
	 * Rotate the image 90 degrees counter-clockwise.
	 */
	private const val FLIP_90CCW = 5

	/**
	 * Rotate the image 180 degrees.
	 */
	private const val FLIP_180 = 6

	fun getRotateInputStream(inputStream: InputStream): InputStream {
		val orientation = getRotation(inputStream)
		return bitmap2InputStream(rotateBitmap(BitmapFactory.decodeStream(inputStream), orientation))
	}

	@Throws(IOException::class)
	private fun read2bytes(inputStream: InputStream): Int {
		return inputStream.read() shl 8 or inputStream.read()
	}

	@Throws(IOException::class)
	private fun readByte(inputStream: InputStream): Int {
		return inputStream.read()
	}

	@Throws(IOException::class)
	private fun getRotation(inputStream: InputStream): Int {
		val exifData = IntArray(100)
		val setFlag: Int

		/* Read File head, check for JPEG SOI + Exif APP1 */for (i in 0..3) exifData[i] = readByte(inputStream)
		if (exifData[0] != 0xFF || exifData[1] != 0xD8 || exifData[2] != 0xFF || exifData[3] != 0xE1) return -2

		/* Get the marker parameter length count */
		var length = read2bytes(inputStream)
		// exif_data = new int[length];

		/* Length includes itself, so must be at least 2 */
		/* Following Exif data length must be at least 6 */
		if (length < 8) return -1
		length -= 8
		/* Read Exif head, check for "Exif" */for (i in 0..5) exifData[i] = inputStream.read()
		if (exifData[0] != 0x45 || exifData[1] != 0x78 || exifData[2] != 0x69 || exifData[3] != 0x66 || exifData[4] != 0 || exifData[5] != 0) return -1

		/* Read Exif body */length = if (length > exifData.size) exifData.size else length
		for (i in 0 until length) exifData[i] = inputStream.read()
		if (length < 12) return -1 /* Length of an IFD entry */

		/* Discover byte order */
		val isMotorola: Int = if (exifData[0] == 0x49 && exifData[1] == 0x49) 0 else if (exifData[0] == 0x4D && exifData[1] == 0x4D) 1 else return -1

		/* Check Tag Mark */if (isMotorola == 1) {
			if (exifData[2] != 0) return -1
			if (exifData[3] != 0x2A) return -1
		}
		else {
			if (exifData[3] != 0) return -1
			if (exifData[2] != 0x2A) return -1
		}

		/* Get first IFD offset (offset to IFD0) */
		var offset: Int
		if (isMotorola == 1) {
			if (exifData[4] != 0) return -1
			if (exifData[5] != 0) return -1
			offset = exifData[6]
			offset = offset shl 8
			offset += exifData[7]
		}
		else {
			if (exifData[7] != 0) return -1
			if (exifData[6] != 0) return -1
			offset = exifData[5]
			offset = offset shl 8
			offset += exifData[4]
		}
		if (offset > length - 2) return -1 /* check end of data segment */

		/* Get the number of directory entries contained in this IFD */
		var numberOfTags: Int
		if (isMotorola == 1) {
			numberOfTags = exifData[offset]
			numberOfTags = numberOfTags shl 8
			numberOfTags += exifData[offset + 1]
		}
		else {
			numberOfTags = exifData[offset + 1]
			numberOfTags = numberOfTags shl 8
			numberOfTags += exifData[offset]
		}
		if (numberOfTags == 0) return -1
		offset += 2

		/* Search for Orientation Tag in IFD0 */while (true) {
			if (offset > length - 12) return -1 /* check end of data segment */
			/* Get Tag number */
			var tagnum: Int
			if (isMotorola == 1) {
				tagnum = exifData[offset]
				tagnum = tagnum shl 8
				tagnum += exifData[offset + 1]
			}
			else {
				tagnum = exifData[offset + 1]
				tagnum = tagnum shl 8
				tagnum += exifData[offset]
			}
			if (tagnum == 0x0112) break /* found Orientation Tag */
			if (--numberOfTags == 0) return -1
			offset += 12
		}

		setFlag = if (isMotorola == 1) {
			if (exifData[offset + 8] != 0) return -1
			exifData[offset + 9]
		}
		else {
			if (exifData[offset + 9] != 0) return -1
			exifData[offset + 8]
		}
		if (setFlag > 8) return -1
		println("set_flag $setFlag")
		return setFlag
	}

	private fun rotateBitmap(src: Bitmap, operation: Int): Bitmap {

		val width: Int = src.width
		val height: Int = src.height
		val inPixels = IntArray(width * height)
		src.getPixels(inPixels, 0, width, 0, 0, width, height)
		var newW = width
		var newH = height
		when (operation) {
			FLIP_HV -> {
				newW = height
				newH = width
			}
			FLIP_90CW -> {
				newW = height
				newH = width
			}
			FLIP_90CCW -> {
				newW = height
				newH = width
			}
		}
		val newPixels = IntArray(newW * newH)
		var index: Int
		var newRow: Int
		var newCol: Int
		var newIndex: Int

		when (operation) {
			FLIP_H -> {
				for (row in 0 until height) {
					for (col in 0 until width) {
						index = row * width + col
						newRow = row
						newCol = width - col - 1
						newIndex = newRow * newW + newCol
						newPixels[newIndex] = inPixels[index]
					}
				}
			}
			FLIP_V -> {
				for (row in 0 until height) {
					for (col in 0 until width) {
						index = row * width + col
						newRow = height - row - 1
						newCol = col
						newIndex = newRow * newW + newCol
						newPixels[newIndex] = inPixels[index]
					}
				}
			}
			FLIP_HV -> {
				for (row in 0 until height) {
					for (col in 0 until width) {
						index = row * width + col
						newRow = col
						newCol = row
						newIndex = newRow * newW + newCol
						newPixels[newIndex] = inPixels[index]
					}
				}
			}
			FLIP_90CW -> {
				for (row in 0 until height) {
					for (col in 0 until width) {
						index = row * width + col
						newRow = col
						newCol = height - row - 1
						newIndex = newRow * newW + newCol
						newPixels[newIndex] = inPixels[index]
					}
				}
			}
			FLIP_90CCW -> {
				for (row in 0 until height) {
					for (col in 0 until width) {
						index = row * width + col
						newRow = width - col - 1
						newCol = row
						newIndex = newRow * newW + newCol
						newPixels[newIndex] = inPixels[index]
					}
				}
			}
			FLIP_180 -> {
				for (row in 0 until height) {
					for (col in 0 until width) {
						index = row * width + col
						newRow = height - row - 1
						newCol = width - col - 1
						newIndex = newRow * newW + newCol
						newPixels[newIndex] = inPixels[index]
					}
				}
			}
		}

		return Bitmap.createBitmap(newPixels, 0, newW, newW, newH, Bitmap.Config.ARGB_8888)
	}

	private fun bitmap2InputStream(bm: Bitmap): InputStream {
		val outputStream = ByteArrayOutputStream()
		bm.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
		return ByteArrayInputStream(outputStream.toByteArray())
	}
}