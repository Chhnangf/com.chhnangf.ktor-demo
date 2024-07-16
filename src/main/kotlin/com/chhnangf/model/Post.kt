package com.chhnangf.model

import kotlinx.serialization.Serializable

@Serializable
data class PostPostObject (
    // 没有数据库，临时屏蔽id，内容设置可空
    val id:Int,
    val title:String?,
    val content:String?,
    val imageUrl:List<String?>,
)

//@Serializable
//data class PhotoObjects(
//    val objectID: Int,
//    val title: String,
//    val artistDisplayName: String,
//    val medium: String,
//    val dimensions: String,
//    val objectURL: String,
//    val objectDate: String,
//    val primaryImage: String,
//    val primaryImageSmall: String,
//    val repository: String,
//    val department: String,
//    val creditLine: String,
//)
