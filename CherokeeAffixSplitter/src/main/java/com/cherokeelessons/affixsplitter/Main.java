package com.cherokeelessons.affixsplitter;

import java.awt.EventQueue;

import com.newsrx.gui.MainWindow;
import com.newsrx.gui.MainWindow.Config;

public class Main {

	public static void main(String[] args) {
		MainWindow.Config config = new Config() {
			@Override
			public String getApptitle() {
				return "Cherokee Affix Splitter";
			}
			
			@Override
			public Thread getApp(String... args) throws Exception {
				return new AffixSplitter(args);
			}
		};
		EventQueue.invokeLater(new MainWindow(config, args));
	}

}
