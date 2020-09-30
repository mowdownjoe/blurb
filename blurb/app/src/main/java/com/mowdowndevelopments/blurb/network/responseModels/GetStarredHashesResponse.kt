package com.mowdowndevelopments.blurb.network.responseModels

import com.squareup.moshi.Json

data class GetStarredHashesResponse(@field:Json(name = "starred_story_hashes") val starredStoryHashes: List<String>)