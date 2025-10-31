package com.youtopin.vaadin.samples.application.tour;

import com.youtopin.vaadin.formengine.options.OptionItem;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Provides in-memory lookup data for the outbound tour wizard.
 */
@Service
public class TourReferenceDataService {

    private final LinkedHashMap<String, String> operators = new LinkedHashMap<>();
    private final LinkedHashMap<String, String> tourLeaders = new LinkedHashMap<>();
    private final LinkedHashMap<String, String> tourClasses = new LinkedHashMap<>();
    private final ConcurrentHashMap<String, String> tourTags = new ConcurrentHashMap<>();

    private final LinkedHashMap<String, String> provinces = new LinkedHashMap<>();
    private final LinkedHashMap<String, List<City>> citiesByProvince = new LinkedHashMap<>();

    private final LinkedHashMap<String, String> continents = new LinkedHashMap<>();
    private final LinkedHashMap<String, List<Country>> countriesByContinent = new LinkedHashMap<>();
    private final LinkedHashMap<String, List<City>> citiesByCountry = new LinkedHashMap<>();

    private final LinkedHashMap<String, String> excludedServices = new LinkedHashMap<>();
    private final LinkedHashMap<String, String> requiredDocuments = new LinkedHashMap<>();
    private final LinkedHashMap<String, String> requiredEquipment = new LinkedHashMap<>();
    private final LinkedHashMap<String, String> difficultyLevels = new LinkedHashMap<>();

    private final LinkedHashMap<String, String> accommodationTypes = new LinkedHashMap<>();
    private final LinkedHashMap<String, String> defaultAccommodations = new LinkedHashMap<>();
    private final LinkedHashMap<String, String> hotelQualities = new LinkedHashMap<>();
    private final LinkedHashMap<String, String> cateringServices = new LinkedHashMap<>();
    private final LinkedHashMap<String, String> amenityServices = new LinkedHashMap<>();
    private final LinkedHashMap<String, String> roomTypes = new LinkedHashMap<>();

    public TourReferenceDataService() {
        seedCoreData();
    }

    public List<OptionItem> searchOperators(String filter) {
        return filterMap(operators, filter);
    }

    public List<OptionItem> searchTourLeaders(String filter) {
        return filterMap(tourLeaders, filter);
    }

    public List<OptionItem> searchTourClasses(String filter) {
        return filterMap(tourClasses, filter);
    }

    public List<OptionItem> searchTags(String filter) {
        return filterMap(new LinkedHashMap<>(tourTags), filter);
    }

    public OptionItem registerTag(String label) {
        String sanitized = normalizeLabel(label);
        if (sanitized.isBlank()) {
            return null;
        }
        String id = slugify(sanitized);
        tourTags.putIfAbsent(id, sanitized);
        return new OptionItem(id, sanitized);
    }

    public List<OptionItem> searchProvinces(String filter) {
        return filterMap(provinces, filter);
    }

    public List<OptionItem> searchCitiesForProvince(String provinceId, String filter) {
        List<City> cities = citiesByProvince.getOrDefault(provinceId, List.of());
        return filterCities(cities, filter);
    }

    public List<OptionItem> searchAllProvinceCities(String filter) {
        return filterCities(citiesByProvince.values().stream()
                .flatMap(List::stream)
                .toList(), filter);
    }

    public List<OptionItem> searchContinents(String filter) {
        return filterMap(continents, filter);
    }

    public List<OptionItem> searchCountries(String continentId, String filter) {
        List<Country> countries = countriesByContinent.getOrDefault(continentId, List.of());
        return filterCountries(countries, filter);
    }

    public List<OptionItem> searchAllCountries(String filter) {
        return filterCountries(countriesByContinent.values().stream()
                .flatMap(List::stream)
                .toList(), filter);
    }

    public List<OptionItem> searchCitiesForCountry(String countryId, String filter) {
        List<City> cities = citiesByCountry.getOrDefault(countryId, List.of());
        return filterCities(cities, filter);
    }

    public List<OptionItem> searchAllWorldCities(String filter) {
        return filterCities(citiesByCountry.values().stream()
                .flatMap(List::stream)
                .toList(), filter);
    }

    public String resolveCityName(String cityId) {
        Optional<String> provinceMatch = citiesByProvince.values().stream()
                .flatMap(List::stream)
                .filter(city -> city.id().equals(cityId))
                .map(City::name)
                .findFirst();
        if (provinceMatch.isPresent()) {
            return provinceMatch.get();
        }
        return citiesByCountry.values().stream()
                .flatMap(List::stream)
                .filter(city -> city.id().equals(cityId))
                .map(City::name)
                .findFirst()
                .orElse("");
    }

    public List<OptionItem> searchExcludedServices(String filter) {
        return filterMap(excludedServices, filter);
    }

    public List<OptionItem> searchRequiredDocuments(String filter) {
        return filterMap(requiredDocuments, filter);
    }

    public List<OptionItem> searchRequiredEquipment(String filter) {
        return filterMap(requiredEquipment, filter);
    }

    public List<OptionItem> searchDifficultyLevels(String filter) {
        return filterMap(difficultyLevels, filter);
    }

    public List<OptionItem> searchAccommodationTypes(String filter) {
        return filterMap(accommodationTypes, filter);
    }

    public List<OptionItem> searchDefaultAccommodations(String filter) {
        return filterMap(defaultAccommodations, filter);
    }

    public List<OptionItem> searchHotelQualities(String filter) {
        return filterMap(hotelQualities, filter);
    }

    public List<OptionItem> searchCateringServices(String filter) {
        return filterMap(cateringServices, filter);
    }

    public List<OptionItem> searchAmenityServices(String filter) {
        return filterMap(amenityServices, filter);
    }

    public List<OptionItem> searchRoomTypes(String filter) {
        return filterMap(roomTypes, filter);
    }

    private void seedCoreData() {
        operators.put("ytp-travel", "یوتوپین تراول");
        operators.put("golden-sky", "آسمان طلایی");
        operators.put("iran-holiday", "ایران هالیدی");

        tourLeaders.put("leader-fatemeh", "فاطمه موسوی");
        tourLeaders.put("leader-alireza", "علیرضا احمدی");
        tourLeaders.put("leader-sara", "سارا رستگار");

        tourClasses.put("economy", "اقتصادی");
        tourClasses.put("standard-plus", "استاندارد پلاس");
        tourClasses.put("luxury", "لوکس");

        tourTags.put("adventure", "ماجراجویی");
        tourTags.put("family", "خانوادگی");
        tourTags.put("cultural", "فرهنگی");
        tourTags.put("honeymoon", "ماه عسل");

        provinces.put("tehran", "تهران");
        provinces.put("isfahan", "اصفهان");
        provinces.put("fars", "فارس");
        provinces.put("khorasan", "خراسان رضوی");

        citiesByProvince.put("tehran", List.of(
                new City("tehran-city", "تهران"),
                new City("rey", "ری"),
                new City("varamin", "ورامین")));
        citiesByProvince.put("isfahan", List.of(
                new City("isfahan-city", "اصفهان"),
                new City("kashan", "کاشان"),
                new City("najafabad", "نجف‌آباد")));
        citiesByProvince.put("fars", List.of(
                new City("shiraz", "شیراز"),
                new City("marvdasht", "مرودشت"),
                new City("jahrom", "جهرم")));
        citiesByProvince.put("khorasan", List.of(
                new City("mashhad", "مشهد"),
                new City("neyshabur", "نیشابور"),
                new City("sabzevar", "سبزوار")));

        continents.put("asia", "آسیا");
        continents.put("europe", "اروپا");
        continents.put("africa", "آفریقا");

        countriesByContinent.put("asia", List.of(
                new Country("iran", "ایران"),
                new Country("turkey", "ترکیه"),
                new Country("thailand", "تایلند")));
        countriesByContinent.put("europe", List.of(
                new Country("france", "فرانسه"),
                new Country("spain", "اسپانیا"),
                new Country("italy", "ایتالیا")));
        countriesByContinent.put("africa", List.of(
                new Country("egypt", "مصر"),
                new Country("morocco", "مراکش")));

        citiesByCountry.put("iran", List.of(
                new City("tehran-city", "تهران"),
                new City("isfahan-city", "اصفهان"),
                new City("shiraz", "شیراز")));
        citiesByCountry.put("turkey", List.of(
                new City("istanbul", "استانبول"),
                new City("antalya", "آنتالیا"),
                new City("ankara", "آنکارا")));
        citiesByCountry.put("thailand", List.of(
                new City("bangkok", "بانکوک"),
                new City("phuket", "پوکت"),
                new City("chiangmai", "چیانگ‌مای")));
        citiesByCountry.put("france", List.of(
                new City("paris", "پاریس"),
                new City("nice", "نیس"),
                new City("lyon", "لیون")));
        citiesByCountry.put("spain", List.of(
                new City("madrid", "مادرید"),
                new City("barcelona", "بارسلونا"),
                new City("sevilla", "سویا")));
        citiesByCountry.put("italy", List.of(
                new City("rome", "رم"),
                new City("venice", "ونیز"),
                new City("florence", "فلورانس")));
        citiesByCountry.put("egypt", List.of(
                new City("cairo", "قاهره"),
                new City("luxor", "لوکسور")));
        citiesByCountry.put("morocco", List.of(
                new City("marrakesh", "مراکش"),
                new City("fes", "فاس")));

        excludedServices.put("visa", "دریافت ویزا");
        excludedServices.put("insurance", "بیمه سفر");
        excludedServices.put("meals", "غذاهای خارج از برنامه");

        requiredDocuments.put("passport", "پاسپورت معتبر");
        requiredDocuments.put("idcard", "کارت ملی");
        requiredDocuments.put("visa-doc", "مدارک ویزا");

        requiredEquipment.put("comfortable-shoes", "کفش راحتی");
        requiredEquipment.put("adapter", "آداپتور برق");
        requiredEquipment.put("raincoat", "بارانی");

        difficultyLevels.put("hard", "سخت");
        difficultyLevels.put("medium", "متوسط");
        difficultyLevels.put("easy", "آسان");

        accommodationTypes.put("hotel", "هتل");
        accommodationTypes.put("boutique", "بوتیک");
        accommodationTypes.put("camp", "کمپ");

        defaultAccommodations.put("grand-hotel", "هتل گراند");
        defaultAccommodations.put("seaside-hotel", "هتل ساحلی");
        defaultAccommodations.put("royal-suites", "هتل رویال سوئیت");
        defaultAccommodations.put("ancient-boutique", "بوتیک هتل باستانی");
        defaultAccommodations.put("desert-camp", "کمپ کویری");

        hotelQualities.put("1-star", "۱ ستاره");
        hotelQualities.put("2-star", "۲ ستاره");
        hotelQualities.put("3-star", "۳ ستاره");
        hotelQualities.put("4-star", "۴ ستاره");
        hotelQualities.put("5-star", "۵ ستاره");

        cateringServices.put("breakfast", "صبحانه");
        cateringServices.put("half-board", "نیم‌پانسیون");
        cateringServices.put("full-board", "فول برد");
        cateringServices.put("all-inclusive", "آل اینکلوسیو");
        cateringServices.put("afternoon-tea", "چای عصرانه");
        cateringServices.put("midnight-snack", "میان وعده نیمه‌شب");

        amenityServices.put("wifi", "اینترنت بی‌سیم");
        amenityServices.put("spa", "اسپا");
        amenityServices.put("pool", "استخر");
        amenityServices.put("gym", "باشگاه ورزشی");
        amenityServices.put("airport-transfer", "ترنسفر فرودگاهی");
        amenityServices.put("kids-club", "کلاب کودکان");

        roomTypes.put("single", "سینگل");
        roomTypes.put("double", "دابل");
        roomTypes.put("superior", "سوپریور");
        roomTypes.put("deluxe", "دلوکس");
        roomTypes.put("twin", "توئین");
        roomTypes.put("triple", "تریپل");
        roomTypes.put("quad", "کوآد");
        roomTypes.put("studio", "استودیو");
        roomTypes.put("suite", "سوییت");
    }

    private List<OptionItem> filterMap(Map<String, String> source, String filter) {
        String normalized = normalizeFilter(filter);
        return source.entrySet().stream()
                .filter(entry -> normalized.isBlank()
                        || entry.getValue().toLowerCase(getLocale()).contains(normalized)
                        || entry.getKey().toLowerCase(getLocale()).contains(normalized))
                .map(entry -> new OptionItem(entry.getKey(), entry.getValue()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<OptionItem> filterCities(List<City> cities, String filter) {
        String normalized = normalizeFilter(filter);
        return cities.stream()
                .filter(city -> normalized.isBlank()
                        || city.name().toLowerCase(getLocale()).contains(normalized)
                        || city.id().toLowerCase(getLocale()).contains(normalized))
                .map(city -> new OptionItem(city.id(), city.name()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<OptionItem> filterCountries(List<Country> countries, String filter) {
        String normalized = normalizeFilter(filter);
        return countries.stream()
                .filter(country -> normalized.isBlank()
                        || country.name().toLowerCase(getLocale()).contains(normalized)
                        || country.id().toLowerCase(getLocale()).contains(normalized))
                .map(country -> new OptionItem(country.id(), country.name()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private String normalizeFilter(String filter) {
        if (filter == null || filter.isBlank()) {
            return "";
        }
        String normalized = Normalizer.normalize(filter, Normalizer.Form.NFKC);
        return normalized.trim().toLowerCase(getLocale());
    }

    private String normalizeLabel(String label) {
        if (label == null) {
            return "";
        }
        return Normalizer.normalize(label, Normalizer.Form.NFKC).trim();
    }

    private String slugify(String label) {
        String lower = label.toLowerCase(Locale.ROOT);
        String ascii = lower.replaceAll("[\u064B-\u065F]", "");
        ascii = ascii.replace('‌', '-');
        ascii = ascii.replaceAll("[^a-z0-9-]+", "-");
        ascii = ascii.replaceAll("-+", "-");
        return ascii.replaceAll("^-|-$", "");
    }

    private Locale getLocale() {
        return Locale.forLanguageTag("fa-IR");
    }

    private record City(String id, String name) {
    }

    private record Country(String id, String name) {
    }
}
