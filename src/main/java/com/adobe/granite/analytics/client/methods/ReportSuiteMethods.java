/*
Copyright 2018 Adobe. All rights reserved.
This file is licensed to you under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License. You may obtain a copy
of the License at http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under
the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
OF ANY KIND, either express or implied. See the License for the specific language
governing permissions and limitations under the License.
*/

package com.adobe.granite.analytics.client.methods;

import static com.adobe.granite.analytics.client.JsonUtil.a;
import static com.adobe.granite.analytics.client.JsonUtil.o;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.adobe.granite.analytics.client.AnalyticsClient;
import com.adobe.granite.analytics.client.domain.CompanyReportSuites;
import com.adobe.granite.analytics.client.domain.ReportDescription;
import com.adobe.granite.analytics.client.domain.ReportSuiteEvars;
import com.adobe.granite.analytics.client.domain.ReportSuiteEvents;
import com.adobe.granite.analytics.client.domain.ReportSuiteProps;
import com.adobe.analytics.client.domain.Bookmark;
import com.adobe.analytics.client.domain.BookmarkFolder;
import com.google.gson.reflect.TypeToken;

public class ReportSuiteMethods {

	private final AnalyticsClient client;

	private Map<String, Bookmark> reportDescriptionBookmarksMap;

	public ReportSuiteMethods(AnalyticsClient client) {
		this.client = client;
		reportDescriptionBookmarksMap = new HashMap<String, Bookmark>();
	}

	public CompanyReportSuites getReportSuites() throws IOException {
		return client.callMethod("Company.GetReportSuites", o("types", a("standard")), CompanyReportSuites.class);
	}

	public List<ReportSuiteEvars> getEvars(String... rsid) throws IOException {
		final Type type = new TypeToken<List<ReportSuiteEvars>>() {}.getType();
		return client.callMethod("ReportSuite.GetEvars", o("rsid_list", rsid), type);
	}

	public List<ReportSuiteProps> getProps(String... rsid) throws IOException {
		final Type type = new TypeToken<List<ReportSuiteProps>>() {}.getType();
		return client.callMethod("ReportSuite.GetProps", o("rsid_list", rsid), type);
	}
	
	public List<ReportSuiteEvents> getEvents(String... rsid) throws IOException {
		final Type type = new TypeToken<List<ReportSuiteEvents>>() {}.getType();
		return client.callMethod("ReportSuite.GetEvents", o("rsid_list", rsid), type);
	}

	public List<BookmarkFolder> getBookmarksFolders() throws IOException {
		//TODO
		// - should it handle offset and limits?
		// - do we need the rsid.... params?
		
		final Type type = new TypeToken<Map<String, List<BookmarkFolder>>>() {}.getType();
		Map<String, List<BookmarkFolder>> foldersResponseMap = client.callMethod("Bookmark.GetBookmarks", null, type);
		
		List<BookmarkFolder> folders = foldersResponseMap.get("folders");
		for (BookmarkFolder bookmarkFolder : folders) {
			bookmarkFolder.setClient(client);
		}
		
		return folders;
	}
	
	public ReportDescription getReportDescription(int bookmarkId) throws IOException {
		final Type type = new TypeToken<ReportDescription>() {}.getType();
		return client.callMethod("Bookmark.GetReportDescription", o("bookmark_id", bookmarkId), type);
	}

	public ReportDescription getReportDescriptionByBookmarkName(String bookmarkName) throws IOException {
		if (reportDescriptionBookmarksMap.isEmpty()) {
			loadBookmarks();
		}
		Bookmark bookmark = reportDescriptionBookmarksMap.get(bookmarkName);
		if (bookmark != null) {
			return bookmark.getReportDescription();
		} else {
			return null;
		}
	}

	private void loadBookmarks() throws IOException {
		List<BookmarkFolder> bookmarkFolders = getBookmarksFolders();
		for (BookmarkFolder folder : bookmarkFolders) {
			for (Bookmark bookmark : folder.getBookmarks()) {
				reportDescriptionBookmarksMap.put(bookmark.getName(), bookmark);
			}
		}
	}
}
