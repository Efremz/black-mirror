package pl.mirror.black.blackmirror.speechrecognition;


import java.util.ArrayList;
import java.util.Arrays;

public class TextCommandInterpreter {

    public interface Listener {
        void onWeatherCommandRecognized(String location);

        void onTimeCommandRecognized(String location);

        void onFailureCommandRecognizing();

        void onHideWeatherWidget();

        void onShowCalendarRecognized();

        void onNewsCommandRecognized();
    }

    private TextCommandInterpreter.Listener listener;

    private final ArrayList<String> weatherVocabulary = new ArrayList<>(
            Arrays.asList("pogoda", "pogodę", "pogode", "pogodą", "pogody", "pogód"));

    private final ArrayList<String> cityVocabulary = new ArrayList<>(
            Arrays.asList("miasto", "miast", "miasta", "miasteczko", "miastom", "miastu"));

    private final ArrayList<String> countryVocabulary = new ArrayList<>(
            Arrays.asList("kraj", "kraju", "krajowi", "krajom"));

    private final ArrayList<String> timeVocabulary = new ArrayList<>(
            Arrays.asList("czas", "strefy", "czasu", "czasom", "czasowej", "strefę", "strefe"));

    private final ArrayList<String> newsVocabulary = new ArrayList<>(
            Arrays.asList("news", "newsy", "newsa", "newsa", "wiadomość", "wiadomosc", "wiadomości", "wiadomością", "wiadomoscia"));

    private final ArrayList<String> showVocabulary = new ArrayList<>(
            Arrays.asList("pokaż", "pokaz", "pokazuj", "wyświetl", "wyswietl", "wyświetlić", "wyswietlic"));

    private final ArrayList<String> calendarVocabulary = new ArrayList<>(
            Arrays.asList("kalendarz", "kalendarzy", "kalendarzom", "kalendarza", "kalendarzów", "kalendarzem", "kalendarzu", "kalendarzowi"));

    public TextCommandInterpreter(TextCommandInterpreter.Listener listener) {
        this.listener = listener;
    }

    public void interpret(String result) {
        String interpretingCommand = result.toLowerCase().trim();
        if (containsOnlyOneWord(interpretingCommand)) {
            listener.onFailureCommandRecognizing();
            return;
        } else if (tryRecognizeWeatherCommand(interpretingCommand)) {
            listener.onWeatherCommandRecognized(getLocationName(result));
        } else if (tryRecognizeTimeCommand(interpretingCommand)) {
            listener.onTimeCommandRecognized(getLocationName(result));
        } else if (tryRecognizeNewsCommand(interpretingCommand)) {
            listener.onNewsCommandRecognized();
        } else if (tryRecognizeShowCalendarCommand(interpretingCommand)){
            listener.onShowCalendarRecognized();
        } else {
            listener.onFailureCommandRecognizing();
        }
    }

    private boolean tryRecognizeShowCalendarCommand(String candidate) {
        return containsWordFromVocabulary(candidate, showVocabulary) && (containsWordFromVocabulary(candidate, calendarVocabulary));
    }

    private Boolean tryRecognizeWeatherCommand(String candidate) {
        return containsWordFromVocabulary(candidate, weatherVocabulary)
                && (containsWordFromVocabulary(candidate, cityVocabulary) || containsWordFromVocabulary(candidate, countryVocabulary));
    }

    private Boolean tryRecognizeTimeCommand(String candidate) {
        return containsWordFromVocabulary(candidate, timeVocabulary)
                && (containsWordFromVocabulary(candidate, cityVocabulary) || containsWordFromVocabulary(candidate, countryVocabulary));
    }

    private Boolean tryRecognizeNewsCommand(String candidate) {
        return containsWordFromVocabulary(candidate, newsVocabulary)
                && (containsWordFromVocabulary(candidate, showVocabulary));
    }

    private Boolean containsWordFromVocabulary(String candidate, ArrayList<String> vocabulary) {
        for (String item : vocabulary) {
            if (candidate.contains(item)) {
                return true;
            }
        }
        return false;
    }

    private Boolean containsOnlyOneWord(String candidate) {
        return candidate.split(" ").length == 1;
    }

    private String getLocationName(String command) {
        return command.substring(command.lastIndexOf(" ") + 1);
    }

}
