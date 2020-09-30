package com.mowdowndevelopments.blurb.network.responseModels

import com.mowdowndevelopments.blurb.database.entities.Feed
import com.squareup.moshi.Json
import java.util.*

data class GetFeedsResponse(/*
    * Maps name of folder to feed IDs.
    * */
        @field:Json(name = "flat_folders") val folders: Map<String, Array<Int>>, /*
      * Maps feed ID parsed to String to its appropriate feed.
      * */
        val feeds //Key will be Feed ID parsed to String
        : Map<String, Feed>) {

    val feedIds: ArrayList<Int>
        get() {
            val ids = ArrayList<Int>()
            for (idString in feeds.keys) {
                ids.add(idString.toInt())
            }
            return ids
        }
    val invertedFolderMap: HashMap<Int, String>
        get() {
            val invertedFolders = HashMap<Int, String>()
            for (key in folders.keys) {
                for (newKey in requireNotNull(folders[key])) {
                    invertedFolders[newKey] = key
                }
            }
            return invertedFolders
        }
}