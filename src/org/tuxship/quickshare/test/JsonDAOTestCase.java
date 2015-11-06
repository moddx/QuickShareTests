package org.tuxship.quickshare.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.tuxship.quickshare.dao.DAOService.TokenNotFoundException;
import org.tuxship.quickshare.dao.JsonDAO;

import android.content.Intent;
import android.test.ServiceTestCase;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;

public class JsonDAOTestCase extends ServiceTestCase<JsonDAO> {

	public JsonDAOTestCase() {
		super(JsonDAO.class);
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

	@SmallTest
	public void testBackupAndRestore() {
		Intent startIntent = new Intent(getContext(), JsonDAO.class);
		startService(startIntent);
		bindService(startIntent);
		
		JsonDAO dao = getService();
		
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
		Intent startIntent = new Intent(getContext(), JsonDAO.class);
		startService(startIntent);
		bindService(startIntent);
		
		JsonDAO dao = getService();
		dao.backupAndClear();

		{
			int shareCountBefore = dao.getShareCount();
			
			String shareName = "Unique Share Name 123#";
			List<String> files = new ArrayList<String>();
			files.add("/storage/sdcard0/123Test/Greenhouse.zip");
			files.add("/storage/sdcard0/123Test/Infobrief_Studierende.pdf");

			String token = dao.addShare(shareName, files);

			assertTrue(token.length() == JsonDAO.tokenLength);
			
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
					List<String> storedFiles = dao.getFiles(token);
					Collections.sort(files);
					Collections.sort(storedFiles);
					assertEquals("Paths stored are not the same as the input paths.",
							files, storedFiles);
					
				} catch (TokenNotFoundException e) {
					e.printStackTrace();
				}
			}
		}

		dao.restore();
		
		shutdownService();
	}
	
	private void addSomeShares(JsonDAO dao) {
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
	
	private void removeSomeShares(JsonDAO dao) {
		dao.removeShare("qwertz");
		dao.removeShare("A Mol");
		dao.removeShare("#Really l0ng share name?,; I wonder how+ that* looks~ in different 'places' of the UI :)");
	}
	
}
