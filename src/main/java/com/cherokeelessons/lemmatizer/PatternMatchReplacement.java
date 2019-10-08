package com.cherokeelessons.lemmatizer;

public class PatternMatchReplacement implements Comparable<PatternMatchReplacement>{
	public String regex_match;
	public String[] match;
	public String[] replacement;
	@Override
	public int compareTo(PatternMatchReplacement o) {
		return -getSortId().compareTo(o.getSortId());
	}
	private String _id=null;
	public String getSortId(){
		if (_id!=null) {
			return _id;
		}
		int len=0;
		for (String r: replacement) {
			len=Math.min(len, r.length());
		}
		StringBuilder id = new StringBuilder();
		if (len<1000){
			id.append("0");
		}
		if (len<100){
			id.append("0");
		}
		if (len<10) {
			id.append("0");
		}
		id.append(len);
		id.append(regex_match);
		_id=id.toString();
		return _id;
	}
}