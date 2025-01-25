const WeatherIcon = ({ weather }) => {
    const weatherIcons = {
      sunny: (
        <path d="M12 8a4 4 0 1 1-8 0 4 4 0 0 1 8 0zM8 0a.5.5 0 0 1 .5.5v2a.5.5 0 0 1-1 0v-2A.5.5 0 0 1 8 0zm0 13a.5.5 0 0 1 .5.5v2a.5.5 0 0 1-1 0v-2a.5.5 0 0 1 .5-.5zm8-5a.5.5 0 0 1-.5.5h-2a.5.5 0 0 1 0-1h2a.5.5 0 0 1 .5.5zM3 8a.5.5 0 0 1-.5.5h-2a.5.5 0 0 1 0-1h2A.5.5 0 0 1 3 8z" />
      ),
      rainy: (
        <path d="M8 2a4 4 0 0 1 3.905 3.01 5.5 5.5 0 0 1 5.09 7.89 1.5 1.5 0 1 1-1.732 1 3.5 3.5 0 1 0-6.682-2H5.5a3.5 3.5 0 0 0-2.79 5.29A1.5 1.5 0 0 1 0 16a5.5 5.5 0 0 1 8-7.79 4 4 0 0 1 0-6.21z" />
      ),
      foggy: (
        <path d="M5.5 6a1.5 1.5 0 1 1 0-3h5a1.5 1.5 0 1 1 0 3h-5zM3.5 9a1.5 1.5 0 1 1 0-3h9a1.5 1.5 0 1 1 0 3h-9zM2 12a1.5 1.5 0 1 1 0-3h11a1.5 1.5 0 1 1 0 3H2z" />
      ),
      windy: (
        <path d="M7 4a2 2 0 0 1 1.994 1.851L9 6h4a1 1 0 0 1 .117 1.993L13 8H8a1 1 0 0 1-.117-1.993L8 6h1.007A2 2 0 0 1 7 4zm-4 3h8a2 2 0 0 1 1.994 1.851L13 9h-1.993A2 2 0 0 1 11 9.01h-2.001A2 2 0 0 1 5 9H3.007A2 2 0 0 1 3 7zM1 13h11a1 1 0 0 1 .117 1.993L12 15H1a1 1 0 0 1-.117-1.993L1 13z" />
      ),
    };
  
    return (
      <svg
        xmlns="http://www.w3.org/2000/svg"
        width="46"
        height="46"
        fill="currentColor"
        viewBox="0 0 16 16"
      >
        {weatherIcons[weather] || weatherIcons.sunny}
      </svg>
    );
  };
  
  export default WeatherIcon;
  