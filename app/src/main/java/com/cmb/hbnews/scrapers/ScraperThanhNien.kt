package com.cmb.hbnews.scrapers

import com.cmb.hbnews.R
import com.cmb.hbnews.models.News
import com.cmb.hbnews.models.NewsHeader
import com.cmb.hbnews.models.NewsItems.NewsItemImage
import com.cmb.hbnews.models.NewsItems.NewsItemText
import okhttp3.Request
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import java.io.IOException

class ScraperThanhNien : NewsScraper {
    override fun getNewsHeaders(category: NewsCategory): ArrayList<NewsHeader> {

        val pageUrl = when (category) {
            NewsCategory.LATEST -> "https://thanhnien.vn/tin-24h.html"
            NewsCategory.CURRENT_AFFAIRS -> "https://thanhnien.vn/thoi-su/"
            NewsCategory.BUSINESS -> "https://thanhnien.vn/tai-chinh-kinh-doanh/"
            NewsCategory.SPORTS -> "https://thanhnien.vn/the-thao/latest.html"
            NewsCategory.ENTERTAINMENT -> "https://thanhnien.vn/giai-tri/"
            NewsCategory.TECHNOLOGY -> "https://thanhnien.vn/cong-nghe-game/tin-tuc/"
            NewsCategory.LIFESTYLE -> "https://thanhnien.vn/doi-song/"
            NewsCategory.HEALTH -> "https://thanhnien.vn/suc-khoe/"
            NewsCategory.TRAVEL -> "https://thanhnien.vn/du-lich/"
            else -> throw NotImplementedError()
        }

        val doc = Jsoup.connect(pageUrl).get()

        val newsHeadersElems = when (category) {
          NewsCategory.TECHNOLOGY -> doc.select("div.zone--timeline > article")
          else -> doc.select("div.zone--timeline > div > article")
        }

        return when (category) {
            NewsCategory.LATEST -> parseNewsHeadersLatest(newsHeadersElems)
            else -> parseNewsHeaders(newsHeadersElems)
        }
    }

    private fun parseNewsHeaders(newsHeadersElems: Elements): ArrayList<NewsHeader> {
        var newsHeaders = arrayListOf<NewsHeader>()
        for (elem in newsHeadersElems) {
        val storyTitle = elem.selectFirst("a.story__title")!!;
            newsHeaders.add(
                NewsHeader(
                    title = storyTitle.text(),
                    description = elem.selectFirst("div.summary")!!.text(),
                    imgSrc = elem.selectFirst("a.story__thumb > img")!!.attr("data-src"),
                    newsSrcLogoResource = R.drawable.ic_logo_thanhnien,
                    newsUrl = storyTitle.attr("href"),
                    date = elem.selectFirst("div.meta > span.time")!!.text()
                )
            )
        }
        return newsHeaders
    }

    private fun parseNewsHeadersLatest(newsHeadersElems: Elements): ArrayList<NewsHeader> {
        var newsHeaders = arrayListOf<NewsHeader>()
        for (elem in newsHeadersElems) {
            var storyTitle = elem.selectFirst("a.story__title")!!
            var imgElem = elem.selectFirst("a.story__thumb > img")!!
            var imageSrc = imgElem.attr("data-src")
            if (imageSrc.isEmpty())
                imageSrc = imgElem.attr("src")

            newsHeaders.add(
                NewsHeader(
                    title = storyTitle.text(),
                    description = storyTitle.text(),
                    imgSrc = elem.selectFirst("a.story__thumb > img")!!.attr("src"),
                    newsSrcLogoResource = R.drawable.ic_logo_thanhnien,
                    newsUrl = storyTitle.attr("href"),
                    date = elem.selectFirst("div.meta > span.time")!!.text()
                )
            )
        }
        return newsHeaders
    }

    override fun getNewsFromUrl(url: String): News {
        val doc = Jsoup.connect(url).get()
        val news = News();

        news.title = doc.selectFirst("h1.cms-title")!!.text();
        news.description = doc.selectFirst("div.cms-desc")!!.text();
        news.author = doc.selectFirst("a.cms-author")!!.text();
        news.date = doc.selectFirst("div.meta > time")!!.text();

        val articleItems = doc.select("div#abody > *");
        for (item in articleItems) {
            when (item.tagName()) {
                "p" -> news.content.add(NewsItemText(item.text(), NewsItemText.TextType.P))
                "h2" -> news.content.add(NewsItemText(item.text(), NewsItemText.TextType.H2))
                "table" -> when (item.className()) {
                    "picture" -> news.content.add(NewsItemImage(
                                    item.selectFirst("img")!!.attr("data-src"),
                                    item.selectFirst("td.caption > p")!!.text()
                                ))
                }
            }
        }
        return news;
    }
}
