package com.cherokeelessons.affixsplitter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;

public class AffixSplitter extends Thread {
	
	private final static String[] bene_affixpre;
	private final static String[] bene_affixreplace;
	private static final String benefactiveSplitter;
	private static final String[] benefixupMatchlist;
	private static final String[] benefixupReplacelist;
	
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
		bene_affixpre = new String[]{"Ꭱ", "Ꭸ", "Ꭾ", "Ꮄ", "Ꮑ", "Ꮗ", "Ꮞ", "Ꮥ", "Ꮦ", "Ꮮ", "Ꮴ", "Ꮺ", "Ᏸ"};
		bene_affixreplace = new String[]{"ᎥᎢ", "ᎬᎢ", "ᎲᎢ", "ᎸᎢ", "ᏅᎢ", "ᏋᎢ", "ᏒᎢ", "ᏛᎢ", "ᏛᎢ", "ᏢᎢ", "ᏨᎢ", "ᏮᎢ", "ᏴᎢ"};
		
		benefactiveSplitter = getBenefactiveSplitter();
		benefixupMatchlist = getBenefactiveSplitterFixupMatch().toArray(new String[0]);
		benefixupReplacelist = getBenefactiveSplitterFixupReplace().toArray(new String[0]);
		
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
		pronoun_splitter= "\\b("+tmp.toString()+")([Ꭰ-Ᏼ]{4,})";
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
		suffix_splitter="([Ꭰ-Ᏼ]{4,})("+tmp.toString()+")\\b";
		suffix_matches=tmp_matches.toArray(new String[0]);
		suffix_replacements=tmp_replacements.toArray(new String[0]);
	}
	
	private final String[] args;
	private boolean doWithoutExtraction=false;
	public boolean isDoWithoutExtraction() {
		return doWithoutExtraction||doAllAffixes;
	}
	private boolean doBenefactive=false;
	private boolean doAllAffixes=true;
	public boolean isDoBenefactive() {
		return doBenefactive||doAllAffixes;
	}

	public boolean isDoSimpleSuffixes() {
		return doSimpleSuffixes||doAllAffixes;
	}

	public boolean isDoAllAffixes(){
		return doAllAffixes;
	}
	private boolean doSimpleSuffixes=false;

	public AffixSplitter(String[] args) {
		this.args=args;
		for (String arg: args) {
			if ("--benefactive".equals(arg)) {
				doBenefactive=true;
				doAllAffixes=false;
			}
			if ("--simple-suffixes".equals(arg)) {
				doSimpleSuffixes=true;
				doAllAffixes=false;
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
			match += "=="+c+"Ꮎ,";
			replace += c+"Ꭲ =ᎥᎾ,";
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
		p.match = new String[] {"Ꮎ==", "Ꮑ==", "Ꮒ==", "Ꮓ==", "Ꮔ==", "Ꮕ=="};
		p.replacement  = new String[] {"Ꮒ= Ꭰ", "Ꮒ= Ꭱ", "Ꮒ= ", "Ꮒ= Ꭳ", "Ꮒ= Ꭴ", "Ꮒ= Ꭵ"};
		matches.add(p);
		matches.add(prefixWithYi(p));
		
		p = new PatternMatchReplacement();		
		p.regex_match = "[ᏓᏕᏙᏚᏛ]"; 
		p.match = new String[] {"Ꮣ==", "Ꮥ==", "Ꮩ==", "Ꮪ==", "Ꮫ=="};
		p.replacement  = new String[] {"Ꮥ= Ꭰ", "Ꮥ= Ꭱ", "Ꮥ= Ꭳ", "Ꮥ= Ꭴ", "Ꮥ= Ꭵ"};
		matches.add(p);
		matches.add(prefixWithNi(p));
		matches.add(prefixWithYi(p));
		matches.add(prefixWithYi(prefixWithNi(p)));
		
		p = new PatternMatchReplacement();		
		p.regex_match = "Ꭼ[ᏯᏰᏱᏲᏳᏴ]?"; // => Ꭼxx== => Ꭼ= x 
		p.match = new String[] {"Ꭼ==", "ᎬᏯ==", "ᎬᏰ==", "ᎬᏲ==", "ᎬᏳ==", "ᎬᏴ=="};
		p.replacement  = new String[] {"Ꭼ= ", "Ꭼ= Ꭰ", "Ꭼ= Ꭱ", "Ꭼ= Ꭳ", "Ꭼ= Ꭴ", "Ꭼ= Ꭵ"};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "ᏍᎩ";
		p.match = new String[] {"ᏍᎩ=="};
		p.replacement  = new String[] {"ᏍᎩ= "};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "Ꮝ[ᏆᏇᎩᏉᏊᏋ]"; 
		p.match = new String[] {"ᏍᏆ==", "ᏍᏇ==", "ᏍᏉ==", "Ꮚ==", "ᏍᏋ=="};
		p.replacement  = new String[] {"ᏍᎩ= Ꭰ", "ᏍᎩ= Ꭱ", "ᏍᎩ= Ꭳ", "ᏍᎩ= Ꭴ", "ᏍᎩ= Ꭵ"};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "ᏍᏛ[ᏯᏰᏲᏳᏴ]?"; 
		p.match = new String[] {"ᏍᏛ==", "ᏍᏛᏯ==", "ᏍᏛᏰ==", "ᏍᏛᏲ==", "ᏍᏛᏳ==", "ᏍᏛᏴ=="};
		p.replacement  = new String[] {"ᏍᏛ= ", "ᏍᏛ= Ꭰ", "ᏍᏛ= Ꭱ", "ᏍᏛ= Ꭳ", "ᏍᏛ= Ꭴ", "ᏍᏛ= Ꭵ"};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "ᏍᎩᏂ";
		p.match = new String[] {"ᏍᎩᏂ=="};
		p.replacement  = new String[] {"ᏍᎩᏂ= "};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "ᏍᎩ[ᎾᏁᏃᏄᏅ]?";
		p.match = new String[] {"ᏍᎩᎾ==", "ᏍᎩᏁ==", "ᏍᎩᏃ==", "ᏍᎩᏄ==", "ᏍᎩᏅ=="};
		p.replacement  = new String[] {"ᏍᎩᏂ= Ꭰ", "ᏍᎩᏂ= Ꭱ", "ᏍᎩᏂ= Ꭳ", "ᏍᎩᏂ= Ꭴ", "ᏍᎩᏂ= Ꭵ"};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "ᎢᏨ[ᏯᏰᏲᏳᏴ]?";
		p.match = new String[] {"ᎢᏨ==", "ᎢᏨᏯ==", "ᎢᏨᏰ==", "ᎢᏨᏲ==", "ᎢᏨᏳ==", "ᎢᏨᏴ=="};
		p.replacement  = new String[] {"ᎢᏨ= ", "ᎢᏨ= Ꭰ", "ᎢᏨ= Ꭱ", "ᎢᏨ= Ꭳ", "ᎢᏨ= Ꭴ", "ᎢᏨ= Ꭵ"};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "ᎢᏍᎩ[ᏯᏰᏲᏳᏴ]?";
		p.match = new String[] {"ᎢᏍᎩ==", "ᎢᏍᎩᏯ==", "ᎢᏍᎩᏰ==", "ᎢᏍᎩᏲ==", "ᎢᏍᎩᏳ==", "ᎢᏍᎩᏴ=="};
		p.replacement  = new String[] {"ᎢᏍᎩ= ", "ᎢᏍᎩ= Ꭰ", "ᎢᏍᎩ= Ꭱ", "ᎢᏍᎩ= Ꭳ", "ᎢᏍᎩ= Ꭴ", "ᎢᏍᎩ= Ꭵ"};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "Ꮵ[ᏯᏰᏲᏳᏴ]?";
		p.match = new String[] {"Ꮵ==", "ᏥᏯ==", "ᏥᏰ==", "ᏥᏲ==", "ᏥᏳ==", "ᏥᏴ=="};
		p.replacement  = new String[] {"Ꮵ= ", "Ꮵ= Ꭰ", "Ꮵ= Ꭱ", "Ꮵ= Ꭳ", "Ꮵ= Ꭴ", "Ꮵ= Ꭵ"};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "ᎠᎩ";
		p.match = new String[] {"ᎠᎩ=="};
		p.replacement  = new String[] {"ᎠᎩ= "};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "Ꭰ[ᏆᏇᏉᏊᏋ]";
		p.match = new String[] {"ᎠᏆ==", "ᎠᏇ==", "ᎠᏉ==", "ᎠᏊ==", "ᎠᏋ=="};
		p.replacement  = new String[] {"ᎠᎩ= Ꭰ", "ᎠᎩ= Ꭱ", "ᎠᎩ= Ꭳ", "ᎠᎩ= Ꭴ", "ᎠᎩ= Ꭵ"};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "Ꭿ[ᏯᏰᏲᏳᏴ]?";
		p.match = new String[] {"Ꭿ==", "ᎯᏯ==", "ᎯᏰ==", "ᎯᏲ==", "ᎯᏳ==", "ᎯᏴ=="};
		p.replacement  = new String[] {"Ꭿ= ", "Ꭿ= Ꭰ", "Ꭿ= Ꭱ", "Ꭿ= Ꭳ", "Ꭿ= Ꭴ", "Ꭿ= Ꭵ"};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		//this pattern has too many false hits
//		p = new PatternMatchReplacement();
//		p.regex_match = "[ᏣᏤᏦᏧᏨ]";
//		p.match = new String[] {"Ꮳ==", "Ꮴ==", "Ꮶ==", "Ꮷ==", "Ꮸ=="};
//		p.replacement  = new String[] {"Ꮳ= ", "Ꮳ= Ꭱ", "Ꮳ= Ꭳ", "Ꮳ= Ꭴ", "Ꮳ= Ꭵ"};
//		matches.add(p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "ᎡᏍ[ᏓᏕᏗᏙᏚᏛ]";
		p.match = new String[] {"ᎡᏍᏓ==", "ᎡᏍᏕ==", "ᎡᏍᏗ==", "ᎡᏍᏙ==", "ᎡᏍᏚ==", "ᎡᏍᏛ=="};
		p.replacement  = new String[] {"ᎡᏍᏗ= Ꭰ", "ᎡᏍᏗ= Ꭱ", "ᎡᏍᏗ= ", "ᎡᏍᏗ= Ꭳ", "ᎡᏍᏗ= Ꭴ", "ᎡᏍᏗ= Ꭵ"};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "Ꮝ[ᏓᏕᏗᏙᏚᏛ]";
		p.match = new String[] {"ᏍᏓ==", "ᏍᏕ==", "ᏍᏗ==", "ᏍᏙ==", "ᏍᏚ==", "ᏍᏛ=="};
		p.replacement  = new String[] {"ᏍᏗ= Ꭰ", "ᏍᏗ= Ꭱ", "ᏍᏗ= ", "ᏍᏗ= Ꭳ", "ᏍᏗ= Ꭴ", "ᏍᏗ= Ꭵ"};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "Ꭱ[ᏣᏤᏥᏦᏧᏨ]";
		p.match = new String[] {"ᎡᏣ==", "ᎡᏤ==", "ᎡᏥ==", "ᎡᏦ==", "ᎡᏧ==", "ᎡᏨ=="};
		p.replacement  = new String[] {"ᎡᏥ= Ꭰ", "ᎡᏥ= Ꭱ", "ᎡᏥ= ", "ᎡᏥ= Ꭳ", "ᎡᏥ= Ꭴ", "ᎡᏥ= Ꭵ"};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "Ꭲ[ᏣᏤᏥᏦᏧᏨ]";
		p.match = new String[] {"ᎢᏣ==", "ᎢᏤ==", "ᎢᏥ==", "ᎢᏦ==", "ᎢᏧ==", "ᎢᏨ=="};
		p.replacement  = new String[] {"ᎢᏥ= Ꭰ", "ᎢᏥ= Ꭱ", "ᎢᏥ= ", "ᎢᏥ= Ꭳ", "ᎢᏥ= Ꭴ", "ᎢᏥ= Ꭵ"};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "Ꭱ[ᏓᏕᏗᏙᏚᏛ]";
		p.match = new String[] {"ᎡᏓ==", "ᎡᏕ==", "ᎡᏗ==", "ᎡᏙ==", "ᎡᏚ==", "ᎡᏛ=="};
		p.replacement  = new String[] {"ᎡᏗ= Ꭰ", "ᎡᏗ= Ꭱ", "ᎡᏗ= ", "ᎡᏗ= Ꭳ", "ᎡᏗ= Ꭴ", "ᎡᏗ= Ꭵ"};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "Ꭲ[ᏓᏕᏗᏙᏚᏛ]";
		p.match = new String[] {"ᎢᏓ==", "ᎢᏕ==", "ᎢᏗ==", "ᎢᏙ==", "ᎢᏚ==", "ᎢᏛ=="};
		p.replacement  = new String[] {"ᎢᏗ= Ꭰ", "ᎢᏗ= Ꭱ", "ᎢᏗ= ", "ᎢᏗ= Ꭳ", "ᎢᏗ= Ꭴ", "ᎢᏗ= Ꭵ"};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "Ꭲ[ᎦᎨᎩᎪᎫᎬ]";
		p.match = new String[] {"ᎢᎦ==", "ᎢᎨ==", "ᎢᎩ==", "ᎢᎪ==", "ᎢᎫ==", "ᎢᎬ=="};
		p.replacement  = new String[] {"ᎢᎩ= Ꭰ", "ᎢᎩ= Ꭱ", "ᎢᎩ= ", "ᎢᎩ= Ꭳ", "ᎢᎩ= Ꭴ", "ᎢᎩ= Ꭵ"};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "Ꭱ[ᎾᏁᏂᏃᏄᏅ]";
		p.match = new String[] {"ᎡᎾ==", "ᎡᏁ==", "ᎡᏂ==", "ᎡᏃ==", "ᎡᏄ==", "ᎡᏅ=="};
		p.replacement  = new String[] {"ᎡᏂ= Ꭰ", "ᎡᏂ= Ꭱ", "ᎡᏂ= ", "ᎡᏂ= Ꭳ", "ᎡᏂ= Ꭴ", "ᎡᏂ= Ꭵ"};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "Ꭲ[ᎾᏁᏂᏃᏄᏅ]";
		p.match = new String[] {"ᎢᎾ==", "ᎢᏁ==", "ᎢᏂ==", "ᎢᏃ==", "ᎢᏄ==", "ᎢᏅ=="};
		p.replacement  = new String[] {"ᎢᏂ= Ꭰ", "ᎢᏂ= Ꭱ", "ᎢᏂ= ", "ᎢᏂ= Ꭳ", "ᎢᏂ= Ꭴ", "ᎢᏂ= Ꭵ"};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "Ꭹ[ᎾᏁᏂᏃᏄᏅ]";
		p.match = new String[] {"ᎩᎾ==", "ᎩᏁ==", "ᎩᏂ==", "ᎩᏃ==", "ᎩᏄ==", "ᎩᏅ=="};
		p.replacement  = new String[] {"ᎩᏂ= Ꭰ", "ᎩᏂ= Ꭱ", "ᎩᏂ= ", "ᎩᏂ= Ꭳ", "ᎩᏂ= Ꭴ", "ᎩᏂ= Ꭵ"};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "ᎣᏍ[ᏓᏕᏗᏙᏚᏛ]";
		p.match = new String[] {"ᎣᏍᏓ==", "ᎣᏍᏕ==", "ᎣᏍᏗ==", "ᎣᏍᏙ==", "ᎣᏍᏚ==", "ᎣᏍᏛ=="};
		p.replacement  = new String[] {"ᎣᏍᏗ= Ꭰ", "ᎣᏍᏗ= Ꭱ", "ᎣᏍᏗ= ", "ᎣᏍᏗ= Ꭳ", "ᎣᏍᏗ= Ꭴ", "ᎣᏍᏗ= Ꭵ"};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "ᎣᎩ[ᎾᏁᏂᏃᏄᏅ]";
		p.match = new String[] {"ᎣᎩᎾ==", "ᎣᎩᏁ==", "ᎣᎩᏂ==", "ᎣᎩᏃ==", "ᎣᎩᏄ==", "ᎣᎩᏅ=="};
		p.replacement  = new String[] {"ᎣᎩᏂ= Ꭰ", "ᎣᎩᏂ= Ꭱ", "ᎣᎩᏂ= ", "ᎣᎩᏂ= Ꭳ", "ᎣᎩᏂ= Ꭴ", "ᎣᎩᏂ= Ꭵ"};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "Ꭳ[ᏣᏤᏥᏦᏧᏨ]";
		p.match = new String[] {"ᎣᏣ==", "ᎣᏤ==", "ᎣᏥ==", "ᎣᏦ==", "ᎣᏧ==", "ᎣᏨ=="};
		p.replacement  = new String[] {"ᎣᏥ= Ꭰ", "ᎣᏥ= Ꭱ", "ᎣᏥ= ", "ᎣᏥ= Ꭳ", "ᎣᏥ= Ꭴ", "ᎣᏥ= Ꭵ"};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "Ꭳ[ᎦᎨᎩᎪᎫᎬ]";
		p.match = new String[] {"ᎣᎦ==", "ᎣᎨ==", "ᎣᎩ==", "ᎣᎪ==", "ᎣᎫ==", "ᎣᎬ=="};
		p.replacement  = new String[] {"ᎣᎩ= Ꭰ", "ᎣᎩ= Ꭱ", "ᎣᎩ= ", "ᎣᎩ= Ꭳ", "ᎣᎩ= Ꭴ", "ᎣᎩ= Ꭵ"};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);

		/*
		 * only adding permutations, the bare matcher has too many false positives
		 */
		p = new PatternMatchReplacement();
		p.regex_match = "[ᎦᎨᎪᎫᎬ]";
		p.match = new String[] {"Ꭶ==", "Ꭸ==", "Ꭺ==", "Ꭻ==", "Ꭼ=="};
		p.replacement  = new String[] {"Ꭶ= ", "Ꭶ= Ꭱ", "Ꭶ= Ꭳ", "Ꭶ= Ꭴ", "Ꭶ= Ꭵ"};
		addCommonPrepronounPermutations(matches, p);
		
		/*
		 * only adding permutations, the bare matcher has too many false positives
		 */
		p = new PatternMatchReplacement();
		p.regex_match = "Ꭰ";
		p.match = new String[] {"Ꭰ=="};
		p.replacement  = new String[] {" Ꭰ"};
		addCommonPrepronounPermutations(matches, p);
		
		/*
		 * only adding permutations, the bare matcher has too many false positives
		 */
		p = new PatternMatchReplacement();
		p.regex_match = "Ꭱ";
		p.match = new String[] {"Ꭱ=="};
		p.replacement  = new String[] {" Ꭱ"};
		addCommonPrepronounPermutations(matches, p);
		
		/*
		 * only adding permutations, the bare matcher has too many false positives
		 */
//		p = new PatternMatchReplacement();
//		p.regex_match = "Ꭲ";
//		p.match = new String[] {"Ꭲ=="};
//		p.replacement  = new String[] {" "};
//		addCommonPrepronounPermutations(matches, p);
		
		/*
		 * only adding permutations, the bare matcher has too many false positives
		 */
		p = new PatternMatchReplacement();
		p.regex_match = "Ꭳ";
		p.match = new String[] {"Ꭳ=="};
		p.replacement  = new String[] {" Ꭳ"};
		addCommonPrepronounPermutations(matches, p);
		
		/*
		 * only adding permutations, the bare matcher has too many false positives
		 */
		p = new PatternMatchReplacement();
		p.regex_match = "Ꭵ";
		p.match = new String[] {"Ꭵ=="};
		p.replacement  = new String[] {" Ꭵ"};
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "Ꭰ[ᎾᏁᏂᏃᏄᏅ]";
		p.match = new String[] {"ᎠᎾ==", "ᎠᏁ==", "ᎠᏂ==", "ᎠᏃ==", "ᎠᏄ==", "ᎠᏅ=="};
		p.replacement  = new String[] {"ᎠᏂ= Ꭰ", "ᎠᏂ= Ꭱ", "ᎠᏂ= ", "ᎠᏂ= Ꭳ", "ᎠᏂ= Ꭴ", "ᎠᏂ= Ꭵ"};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "Ꭴ[ᎾᏁᏂᏃᏄᏅ]";
		p.match = new String[] {"ᎤᎾ==", "ᎤᏁ==", "ᎤᏂ==", "ᎤᏃ==", "ᎤᏄ==", "ᎤᏅ=="};
		p.replacement  = new String[] {"ᎤᏂ= Ꭰ", "ᎤᏂ= Ꭱ", "ᎤᏂ= ", "ᎤᏂ= Ꭳ", "ᎤᏂ= Ꭴ", "ᎤᏂ= Ꭵ"};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "ᎥᎩ";
		p.match = new String[] {"ᎥᎩ=="};
		p.replacement  = new String[] {"ᎥᎩ= "};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "Ꭵ[ᏆᏇᏉᏊᏋ]";
		p.match = new String[] {"ᎥᏆ==", "ᎥᏇ==", "ᎥᏉ==", "ᎥᏊ==", "ᎥᏋ=="};
		p.replacement  = new String[] {"ᎥᎩ= Ꭰ", "ᎥᎩ= Ꭱ", "ᎥᎩ= Ꭳ", "ᎥᎩ= Ꭴ", "ᎥᎩ= Ꭵ"};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "ᎡᎩ[ᎾᏁᏂᏃᏄᏅ]";
		p.match = new String[] {"ᎡᎩᎾ==", "ᎡᎩᏁ==", "ᎡᎩᏂ==", "ᎡᎩᏃ==", "ᎡᎩᏄ==", "ᎡᎩᏅ=="};
		p.replacement  = new String[] {"ᎡᎩᏂ= Ꭰ", "ᎡᎩᏂ= Ꭱ", "ᎡᎩᏂ= ", "ᎡᎩᏂ= Ꭳ", "ᎡᎩᏂ= Ꭴ", "ᎡᎩᏂ= Ꭵ"};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "ᎠᏥ";
		p.match = new String[] {"ᎠᏥ=="};
		p.replacement  = new String[] {"ᎠᏥ= "};
		matches.add(p);
		addCommonPrepronounPermutations(matches, p);
		
		p = new PatternMatchReplacement();
		p.regex_match = "Ꭰ[ᎦᎨᎪᎫᎬ]";
		p.match = new String[] {"ᎠᎦ==", "ᎠᎨ==", "ᎠᎪ==", "ᎠᎫ==", "ᎠᎬ=="};
		p.replacement  = new String[] {"ᎠᏥ= Ꭰ", "ᎠᏥ= Ꭱ", "ᎠᏥ= Ꭳ", "ᎠᏥ= Ꭴ", "ᎠᏥ= Ꭵ"};
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
//			q.replacement[i]="Ꮥ= "+q.replacement[i];
			q.replacement[i]="Ꮥ="+q.replacement[i];
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
//			q.replacement[i]="Ꮒ= "+q.replacement[i];
			q.replacement[i]="Ꮒ="+q.replacement[i];
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
//			q.replacement[i]="Ᏹ= "+q.replacement[i];
			q.replacement[i]="Ᏹ="+q.replacement[i];
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
			if (!new File(arg).exists() || !new File(arg).canWrite()) {
				throw new RuntimeException("Unable to process file '"+arg+"'");
			}
			process(arg);
		}
	}

	private void process(String arg) throws IOException {
		File absoluteFile = new File(arg).getAbsoluteFile();
		File output = File.createTempFile(absoluteFile.getName(), ".tmp", absoluteFile.getParentFile());
		OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(output), "UTF-8");
		LineIterator lines = FileUtils.lineIterator(absoluteFile, "UTF-8");
		while (lines.hasNext()) {
			String line = lines.next();
			if (isDoSimpleSuffixes()) {
				line = simpleSuffixSplits(line);
			}
			if (isDoWithoutExtraction()) {
				line = doWithoutExtractions(line);
			}
			if (isDoAllAffixes()) {
				line = suffixSplits(line);
			}
			if (isDoBenefactive()) {
				line = benefactiveSplit(line);
			}
			if (isDoAllAffixes()) {
				line = simplePronounSplits(line);
			}
			writer.write(line);
			writer.write("\n");
		}
		writer.close();
		lines.close();
		FileUtils.copyFile(output, absoluteFile);
		output.delete();
	}

	private String regex_without = "([ᎾᏁᏂᏃᏄᏅ])([Ꭰ-Ᏼ]+)([ᎥᎬᎲᎸᏅᏋᏒᏛᏢᏨᏮᏴ])(Ꮎ)";
	private String regex_without_replace = "$1==$2$3Ꭲ =Ꭵ$4";
	private String doWithoutExtractions(String line) {
		line=line.replaceAll(regex_without, regex_without_replace);
		if (line.contains("==")){
			line=line.replace("Ꮎ==", "Ꮒ= Ꭰ");
			line=line.replace("Ꮑ==", "Ꮒ= Ꭱ");
			line=line.replace("Ꮒ==", "Ꮒ= ");
			line=line.replace("Ꮓ==", "Ꮒ= Ꭳ");
			line=line.replace("Ꮔ==", "Ꮒ= Ꭴ");
			line=line.replace("Ꮕ==", "Ꮒ= Ꭵ");
		}
		return line;
	}

	private String simplePronounSplits(String line) {
		line = line.replaceAll(pronoun_splitter, "$1==$2");
		if (line.contains("==")){
			line=StringUtils.replaceEach(line, pronoun_matches, pronoun_replacements);
		}
		return line;
	}
	
	private String suffixSplits(String line) {
		line = line.replaceAll(suffix_splitter, "$1==$2");
		if (line.contains("==")){
			line=StringUtils.replaceEach(line, suffix_matches, suffix_replacements);
		}
		return line;
	}

	private String simpleSuffixSplits(String line) {
		line = line.replaceAll("([Ꭰ-Ᏼ]{3,})ᏍᎩᏂ\\b", "$1 =ᏍᎩᏂ");
		line = line.replaceAll("([Ꭰ-Ᏼ]{3,})ᏉᏃ\\b", "$1 =Ꮙ =Ꮓ");
		line = line.replaceAll("([Ꭰ-Ᏼ]{3,})ᏰᏃ\\b", "$1 =ᏰᏃ");
		line = line.replaceAll("^([Ꭰ-Ᏼ]{3,})Ꮓ\\b", "$1 =Ꮓ");
		line = line.replaceAll("([Ꭰ-Ᏼ]{3,})Ꮙ\\b", "$1 =Ꮙ");
		line = line.replaceAll("([Ꭰ-Ᏼ]{3,})Ᏹ\\b", "$1 =Ᏹ");
		return line;
	}

	private static String benefactiveSplit(String line) {
		line = line.replaceAll(benefactiveSplitter, "$1 =$2");
		line = StringUtils.replaceEach(line, benefixupMatchlist, benefixupReplacelist);
		return line;
	}
	
	private static String getBenefactiveSplitter(){
		List<String> matches=new ArrayList<>();
		for (String prefix: bene_affixpre){
			matches.add(prefix+"Ꭽ");
			matches.add(prefix+"ᎸᎢ");
			matches.add(prefix+"Ꮈ");
			matches.add(prefix+"ᎴᎢ");
			matches.add(prefix+"Ꮄ");
			matches.add(prefix+"Ꮅ");
			matches.add(prefix+"ᎰᎢ");
			matches.add(prefix+"Ꮀ");
			matches.add(prefix+"ᎲᎢ");
			matches.add(prefix+"Ꮂ");
			matches.add(prefix+"ᎮᏍᏗ");
			matches.add(prefix+"Ꮅ");
			matches.add(prefix+"Ꮧ");
			
			matches.add(prefix+"ᎸᎩ");
			matches.add(prefix+"ᎮᎢ");
			matches.add(prefix+"Ꭾ");
		}
		Collections.sort(matches,(a,b)->{
			if (a==null) {
				return -1;
			}
			if (b==null) {
				return 1;
			}
			if (a.length()==b.length()) {
				return a.compareTo(b);
			}
			return b.length()-a.length();
		});
		String pattern = "([Ꭰ-Ᏼ]{3,})("+StringUtils.join(matches, "|")+")\\b";
		return pattern;
	}
	
	private static List<String> getBenefactiveSplitterFixupReplace(){
		List<String> replacements=new ArrayList<>();
		for (String prefix: bene_affixreplace){
			replacements.add(prefix+" =ᎡᎭ");
			replacements.add(prefix+" =ᎡᎸᎢ");
			replacements.add(prefix+" =ᎡᎸ");
			replacements.add(prefix+" =ᎡᎴᎢ");
			replacements.add(prefix+" =ᎡᎴ");
			replacements.add(prefix+" =ᎡᎵ");
			replacements.add(prefix+" =ᎡᎰᎢ");
			replacements.add(prefix+" =ᎡᎰ");
			replacements.add(prefix+" =ᎡᎲᎢ");
			replacements.add(prefix+" =ᎡᎲ");
			replacements.add(prefix+" =ᎡᎮᏍᏗ");
			replacements.add(prefix+" =ᎡᎵ");
			replacements.add(prefix+" =ᎡᏗ");
			replacements.add(prefix+" =ᎡᎸᎩ");
			replacements.add(prefix+" =ᎡᎮᎢ");
			replacements.add(prefix+" =ᎡᎮ");
		}
		return replacements;
	}
	
	private static List<String> getBenefactiveSplitterFixupMatch(){
		List<String> matches=new ArrayList<>();
		for (String prefix: bene_affixpre){
			matches.add(" ="+prefix+"Ꭽ");
			matches.add(" ="+prefix+"ᎸᎢ");
			matches.add(" ="+prefix+"Ꮈ");
			matches.add(" ="+prefix+"ᎴᎢ");
			matches.add(" ="+prefix+"Ꮄ");
			matches.add(" ="+prefix+"Ꮅ");
			matches.add(" ="+prefix+"ᎰᎢ");
			matches.add(" ="+prefix+"Ꮀ");
			matches.add(" ="+prefix+"ᎲᎢ");
			matches.add(" ="+prefix+"Ꮂ");
			matches.add(" ="+prefix+"ᎮᏍᏗ");
			matches.add(" ="+prefix+"Ꮅ");
			matches.add(" ="+prefix+"Ꮧ");
			matches.add(" ="+prefix+"ᎸᎩ");
			matches.add(" ="+prefix+"ᎮᎢ");
			matches.add(" ="+prefix+"Ꭾ");
		}
		return matches;
	}
}
