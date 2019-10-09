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
		assert !prefix.contains("@"):toString();
	}

	public void addSuffix(String suffix) {
		suffixes = "-" + suffix.trim() + getSuffixes();
		assert !suffix.contains("@"):toString();
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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("FactoredForm [");
		if (surfaceForm != null) {
			builder.append("surfaceForm=");
			builder.append(surfaceForm);
			builder.append(", ");
		}
		if (pos != null) {
			builder.append("pos=");
			builder.append(pos);
			builder.append(", ");
		}
		if (lemma != null) {
			builder.append("lemma=");
			builder.append(lemma);
			builder.append(", ");
		}
		if (stem != null) {
			builder.append("stem=");
			builder.append(stem);
			builder.append(", ");
		}
		if (prefixes != null) {
			builder.append("prefixes=");
			builder.append(prefixes);
			builder.append(", ");
		}
		if (suffixes != null) {
			builder.append("suffixes=");
			builder.append(suffixes);
		}
		builder.append("]");
		return builder.toString();
	}

	public void setLemma(String lemma) {
		this.lemma = lemma.trim();
		assert !lemma.contains("@"):toString();
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
