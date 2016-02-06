package com.cherokeelessons.affixsplitter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
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
	static {
		bene_affixpre = new String[]{"Ꭱ", "Ꭸ", "Ꭾ", "Ꮄ", "Ꮑ", "Ꮗ", "Ꮞ", "Ꮥ", "Ꮦ", "Ꮮ", "Ꮴ", "Ꮺ", "Ᏸ"};
		bene_affixreplace = new String[]{"ᎥᎢ", "ᎬᎢ", "ᎲᎢ", "ᎸᎢ", "ᏅᎢ", "ᏋᎢ", "ᏒᎢ", "ᏛᎢ", "ᏛᎢ", "ᏢᎢ", "ᏨᎢ", "ᏮᎢ", "ᏴᎢ"};
		
		benefactiveSplitter = getBenefactiveSplitter();
		benefixupMatchlist = getBenefactiveSplitterFixupMatch().toArray(new String[0]);
		benefixupReplacelist = getBenefactiveSplitterFixupReplace().toArray(new String[0]);
	}

	private final String[] args;

	public AffixSplitter(String[] args) {
		this.args=args;
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
			line = simpleSuffixSplits(line);
			line = benefactiveSplit(line);
			writer.write(line);
			writer.write("\n");
		}
		writer.close();
		lines.close();
		FileUtils.copyFile(output, absoluteFile);
		output.delete();
	}
	
	private String simpleSuffixSplits(String line) {
		line = line.replaceAll("([Ꭰ-Ᏼ]{2,})ᏍᎩᏂ\\b", "$1 =ᏍᎩᏂ");
		line = line.replaceAll("([Ꭰ-Ᏼ]{2,})ᏉᏃ\\b", "$1 =Ꮙ =Ꮓ");
		line = line.replaceAll("([Ꭰ-Ᏼ]{2,})ᏰᏃ\\b", "$1 =ᏰᏃ");
		line = line.replaceAll("^([Ꭰ-Ᏼ]{2,})Ꮓ\\b", "$1 =Ꮓ");
		line = line.replaceAll("([Ꭰ-Ᏼ]{2,})Ꮙ\\b", "$1 =Ꮙ");
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
