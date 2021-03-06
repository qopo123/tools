/**
 * Copyright [2016-2017] [yadong.zhang]
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zyd.ztools.str2Png;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.imageio.ImageIO;

import org.fit.cssbox.css.CSSNorm;
import org.fit.cssbox.css.DOMAnalyzer;
import org.fit.cssbox.io.DOMSource;
import org.fit.cssbox.io.DefaultDOMSource;
import org.fit.cssbox.io.DefaultDocumentSource;
import org.fit.cssbox.io.DocumentSource;
import org.fit.cssbox.layout.BrowserCanvas;
import org.fit.cssbox.layout.BrowserConfig;
import org.fit.cssbox.layout.Viewport;
import org.fit.cssbox.render.SVGRenderer;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import cz.vutbr.web.css.MediaSpec;

public class ImageRenderer {
	public enum Type {
		PNG, SVG
	}

	private String mediaType = "screen";
	private Dimension windowSize;
	private boolean cropWindow = false;
	private boolean loadImages = true;
	private boolean loadBackgroundImages = true;

	public ImageRenderer() {
		windowSize = new Dimension(1200, 600);
	}

	public void setMediaType(String media) {
		mediaType = new String(media);
	}

	public void setWindowSize(Dimension size, boolean crop) {
		windowSize = new Dimension(size);
		cropWindow = crop;
	}

	public void setLoadImages(boolean content, boolean background) {
		loadImages = content;
		loadBackgroundImages = background;
	}

	/**
	 * Renders the URL and prints the result to the specified output stream in
	 * the specified format.
	 * 
	 * @param urlstring
	 *            the source URL
	 * @param out
	 *            output stream
	 * @param type
	 *            output type
	 * @return true in case of success, false otherwise
	 * @throws SAXException
	 */
	public boolean renderURL(String urlstring, OutputStream out, Type type) throws IOException, SAXException {
		if (!urlstring.startsWith("http:") && !urlstring.startsWith("https:") && !urlstring.startsWith("ftp:")
				&& !urlstring.startsWith("file:"))
			urlstring = "http://" + urlstring;

		// Open the network connection
		DocumentSource docSource = new DefaultDocumentSource(urlstring);

		// Parse the input document
		DOMSource parser = new DefaultDOMSource(docSource);
		Document doc = parser.parse();

		// create the media specification
		MediaSpec media = new MediaSpec(mediaType);
		media.setDimensions(windowSize.width, windowSize.height);
		media.setDeviceDimensions(windowSize.width, windowSize.height);

		// Create the CSS analyzer
		DOMAnalyzer da = new DOMAnalyzer(doc, docSource.getURL());
		da.setMediaSpec(media);
		// convert the HTML presentation attributes to inline styles
		da.attributesToStyles();
		// use the standard style sheet
		da.addStyleSheet(null, CSSNorm.stdStyleSheet(), DOMAnalyzer.Origin.AGENT);
		// use the additional style sheet
		da.addStyleSheet(null, CSSNorm.userStyleSheet(), DOMAnalyzer.Origin.AGENT);
		// render form fields using css
		da.addStyleSheet(null, CSSNorm.formsStyleSheet(), DOMAnalyzer.Origin.AGENT);
		// load the author style sheets
		da.getStyleSheets();

		BrowserCanvas contentCanvas = new BrowserCanvas(da.getRoot(), da, docSource.getURL());
		// we have a correct media specification, do not update
		contentCanvas.setAutoMediaUpdate(false);
		contentCanvas.getConfig().setClipViewport(cropWindow);
		contentCanvas.getConfig().setLoadImages(loadImages);
		contentCanvas.getConfig().setLoadBackgroundImages(loadBackgroundImages);

		if (type == Type.PNG) {
			contentCanvas.createLayout(windowSize);
			BufferedImage image = contentCanvas.getImage();

			ImageIO.write(image, "png", out);
		} else if (type == Type.SVG) {
			setDefaultFonts(contentCanvas.getConfig());
			contentCanvas.createLayout(windowSize);
			Writer w = new OutputStreamWriter(out, "utf-8");
			writeSVG(contentCanvas.getViewport(), w);
			w.close();
		}

		docSource.close();

		return true;
	}

	/**
	 * @Description 自定义
	 * @author zhangyd
	 * @date 2017年3月14日 下午2:49:22
	 * @param urlstring
	 * @param file
	 * @param type
	 * @return
	 * @throws IOException
	 * @throws SAXException
	 */
	public boolean renderURLByFile(String urlstring, File file, Type type) throws IOException, SAXException {
		if (file == null) {
			throw new NullPointerException("File参数不可为空");
		}
		if (!urlstring.startsWith("http:") && !urlstring.startsWith("https:") && !urlstring.startsWith("ftp:")
				&& !urlstring.startsWith("file:"))
			urlstring = "http://" + urlstring;

		// Open the network connection
		DocumentSource docSource = new DefaultDocumentSource(urlstring);

		// Parse the input document
		DOMSource parser = new DefaultDOMSource(docSource);
		Document doc = parser.parse();

		// create the media specification
		MediaSpec media = new MediaSpec(mediaType);
		media.setDimensions(windowSize.width, windowSize.height);
		media.setDeviceDimensions(windowSize.width, windowSize.height);

		// Create the CSS analyzer
		DOMAnalyzer da = new DOMAnalyzer(doc, docSource.getURL());
		da.setMediaSpec(media);
		// convert the HTML presentation attributes to inline styles
		da.attributesToStyles();
		// use the standard style sheet
		da.addStyleSheet(null, CSSNorm.stdStyleSheet(), DOMAnalyzer.Origin.AGENT);
		// use the additional style sheet
		da.addStyleSheet(null, CSSNorm.userStyleSheet(), DOMAnalyzer.Origin.AGENT);
		// render form fields using css
		da.addStyleSheet(null, CSSNorm.formsStyleSheet(), DOMAnalyzer.Origin.AGENT);
		// load the author style sheets
		da.getStyleSheets();

		BrowserCanvas contentCanvas = new BrowserCanvas(da.getRoot(), da, docSource.getURL());
		// we have a correct media specification, do not update
		contentCanvas.setAutoMediaUpdate(false);
		contentCanvas.getConfig().setClipViewport(cropWindow);
		contentCanvas.getConfig().setLoadImages(loadImages);
		contentCanvas.getConfig().setLoadBackgroundImages(loadBackgroundImages);

		if (type == Type.PNG) {
			contentCanvas.createLayout(windowSize);
			BufferedImage image = contentCanvas.getImage();
			ImageIO.write(image, "png", file);
		}

		docSource.close();

		return true;
	}

	/**
	 * Sets some common fonts as the defaults for generic font families.
	 */
	protected void setDefaultFonts(BrowserConfig config) {
		config.setDefaultFont(Font.SERIF, "Times New Roman");
		config.setDefaultFont(Font.SANS_SERIF, "Arial");
		config.setDefaultFont(Font.MONOSPACED, "Courier New");
	}

	/**
	 * Renders the viewport using an SVGRenderer to the given output writer.
	 * 
	 * @param vp
	 * @param out
	 * @throws IOException
	 */
	protected void writeSVG(Viewport vp, Writer out) throws IOException {
		// obtain the viewport bounds depending on whether we are clipping to
		// viewport size or using the whole page
		int w = vp.getClippedContentBounds().width;
		int h = vp.getClippedContentBounds().height;

		SVGRenderer render = new SVGRenderer(w, h, out);
		vp.draw(render);
		render.close();
	}

	// =================================================================================

	public static void main(String[] args) {
		if (args.length != 3) {
			System.err.println("Usage: ImageRenderer <url> <output_file> <format>");
			System.err.println();
			System.err.println("Renders a document at the specified URL and stores the document image");
			System.err.println("to the specified file.");
			System.err.println("Supported formats:");
			System.err.println("png: a Portable Network Graphics file (bitmap image)");
			System.err.println("svg: a SVG file (vector image)");
			System.exit(0);
		}

		try {
			Type type = null;
			if (args[2].equalsIgnoreCase("png"))
				type = Type.PNG;
			else if (args[2].equalsIgnoreCase("svg"))
				type = Type.SVG;
			else {
				System.err.println("Error: unknown format");
				System.exit(0);
			}

			FileOutputStream os = new FileOutputStream(args[1]);

			ImageRenderer r = new ImageRenderer();
			r.renderURL(args[0], os, type);

			os.close();
			System.err.println("Done.");
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}

	}

}
