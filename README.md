# Currency Exchange Spring Boot Application

## Project Overview

Welcome to our Spring Boot Java project! This application serves as a robust platform for managing currency exchange operations effortlessly. It offers a versatile API enabling users to access real-time exchange rates and seamlessly execute currency conversions on-the-fly.

## Getting Started

To start using our currency exchange platform, follow these simple steps:

1. **Clone the Repository:** Obtain the project source code by cloning the repository to your local machine.

2. **Run Locally:** Start the application locally with the default profile.

Once you've completed these steps, you'll be ready to explore our currency exchange API and perform various operations seamlessly.

## Testing Endpoints

To test the functionality of our API, you can use the following endpoints:

1. **Convert Currency:**
   - Endpoint:
     ```http
     http://localhost:8721/api/currencies/convert?sourceCurrency=USD&targetCurrency=EUR&amount=100
     ```
   - Description: This endpoint allows you to convert a specific amount from one currency to another.
   - Example: To convert 100 USD to EUR, use the provided URL.

2. **List Supported Currencies:**
   - Endpoint:
     ```http
     http://localhost:8721/api/currencies
     ```
   - Description: This endpoint provides a list of supported currencies along with their codes.
   - Example: Use this endpoint to retrieve the list of supported currencies.
