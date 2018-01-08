package pl.mirror.black.blackmirror.ui;

import android.util.Log;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import pl.mirror.black.blackmirror.model.location.TimeZone;
import pl.mirror.black.blackmirror.model.news.News;
import pl.mirror.black.blackmirror.model.weather.WeatherResponse;
import pl.mirror.black.blackmirror.network.LocationRepository;
import pl.mirror.black.blackmirror.network.NewsRepository;
import pl.mirror.black.blackmirror.network.WeatherRepository;
import pl.mirror.black.blackmirror.network.api.location.LocationDataSource;
import pl.mirror.black.blackmirror.network.api.weather.WeatherDataSource;
import pl.mirror.black.blackmirror.network.rss.news.NewsDataSource;
import pl.mirror.black.blackmirror.speechrecognition.TextCommandInterpreter;
import pl.mirror.black.blackmirror.ui.presenter.Presenter;

/**
 * Klasa zarządzająca widokiem HomeView.
 */
class HomePresenter extends Presenter<HomeView> implements TextCommandInterpreter.Listener {

    private static final String TAG = "HomePresenter";

    private final WeatherRepository weatherDataSource = new WeatherDataSource();

    private final LocationRepository locationDataSource = new LocationDataSource();

    private final NewsRepository newsDataSource = new NewsDataSource();

    private TextCommandInterpreter textCommandInterpreter = new TextCommandInterpreter(this);

    private CompositeDisposable disposables = new CompositeDisposable();

    void onSpeechRecognized(String result) {
        textCommandInterpreter.interpret(result);
    }

    @Override
    protected void onAttachView(HomeView view) {
        super.onAttachView(view);
        this.view.startSplashScreen();
    }

    @Override
    protected void onDetachView() {
        super.onDetachView();
        if (!disposables.isDisposed()) {
            disposables.dispose();
        }
    }

    @Override
    public void onFailureCommandRecognizing() {
        Log.e(TAG, "Text command interpreter failed to recognize command");
        this.view.showError("Niepoprawna komenda");
    }

    @Override
    public void onShowWeatherCommandRecognized(String location) {
        Log.i(TAG, "Text command interpreter recognized weather command for location: " + location);
        disposables.add(weatherDataSource.getWeatherByCityName(location, "metric", "pl")
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new WeatherResponseObserver()));
    }

    @Override
    public void onHideWeatherCommandRecognized() {
        Log.i(TAG, "Text command interpreter recognized hide weather widget command.");
        this.view.hideWeatherWidget();
    }

    @Override
    public void onShowTimeCommandRecognized(String location) {
        Log.i(TAG, "Text command interpreter recognized time command for location: " + location);
        disposables.add(locationDataSource.getTimeZoneByLocationName(location)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new TimeZoneObserver()));
    }

    @Override
    public void onHideTimeCommandRecognized() {
        Log.i(TAG, "Text command interpreter recognized hide time widget command.");
        this.view.hideTimeWidget();
    }

    @Override
    public void onShowCalendarCommandRecognized() {
        Log.i(TAG, "Text command interpreter recognized show calendar command.");
        this.view.showCalendarWidget();
    }

    @Override
    public void onHideCalendarCommandRecognized() {
        Log.i(TAG, "Text command interpreter recognized hide calendar command.");
        this.view.hideCalendarWidget();
    }

    @Override
    public void onNextMonthCommandRecognized() {
        Log.i(TAG, "Text command interpreter recognized calendar next month command.");
        this.view.setCalendarNextMonth();
    }

    @Override
    public void onPreviousMonthRecognized() {
        Log.i(TAG, "Text command interpreter recognized calendar previous month command.");
        this.view.setCalendarPreviousMonth();
    }

    @Override
    public void onShowNewsCommandRecognized() {
        Log.i(TAG, "Text command interpreter recognized show all news command.");
        callPolsatNews();
        callTvnNews();
    }

    @Override
    public void onHideNewsCommandRecognized() {
        Log.i(TAG, "Text command interpreter recognized hide all news command.");
        this.view.hideAllNewsWidget();
    }

    @Override
    public void onShowTvnNewsCommandRecognized() {
        Log.i(TAG, "Text command interpreter recognized show tvn news command.");
        callTvnNews();
    }

    @Override
    public void onShowPolsatNewsCommandRecognized() {
        Log.i(TAG, "Text command interpreter recognized show polsat news command.");
        callPolsatNews();
    }

    private void callTvnNews() {
        Log.i(TAG, "Text command interpreter recognized news command for tvn news, polsat hide if visible");
        disposables.add(newsDataSource.getTvnNews()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new TvnNewsObserver()));
    }

    private void callPolsatNews() {
        Log.i(TAG, "Text command interpreter recognized news command for polsat news, tvn24 hide if visible");
        disposables.add(newsDataSource.getPolsatNews()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new PolsatNewsObserver()));
    }

    /**
     * Obserwuje status wykonanego żądania restowego pogody.
     */
    private class WeatherResponseObserver extends DisposableSingleObserver<WeatherResponse> {
        @Override
        public void onSuccess(@NonNull WeatherResponse weatherResponse) {
            Log.i(TAG, "WeatherResponseObserver: onSuccess");
            HomePresenter.this.view.showWeatherWidget(weatherResponse);
        }

        @Override
        public void onError(@NonNull Throwable e) {
            Log.e(TAG, "WeatherResponseObserver: onError - " + e.getCause());
            HomePresenter.this.view.showError("Nie znaleziono pogody");
        }
    }

    /**
     * Obserwuje status wykonanego żądania restowego czasu.
     */
    private class TimeZoneObserver extends DisposableSingleObserver<TimeZone> {
        @Override
        public void onSuccess(@NonNull TimeZone timeZone) {
            Log.i(TAG, "TimeZoneObserver: onSuccess");
            HomePresenter.this.view.showTimeWidget(timeZone.timeZone);
        }

        @Override
        public void onError(@NonNull Throwable e) {
            Log.e(TAG, "TimeZoneObserver: onError - " + e.getCause());
            HomePresenter.this.view.showError("Nie znaleziono czasu dla podanej strefy");
        }
    }

    /**
     * Obserwuje status wykonanego żądania rss wiadomości ze świata.
     */
    private class TvnNewsObserver extends DisposableSingleObserver<List<News>> {
        @Override
        public void onSuccess(List<News> news) {
            Log.i(TAG, "TvnNewsObserver: onSuccess");
            HomePresenter.this.view.showTvnNewsWidget(news);
        }

        @Override
        public void onError(Throwable e) {
            Log.e(TAG, "TvnNewsObserver: onError - " + e.getCause());
            HomePresenter.this.view.showError("Nie udało się pobrać wiadomości");
        }
    }

    /**
     * Obserwuje status wykonanego żądania rss wiadomości ze świata.
     */
    private class PolsatNewsObserver extends DisposableSingleObserver<List<News>> {
        @Override
        public void onSuccess(List<News> news) {
            Log.i(TAG, "PolsatNewsObserver: onSuccess");
            HomePresenter.this.view.showPolsatNewsWidget(news);
        }

        @Override
        public void onError(Throwable e) {
            Log.e(TAG, "PolsatNewsObserver: onError - " + e.getCause());
            e.printStackTrace();
            HomePresenter.this.view.showError("Nie udało się pobrać wiadomości");
        }
    }
}
