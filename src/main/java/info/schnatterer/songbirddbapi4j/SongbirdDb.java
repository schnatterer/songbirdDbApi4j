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
package info.schnatterer.songbirddbapi4j;

import info.schnatterer.songbirddbapi4j.domain.MediaItem;
import info.schnatterer.songbirddbapi4j.domain.MediaListTypes;
import info.schnatterer.songbirddbapi4j.domain.MemberMediaItem;
import info.schnatterer.songbirddbapi4j.domain.Property;
import info.schnatterer.songbirddbapi4j.domain.SimpleMediaList;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Provides queries to the songbird database related to playlists and its
 * members.
 * 
 * @author schnatterer
 * 
 */
public class SongbirdDb {
	// /** SLF4J-Logger. */
	// private static Logger logger =
	// LoggerFactory.getLogger(PlaylistService.class);

	/**
	 * Gets all media items that have is_list = 0.
	 */
	public static final String QUERY_MEDIA_ITEMS = "select m.media_item_id, m.content_url"
			+ ", m.created, m.updated, r.property_id, r.obj from media_items m "
			+ "left join resource_properties as r on m.media_item_id = r.media_item_id "
			+ "where m.is_list = 0 " + "order by m.media_item_id ";

	/**
	 * Gets all media items that have is_list = 1, INCLUDING emtpy ones. Also
	 * finds dynamic playlist (subscriptions) (unfortunately also finds playlist
	 * that have name null)
	 */
	public static final String QUERY_MEDIA_LISTS_TYPE_SIMPLE = "select m.media_item_id, m.content_url"
			+ ", m.media_list_type_id, m.created, m.updated, r.property_id, r.obj from media_items m "
			+ "left join resource_properties as r on m.media_item_id = r.media_item_id "
			+ "left join media_list_types as mlt on m.media_list_type_id = mlt.media_list_type_id "
			+ "where m.is_list = 1 "
			+ "and m.media_list_type_id is not null "
			+ "order by m.media_item_id ";

	/**
	 * Gets all media items with a single playlist (realized as
	 * {@link PreparedStatement}). The results contains several lines for one
	 * member, that is it also contains the properties, in order to minimize the
	 * queries sent to SQLite.
	 * 
	 */
	public static final String QUERY_MEDIA_LIST = "select l.member_media_item_id media_item_id, l.ordinal, m.content_url "
			+ ", m.media_list_type_id, m.created, m.updated, r.property_id, r.obj from simple_media_lists l "
			+ "left join media_items m ON m.media_item_id = l.member_media_item_id "
			+ "left join resource_properties as r on m.media_item_id = r.media_item_id "
			// + "where l.media_item_id =? COLLATE NOCASE " +
			// "order by l.member_media_item_id ";
			+ "where l.media_item_id =? COLLATE NOCASE "
			+ "order by l.ordinal COLLATE NOCASE";

	private final String pathToDb;

	// @SuppressWarnings("serial")
	// public static final Set<String> PLAYLISTS_IGNORE = new HashSet<String>()
	// {
	// {
	// add("&chrome://songbird/locale/songbird.properties#device.download");
	// add("&smart.defaultlist.recentlyplayed");
	// add("&device.sync.video-togo.playlist");
	// }
	// };

	// /** SLF4J-Logger. */
	// private static Logger logger = LoggerFactory
	// .getLogger(PlaylistService.class);

	// private List<MediaItem> playListItems = new LinkedList<MediaItem>();
	// private List<SimpleMediaList> playLists = new
	// LinkedList<SimpleMediaList>();

	/**
	 * Creates a wrapper for the songbird database at a specific location.
	 * 
	 * @param pathToDb
	 *            the dbUrl to the songbird database file
	 */
	public SongbirdDb(String pathToDb) {
		this.pathToDb = pathToDb;

		// Make sure Property and MediaListType Maps are initialized
		synchronized (SongbirdDb.class) {
			if (!MediaListTypes.isInitialized() || !Property.isInitialized()) {
				SongbirdDbConnection connection = new SongbirdDbConnection(
						pathToDb);
				try {
					MediaListTypes.populatelistTypeMap(connection);
					Property.populateResourceMap(connection);
				} catch (SQLException e) {
					throw new RuntimeException(e);
				} finally {
					connection.close();
				}
			}
		}
	}

	/**
	 * Gets only the {@link MediaItem}s that are not playlists.
	 * 
	 * @return a list of all tracks
	 * 
	 * @throws SQLException
	 *             database-related exceptions
	 */
	public List<MediaItem> getAllTracks() throws SQLException {
		List<MediaItem> playListItems = new LinkedList<MediaItem>();

		SongbirdDbConnection connection = new SongbirdDbConnection(pathToDb);
		try {
			ResultSet rs = connection.executeQuery(QUERY_MEDIA_ITEMS);

			if (rs.next()) { // If there are results at all
				int currentId = rs.getInt("media_item_id");
				while (currentId >= 0) {
					MediaItem m = new MediaItem();
					currentId = readMediaItem(rs, currentId, m, false);
					playListItems.add(m);
				}
			}
			return playListItems;
		} finally {
			connection.close();
		}
	}

	/**
	 * Gets only the {@link MediaItem}s that are playlists. Does not get the
	 * {@link MediaItem}s which are members of the playlist. Ignores all
	 * playlists that don't have a name.
	 * 
	 * @param ignoreInternalPlaylists
	 *            <code>true</code> ignores Songbird's internal playlists (all
	 *            playlists having an mediaListType != simple)
	 * @param skipDynamicLists
	 *            <code>true</code> does not return songbird's "smart" playlists
	 *            (all playlists whose name starts with "&amp;smart")
	 * @return the playlist {@link MediaItem}
	 * @throws SQLException
	 *             database-related exceptions
	 * @see #getPlayLists(boolean, boolean)
	 */
	public List<MediaItem> getPlaylistItems(
			final boolean ignoreInternalPlaylists,
			final boolean skipDynamicLists) throws SQLException {
		/**
		 * Note: Get all playlists and attributes in on result set like this
		 * 
		 * <pre>
		 * select m.media_item_id, m.content_url, lt.type, p.property_name, r.obj from media_items m 
		 * 	left join resource_properties as r on m.media_item_id = r.media_item_id 
		 * 	left join media_list_types as mlt on m.media_list_type_id = mlt.media_list_type_id
		 * 	left join properties p on r.property_id =  p.property_id
		 * 	left join MEDIA_LIST_TYPES lt on m.media_list_type_id = lt.media_list_type_id
		 * where m.is_list = 1 and m.media_list_type_id is not null order by m.media_item_id
		 * </pre>
		 */
		List<MediaItem> playListItems = new LinkedList<MediaItem>();

		SongbirdDbConnection connection = new SongbirdDbConnection(pathToDb);
		try {
			ResultSet rs = connection
					.executeQuery(QUERY_MEDIA_LISTS_TYPE_SIMPLE);

			if (rs.next()) { // If there are results at all
				int currentId = rs.getInt("media_item_id");
				while (currentId >= 0) {
					MediaItem m = new MediaItem();
					currentId = readMediaItem(rs, currentId, m, true);

					String mediaListName = m
							.getProperty(Property.PROP_MEDIA_LIST_NAME);

					// Skip all playlist without name
					if (mediaListName == null) {
						continue;
					}

					if (ignoreInternalPlaylists) {
						String mediaListType = m
								.getProperty(Property.PROP_CUSTOM_TYPE);
						// if (mediaListName != null &&
						// mediaListName.startsWith("&")) {
						/*
						 * Note: Dynamic and internal playlists start with '&'.
						 * However, a dynamic playlist's customType is "simple",
						 * as with it is for "normal" playlists. Internal
						 * playlists have a type like "download" or "smart". So
						 * in order to ignore internal playlists it would be
						 * possible to filter for the custom type.
						 * 
						 * In addition, most internal playlists don't have a
						 * name. But not all!
						 * 
						 * It also seems that only internal lists have a
						 * mediaListType of "dynamic". But not all!
						 * 
						 * So skip any playlists with customType != "simple"
						 */
						if (mediaListType != null
								&& !mediaListType.equals("simple")) {
							// && mediaListName == null &&
							// m.getListType().equals("dynamic")) {
							// Skip this one
							continue;
						}
					}

					if (skipDynamicLists) {
						/* Note: Dynamic lists begin with "&smart". */
						if (mediaListName.startsWith("&smart")) {
							// Skip this one
							continue;
						}
					}

					// /* Discard playlist that don't have a name property */
					// if (m.getProperty(Property.PROP_MEDIA_LIST_NAME) == null)
					// {
					// logger.warn("Found playlist with no name. Skipping list. "
					// + m);
					// } else if (skipDynamicLists &&
					// m.getListType().equals("dynamic")) {
					// logger.info("Skipping dynamic list " +
					// m.getProperty(Property.PROP_MEDIA_LIST_NAME));
					// } else {
					playListItems.add(m);
					// }

					// logger.debug("ID: " + m.getId() + ": \""
					// + m.getProperty(Property.PROP_MEDIA_LIST_NAME)
					// + "\"; Type: " + m.getListType());
				}
			}
			return playListItems;
		} finally {
			connection.close();
		}
	}

	/**
	 * Gets all {@link MediaItem}s that are playlists and also aggregates the
	 * {@link MediaItem}s that are members of the playlists. Ignores all
	 * playlists that don't have a name.
	 * 
	 * @param ignoreInternalPlaylists
	 *            <code>true</code> ignores Songbird's internal playlists (all
	 *            playlists having an mediaListType != simple)
	 * @param skipDynamicLists
	 *            <code>true</code> does not return songbird's "smart" playlists
	 *            (all playlists whose name starts with "&amp;smart")
	 * 
	 * @return an object that contains the "parent" (playlist) {@link MediaItem}
	 *         as well as all of its member {@link MediaItem}s
	 * @throws SQLException
	 *             database-related exceptions
	 */
	public List<SimpleMediaList> getPlayLists(
			final boolean ignoreInternalPlaylists,
			final boolean skipDynamicLists) throws SQLException {

		List<MediaItem> playListItems = getPlaylistItems(
				ignoreInternalPlaylists, skipDynamicLists);

		List<SimpleMediaList> playLists = new LinkedList<SimpleMediaList>();
		SongbirdDbConnection connection = new SongbirdDbConnection(pathToDb);
		try {
			PreparedStatement queryMediaList = connection
					.preparedStatement(QUERY_MEDIA_LIST);
			for (MediaItem playlistMediaItem : playListItems) {

				// This is taken care of by getPlaylistItems() now
				// if (ignoreInternalPlaylists) {
				// String mediaListName =
				// playlistMediaItem.getProperty(Property.PROP_MEDIA_LIST_NAME);
				// String mediaListType =
				// playlistMediaItem.getProperty(Property.PROP_CUSTOM_TYPE);
				// /*
				// * Note: Dynamic and internal playlists start with '&'.
				// However, a dynamic playlist's customType is
				// * "simple", as with it is for "normal" playlists. Internal
				// playlists have a type like "download" or
				// * "smart". So in order to ignore internal playlists it would
				// be possible to filter for the custom
				// type.
				// * In addition, most internal playlists don't have a name.
				// *
				// * So skip any playlists without name and customType !=
				// "simple".
				// */
				// // if (mediaListName != null &&
				// mediaListName.startsWith("&")) {
				// if (mediaListName == null && mediaListType != null &&
				// !mediaListType.equals("simple")) {
				// // Skip this one
				// continue;
				// }
				// }

				/* Query members of playlist */
				queryMediaList.setInt(1, playlistMediaItem.getId());

				ResultSet rs = queryMediaList.executeQuery();

				SimpleMediaList list = new SimpleMediaList();
				list.setList(playlistMediaItem);
				// Map<String, MediaItem> members = new TreeMap<String,
				// MediaItem>();

				List<MemberMediaItem> members = list.getMembers();

				if (rs.next()) { // If there are results at all
					int currentId = rs.getInt("media_item_id");
					while (currentId >= 0) {

						MemberMediaItem memberWrapper = new MemberMediaItem();
						// ordinal looks like "168.225.0.-1.0"
						memberWrapper.setOridnal(rs.getString("ordinal"));

						// read the result set
						MediaItem member = new MediaItem();
						currentId = readMediaItem(rs, currentId, member, true);

						memberWrapper.setMember(member);

						// members.put(ordinal, m);
						members.add(memberWrapper);
					}
				}

				// list.setMembers(members);
				list.sortMembers(true);

				playLists.add(list);
				// } catch (IOException e) {
				// logger.error("unable to write to file", e);
				// } finally {
				// try {
				// out.close();
				// } catch (IOException e) {
				// logger.error("unable to close file", e);
				// }
				// }
				// } catch (FileNotFoundException e) {
				// logger.error("unable to open file for writing", e);
				// }
			}
			return playLists;
		} finally {
			connection.close();
		}
	}

	/**
	 * Reads all attributes of a {@link MediaItem} from a {@link ResultSet}.
	 * 
	 * @param rs
	 *            database to read from
	 * @param currentId
	 * @param mediaItem
	 *            item to attach the properties to
	 * @return the next id returned by the cursor
	 * @throws SQLException
	 */
	private int readMediaItem(ResultSet rs, int currentId, MediaItem mediaItem,
			boolean setListType) throws SQLException {
		mediaItem.setId(currentId);
		mediaItem.setDateCreated(new Date(rs.getLong("created")));
		mediaItem.setDateUpdated(new Date(rs.getLong("updated")));
		if (setListType) {
			mediaItem.setListType(rs.getInt("media_list_type_id"));
		}
		mediaItem.setContentUrl(rs.getString("content_url"));
		currentId = readProperties(mediaItem, rs, "media_item_id");
		return currentId;
	}

	/**
	 * Reads all properties for an id column (integer) and appends them to
	 * <code>mediaItem</code>. Note: This will only work well if <code>rs</code>
	 * is ordered by <code>idColumn</code>.
	 * 
	 * @param mediaItem
	 *            item to attach the properties to
	 * @param rs
	 *            database to read from
	 * @param idColumn
	 *            name of the ID column (needed for comparing)
	 * @return the next id or -1 if there is no more data in <code>rs</code>
	 * @throws SQLException
	 *             database-related exceptions
	 */
	private int readProperties(final MediaItem mediaItem, final ResultSet rs,
			final String idColumn) throws SQLException {
		boolean moreData = false;
		int currentId = mediaItem.getId();
		Map<Integer, String> props = mediaItem.getProperties();
		/* Set all props to this media item */
		do {
			props.put(rs.getInt("property_id"), rs.getString("obj"));
			moreData = rs.next();
			if (!moreData) {
				break;
			}
			currentId = rs.getInt(idColumn);
		} while (currentId == mediaItem.getId());

		if (!moreData) {
			return -1;
		}
		return currentId;
	}

}
