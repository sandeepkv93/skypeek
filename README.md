# SkyPeek 🌤️

A modern, feature-rich Android weather application built with Jetpack Compose, offering comprehensive weather information through both an intuitive mobile app and customizable home screen widgets.

## 📱 Features

### Main Application

- **Real-time Weather Data**: Current conditions, hourly forecasts, and 10-day forecasts
- **Multiple Location Support**: Add and manage multiple locations with swipe navigation
- **Location Services**: Automatic current location detection with GPS integration
- **Modern UI**: Built with Jetpack Compose and Material Design 3
- **Dark/Light Theme**: Automatic theme switching based on system preferences
- **Offline Support**: Local caching with Room database for offline access
- **Pull-to-Refresh**: Intuitive refresh mechanism for updated weather data

### Home Screen Widgets

- **4x1 Compact Widget**: Essential weather info in minimal space
- **4x2 Detailed Widget**: Current weather with hourly forecast
- **5x1 Extended Widget**: Current conditions with 3-hour forecast timeline
- **5x2 Comprehensive Widget**: Full weather overview with 6-hour forecast and daily summary

### Weather Information

- Current temperature and "feels like" temperature
- Weather conditions with descriptive icons
- High/low temperatures for the day
- Humidity, wind speed, and visibility
- UV index and atmospheric pressure
- Sunrise and sunset times
- Hourly forecasts with temperature trends
- 10-day extended forecast

## 🏗️ Architecture

SkyPeek follows **Clean Architecture** principles with **MVVM** pattern, ensuring maintainable, testable, and scalable code.

### Architecture Layers

```
┌─────────────────────────────────────┐
│           Presentation Layer        │
│  ┌─────────────┐ ┌─────────────────┐│
│  │   Compose   │ │    ViewModels   ││
│  │     UI      │ │                 ││
│  └─────────────┘ └─────────────────┘│
└─────────────────────────────────────┘
┌─────────────────────────────────────┐
│            Domain Layer             │
│  ┌─────────────┐ ┌─────────────────┐│
│  │ Use Cases   │ │   Repositories  ││
│  │             │ │  (Interfaces)   ││
│  └─────────────┘ └─────────────────┘│
└─────────────────────────────────────┘
┌─────────────────────────────────────┐
│             Data Layer              │
│  ┌─────────────┐ ┌─────────────────┐│
│  │   Remote    │ │      Local      ││
│  │ Data Source │ │  Data Source    ││
│  │   (APIs)    │ │   (Database)    ││
│  └─────────────┘ └─────────────────┘│
└─────────────────────────────────────┘
```

### Key Components

#### Presentation Layer (`presentation/`)

- **MainActivity**: Main entry point with navigation and permission handling
- **ViewModels**: State management with reactive data flows
- **Compose UI**: Modern declarative UI components
- **Widgets**: Home screen widget providers and services

#### Domain Layer (`domain/`)

- **Models**: Core business entities (`WeatherData`, `LocationData`)
- **Repository Interfaces**: Contracts for data access
- **Use Cases**: Business logic encapsulation

#### Data Layer (`data/`)

- **Remote**: API services and DTOs for weather data
- **Local**: Room database for caching and offline support
- **Repository Implementations**: Data source coordination

## 🛠️ Technology Stack

### Core Technologies

- **Kotlin**: 100% Kotlin codebase
- **Jetpack Compose**: Modern declarative UI toolkit
- **Material Design 3**: Latest Material Design components
- **Coroutines**: Asynchronous programming and reactive streams

### Architecture Components

- **Hilt**: Dependency injection framework
- **Room**: Local database for caching
- **DataStore**: Preferences and settings storage
- **WorkManager**: Background task scheduling
- **Navigation Compose**: Type-safe navigation

### Networking & APIs

- **Retrofit**: HTTP client for API communication
- **OkHttp**: Network layer with logging and caching
- **Gson**: JSON serialization/deserialization

### Location Services

- **Google Play Services**: Location and geocoding services
- **FusedLocationProviderClient**: Efficient location updates

### Testing

- **JUnit**: Unit testing framework
- **Mockito**: Mocking framework for tests
- **Coroutines Test**: Testing coroutines and flows
- **Compose Test**: UI testing for Compose components

## 🌐 Weather Data Sources

SkyPeek uses multiple weather APIs for reliability and comprehensive data:

### Primary: Open-Meteo API

- **Free**: No API key required
- **Comprehensive**: Current, hourly, and daily forecasts
- **Reliable**: High uptime and accuracy
- **Features**: Temperature, weather codes, wind, humidity, pressure

### Secondary: WeatherAPI.com

- **Free Tier**: 1M calls/month
- **Rich Data**: Air quality, alerts, astronomy data
- **Global Coverage**: Worldwide weather information

### Fallback: OpenWeatherMap

- **Free Tier**: 1K calls/day
- **Established**: Reliable fallback service
- **Standard**: Industry-standard weather data

## 📦 Installation & Setup

### Prerequisites

- Android Studio Arctic Fox or later
- Android SDK 26+ (Android 8.0)
- Kotlin 1.8+

### Setup Instructions

1. **Clone the Repository**

   ```bash
   git clone https://github.com/sandeepkv93/skypeek.git
   cd skypeek
   ```

2. **Configure API Keys** (Optional)

   Create or edit `local.properties` file:

   ```properties
   # WeatherAPI.com (Optional - 1M calls/month free)
   WEATHER_API_KEY=your_weatherapi_key_here

   # OpenWeatherMap (Optional - 1K calls/day free)
   OPENWEATHER_API_KEY=your_openweather_key_here
   ```

   **Note**: The app works without API keys using the free Open-Meteo service.

3. **Build and Run**

   ```bash
   ./gradlew assembleDebug
   ```

4. **Install on Device**
   ```bash
   ./gradlew installDebug
   ```

### API Key Setup (Optional)

#### WeatherAPI.com

1. Visit [WeatherAPI.com](https://www.weatherapi.com/)
2. Sign up for a free account
3. Get your API key from the dashboard
4. Add to `local.properties`: `WEATHER_API_KEY=your_key_here`

#### OpenWeatherMap

1. Visit [OpenWeatherMap](https://openweathermap.org/api)
2. Sign up for a free account
3. Get your API key from the dashboard
4. Add to `local.properties`: `OPENWEATHER_API_KEY=your_key_here`

## 🎨 UI/UX Design

### Design Principles

- **Material Design 3**: Latest design system with dynamic theming
- **Accessibility**: Full accessibility support with content descriptions
- **Responsive**: Adaptive layouts for different screen sizes
- **Intuitive**: User-friendly navigation and interactions

### Key UI Components

- **Weather Cards**: Clean, informative weather displays
- **Horizontal Pager**: Smooth navigation between locations
- **Bottom Sheets**: Contextual actions and settings
- **Pull-to-Refresh**: Intuitive data refresh mechanism
- **Loading States**: Smooth loading animations and skeleton screens

### Widget Design

- **Adaptive Backgrounds**: Dynamic backgrounds based on weather conditions
- **Readable Typography**: High contrast text for outdoor visibility
- **Touch Targets**: Appropriately sized interactive elements
- **Information Hierarchy**: Clear visual hierarchy for quick scanning

## 🔧 Configuration

### Build Variants

- **Debug**: Development build with debugging enabled
- **Release**: Production build with ProGuard optimization

### Permissions

```xml
<!-- Essential Permissions -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<!-- Location Permissions -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

<!-- Widget Permissions -->
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
```

### ProGuard Configuration

The app includes optimized ProGuard rules for release builds:

- Code obfuscation and minification
- Resource shrinking
- Unused code elimination

## 🧪 Testing

### Test Structure

```
src/
├── test/                    # Unit tests
│   ├── domain/             # Domain layer tests
│   ├── data/               # Data layer tests
│   └── presentation/       # ViewModel tests
└── androidTest/            # Integration tests
    ├── ui/                 # UI tests
    └── database/           # Database tests
```

### Running Tests

```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest

# All tests
./gradlew check
```

### Test Coverage

- **Unit Tests**: ViewModels, repositories, use cases
- **Integration Tests**: Database operations, API calls
- **UI Tests**: Compose components and user interactions

## 🚀 Performance Optimizations

### Memory Management

- **Efficient Image Loading**: Coil for optimized image caching
- **Database Optimization**: Room with efficient queries
- **Memory Leaks**: Proper lifecycle management and coroutine scoping

### Network Optimization

- **Caching**: HTTP caching with OkHttp
- **Request Deduplication**: Avoiding duplicate API calls
- **Offline Support**: Local data persistence

### Battery Optimization

- **Background Tasks**: Efficient WorkManager scheduling
- **Location Updates**: Optimized location request intervals
- **Widget Updates**: Smart update frequency based on user interaction

## 📱 Widget Implementation

### Widget Types

#### 4x1 Compact Widget

- **Size**: 4 cells wide × 1 cell tall
- **Content**: City name, temperature, weather icon, condition
- **Use Case**: Minimal space, essential information

#### 4x2 Detailed Widget

- **Size**: 4 cells wide × 2 cells tall
- **Content**: Current weather + 3-hour forecast
- **Features**: High/low temperatures, detailed conditions

#### 5x1 Extended Widget

- **Size**: 5 cells wide × 1 cell tall
- **Content**: Current weather + hourly timeline
- **Features**: 3-hour forecast with temperature trend

#### 5x2 Comprehensive Widget

- **Size**: 5 cells wide × 2 cells tall
- **Content**: Full weather overview
- **Features**: 6-hour forecast, tomorrow's weather, sunrise/sunset

### Widget Features

- **Auto-refresh**: Configurable update intervals
- **Location Sync**: Automatic location updates
- **Interactive Elements**: Tap to open app or refresh
- **Dynamic Backgrounds**: Weather-appropriate themes
- **Error Handling**: Graceful error states and retry mechanisms

## 🔒 Security & Privacy

### Data Privacy

- **Location Data**: Used only for weather services, not stored permanently
- **API Keys**: Securely stored in build configuration
- **User Data**: Minimal data collection, no personal information tracking

### Security Measures

- **HTTPS Only**: All network communication encrypted
- **Certificate Pinning**: Enhanced security for API communications
- **Input Validation**: Proper validation of user inputs and API responses

## 🌍 Localization

### Supported Features

- **String Resources**: Externalized strings for easy translation
- **RTL Support**: Right-to-left language support
- **Date/Time Formatting**: Locale-appropriate formatting
- **Temperature Units**: Celsius/Fahrenheit based on locale

### Adding New Languages

1. Create new `values-{language}/strings.xml`
2. Translate all string resources
3. Test with different locales
4. Update documentation

## 🤝 Contributing

### Development Setup

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Follow the coding standards and architecture patterns
4. Write tests for new functionality
5. Submit a pull request

### Coding Standards

- **Kotlin Style Guide**: Follow official Kotlin conventions
- **Architecture**: Maintain clean architecture principles
- **Documentation**: Document public APIs and complex logic
- **Testing**: Write tests for new features and bug fixes

### Code Review Process

- All changes require code review
- Automated tests must pass
- Follow the established architecture patterns
- Maintain backward compatibility

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **Open-Meteo**: Free weather API service
- **Material Design**: Google's design system
- **Jetpack Compose**: Modern Android UI toolkit
- **Weather Icons**: Custom weather icon designs
- **Community**: Open source contributors and testers

**SkyPeek** - Your window to the sky 🌤️