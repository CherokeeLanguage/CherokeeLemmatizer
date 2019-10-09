package com.cherokeelessons.lemmatizer;

public class FactoredForm {
	private String surfaceForm;
	private String pos;
	private String lemma;
	private String stem;
	private String prefixes;
	private String suffixes;

	public void addPrefix(String prefix) {
		prefixes = getPrefixes() + prefix.trim() + "-";
	}

	public void addSuffix(String suffix) {
		suffixes = "-" + suffix.trim() + getSuffixes();
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
		this.pos = pos.trim();
	}

	public String getLemma() {
		return lemma == null ? "" : lemma;
	}

	public void setLemma(String lemma) {
		this.lemma = lemma.trim();
	}

	public String getStem() {
		return stem == null ? "" : stem;
	}

	public void setStem(String stem) {
		this.stem = stem.trim();
	}

	public String getPrefixes() {
		return prefixes == null ? "" : prefixes;
	}

	public void setPrefixes(String prefixes) {
		this.prefixes = prefixes.trim();
	}

	public String getSuffixes() {
		return suffixes == null ? "" : suffixes;
	}

	public void setSuffixes(String suffixes) {
		this.suffixes = suffixes.trim();
	}
}
