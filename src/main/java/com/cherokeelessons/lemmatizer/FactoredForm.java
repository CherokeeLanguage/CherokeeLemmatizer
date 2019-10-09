package com.cherokeelessons.lemmatizer;

public class FactoredForm {
	private String surfaceForm;
	private String pos;
	private String lemma;
	private String stem;
	private String prefixes;
	private String suffixes;

	public void addPrefix(String prefix) {
		prefixes = getPrefixes() + prefix + "-";
	}

	public void addSuffix(String suffix) {
		suffixes = "-" + suffix + getSuffixes();
	}

	public String getSurfaceForm() {
		return surfaceForm == null ? "" : surfaceForm;
	}

	public void setSurfaceForm(String surfaceForm) {
		this.surfaceForm = surfaceForm;
	}

	public String getPos() {
		return pos == null ? "" : pos;
	}

	public void setPos(String pos) {
		this.pos = pos;
	}

	public String getLemma() {
		return lemma == null ? "" : lemma;
	}

	public void setLemma(String lemma) {
		this.lemma = lemma;
	}

	public String getStem() {
		return stem == null ? "" : stem;
	}

	public void setStem(String stem) {
		this.stem = stem;
	}

	public String getPrefixes() {
		return prefixes == null ? "" : prefixes;
	}

	public void setPrefixes(String prefixes) {
		this.prefixes = prefixes;
	}

	public String getSuffixes() {
		return suffixes == null ? "" : suffixes;
	}

	public void setSuffixes(String suffixes) {
		this.suffixes = suffixes;
	}
}
