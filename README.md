# Description
This project contain a group of Azure functions that can be used together to manage a movie catalog.

## fnPostDataStorage
Save into Azure blob data that is sent in the form data for a POST request.

## fnPostDataBase
Save into Azure cosmos db the data for a movie.

## fnGetDetails
Get the movie data for the given id from the Azure cosmos db.

## fnGetAll
Get all movies from Azure cosmos db.


### Example movie object

```
{
  "id":"c1e8290f-5cb7-458c-8769-2666a4f4373d"
  "title": "Inception",
  "year": "2010",
  "video": "https://example.com/video/inception.mp4",
  "thumbnail": "https://example.com/thumbnails/inception.jpg"
}
```


