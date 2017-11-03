import java.io.*;
import java.util.*;

import org.jdom2.JDOMException;

import FCBR.Parser;

public class Main {
	public static void main(String args[]) {
		Parser P = new Parser();
		P.info();
		try {
			P.parse("TermPaperExample/attributes.xml");
		} catch(JDOMException err) {
			System.err.println(err);
		} catch (IOException err) {
			System.err.println(err);
		}
	}
}