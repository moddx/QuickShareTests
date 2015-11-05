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
	public void testStartService() {
		Intent startIntent = new Intent(getContext(), JsonDAO.class);
		startService(startIntent);
		bindService(startIntent);
	}
	
	@MediumTest
	public void testAddShares() {
		MyMockApplication myMock = new MyMockApplication();
		Intent startIntent = new Intent(getContext(), JsonDAO.class);
		startService(startIntent);
		bindService(startIntent);
		
		setApplication(myMock);
		setContext(myMock);
		
		JsonDAO dao = getService();

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

	}
}
