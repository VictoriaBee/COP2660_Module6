package com.raywenderlich.podplay.service

import com.raywenderlich.podplay.util.DateUtils
import okhttp3.*
import org.w3c.dom.Node
import java.io.IOException
import javax.xml.parsers.DocumentBuilderFactory

class RssFeedService: FeedService {
    override fun getFeed(xmlFileURL: String,
        callBack: (RssFeedResponse?) -> Unit) {

        // 1 - Creates new instance of OkHttpClient.
        val client = OkHttpClient()
        // 2 - An HTTP Request obj is required to make a call
        // with OkHttpClient.
        val request = Request.Builder()
            .url(xmlFileURL)
            .build()
        // 3 - Passes it into the client through the newCall() method,
        // which returns a Call obj.
        client.newCall(request).enqueue(object : Callback {
            // 4 - Defines onFailure() to handle the call from OkHttp
            // if the Request fails.
            override fun onFailure(call: Call, e: IOException) {
                callBack(null)
            }
            // 5 - If Request succeeds, it's called by OkHttp.
            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                // 6 - Checks the response for success.
                if (response.isSuccessful) {
                    // 7 - Checks the response body for null.
                    response.body()?.let { responseBody ->
                        // 8
                        val dbFactory = DocumentBuilderFactory.newInstance()
                        val dBuilder = dbFactory.newDocumentBuilder()
                        val doc = dBuilder.parse(responseBody.byteStream())

                        val rssFeedResponse = RssFeedResponse(episodes = mutableListOf())
                        domToRssFeedResponse(doc, rssFeedResponse)
                        callBack(rssFeedResponse)
                        println(rssFeedResponse)

                        return
                    }
                }
                // 9
                callBack(null)
            }
        })
    }

    private fun domToRssFeedResponse(node: Node,
        rssFeedResponse: RssFeedResponse) {
        // 1 - Checks the nodeType to make sure it's an XML element.
        if (node.nodeType == Node.ELEMENT_NODE) {
            // 2 - Stores node's name and parent name.
            val nodeName = node.nodeName
            val parentName = node.parentNode.nodeName
            // 1
            val grandParentName = node.parentNode.parentNode?.nodeName ?: ""
            // 2 - If node is child of an item node, and item node is a child of
            // a channel node, then it's an episode element.
            if (parentName == "item" && grandParentName == "channel") {
                // 3 - Assigns currentItem to the last episode in episodes list.
                val currentItem = rssFeedResponse.episodes?.last()
                if (currentItem != null) {
                    // 4 - when expression is used to switch on current node's name.
                    when (nodeName) {
                        "title" -> currentItem.title = node.textContent
                        "description" -> currentItem.description = node.textContent
                        "itunes:duration" -> currentItem.duration = node.textContent
                        "guid" -> currentItem.guid = node.textContent
                        "pubDate" -> currentItem.pubDate = node.textContent
                        "link" -> currentItem.link = node.textContent
                        "enclosure" -> {
                            currentItem.url = node.attributes.getNamedItem("url")
                                    .textContent
                            currentItem.type = node.attributes.getNamedItem("type")
                                    .textContent
                        }
                    }
                }
            }
            // 3 - If current node is child of the channel node,
            // it extracts the top level RSS feed info from this node.
            if (parentName == "channel") {
                // 4 - Depending on the name, it fills in data with textContent of the node.
                // If node is episode item,
                // it adds a new empty EpisodeResponse obj to the episodes list.
                when (nodeName) {
                    "title" -> rssFeedResponse.title = node.textContent
                    "description" -> rssFeedResponse.description = node.textContent
                    "itunes:summary" -> rssFeedResponse.summary = node.textContent
                    "item" -> rssFeedResponse.episodes?.
                            add(RssFeedResponse.EpisodeResponse())
                    "pubDate" -> rssFeedResponse.lastUpdated =
                            DateUtils.xmlDateToDate(node.textContent)
                }
            }
        }
        // 5 - Assigns the nodeList to the list of child nodes for current node.
        val nodeList = node.childNodes
        for (i in 0 until nodeList.length) {
            val childNode = nodeList.item(i)
            // 6 - For each child node, calls domToRssFeedResponse(),
            // which allows domToRssResponse() to keep building
            // out the rssFeedResponse obj.
            domToRssFeedResponse(childNode, rssFeedResponse)
        }
    }
}

interface FeedService {
    // 1 - Takes a URL pointing to an RSS file and callback method.
    // After file is loaded and parsed, callback method gets called
    // with the final RSS feed response.
    fun getFeed(xmlFileURL: String,
        callBack: (RssFeedResponse?) -> Unit)
    // 2 - Provides a singleton instance of the FeedService.
    companion object {
        val instance: FeedService by lazy {
            RssFeedService()
        }
    }
}