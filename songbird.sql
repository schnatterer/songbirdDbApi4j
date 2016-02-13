
select * from MEDIA_ITEMS where content_url like '%songname%'

select DISTINCT item.media_item_id from MEDIA_ITEMS item
inner join SIMPLE_MEDIA_LISTS list ON item.media_item_id = list.media_item_id COLLATE NOCASE

select DISTINCT media_item_id FROM MEDIA_ITEMS
 

select distinct list.media_item_id collate nocase from SIMPLE_MEDIA_LISTS list
left join RESOURCE_PROPERTIES 


-- Duplicates in Playlist
select * from MEDIA_ITEMS i 
left join RESOURCE_PROPERTIES p on i.media_item_id = p.media_item_id
where p.property_id = 15 and obj = 'Playlistname.m3u'

select count(l.member_media_item_id) as numberOfItems, l.member_media_item_id, i.content_url from SIMPLE_MEDIA_LISTS l
left join MEDIA_ITEMS i ON l.member_media_item_id = i.media_item_id
--left join RESOURCE_PROPERTIES artist ON l.media_item_id = artist.media_item_id -- AND artist.property_id = 3
--left join RESOURCE_PROPERTIES track ON l.media_item_id = track.media_item_id AND track.property_id = 1
where l.media_item_id = 31917 collate nocase
group by member_media_item_id
order by numberOfItems desc;


-- Playlists a file belongs to
select props.*, item.content_url from MEDIA_ITEMS item
inner join SIMPLE_MEDIA_LISTS mem on item.media_item_id = mem.member_media_item_id
inner join MEDIA_ITEMS list on mem.media_item_id = list.media_item_id
inner join RESOURCE_PROPERTIES props on list.media_item_id = props.media_item_id and property_id = 15
where item.content_url = lower('file:///X:/music/file.mp3')
