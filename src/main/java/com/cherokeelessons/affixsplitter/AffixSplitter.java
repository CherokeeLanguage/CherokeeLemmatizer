package com.cherokeelessons.affixsplitter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;

public class AffixSplitter extends Thread {
	
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
		c_v = new String[]{"Ꭵ","Ꭼ","Ꮂ","Ꮈ","Ꮕ","Ꮛ","Ꮢ","Ꮫ","Ꮲ","Ꮸ","Ꮾ","Ᏼ"};

		StringBuilder tmp = new StringBuilder();
		pronouns=getPronounsMatches();
		List<String> tmp_matches = new ArrayList<>();
		List<String> tmp_replacements = new ArrayList<>();
		for (PatternMatchReplacement p: pronouns) {
			if (tmp.length()>0) {
				tmp.append("|");
			}
			tmp.append(p.regex_match);
			tmp_matches.addAll(Arrays.asList(p.match));
			tmp_replacements.addAll(Arrays.asList(p.replacement));
		}
		pronoun_splitter= "\\b("+tmp.toString()+")([Ꭰ-Ᏼ]{3,})";
		pronoun_matches = tmp_matches.toArray(new String[0]);
		pronoun_replacements = tmp_replacements.toArray(new String[0]);
		
		suffixes=getSuffixMatches();
		tmp.setLength(0);
		tmp_matches.clear();
		tmp_replacements.clear();
		for (PatternMatchReplacement p: suffixes) {
			if (tmp.length()>0) {
				tmp.append("|");
			}
			tmp.append(p.regex_match);
			tmp_matches.addAll(Arrays.asList(p.match));
			tmp_replacements.addAll(Arrays.asList(p.replacement));
		}
		suffix_splitter="([Ꭰ-Ᏼ]{3,})("+tmp.toString()+")\\b";
		suffix_matches=tmp_matches.toArray(new String[0]);
		suffix_replacements=tmp_replacements.toArray(new String[0]);
	}
	
	private void process(String arg) throws IOException {
		File absoluteFile = new File(arg).getAbsoluteFile();
		File output=null;
		PrintStream writer;
		if (!stdout) {
			output = File.createTempFile(absoluteFile.getName(), ".tmp", absoluteFile.getParentFile());
			writer = new PrintStream(new FileOutputStream(output), true, "UTF-8");
		} else {
			writer = System.out;
		}
		LineIterator lines = FileUtils.lineIterator(absoluteFile, "UTF-8");
		while (lines.hasNext()) {
			String line = lines.next();
			line = simpleAffixSplits(line);
			line = doWithoutExtractions(line);
			line = doAlreadyHaveExtractions(line);
			line = doAmbulativeExtractions(line);
			line = doApproachingExtractions(line);
			line = doDepartingExtractions(line);
			line = doCompletelyExtractions(line);
			line = doWillAlreadyExtractions(line);
			line = doBenefactiveExtractions(line);
			line = suffixSplits(line);
			if (doPronouns) {
				line = simplePronounSplits(line);
			}
			writer.println(line);
		}
		lines.close();
		writer.flush();
		if (!stdout) {
			writer.close();
			FileUtils.copyFile(output, absoluteFile);
			output.delete();
		}
	}
	
	private String regex_benefactive = "([Ꭰ-Ᏼ]{2,})([ᎡᎨᎮᎴᏁᏇᏎᏕᏖᏞᏤᏪᏰ])(Ꭽ|ᎸᎢ?|ᎰᎢ?|ᎲᎢ?|ᎮᎢ?|ᎮᏍᏗ|Ꮅ|Ꮧ)\\b";
	private String regex_benefactiveReplace = "$1@@$2 @Ꭱ$3 ";
	private String doBenefactiveExtractions(String line) {
		line=line.replaceAll(regex_benefactive, regex_benefactiveReplace);
		if (line.contains("@@")){
			line=line.replace("@@Ꭱ ", "ᎥᎢ ");
			line=line.replace("@@Ꭸ ", "ᎬᎢ ");
			line=line.replace("@@Ꭾ ", "ᎲᎢ ");
			line=line.replace("@@Ꮄ ", "ᎸᎢ ");
			line=line.replace("@@Ꮑ ", "ᏅᎢ ");
			line=line.replace("@@Ꮗ ", "ᏋᎢ ");
			line=line.replace("@@Ꮞ ", "ᏒᎢ ");
			line=line.replace("@@Ꮥ ", "ᏛᎢ ");
			line=line.replace("@@Ꮦ ", "ᏛᎢ ");
			line=line.replace("@@Ꮮ ", "ᏢᎢ ");
			line=line.replace("@@Ꮴ ", "ᏨᎢ ");
			line=line.replace("@@Ꮺ ", "ᏮᎢ ");
			line=line.replace("@@Ᏸ ", "ᏴᎢ ");
		}
		return line;
	}

	private final String[] args;
	private boolean doPronouns=false;
	private boolean stdout=false;
	
	public AffixSplitter(String[] args) {
		this.args=args;
		for (String arg: args) {
			if ("--doPronouns".equals(arg)) {
				doPronouns=true;
			}
			if ("--stdout".equals(arg)) {
				stdout=true;
			}
		}
	}

	private static List<PatternMatchReplacement> getSuffixMatches() {
		PatternMatchReplacement p;
		List<PatternMatchReplacement> list=new ArrayList<>();
		p = new PatternMatchReplacement();
		p.regex_match = "[" + StringUtils.join(c_v) + "]Ꮎ";
		String match = "";
		String replace = "";
		for (String c: c_v) {
			match += "@@"+c+"Ꮎ,";
			replace += c+"Ꭲ @ᎥᎾ,";
		}
		p.match = StringUtils.split(match, ",");
		p.replacement  = StringUtils.split(replace, ",");
		list.add(p);

		return list;
	}

	private static List<PatternMatchReplacement> getPronounsMatches() {
		List<PatternMatchReplacement> matches = new ArrayList<>();
		PatternMatchReplacement p;
		
		p = new PatternMatchReplacement();		
		p.regex_match = "[ᎾᏁᏂᏃᏄᏅ]"; 
		p.match = new String[] {"Ꮎ@@", "Ꮑ@@", "Ꮒ@@", "Ꮓ@@", "Ꮔ@@", "Ꮕ@@"};
		p.replacement  = new String[] {"Ꮒ@ Ꭰ", "Ꮒ@ Ꭱ", "Ꮒ@ ",
				"Ꮒ@ Ꭳ", "Ꮒ@ Ꭴ", "Ꮒ@ Ꭵ"};
		matches.add(p);
		matches.add(prefixWithYi(p));
		
		p = new PatternMatchReplacement();		
		p.regex_match = "[ᏓᏕᏙᏚᏛ]"; 
		p.match = new String[] {"Ꮣ@@", "Ꮥ@@", "Ꮩ@@", "Ꮪ@@", "Ꮫ@@"};
		p.replacement  = new String[] {"Ꮥ@ Ꭰ", "Ꮥ@ Ꭱ", "Ꮥ@ Ꭳ", "Ꮥ@ Ꭴ", "Ꮥ@ Ꭵ"};
		matches.add(p);
		matches.add(prefixWithNi(p));
		matches.add(prefixWithYi(p));
		matches.add(prefixWithYi(prefixWithNi(p)));
		
		p = new PatternMatchReplacement();		
		p.regex_match = "Ꭼ[ᏯᏰᏱᏲᏳᏴ]?"; // => Ꭼxx@@ => Ꭼ@ x 
		p.match = new String[] {"Ꭼ@@", "ᎬᏯ@@", "ᎬᏰ@@", "ᎬᏲ@@", "ᎬᏳ@@", "ᎬᏴ@@"};
		p.replacement  = new String[] {"Ꭼ@ ", "Ꭼ@ Ꭰ", "Ꭼ@ Ꭱ", "Ꭼ@ Ꭳ", "Ꭼ@ Ꭴ", "Ꭼ@ Ꭵ"};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "ᏍᎩ";
		p.match = new String[] {"ᏍᎩ@@"};
		p.replacement  = new String[] {"ᏍᎩ@ "};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "Ꮝ[ᏆᏇᎩᏉᏊᏋ]"; 
		p.match = new String[] {"ᏍᏆ@@", "ᏍᏇ@@", "ᏍᏉ@@", "Ꮚ@@", "ᏍᏋ@@"};
		p.replacement  = new String[] {"ᏍᎩ@ Ꭰ", "ᏍᎩ@ Ꭱ", "ᏍᎩ@ Ꭳ", "ᏍᎩ@ Ꭴ", "ᏍᎩ@ Ꭵ"};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "ᏍᏛ[ᏯᏰᏲᏳᏴ]?"; 
		p.match = new String[] {"ᏍᏛ@@", "ᏍᏛᏯ@@", "ᏍᏛᏰ@@", "ᏍᏛᏲ@@", "ᏍᏛᏳ@@", "ᏍᏛᏴ@@"};
		p.replacement  = new String[] {"ᏍᏛ@ ", "ᏍᏛ@ Ꭰ", "ᏍᏛ@ Ꭱ", "ᏍᏛ@ Ꭳ", "ᏍᏛ@ Ꭴ", "ᏍᏛ@ Ꭵ"};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "ᏍᎩᏂ";
		p.match = new String[] {"ᏍᎩᏂ@@"};
		p.replacement  = new String[] {"ᏍᎩᏂ@ "};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "ᏍᎩ[ᎾᏁᏃᏄᏅ]?";
		p.match = new String[] {"ᏍᎩᎾ@@", "ᏍᎩᏁ@@", "ᏍᎩᏃ@@", "ᏍᎩᏄ@@", "ᏍᎩᏅ@@"};
		p.replacement  = new String[] {"ᏍᎩᏂ@ Ꭰ", "ᏍᎩᏂ@ Ꭱ", "ᏍᎩᏂ@ Ꭳ", "ᏍᎩᏂ@ Ꭴ", "ᏍᎩᏂ@ Ꭵ"};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "ᎢᏨ[ᏯᏰᏲᏳᏴ]?";
		p.match = new String[] {"ᎢᏨ@@", "ᎢᏨᏯ@@", "ᎢᏨᏰ@@", "ᎢᏨᏲ@@", "ᎢᏨᏳ@@", "ᎢᏨᏴ@@"};
		p.replacement  = new String[] {"ᎢᏨ@ ", "ᎢᏨ@ Ꭰ", "ᎢᏨ@ Ꭱ", "ᎢᏨ@ Ꭳ", "ᎢᏨ@ Ꭴ", "ᎢᏨ@ Ꭵ"};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "ᎢᏍᎩ[ᏯᏰᏲᏳᏴ]?";
		p.match = new String[] {"ᎢᏍᎩ@@", "ᎢᏍᎩᏯ@@", "ᎢᏍᎩᏰ@@", "ᎢᏍᎩᏲ@@", "ᎢᏍᎩᏳ@@", "ᎢᏍᎩᏴ@@"};
		p.replacement  = new String[] {"ᎢᏍᎩ@ ", "ᎢᏍᎩ@ Ꭰ", "ᎢᏍᎩ@ Ꭱ", "ᎢᏍᎩ@ Ꭳ", "ᎢᏍᎩ@ Ꭴ", "ᎢᏍᎩ@ Ꭵ"};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "Ꮵ[ᏯᏰᏲᏳᏴ]?";
		p.match = new String[] {"Ꮵ@@", "ᏥᏯ@@", "ᏥᏰ@@", "ᏥᏲ@@", "ᏥᏳ@@", "ᏥᏴ@@"};
		p.replacement  = new String[] {"Ꮵ@ ", "Ꮵ@ Ꭰ", "Ꮵ@ Ꭱ", "Ꮵ@ Ꭳ", "Ꮵ@ Ꭴ", "Ꮵ@ Ꭵ"};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "ᎠᎩ";
		p.match = new String[] {"ᎠᎩ@@"};
		p.replacement  = new String[] {"ᎠᎩ@ "};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "Ꭰ[ᏆᏇᏉᏊᏋ]";
		p.match = new String[] {"ᎠᏆ@@", "ᎠᏇ@@", "ᎠᏉ@@", "ᎠᏊ@@", "ᎠᏋ@@"};
		p.replacement  = new String[] {"ᎠᎩ@ Ꭰ", "ᎠᎩ@ Ꭱ", "ᎠᎩ@ Ꭳ", "ᎠᎩ@ Ꭴ", "ᎠᎩ@ Ꭵ"};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "Ꭿ[ᏯᏰᏲᏳᏴ]?";
		p.match = new String[] {"Ꭿ@@", "ᎯᏯ@@", "ᎯᏰ@@", "ᎯᏲ@@", "ᎯᏳ@@", "ᎯᏴ@@"};
		p.replacement  = new String[] {"Ꭿ@ ", "Ꭿ@ Ꭰ", "Ꭿ@ Ꭱ", "Ꭿ@ Ꭳ", "Ꭿ@ Ꭴ", "Ꭿ@ Ꭵ"};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		//this pattern has too many false hits
//		p = new PatternMatchReplacement();
//		p.regex_match = "[ᏣᏤᏦᏧᏨ]";
//		p.match = new String[] {"Ꮳ@@", "Ꮴ@@", "Ꮶ@@", "Ꮷ@@", "Ꮸ@@"};
//		p.replacement  = new String[] {"Ꮳ@ ", "Ꮳ@ Ꭱ", "Ꮳ@ Ꭳ", "Ꮳ@ Ꭴ", "Ꮳ@ Ꭵ"};
//		matches.add(p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "ᎡᏍ[ᏓᏕᏗᏙᏚᏛ]";
		p.match = new String[] {"ᎡᏍᏓ@@", "ᎡᏍᏕ@@", "ᎡᏍᏗ@@", "ᎡᏍᏙ@@", "ᎡᏍᏚ@@", "ᎡᏍᏛ@@"};
		p.replacement  = new String[] {"ᎡᏍᏗ@ Ꭰ", "ᎡᏍᏗ@ Ꭱ", "ᎡᏍᏗ@ ", "ᎡᏍᏗ@ Ꭳ", "ᎡᏍᏗ@ Ꭴ", "ᎡᏍᏗ@ Ꭵ"};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "Ꮝ[ᏓᏕᏗᏙᏚᏛ]";
		p.match = new String[] {"ᏍᏓ@@", "ᏍᏕ@@", "ᏍᏗ@@", "ᏍᏙ@@", "ᏍᏚ@@", "ᏍᏛ@@"};
		p.replacement  = new String[] {"ᏍᏗ@ Ꭰ", "ᏍᏗ@ Ꭱ", "ᏍᏗ@ ", "ᏍᏗ@ Ꭳ", "ᏍᏗ@ Ꭴ", "ᏍᏗ@ Ꭵ"};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "Ꭱ[ᏣᏤᏥᏦᏧᏨ]";
		p.match = new String[] {"ᎡᏣ@@", "ᎡᏤ@@", "ᎡᏥ@@", "ᎡᏦ@@", "ᎡᏧ@@", "ᎡᏨ@@"};
		p.replacement  = new String[] {"ᎡᏥ@ Ꭰ", "ᎡᏥ@ Ꭱ", "ᎡᏥ@ ", "ᎡᏥ@ Ꭳ", "ᎡᏥ@ Ꭴ", "ᎡᏥ@ Ꭵ"};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "Ꭲ[ᏣᏤᏥᏦᏧᏨ]";
		p.match = new String[] {"ᎢᏣ@@", "ᎢᏤ@@", "ᎢᏥ@@", "ᎢᏦ@@", "ᎢᏧ@@", "ᎢᏨ@@"};
		p.replacement  = new String[] {"ᎢᏥ@ Ꭰ", "ᎢᏥ@ Ꭱ", "ᎢᏥ@ ", "ᎢᏥ@ Ꭳ", "ᎢᏥ@ Ꭴ", "ᎢᏥ@ Ꭵ"};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "Ꭱ[ᏓᏕᏗᏙᏚᏛ]";
		p.match = new String[] {"ᎡᏓ@@", "ᎡᏕ@@", "ᎡᏗ@@", "ᎡᏙ@@", "ᎡᏚ@@", "ᎡᏛ@@"};
		p.replacement  = new String[] {"ᎡᏗ@ Ꭰ", "ᎡᏗ@ Ꭱ", "ᎡᏗ@ ", "ᎡᏗ@ Ꭳ", "ᎡᏗ@ Ꭴ", "ᎡᏗ@ Ꭵ"};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "Ꭲ[ᏓᏕᏗᏙᏚᏛ]";
		p.match = new String[] {"ᎢᏓ@@", "ᎢᏕ@@", "ᎢᏗ@@", "ᎢᏙ@@", "ᎢᏚ@@", "ᎢᏛ@@"};
		p.replacement  = new String[] {"ᎢᏗ@ Ꭰ", "ᎢᏗ@ Ꭱ", "ᎢᏗ@ ", "ᎢᏗ@ Ꭳ", "ᎢᏗ@ Ꭴ", "ᎢᏗ@ Ꭵ"};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "Ꭲ[ᎦᎨᎩᎪᎫᎬ]";
		p.match = new String[] {"ᎢᎦ@@", "ᎢᎨ@@", "ᎢᎩ@@", "ᎢᎪ@@", "ᎢᎫ@@", "ᎢᎬ@@"};
		p.replacement  = new String[] {"ᎢᎩ@ Ꭰ", "ᎢᎩ@ Ꭱ", "ᎢᎩ@ ", "ᎢᎩ@ Ꭳ", "ᎢᎩ@ Ꭴ", "ᎢᎩ@ Ꭵ"};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "Ꭱ[ᎾᏁᏂᏃᏄᏅ]";
		p.match = new String[] {"ᎡᎾ@@", "ᎡᏁ@@", "ᎡᏂ@@", "ᎡᏃ@@", "ᎡᏄ@@", "ᎡᏅ@@"};
		p.replacement  = new String[] {"ᎡᏂ@ Ꭰ", "ᎡᏂ@ Ꭱ", "ᎡᏂ@ ", "ᎡᏂ@ Ꭳ", "ᎡᏂ@ Ꭴ", "ᎡᏂ@ Ꭵ"};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "Ꭲ[ᎾᏁᏂᏃᏄᏅ]";
		p.match = new String[] {"ᎢᎾ@@", "ᎢᏁ@@", "ᎢᏂ@@", "ᎢᏃ@@", "ᎢᏄ@@", "ᎢᏅ@@"};
		p.replacement  = new String[] {"ᎢᏂ@ Ꭰ", "ᎢᏂ@ Ꭱ", "ᎢᏂ@ ", "ᎢᏂ@ Ꭳ", "ᎢᏂ@ Ꭴ", "ᎢᏂ@ Ꭵ"};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "Ꭹ[ᎾᏁᏂᏃᏄᏅ]";
		p.match = new String[] {"ᎩᎾ@@", "ᎩᏁ@@", "ᎩᏂ@@", "ᎩᏃ@@", "ᎩᏄ@@", "ᎩᏅ@@"};
		p.replacement  = new String[] {"ᎩᏂ@ Ꭰ", "ᎩᏂ@ Ꭱ", "ᎩᏂ@ ", "ᎩᏂ@ Ꭳ", "ᎩᏂ@ Ꭴ", "ᎩᏂ@ Ꭵ"};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "ᎣᏍ[ᏓᏕᏗᏙᏚᏛ]";
		p.match = new String[] {"ᎣᏍᏓ@@", "ᎣᏍᏕ@@", "ᎣᏍᏗ@@", "ᎣᏍᏙ@@", "ᎣᏍᏚ@@", "ᎣᏍᏛ@@"};
		p.replacement  = new String[] {"ᎣᏍᏗ@ Ꭰ", "ᎣᏍᏗ@ Ꭱ", "ᎣᏍᏗ@ ", "ᎣᏍᏗ@ Ꭳ", "ᎣᏍᏗ@ Ꭴ", "ᎣᏍᏗ@ Ꭵ"};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "ᎣᎩ[ᎾᏁᏂᏃᏄᏅ]";
		p.match = new String[] {"ᎣᎩᎾ@@", "ᎣᎩᏁ@@", "ᎣᎩᏂ@@", "ᎣᎩᏃ@@", "ᎣᎩᏄ@@", "ᎣᎩᏅ@@"};
		p.replacement  = new String[] {"ᎣᎩᏂ@ Ꭰ", "ᎣᎩᏂ@ Ꭱ", "ᎣᎩᏂ@ ", "ᎣᎩᏂ@ Ꭳ", "ᎣᎩᏂ@ Ꭴ", "ᎣᎩᏂ@ Ꭵ"};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "Ꭳ[ᏣᏤᏥᏦᏧᏨ]";
		p.match = new String[] {"ᎣᏣ@@", "ᎣᏤ@@", "ᎣᏥ@@", "ᎣᏦ@@", "ᎣᏧ@@", "ᎣᏨ@@"};
		p.replacement  = new String[] {"ᎣᏥ@ Ꭰ", "ᎣᏥ@ Ꭱ", "ᎣᏥ@ ", "ᎣᏥ@ Ꭳ", "ᎣᏥ@ Ꭴ", "ᎣᏥ@ Ꭵ"};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "Ꭳ[ᎦᎨᎩᎪᎫᎬ]";
		p.match = new String[] {"ᎣᎦ@@", "ᎣᎨ@@", "ᎣᎩ@@", "ᎣᎪ@@", "ᎣᎫ@@", "ᎣᎬ@@"};
		p.replacement  = new String[] {"ᎣᎩ@ Ꭰ", "ᎣᎩ@ Ꭱ", "ᎣᎩ@ ", "ᎣᎩ@ Ꭳ", "ᎣᎩ@ Ꭴ", "ᎣᎩ@ Ꭵ"};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);

		/*
		 * only adding permutations, the bare matcher has too many false positives
		 */
		p = new PatternMatchReplacement();
		p.regex_match = "[ᎦᎨᎪᎫᎬ]";
		p.match = new String[] {"Ꭶ@@", "Ꭸ@@", "Ꭺ@@", "Ꭻ@@", "Ꭼ@@"};
		p.replacement  = new String[] {"Ꭶ@ ", "Ꭶ@ Ꭱ", "Ꭶ@ Ꭳ", "Ꭶ@ Ꭴ", "Ꭶ@ Ꭵ"};
		addCommonPrepronounPermutations(matches, p);
		
		/*
		 * only adding permutations, the bare matcher has too many false positives
		 */
		p = new PatternMatchReplacement();
		p.regex_match = "Ꭰ";
		p.match = new String[] {"Ꭰ@@"};
		p.replacement  = new String[] {" Ꭰ"};
		addCommonPrepronounPermutations(matches, p);
		
		/*
		 * only adding permutations, the bare matcher has too many false positives
		 */
		p = new PatternMatchReplacement();
		p.regex_match = "Ꭱ";
		p.match = new String[] {"Ꭱ@@"};
		p.replacement  = new String[] {" Ꭱ"};
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
		p.match = new String[] {"Ꭳ@@"};
		p.replacement  = new String[] {" Ꭳ"};
		addCommonPrepronounPermutations(matches, p);
		
		/*
		 * only adding permutations, the bare matcher has too many false positives
		 */
		p = new PatternMatchReplacement();
		p.regex_match = "Ꭵ";
		p.match = new String[] {"Ꭵ@@"};
		p.replacement  = new String[] {" Ꭵ"};
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "Ꭰ[ᎾᏁᏂᏃᏄᏅ]";
		p.match = new String[] {"ᎠᎾ@@", "ᎠᏁ@@", "ᎠᏂ@@", "ᎠᏃ@@", "ᎠᏄ@@", "ᎠᏅ@@"};
		p.replacement  = new String[] {"ᎠᏂ@ Ꭰ", "ᎠᏂ@ Ꭱ", "ᎠᏂ@ ", "ᎠᏂ@ Ꭳ", "ᎠᏂ@ Ꭴ", "ᎠᏂ@ Ꭵ"};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "Ꭴ[ᎾᏁᏂᏃᏄᏅ]";
		p.match = new String[] {"ᎤᎾ@@", "ᎤᏁ@@", "ᎤᏂ@@", "ᎤᏃ@@", "ᎤᏄ@@", "ᎤᏅ@@"};
		p.replacement  = new String[] {"ᎤᏂ@ Ꭰ", "ᎤᏂ@ Ꭱ", "ᎤᏂ@ ", "ᎤᏂ@ Ꭳ", "ᎤᏂ@ Ꭴ", "ᎤᏂ@ Ꭵ"};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "ᎥᎩ";
		p.match = new String[] {"ᎥᎩ@@"};
		p.replacement  = new String[] {"ᎥᎩ@ "};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "Ꭵ[ᏆᏇᏉᏊᏋ]";
		p.match = new String[] {"ᎥᏆ@@", "ᎥᏇ@@", "ᎥᏉ@@", "ᎥᏊ@@", "ᎥᏋ@@"};
		p.replacement  = new String[] {"ᎥᎩ@ Ꭰ", "ᎥᎩ@ Ꭱ", "ᎥᎩ@ Ꭳ", "ᎥᎩ@ Ꭴ", "ᎥᎩ@ Ꭵ"};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "ᎡᎩ[ᎾᏁᏂᏃᏄᏅ]";
		p.match = new String[] {"ᎡᎩᎾ@@", "ᎡᎩᏁ@@", "ᎡᎩᏂ@@", "ᎡᎩᏃ@@", "ᎡᎩᏄ@@", "ᎡᎩᏅ@@"};
		p.replacement  = new String[] {"ᎡᎩᏂ@ Ꭰ", "ᎡᎩᏂ@ Ꭱ", "ᎡᎩᏂ@ ", "ᎡᎩᏂ@ Ꭳ", "ᎡᎩᏂ@ Ꭴ", "ᎡᎩᏂ@ Ꭵ"};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "ᎠᏥ";
		p.match = new String[] {"ᎠᏥ@@"};
		p.replacement  = new String[] {"ᎠᏥ@ "};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "Ꭰ[ᎦᎨᎪᎫᎬ]";
		p.match = new String[] {"ᎠᎦ@@", "ᎠᎨ@@", "ᎠᎪ@@", "ᎠᎫ@@", "ᎠᎬ@@"};
		p.replacement  = new String[] {"ᎠᏥ@ Ꭰ", "ᎠᏥ@ Ꭱ", "ᎠᏥ@ Ꭳ", "ᎠᏥ@ Ꭴ", "ᎠᏥ@ Ꭵ"};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		Collections.sort(matches);
		
		return matches;
	}

	private static void addCommonPrepronounPermutations(List<PatternMatchReplacement> matches, PatternMatchReplacement p) {
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
		
		q.match=Arrays.copyOf(p.match, p.match.length);
		q.replacement=Arrays.copyOf(p.replacement, p.replacement.length);
		
		q.regex_match=prefixWithDe(p.regex_match);
		
		for (int i=0; i<q.match.length; i++) {
			q.match[i]=prefixWithDe(q.match[i]);
		}
		
		for (int i=0; i<q.match.length; i++) {
//			q.replacement[i]="Ꮥ@ "+q.replacement[i];
			q.replacement[i]="Ꮥ@"+q.replacement[i];
		}
		
		return q;
	}
	
	private static PatternMatchReplacement prefixWithNi(PatternMatchReplacement p) {
		PatternMatchReplacement q = new PatternMatchReplacement();
		
		q.match=Arrays.copyOf(p.match, p.match.length);
		q.replacement=Arrays.copyOf(p.replacement, p.replacement.length);
		
		q.regex_match=prefixWithNi(p.regex_match);
		
		for (int i=0; i<q.match.length; i++) {
			q.match[i]=prefixWithNi(q.match[i]);
		}
		
		for (int i=0; i<q.match.length; i++) {
//			q.replacement[i]="Ꮒ@ "+q.replacement[i];
			q.replacement[i]="Ꮒ@"+q.replacement[i];
		}
		
		return q;
	}
	
	private static PatternMatchReplacement prefixWithYi(PatternMatchReplacement p) {
		PatternMatchReplacement q = new PatternMatchReplacement();
		
		q.match=Arrays.copyOf(p.match, p.match.length);
		q.replacement=Arrays.copyOf(p.replacement, p.replacement.length);
		
		q.regex_match=prefixWithYi(p.regex_match);
		
		for (int i=0; i<q.match.length; i++) {
			q.match[i]=prefixWithYi(q.match[i]);
		}
		
		for (int i=0; i<q.match.length; i++) {
//			q.replacement[i]="Ᏹ@ "+q.replacement[i];
			q.replacement[i]="Ᏹ@"+q.replacement[i];
		}
		
		return q;
	}

	private static String prefixWithDe(String tmp) {
		tmp = "Ꮥ" + tmp;
		tmp=tmp.replaceFirst("^ᏕᎠ", "Ꮣ");
		tmp=tmp.replaceFirst("^ᏕᎡ", "Ꮥ");
		tmp=tmp.replaceFirst("^ᏕᎢ", "Ꮥ");
		tmp=tmp.replaceFirst("^ᏕᎣ", "Ꮩ");
		tmp=tmp.replaceFirst("^ᏕᎤ", "Ꮪ");
		tmp=tmp.replaceFirst("^ᏕᎥ", "Ꮫ");
		return tmp;
	}
	
	private static String prefixWithNi(String tmp) {
		tmp = "Ꮒ" + tmp;
		tmp=tmp.replaceFirst("^ᏂᎠ", "Ꮎ");
		tmp=tmp.replaceFirst("^ᏂᎡ", "Ꮑ");
		tmp=tmp.replaceFirst("^ᏂᎢ", "Ꮒ");
		tmp=tmp.replaceFirst("^ᏂᎣ", "Ꮓ");
		tmp=tmp.replaceFirst("^ᏂᎤ", "Ꮔ");
		tmp=tmp.replaceFirst("^ᏂᎥ", "Ꮕ");
		return tmp;
	}
	
	private static String prefixWithYi(String tmp) {
		tmp = "Ᏹ" + tmp;
		tmp=tmp.replaceFirst("^ᏱᎠ", "Ꮿ");
		tmp=tmp.replaceFirst("^ᏱᎡ", "Ᏸ");
		tmp=tmp.replaceFirst("^ᏱᎢ", "Ᏹ");
		tmp=tmp.replaceFirst("^ᏱᎣ", "Ᏺ");
		tmp=tmp.replaceFirst("^ᏱᎤ", "Ᏻ");
		tmp=tmp.replaceFirst("^ᏱᎥ", "Ᏼ");
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
		if (args==null) {
			return;
		}
		for (String arg: args) {
			if (arg.startsWith("--")) {
				continue;
			}
			if (!new File(arg).exists() || !new File(arg).canWrite()) {
				throw new RuntimeException("Unable to process file '"+arg+"'");
			}
			process(arg);
		}
	}

	private String regex_without = "\\b(Ᏹ?Ꮻ?[ᎾᏁᏂᏃᏄᏅ])([Ꭰ-Ᏼ]+)([ᎥᎬᎲᎸᏅᏋᏒᏛᏢᏨᏮᏴ])(Ꮎ)\\b";
	private String regex_without_replace = "$1@@ $2$3Ꭲ @Ꭵ$4";
	private String doWithoutExtractions(String line) {
		line=line.replaceAll(regex_without, regex_without_replace);
		if (line.contains("@@")){
			line=line.replace("Ꮎ@@ ", "Ꮒ@ Ꭰ");
			line=line.replace("Ꮑ@@ ", "Ꮒ@ Ꭱ");
			line=line.replace("Ꮒ@@ ", "Ꮒ@ ");
			line=line.replace("Ꮓ@@ ", "Ꮒ@ Ꭳ");
			line=line.replace("Ꮔ@@ ", "Ꮒ@ Ꭴ");
			line=line.replace("Ꮕ@@ ", "Ꮒ@ Ꭵ");
		}
		return line;
	}
	
	private String regex_alreadyHave = "\\b([ᎾᏁᏂᏃᏄᏅ]{2,})([Ꭰ-Ᏼ]+)([ᎣᎪᎰᎶᎼᏃᏉᏐᏙᏠᏦᏬᏲ])(Ꭲ?)\\b";
	private String regex_alreadyHaveReplace = "$1@@ $2@@$3 ";
	private String doAlreadyHaveExtractions(String line) {
		line=line.replaceAll(regex_alreadyHave, regex_alreadyHaveReplace);
		if (line.contains("@@")){
			line=line.replace("@@Ꭳ ", "ᎥᎢ @ᎣᎢ ");
			line=line.replace("@@Ꭺ ", "ᎬᎢ @ᎣᎢ ");
			line=line.replace("@@Ꮀ ", "ᎲᎢ @ᎣᎢ ");
			line=line.replace("@@Ꮆ ", "ᎸᎢ @ᎣᎢ ");
			line=line.replace("@@Ꮓ ", "ᏅᎢ @ᎣᎢ ");
			line=line.replace("@@Ꮙ ", "ᏋᎢ @ᎣᎢ ");
			line=line.replace("@@Ꮠ ", "ᏒᎢ @ᎣᎢ ");
			line=line.replace("@@Ꮩ ", "ᏛᎢ @ᎣᎢ ");
			line=line.replace("@@Ꮰ ", "ᏢᎢ @ᎣᎢ ");
			line=line.replace("@@Ꮶ ", "ᏨᎢ @ᎣᎢ ");
			line=line.replace("@@Ꮼ ", "ᏮᎢ @ᎣᎢ ");
			line=line.replace("@@Ᏺ ", "ᏴᎢ @ᎣᎢ ");
			line=line.replace("Ꮎ@@ ", "Ꮒ@ Ꭰ");
			line=line.replace("Ꮑ@@ ", "Ꮒ@ Ꭱ");
			line=line.replace("Ꮒ@@ ", "Ꮒ@ ");
			line=line.replace("Ꮓ@@ ", "Ꮒ@ Ꭳ");
			line=line.replace("Ꮔ@@ ", "Ꮒ@ Ꭴ");
			line=line.replace("Ꮕ@@ ", "Ꮒ@ Ꭵ");
		}
		return line;
	}
	
	private String regex_willAlready = "\\b(Ᏹ?[ᎾᏁᏂᏃᏄᏅ]{2,})([Ꭰ-Ᏼ]+)([ᎡᎨᎮᎴᎺᏁᏇᏎᏕᏖᏞᏤᏪᏰ])(ᏍᏗ)\\b";
	private String regex_willAlreadyReplace = "$1@@ $2@@$3 ᎡᏍᏗ";
	private String doWillAlreadyExtractions(String line) {
		line=line.replaceAll(regex_willAlready, regex_willAlreadyReplace);
		if (line.contains("@@")){
			line=line.replace("@@Ꭱ ", "ᎥᎢ ");
			line=line.replace("@@Ꭸ ", "ᎬᎢ ");
			line=line.replace("@@Ꭾ ", "ᎲᎢ ");
			line=line.replace("@@Ꮄ ", "ᎸᎢ ");
			line=line.replace("@@Ꮑ ", "ᏅᎢ ");
			line=line.replace("@@Ꮗ ", "ᏋᎢ ");
			line=line.replace("@@Ꮞ ", "ᏒᎢ ");
			line=line.replace("@@Ꮥ ", "ᏛᎢ ");
			line=line.replace("@@Ꮦ ", "ᏛᎢ ");
			line=line.replace("@@Ꮮ ", "ᏢᎢ ");
			line=line.replace("@@Ꮴ ", "ᏨᎢ ");
			line=line.replace("@@Ꮺ ", "ᏮᎢ ");
			line=line.replace("@@Ᏸ ", "ᏴᎢ ");
			line=line.replace("Ꮎ@@ ", "Ꮒ@ Ꭰ");
			line=line.replace("Ꮑ@@ ", "Ꮒ@ Ꭱ");
			line=line.replace("Ꮒ@@ ", "Ꮒ@ ");
			line=line.replace("Ꮓ@@ ", "Ꮒ@ Ꭳ");
			line=line.replace("Ꮔ@@ ", "Ꮒ@ Ꭴ");
			line=line.replace("Ꮕ@@ ", "Ꮒ@ Ꭵ");
		}
		return line;
	}
	
	private String regex_completely = "([Ꭰ-Ᏼ]{2,})([ᎣᎪᎰᎶᎼᏃᏉᏐᏙᏠᏦᏬᏲ])(ᎲᏍᎪᎢ?|ᎲᏍᎨᎢ?|ᎲᏍᎬᎢ?|ᎲᏍᎬᎩ|ᏅᎢ?|ᏁᎢ?|ᏅᎩ|Ꮎ|ᎲᏍᏗ|ᎲᏍᎦ)\\b";
	private String regex_completelyReplace = "$1@@$2 @Ꭳ$3 ";
	private String doCompletelyExtractions(String line) {
		line=line.replaceAll(regex_completely, regex_completelyReplace);
		if (line.contains("@@")){
			line=line.replace("@@Ꭳ ", "ᎥᎢ ");
			line=line.replace("@@Ꭺ ", "ᎬᎢ ");
			line=line.replace("@@Ꮀ ", "ᎲᎢ ");
			line=line.replace("@@Ꮆ ", "ᎸᎢ ");
			line=line.replace("@@Ꮓ ", "ᏅᎢ ");
			line=line.replace("@@Ꮙ ", "ᏋᎢ ");
			line=line.replace("@@Ꮠ ", "ᏒᎢ ");
			line=line.replace("@@Ꮩ ", "ᏛᎢ ");
			line=line.replace("@@Ꮰ ", "ᏢᎢ ");
			line=line.replace("@@Ꮶ ", "ᏨᎢ ");
			line=line.replace("@@Ꮼ ", "ᏮᎢ ");
			line=line.replace("@@Ᏺ ", "ᏴᎢ ");
		}
		return line;
	}
	
	private String regex_approaching = "([Ꭰ-Ᏼ]{2,})([ᎢᎩᎯᎵᎻᏂᏈᏏᏗᏘᏟᏥᏫᏱ])(Ꭶ|ᎸᎢ?|ᎸᎩ|ᎴᎢ?|ᎯᎰᎢ?|ᎯᎲᎢ?|ᎯᎲᎩ|ᎯᎮᎢ?|ᎯᎮᏍᏗ|ᏍᏗ)\\b";
	private String regex_approachingReplace = "$1@@$2 @Ꭲ$3";
	private String doApproachingExtractions(String line) {
		line=line.replaceAll(regex_approaching, regex_approachingReplace);
		if (line.contains("@@")){
			line=line.replace("@@Ꭲ ", "ᎥᎢ ");
			line=line.replace("@@Ꭹ ", "ᎬᎢ ");
			line=line.replace("@@Ꭿ ", "ᎲᎢ ");
			line=line.replace("@@Ꮅ ", "ᎸᎢ ");
			line=line.replace("@@Ꮒ ", "ᏅᎢ ");
			line=line.replace("@@Ꮘ ", "ᏋᎢ ");
			line=line.replace("@@Ꮟ ", "ᏒᎢ ");
			line=line.replace("@@Ꮧ ", "ᏛᎢ ");
			line=line.replace("@@Ꮨ ", "ᏛᎢ ");
			line=line.replace("@@Ꮯ ", "ᏢᎢ ");
			line=line.replace("@@Ꮵ ", "ᏨᎢ ");
			line=line.replace("@@Ꮻ ", "ᏮᎢ ");
			line=line.replace("@@Ᏹ ", "ᏴᎢ ");
		}
		return line;
	}
	
	private String regex_departing = "([Ꭰ-Ᏼ]{2,})([ᎥᎬᎲᎸᏅᏋᏒᏛᏢᏨᏮᏴ])(ᏒᎢ?|ᏍᏗ)\\b";
	private String regex_departingReplace = "$1$2Ꭲ @Ꭵ$3";
	private String doDepartingExtractions(String line) {
		line = line.replaceAll(regex_departing, regex_departingReplace);
		line = doDepartingExtractions2(line);
		return line;
	}
	
	private String regex_departing2 = "([Ꭰ-Ᏼ]{2,})([ᎡᎨᎮᎴᎺᏁᏇᏎᏕᏖᏞᏤᏪᏰ])(Ꭶ|ᎪᎢ?|ᎨᎢ?|ᎬᎢ?|Ꮎ)\\b";
	private String regex_departingReplace2 = "$1@@$2 @$3";
	private String doDepartingExtractions2(String line) {
		line=line.replaceAll(regex_departing2, regex_departingReplace2);
		if (line.contains("@@")){
			line=line.replace("@@Ꭱ ", "ᎥᎢ ");
			line=line.replace("@@Ꭸ ", "ᎬᎢ ");
			line=line.replace("@@Ꭾ ", "ᎲᎢ ");
			line=line.replace("@@Ꮄ ", "ᎸᎢ ");
			line=line.replace("@@Ꮑ ", "ᏅᎢ ");
			line=line.replace("@@Ꮗ ", "ᏋᎢ ");
			line=line.replace("@@Ꮞ ", "ᏒᎢ ");
			line=line.replace("@@Ꮥ ", "ᏛᎢ ");
			line=line.replace("@@Ꮦ ", "ᏛᎢ ");
			line=line.replace("@@Ꮮ ", "ᏢᎢ ");
			line=line.replace("@@Ꮴ ", "ᏨᎢ ");
			line=line.replace("@@Ꮺ ", "ᏮᎢ ");
			line=line.replace("@@Ᏸ ", "ᏴᎢ ");
		}
		return line;
	}

	private String regex_ambulative = "([Ꭰ-Ᏼ]{2,})([ᎢᎩᎯᎵᎻᏂᏈᏏᏗᏘᏟᏥᏫᏱ])(ᏙᎭ|ᏙᎸᎩ|ᏙᎸᎢ?|ᏙᎴᎢ?|ᏙᎰᎢ?|ᏙᎲᎩ|ᏙᎲᎢ?|ᏙᎮᏍᏗ|ᏙᎮᎢ?|ᏓᏍᏗ|Ꮣ)\\b";
	private String regex_ambulativeReplace = "$1@@$2 @Ꭲ$3 ";
	private String doAmbulativeExtractions(String line) {
		line=line.replaceAll(regex_ambulative, regex_ambulativeReplace);
		if (line.contains("@@")){
			line=line.replace("@@Ꭲ ", "ᎥᎢ ");
			line=line.replace("@@Ꭹ ", "ᎬᎢ ");
			line=line.replace("@@Ꭿ ", "ᎲᎢ ");
			line=line.replace("@@Ꮅ ", "ᎸᎢ ");
			line=line.replace("@@Ꮒ ", "ᏅᎢ ");
			line=line.replace("@@Ꮘ ", "ᏋᎢ ");
			line=line.replace("@@Ꮟ ", "ᏒᎢ ");
			line=line.replace("@@Ꮧ ", "ᏛᎢ ");
			line=line.replace("@@Ꮨ ", "ᏛᎢ ");
			line=line.replace("@@Ꮯ ", "ᏢᎢ ");
			line=line.replace("@@Ꮵ ", "ᏨᎢ ");
			line=line.replace("@@Ꮻ ", "ᏮᎢ ");
			line=line.replace("@@Ᏹ ", "ᏴᎢ ");
		}
		return line;
	}
	
	private String simplePronounSplits(String line) {
		line = line.replaceAll(pronoun_splitter, "$1@@$2");
		if (line.contains("@@")){
			line=StringUtils.replaceEach(line, pronoun_matches, pronoun_replacements);
		}
		return line;
	}
	
	private String suffixSplits(String line) {
		line = line.replaceAll(suffix_splitter, "$1@@$2");
		if (line.contains("@@")){
			line=StringUtils.replaceEach(line, suffix_matches, suffix_replacements);
		}
		return line;
	}

	private String yi_prefix="\\b([ᏯᏰᏱᏲᏳᏴ])([Ꭰ-Ᏼ]{2,})";
	private String yi_prefixReplace="$1@@$2";
	private String doYiPrefixSplit(String line) {
		line=line.replaceAll(yi_prefix, yi_prefixReplace);
		if (line.contains("@@")){
			line=line.replace("Ꮿ@@", "Ᏹ@ Ꭰ");
			line=line.replace("Ᏸ@@", "Ᏹ@ Ꭱ");
			line=line.replace("Ᏹ@@", "Ᏹ@ ");
			line=line.replace("Ᏺ@@", "Ᏹ@ Ꭳ");
			line=line.replace("Ᏻ@@", "Ᏹ@ Ꭴ");
			line=line.replace("Ᏼ@@", "Ᏹ@ Ꭵ");
		}
		return line;
	}
	
	private String wi_prefix="\\b([ᏩᏪᏫᏬᏭᏮ])([Ꭰ-Ᏼ]{2,})";
	private String wi_prefixReplace="$1@@$2";
	private String doWiPrefixSplit(String line) {
		line=line.replaceAll(wi_prefix, wi_prefixReplace);
		if (line.contains("@@")){
			line=line.replace("Ꮹ@@", "Ꮻ@ Ꭰ");
			line=line.replace("Ꮺ@@", "Ꮻ@ Ꭱ");
			line=line.replace("Ꮻ@@", "Ꮻ@ ");
			line=line.replace("Ꮼ@@", "Ꮻ@ Ꭳ");
			line=line.replace("Ꮽ@@", "Ꮻ@ Ꭴ");
			line=line.replace("Ꮾ@@", "Ꮻ@ Ꭵ");
		}
		return line;
	}
	
	private String simpleAffixSplits(String line) {
		line = doYiPrefixSplit(line);
		line = doWiPrefixSplit(line);
		line = line.replaceAll("\\bᎯᎠ([Ꭰ-Ᏼ]{2,})", "ᎯᎠ @$1");
		line = line.replaceAll("([Ꭰ-Ᏼ]{2,})ᏍᎩᏂ\\b", "$1 @ᏍᎩᏂ");
		line = line.replaceAll("([Ꭰ-Ᏼ]{2,})ᏉᏃ\\b", "$1 @Ꮙ @Ꮓ");
		line = line.replaceAll("([Ꭰ-Ᏼ]{2,})ᏰᏃ\\b", "$1 @ᏰᏃ");
		line = line.replaceAll("([Ꭰ-Ᏼ]{2,})ᏗᏜ\\b", "$1 @ᏗᏜ");
		line = line.replaceAll("([Ꭰ-Ᏼ]{2,})ᏗᏢ\\b", "$1 @ᏗᏢ");
		line = line.replaceAll("([Ꭰ-Ᏼ]{2,})Ꮓ\\b", "$1 @Ꮓ");
		line = line.replaceAll("([Ꭰ-Ᏼ]{2,})Ꮙ\\b", "$1 @Ꮙ");
		line = line.replaceAll("([Ꭰ-Ᏼ]{2,})Ᏹ\\b", "$1 @Ᏹ");
		line = line.replaceAll("([Ꭰ-Ᏼ]{2,})Ᏻ\\b", "$1 @Ᏻ");
		return line;
	}
}
