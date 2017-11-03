package FCBR;

import java.io.*;
import java.util.*;

import org.jdom2.Comment;
import org.jdom2.Content;
import org.jdom2.Content.CType;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.util.IteratorIterable;

public class Parser {
	public void info() {
		System.err.println("Parser\n-> Read XML documents of the fuzzy attributes");
	}	
	public void parse(String file_name) throws JDOMException, IOException {

		File input_file = new File(file_name); 
		SAXBuilder saxBuilder = new SAXBuilder(); 
		Document document = saxBuilder.build(input_file);

		System.err.println("Parsing : "+file_name);

		Element root = document.getRootElement();
		System.err.println("Identified Root -> "+root.getName());
		
		List<Element> attributes = root.getChildren();

		for (int i=0; i<attributes.size(); i++) {
			Element attribute = attributes.get(i);
			System.out.println(attribute.getName());
			System.out.println('\t'+attribute.getChildText("name")+" ( "+ attribute.getChildText("type")+" ) ");
			
			Element fuzzy_categories = attribute.getChild("levels");

			List<Element> levels = fuzzy_categories.getChildren();

			for (int j=0; j<levels.size(); j++) {
				Element value = levels.get(j);
				System.out.println("\t\t"+value.getText());

			}

		}

	}
}