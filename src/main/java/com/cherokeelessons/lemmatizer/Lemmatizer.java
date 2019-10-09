package com.cherokeelessons.lemmatizer;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;

public class Lemmatizer extends Thread {

	private static final List<PatternMatchReplacement> pronouns;
	private static final String pronoun_splitter;
	private static final String[] pronoun_matches;
	private static final String[] pronoun_replacements;

	private static final List<PatternMatchReplacement> suffixes;
	private static final String suffix_splitter;
	private static final String[] suffix_matches;
	private static final String[] suffix_replacements;

	private static final String[] c_v;

	static {
		c_v = new String[] { "Ꭵ", "Ꭼ", "Ꮂ", "Ꮈ", "Ꮕ", "Ꮛ", "Ꮢ", "Ꮫ", "Ꮲ", "Ꮸ", "Ꮾ", "Ᏼ" };

		StringBuilder tmp = new StringBuilder();
		pronouns = getPronounsMatches();
		List<String> tmp_matches = new ArrayList<>();
		List<String> tmp_replacements = new ArrayList<>();
		for (PatternMatchReplacement p : pronouns) {
			if (tmp.length() > 0) {
				tmp.append("|");
			}
			tmp.append(p.regex_match);
			tmp_matches.addAll(Arrays.asList(p.match));
			tmp_replacements.addAll(Arrays.asList(p.replacement));
		}
		pronoun_splitter = "\\b(" + tmp.toString() + ")([Ꭰ-Ᏼ]{2,})";
		pronoun_matches = tmp_matches.toArray(new String[0]);
		pronoun_replacements = tmp_replacements.toArray(new String[0]);

		suffixes = getSuffixMatches();
		tmp.setLength(0);
		tmp_matches.clear();
		tmp_replacements.clear();
		for (PatternMatchReplacement p : suffixes) {
			if (tmp.length() > 0) {
				tmp.append("|");
			}
			tmp.append(p.regex_match);
			tmp_matches.addAll(Arrays.asList(p.match));
			tmp_replacements.addAll(Arrays.asList(p.replacement));
		}
		suffix_splitter = "([Ꭰ-Ᏼ]{3,})(" + tmp.toString() + ")\\b";
		suffix_matches = tmp_matches.toArray(new String[0]);
		suffix_replacements = tmp_replacements.toArray(new String[0]);
	}

	private void process() throws IOException {
		PrintStream writer = System.out;
		LineIterator lines = IOUtils.lineIterator(System.in, StandardCharsets.UTF_8);
		while (lines.hasNext()) {
			String line = lines.next();

			String[] words = line.split("\\s");
			if (words == null || words.length == 0) {
				continue;
			}

			line = "";
			for (String word : words) {
				FactoredForm ff = new FactoredForm();
				ff.setSurfaceForm(word);
				ff.setLemma(word);

				simpleAffixSplits(ff);
				doWithoutExtractions(ff);
				doAlreadyHaveExtractions(ff);
				doAmbulativeExtractions(ff);
				doApproachingExtractions(ff);
				doDepartingExtractions(ff);
				doCompletelyExtractions(ff);
				doWillAlreadyExtractions(ff);
				doBenefactiveExtractions(ff);
				suffixSplits(ff);
				simplePronounSplits(ff);
				line = (line.isEmpty() ? line : line + " ") + ff.getSurfaceForm() + "|" + ff.getLemma() + "|"
						+ ff.getPrefixes() + "|" + ff.getSuffixes();
			}
			writer.println(line);
		}
		lines.close();
		writer.flush();
	}

	private String regex_benefactive = "([Ꭰ-Ᏼ]{2,})([ᎡᎨᎮᎴᏁᏇᏎᏕᏖᏞᏤᏪᏰ])(Ꭽ|ᎸᎢ?|ᎰᎢ?|ᎲᎢ?|ᎮᎢ?|ᎮᏍᏗ|Ꮅ|Ꮧ)\\b";
	private String regex_benefactiveReplace = "$1@@$2 @Ꭱ$3 ";

	private void doBenefactiveExtractions(FactoredForm ff) {
		String line = ff.getLemma();
		line = line.replaceAll(regex_benefactive, regex_benefactiveReplace);
		if (line.contains("@@")) {
			line = line.replace("@@Ꭱ ", "ᎥᎢ ");
			line = line.replace("@@Ꭸ ", "ᎬᎢ ");
			line = line.replace("@@Ꭾ ", "ᎲᎢ ");
			line = line.replace("@@Ꮄ ", "ᎸᎢ ");
			line = line.replace("@@Ꮑ ", "ᏅᎢ ");
			line = line.replace("@@Ꮗ ", "ᏋᎢ ");
			line = line.replace("@@Ꮞ ", "ᏒᎢ ");
			line = line.replace("@@Ꮥ ", "ᏛᎢ ");
			line = line.replace("@@Ꮦ ", "ᏛᎢ ");
			line = line.replace("@@Ꮮ ", "ᏢᎢ ");
			line = line.replace("@@Ꮴ ", "ᏨᎢ ");
			line = line.replace("@@Ꮺ ", "ᏮᎢ ");
			line = line.replace("@@Ᏸ ", "ᏴᎢ ");
			
			String suffix = line.replaceAll(".* @(.*)", "$1");
			line = line.replaceAll("(.*) @.*", "$1");
			
			ff.setLemma(line);
			ff.addSuffix(suffix);
		}
	}


	public Lemmatizer() {
	}

	private static List<PatternMatchReplacement> getSuffixMatches() {
		PatternMatchReplacement p;
		List<PatternMatchReplacement> list = new ArrayList<>();
		p = new PatternMatchReplacement();
		p.regex_match = "[" + StringUtils.join(c_v) + "]Ꮎ";
		String match = "";
		String replace = "";
		for (String c : c_v) {
			match += "@@" + c + "Ꮎ,";
			replace += c + "Ꭲ @ᎥᎾ,";
		}
		p.match = StringUtils.split(match, ",");
		p.replacement = StringUtils.split(replace, ",");
		list.add(p);

		return list;
	}

	private static List<PatternMatchReplacement> getPronounsMatches() {
		List<PatternMatchReplacement> matches = new ArrayList<>();
		PatternMatchReplacement p;

		p = new PatternMatchReplacement();
		p.regex_match = "[ᎾᏁᏂᏃᏄᏅ]";
		p.match = new String[] { "Ꮎ@@", "Ꮑ@@", "Ꮒ@@", "Ꮓ@@", "Ꮔ@@", "Ꮕ@@" };
		p.replacement = new String[] { "Ꮒ@ Ꭰ", "Ꮒ@ Ꭱ", "Ꮒ@ ", "Ꮒ@ Ꭳ", "Ꮒ@ Ꭴ", "Ꮒ@ Ꭵ" };
		matches.add(p);
		matches.add(prefixWithYi(p));

		p = new PatternMatchReplacement();
		p.regex_match = "[ᏓᏕᏙᏚᏛ]";
		p.match = new String[] { "Ꮣ@@", "Ꮥ@@", "Ꮩ@@", "Ꮪ@@", "Ꮫ@@" };
		p.replacement = new String[] { "Ꮥ@ Ꭰ", "Ꮥ@ Ꭱ", "Ꮥ@ Ꭳ", "Ꮥ@ Ꭴ", "Ꮥ@ Ꭵ" };
		matches.add(p);
		matches.add(prefixWithNi(p));
		matches.add(prefixWithYi(p));
		matches.add(prefixWithYi(prefixWithNi(p)));

		p = new PatternMatchReplacement();
		p.regex_match = "Ꭼ[ᏯᏰᏱᏲᏳᏴ]?"; // => Ꭼxx@@ => Ꭼ@ x
		p.match = new String[] { "Ꭼ@@", "ᎬᏯ@@", "ᎬᏰ@@", "ᎬᏲ@@", "ᎬᏳ@@", "ᎬᏴ@@" };
		p.replacement = new String[] { "Ꭼ@ ", "Ꭼ@ Ꭰ", "Ꭼ@ Ꭱ", "Ꭼ@ Ꭳ", "Ꭼ@ Ꭴ", "Ꭼ@ Ꭵ" };
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);

		p = new PatternMatchReplacement();
		p.regex_match = "ᏍᎩ";
		p.match = new String[] { "ᏍᎩ@@" };
		p.replacement = new String[] { "ᏍᎩ@ " };
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);

		p = new PatternMatchReplacement();
		p.regex_match = "Ꮝ[ᏆᏇᎩᏉᏊᏋ]";
		p.match = new String[] { "ᏍᏆ@@", "ᏍᏇ@@", "ᏍᏉ@@", "Ꮚ@@", "ᏍᏋ@@" };
		p.replacement = new String[] { "ᏍᎩ@ Ꭰ", "ᏍᎩ@ Ꭱ", "ᏍᎩ@ Ꭳ", "ᏍᎩ@ Ꭴ", "ᏍᎩ@ Ꭵ" };
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);

		p = new PatternMatchReplacement();
		p.regex_match = "ᏍᏛ[ᏯᏰᏲᏳᏴ]?";
		p.match = new String[] { "ᏍᏛ@@", "ᏍᏛᏯ@@", "ᏍᏛᏰ@@", "ᏍᏛᏲ@@", "ᏍᏛᏳ@@", "ᏍᏛᏴ@@" };
		p.replacement = new String[] { "ᏍᏛ@ ", "ᏍᏛ@ Ꭰ", "ᏍᏛ@ Ꭱ", "ᏍᏛ@ Ꭳ", "ᏍᏛ@ Ꭴ", "ᏍᏛ@ Ꭵ" };
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);

		p = new PatternMatchReplacement();
		p.regex_match = "ᏍᎩᏂ";
		p.match = new String[] { "ᏍᎩᏂ@@" };
		p.replacement = new String[] { "ᏍᎩᏂ@ " };
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);

		p = new PatternMatchReplacement();
		p.regex_match = "ᏍᎩ[ᎾᏁᏃᏄᏅ]?";
		p.match = new String[] { "ᏍᎩᎾ@@", "ᏍᎩᏁ@@", "ᏍᎩᏃ@@", "ᏍᎩᏄ@@", "ᏍᎩᏅ@@" };
		p.replacement = new String[] { "ᏍᎩᏂ@ Ꭰ", "ᏍᎩᏂ@ Ꭱ", "ᏍᎩᏂ@ Ꭳ", "ᏍᎩᏂ@ Ꭴ", "ᏍᎩᏂ@ Ꭵ" };
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);

		p = new PatternMatchReplacement();
		p.regex_match = "ᎢᏨ[ᏯᏰᏲᏳᏴ]?";
		p.match = new String[] { "ᎢᏨ@@", "ᎢᏨᏯ@@", "ᎢᏨᏰ@@", "ᎢᏨᏲ@@", "ᎢᏨᏳ@@", "ᎢᏨᏴ@@" };
		p.replacement = new String[] { "ᎢᏨ@ ", "ᎢᏨ@ Ꭰ", "ᎢᏨ@ Ꭱ", "ᎢᏨ@ Ꭳ", "ᎢᏨ@ Ꭴ", "ᎢᏨ@ Ꭵ" };
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);

		p = new PatternMatchReplacement();
		p.regex_match = "ᎢᏍᎩ[ᏯᏰᏲᏳᏴ]?";
		p.match = new String[] { "ᎢᏍᎩ@@", "ᎢᏍᎩᏯ@@", "ᎢᏍᎩᏰ@@", "ᎢᏍᎩᏲ@@", "ᎢᏍᎩᏳ@@", "ᎢᏍᎩᏴ@@" };
		p.replacement = new String[] { "ᎢᏍᎩ@ ", "ᎢᏍᎩ@ Ꭰ", "ᎢᏍᎩ@ Ꭱ", "ᎢᏍᎩ@ Ꭳ", "ᎢᏍᎩ@ Ꭴ", "ᎢᏍᎩ@ Ꭵ" };
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);

		p = new PatternMatchReplacement();
		p.regex_match = "Ꮵ[ᏯᏰᏲᏳᏴ]?";
		p.match = new String[] { "Ꮵ@@", "ᏥᏯ@@", "ᏥᏰ@@", "ᏥᏲ@@", "ᏥᏳ@@", "ᏥᏴ@@" };
		p.replacement = new String[] { "Ꮵ@ ", "Ꮵ@ Ꭰ", "Ꮵ@ Ꭱ", "Ꮵ@ Ꭳ", "Ꮵ@ Ꭴ", "Ꮵ@ Ꭵ" };
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);

		p = new PatternMatchReplacement();
		p.regex_match = "ᎠᎩ";
		p.match = new String[] { "ᎠᎩ@@" };
		p.replacement = new String[] { "ᎠᎩ@ " };
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);

		p = new PatternMatchReplacement();
		p.regex_match = "Ꭰ[ᏆᏇᏉᏊᏋ]";
		p.match = new String[] { "ᎠᏆ@@", "ᎠᏇ@@", "ᎠᏉ@@", "ᎠᏊ@@", "ᎠᏋ@@" };
		p.replacement = new String[] { "ᎠᎩ@ Ꭰ", "ᎠᎩ@ Ꭱ", "ᎠᎩ@ Ꭳ", "ᎠᎩ@ Ꭴ", "ᎠᎩ@ Ꭵ" };
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);

		p = new PatternMatchReplacement();
		p.regex_match = "Ꭿ[ᏯᏰᏲᏳᏴ]?";
		p.match = new String[] { "Ꭿ@@", "ᎯᏯ@@", "ᎯᏰ@@", "ᎯᏲ@@", "ᎯᏳ@@", "ᎯᏴ@@" };
		p.replacement = new String[] { "Ꭿ@ ", "Ꭿ@ Ꭰ", "Ꭿ@ Ꭱ", "Ꭿ@ Ꭳ", "Ꭿ@ Ꭴ", "Ꭿ@ Ꭵ" };
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);

		// this pattern has too many false hits
//		p = new PatternMatchReplacement();
//		p.regex_match = "[ᏣᏤᏦᏧᏨ]";
//		p.match = new String[] {"Ꮳ@@", "Ꮴ@@", "Ꮶ@@", "Ꮷ@@", "Ꮸ@@"};
//		p.replacement  = new String[] {"Ꮳ@ ", "Ꮳ@ Ꭱ", "Ꮳ@ Ꭳ", "Ꮳ@ Ꭴ", "Ꮳ@ Ꭵ"};
//		matches.add(p);

		p = new PatternMatchReplacement();
		p.regex_match = "ᎡᏍ[ᏓᏕᏗᏙᏚᏛ]";
		p.match = new String[] { "ᎡᏍᏓ@@", "ᎡᏍᏕ@@", "ᎡᏍᏗ@@", "ᎡᏍᏙ@@", "ᎡᏍᏚ@@", "ᎡᏍᏛ@@" };
		p.replacement = new String[] { "ᎡᏍᏗ@ Ꭰ", "ᎡᏍᏗ@ Ꭱ", "ᎡᏍᏗ@ ", "ᎡᏍᏗ@ Ꭳ", "ᎡᏍᏗ@ Ꭴ", "ᎡᏍᏗ@ Ꭵ" };
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);

		p = new PatternMatchReplacement();
		p.regex_match = "Ꮝ[ᏓᏕᏗᏙᏚᏛ]";
		p.match = new String[] { "ᏍᏓ@@", "ᏍᏕ@@", "ᏍᏗ@@", "ᏍᏙ@@", "ᏍᏚ@@", "ᏍᏛ@@" };
		p.replacement = new String[] { "ᏍᏗ@ Ꭰ", "ᏍᏗ@ Ꭱ", "ᏍᏗ@ ", "ᏍᏗ@ Ꭳ", "ᏍᏗ@ Ꭴ", "ᏍᏗ@ Ꭵ" };
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);

		p = new PatternMatchReplacement();
		p.regex_match = "Ꭱ[ᏣᏤᏥᏦᏧᏨ]";
		p.match = new String[] { "ᎡᏣ@@", "ᎡᏤ@@", "ᎡᏥ@@", "ᎡᏦ@@", "ᎡᏧ@@", "ᎡᏨ@@" };
		p.replacement = new String[] { "ᎡᏥ@ Ꭰ", "ᎡᏥ@ Ꭱ", "ᎡᏥ@ ", "ᎡᏥ@ Ꭳ", "ᎡᏥ@ Ꭴ", "ᎡᏥ@ Ꭵ" };
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);

		p = new PatternMatchReplacement();
		p.regex_match = "Ꭲ[ᏣᏤᏥᏦᏧᏨ]";
		p.match = new String[] { "ᎢᏣ@@", "ᎢᏤ@@", "ᎢᏥ@@", "ᎢᏦ@@", "ᎢᏧ@@", "ᎢᏨ@@" };
		p.replacement = new String[] { "ᎢᏥ@ Ꭰ", "ᎢᏥ@ Ꭱ", "ᎢᏥ@ ", "ᎢᏥ@ Ꭳ", "ᎢᏥ@ Ꭴ", "ᎢᏥ@ Ꭵ" };
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);

		p = new PatternMatchReplacement();
		p.regex_match = "Ꭱ[ᏓᏕᏗᏙᏚᏛ]";
		p.match = new String[] { "ᎡᏓ@@", "ᎡᏕ@@", "ᎡᏗ@@", "ᎡᏙ@@", "ᎡᏚ@@", "ᎡᏛ@@" };
		p.replacement = new String[] { "ᎡᏗ@ Ꭰ", "ᎡᏗ@ Ꭱ", "ᎡᏗ@ ", "ᎡᏗ@ Ꭳ", "ᎡᏗ@ Ꭴ", "ᎡᏗ@ Ꭵ" };
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);

		p = new PatternMatchReplacement();
		p.regex_match = "Ꭲ[ᏓᏕᏗᏙᏚᏛ]";
		p.match = new String[] { "ᎢᏓ@@", "ᎢᏕ@@", "ᎢᏗ@@", "ᎢᏙ@@", "ᎢᏚ@@", "ᎢᏛ@@" };
		p.replacement = new String[] { "ᎢᏗ@ Ꭰ", "ᎢᏗ@ Ꭱ", "ᎢᏗ@ ", "ᎢᏗ@ Ꭳ", "ᎢᏗ@ Ꭴ", "ᎢᏗ@ Ꭵ" };
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);

		p = new PatternMatchReplacement();
		p.regex_match = "Ꭲ[ᎦᎨᎩᎪᎫᎬ]";
		p.match = new String[] { "ᎢᎦ@@", "ᎢᎨ@@", "ᎢᎩ@@", "ᎢᎪ@@", "ᎢᎫ@@", "ᎢᎬ@@" };
		p.replacement = new String[] { "ᎢᎩ@ Ꭰ", "ᎢᎩ@ Ꭱ", "ᎢᎩ@ ", "ᎢᎩ@ Ꭳ", "ᎢᎩ@ Ꭴ", "ᎢᎩ@ Ꭵ" };
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);

		p = new PatternMatchReplacement();
		p.regex_match = "Ꭱ[ᎾᏁᏂᏃᏄᏅ]";
		p.match = new String[] { "ᎡᎾ@@", "ᎡᏁ@@", "ᎡᏂ@@", "ᎡᏃ@@", "ᎡᏄ@@", "ᎡᏅ@@" };
		p.replacement = new String[] { "ᎡᏂ@ Ꭰ", "ᎡᏂ@ Ꭱ", "ᎡᏂ@ ", "ᎡᏂ@ Ꭳ", "ᎡᏂ@ Ꭴ", "ᎡᏂ@ Ꭵ" };
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);

		p = new PatternMatchReplacement();
		p.regex_match = "Ꭲ[ᎾᏁᏂᏃᏄᏅ]";
		p.match = new String[] { "ᎢᎾ@@", "ᎢᏁ@@", "ᎢᏂ@@", "ᎢᏃ@@", "ᎢᏄ@@", "ᎢᏅ@@" };
		p.replacement = new String[] { "ᎢᏂ@ Ꭰ", "ᎢᏂ@ Ꭱ", "ᎢᏂ@ ", "ᎢᏂ@ Ꭳ", "ᎢᏂ@ Ꭴ", "ᎢᏂ@ Ꭵ" };
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);

		p = new PatternMatchReplacement();
		p.regex_match = "Ꭹ[ᎾᏁᏂᏃᏄᏅ]";
		p.match = new String[] { "ᎩᎾ@@", "ᎩᏁ@@", "ᎩᏂ@@", "ᎩᏃ@@", "ᎩᏄ@@", "ᎩᏅ@@" };
		p.replacement = new String[] { "ᎩᏂ@ Ꭰ", "ᎩᏂ@ Ꭱ", "ᎩᏂ@ ", "ᎩᏂ@ Ꭳ", "ᎩᏂ@ Ꭴ", "ᎩᏂ@ Ꭵ" };
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);

		p = new PatternMatchReplacement();
		p.regex_match = "ᎣᏍ[ᏓᏕᏗᏙᏚᏛ]";
		p.match = new String[] { "ᎣᏍᏓ@@", "ᎣᏍᏕ@@", "ᎣᏍᏗ@@", "ᎣᏍᏙ@@", "ᎣᏍᏚ@@", "ᎣᏍᏛ@@" };
		p.replacement = new String[] { "ᎣᏍᏗ@ Ꭰ", "ᎣᏍᏗ@ Ꭱ", "ᎣᏍᏗ@ ", "ᎣᏍᏗ@ Ꭳ", "ᎣᏍᏗ@ Ꭴ", "ᎣᏍᏗ@ Ꭵ" };
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);

		p = new PatternMatchReplacement();
		p.regex_match = "ᎣᎩ[ᎾᏁᏂᏃᏄᏅ]";
		p.match = new String[] { "ᎣᎩᎾ@@", "ᎣᎩᏁ@@", "ᎣᎩᏂ@@", "ᎣᎩᏃ@@", "ᎣᎩᏄ@@", "ᎣᎩᏅ@@" };
		p.replacement = new String[] { "ᎣᎩᏂ@ Ꭰ", "ᎣᎩᏂ@ Ꭱ", "ᎣᎩᏂ@ ", "ᎣᎩᏂ@ Ꭳ", "ᎣᎩᏂ@ Ꭴ", "ᎣᎩᏂ@ Ꭵ" };
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);

		p = new PatternMatchReplacement();
		p.regex_match = "Ꭳ[ᏣᏤᏥᏦᏧᏨ]";
		p.match = new String[] { "ᎣᏣ@@", "ᎣᏤ@@", "ᎣᏥ@@", "ᎣᏦ@@", "ᎣᏧ@@", "ᎣᏨ@@" };
		p.replacement = new String[] { "ᎣᏥ@ Ꭰ", "ᎣᏥ@ Ꭱ", "ᎣᏥ@ ", "ᎣᏥ@ Ꭳ", "ᎣᏥ@ Ꭴ", "ᎣᏥ@ Ꭵ" };
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);

		p = new PatternMatchReplacement();
		p.regex_match = "Ꭳ[ᎦᎨᎩᎪᎫᎬ]";
		p.match = new String[] { "ᎣᎦ@@", "ᎣᎨ@@", "ᎣᎩ@@", "ᎣᎪ@@", "ᎣᎫ@@", "ᎣᎬ@@" };
		p.replacement = new String[] { "ᎣᎩ@ Ꭰ", "ᎣᎩ@ Ꭱ", "ᎣᎩ@ ", "ᎣᎩ@ Ꭳ", "ᎣᎩ@ Ꭴ", "ᎣᎩ@ Ꭵ" };
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);

		/*
		 * only adding permutations, the bare matcher has too many false positives
		 */
		p = new PatternMatchReplacement();
		p.regex_match = "[ᎦᎨᎪᎫᎬ]";
		p.match = new String[] { "Ꭶ@@", "Ꭸ@@", "Ꭺ@@", "Ꭻ@@", "Ꭼ@@" };
		p.replacement = new String[] { "Ꭶ@ ", "Ꭶ@ Ꭱ", "Ꭶ@ Ꭳ", "Ꭶ@ Ꭴ", "Ꭶ@ Ꭵ" };
		addCommonPrepronounPermutations(matches, p);

		/*
		 * only adding permutations, the bare matcher has too many false positives
		 */
		p = new PatternMatchReplacement();
		p.regex_match = "Ꭰ";
		p.match = new String[] { "Ꭰ@@" };
		p.replacement = new String[] { " Ꭰ" };
		addCommonPrepronounPermutations(matches, p);

		/*
		 * only adding permutations, the bare matcher has too many false positives
		 */
		p = new PatternMatchReplacement();
		p.regex_match = "Ꭱ";
		p.match = new String[] { "Ꭱ@@" };
		p.replacement = new String[] { " Ꭱ" };
		addCommonPrepronounPermutations(matches, p);

		/*
		 * only adding permutations, the bare matcher has too many false positives
		 */
//		p = new PatternMatchReplacement();
//		p.regex_match = "Ꭲ";
//		p.match = new String[] {"Ꭲ@@"};
//		p.replacement  = new String[] {" "};
//		addCommonPrepronounPermutations(matches, p);

		/*
		 * only adding permutations, the bare matcher has too many false positives
		 */
		p = new PatternMatchReplacement();
		p.regex_match = "Ꭳ";
		p.match = new String[] { "Ꭳ@@" };
		p.replacement = new String[] { " Ꭳ" };
		addCommonPrepronounPermutations(matches, p);

		/*
		 * only adding permutations, the bare matcher has too many false positives
		 */
		p = new PatternMatchReplacement();
		p.regex_match = "Ꭵ";
		p.match = new String[] { "Ꭵ@@" };
		p.replacement = new String[] { " Ꭵ" };
		addCommonPrepronounPermutations(matches, p);

		p = new PatternMatchReplacement();
		p.regex_match = "Ꭰ[ᎾᏁᏂᏃᏄᏅ]";
		p.match = new String[] { "ᎠᎾ@@", "ᎠᏁ@@", "ᎠᏂ@@", "ᎠᏃ@@", "ᎠᏄ@@", "ᎠᏅ@@" };
		p.replacement = new String[] { "ᎠᏂ@ Ꭰ", "ᎠᏂ@ Ꭱ", "ᎠᏂ@ ", "ᎠᏂ@ Ꭳ", "ᎠᏂ@ Ꭴ", "ᎠᏂ@ Ꭵ" };
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);

		p = new PatternMatchReplacement();
		p.regex_match = "Ꭴ[ᎾᏁᏂᏃᏄᏅ]";
		p.match = new String[] { "ᎤᎾ@@", "ᎤᏁ@@", "ᎤᏂ@@", "ᎤᏃ@@", "ᎤᏄ@@", "ᎤᏅ@@" };
		p.replacement = new String[] { "ᎤᏂ@ Ꭰ", "ᎤᏂ@ Ꭱ", "ᎤᏂ@ ", "ᎤᏂ@ Ꭳ", "ᎤᏂ@ Ꭴ", "ᎤᏂ@ Ꭵ" };
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);

		p = new PatternMatchReplacement();
		p.regex_match = "ᎥᎩ";
		p.match = new String[] { "ᎥᎩ@@" };
		p.replacement = new String[] { "ᎥᎩ@ " };
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);

		p = new PatternMatchReplacement();
		p.regex_match = "Ꭵ[ᏆᏇᏉᏊᏋ]";
		p.match = new String[] { "ᎥᏆ@@", "ᎥᏇ@@", "ᎥᏉ@@", "ᎥᏊ@@", "ᎥᏋ@@" };
		p.replacement = new String[] { "ᎥᎩ@ Ꭰ", "ᎥᎩ@ Ꭱ", "ᎥᎩ@ Ꭳ", "ᎥᎩ@ Ꭴ", "ᎥᎩ@ Ꭵ" };
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);

		p = new PatternMatchReplacement();
		p.regex_match = "ᎡᎩ[ᎾᏁᏂᏃᏄᏅ]";
		p.match = new String[] { "ᎡᎩᎾ@@", "ᎡᎩᏁ@@", "ᎡᎩᏂ@@", "ᎡᎩᏃ@@", "ᎡᎩᏄ@@", "ᎡᎩᏅ@@" };
		p.replacement = new String[] { "ᎡᎩᏂ@ Ꭰ", "ᎡᎩᏂ@ Ꭱ", "ᎡᎩᏂ@ ", "ᎡᎩᏂ@ Ꭳ", "ᎡᎩᏂ@ Ꭴ", "ᎡᎩᏂ@ Ꭵ" };
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);

		p = new PatternMatchReplacement();
		p.regex_match = "ᎠᏥ";
		p.match = new String[] { "ᎠᏥ@@" };
		p.replacement = new String[] { "ᎠᏥ@ " };
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);

		p = new PatternMatchReplacement();
		p.regex_match = "Ꭰ[ᎦᎨᎪᎫᎬ]";
		p.match = new String[] { "ᎠᎦ@@", "ᎠᎨ@@", "ᎠᎪ@@", "ᎠᎫ@@", "ᎠᎬ@@" };
		p.replacement = new String[] { "ᎠᏥ@ Ꭰ", "ᎠᏥ@ Ꭱ", "ᎠᏥ@ Ꭳ", "ᎠᏥ@ Ꭴ", "ᎠᏥ@ Ꭵ" };
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);

		Collections.sort(matches);

		return matches;
	}

	private static void addCommonPrepronounPermutations(List<PatternMatchReplacement> matches,
			PatternMatchReplacement p) {
		matches.add(prefixWithDe(p));
		matches.add(prefixWithNi(p));
		matches.add(prefixWithNi(prefixWithDe(p)));
		matches.add(prefixWithYi(p));
		matches.add(prefixWithYi(prefixWithDe(p)));
		matches.add(prefixWithYi(prefixWithNi(p)));
		matches.add(prefixWithYi(prefixWithNi(prefixWithDe(p))));
	}

	private static PatternMatchReplacement prefixWithDe(PatternMatchReplacement p) {
		PatternMatchReplacement q = new PatternMatchReplacement();

		q.match = Arrays.copyOf(p.match, p.match.length);
		q.replacement = Arrays.copyOf(p.replacement, p.replacement.length);

		q.regex_match = prefixWithDe(p.regex_match);

		for (int i = 0; i < q.match.length; i++) {
			q.match[i] = prefixWithDe(q.match[i]);
		}

		for (int i = 0; i < q.match.length; i++) {
//			q.replacement[i]="Ꮥ@ "+q.replacement[i];
			q.replacement[i] = "Ꮥ@" + q.replacement[i];
		}

		return q;
	}

	private static PatternMatchReplacement prefixWithNi(PatternMatchReplacement p) {
		PatternMatchReplacement q = new PatternMatchReplacement();

		q.match = Arrays.copyOf(p.match, p.match.length);
		q.replacement = Arrays.copyOf(p.replacement, p.replacement.length);

		q.regex_match = prefixWithNi(p.regex_match);

		for (int i = 0; i < q.match.length; i++) {
			q.match[i] = prefixWithNi(q.match[i]);
		}

		for (int i = 0; i < q.match.length; i++) {
//			q.replacement[i]="Ꮒ@ "+q.replacement[i];
			q.replacement[i] = "Ꮒ@" + q.replacement[i];
		}

		return q;
	}

	private static PatternMatchReplacement prefixWithYi(PatternMatchReplacement p) {
		PatternMatchReplacement q = new PatternMatchReplacement();

		q.match = Arrays.copyOf(p.match, p.match.length);
		q.replacement = Arrays.copyOf(p.replacement, p.replacement.length);

		q.regex_match = prefixWithYi(p.regex_match);

		for (int i = 0; i < q.match.length; i++) {
			q.match[i] = prefixWithYi(q.match[i]);
		}

		for (int i = 0; i < q.match.length; i++) {
//			q.replacement[i]="Ᏹ@ "+q.replacement[i];
			q.replacement[i] = "Ᏹ@" + q.replacement[i];
		}

		return q;
	}

	private static String prefixWithDe(String tmp) {
		tmp = "Ꮥ" + tmp;
		tmp = tmp.replaceFirst("^ᏕᎠ", "Ꮣ");
		tmp = tmp.replaceFirst("^ᏕᎡ", "Ꮥ");
		tmp = tmp.replaceFirst("^ᏕᎢ", "Ꮥ");
		tmp = tmp.replaceFirst("^ᏕᎣ", "Ꮩ");
		tmp = tmp.replaceFirst("^ᏕᎤ", "Ꮪ");
		tmp = tmp.replaceFirst("^ᏕᎥ", "Ꮫ");
		return tmp;
	}

	private static String prefixWithNi(String tmp) {
		tmp = "Ꮒ" + tmp;
		tmp = tmp.replaceFirst("^ᏂᎠ", "Ꮎ");
		tmp = tmp.replaceFirst("^ᏂᎡ", "Ꮑ");
		tmp = tmp.replaceFirst("^ᏂᎢ", "Ꮒ");
		tmp = tmp.replaceFirst("^ᏂᎣ", "Ꮓ");
		tmp = tmp.replaceFirst("^ᏂᎤ", "Ꮔ");
		tmp = tmp.replaceFirst("^ᏂᎥ", "Ꮕ");
		return tmp;
	}

	private static String prefixWithYi(String tmp) {
		tmp = "Ᏹ" + tmp;
		tmp = tmp.replaceFirst("^ᏱᎠ", "Ꮿ");
		tmp = tmp.replaceFirst("^ᏱᎡ", "Ᏸ");
		tmp = tmp.replaceFirst("^ᏱᎢ", "Ᏹ");
		tmp = tmp.replaceFirst("^ᏱᎣ", "Ᏺ");
		tmp = tmp.replaceFirst("^ᏱᎤ", "Ᏻ");
		tmp = tmp.replaceFirst("^ᏱᎥ", "Ᏼ");
		return tmp;
	}

	@Override
	public void run() {
		try {
			_run();
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void _run() throws IOException {
		process();
	}

	private String regex_without = "\\b(Ᏹ?Ꮻ?[ᎾᏁᏂᏃᏄᏅ])([Ꭰ-Ᏼ]+)([ᎥᎬᎲᎸᏅᏋᏒᏛᏢᏨᏮᏴ])(Ꮎ)\\b";
	private String regex_without_replace = "$1@@ $2$3Ꭲ @Ꭵ$4";

	private void doWithoutExtractions(FactoredForm ff) {
		String word = ff.getLemma();
		word = word.replaceAll(regex_without, regex_without_replace);
		if (word.contains("@@")) {
			word = word.replace("Ꮎ@@ ", "Ꭰ");
			word = word.replace("Ꮑ@@ ", "Ꭱ");
			word = word.replace("Ꮒ@@ ", "");
			word = word.replace("Ꮓ@@ ", "Ꭳ");
			word = word.replace("Ꮔ@@ ", "Ꭴ");
			word = word.replace("Ꮕ@@ ", "Ꭵ");
			ff.addPrefix("Ꮒ");
			ff.setLemma(word);
		}
	}

	private String regex_alreadyHave = "\\b([ᎾᏁᏂᏃᏄᏅ]{2,})([Ꭰ-Ᏼ]+)([ᎣᎪᎰᎶᎼᏃᏉᏐᏙᏠᏦᏬᏲ])(Ꭲ?)\\b";
	private String regex_alreadyHaveReplace = "$1@@ $2@@$3 ";

	private void doAlreadyHaveExtractions(FactoredForm ff) {
		String word = ff.getLemma();
		word = word.replaceAll(regex_alreadyHave, regex_alreadyHaveReplace);
		if (word.contains("@@")) {

			word = word.replace("@@Ꭳ ", "ᎥᎢ @ᎣᎢ ");
			word = word.replace("@@Ꭺ ", "ᎬᎢ @ᎣᎢ ");
			word = word.replace("@@Ꮀ ", "ᎲᎢ @ᎣᎢ ");
			word = word.replace("@@Ꮆ ", "ᎸᎢ @ᎣᎢ ");
			word = word.replace("@@Ꮓ ", "ᏅᎢ @ᎣᎢ ");
			word = word.replace("@@Ꮙ ", "ᏋᎢ @ᎣᎢ ");
			word = word.replace("@@Ꮠ ", "ᏒᎢ @ᎣᎢ ");
			word = word.replace("@@Ꮩ ", "ᏛᎢ @ᎣᎢ ");
			word = word.replace("@@Ꮰ ", "ᏢᎢ @ᎣᎢ ");
			word = word.replace("@@Ꮶ ", "ᏨᎢ @ᎣᎢ ");
			word = word.replace("@@Ꮼ ", "ᏮᎢ @ᎣᎢ ");
			word = word.replace("@@Ᏺ ", "ᏴᎢ @ᎣᎢ ");

			if (word.contains("@ᎣᎢ")) {
				word = word.replace(" @ᎣᎢ ", "");
				ff.addSuffix("ᎣᎢ");
				ff.setLemma(word);
			}

			word = word.replace("Ꮎ@@ ", "Ꮒ@ Ꭰ");
			word = word.replace("Ꮑ@@ ", "Ꮒ@ Ꭱ");
			word = word.replace("Ꮒ@@ ", "Ꮒ@ ");
			word = word.replace("Ꮓ@@ ", "Ꮒ@ Ꭳ");
			word = word.replace("Ꮔ@@ ", "Ꮒ@ Ꭴ");
			word = word.replace("Ꮕ@@ ", "Ꮒ@ Ꭵ");

			if (word.contains("Ꮒ@ ")) {
				word = word.replace("Ꮒ@ ", "");
				ff.addPrefix("Ꮒ");
				ff.setLemma(word);
			}

		}
	}

	private String regex_willAlready = "\\b(Ᏹ?[ᎾᏁᏂᏃᏄᏅ]{2,})([Ꭰ-Ᏼ]+)([ᎡᎨᎮᎴᎺᏁᏇᏎᏕᏖᏞᏤᏪᏰ])(ᏍᏗ)\\b";
	private String regex_willAlreadyReplace = "$1@@ $2@@$3 ᎡᏍᏗ";

	private void doWillAlreadyExtractions(FactoredForm ff) {
		String line = ff.getLemma();
		line = line.replaceAll(regex_willAlready, regex_willAlreadyReplace);
		if (line.contains("@@")) {
			line = line.replace("@@Ꭱ ", "ᎥᎢ ");
			line = line.replace("@@Ꭸ ", "ᎬᎢ ");
			line = line.replace("@@Ꭾ ", "ᎲᎢ ");
			line = line.replace("@@Ꮄ ", "ᎸᎢ ");
			line = line.replace("@@Ꮑ ", "ᏅᎢ ");
			line = line.replace("@@Ꮗ ", "ᏋᎢ ");
			line = line.replace("@@Ꮞ ", "ᏒᎢ ");
			line = line.replace("@@Ꮥ ", "ᏛᎢ ");
			line = line.replace("@@Ꮦ ", "ᏛᎢ ");
			line = line.replace("@@Ꮮ ", "ᏢᎢ ");
			line = line.replace("@@Ꮴ ", "ᏨᎢ ");
			line = line.replace("@@Ꮺ ", "ᏮᎢ ");
			line = line.replace("@@Ᏸ ", "ᏴᎢ ");
			if (line.endsWith(" ᎡᏍᏗ")) {
				line=line.replace(" ᎡᏍᏗ", "");
				ff.setLemma(line);
				ff.addSuffix("ᎡᏍᏗ");
			}
			
			line = line.replace("Ꮎ@@ ", "Ꮒ@ Ꭰ");
			line = line.replace("Ꮑ@@ ", "Ꮒ@ Ꭱ");
			line = line.replace("Ꮒ@@ ", "Ꮒ@ ");
			line = line.replace("Ꮓ@@ ", "Ꮒ@ Ꭳ");
			line = line.replace("Ꮔ@@ ", "Ꮒ@ Ꭴ");
			line = line.replace("Ꮕ@@ ", "Ꮒ@ Ꭵ");
			if (line.contains("Ꮒ@ ")) {
				line = line.replace("Ꮒ@ ", "").trim();
				ff.setLemma(line);
				ff.addPrefix("Ꮒ");
			}
		}
	}

	private String regex_completely = "([Ꭰ-Ᏼ]{2,})([ᎣᎪᎰᎶᎼᏃᏉᏐᏙᏠᏦᏬᏲ])(ᎲᏍᎪᎢ?|ᎲᏍᎨᎢ?|ᎲᏍᎬᎢ?|ᎲᏍᎬᎩ|ᏅᎢ?|ᏁᎢ?|ᏅᎩ|Ꮎ|ᎲᏍᏗ|ᎲᏍᎦ)\\b";
	private String regex_completelyReplace = "$1@@$2 @Ꭳ$3 ";

	private void doCompletelyExtractions(FactoredForm ff) {
		String line = ff.getLemma();
		line = line.replaceAll(regex_completely, regex_completelyReplace);
		if (line.contains("@@")) {
			line = line.replace("@@Ꭳ ", "ᎥᎢ ");
			line = line.replace("@@Ꭺ ", "ᎬᎢ ");
			line = line.replace("@@Ꮀ ", "ᎲᎢ ");
			line = line.replace("@@Ꮆ ", "ᎸᎢ ");
			line = line.replace("@@Ꮓ ", "ᏅᎢ ");
			line = line.replace("@@Ꮙ ", "ᏋᎢ ");
			line = line.replace("@@Ꮠ ", "ᏒᎢ ");
			line = line.replace("@@Ꮩ ", "ᏛᎢ ");
			line = line.replace("@@Ꮰ ", "ᏢᎢ ");
			line = line.replace("@@Ꮶ ", "ᏨᎢ ");
			line = line.replace("@@Ꮼ ", "ᏮᎢ ");
			line = line.replace("@@Ᏺ ", "ᏴᎢ ");
			String suffix = line.replaceAll(".* @(.*)", "$1").trim();
			line=line.replaceAll("(.*) @.*", "$1").trim();
			ff.setLemma(line);
			ff.addSuffix(suffix);
		}
	}

	private String regex_approaching = "([Ꭰ-Ᏼ]{2,})([ᎢᎩᎯᎵᎻᏂᏈᏏᏗᏘᏟᏥᏫᏱ])(Ꭶ|ᎸᎢ?|ᎸᎩ|ᎴᎢ?|ᎯᎰᎢ?|ᎯᎲᎢ?|ᎯᎲᎩ|ᎯᎮᎢ?|ᎯᎮᏍᏗ|ᏍᏗ)\\b";
	private String regex_approachingReplace = "$1@@$2 @Ꭲ$3";

	private void doApproachingExtractions(FactoredForm ff) {
		String line = ff.getLemma();
		line = line.replaceAll(regex_approaching, regex_approachingReplace);
		if (line.contains("@@")) {
			line = line.replace("@@Ꭲ ", "ᎥᎢ ");
			line = line.replace("@@Ꭹ ", "ᎬᎢ ");
			line = line.replace("@@Ꭿ ", "ᎲᎢ ");
			line = line.replace("@@Ꮅ ", "ᎸᎢ ");
			line = line.replace("@@Ꮒ ", "ᏅᎢ ");
			line = line.replace("@@Ꮘ ", "ᏋᎢ ");
			line = line.replace("@@Ꮟ ", "ᏒᎢ ");
			line = line.replace("@@Ꮧ ", "ᏛᎢ ");
			line = line.replace("@@Ꮨ ", "ᏛᎢ ");
			line = line.replace("@@Ꮯ ", "ᏢᎢ ");
			line = line.replace("@@Ꮵ ", "ᏨᎢ ");
			line = line.replace("@@Ꮻ ", "ᏮᎢ ");
			line = line.replace("@@Ᏹ ", "ᏴᎢ ");

			String suffix = line.replaceAll(".* @(.*)", "$1").trim();

			// (Ꭶ|ᎸᎢ?|ᎸᎩ|ᎴᎢ?|ᎯᎰᎢ?|ᎯᎲᎢ?|ᎯᎲᎩ|ᎯᎮᎢ?|ᎯᎮᏍᏗ|ᏍᏗ)
			if (suffix.equals("ᎢᎸ")) {
				suffix += "Ꭲ";
			}
			if (suffix.equals("ᎢᎴ")) {
				suffix += "Ꭲ";
			}
			if (suffix.equals("ᎢᎯᎰ")) {
				suffix += "Ꭲ";
			}
			if (suffix.equals("ᎢᎯᎲ")) {
				suffix += "Ꭲ";
			}
			if (suffix.equals("ᎢᎯᎮ")) {
				suffix += "Ꭲ";
			}

			line = line.replaceAll("(.*) @.*", "$1");
			ff.setLemma(line);
			ff.addSuffix(suffix);

		}
	}

	private String regex_departing = "([Ꭰ-Ᏼ]{2,})([ᎥᎬᎲᎸᏅᏋᏒᏛᏢᏨᏮᏴ])(ᏒᎢ?|ᏍᏗ)\\b";
	private String regex_departingReplace = "$1$2Ꭲ @Ꭵ$3";
	
	private String regex_departing2 = "([Ꭰ-Ᏼ]{2,})([ᎡᎨᎮᎴᎺᏁᏇᏎᏕᏖᏞᏤᏪᏰ])(Ꭶ|ᎪᎢ?|ᎨᎢ?|ᎬᎢ?|Ꮎ)\\b";
	private String regex_departingReplace2 = "$1@@$2 @$3";

	private void doDepartingExtractions(FactoredForm ff) {
		String word = ff.getLemma();
		word = word.replaceAll(regex_departing, regex_departingReplace);
		word = word.replaceAll(regex_departing2, regex_departingReplace2);
		if (word.contains("@@")) {
			word = word.replace("@@Ꭱ ", "ᎥᎢ ");
			word = word.replace("@@Ꭸ ", "ᎬᎢ ");
			word = word.replace("@@Ꭾ ", "ᎲᎢ ");
			word = word.replace("@@Ꮄ ", "ᎸᎢ ");
			word = word.replace("@@Ꮑ ", "ᏅᎢ ");
			word = word.replace("@@Ꮗ ", "ᏋᎢ ");
			word = word.replace("@@Ꮞ ", "ᏒᎢ ");
			word = word.replace("@@Ꮥ ", "ᏛᎢ ");
			word = word.replace("@@Ꮦ ", "ᏛᎢ ");
			word = word.replace("@@Ꮮ ", "ᏢᎢ ");
			word = word.replace("@@Ꮴ ", "ᏨᎢ ");
			word = word.replace("@@Ꮺ ", "ᏮᎢ ");
			word = word.replace("@@Ᏸ ", "ᏴᎢ ");
			String suffix = word.replaceAll(".* @(.*)", "$1").trim();
			word=word.replaceAll("(.*) @.*", "$1").trim();
			ff.setLemma(word);
			ff.addSuffix(suffix);
		}
	}


	private String regex_ambulative = "([Ꭰ-Ᏼ]{2,})([ᎢᎩᎯᎵᎻᏂᏈᏏᏗᏘᏟᏥᏫᏱ])(ᏙᎭ|ᏙᎸᎩ|ᏙᎸᎢ?|ᏙᎴᎢ?|ᏙᎰᎢ?|ᏙᎲᎩ|ᏙᎲᎢ?|ᏙᎮᏍᏗ|ᏙᎮᎢ?|ᏓᏍᏗ|Ꮣ)\\b";
	private String regex_ambulativeReplace = "$1@@$2 @Ꭲ$3 ";

	private void doAmbulativeExtractions(FactoredForm ff) {
		String line = ff.getLemma();
		line = line.replaceAll(regex_ambulative, regex_ambulativeReplace);
		if (line.contains("@@")) {
			line = line.replace("@@Ꭲ ", "ᎥᎢ ");
			line = line.replace("@@Ꭹ ", "ᎬᎢ ");
			line = line.replace("@@Ꭿ ", "ᎲᎢ ");
			line = line.replace("@@Ꮅ ", "ᎸᎢ ");
			line = line.replace("@@Ꮒ ", "ᏅᎢ ");
			line = line.replace("@@Ꮘ ", "ᏋᎢ ");
			line = line.replace("@@Ꮟ ", "ᏒᎢ ");
			line = line.replace("@@Ꮧ ", "ᏛᎢ ");
			line = line.replace("@@Ꮨ ", "ᏛᎢ ");
			line = line.replace("@@Ꮯ ", "ᏢᎢ ");
			line = line.replace("@@Ꮵ ", "ᏨᎢ ");
			line = line.replace("@@Ꮻ ", "ᏮᎢ ");
			line = line.replace("@@Ᏹ ", "ᏴᎢ ");
			String suffix = line.replaceAll(".* @(.*)", "$1").trim();

			// ᏙᎭ|ᏙᎸᎩ|ᏙᎸᎢ?|ᏙᎴᎢ?|ᏙᎰᎢ?|ᏙᎲᎩ|ᏙᎲᎢ?|ᏙᎮᏍᏗ|ᏙᎮᎢ?|ᏓᏍᏗ|Ꮣ
			if (suffix.equals("ᎢᏙᎸ")) {
				suffix += "Ꭲ";
			}
			if (suffix.equals("ᎢᏙᎴ")) {
				suffix += "Ꭲ";
			}
			if (suffix.equals("ᎢᏙᎰ")) {
				suffix += "Ꭲ";
			}
			if (suffix.equals("ᎢᏙᎲ")) {
				suffix += "Ꭲ";
			}
			if (suffix.equals("ᎢᏙᎮ")) {
				suffix += "Ꭲ";
			}

			line = line.replaceAll("(.*) @.*", "$1").trim();
			ff.setLemma(line);
			ff.addSuffix(suffix);
		}
	}

	private void simplePronounSplits(FactoredForm ff) {
		String line = ff.getLemma();
		line = line.replaceAll(pronoun_splitter, "$1@@$2");
		if (line.contains("@@")) {
			line = StringUtils.replaceEach(line, pronoun_matches, pronoun_replacements);
			while (line.contains("@ ")) {
				String prefix=line.replaceFirst("(.*?)@ .*", "$1");
				line=line.replaceFirst(".*?@ (.*)", "$1");
				while(prefix.contains("@")) {
					String preprefix = prefix.replaceFirst("(.*?)@.*", "$1");
					prefix = prefix.replaceFirst(".*?@(.*)", "$1");
					ff.addPrefix(preprefix);
				}
				ff.addPrefix(prefix);
				ff.setLemma(line);
			}
		}
	}

	private void suffixSplits(FactoredForm ff) {
		String line = ff.getLemma();
		line = line.replaceAll(suffix_splitter, "$1@@$2");
		if (line.contains("@@")) {
			line = StringUtils.replaceEach(line, suffix_matches, suffix_replacements);
			if (line.contains(" @ᎥᎾ")) {
				line = line.replace(" @ᎥᎾ", "");
				ff.setLemma(line);
				ff.addSuffix("ᎥᎾ");
			}
		}
	}

	private String yi_prefix = "\\b([ᏯᏰᏱᏲᏳᏴ])([Ꭰ-Ᏼ]{2,})";
	private String yi_prefixReplace = "$1@@$2";

	private void doYiPrefixSplit(FactoredForm ff) {
		String word = ff.getLemma();
		word = word.replaceAll(yi_prefix, yi_prefixReplace);
		if (word.contains("@@")) {
			word = word.replace("Ꮿ@@", "Ꭰ");
			word = word.replace("Ᏸ@@", "Ꭱ");
			word = word.replace("Ᏹ@@", "");
			word = word.replace("Ᏺ@@", "Ꭳ");
			word = word.replace("Ᏻ@@", "Ꭴ");
			word = word.replace("Ᏼ@@", "Ꭵ");
			ff.setLemma(word);
			ff.addPrefix("Ᏹ");
		}
	}

	private String wi_prefix = "\\b([ᏩᏪᏫᏬᏭᏮ])([Ꭰ-Ᏼ]{2,})";
	private String wi_prefixReplace = "$1@@$2";

	private void doWiPrefixSplit(FactoredForm ff) {
		String word = ff.getLemma();
		word = word.replaceAll(wi_prefix, wi_prefixReplace);
		if (word.contains("@@")) {
			word = word.replace("Ꮹ@@", "Ꭰ");
			word = word.replace("Ꮺ@@", "Ꭱ");
			word = word.replace("Ꮻ@@", "");
			word = word.replace("Ꮼ@@", "Ꭳ");
			word = word.replace("Ꮽ@@", "Ꭴ");
			word = word.replace("Ꮾ@@", "Ꭵ");
			ff.setLemma(word);
			ff.addPrefix("Ꮻ");
		}
	}

	private void simpleAffixSplits(FactoredForm ff) {
		String word = ff.getLemma();
		String regex;

		regex = "\\bᎯᎠ([Ꭰ-Ᏼ]+)";
		if (word.matches(regex)) {
			word = word.replaceAll(regex, "$1");
			ff.addPrefix("ᎯᎠ");
			ff.setLemma(word);
		}

		doYiPrefixSplit(ff);
		doWiPrefixSplit(ff);

		regex = "([Ꭰ-Ᏼ]+)ᏍᎩᏂ\\b";
		if (word.matches(regex)) {
			word = word.replaceAll(regex, "$1");
			ff.addSuffix("ᏍᎩᏂ");
			ff.setLemma(word);
		}
		regex = "([Ꭰ-Ᏼ]+)ᏉᏃ\\b";
		if (word.matches(regex)) {
			word = word.replaceAll(regex, "$1");
			ff.addSuffix("Ꮙ");
			ff.addSuffix("Ꮓ");
			ff.setLemma(word);
		}
		regex = "([Ꭰ-Ᏼ]+)ᏰᏃ\\b";
		if (word.matches(regex)) {
			word = word.replaceAll(regex, "$1");
			ff.addSuffix("ᏰᏃ");
			ff.setLemma(word);
		}
		regex = "([Ꭰ-Ᏼ]+)ᏗᏜ\\b";
		if (word.matches(regex)) {
			word = word.replaceAll(regex, "$1");
			ff.addSuffix("ᏗᏜ");
			ff.setLemma(word);
		}
		regex = "([Ꭰ-Ᏼ]+)ᏗᏢ\\b";
		if (word.matches(regex)) {
			word = word.replaceAll(regex, "$1");
			ff.addSuffix("ᏗᏢ");
			ff.setLemma(word);
		}
		regex = "([Ꭰ-Ᏼ]+)Ꮓ\\b";
		if (word.matches(regex)) {
			word = word.replaceAll(regex, "$1");
			ff.addSuffix("Ꮓ");
			ff.setLemma(word);
		}
		regex = "([Ꭰ-Ᏼ]+)Ꮙ\\b";
		if (word.matches(regex)) {
			word = word.replaceAll(regex, "$1");
			ff.addSuffix("Ꮙ");
			ff.setLemma(word);
		}

		regex = "([Ꭰ-Ᏼ]+)Ᏹ\\b";
		if (word.matches(regex)) {
			word = word.replaceAll(regex, "$1");
			ff.addSuffix("Ᏹ");
			ff.setLemma(word);
		}

		regex = "([Ꭰ-Ᏼ]+)Ᏻ\\b";
		if (word.matches(regex)) {
			word = word.replaceAll(regex, "$1");
			ff.addSuffix("Ᏻ");
			ff.setLemma(word);
		}
	}
}
