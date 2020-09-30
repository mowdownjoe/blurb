package com.mowdowndevelopments.blurb.network.responseModels

import com.squareup.moshi.Json

data class AutoCompleteResponse(@field:Json(name = "label") val feedTitle: String,
                                @field:Json(name = "num_subscribers") val subscriberCount: Int,
                                val tagline: String,
                                @field:Json(name = "value") val url: String)