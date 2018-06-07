/*
 * Copyright [2018] [Michael Bulla, michaelbulla@gmail.com]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.popper.gherkin.listener;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.popper.gherkin.Narrative;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of {@link GherkinListener} writing events to xml file
 * 
 * @author Michael
 *
 */
public class XmlGherkinListener implements GherkinFileListener {
	
	private Document doc;
	
	private Element actualStory;
	
	private Element actualScenario;

	@Override
	public void storyStarted(Class<?> storyClass) {
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

			// create the root element
			actualStory = doc.createElement("story");
			actualStory.setAttribute("name", storyClass.getSimpleName());
			actualStory.setAttribute("path", storyClass.getName().replace('.', '/'));
			doc.appendChild(actualStory);
		} catch (ParserConfigurationException re) {
			throw new IllegalStateException(re);
		}
	}

	@Override
	public void narrative(Narrative narrative) {
		Element inOrder = doc.createElement("inOrderTo");
		inOrder.setTextContent(narrative.inOrderTo());
		actualStory.appendChild(inOrder);
		
		Element iWant = doc.createElement("iWantTo");
		iWant.setTextContent(narrative.iWantTo());
		actualStory.appendChild(iWant);
		
		Element asA = doc.createElement("asA");
		asA.setTextContent(narrative.asA());
		actualStory.appendChild(asA);
	}

	@Override
	public void scenarioStarted(String scenarioTitle, Method method) {
		actualScenario = doc.createElement("scenario");
		actualScenario.setAttribute("title", scenarioTitle);
		actualStory.appendChild(actualScenario);
	}

	@Override
	public void stepExecutionFailed(String stepName, Throwable throwable) {
		Element step = doc.createElement("step");
		step.setAttribute("name", stepName);
		step.setAttribute("state", "failed");
		
		Element failure = doc.createElement("failure");
		failure.setTextContent(throwableToString(throwable));
		step.appendChild(failure);
		
		actualScenario.appendChild(step);
	}

	@Override
	public void stepExecutionSucceed(String stepName) {
		Element step = doc.createElement("step");
		step.setAttribute("name", stepName);
		step.setAttribute("state", "success");
		actualScenario.appendChild(step);
	}
	
	@Override
	public void stepExecutionSkipped(String stepName) {
		Element step = doc.createElement("step");
		step.setAttribute("name", stepName);
		step.setAttribute("state", "skipped");
		actualScenario.appendChild(step);
	}

	@Override
	public void scenarioFailed(String scenarioTitle, Method method, Throwable throwable) {
		Element failure = doc.createElement("failure");
		failure.setTextContent(throwableToString(throwable));
		actualScenario.appendChild(failure);

		actualStory.appendChild(actualScenario);
		actualScenario = null;
	}

	@Override
	public void scenarioSucceed(String scenarioTitle, Method method) {
		actualStory.appendChild(actualScenario);
		actualScenario = null;
	}
	
	@Override
	public void toFile(File baseDir) {
		if (actualStory == null) {
			return;
		}
		
		try {
			 Transformer tr = TransformerFactory.newInstance().newTransformer();
	         tr.setOutputProperty(OutputKeys.INDENT, "yes");
	         tr.setOutputProperty(OutputKeys.METHOD, "xml");
	         tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
	         tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
	
	         // send DOM to file
	         tr.transform(new DOMSource(doc),
	                              new StreamResult(baseDir.getAbsoluteFile() + "/" + actualStory.getAttribute("name") + ".xml"));
		} catch (TransformerFactoryConfigurationError | TransformerException e) {
			throw new IllegalStateException(e);
		} finally {
			actualStory = null;
		}
	}
	
	private String throwableToString(Throwable throwable) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter pw = new PrintWriter(stringWriter);
		throwable.printStackTrace(pw);
		return stringWriter.toString();
	}
}
