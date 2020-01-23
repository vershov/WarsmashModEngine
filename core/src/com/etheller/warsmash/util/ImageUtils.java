package com.etheller.warsmash.util;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;

/**
 * Uses AWT stuff
 *
 */
public final class ImageUtils {
	private static final int BYTES_PER_PIXEL = 4;

	public static Texture getTexture(final BufferedImage image) {
		final int[] pixels = new int[image.getWidth() * image.getHeight()];
		image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());

		// 4
		// for
		// RGBA,
		// 3
		// for
		// RGB

		final Pixmap pixmap = new Pixmap(image.getWidth(), image.getHeight(), Format.RGBA8888) {
			@Override
			public int getGLInternalFormat() {
				return GL30.GL_SRGB8_ALPHA8;
			}
		};
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				final int pixel = pixels[(y * image.getWidth()) + x];
				pixmap.drawPixel(x, y, (pixel << 8) | (pixel >>> 24));
			}
		}
		final Texture texture = new Texture(pixmap);
		return texture;
	}

	public static Buffer getTextureBuffer(final BufferedImage image) {

		final int imageWidth = image.getWidth();
		final int imageHeight = image.getHeight();
		final int[] pixels = new int[imageWidth * imageHeight];
		image.getRGB(0, 0, imageWidth, imageHeight, pixels, 0, imageWidth);

		final ByteBuffer buffer = ByteBuffer.allocateDirect(imageWidth * imageHeight * BYTES_PER_PIXEL)
				.order(ByteOrder.nativeOrder());
		// 4
		// for
		// RGBA,
		// 3
		// for
		// RGB

		for (int y = 0; y < imageHeight; y++) {
			for (int x = 0; x < imageWidth; x++) {
				final int pixel = pixels[(y * imageWidth) + x];
				buffer.put((byte) ((pixel >> 16) & 0xFF)); // Red component
				buffer.put((byte) ((pixel >> 8) & 0xFF)); // Green component
				buffer.put((byte) (pixel & 0xFF)); // Blue component
				buffer.put((byte) ((pixel >> 24) & 0xFF)); // Alpha component.
				// Only for RGBA
			}
		}

		buffer.flip();
		return buffer;
	}

	/**
	 * Convert an input buffered image into sRGB color space using component values
	 * directly instead of performing a color space conversion.
	 *
	 * @param in Input image to be converted.
	 * @return Resulting sRGB image.
	 */
	public static BufferedImage forceBufferedImagesRGB(final BufferedImage in) {
		// Resolve input ColorSpace.
		final ColorSpace inCS = in.getColorModel().getColorSpace();
		final ColorSpace sRGBCS = ColorSpace.getInstance(ColorSpace.CS_sRGB);
		if (inCS == sRGBCS) {
			// Already is sRGB.
			return in;
		}
		if (inCS.getNumComponents() != sRGBCS.getNumComponents()) {
			throw new IllegalArgumentException("Input color space has different number of components from sRGB.");
		}

		// Draw input.
		final ColorModel lRGBModel = new ComponentColorModel(inCS, true, false, Transparency.TRANSLUCENT,
				DataBuffer.TYPE_BYTE);
		final ColorModel sRGBModel = new ComponentColorModel(sRGBCS, true, false, Transparency.TRANSLUCENT,
				DataBuffer.TYPE_BYTE);
		final BufferedImage lRGB = new BufferedImage(lRGBModel,
				lRGBModel.createCompatibleWritableRaster(in.getWidth(), in.getHeight()), false, null);
		for (int i = 0; i < in.getWidth(); i++) {
			for (int j = 0; j < in.getHeight(); j++) {
				lRGB.setRGB(i, j, in.getRGB(i, j));
			}
		}

		// Convert to sRGB.
		final BufferedImage sRGB = new BufferedImage(sRGBModel, lRGB.getRaster(), false, null);

		return sRGB;
	}

	private ImageUtils() {
	}
}
