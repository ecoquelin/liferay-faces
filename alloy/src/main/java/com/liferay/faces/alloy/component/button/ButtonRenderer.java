/**
 * Copyright (c) 2000-2014 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
package com.liferay.faces.alloy.component.button;

import java.io.IOException;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlOutcomeTargetButton;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.FacesRenderer;

import com.liferay.faces.util.component.Styleable;
import com.liferay.faces.util.lang.StringPool;
import com.liferay.faces.util.render.RendererUtil;


/**
 * @author  Kyle Stiemann
 */
@FacesRenderer(componentFamily = Button.COMPONENT_FAMILY, rendererType = Button.RENDERER_TYPE)
public class ButtonRenderer extends ButtonRendererCompat {

	// Private Constants
	private static final String DEFAULT_ONBLUR = "this.className=this.className.replace(' btn-focus','');";
	private static final String DEFAULT_ONFOCUS = "this.className+=' btn-focus';";
	private static final String DEFAULT_BUTTON_CSS_CLASSES = "yui3-widget btn btn-content";
	private static final String DISABLED_BUTTON_CSS_CLASSES = "btn-disabled disabled";
	private static final String FACES_RUNTIME_SRC = "facesRuntimeSrc";
	private static final String RETURN_FALSE = "return false;";

	@Override
	public void encodeBegin(FacesContext facesContext, UIComponent uiComponent) throws IOException {

		ResponseWriter responseWriter = facesContext.getResponseWriter();

		// It is not possible for the ButtonResponseWriter to intercept writing of the input element that is rendered
		// by the JSF runtime, because endElement("input") may be called in either encodeBegin() or encodeEnd(). Button
		// requires that endElement() be called in encodeEnd(), so that children can be added to the button if
		// neccessary.
		responseWriter.startElement(FacesButton.BUTTON, uiComponent);

		HTML5Button html5Button = (HTML5Button) uiComponent;
		Boolean autofocus = html5Button.isAutofocus();

		if (autofocus != null) {
			responseWriter.writeAttribute(HTML5Button.AUTOFOCUS, autofocus, HTML5Button.AUTOFOCUS);
		}

		FacesButton facesButton = (FacesButton) uiComponent;

		// Do not delegate the writing of the class or style attributes because we need to apply certain default
		// classes.
		StringBuilder classNames = new StringBuilder();
		classNames.append(DEFAULT_BUTTON_CSS_CLASSES);

		boolean disabled = facesButton.isDisabled();

		if (disabled) {

			classNames.append(StringPool.SPACE);
			classNames.append(DISABLED_BUTTON_CSS_CLASSES);
		}

		RendererUtil.encodeStyleable(responseWriter, (Styleable) facesButton, classNames.toString());

		// Do not delegate the writing of the disabled attribute because the JSF runtime may disable the button
		// programmatically based on navigation case matching.
		responseWriter.writeAttribute(FacesButton.DISABLED, disabled, FacesButton.DISABLED);

		// Do not delegate the writing of the type attribute because the JSF runtime hard codes the type for button.
		responseWriter.writeAttribute(StringPool.TYPE, facesButton.getType(), StringPool.TYPE);

		// Determine if we should delegate the rendering of onclick or render it ourselves.
		Boolean delegateOnclick = Boolean.TRUE;

		if (uiComponent instanceof HtmlOutcomeTargetButton) {

			HtmlOutcomeTargetButton htmlOutcomeTargetButton = (HtmlOutcomeTargetButton) uiComponent;

			if (htmlOutcomeTargetButton.getOutcome() == null) {

				delegateOnclick = Boolean.FALSE;

				String onclick = htmlOutcomeTargetButton.getOnclick();

				if (onclick != null) {

					// Do not delegate the writing of the onclick attribute because the JSF runtime assumes that it
					// should include navigation, and we do not need to navigate in this case.
					responseWriter.writeAttribute(StringPool.ONCLICK, onclick, StringPool.ONCLICK);
				}
			}
		}

		// Do not delegate the writing of the onfocus attribute because we need to supply a script to modify the css
		// class.
		String onfocus = facesButton.getOnfocus();

		if (onfocus == null) {
			onfocus = RETURN_FALSE;
		}

		StringBuilder onfocusBuilder = new StringBuilder();
		onfocusBuilder.append(DEFAULT_ONFOCUS);
		onfocusBuilder.append(onfocus);
		responseWriter.writeAttribute(FacesButton.ONFOCUS, onfocusBuilder.toString(), FacesButton.ONFOCUS);

		// Do not delegate the writing of the onblur attribute because we need to supply a script to modify the css
		// class.
		String onblur = facesButton.getOnblur();

		if (onblur == null) {
			onblur = RETURN_FALSE;
		}

		StringBuilder onblurBuilder = new StringBuilder();
		onblurBuilder.append(DEFAULT_ONBLUR);
		onblurBuilder.append(onblur);
		responseWriter.writeAttribute(FacesButton.ONBLUR, onblurBuilder.toString(), FacesButton.ONBLUR);

		// Do not delegate the writing of the value attribute because the JSF runtime may not render value
		Object value = facesButton.getValue();

		if (value != null) {
			responseWriter.writeAttribute(StringPool.VALUE, value.toString(), StringPool.VALUE);
		}

		// Delegate to the JSF implementation's renderer while using our own ButtonResponseWriter to control the output.
		ButtonResponseWriter buttonResponseWriter = new ButtonResponseWriter(responseWriter, delegateOnclick);
		super.encodeBegin(facesContext, uiComponent, buttonResponseWriter);
		facesContext.getAttributes().put(FACES_RUNTIME_SRC, buttonResponseWriter.getSrc());
	}

	@Override
	public void encodeChildren(FacesContext facesContext, UIComponent uiComponent) throws IOException {

		ResponseWriter responseWriter = facesContext.getResponseWriter();
		FacesButton facesButton = (FacesButton) uiComponent;

		// Do not delegate the writing of the image attribute because the image needs to be a child rather than an
		// attribute of the button.
		String image = facesButton.getImage();

		if (image != null) {
			String src = (String) facesContext.getAttributes().remove(FACES_RUNTIME_SRC);

			if (src != null) {
				responseWriter.startElement(StringPool.IMG, uiComponent);
				responseWriter.writeAttribute(StringPool.SRC, src, FacesButton.IMAGE);
				responseWriter.endElement(StringPool.IMG);
			}
		}
		else {

			if (getVisualChildCount(uiComponent) == 0) {

				// Do not delegate the writing of the value attribute because the value needs to be a child rather than
				// an attribute of the button.
				Object value = facesButton.getValue();

				if (value != null) {
					responseWriter.writeText(value.toString(), StringPool.VALUE);
				}
			}
		}

		super.encodeChildren(facesContext, uiComponent);
	}

	@Override
	public void encodeEnd(FacesContext facesContext, UIComponent uiComponent) throws IOException {

		ResponseWriter responseWriter = facesContext.getResponseWriter();
		responseWriter.endElement(FacesButton.BUTTON);
		super.encodeEnd(facesContext, uiComponent);
	}

	@Override
	public String getDelegateComponentFamily() {
		return Button.DELEGATE_COMPONENT_FAMILY;
	}

	@Override
	public String getDelegateRendererType() {
		return Button.DELEGATE_RENDERER_TYPE;
	}

	@Override
	public boolean getRendersChildren() {
		return true;
	}

	protected int getVisualChildCount(UIComponent uiComponent) {

		int uiChildCount = 0;
		List<UIComponent> children = uiComponent.getChildren();

		for (UIComponent child : children) {

			if (isVisualComponent(child)) {
				uiChildCount++;
			}
		}

		return uiChildCount;
	}
}
