package pl.mirror.black.blackmirror.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import pl.mirror.black.blackmirror.R;
import pl.mirror.black.blackmirror.model.weather.WeatherResponse;
import pl.mirror.black.blackmirror.speechrecognition.googlespeechapi.SpeechRecognizer;
import pl.mirror.black.blackmirror.speechrecognition.sphinx.ActivationKeywordListener;
import pl.mirror.black.blackmirror.speechrecognition.sphinx.PocketSphinx;
import pl.mirror.black.blackmirror.ui.widget.CommandRecognizingAnimationView;
import pl.mirror.black.blackmirror.ui.widget.WeatherWidgetView;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class HomeActivity extends BaseActivity implements HomeView,
        ActivationKeywordListener, SpeechRecognizer.Listener {

    @Override
    protected Integer getLayoutRes() {
        return R.layout.activity_home;
    }

    public HomePresenter homePresenter = new HomePresenter();

    @BindView(R.id.activation_keyword_indicator)
    public TextView activationKeywordIndicator;

    @BindView(R.id.command_recognizing_animation)
    public CommandRecognizingAnimationView commandRecognizingAnimation;

    @BindView(R.id.weather_widget)
    public WeatherWidgetView weatherWidget;

    @BindView(R.id.clock)
    public TextClock clock;

    private PocketSphinx pocketSphinx;

    private static final String TAG = "HomeActivity";

    private SpeechRecognizer commandSpeechRecognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pocketSphinx = new PocketSphinx(this, this);

        commandSpeechRecognizer = new SpeechRecognizer(this, this);
        homePresenter.onAttachView(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        commandSpeechRecognizer.onStart();
    }

    @Override
    protected void onStop() {
        commandSpeechRecognizer.onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pocketSphinx.onDestroy();
    }

    @Override
    public void onActivationKeywordRecognizerReady() {
        Log.i("ACTIVATION PHRASE ", " ACTIVATION READY ");
        activationKeywordIndicator.setVisibility(View.VISIBLE);
        pocketSphinx.startListeningToActivationKeyword();
    }

    @Override
    public void onActivationKeywordDetected() {
        Log.i("ACTIVATION PHRASE ", " DETECTED ");
        commandRecognizingAnimation.startAnimation();
        activationKeywordIndicator.setVisibility(INVISIBLE);
        commandSpeechRecognizer.startListeningCommand();
    }

    @Override
    public void onSpeechRecognized(final String result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                homePresenter.onSpeechRecognized(result);
            }
        });
    }

    @Override
    public void onFinishSpeechRecognizing() {
        pocketSphinx.startListeningToActivationKeyword();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activationKeywordIndicator.setVisibility(VISIBLE);
                commandRecognizingAnimation.stopAnimation();
            }
        });
    }

    @Override
    public void showWeatherWidget(WeatherResponse weatherResponse) {
        weatherWidget.show(weatherResponse);
    }

    @Override
    public void showClockWidget(String timeZone) {
        clock.setTimeZone(timeZone);
    }

    @Override
    public void showNewsWidget() {

    }

    @Override
    public void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

}
