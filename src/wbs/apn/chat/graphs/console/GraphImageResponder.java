package wbs.apn.chat.graphs.console;

import static wbs.utils.etc.NumberUtils.toJavaIntegerRequired;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import lombok.NonNull;

import wbs.console.request.ConsoleRequestContext;
import wbs.console.responder.ConsoleResponder;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.graph.console.GraphScale;

import wbs.utils.io.RuntimeIoException;

public abstract
class GraphImageResponder
	extends ConsoleResponder {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// state

	protected
	int imageWidth, imageHeight;

	protected
	int borderSize;

	protected
	int xAxisSize, yAxisSize;

	protected
	int plotWidth, plotHeight;

	protected
	int xOrigin, yOrigin;

	protected
	GraphScale verticalScale;

	protected
	BufferedImage image;

	protected
	Graphics2D graphics;

	protected
	FontMetrics fontMetrics;

	protected abstract
	void prepareData (
			Transaction parentTransaction);

	protected abstract
	void prepareVerticalScale (
			Transaction parentTransaction);

	protected abstract
	void prepareImageData ();

	public
	GraphImageResponder (
			int newImageWidth,
			int newImageHeight,
			int newBorderSize) {

		imageWidth = newImageWidth;
		imageHeight = newImageHeight;
		borderSize = newBorderSize;

	}

	protected
	void prepareImageCreate () {

		// create image and graphics objects

		image =
			new BufferedImage (
				imageWidth,
				imageHeight,
				BufferedImage.TYPE_INT_RGB);

		graphics =
			image.createGraphics ();

		fontMetrics =
			graphics.getFontMetrics ();

		// enable anti-aliasing

		Map<Object,Object> renderingHints =
			new HashMap<Object,Object> ();

		renderingHints.put (
			RenderingHints.KEY_ANTIALIASING,
			RenderingHints.VALUE_ANTIALIAS_ON);

		graphics.addRenderingHints (
			renderingHints);

	}

	protected
	void prepareImageBackground () {

		// fill white

		graphics.setColor (
			Color.white);

		graphics.fillRect (
			0,
			0,
			imageWidth,
			imageHeight);

	}

	protected
	void prepareImageDimensions () {

		// work out axis sizes

		xAxisSize = 0;

		for (GraphScale.Step step
				: verticalScale.getSteps ()) {

			int stringWidth =
				fontMetrics.stringWidth (step.getLabel ());

			if (stringWidth > xAxisSize)
				xAxisSize = stringWidth;

		}

		yAxisSize =
			+ fontMetrics.getAscent ()
			+ fontMetrics.getDescent ();

		// work out graph size

		plotWidth =
			+ imageWidth
			- borderSize * 2
			- xAxisSize;

		plotHeight =
			+ imageHeight
			- borderSize * 2
			- yAxisSize;

		if (plotWidth < 0 || plotHeight < 0)
			throw new RuntimeException ("Image size is too small");

		// set origin

		xOrigin =
			+ borderSize
			+ xAxisSize;

		yOrigin =
			+ borderSize
			+ plotHeight;

	}

	protected
	void prepareImageShadingHorizontal () {

		// draw shading

		graphics.setColor (
			new Color (224, 224, 224));

		for (
			GraphScale.Step step
				: verticalScale.getSteps ()
		) {

			if (! step.isOdd () || step.isLast ())
				continue;

			graphics.fillRect (
				xOrigin,
				toJavaIntegerRequired (
					yOrigin
					- plotHeight
						* (step.getStep () + 1)
						/ verticalScale.getNumSteps ()),
				plotWidth,
				toJavaIntegerRequired (
					plotHeight
						/ verticalScale.getNumSteps ()));

		}

	}

	protected
	void prepareImageShadingVertical () {

	}

	protected
	void prepareImageAxes () {

		graphics.setColor (
			Color.black);

		graphics.drawLine (
			xOrigin,
			yOrigin - plotHeight,
			xOrigin,
			yOrigin);

		graphics.drawLine (
			xOrigin,
			yOrigin,
			xOrigin + plotWidth,
			yOrigin);

		for (
			GraphScale.Step step
				: verticalScale.getSteps ()
		) {

			String string =
				step.getLabel ();

			int y =
				toJavaIntegerRequired (
					+ yOrigin
					- plotHeight
						* step.getStep ()
						/ verticalScale.getNumSteps ());

			graphics.drawString (
				string,
				xOrigin - fontMetrics.stringWidth (string),
				y + fontMetrics.getAscent ());

			graphics.drawLine (
				xOrigin - 3,
				y,
				xOrigin,
				y);

		}
	}

	protected
	void prepareImageDone () {

		graphics.dispose ();

	}

	protected
	void prepareImage (
			@NonNull TaskLogger parentTaskLogger) {

		prepareImageCreate ();
		prepareImageDimensions ();
		prepareImageBackground ();
		prepareImageShadingHorizontal ();
		prepareImageShadingVertical ();
		prepareImageAxes ();
		prepareImageData ();
		prepareImageDone ();

	}

	@Override
	protected
	void prepare (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepare");

		) {

			prepareData (
				transaction);

			prepareVerticalScale (
				transaction);

			prepareImage (
				transaction);

		}

	}

	@Override
	protected
	void setHtmlHeaders (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"setHtmlHeaders");

		) {

			requestContext.setHeader (
				"Content-Type",
				"image/png");

		}

	}

	@Override
	protected
	void render (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"render");

		) {

			ImageIO.write (
				image,
				"PNG",
				requestContext.outputStream ());

		} catch (IOException ioException) {

			throw new RuntimeIoException (
				ioException);

		}

	}

}
