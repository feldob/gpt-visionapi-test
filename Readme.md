# OpenAI Image Test

This project provides a service to check if a specific object exists in an image using the OpenAI GPT-4 API. The class `ExistsInImage` is designed to be instantiated with the name of the object to find (e.g., "bike", "apple"), and provides a method `check` that takes the path of an image file, uploads it to the OpenAI API, and checks if the specified object exists in the image.

## Prerequisites

- Java 8 or higher
- Maven 3.6.0 or higher
- An OpenAI API key stored in a file named `openapi.key` in your home directory.

## Building the Project

To build the project and create a FAT JAR, run the following command in the root directory of the project:

```sh
mvn clean install
```

This will generate a JAR file named `exists.jar` in the `target` directory.

## Running the Project

To run the project, use the following command:

```sh
java -jar target/exists.jar
```

This will execute the main method in the `ExistsInImage` class, which checks if a bike exists in the image located at `~/surprise.jpg`.

## Example Usage

```java
public static void main(String[] args) {
    ExistsInImage existsInImage = new ExistsInImage("bike");
    boolean result = existsInImage.check(System.getProperty("user.home") + "/surprise.jpg");
    System.out.println("Does the image contain a bike? " + result);
}
```

## Dependencies

The project uses the following dependencies:

- `okhttp` for making HTTP requests
- `jackson-databind` for JSON processing
- `commons-io` for IO utilities

These dependencies are managed via Maven and are specified in the `pom.xml` file.
