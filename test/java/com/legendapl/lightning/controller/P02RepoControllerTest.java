package com.legendapl.lightning.controller;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.legendapl.lightning.model.FolderResource;

@RunWith(com.legendapl.lightning.JavaFxJUnit4ClassRunner.class)
public class P02RepoControllerTest {

	// コントローラ呼び出し
	private P02RepoController controller = new P02RepoController();

	// System.out.println読み込み用Stream
	private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
	private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();

	@Before
	public void setUpStreams() {
		System.setOut(new PrintStream(outContent));
		System.setErr(new PrintStream(errContent));
	}

	@After
	public void cleanUpStreams() {
		System.setOut(null);
		System.setErr(null);
	}

	@Test
	public void testMoveFolderRoot() {
		FolderResource folder = (FolderResource) new FolderResource();
		folder.setUri("/");
		controller.moveFolder(folder, null);
		assertEquals("", outContent.toString());
	}

	@Test
	public void testMoveFolderRootPublic() {
		FolderResource folder = (FolderResource) new FolderResource();
		folder.setUri("/public");
		controller.moveFolder(folder, null);

		String[] hierarchy = { "uri=/", "public", "uri=/public/" };
		String checkString = "\n";
		for (String item : hierarchy) {
			checkString += item + "\n";
		}
		checkString += "\n";

		assertEquals(checkString, outContent.toString());

	}

	@Test
	public void testMoveFolderRootPulicSlashEnd() {
		FolderResource folder = (FolderResource) new FolderResource();
		folder.setUri("/public/");
		controller.moveFolder(folder, null);
		String[] hierarchy = { "uri=/", "public", "uri=/public/" };
		assertEquals(hierarchy.toString(), outContent.toString());
	}

}
