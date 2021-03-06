/**
 * Copyright (C) 2015 Johannes Schnatterer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package info.schnatterer.songbirddbapi4j.domain;

import info.schnatterer.songbirddbapi4j.SongbirdDbConnection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * The type of a {@link MediaItem}.
 * 
 * @author schnatterer
 * 
 */
public final class MediaListTypes {
	/** Don't instantiate utility classes! */
	private MediaListTypes() {
	}

	/** Gets all available list types. */
	public static final String QUERY_LIST_TYPES = "select * from media_list_types";

	/*
	 * Why not use enum, which would be easier to use and more efficient?
	 * Software must be adapted to DB changes, i.e. map is more flexible
	 */
	/** Mapping from string constants (database enums) to numerical IDs. */
	private static Map<String, Integer> listTyep2IdMap;
	/** Mapping from numerical IDs to string constants (database enums). */
	private static Map<Integer, String> id2ListTypeMap;

	/**
	 * Initializes the mappings from string constants (database enums) to
	 * numerical IDs and the other way round from the database.
	 * 
	 * To avoid doing this again and again, use {@link #isInitialized()}.
	 * 
	 * @param connection
	 *            database connection to use for querying
	 * @throws SQLException
	 *             in case an error occurs during the db query.
	 * 
	 */
	public static void populatelistTypeMap(SongbirdDbConnection connection)
			throws SQLException {
		listTyep2IdMap = new HashMap<String, Integer>();
		id2ListTypeMap = new HashMap<Integer, String>();
		ResultSet rs = connection.executeQuery(QUERY_LIST_TYPES);
		while (rs.next()) {
			// read the result set
			Integer id = rs.getInt(1);
			String type = rs.getString(2);
			id2ListTypeMap.put(id, type);
			listTyep2IdMap.put(type, id);
			// logger.debug("id=" + id + "; type=" + type );
		}
	}

	/**
	 * @param id
	 *            a numerical ID to be mapped to the corresponding string
	 * @return the string representation of a numerical ID.
	 */
	public static String id2ListType(final int id) {
		return id2ListTypeMap.get(id);
	}

	/**
	 * @param listType
	 *            a string representation to be mapped to the corresponding
	 *            numerical ID.
	 * @return the numerical ID of a string representation.
	 */
	public static int listType2Id(final String listType) {
		return listTyep2IdMap.get(listType);
	}

	/**
	 * Checks if maps have been initialized from database. If not better call
	 * {@link #populatelistTypeMap(SongbirdDbConnection)}
	 * 
	 * @return <code>true</code> if initialized, otherwise <code>false</code>
	 */
	public static boolean isInitialized() {
		if (id2ListTypeMap == null || id2ListTypeMap.size() == 0
				|| listTyep2IdMap == null || listTyep2IdMap.size() == 0) {
			return false;
		} else {
			return true;
		}
	}
}
