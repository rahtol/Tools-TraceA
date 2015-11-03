(: all ids of segemnts with a bumper :)
for $seg in //SegmentDataList/Item[BumperList]//descendant::segment_subid
return $seg + ($seg/ancestor::NetworkSection/descendant::networksection_id)*256