package com.cherokeelessons.affixsplitter;

import java.awt.EventQueue;
import java.io.IOException;

import com.cherokeelessons.gui.MainWindow;
import com.cherokeelessons.gui.MainWindow.Config;

public class Main {

	public static void main(String[] args) throws IOException {
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
