package example;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.theokanning.openai.completion.chat.ChatFunction;

/**
 * @author LiangTao
 * @date 2024年04月28 16:25
 **/
public class ToolUtil {
    public static ChatFunction weatherFunction() {
        return ChatFunction.builder()
                .name("get_weather")
                .description("Get the current weather in a given location")
                //这里的executor是一个lambda表达式,这个lambda表达式接受一个Weather对象,返回一个WeatherResponse对象
                .executor(Weather.class, w -> new WeatherResponse(w.location, w.unit, 25, "sunny"))
                .build();
    }

    public enum WeatherUnit {
        CELSIUS, FAHRENHEIT;
    }

    public static class City {
        @JsonPropertyDescription("The time to get the cities")
        public String time;
    }

    public static class Weather {
        @JsonPropertyDescription("City and state, for example: León, Guanajuato")
        public String location;

        @JsonPropertyDescription("The temperature unit, can be 'celsius' or 'fahrenheit'")
        @JsonProperty(required = true)
        public WeatherUnit unit;
    }

    public static class WeatherResponse {
        public String location;
        public WeatherUnit unit;
        public int temperature;
        public String description;

        public WeatherResponse(String location, WeatherUnit unit, int temperature, String description) {
            this.location = location;
            this.unit = unit;
            this.temperature = temperature;
            this.description = description;
        }
    }


}
