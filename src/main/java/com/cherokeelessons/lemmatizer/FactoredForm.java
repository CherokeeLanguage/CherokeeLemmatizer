package com.cherokeelessons.lemmatizer;

public class FactoredForm {
	private String surfaceForm;
	private String pos;
	private String lemma;
	private String stem;
	public String getSurfaceForm() {
		return surfaceForm;
	}
	public void setSurfaceForm(String surfaceForm) {
		this.surfaceForm = surfaceForm;
	}
	public String getPos() {
		return pos;
	}
	public void setPos(String pos) {
		this.pos = pos;
	}
	public String getLemma() {
		return lemma;
	}
	public void setLemma(String lemma) {
		this.lemma = lemma;
	}
	public String getStem() {
		return stem;
	}
	public void setStem(String stem) {
		this.stem = stem;
	}
}
