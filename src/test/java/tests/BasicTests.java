package tests;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.testng.annotations.Test;

import com.cherokeelessons.lemmatizer.Lemmatizer;

public class BasicTests {

	@Test
	public void noRegexReplaceLeaks() throws IOException {
		Lemmatizer l = new Lemmatizer();
		LineIterator lineIterator = IOUtils.lineIterator(this.getClass().getResourceAsStream("corpus-nt-web.chr"), StandardCharsets.UTF_8);
		while (lineIterator.hasNext()) {
			String line = lineIterator.next();
			line = line.replace("@", " ");
			assert !l.getFactored(line).contains("@"):reportBadFactorings();
		}
		lineIterator.close();
	}

	private String reportBadFactorings() throws IOException {
		Lemmatizer l = new Lemmatizer();
		StringBuilder sb = new StringBuilder();
		LineIterator lineIterator = IOUtils.lineIterator(this.getClass().getResourceAsStream("corpus-nt-web.chr"), StandardCharsets.UTF_8);
		while (lineIterator.hasNext()) {
			String line = lineIterator.next();
			line = line.replace("@", " ");
			line = l.getFactored(line);
			String[] parts = line.split("\\s");
			for (String part: parts) {
				if (part.contains("@")) {
					sb.append(part);
					sb.append("\n");
				}
			}
		}
		lineIterator.close();
		return sb.toString();
	}
}