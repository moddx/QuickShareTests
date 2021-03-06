package org.tuxship.quickshare.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.tuxship.quickshare.dao.DAOService;
import org.tuxship.quickshare.dao.DAOService.ShareNotFoundException;
import org.tuxship.quickshare.dao.DAOService.TokenNotFoundException;
import org.tuxship.quickshare.dao.sql.SQLiteDAO;

import android.content.Intent;
import android.test.ServiceTestCase;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;

public class SQLiteDAOTestCase extends ServiceTestCase<SQLiteDAO> {

	public SQLiteDAOTestCase() {
		super(SQLiteDAO.class);
	}

	/*
	 * Is called before each test method is executed.
	 * 
	 * (non-Javadoc)
	 * @see android.test.ServiceTestCase#setUp()
	 */
	@Override
	public void setUp() throws Exception {
		super.setUp();
	}

	/*
	 * Is called after each test method is executed.
	 * 
	 * (non-Javadoc)
	 * @see android.test.ServiceTestCase#tearDown()
	 */
	@Override
	public void tearDown() throws Exception {
		super.tearDown();
	}
	
	private SQLiteDAO getDAO() {
		Intent startIntent = new Intent(getContext(), SQLiteDAO.class);
		startService(startIntent);
		bindService(startIntent);

		SQLiteDAO dao = getService();
		dao.backupAndClear();

		return dao;
	}
	
	private void releaseDAO(SQLiteDAO dao) {
		dao.restore();
		shutdownService();
	}

	@MediumTest
	public void testBackupAndRestore() {
		Intent startIntent = new Intent(getContext(), SQLiteDAO.class);
		startService(startIntent);
		bindService(startIntent);

		SQLiteDAO dao = getService();

		addSomeShares(dao);

		List<String> sharesBefore = dao.getShares();

		{
			assertTrue(dao.backupAndClear());
			List<String> shares = dao.getShares();
			assertTrue("backupAndClear() did not clear the share db. contains: " + shares.toString(), shares.isEmpty());
		}

		{
			assertTrue(dao.restore());
			List<String> sharesAfter = dao.getShares();
			assertEquals("restore() did not restore the exact amount of shares.", sharesBefore.size(), sharesAfter.size());
			assertEquals("restore() modified the shares", sharesBefore, sharesAfter); 
		}

		removeSomeShares(dao);

		shutdownService();
	}

	@MediumTest
	public void testAddShare() {
		SQLiteDAO dao = getDAO();

		{
			int shareCountBefore = dao.getShareCount();

			String shareName = "Unique Share Name 123#";
			List<String> files = new ArrayList<String>();
			files.add("/storage/sdcard0/123Test/Greenhouse.zip");
			files.add("/storage/sdcard0/123Test/Infobrief_Studierende.pdf");

			String token = dao.addShare(shareName, files);

			assertTrue(token.length() == DAOService.TOKEN_LENGTH);

			{
				List<String> shares = dao.getShares();

				assertNotNull(shares);
				assertFalse("DAO contains no shares, altough 'some' were added",
						shares.isEmpty());
				assertEquals("Wrong count of shares" + shares.size(), 
						shareCountBefore + 1, shares.size());
			}

			{
				try {
					List<String> storedFiles = dao.getFiles(token, DAOService.TYPE_TOKEN);
					Collections.sort(files);
					Collections.sort(storedFiles);
					assertEquals("Paths stored are not the same as the input paths.",
							files, storedFiles);

				} catch (TokenNotFoundException | ShareNotFoundException e) {
					e.printStackTrace();
				}
			}
		}

		dao.restore();

		shutdownService();
	}

	@SmallTest
	public void testAddShareWithEmptyParameters() {
		SQLiteDAO dao = getDAO();

		{
			List<String> files = new ArrayList<String>();
			String token = dao.addShare("Share without files", files);
			assertEquals("addShare() should return an empty String when passed an empty file list.", "", token);
		}
		
		{
			List<String> files = getRandomFiles(5);
			String token = dao.addShare("", files);
			
			assertEquals("addShare() should return an empty String when passed an empty share name.", "", token);
		}
		
		releaseDAO(dao);
	}

	@SmallTest
	public void testAddShareWithSameName() {
		SQLiteDAO dao = getDAO();

		String shareName = "absolutelyUniqueShare#1a65b2c3";

		ArrayList<String> firstShareFiles = new ArrayList<String>();
		firstShareFiles.add("/mnt/sdcard/important_document.pdf");
		firstShareFiles.add("/mnt/sdcard/sub folder/photo.png");
		dao.addShare(shareName, firstShareFiles);

		ArrayList<String> secondShareFiles = new ArrayList<String>();
		secondShareFiles.add("/mnt/sdcard/OTHER.pdf");
		secondShareFiles.add("/mnt/sdcard/sub folder/OTHER photo.png");
		dao.addShare(shareName, secondShareFiles);

		try {
			List<String> storedFiles = dao.getFiles(shareName, DAOService.TYPE_SHARENAME);

			List<String> mergedInputLists = new ArrayList<String>(firstShareFiles);
			mergedInputLists.addAll(secondShareFiles);
			
			Collections.sort(mergedInputLists);
			Collections.sort(storedFiles);
			Collections.sort(firstShareFiles);
			Collections.sort(secondShareFiles);

			assertFalse("Second share with the same name overwrites / hides files of the first share.", secondShareFiles.equals(storedFiles));
			assertFalse("Files of both shares are stored under the same name", mergedInputLists.equals(storedFiles));
			assertEquals("Stored files are not the same as the files of the first share", firstShareFiles, storedFiles);
		} catch (TokenNotFoundException | ShareNotFoundException e) {
			e.printStackTrace();
		}

		releaseDAO(dao);
	}
	
	@SmallTest
	public void testRemoveShare() {
		SQLiteDAO dao = getDAO();
		
		String shareName = "some share name 123";
		ArrayList<String> files = new ArrayList<String>();
		files.add("/storage/sdcard0/123Test/Greenhouse.zip");
		files.add("/storage/sdcard0/123Test/Infos_Studierende.pdf");
		dao.addShare(shareName, files);
		
		assertTrue(dao.getShareCount() == 1);
		
		assertFalse(dao.removeShare(shareName + "different"));
		assertTrue(dao.removeShare(shareName));
		assertFalse(dao.removeShare(shareName));
		
		assertTrue(dao.getShareCount() == 0);
		
		releaseDAO(dao);
	}

	private void addSomeShares(SQLiteDAO dao) {
		{
			ArrayList<String> files = new ArrayList<String>();
			files.add("/storage/sdcard0/123Test/Greenhouse.zip");
			files.add("/storage/sdcard0/123Test/Infos_Studierende.pdf");
			dao.addShare("qwertz", files);
		}

		{
			ArrayList<String> files = new ArrayList<String>();
			files.add("/storage/sdcard0/Kompositionen/record0.flac");
			files.add("/storage/sdcard0/Kompositionen/record1.flac");
			files.add("/storage/sdcard0/Kompositionen/record2.flac");
			files.add("/storage/sdcard0/Kompositionen/record3.flac");
			files.add("/storage/sdcard0/Kompositionen/record4.flac");
			files.add("/storage/sdcard0/Kompositionen/record5.flac");
			dao.addShare("A Mol", files);
		}

		{
			ArrayList<String> files = new ArrayList<String>();
			files.add("/storage/sdcard0/Meine Fotos/Mein Urlaub in Athen/foto mit leerzeichen.png");
			files.add("/storage/sdcard0/Meine Fotos/Mein Urlaub in Athen/foto mit~! leerzeichen.png");
			files.add("/storage/sdcard0/Kamera/dmca13123.jpg");
			dao.addShare("#Really l0ng share name?,; I wonder how+ that* looks~ in different 'places' of the UI :)", files);
		}
	}

	private void removeSomeShares(SQLiteDAO dao) {
		dao.removeShare("qwertz");
		dao.removeShare("A Mol");
		dao.removeShare("#Really l0ng share name?,; I wonder how+ that* looks~ in different 'places' of the UI :)");
	}
	
	private static String[] animals = {
			"Kangaroo",
			"Tiger",
			"Cameleon",
			"Pidgeon",
			"Weasel",
			"Rabbit",
			"Pig",
			"Snake"
	};
	
	private static String[] adjectives = {
			"fast",
			"infurious",
			"infamous",
			"legit",
			"sleepy",
			"mauling",
			"murderous"
	};
	
	private List<String> getRandomFiles(int count) {
		List<String> files = new ArrayList<String>(count);
		for(int i = 0; i < count; i++) {
			String f = 	adjectives[ (int)(Math.random() * (adjectives.length - 1)) ] +  
						animals[ (int)(Math.random() * (animals.length - 1))];
			
			if(files.contains(f)) {
				i--;
				continue;
			}
				
			files.add(f);
		}
		
		return files;
	}

	
}
