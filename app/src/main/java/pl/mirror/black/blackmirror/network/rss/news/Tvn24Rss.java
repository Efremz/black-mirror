package pl.mirror.black.blackmirror.network.rss.news;

import io.reactivex.Single;
import pl.mirror.black.blackmirror.model.news.Tvn24News;
import retrofit2.http.GET;

public interface Tvn24Rss {

    @GET("/najnowsze.xml")
    Single<Tvn24News> getNews();
    /* TEFAL KURWA EN XD */

}
